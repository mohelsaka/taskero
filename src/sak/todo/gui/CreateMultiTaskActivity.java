package sak.todo.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import sak.todo.database.Task;
import sak.todo.database.TasksIterator;
import sak.todo.gui.schedules.SchedulesActivity;
import taskero.learner.Preference_Learner;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeSlider;
import com.learner.svm.SVMAdapter;

import cr.Constraint;
import cr.Interval;
import cr.Reasoner;
import cr.STPNotConnsistentException;

public class CreateMultiTaskActivity extends Activity {
	int i = 1;
	LinearLayout listofAddedTasks;
	LinearLayout listofConstraintBefore;
	LinearLayout listofConstraintAfter;
	ArrayList<Task> addedTasks;
	public static final String PREFS_NAME = "MyPrefsFile";
	// ArrayList<Constraint> constraints;
	public SharedPreferences sharedPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_multi_task);
		
		bodyView = (EditText) this.findViewById(R.id.ScrollViewParent).findViewById(R.id.taskBody);
		priorityView = (RatingBar) this.findViewById(R.id.ScrollViewParent).findViewById(R.id.ratingBar1);
		estimateView = (EditText) this.findViewById(R.id.ScrollViewParent).findViewById(R.id.editText1);
		deadLineView = (Button) this.findViewById(R.id.ScrollViewParent).findViewById(R.id.deadline);
		dueDateView = (Button) this.findViewById(R.id.ScrollViewParent).findViewById(R.id.duedate);

		sharedPrefs = getSharedPreferences(PREFS_NAME, 0);

		if (!sharedPrefs.contains("numberOfRuns"))
			sharedPrefs.edit().putInt("numberOfRuns", 0).commit();

		listofAddedTasks = ((LinearLayout) findViewById(R.id.ListofAddedTasks));
		listofConstraintBefore = ((LinearLayout) findViewById(R.id.ConstraintLayoutBefore));
		listofConstraintAfter = ((LinearLayout) findViewById(R.id.ConstraintLayoutAfter));
		
		TextView taskBefore = (TextView) LayoutInflater.from(
				CreateMultiTaskActivity.this).inflate(R.drawable.task,
				null);
		TextView taskAfter = (TextView) LayoutInflater.from(
				CreateMultiTaskActivity.this).inflate(R.drawable.task,
				null);
		
		taskBefore.setBackgroundResource(R.drawable.shape_clicked);
		taskAfter.setBackgroundResource(R.drawable.shape_clicked);
		taskBefore.setText(" ADD ");
		taskAfter.setText(" ADD ");
		
		listofConstraintBefore.addView(taskBefore);
		listofConstraintAfter.addView(taskAfter);

		taskBefore.setOnDragListener(new constraintDragListner(true));
		taskAfter.setOnDragListener(new constraintDragListner(false));

		addedTasks = new ArrayList<Task>();
		addedTasks.add(Task.NULLTASK);
		parentTask = new Task();
		
		findViewById(R.id.SaveAll).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d("debug", "Save alll ........... ");

				TasksIterator itr = Task.getScheduledTasks(new Date(System.currentTimeMillis()), new Date(Reasoner.INFINITY));
				Task task = itr.nextTask();
				while (task != null) {
					addedTasks.add(task);
					task = itr.nextTask();
				}

				Reasoner reasoner = Reasoner.instance();

				ArrayList<ArrayList<Task>> assignments;
				try {
					assignments = reasoner.schedule(addedTasks);

					SVMAdapter svmAdapter;
					try {
						Log.d("assignments length", assignments.size() + "");
						svmAdapter = new SVMAdapter(getApplicationContext());

						int numOfRuns = sharedPrefs.getInt("numberOfRuns", 0);
						Preference_Learner pl = Preference_Learner.getInstance(assignments, svmAdapter, numOfRuns);
						try {
							svmAdapter.init();
							assignments = pl.rank(pl.setcalenderFeatVector(assignments));

						} catch (IOException e) {
							e.printStackTrace();
						}

					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}

					for (int i = 0; i < assignments.size(); i++) {
						Log.d("debug", "assignment: " + i);
						ArrayList<Task> assignment = assignments.get(i);
						for (int j = 0; j < assignment.size(); j++) {
							Task t = assignment.get(j);
							Log.d("debug", "Task: " + t.getStartDate());
						}
						Log.d("debug", "-----------------------");
					}
					
					Intent intent = new Intent(CreateMultiTaskActivity.this, SchedulesActivity.class);
					intent.putExtra("assignments", assignments);

					startActivity(intent);

				} catch (STPNotConnsistentException e) {
					Toast.makeText(getApplicationContext(),
							"Constraints you entered are not consistent",
							Toast.LENGTH_LONG).show();
				}
			}
		});
		findViewById(R.id.AddTask).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				final TextView task = (TextView) LayoutInflater.from(
						CreateMultiTaskActivity.this).inflate(
						R.drawable.task, null);

				// task.setText("Task"+(i++));

				Log.d("debug", task.toString());
				//
				String body = bodyView.getText().toString();
				task.setText(body);
				int priority = (int) (priorityView.getRating() * 2);
				String _estimate = estimateView.getText().toString();

				if (body.isEmpty()) {
					Toast.makeText(CreateMultiTaskActivity.this,
							"Please write a body for task!", Toast.LENGTH_LONG)
							.show();
					return;
				}

				if (_estimate.isEmpty()) {
					Toast.makeText(
							CreateMultiTaskActivity.this,
							"Please choose estimate duration of task in hours!",
							Toast.LENGTH_LONG).show();
					return;
				}
				if (priority == 0) {
					Toast.makeText(CreateMultiTaskActivity.this,
							"Please indicate priority to task!",
							Toast.LENGTH_LONG).show();
					return;
				}

				if (parentTask.deadline == null) {
					Toast.makeText(CreateMultiTaskActivity.this,
							"Please select task deadline!", Toast.LENGTH_LONG)
							.show();
					return;
				}
				parentTask.estimate = Float.parseFloat(_estimate);
				parentTask.body = body;
				parentTask.priority = priority;
				Task.NULLTASK.addAfter(
						parentTask,
						new Interval(
								System.currentTimeMillis(),
								(long) (parentTask.deadline.getTime() - parentTask.estimate * 60 * 60 * 1000)));
				parentTask.save();
				listofAddedTasks.addView(task);
				listofAddedTasks.addView(new TextView(
						CreateMultiTaskActivity.this));
				addedTasks.add(parentTask);
				parentTask = new Task();
				bodyView.setText("");
				priorityView.setRating(0);

				estimateView.setText("");
				for (int i = 1; i < listofConstraintAfter.getChildCount() - 1; i++) {
					listofConstraintAfter.removeViewAt(i + 1);
				}

				for (int i = 1; i < listofConstraintBefore.getChildCount() - 1; i++) {
					listofConstraintBefore.removeViewAt(i + 1);
				}
				dateText.setText("Select date and time");
				parentTask = new Task();
				Log.d("debug", "task details " + parentTask.body + " , "
						+ parentTask.priority + " , " + parentTask.estimate
						+ " , " + parentTask.deadline);
				Toast.makeText(CreateMultiTaskActivity.this,
						"Saved successfuly.", Toast.LENGTH_SHORT).show();

				task.setOnTouchListener(new OnTouchListener() {

					public boolean onTouch(View v, MotionEvent event) {

						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							ClipData data = ClipData.newPlainText("", "");

							DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
									v);
							v.startDrag(data, shadowBuilder, v, 0);
							// v.setVisibility(View.INVISIBLE);
							v.setBackgroundResource(R.drawable.shape_clicked);
							return true;
						} else {
							return false;
						}
					}
				});
				task.setOnDragListener(new OnDragListener() {

					public boolean onDrag(View v, DragEvent event) {
						// TODO Auto-generated method stub

						int action = event.getAction();
						switch (event.getAction()) {
						case DragEvent.ACTION_DRAG_STARTED:
							// Do nothing
							break;
						case DragEvent.ACTION_DRAG_ENTERED:

							// v.setBackgroundResource(R.drawable.shape_clicked);
							break;
						case DragEvent.ACTION_DRAG_EXITED:
							// v.setBackgroundResource(R.drawable.shape_clicked);

							break;
						case DragEvent.ACTION_DROP:
							// Dropped, reassign View to ViewGroup
							//
							v.setBackgroundResource(R.drawable.shape);
							// ((TableLayout)findViewById(R.id.ListofAddedTasks)).removeView(task);
							// ((TableLayout)findViewById(R.id.ListofAddedTasks)).addView(task);

							break;
						case DragEvent.ACTION_DRAG_ENDED:
							v.setBackgroundResource(R.drawable.shape);
						default:
							break;
						}
						return true;
					}
				});
			}
		});
	}

	class constraintDragListner implements OnDragListener {

		boolean before = false;

		public constraintDragListner(boolean b) {
			// TODO Auto-generated constructor stub
			before = b;
		}

		public boolean onDrag(View v, DragEvent event) {
			// TODO Auto-generated method stub
			int action = event.getAction();
			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				// Do nothing
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				v.setBackgroundResource(R.drawable.shape);
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				v.setBackgroundResource(R.drawable.shape_clicked);
				break;
			case DragEvent.ACTION_DROP:
				// Dropped, reassign View to ViewGroup
				// ((LinearLayout)v.getParent()).addView(child)
				View view = (View) event.getLocalState();
				TextView task = (TextView) LayoutInflater.from(
						CreateMultiTaskActivity.this).inflate(
						R.drawable.task, null);
				;
				task.setText(((TextView) view).getText());

				LinearLayout container = (LinearLayout) v.getParent();
				TextView t = new TextView(CreateMultiTaskActivity.this);
				t.setText("  ");
				container.addView(t);
				container.addView(task);

				view.setVisibility(View.VISIBLE);

				// constraint creation:

				Task t1 = null;
				for (int i = 1; i < addedTasks.size(); i++) {
					if (addedTasks.get(i).body.equals(task.getText() + "")) {
						t1 = addedTasks.get(i);
						Log.d("debug", "Constraint on drag task body: "
								+ t1.body);
					}
				}

				if (before) {
					parentTask.addAfter(t1, new Interval(0, Reasoner.INFINITY));
				} else {
					parentTask
							.addBefore(t1, new Interval(0, Reasoner.INFINITY));
				}

				break;
			case DragEvent.ACTION_DRAG_ENDED:
				v.setBackgroundResource(R.drawable.shape_clicked);
			default:
				break;
			}
			return true;
		}

	}

	class taskTouchListner implements OnTouchListener {

		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	class taskDragListner implements OnDragListener {

		public boolean onDrag(View v, DragEvent event) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	private Task parentTask;
	private EditText bodyView;
	private RatingBar priorityView;
	private EditText estimateView;
	private Button deadLineView;
	private Button dueDateView;
	private Button dateText;

	private final DateSlider.OnDateSetListener mDateTimeSetListener = new DateSlider.OnDateSetListener() {
		public void onDateSet(DateSlider view, Calendar selectedDate) {
			// update the dateText view with the corresponding date
			final int minute = (selectedDate.get(Calendar.MINUTE) / DateTimeSlider.MINUTEINTERVAL)
					* DateTimeSlider.MINUTEINTERVAL;
			dateText.setText(String.format("%te. %tB %tY  %tH:%02d",
					selectedDate, selectedDate, selectedDate, selectedDate,
					minute));

			selectedDate.set(Calendar.MINUTE, minute);
			selectedDate.set(Calendar.SECOND, 0);
			// updating task object's dates
			if (dateText.getId() == R.id.deadline)
				parentTask.deadline = selectedDate.getTime();
			else
				parentTask.duedate = selectedDate.getTime();

		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		// this method is called after invoking 'showDialog' for the first time
		// here we initiate the corresponding DateSlideSelector and return the
		// dialog to its caller

		// get todays date and the time
		if (id == DateSlider.DATETIMESELECTOR_ID) {
			final Calendar c = Calendar.getInstance();
			return new DateTimeSlider(this, mDateTimeSetListener, c);
		}
		return null;
	}

	public void showDateDialog(View view) {
		dateText = (Button) view;
		showDialog(DateSlider.DATETIMESELECTOR_ID);
	}

}
