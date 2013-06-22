package sak.todo.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.client.ClientProtocolException;

import sak.todo.database.Meeting;
import sak.todo.gcm.GCMUtilities;
import sak.todo.gcm.ServerUtilities;
import sak.todo.gui.R;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeSlider;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class CreateMeeting extends Activity implements 

OnClickListener, OnMenuItemClickListener, android.widget.PopupMenu.OnMenuItemClickListener {

	private ListView monthsListView;
	private PopupMenu popupMenu; 
    private ArrayAdapter arrayAdapter;
	private Button dateText;
	private Button saveAll;
	private ImageButton addPart;
	private EditText taskBody;
	private EditText duration;
	
    private final static int ONE = 1;
    private final static int TWO = 2;
    private final static int THREE = 3;
    private ArrayList<String> emails;
    public static final int NOTIFICATION_ID = 0;
    private long deadline;
	private NotificationManager mNotificationManager;
	
    
    
    private ArrayList<String> selectedEmails = new ArrayList<String>();

    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_meeting);
		
//		dateText = (TextView) findViewById(R.id.email);
//		view.setMovementMethod(new ScrollingMovementMethod());
		
		String[] monthsArray = { };
				 
				    // Declare the UI components
		taskBody = (EditText) findViewById(R.id.task_body);
		monthsListView = (ListView) findViewById(R.id.months_list);
		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, selectedEmails);
		monthsListView.setAdapter(arrayAdapter);
		addPart = (ImageButton) findViewById(R.id.add_participants);
		popupMenu = new PopupMenu(this, findViewById(R.id.participants));
		popupMenu.setOnMenuItemClickListener(this);
		saveAll = (Button) findViewById(R.id.SaveAll);
		duration = (EditText) findViewById(R.id.editText1);
		
		
		saveAll.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "A Meeting has been sent", Toast.LENGTH_LONG);
				
				
				Meeting meeting = new Meeting();
				meeting.body = taskBody.getText().toString();
				
				for(int i=0;i<selectedEmails.size();i++)
				{
					meeting.collaborators +=selectedEmails.get(i)+","; 
				}
				meeting.status = Meeting.PENDING;
				meeting.estimate = Float.parseFloat(duration.getText().toString());
				meeting.save();
				sendNotification("here");
				
			}
		});
		
		
		
		
		emails = retrieveEmails();
		for(int i=0;i<emails.size();i++)
		{
			popupMenu.getMenu().add(Menu.NONE, i+1, Menu.NONE, emails.get(i));
		}
		
		
		
		addPart.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {

	             popupMenu.show();
	             
			}
		});
		
		 
//		view.setText(email);
	}

	public void showDateDialog(View view) {
		dateText = (Button) view;
		showDialog(DateSlider.DATETIMESELECTOR_ID);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}
	
	private final DateSlider.OnDateSetListener mDateTimeSetListener = new DateSlider.OnDateSetListener() {
		public void onDateSet(DateSlider view, Calendar selectedDate) {
			// update the dateText view with the corresponding dateetTime
			final int minute = (selectedDate.get(Calendar.MINUTE) / DateTimeSlider.MINUTEINTERVAL)
					* DateTimeSlider.MINUTEINTERVAL;
			dateText.setText(String.format("%te. %tB %tY  %tH:%02d",
					selectedDate, selectedDate, selectedDate, selectedDate, minute));

			selectedDate.set(Calendar.MINUTE, minute);
			selectedDate.set(Calendar.SECOND, 0);
			deadline = selectedDate.getTime().getTime();
			// updating task object's dates
//			if (dateText.getId() == R.id.deadline)
//				task.deadline = selectedDate.getTime();

		}
	};

	private ArrayList<String> retrieveEmails()
	{
		ArrayList<String> email = new ArrayList<String>();
		Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null); 
//		String email="";
		while (cursor.moveToNext()) { 
		   String contactId = cursor.getString(cursor.getColumnIndex( 
		   ContactsContract.Contacts._ID)); 
		   String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)); 
		   if (Boolean.parseBoolean(hasPhone)) { 
		      // You know it has a number so now query it like this
		      Cursor phones = getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null, null); 
		      while (phones.moveToNext()) { 
		         String phoneNumber = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));                 
		      } 
		      phones.close(); 
		   }

		   Cursor emails = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null); 
		   while (emails.moveToNext()) { 
		      // This would allow you get several email addresses 
		      String emailAddress = emails.getString( 
		      emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
		      int index = emailAddress.indexOf('@');
		      if(emailAddress.length()!=0)
		      {
		    	  if(emailAddress.substring(index+1).equals("gmail.com"))
		    	  {
		    		  email.add(emailAddress);
		    		  
		    	  }
		      }
		   } 
		   emails.close();
		}
		cursor.close();
		
		return email;
	}
	
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

	public boolean onMenuItemClick(MenuItem item) {
		
		selectedEmails.add(emails.get(item.getItemId()-1));
//		arrayAdapter.clear();
//		arrayAdapter.add(emails.get(item.getItemId()));
		arrayAdapter.notifyDataSetChanged();
		
		
		return false;
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
	private void sendNotification(String msg) {
		
//		Context t = new CreateMultiTaskActivity();
		mNotificationManager = (NotificationManager) 
				getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MeetingDescision.class),0);

		
		
		Notification mBuilder = new Notification.Builder(this)
				.setContentTitle("New mail from " + "test@gmail.com")
				.setContentText(msg)
				.setTicker("New mail from " + "test@gmail.com")
				.setSmallIcon(R.drawable.icon)
				.setContentIntent(contentIntent)
				.build();

//		mBuilder.setContentIntent(contentIntent);
		mNotificationManager
				.notify(NOTIFICATION_ID, mBuilder);
		
//		mNotificationManager.notify(0, mBuilder);
	}

}
