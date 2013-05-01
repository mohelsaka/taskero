package sak.todo.gui;


import java.util.Arrays;
import java.util.Vector;

import cr.*;
import sak.todo.database.Task;
import sak.todo.database.TasksIterator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ShowTasksActivity extends Activity {
	
	ArrayAdapter<String> adapter;
	ListView listview;
	Task[] tasks;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backlog_view);
		
		listview=(ListView)findViewById(R.id.listView1);
		
		Intent intent=getIntent();
		boolean schedulled=false;
		
		schedulled=intent.getBooleanExtra("Schedulled", schedulled);
		
		
		TasksIterator ti;
		if(schedulled)ti=Task.getAllTasks();
		else ti=Task.getBackLogtask();
		
		int count = ti.getCursor().getCount();
		tasks = new Task[count];
		Task t = ti.nextTask();
		int k = 0;
		while (t != null) {
			tasks[k++] = t;
			t = ti.nextTask();
		}
		
		final String[] listOfTasks=new String[tasks.length];
		for (int i = 0; i < listOfTasks.length; i++) {
			Log.d("debug", "=> "+tasks[i].toString());
			listOfTasks[i]=tasks[i].body;
		}
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,listOfTasks);
		listview.setAdapter(adapter);
		listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		 
		
		
 	}
	
	/**
	 * TODO: some code has been commented to be able to integrate
	 * Integrated by moh.saka
	 * */
	public void schedule(View view){
		long[] selectedPeople = listview.getCheckItemIds();
		Task[] schTasks=new Task[selectedPeople.length];
		Constraint[] constraints=new Constraint[100];
		
		
		Log.d("debug", ""+schTasks.length);
		for (int i = 0; i < selectedPeople.length; i++) {
			schTasks[i]=tasks[(int) selectedPeople[i]];
			Vector<Interval>v=new Vector<Interval>(); 
//			v.add(new Interval((int)schTasks[i].estimate, (int)schTasks[i].estimate, 1));
//			constraints[i]=new Constraint(schTasks[i].getStartTimePoint(), schTasks[i].getEndTimePoint(),v );
			Log.d("debug", ""+schTasks[i].body);
		}
		
//		ScheduleTasks st=new ScheduleTasks(schTasks);
		
		
		
//		Reasoner.schedule(schTasks, constraints);
		Toast.makeText(this, "Finish!",
				Toast.LENGTH_LONG).show();
		this.finish();
		
		
	}

}
