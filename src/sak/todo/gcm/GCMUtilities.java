package sak.todo.gcm;

import java.io.IOException;
import java.sql.Timestamp;

import org.json.JSONException;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Main UI for the demo app.
 */
public class GCMUtilities {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_SERVER_REG_ID = "server_registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";

    /**
     * Default lifespan (30 days) of a reservation until it is considered expired.
     */
    public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 30;

    /**
     * Substitute you own sender ID here.
     */
    private static final String SENDER_ID = "806335698327";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCMDemo";

    private static GoogleCloudMessaging gcm;
    
    private static Context context;

    private static String regid;

    public static void initialize(Context _context) {
        context = _context;
        regid = getRegistrationId(context);

        if (regid.length() == 0) {
            registerBackground();
        }
        gcm = GoogleCloudMessaging.getInstance(context);
    }
    
    /**
     * Gets the current registration id for application on GCM service.
     * <p>
     * If result is empty, the registration has failed.
     *
     * @return registration id, or empty string if the registration is not
     *         complete.
     */
    private static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.length() == 0) {
            Log.v(TAG, "Registration not found.");
            return "";
        }
        // check if app was updated; if so, it must clear registration id to
        // avoid a race condition if GCM sends a message
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion || isRegistrationExpired()) {
            Log.v(TAG, "App version changed or registration expired.");
            return "";
        }
        return registrationId;
    }
    
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private static SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(GCMUtilities.class.getSimpleName(), Context.MODE_PRIVATE);
    }
    
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Checks if the registration has expired.
     *
     * <p>To avoid the scenario where the device sends the registration to the
     * server but the server loses it, the app developer may choose to re-register
     * after REGISTRATION_EXPIRY_TIME_MS.
     *
     * @return true if the registration has expired.
     */
    private static boolean isRegistrationExpired() {
        final SharedPreferences prefs = getGCMPreferences(context);
        // checks if the information is not stale
        long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
        return System.currentTimeMillis() > expirationTime;
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration id, app versionCode, and expiration time in the 
     * application's shared preferences.
     */
    private static void registerBackground() {
    	AsyncTask.execute(new Runnable() {
			
			public void run() {
				try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    Log.d("GCM", "Registration id is: " + regid);
                    
                    // sending the registration id to our server
                    String meetingServerRegID = ServerUtilities.register(GCMUtilities.getGmailAccount(), regid);
                    
                    // Save the registration id - no need to register again.
                    setRegistrationId(context, regid, meetingServerRegID);
                } catch (IOException ex) {
                	ex.printStackTrace();
                } catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
    }
    
    /**
     * Stores the registration id, app versionCode, and expiration time in the
     * application's {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration id
     */
    private static void setRegistrationId(Context context, String regId, String serverRegID) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.v(TAG, "Saving regId on app version " + appVersion);
        
        SharedPreferences.Editor editor = prefs.edit();
        
        // adding registration ids
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putString(PROPERTY_SERVER_REG_ID, serverRegID);
        
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        long expirationTime = System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS;

        Log.v(TAG, "Setting registration expiry time to " + new Timestamp(expirationTime));
        editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
        editor.commit();
    }
    
    private static String getGmailAccount(){
    	AccountManager mgr = AccountManager.get(context);
    	Account[] gAccounts = mgr.getAccountsByType("com.google");
    	
    	return gAccounts[0].name;
    }
}
