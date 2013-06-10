package sak.todo.gui.schedules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import sak.todo.database.Task;
import sak.todo.gui.R;
import sak.todo.gui.R.color;
import sak.todo.gui.R.id;
import sak.todo.gui.R.layout;
import android.app.ActionBar;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

public class SchedulesActivity extends Activity implements TabListener{
	ArrayList<ArrayList<Task>> assignments;
	TasksListAdapter[] adapters;
	
	ListView tasksList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// requesting action bar feature
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		
		// setup action bar for tabs
	    ActionBar actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(false);
	    actionBar.setHomeButtonEnabled(false);
	    actionBar.setDisplayShowTitleEnabled(false);
	    actionBar.setDisplayShowHomeEnabled(false);
	    actionBar.setStackedBackgroundDrawable(new ColorDrawable(R.color.tabsColor));
	    
	    // getting assignments to be displayed
		Bundle b = getIntent().getExtras();
		Object assignmentsObject;
		if(b != null && ((assignmentsObject = b.get("assignments")) != null))
			assignments = (ArrayList<ArrayList<Task>>) assignmentsObject;
		
		buildRedundantAssignments();
		
		setContentView(R.layout.schedules_layout);
		tasksList = (ListView)findViewById(R.id.tasksList);

		if(assignments == null){
			// something went wrong and this activity should be ended
			Toast.makeText(this, "Assignmets are null", Toast.LENGTH_LONG).show();
			finish();
		}else {
			int numOfAssignments = assignments.size();
			adapters = new TasksListAdapter[numOfAssignments];
			
			if (numOfAssignments == 0) {
				Toast.makeText(this, "No schdules are available", Toast.LENGTH_LONG).show();
				finish();
			}else{
				// adding tab for each assignment
				for (int i = 0; i < numOfAssignments; i++) {
					Tab tab = actionBar.newTab().setText(String.format("Plan#%d", i)).setTabListener(this);
					actionBar.addTab(tab);
				}
			}
		}
	}
	
	private void loadAssignment(int assignmentIndex){
		// lazy load for adapters and caching them
		if(adapters[assignmentIndex] == null){
			adapters[assignmentIndex] = new TasksListAdapter(this, R.layout.list_tasks,
					R.id.task_body, assignments.get(assignmentIndex)); 
		}
		
		// setting the new adatper
		tasksList.setAdapter(adapters[assignmentIndex]);
	}
	
	private void buildRedundantAssignments(){
		assignments = new ArrayList<ArrayList<Task>>();

		Task[] tasks = new Task[5];
		for (int j = 0; j < 5; j++) {
			Task t = new Task();
			t.body = "task #" + j;
			t.duedate =  new Date();
			t.estimate = 1.5f;
			t.priority = j * 2;
			t.id = j;
			tasks[j] = t;
		}
		
		long shift = 1000 * 60 * 60 * 3;
		int[] indexes = {0, 1, 2, 3, 4};
		for (int i = 0; i < 4; i++) {
			ArrayList<Task> s = new ArrayList<Task>();
			shuffleArray(indexes);
			
			for (int j = 0; j < 5; j++) {
				Task t = null;
				try {
					t = tasks[indexes[j]].clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				
				t.duedate = new Date(t.duedate.getTime() + (shift * j));
				
				s.add(t);
			}
			
			assignments.add(s);
		}
	}
	
	static Random rnd = new Random();
	// Implementing Fisher–Yates shuffle
	static void shuffleArray(int[] ar){
		for (int i = ar.length - 1; i >= 0; i--){
			int index = rnd.nextInt(i + 1);
			
			// Simple swap
			int a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}
	
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		loadAssignment(tab.getPosition());
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}
