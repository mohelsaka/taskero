package sak.todo.gui;

import java.util.ArrayList;
import java.util.Calendar;

import sak.todo.gui.R;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeSlider;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

public class CreateMeeting extends Activity implements 

OnClickListener, OnMenuItemClickListener, android.widget.PopupMenu.OnMenuItemClickListener {

	private ListView monthsListView;
	private PopupMenu popupMenu; 
    private ArrayAdapter arrayAdapter;
	private Button dateText;
	private ImageButton addPart;
    private final static int ONE = 1;
    private final static int TWO = 2;
    private final static int THREE = 3;
    private ArrayList<String> emails;
    private Button saveAll;
    
    private ArrayList<String> selectedEmails = new ArrayList<String>();

    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_meeting);
		
//		dateText = (TextView) findViewById(R.id.email);
//		view.setMovementMethod(new ScrollingMovementMethod());
		
		String[] monthsArray = { };
				 
				    // Declare the UI components
		monthsListView = (ListView) findViewById(R.id.months_list);
		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, selectedEmails);
		monthsListView.setAdapter(arrayAdapter);
		addPart = (ImageButton) findViewById(R.id.add_participants);
		popupMenu = new PopupMenu(this, findViewById(R.id.participants));
		popupMenu.setOnMenuItemClickListener(this);
		saveAll = (Button) findViewById(R.id.SaveAll);
		
		saveAll.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
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
			// update the dateText view with the corresponding date
			final int minute = (selectedDate.get(Calendar.MINUTE) / DateTimeSlider.MINUTEINTERVAL)
					* DateTimeSlider.MINUTEINTERVAL;
			dateText.setText(String.format("%te. %tB %tY  %tH:%02d",
					selectedDate, selectedDate, selectedDate, selectedDate, minute));

			selectedDate.set(Calendar.MINUTE, minute);
			selectedDate.set(Calendar.SECOND, 0);
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

}
