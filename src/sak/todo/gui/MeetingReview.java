package sak.todo.gui;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sak.todo.database.DBHelper;
import sak.todo.database.Meeting;
import sak.todo.database.Task;
import sak.todo.gcm.GCMUtilities;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MeetingReview extends Activity{

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meeting_info);
		
		String action = getIntent().getStringExtra("action");

		try {
			JSONObject meetingJson = new JSONObject(getIntent().getStringExtra("meeting_json"));

			// viewing body body
			((TextView)findViewById(R.id.meetingBody)).setText(meetingJson.getString("body"));
			
			// viewing due date
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(meetingJson.getLong("duedate"));
			((TextView)findViewById(R.id.meetingDuedate)).setText(Task.getFormatedDate(c));

			if (action.equals("accept")) {
				displayCollaboratorsWithoutState(meetingJson);
			} else {
				displayCollaboratorsWithState(meetingJson);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, "ERROR IN PARSING", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	private void displayCollaboratorsWithoutState(JSONObject meetingJson) throws JSONException{
		LinearLayout collaboratorsView = (LinearLayout)findViewById(R.id.collaboratorsList);
		JSONArray ar = meetingJson.getJSONArray("users");
		JSONArray collaboratorsArray = new JSONArray();

		for (int i = 0; i < ar.length(); i++) {
			JSONObject o = ar.getJSONObject(i);
			collaboratorsArray.put(o.getString("email"));
			
			TextView v = new TextView(this);
			v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			v.setText(o.getString("email"));

			collaboratorsView.addView(v);
		}
	}
	
	private void displayCollaboratorsWithState(JSONObject meetingJson) throws JSONException{
		LinearLayout collaboratorsView = (LinearLayout)findViewById(R.id.collaboratorsList);
		JSONArray users = meetingJson.getJSONArray("users");
		JSONArray users_state = meetingJson.getJSONArray("meeting_collaborators");

		for (int i = 0; i < users.length(); i++) {
			String user_email = users.getJSONObject(i).getString("email");
			String state = users_state.getJSONObject(i).getString("state");
			
			TextView v = new TextView(this);
			v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			v.setText(user_email);
			
			if (state.equalsIgnoreCase("declined")) {
				v.setTextColor(Color.RED);
			} else if (state.equalsIgnoreCase("accepted")) {
				v.setTextColor(Color.GREEN);
			}
			
			collaboratorsView.addView(v);
		}
	}
	
	public void endReview(View view){
		finish();
	}
}
