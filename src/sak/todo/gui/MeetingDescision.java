package sak.todo.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;

import sak.todo.database.DBHelper;
import sak.todo.database.Meeting;
import sak.todo.database.Task;
import sak.todo.gcm.GCMUtilities;
import sak.todo.gcm.ServerUtilities;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MeetingDescision extends Activity{
	
	private String meetingRemoteID = "";
	private String userID = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meeting_descision);
		
		DBHelper.initialize(this.getApplicationContext());
		GCMUtilities.initialize(this.getApplicationContext());
		Meeting m = Meeting.findById(getIntent().getLongExtra("meeting_id", -1));
		
		meetingRemoteID = Long.toString(m.remote_id);
		userID = GCMUtilities.getServerRegistrationId(this);
		
		((TextView)findViewById(R.id.meetingBody)).setText(m.body);
		Calendar c = Calendar.getInstance();
		
		c.setTimeInMillis(m.duedate);
		((TextView)findViewById(R.id.meetingDuedate)).setText(Task.getFormatedDate(c));
		
		try {
			JSONArray collaborators = new JSONArray(m.collaborators);
			LinearLayout collaboratorsView = (LinearLayout)findViewById(R.id.collaboratorsList);
			
			for (int i = 0; i < collaborators.length(); i++) {
				TextView v = new TextView(this);
				v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				v.setText(collaborators.getString(i));
				collaboratorsView.addView(v);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void acceptMeeting(View view){
		ServerUtilities.runInBackGround(this, new Runnable() {
			public void run() {
				try {
					ServerUtilities.acceptMeeting(meetingRemoteID, userID);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		},
		null,
		new Runnable() {
			public void run() {
				finish();
			}
		});
	}
	
	public void declineMeeting(View view){
		ServerUtilities.runInBackGround(this, new Runnable() {
			public void run() {
				try {
					ServerUtilities.declinetMeeting(meetingRemoteID, userID);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		},
		null,
		new Runnable() {
			public void run() {
				finish();
			}
		});
	}
	
}
