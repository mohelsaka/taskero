package sak.todo.gcm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sak.todo.database.DBHelper;
import sak.todo.database.Meeting;
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
			sendNotification("Send error: " + intent.getExtras().toString(), context);
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
			sendNotification("Deleted messages on server: " + intent.getExtras().toString(), context);
		} else {
			Toast.makeText(context, intent.getExtras().toString(), Toast.LENGTH_LONG).show();
			messageType = intent.getExtras().getString("collapse_key");

			if(messageType.equalsIgnoreCase("meeting_request")){
				try {
					handleMeetingRequest(intent.getExtras().getString("meeting"), context);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		setResultCode(Activity.RESULT_OK);
	}
/*
06-30 13:30:25.890: D/GCM-message(16397): Bundle[{collapse_key=meeting_request, from=806335698327, meeting={"body":"dhfgj","created_at":"2013-06-30T09:39:07Z","deadline":2147483647,"duedate":1372591820000,"duration":2.0,"id":7,"state":null,"updated_at":"2013-06-30T11:30:21Z","users":[{"email":"mohamed.elsaka2007@gmail.com"},{"email":"moamen.elgendy2010@gmail.com"}]}}]
 * 
 * */
	private static void handleMeetingRequest(String json, Context context) throws JSONException{
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
	
	
	// Put the GCM message into a notification and post it.
	public static void sendNotification(String msg, Context context) {
		NotificationManager mNotificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, MeetingDescision.class),0);
		
		/*
		Notification mBuilder = new Notification.Builder(context.getApplicationContext())
				.setContentTitle("New mail from " + "test@gmail.com")
				.setContentText(msg)
				.setTicker("New mail from " + "test@gmail.com")
				.setContentIntent(contentIntent)
				.getNotification();
		*/
		
		Notification mBuilder= new Notification.Builder(context.getApplicationContext())
        .setContentTitle("New mail from " + "test@gmail.com")
        .setContentText("Subject")
        .setSmallIcon(R.drawable.icon)
        .setContentIntent(contentIntent).getNotification();
		
        //Setting Notification Flags
		mBuilder.flags |= Notification.FLAG_AUTO_CANCEL;
		mBuilder.defaults |= Notification.DEFAULT_SOUND;
		mBuilder.flags |= Notification.FLAG_INSISTENT;
        
//		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder);
		
	}
}
