package sak.todo.gui;

import java.util.ArrayList;
import java.util.Calendar;


import sak.todo.database.Task;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeSlider;

public class CreateTaskActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_crud);

		// getting holder of task information
		bodyView = (EditText) findViewById(R.id.taskBody);
		priorityView = (RatingBar) findViewById(R.id.ratingBar1);
		impressionyView = (RatingBar) findViewById(R.id.ratingBar2);
		estimateView = (EditText) findViewById(R.id.editText1);
		deadLineView = (Button) findViewById(R.id.deadline);
		dueDateView = (Button) findViewById(R.id.duedate);

		// Retrieving task id data id exist
		long task_id = -1;
		Intent i = getIntent();
		if (i.hasExtra("task_id")) {
			task_id = i.getExtras().getLong("task_id");
		}

		if (task_id > -1) {
			task = Task.findById(task_id);
			bodyView.setText(task.body);
			priorityView.setRating(((float) task.priority) / 2);
			impressionyView.setRating((task.impression)/2);
			estimateView.setText((float)(task.estimate/60f) + "");
			if (task.deadline != null)
				deadLineView.setText(task.deadline.toLocaleString());

			if (task.duedate != null)
				dueDateView.setText(task.duedate.toLocaleString());

		} else {
			task = new Task();
		}

	}

	private EditText bodyView;
	private RatingBar priorityView;
	private RatingBar impressionyView;
	private EditText estimateView;
	private Button deadLineView;
	private Button dueDateView;

	private Button dateText;

	private Task task;

	public void showDateDialog(View view) {
		dateText = (Button) view;
		showDialog(DateSlider.DATETIMESELECTOR_ID);
	}

	private final DateSlider.OnDateSetListener mDateTimeSetListener = new DateSlider.OnDateSetListener() {
		public void onDateSet(DateSlider view, Calendar selectedDate) {
			// update the dateText view with the corresponding date
			final int minute = (selectedDate.get(Calendar.MINUTE) / DateTimeSlider.MINUTEINTERVAL)
					* DateTimeSlider.MINUTEINTERVAL;
			dateText.setText(String.format("%te. %tB %tY  %tH:%02d",
					selectedDate, selectedDate, selectedDate, selectedDate, minute));

			selectedDate.set(Calendar.MINUTE, minute);
			selectedDate.set(Calendar.SECOND, 0);
			// updating task object's dates
			if (dateText.getId() == R.id.deadline)
				task.deadline = selectedDate.getTime();
			else
				task.duedate = selectedDate.getTime();

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

	public void cancel(View view) {
		this.finish();
	}

	public void saveTask(View view) {
		task.body = bodyView.getText().toString();
		if (task.body.isEmpty()) {
			Toast.makeText(this, "task body is empty", Toast.LENGTH_LONG)
					.show();
			return;
		}
		task.priority = (int) (priorityView.getRating() * 2);
		task.impression = (int) (impressionyView.getRating()*2);
		// duedate and deadline are being edited ate modfying time
		String _estimate = estimateView.getText().toString();

		task.estimate = Float.parseFloat(_estimate)*60f; // estimate in minutes

		if (task.priority == 0) {
			Toast.makeText(this, "Please indicate priority to task!",
					Toast.LENGTH_LONG).show();
			return;
		}
		if (task.impression == 0) {
			Toast.makeText(this, "Please indicate your impression about task!",
					Toast.LENGTH_LONG).show();
			return;
		}
		if (task.deadline == null) {
			Toast.makeText(this, "Please select task deadline!",
					Toast.LENGTH_LONG).show();
			return;
		}
		if (_estimate.isEmpty()) {
			Toast.makeText(this,
					"Please choose estimate duration of task in hours!",
					Toast.LENGTH_LONG).show();
			return;
		}
		Calendar c=Calendar.getInstance();
		if(task.duedate!=null){
			c.setTime(task.duedate);
			
		}
		c.add(Calendar.MINUTE, (int)task.estimate);
		Log.d("debug", c.getTime()+"");
		Log.d("debug", task.deadline+"");
		// saving the task
		if (c.getTime().after(task.deadline)){
			Toast.makeText(this, "Cannot accomplish TASK before deadline!",
					Toast.LENGTH_LONG).show();
			return;
		}
		else if(task.deadline.before(c.getTime())){
			Toast.makeText(this, "How come to Create TASK before Now!!",
					Toast.LENGTH_LONG).show();
			return;
		}
		Log.d("debug", task.toString());
		task.save();

		Toast.makeText(this, "task saved", Toast.LENGTH_LONG).show();
		
		ArrayList<Integer> list=fillPreferences();
		
		
		this.finish();
	}

	private ArrayList<Integer> fillPreferences() {
		// TODO Auto-generated method stub
		ArrayList<Integer>result=new ArrayList<Integer>();
		// later this part will be generalized 
		result.add(task.impression);
		result.add(task.priority);
		return result;
	}
}
