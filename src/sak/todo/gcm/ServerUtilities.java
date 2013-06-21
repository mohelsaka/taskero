package sak.todo.gcm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class ServerUtilities {

	private static final String TASKERO_SERVER_ADDRESS = "http://192.168.171.101:3000/";
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
	 * Temp Meeting class, used until the final version of meeting class be ready. 
	 * */
	static class Meeting{
		String body;
		long deadLine;
		Vector<String> collaborators;
	}
	
	/**
	 * sends meeting request to the server
	 * */
	public static void requestMeeting(Meeting minfo) throws ClientProtocolException, IOException{
		// building JSON object to be sent
		JSONObject meeting = new JSONObject();
		meeting.put("body", minfo.body);
		meeting.put("deadline", minfo.deadLine);
		meeting.put("collaborators", new JSONArray(minfo.collaborators));
		
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
	}
	
	public static void updateUserState(Context context, int userId, String userEmail) throws ClientProtocolException, IOException, NameNotFoundException{
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
	}
}
