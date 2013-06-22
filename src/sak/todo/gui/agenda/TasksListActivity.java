package sak.todo.gui.agenda;

import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import sak.todo.database.DBHelper;
import sak.todo.database.Task;
import sak.todo.database.TasksIterator;
import sak.todo.gcm.GCMUtilities;
import sak.todo.gui.CreateMeeting;
import sak.todo.gui.CreateMultiTaskActivity;
import sak.todo.gui.R;
import android.app.ActionBar;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class TasksListActivity extends ListActivity implements TabListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// requesting action bar feature
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		
	    setContentView(R.layout.tasks_list);
	    
	    initilaizeHelpers();

//		deleteAllTasks();
//		createSomeTasks();
//		createSomeTasks();
//		updatePrioirties();
		
		Cursor cursor = Task.getAllTasksPointedAtToday();
		int position = cursor.getPosition();
		
		TasksAgendaAdapter dataAdapter = new TasksAgendaAdapter(this, cursor, false);

		getListView().setAdapter(dataAdapter);
		getListView().setSelection(position);
	}
	
	private void initilaizeHelpers(){
		DBHelper.initialize(this);
		GCMUtilities.initialize(this);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Cursor cursor = Task.getAllTasksPointedAtToday();
		int position = cursor.getPosition();
		
		TasksAgendaAdapter dataAdapter = new TasksAgendaAdapter(this, cursor, false);

		getListView().setAdapter(dataAdapter);
		getListView().setSelection(position);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.creat_multitask_item:
	        	Intent i = new Intent(this, CreateMultiTaskActivity.class);
	        	startActivity(i);
	            return true;
	        case R.id.create_meeting:
	        	Intent j = new Intent(this, CreateMeeting.class);
	        	startActivity(j);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void updatePrioirties(){
		TasksIterator it = Task.getAllTasks();
		Task t = null;
		Random r = new Random();
		while ((t = it.nextTask()) != null) {
			t.priority = Math.abs(r.nextInt()) % 10;
			t.save();
		}
	}
	
	private void createSomeTasks(){
		long day = 1000 * 60 * 60 * 24;
		
		Task t = new Task();
		t.duedate = new Date(System.currentTimeMillis() - day);
		t.estimate = 3.5f;
		t.body = "this task is to solve AI sheet DR Marwan bta3 doctor marwan!";
		t.save();
		
		t = new Task();
		t.duedate = new Date(System.currentTimeMillis() - 2 * day);
		t.estimate = 1f;
		t.body = "Solving AI sheet tany";
		t.save();
		
		t = new Task();
		t.duedate = new Date(System.currentTimeMillis());
		t.estimate = 1.25f;
		t.body = "Solving AI sheet we talet";
		t.save();
		
		t = new Task();
		t.duedate = new Date(System.currentTimeMillis() + 2 * day);
		t.estimate = 1.5f;
		t.body = "Solving AI sheet we rabe3";
		t.save();
		
		t = new Task();
		t.duedate = new Date(System.currentTimeMillis() + day);
		t.estimate = 0.5f;
		t.body = "Solving AI sheet mafeesh 3'er omoh dah!";
		t.save();
		
		t = new Task();
		t.duedate = new Date(System.currentTimeMillis() + 3 * day);
		t.estimate = 1.0f;
		t.body = "Solving CV sheet";
		t.save();
		
		t = new Task();
		t.duedate = new Date(System.currentTimeMillis() + 4 * day);
		t.estimate = 2.5f;
		t.body = "Solving FRE sheet";
		t.save();
	}
	
	private void deleteAllTasks(){
		TasksIterator it = Task.getAllTasks();
		Task t = null;
		while ((t = it.nextTask()) != null) {
			t.delete();
		}
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
	int selectedTaskIndex = 0;
	// array contains list of actions that defined on the task when dialog is show
	public static final CharSequence[] TASKS_ACTIONS = new CharSequence[] {"Edit", "Delete", "Reschedule"};
	Runnable[] tasksActions = new Runnable[]{
			new Runnable() {
				public void run() {
					Toast.makeText(TasksListActivity.this, "" + selectedTaskIndex, Toast.LENGTH_LONG).show();
				}
			},
			new Runnable() {
				public void run() {
					Toast.makeText(TasksListActivity.this, "" + selectedTaskIndex, Toast.LENGTH_LONG).show();
				}
			},
			new Runnable() {
				public void run() {
					Toast.makeText(TasksListActivity.this, "" + selectedTaskIndex, Toast.LENGTH_LONG).show();
				}
			}
	};
}
