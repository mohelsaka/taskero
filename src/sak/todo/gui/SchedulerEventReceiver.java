package sak.todo.gui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SchedulerEventReceiver extends BroadcastReceiver {

	private static final String APP_TAG = "Enzo";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		Log.d(APP_TAG, "SchedulerEventReceiver.onReceive() called");
//		Intent eventService = new Intent(context, AlarmService.class);
//		context.startService(eventService);
		Notifier.CreateNotification(context, intent);
	}
}