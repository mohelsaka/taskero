package sak.todo.gui;


import sak.todo.constants.Constants;
import sak.todo.database.DBHelper;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;

public class Intro extends Activity {
	
	String[] icons=new String[]{
			"1. Time line","2. Create Task","3. Create Meeting","4.Create Multi_Task",
			"5. BackLog tasks","6. Donations"
	};
	Intent[] intents;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);
		DBHelper.initialize(this);
		
		intents=new Intent[]{
				new Intent(this,sak.todo.timeline.Calendar.class),
				new Intent(this,CreateTaskActivity.class),
				new Intent(this,CreateMeeting.class),
				new Intent(this,CreateMultiTaskActivityUpdated.class),
				new Intent(this,ShowTasksActivity.class),
				new Intent(this,SchedulesActivity.class),
			} ;
//		AccountManager accountManager = AccountManager.get(this);
//		Account ac[] = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
//		if(ac.length == 0){
////			AccountAuthenticator accountAuth = new AccountAuthenticator(this);
//			accountManager.addAccount(Constants.ACCOUNT_TYPE, Constants.AUTHTOKEN_TYPE, null, null, this, null, null);
//		}
		
		GridView gv=(GridView)findViewById(R.id.gridView);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, icons);
 
		gv.setAdapter(adapter);
 
		gv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
				int position, long id) {
				if(position==5){// backLog tasks
					intents[position].putExtra("Schedulled", false);
				}
				startActivity(intents[position]);
			}
		});
	}

}
