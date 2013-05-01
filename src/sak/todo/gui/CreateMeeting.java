package sak.todo.gui;

import java.util.Calendar;
import java.util.Date;

import sak.todo.constants.Constants;
import sak.todo.database.Meeting;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeSlider;

public class CreateMeeting extends Activity {

	public static String userName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.meeting_request);
		
		inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		meetingDueDatesTable = (TableLayout) findViewById(R.id.meetingDueDatesTable);
		receiversTable = (TableLayout) findViewById(R.id.receivers);
		bodyView = (EditText) findViewById(R.id.meetingBody);
		estimateView = (EditText) findViewById(R.id.meetingEstimate);
		
		// retrieving the user name from the account manager
		if(userName == null){
			AccountManager accountManager = AccountManager.get(this);
			Account ac[] = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
			if(ac.length != 0){
				userName = ac[0].name;
			}
		}
	}
	private TableLayout meetingDueDatesTable;
	private TableLayout receiversTable;
	private EditText bodyView;
	private EditText estimateView;
	
	
	private final DateSlider.OnDateSetListener mDateTimeSetListener = new DateSlider.OnDateSetListener() {
		public void onDateSet(DateSlider view, Calendar selectedDate) {
			// update the dateText view with the corresponding date
			final int minute = selectedDate.get(Calendar.MINUTE)
					/ DateTimeSlider.MINUTEINTERVAL
					* DateTimeSlider.MINUTEINTERVAL;

			TableRow v = (TableRow)inflater.inflate(R.layout.meeting_data_row, null);
			
//			((TextView)v.getChildAt(0)).setText(String.format("%te. %tB %tY  %tH:%02d",
//					selectedDate, selectedDate, selectedDate, selectedDate,
//					minute));
			
			Date d = selectedDate.getTime();
			d.setSeconds(0);
			d.setMinutes(15*(d.getMinutes() / 15));
			
			((TextView)v.getChildAt(0)).setText(d.toLocaleString());
			meetingDueDatesTable.addView(v);
		}
	};
	
	private LayoutInflater inflater;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DateSlider.DATETIMESELECTOR_ID) {
			final Calendar c = Calendar.getInstance();
			return new DateTimeSlider(this, mDateTimeSetListener, c);
		}
		return null;
	}

	public void removeRow(View view) {
		final TableLayout t = (TableLayout)view.getParent().getParent();
		t.removeView((View) view.getParent());
	}

	public void addNewDate(View view) {
		showDialog(DateSlider.DATETIMESELECTOR_ID);
	}
	
	public void invitePeople(View view){
		Intent i = new Intent(this, FriendsList.class);
		startActivityForResult(i,FriendsList.SELECTED_FRIENDS_RESULT_ID);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == FriendsList.SELECTED_FRIENDS_RESULT_ID){
			long[] ids = data.getLongArrayExtra("selected_ids");
			for (long l : ids) {
				TableRow v = (TableRow)inflater.inflate(R.layout.meeting_data_row, null);
				
				((TextView)v.getChildAt(0)).setText(String.format(FriendsList.getFriendName(l)));
				receiversTable.addView(v);
			}
		}
	}
	
	Meeting meeting = new Meeting();
	public void createMeetingRequest(View view){
		meeting.body = bodyView.getText().toString();
		if(meeting.body.equals("")){
			Toast.makeText(this, "Hey! you forgot the body!", Toast.LENGTH_LONG).show();
			return;
		}
		String estimateValue = estimateView.getText().toString();
		if(TextUtils.isEmpty(estimateValue)){
			Toast.makeText(this, "You fogot to set the estimated period of the meeting", Toast.LENGTH_LONG).show();
			return;
		}
		meeting.estimate = Float.parseFloat(estimateValue);
		
		// adding meeting collaborators;
		int rowsCount = receiversTable.getChildCount();
		if(rowsCount == 0){
			Toast.makeText(this, "Will you meet with yourself! where's you mind?!", Toast.LENGTH_LONG).show();
			return;
		}
		meeting.collaborators = new String[rowsCount];
		for (int i = 0; i < rowsCount; i++) {
			TableRow r = (TableRow) receiversTable.getChildAt(i);
			meeting.collaborators[i] = ((TextView)r.getChildAt(0)).getText().toString();
		}
		
		// adding candidate dates
		rowsCount = meetingDueDatesTable.getChildCount();
		if(rowsCount == 0){
			Toast.makeText(this, "Add at least one due date", Toast.LENGTH_LONG).show();
			return;
		}
		meeting.dueDates = new long[rowsCount];
		meeting.votes = new int[rowsCount];
		meeting.stared = new int[rowsCount];
		for (int i = 0; i < rowsCount; i++) {
			// setting the date
			TableRow r = (TableRow) meetingDueDatesTable.getChildAt(i);
			meeting.dueDates[i] = Date.parse(((TextView)r.getChildAt(0)).getText().toString());
			
			// setting initial votes and stars
			meeting.votes[i] = (meeting.stared[i] = 1);
		}
		
		// setting the creator
		meeting.creator = CreateMeeting.userName;
		
		meeting.save();
		Toast.makeText(this, "Meeting is created and waiting to be synced", Toast.LENGTH_LONG).show();
		finish();
	}
}
