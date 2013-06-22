package sak.todo.gui;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import sak.todo.gcm.ServerUtilities;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MeetingDescision extends Activity{
	
	private ListView participantsList;
	private ArrayAdapter arrayAdapter;
	private Button acceptMeeting;
	private Button declineMeeting;
	private TextView text;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meeting_descision);
		participantsList = (ListView) findViewById(R.id.listView1);
		acceptMeeting = (Button) findViewById(R.id.accept_meeting);
		declineMeeting = (Button) findViewById(R.id.decline_meeting);
		text = (TextView) findViewById(R.id.meeting_info);
		
		acceptMeeting.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				
				int userId = 0;
				String userEmail = "test@gmail.com";
				
				// need the userId and the user email
				
				
				try {
					ServerUtilities.updateUserState(getApplicationContext(), userId, userEmail);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendDescision(true);
			}
		});
		
		declineMeeting.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				int userId = 0;
				String userEmail = "test@gmail.com";
				
				// need the userId and the user email
				
				
				try {
					ServerUtilities.updateUserState(getApplicationContext(), userId, userEmail);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendDescision(false);
			}
		});
		
		text.setTextSize(30);
		ArrayList<String> selectedEmails = new ArrayList<String>();
		selectedEmails.add("ebn kalb1");
		selectedEmails.add("ebn kalb2");
		selectedEmails.add("ebn kalb3");
		selectedEmails.add("ebn kalb4");
		selectedEmails.add("ebn kalb5");
		selectedEmails.add("ebn kalb6");
		selectedEmails.add("ebn kalb7");
		selectedEmails.add("ebn kalb8");
		selectedEmails.add("ebn kalb9");
		selectedEmails.add("ebn kalb10");
		selectedEmails.add("ebn kalb11");
		selectedEmails.add("ebn kalb12");
		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, selectedEmails);
		participantsList.setAdapter(arrayAdapter);
	}
	
	/*
	 * sends the descision of the user about the meeting request 
	 * true-> accept false-> decline
	 */
	private void sendDescision(boolean descision)
	{
		
	}

}
