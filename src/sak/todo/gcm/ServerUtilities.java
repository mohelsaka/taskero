package sak.todo.gcm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

import sak.todo.database.Meeting;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class ServerUtilities {

	private static final String TASKERO_SERVER_ADDRESS = "http://ec2-54-218-15-43.us-west-2.compute.amazonaws.com:3000/";
	private static final String USERS_ROUTE = "users.json";
	private static final String MEETINGS_ROUTE = "meetings.json";
	
	/**
	 * Registers the user on the server and sends back an id given by the server
	 * */
	public static String register(String email, String regid) throws ClientProtocolException, IOException, JSONException{
		// building JSON object to be sent
		JSONObject user = new JSONObject();
		user.put("email", email);
		user.put("registration_id", regid);
		user.put("state", "user state");
		
		JSONObject data = new JSONObject();
		data.put("user", user);
		
		StringEntity e = new StringEntity(data.toString());
		e.setContentType("application/json;charset=UTF-8");
		
		// building post request
		HttpPost httppost = new HttpPost(TASKERO_SERVER_ADDRESS + USERS_ROUTE);
		httppost.setEntity(e);
		
        // Execute HTTP Post Request
        HttpResponse response = new DefaultHttpClient().execute(httppost);
        Log.d("SERVER", response.getStatusLine().getReasonPhrase());
        byte[] buffer = new byte[1024];
        int byteCount = response.getEntity().getContent().read(buffer);
        
        org.json.JSONObject userJSON = new org.json.JSONObject(new String(buffer, 0, byteCount));
        String id = userJSON.getString("id");
        
        return id;
	}
	
	/**
	 * sends meeting request to the server
	 * 
	 * @return	String	meeting id at the server 
	 * @throws JSONException 
	 * */
	public static String requestMeeting(Meeting minfo, String userGmail) throws ClientProtocolException, IOException, JSONException{
		// building JSON object to be sent
		JSONObject meeting = new JSONObject();
		meeting.put("body", minfo.body);
		meeting.put("deadline", minfo.duedate); // this is virtual holder for deadline
		meeting.put("duration", minfo.estimate);
		
		String [] collaborators = minfo.collaborators.split(",");
		Vector<String> collVec = new Vector<String>();
		
		// the creator of the meeting is on top of the collaborators list
		collVec.add(userGmail);
		// add other collaborators
		for (String user : collaborators)
			collVec.add(user);
		
		meeting.put("collaborators", new JSONArray(collVec));
		
		JSONObject data = new JSONObject();
		data.put("meeting", meeting);
		
		StringEntity e = new StringEntity(data.toString());
		e.setContentType("application/json;charset=UTF-8");
		
		// building post request
		HttpPost httppost = new HttpPost(TASKERO_SERVER_ADDRESS + MEETINGS_ROUTE);
		httppost.setEntity(e);
		
		// Execute HTTP Post Request
		HttpResponse response = new DefaultHttpClient().execute(httppost);
        Log.d("SERVER", response.getStatusLine().getReasonPhrase());
        
        byte[] buffer = new byte[1024];
        int byteCount = response.getEntity().getContent().read(buffer);
        
        org.json.JSONObject meetingResponseJSON = new org.json.JSONObject(new String(buffer, 0, byteCount));
        String meetingID = meetingResponseJSON.getString("id");
        
        return meetingID;
	}
	
	public static boolean updateUserState(Context context, String userId, String userEmail) throws ClientProtocolException, IOException, NameNotFoundException{
		// building JSON object to be sent
		JSONObject user = new JSONObject();
		user.put("email", userEmail);
		user.put("state", UserState.getCurrentUserStateAsJSON(context));
		
		JSONObject data = new JSONObject();
		data.put("user", user);
		
		StringEntity e = new StringEntity(data.toString());
		e.setContentType("application/json;charset=UTF-8");
		
		// building post request
		HttpPut httpput = new HttpPut(TASKERO_SERVER_ADDRESS + "users/" + userId + ".json");
		httpput.setEntity(e);
		
        // Execute HTTP Post Request
        HttpResponse response = new DefaultHttpClient().execute(httpput);
        Log.d("SERVER", response.getStatusLine().getReasonPhrase());
        
        return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
	}
	
	/**
	 * sends an acceptance on the meeting specified by meetingRemoteID
	 * 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * */
	public static boolean acceptMeeting(String meetingRemoteID, String userID) throws ClientProtocolException, IOException{
		// building post request
		HttpPut httpput = new HttpPut(TASKERO_SERVER_ADDRESS + "meetings/" + meetingRemoteID + "/accept/"+ userID + ".json");
		
        // Execute HTTP Post Request
        HttpResponse response = new DefaultHttpClient().execute(httpput);
        Log.d("SERVER", response.getStatusLine().getReasonPhrase());
        
        return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
	}

	/**
	 * sending request to the server to decline the meeting specified by meetingRemoteID
	 * 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * */
	public static boolean declinetMeeting(String meetingRemoteID, String userID) throws ClientProtocolException, IOException{
		// building post request
		HttpPut httpput = new HttpPut(TASKERO_SERVER_ADDRESS + "meetings/" + meetingRemoteID + "/decline/"+ userID + ".json");
		
        // Execute HTTP Post Request
        HttpResponse response = new DefaultHttpClient().execute(httpput);
        Log.d("SERVER", response.getStatusLine().getReasonPhrase());
        
        return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
	}
}
