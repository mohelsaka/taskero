package sak.todo.gcm;

import sak.todo.gui.MeetingDescision;
import sak.todo.gui.R;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Handling of GCM messages.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {
	static final String TAG = "GCMDemo";
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	Context ctx;

	
	// we need the to know the type of message so that we can send the 
	// right notification
	
	@Override
	public void onReceive(Context context, Intent intent) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		ctx = context;
		Log.d("GCM-message", intent.getExtras().toString());
		String messageType = gcm.getMessageType(intent);
		if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
			sendNotification("Send error: " + intent.getExtras().toString());
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
				.equals(messageType)) {
			sendNotification("Deleted messages on server: "
					+ intent.getExtras().toString());
		} else {
//			sendNotification("Received: " + intent.getExtras().toString());
			Log.d("GCM-message", intent.getExtras().toString());
		}
		
		setResultCode(Activity.RESULT_OK);
	}

	// Put the GCM message into a notification and post it.
	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
				new Intent(ctx, MeetingDescision.class),0);

		
		
		Notification mBuilder = new Notification.Builder(ctx)
				.setContentTitle("New mail from " + "test@gmail.com")
				.setContentText(msg)
				.setTicker("New mail from " + "test@gmail.com")
				.setSmallIcon(R.drawable.icon)
				.setContentIntent(contentIntent)
				.getNotification();

//		mBuilder.setContentIntent(contentIntent);
		mNotificationManager
				.notify(NOTIFICATION_ID, mBuilder);

	}
}
