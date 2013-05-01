package sak.todo.gui;

import java.util.Calendar;
import java.util.Date;

import sak.todo.database.Meeting;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeSlider;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MeetingReview extends Activity {

	public static final String MEETING_ID = "meeting_id";
	
	Meeting meeting;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meeting_review);

		meetingDueDatesTable = (TableLayout) findViewById(R.id.meetingDueDatesTable);
		meetingCollaboratorsTable = (TableLayout) findViewById(R.id.receivers);
		
		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// loading the meeting to be reviewed
		final long id = getIntent().getLongExtra(MEETING_ID, -1);
		if(id == -1)
			Toast.makeText(this, "meeting id is -ve!", Toast.LENGTH_LONG).show();
		meeting = Meeting.findById(id);
		
		// body and estimate
		((TextView)findViewById(R.id.meetingBodyReview)).setText(meeting.body);
		((TextView)findViewById(R.id.meetingEstimate)).setText(""+meeting.estimate);
		
		// Displaying meeting due dates
		TableRow t ;
		Date tempDate = new Date();
		for (int i = 0; i < meeting.dueDates.length; i++) {
			// setting the date
			t = (TableRow) inflater.inflate(R.layout.meeting_date_review, null);
			tempDate.setTime(meeting.dueDates[i]);
			((TextView)t.getChildAt(0)).setText(tempDate.toLocaleString());
			
			// setting the votes and the star
			LinearLayout l = (LinearLayout)t.getChildAt(1);
			((TextView)l.getChildAt(0)).setText(""+meeting.votes[i]);
			
			((ToggleButton)l.getChildAt(1)).setChecked(meeting.stared[i] == 1);
			meetingDueDatesTable.addView(t);
		}
		
		// Displaying the collaborators
		for (int i = 0; i < meeting.collaborators.length; i++) {
			TableRow r = (TableRow)inflater.inflate(R.layout.meeting_data_row, null);
			((TextView)r.getChildAt(0)).setText(meeting.collaborators[i]);
			r.getChildAt(1).setVisibility(View.INVISIBLE);
			meetingCollaboratorsTable.addView(r);
		}
		
		// Displaying the creator of the meeting
		((TextView)findViewById(R.id.creatorLabel)).setText(meeting.creator);
		
		Toast.makeText(this, meeting.toString(), Toast.LENGTH_LONG).show();
	}

	private TableLayout meetingDueDatesTable;
	private TableLayout meetingCollaboratorsTable;
	private LayoutInflater inflater;

	private final DateSlider.OnDateSetListener mDateTimeSetListener = new DateSlider.OnDateSetListener() {
		public void onDateSet(DateSlider view, Calendar selectedDate) {
			// update the dateText view with the corresponding date
			TableRow v = (TableRow) inflater.inflate(R.layout.meeting_date_review, null);
			
			Date d = selectedDate.getTime();
			d.setSeconds(0);
			d.setMinutes(15*(d.getMinutes() / 15));
			
			((TextView)v.getChildAt(0)).setText(d.toLocaleString());
			((TextView)((LinearLayout)v.getChildAt(1)).getChildAt(0)).setText(""+1);
			meetingDueDatesTable.addView(v);
		}
	};
	
	public void addNewDate(View view) {
		showDialog(DateSlider.DATETIMESELECTOR_ID);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DateSlider.DATETIMESELECTOR_ID) {
			final Calendar c = Calendar.getInstance();
			return new DateTimeSlider(this, mDateTimeSetListener, c);
		}
		return null;
	}
	
	public void toggleStar(View v){
		ToggleButton tb = (ToggleButton)v;
		TextView te = (TextView)((LinearLayout)tb.getParent()).getChildAt(0);
		if(tb.isChecked()){
			te.setText(""+(Integer.parseInt((String) te.getText())+1));
		}else{
			te.setText(""+(Integer.parseInt((String) te.getText())-1));
		}
	}
	
	public void saveReviewInformation(View v){
		int count = meetingDueDatesTable.getChildCount();
		meeting.dueDates = new long[count];
		meeting.stared = new int[count];
		meeting.votes = new int[count];
		for (int j = 0; j < count; j++) {
			TableRow r = (TableRow)meetingDueDatesTable.getChildAt(j);
			
			LinearLayout l = (LinearLayout)r.getChildAt(1);
			ToggleButton tb = (ToggleButton)l.getChildAt(1);
			TextView te = (TextView)l.getChildAt(0);
			
			meeting.dueDates[j] = Date.parse(((TextView)r.getChildAt(0)).getText().toString());
			meeting.votes[j] = Integer.parseInt(te.getText().toString());
			meeting.stared[j] = tb.isChecked()? 1 : 0;
		}
		meeting.saveNewDatesAndVotes();
		finish();
	}
	
	public void declineMeeting(View view){
		meeting.decline();
		finish();
	}
}
