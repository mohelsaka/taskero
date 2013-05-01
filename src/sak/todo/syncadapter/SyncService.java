package sak.todo.syncadapter;

import sak.todo.database.DBHelper;
import sak.todo.gui.MeetingListActivity;
import sak.todo.gui.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


/**
 * Service to handle Account sync. This is invoked with an intent with action
 * ACTION_AUTHENTICATOR_INTENT. It instantiates the syncadapter and returns its
 * IBinder.
 */
public class SyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();

    private static SyncAdapter sSyncAdapter = null;

	NotificationManager mNotificationManager;
	Notification meetingRequest;
	Notification friendRequest;
	private static final int mr = 1;
    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
            	DBHelper.initialize(this);
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();

		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
    	meetingRequest = new Notification(icon, "Meeting Request!", when);
    	
		Context context = getApplicationContext();
		CharSequence contentTitle = "Meeting Request.. ";
		CharSequence contentText = "You have meeting requests ..";
		Intent intent = new Intent(this, MeetingListActivity.class);
		/*
		 * Here you put extras to this intent
		 * Extra here is the meeting  ids only
		 */
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		meetingRequest.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		mNotificationManager.notify(mr, meetingRequest);	
    }
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
