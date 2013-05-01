package sak.todo.syncadapter;

import java.io.IOException;

import org.apache.http.auth.AuthenticationException;
import org.json.JSONException;
import org.json.JSONObject;

import sak.todo.database.Meeting;
import sak.todo.network.NetworkUtilities;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class SyncAdapter extends AbstractThreadedSyncAdapter{

	private String TAG = "SyncAdapter";
	
    private final AccountManager mAccountManager;
    private static final String SYNC_MARKER_KEY = "sak.todo.sync.SyncMarker";
    
    
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		
		Log.d(TAG, "Sync Adapter Created");
		mAccountManager = AccountManager.get(context);
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		Log.d(TAG, "Syncing Started");
		
		try {
            // see if we already have a sync-state attached to this account. By handing
            // This value to the server, we can just get the contacts that have
            // been updated on the server-side since our last sync-up
            long lastSyncMarker = getServerSyncMarker(account);

            
            // Use the account manager to request the AuthToken we'll need
            // to talk to our sample server.  If we don't have an AuthToken
            // yet, this could involve a round-trip to the server to request
            // and AuthToken.
            // authTokenType > The auth token type, an authenticator-dependent string token, must not be null
            final String authtoken = mAccountManager.blockingGetAuthToken(account,
                    sak.todo.constants.Constants.AUTHTOKEN_TYPE, true);
            final String username = account.name;
            
            // getting the dirty data
			JSONObject dirtyData = Meeting.getDirtyMeetings(lastSyncMarker);
			
			// adding some information to help the server in syncing
			dirtyData.put("lastSync", lastSyncMarker);
			dirtyData.put("localTime", System.currentTimeMillis());
			
			// send dirty data
			JSONObject serverUpdates = NetworkUtilities.sendSyncData(dirtyData, account, authtoken);
			
			// process the response
			Meeting.processServerResponse(serverUpdates, username);
			
			// setting the lastSyncMarker flag
			mAccountManager.setUserData(account, SYNC_MARKER_KEY, Long.toString(System.currentTimeMillis()));
			
			Log.d(TAG, "Sync Done!");
			
			// put notifications
			
		} catch (JSONException e) {
			Log.d(TAG, "Data corrupted!");
			e.printStackTrace();
		} catch (OperationCanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthenticatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    /**
     * This helper function fetches the last known high-water-mark
     * we received from the server - or 0 if we've never synced.
     * @param account the account we're syncing
     * @return the change high-water-mark
     */
    private long getServerSyncMarker(Account account) {
        String markerString = mAccountManager.getUserData(account, SYNC_MARKER_KEY);
        if (!TextUtils.isEmpty(markerString)) {
            return Long.parseLong(markerString);
        }
        return 0;
    }
}
