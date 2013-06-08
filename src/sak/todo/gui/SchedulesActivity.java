package sak.todo.gui;

import java.util.ArrayList;

import sak.todo.database.Task;
import android.app.ActionBar;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

public class SchedulesActivity extends Activity implements TabListener{
	ArrayList<ArrayList<Task>> assignments;
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
		
		if(assignments == null){
			// something went wrong and this activity should be ended
			Toast.makeText(this, "Assignmets are null", Toast.LENGTH_LONG).show();
			finish();
		}else {
			int numOfAssignments = assignments.size();
			
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
		
	    setContentView(R.layout.schedules_layout);
	    tasksList = (ListView)findViewById(R.id.tasksList);
	}
	
	private void loadAssignment(int assignmentIndex){
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
	    switch (item.getItemId()) {
	        case R.id.creat_multitask_item:
	        	startActivity(new Intent(this, CreateMultiTaskActivityUpdated.class));
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
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
}
