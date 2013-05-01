package sak.todo.gui;


import sak.todo.database.DBHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;



public class TodoGUIActivity extends Activity {
	/** Called when the activity is first created. */



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DBHelper.initialize(this);
//		
//		for (int i = 0; i < 100; i++) {
//			Task.deleteById(i);
//		}
//			Task.deleteById(29);
//			Task.deleteById(30);
//			Task.deleteById(31);
		
		 
		setContentView(R.layout.main);
		
//		setContentView(R.layout.meeting_request);
//		setContentView(R.layout.add_connection);
//		startActivity(new Intent(this, MeetingReview.class));
		
//		startActivity(new Intent(this, CreateMeeting.class));
		
		
	}
	
	public void addTask(View view){
		startActivity(new Intent(this, CreateTaskActivity.class));
	}
	
	public void showTasks(View view){
		Intent i=new Intent(this, ShowTasksActivity.class);
		i.putExtra("Schedulled", true);
		startActivity(i);
	}
	public void BackLogTasks(View view){
		Intent i=new Intent(this, ShowTasksActivity.class);
		i.putExtra("Schedulled", false);
		startActivity(i);
	}
	public void ScheduleTasks(View view){
		/*
		 * Constructor of object do all stuff... could be needed later to retreive updated data.
		 * 
		 */
//		ScheduleTasks st=new ScheduleTasks();
		
		
	}
	



}