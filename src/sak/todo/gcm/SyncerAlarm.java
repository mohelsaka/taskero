package sak.todo.gcm;

import sak.todo.database.DBHelper;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class SyncerAlarm extends BroadcastReceiver{

	public static final String SYNCING_USER_STATE = "syncing_user_state";
	private static final long TIME_OFFSET = 5 * 1000;
	
	@Override
	public void onReceive(Context context, Intent arg1) {
		
		// getting application context
		context = context.getApplicationContext();
		
		// Initialize utilities
		GCMUtilities.initialize(context);
		DBHelper.initialize(context);

		final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		boolean connected = (activeNetworkInfo != null && activeNetworkInfo.isConnected());
		
		// getting user email
	    final String email =  GCMUtilities.getGmailAccount(context);
		
	    // getting user id
	    final String userID = GCMUtilities.getServerRegistrationId(context);
		
		if(!connected){
			reschedule(context, TIME_OFFSET);
			return;
		}
		
		try {
			boolean scucess = ServerUtilities.updateUserState(context, userID, email);

			if (!scucess) {
				reschedule(context, TIME_OFFSET);
			}else{
				SharedPreferences pref = context.getSharedPreferences(SyncerAlarm.class.getSimpleName(), Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean(SYNCING_USER_STATE, false).commit();
			}
		} catch (Exception e) {
			reschedule(context, TIME_OFFSET);
			e.printStackTrace();
		}
	}
	
	public void reschedule(Context context, long timeOffset){
		Log.d("SYNCING", "<><><><>rescheduling<><><><><>");
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, SyncerAlarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + timeOffset, pi);
    }
}
