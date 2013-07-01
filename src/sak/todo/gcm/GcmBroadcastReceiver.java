package sak.todo.gcm;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sak.todo.database.DBHelper;
import sak.todo.database.Meeting;
import sak.todo.database.Task;
import sak.todo.gui.MeetingDescision;
import sak.todo.gui.R;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.internal.m;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Handling of GCM messages.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {
	static final String TAG = "GCMDemo";
	public static final int NOTIFICATION_ID = 0;

	
	// we need the to know the type of message so that we can send the 
	// right notification
	
	@Override
	public void onReceive(Context context, Intent intent) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		Log.d("GCM-message", intent.getExtras().toString());
		String messageType = gcm.getMessageType(intent);
		if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
			Log.d("GCM-message", "Send error: " + intent.getExtras().toString());
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
			Log.d("GCM-message", "Deleted messages on server: " + intent.getExtras().toString());
		} else {
			Toast.makeText(context, intent.getExtras().toString(), Toast.LENGTH_LONG).show();
			messageType = intent.getExtras().getString("collapse_key");

			try {
				if(messageType.equalsIgnoreCase("meeting_request")){
					handleMeetingRequest(intent.getExtras().getString("meeting"), context);
				} else if(messageType.equalsIgnoreCase("decline_meeting")){
					handleDeclineMeeting(intent.getExtras().getString("meeting"), context);
				} else if(messageType.equalsIgnoreCase("accept_meeting")){
					handleAcceptMeeting(intent.getExtras().getString("meeting"), context);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		setResultCode(Activity.RESULT_OK);
	}

	private void handleMeetingRequest(String json, Context context) throws JSONException{
		JSONObject meetingJson = new JSONObject(json);
		
		// save meeting into the database
		DBHelper.initialize(context);
		Meeting meeting = Meeting.saveMeetingFromJSON(meetingJson);

		NotificationManager mNotificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		
		Intent i = new Intent(context, MeetingDescision.class);
		i.putExtra("meeting_id", meeting.id);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i,0);
		
		String requester = new JSONArray(meeting.collaborators).get(0).toString();
		
		Notification mBuilder= new Notification.Builder(context.getApplicationContext())
        .setContentTitle("Meeting requset from " + requester)
        .setContentText(meeting.body)
        .setSmallIcon(R.drawable.icon)
        .setContentIntent(contentIntent).getNotification();
		
        //Setting Notification Flags
		mBuilder.flags |= Notification.FLAG_AUTO_CANCEL;
		mBuilder.defaults |= Notification.DEFAULT_SOUND;
        
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder);
	}
	
	private void handleDeclineMeeting(String json, Context context) throws JSONException{
		JSONObject meetingJson = new JSONObject(json);
		
		// update meeting status in local DB
		DBHelper.initialize(context);
		Meeting m = Meeting.findByRemoteId(meetingJson.getLong("id")); // should never be null
		m.status = Meeting.DECLINED;
		m.save();
		
		Intent i = new Intent(context, MeetingDescision.class);
		i.putExtra("meeting_json", json);
		i.putExtra("action", "decline");
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, 0);
		
		NotificationManager mNotificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		Notification mBuilder= new Notification.Builder(context.getApplicationContext())
        .setContentTitle("Meeting declined")
        .setContentText(meetingJson.getString("body"))
        .setSmallIcon(R.drawable.icon)
        .setContentIntent(contentIntent).getNotification();
		
        //Setting Notification Flags
		mBuilder.flags |= Notification.FLAG_AUTO_CANCEL;
		mBuilder.defaults |= Notification.DEFAULT_SOUND;
        
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder);		
	}

	private void handleAcceptMeeting(String json, Context context) throws JSONException{
		JSONObject meetingJson = new JSONObject(json);
		
		// update meeting status in local DB
		DBHelper.initialize(context);
		Meeting m = Meeting.findByRemoteId(meetingJson.getLong("id")); // should never be null
		m.duedate = meetingJson.getLong("duedate");
		m.status = Meeting.CONFIRMED;
		m.save();
		
		// create task that refers to this meeting
		Task t = new Task();
		t.body = m.body;
		t.duedate = new Date(m.duedate);
		t.priority = 2;
		t.estimate = m.estimate;
		t.save();
		
		Intent i = new Intent(context, MeetingDescision.class);
		i.putExtra("meeting_json", json);
		i.putExtra("action", "accept");
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, 0);
		
		NotificationManager mNotificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		Notification mBuilder= new Notification.Builder(context.getApplicationContext())
        .setContentTitle("Meeting accepted")
        .setContentText(meetingJson.getString("body"))
        .setSmallIcon(R.drawable.icon)
        .setContentIntent(contentIntent).getNotification();
		
        //Setting Notification Flags
		mBuilder.flags |= Notification.FLAG_AUTO_CANCEL;
		mBuilder.defaults |= Notification.DEFAULT_SOUND;
        
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder);		
	}
	
}
