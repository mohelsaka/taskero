package sak.todo.gui;

import java.util.Calendar;

import sak.todo.constants.Constants;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


public class Notifier {

	public static NotificationManager mNotificationManager;
	private static final int ICON = R.drawable.stat_sample;

	private static void intiNotificationManager(Context context) {
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public static void CreateNotification(Context context, Intent intent) {

		// Update the status in the notification database table
		Bundle intentBundle = intent.getExtras();
		long notificationId = Long.parseLong(intent.getData()
				.getSchemeSpecificPart());

		CharSequence contentTitle = intentBundle
				.getString(Constants.TASK_TITLE_TAG);
		CharSequence contentText = Constants.TASK_CONTENT;

		// Raise the notification so that user can check the details
		intiNotificationManager(context);

		CharSequence tickerText = "Alarm for Task " + contentTitle;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(ICON, tickerText, when);

		// The PendingIntent to launch our activity if the user selects this
		// notification.
		Intent notificationIntent = new Intent(context, ClickActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		// Set the info for the views that show in the notification panel.
		// Light + Sound + Vibration.
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		notification.flags = notification.flags
				| Notification.FLAG_ONGOING_EVENT | Notification.DEFAULT_LIGHTS
				| Notification.DEFAULT_SOUND | Notification.FLAG_AUTO_CANCEL
				| Notification.DEFAULT_VIBRATE;

		// Integer.parseInt(intent.getData().getSchemeSpecificPart())
		mNotificationManager.notify((int) notificationId, notification);

		// Show the toast of what happened.
		Toast mToast = Toast.makeText(
				context,
				"Reminders added to the calendar successfully for "
						+ android.text.format.DateFormat.format(
								"MM/dd/yy h:mmaa", Calendar.getInstance()
										.getTimeInMillis()), Toast.LENGTH_LONG);
		mToast.show();
	}
}
