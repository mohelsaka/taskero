package sak.todo.gui;

import java.util.ArrayList;

import sak.todo.database.Meeting;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MeetingListActivity extends ListActivity {

	ArrayList<Pair<Long, String>> meetings;
	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		meetings = Meeting.getAllMeetingsBody();
		
		// for testing issues only .... we'll keep it very simple!
		String[] values = new String[meetings.size()];
		for (int i = 0; i < values.length; i++) {
			values[i] = ""+meetings.get(i).first +": "+meetings.get(i).second;
		}
		
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, values);
		setListAdapter(adapter);
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		String item = (String) getListAdapter().getItem(position);
		Intent i = new Intent(this, MeetingReview.class);
		
//		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		
		item = item.substring(0, item.indexOf(':'));
		
		i.putExtra(MeetingReview.MEETING_ID ,Long.parseLong(item));
		startActivity(i);
	}
}
