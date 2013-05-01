package sak.todo.gui;

import java.util.Calendar;
import java.util.Date;

import sak.todo.constants.Constants;
import sak.todo.database.DBHelper;
import sak.todo.database.Task;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;


//Called when three events occur BOOT_COPLETED + ACTION_EXTERNAL_APPLICATIONS_AVAILABLE + USER_AVAILABLE.
public class SchedulerSetupReceiver extends BroadcastReceiver {

	private static final String APP_TAG = "ENZO";

	private Task todayTasks[];
	private Date today;

	// Call the DB to get today's tasks.
	public void getTodayTasks() {
		today = Calendar.getInstance().getTime();
		todayTasks = Task.getTasksOfDayFull(today);
		// todayTasks = Task.getDecoyTasks();
	}

	public void onReceive(final Context context, final Intent intent) {

		DBHelper.initialize(context);
		Log.d(APP_TAG, "SchedulerSetupReceiver.onReceive() called");
		// Fill Current Day Tasks from the Database.
		getTodayTasks();

		// Get an instance of the alarmManager.
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();

		for (int j = 0; j < todayTasks.length; j++) {
			Intent mIntent = new Intent(context, SchedulerEventReceiver.class);

			mIntent.setData(Uri.parse("task:" + todayTasks[j].id));
			mIntent.putExtra(Constants.TASK_TITLE_TAG, "todayTasks[" + j
					+ "].title");
			mIntent.putExtra(Constants.TASK_DUEDATE_TAG, ""
					+ todayTasks[j].duedate.getHours() + ":"
					+ todayTasks[j].duedate.getMinutes());

			PendingIntent intentExecuted = PendingIntent.getBroadcast(context,
					0, mIntent, Intent.FLAG_GRANT_READ_URI_PERMISSION);

			calendar.setTime(todayTasks[j].duedate);
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(), intentExecuted);

			Toast.makeText(context, "Alarm added to AlarmManager.",
					Toast.LENGTH_SHORT).show();
		}

		// calendar.add(Calendar.SECOND, 10);
		// alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),
		// EXEC_INTERVAL, intentExecuted);
	}
}