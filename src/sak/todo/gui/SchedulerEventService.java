package sak.todo.gui;

import java.util.Date;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/*
 * And last but not least the service is executed.
 * There is only one specialty here.
 * we are using onStartCommand instead of onStart and 
 * we are returning Service.START_NOT_STICKY.
 * First of all onStart is deprecated in Android 2.0 and has no return value.
 * The return value of onStartCommand allows us to specify what to do when our Service is killed.
 * In our case, START_NOT_STICKY tells the operating system 
 * that it may forget about the service if it has to kill it.
 * in our scenario this is not really a problem because the next tick 
 * from the AlarmManager will be creating a new instance (indirectly) anyway.
 * 
 */
public class SchedulerEventService extends Service {
	private static final String APP_TAG = "ENZO";

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {
		Log.d(APP_TAG, "event received in service: " + new Date().toString());
		return Service.START_NOT_STICKY;
	}

}