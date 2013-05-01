package sak.todo.gui;

import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class AlarmService extends Service {

	@Override
	public void onCreate() {
		Toast.makeText(this, "Created", Toast.LENGTH_LONG).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(this, "Binded", Toast.LENGTH_LONG).show();
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "Destroyed", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		// Here the Magic Happens: display alarm has been notified.
		Toast mToast = Toast.makeText(
				this,
				"Notification has been Opened."
						+ android.text.format.DateFormat.format(
								"MM/dd/yy h:mmaa", Calendar.getInstance()
										.getTimeInMillis()), Toast.LENGTH_LONG);
		mToast.show();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Toast.makeText(this, "UnBinded", Toast.LENGTH_LONG).show();
		return super.onUnbind(intent);
	}

}