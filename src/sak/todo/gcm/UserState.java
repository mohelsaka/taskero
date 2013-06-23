package sak.todo.gcm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import com.google.android.gms.internal.e;
import com.learner.svm.SVMAdapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import sak.todo.database.Task;
import sak.todo.database.TasksIterator;
import sak.todo.gui.CreateMeeting;


/**
 * This class contains the required information to describe the state of the user to the meeting server
 * 
 * The server will use this info to create global temp state for more than one user.
 * 
 * This state has to be synced with the user state on the server. We will implement this sync mechanism by
 * letting the user to push its state periodically, also the server may send a request to the user to push its state.
 * 
 * User state should has a time stamp, this time stamp indicates that last modification to user state and it has to be
 * updated whenever new task is added of removed.
 * 
 * The server may ask the user to push its state, the server will send the last modified date with its request. User will
 * check this date with its date, if they are the same then it send back the server telling him just to update its
 * time stamp to be NOW without sending its state, else it'll send its state thus the server will update the time stamp to be NOW.
 * */
public class UserState {
	int [] calendarState;
	int [] svmModel;
	
	
	/**
	 * Vector contains all tasks that has due date greater that now.
	 * */
	Vector<Task> tasks;
	
	/**
	 * This should be kept in {@code SharedPreferences}
	 * */
	long lastModifiedDate;
	
	/**
	 * This function returns a JSON object represents the current state of the user
	 * 
	 * It has to be like this one:
	 * 
	 * {
	 * 	tasks: [{
	 * 			priority: int,
	 * 			duedate: long,
	 * 			duration: float
	 * 			}, ...],
	 * model: [int, ...],
	 * }
	 * @throws NameNotFoundException 
	 * @throws IOException 
	 * 
	 * */
	public static JSONObject getCurrentUserStateAsJSON(Context context) throws NameNotFoundException, IOException{
		JSONObject state = new JSONObject();
		
		// get tasks info
		TasksIterator it = Task.getScheduledTasks(new Date(), new Date(Long.MAX_VALUE));
		JSONArray tasks = new JSONArray();
		Task t = null;
		while ((t = it.nextTask()) != null) {
			JSONObject o = new JSONObject();
			o.put("priority", t.priority);
			o.put("duedate", t.duedate.getTime());
			o.put("duration", t.estimate);
			tasks.put(o);
		}
		state.put("tasks", tasks);
		
		// getting SVM model weights
		SVMAdapter model = new SVMAdapter(context);
		Collection<Float> weights = new ArrayList<Float>();
		JSONArray arr = new JSONArray();
		arr.put(model.getSVMWeights());
		state.put("model", arr);
		
		return state;
	}
	
	/**
	 * CAUTION: this function calls {@code GCMUtilities}, thus {@link GCMUtilities} must be initialized first.
	 * */
	public static void pushUserState(final Context context){
		// getting user email
	    final String email =  GCMUtilities.getGmailAccount();
		
	    // getting user id
	    final String userID = GCMUtilities.getServerRegistrationId();
	    
	    final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    
		TimerTask syncProcess = new TimerTask() {
			
			@Override
			public void run() {
				try {
					NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
					boolean connected = (activeNetworkInfo != null && activeNetworkInfo.isConnected());
					
					if(!connected){
						reschedule();
						return;
					}
					
					boolean scucess = ServerUtilities.updateUserState(context, userID, email);
					if (!scucess) {
						reschedule();
					}
				} catch (Exception e) {
					e.printStackTrace();
					reschedule();
				}
			}
			
			private void reschedule(){
				Timer t = new Timer();
				Date d = new Date(System.currentTimeMillis() + (1000 * 30));
				t.schedule(this, d);
			}
		};
		
		syncProcess.run();
	}
}
