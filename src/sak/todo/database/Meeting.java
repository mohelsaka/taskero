package sak.todo.database;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

public class Meeting {
	private static String TAG = "Meetings";
	

	
	public boolean save() {
		ContentValues cv = new ContentValues();

		if(id > 0)
			cv.put("_id", this.id);
		
		cv.put("body", this.body);
		cv.put("status", this.status);
		cv.put("estimate", this.estimate);
		cv.put("remote_id", this.remote_id);
		cv.put("collaborators", this.collaborators);
		cv.put("duedate", this.duedate);
		

		// saving the meeting;
		SQLiteDatabase db = DBHelper.instance.getWritableDatabase();
		this.id = db.replace("meetings", null, cv);

		
		db.close();
		return true;
	}
		

	public static Meeting findByRemoteId(long remote_id){
		Meeting m = new Meeting();
		m.remote_id = remote_id;
		m.loadByRemoteID();
		return m;
	}
	
	public static Meeting findById(long id){
		Meeting m = new Meeting();
		m.id = id;
		m.loadByID();
		return m;
	}

	public long loadByRemoteID() {
		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
		Cursor c = db.rawQuery("Select * from meetings Where remote_id = ?", new String[] { "" + this.remote_id });

		c.moveToFirst();
		if (c.getCount() != 1) {
			Log.d(TAG, "Can't be loaded info: mteeting_remote_id = " + this.remote_id);
			return -1;
		}

		this.id = c.getLong(0);
		this.body = c.getString(1);
		this.status = c.getInt(2);
		this.estimate = c.getFloat(3);
		this.duedate = c.getLong(4);
		this.remote_id = c.getLong(5);
		this.collaborators = c.getString(6);
		c.close();
		
		db.close();
		return this.id;
	}
	
	public long loadByID() {
		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
		Cursor c = db.rawQuery("Select * from meetings Where _id = ?", new String[] { "" + this.id });

		c.moveToFirst();
		if (c.getCount() != 1) {
			Log.d(TAG, "Can't be loaded info: mteeting_id = " + this.id);
			return -1;
		}

		this.id = c.getLong(0);
		this.body = c.getString(1);
		this.status = c.getInt(2);
		this.estimate = c.getFloat(3);
		this.duedate = c.getLong(4);
		this.remote_id = c.getLong(5);
		this.collaborators = c.getString(6);
		c.close();
		
		db.close();
		return this.id;
	}
	
	
//	/**
//	 * Dirty meeting is:
//	 * 1- Meetings created by the user and needs to sent to the server.
//	 * 2- Meeting has been modified by the user:
//	 * 			1- Added new Candidate Date.
//	 * 			2- Voted up or down for candidate date.
//	 * 3- Declined meetings.
//	 * That's it.
//	 * 
//	 * @return JSON object contains data about the new object
//	 * @param the last sync date where dirty meetings should be updated after it
//	 * */
//	public static JSONObject getDirtyMeetings(long lastSync) throws JSONException{
//		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
//		
//		// getting the new meeting requests
//		String sql = "select _id from meetings where updated_at > "+lastSync +" and remote_id = 0 ;";
//		Cursor c = db.rawQuery(sql, null);
//		int rawsCount = c.getCount();
//		c.moveToFirst();
//		
//		Meeting temp = new Meeting();
//		JSONArray newMeetingRequests = new JSONArray();
//		for (int i = 0; i < rawsCount; i++) {
//			temp.id = c.getLong(0);
//			temp.load();
//			newMeetingRequests.put(temp.toJson());
//			c.moveToNext();
//		}
//		c.close();
//		
//		// getting the new updated requests
//		sql = "select _id,remote_id from meetings where updated_at > "+lastSync+" and status = "+PENDING 
//				+" and remote_id != 0 ;";
//		db = DBHelper.instance.getReadableDatabase();
//		c = db.rawQuery(sql, null);
//		rawsCount = c.getCount();
//		c.moveToFirst();
//		
//		JSONArray newDueDatesAndVotes = new JSONArray();
//		sql = "select * from candidate_dates where meeting_id = ?";
//		for (int i = 0; i < rawsCount; i++) {
//			Cursor c_dates = db.rawQuery(sql, new String[]{""+c.getLong(0)});
//			c_dates.moveToFirst();
//			
//			JSONArray ar = new JSONArray();
//			while (!c_dates.isAfterLast()) {
//				ar.put(candidateDateToJSON(c_dates));
//				c_dates.moveToNext();
//			}
//			JSONObject o = new JSONObject();
//			o.put("meeting_id", c.getLong(1));// the remote id
//			o.put("dates", ar);
//			newDueDatesAndVotes.put(o);
//			c.moveToNext();
//		}
//		c.close();
//		
//		// declined meetings
//		// declined meetings will be deleted after the sycn process
//		sql = "select remote_id from meetings where status = "+DECLINED+" ;";
//		JSONArray declinedMeetings = new JSONArray();
//		c = db.rawQuery(sql, null);
//		while (!c.isAfterLast()) {
//			declinedMeetings.put(c.getLong(0));
//			c.moveToNext();
//		}
//		c.close();
//		
//		// closing the database object as we don't need it any more
//		db.close();
//		
//		// preparing the json object
//		JSONObject o = new JSONObject();
//		o.put("declined", declinedMeetings);
//		o.put("newRequests", newMeetingRequests);
//		o.put("newUpdates", newDueDatesAndVotes);
//		
//		return o;
//	}
	public JSONObject toJson() throws JSONException{
		JSONObject j = new JSONObject();
		j.put("id", this.id);
		j.put("body", body);
		j.put("estimate", estimate);
		j.put("collaborators", collaborators);
		j.put("duedate",duedate);

		return j;
	}
	private static JSONObject candidateDateToJSON(Cursor c) throws JSONException{
		JSONObject o = new JSONObject();
		o.put("date", c.getLong(1));
		o.put("votes", c.getInt(2));
		o.put("stared", c.getInt(3));
		return o;
	}
	public long id = -1;
	public String body;
	public float estimate;
	
	
	public long remote_id = 0;
	
	// saved to differentiate between declined and undeclined meetings;
	public int status = PENDING;
	public static final int PENDING = 0;
	public static final int  CONFIRMED = 1;
	public static final int  DECLINED = 2;
	
	public String  collaborators = "";
	
	// due dates and their rating
	// decomposing {duedate, rate, stared}
	public long  duedate;

	
	
	
	
	/**
	 * This function takes an JSON object, parses into a meeting and saves it.
	 * @return the meeting that has been saved
	 * */
	//meeting={"body":"dhfgj","created_at":"2013-06-30T09:39:07Z","deadline":2147483647,"duedate":1372591820000,"duration":2.0,"id":7,"state":null,"updated_at":"2013-06-30T11:30:21Z","users":[{"email":"mohamed.elsaka2007@gmail.com"},{"email":"moamen.elgendy2010@gmail.com"}]}}
	public static Meeting saveMeetingFromJSON(JSONObject meeting) throws JSONException{
		Meeting m = new Meeting();
		m.body = meeting.getString("body");
		m.status = PENDING;
		m.estimate = meeting.getLong("duration");
		
		JSONArray ar = meeting.getJSONArray("users");
		JSONArray collaboratorsArray = new JSONArray();
		for (int i = 0; i < ar.length(); i++) {
			JSONObject o = ar.getJSONObject(i);
			collaboratorsArray.put(o.getString("email"));
		}
		m.collaborators = collaboratorsArray.toString();
		
		m.duedate = meeting.getLong("duedate");
		m.remote_id = meeting.getInt("id");
		m.save();
		
		if(m.id > 0)
			return m;
		else
			return null;
	}
		
	
//	private static String[] jsonArrayToStringArray(JSONArray ja) throws JSONException{
//		String[] ar  = new String[ja.length()];
//		for (int i = 0; i < ar.length; i++) {
//			ar[i] = ja.getString(i);
//		}
//		return ar;
//	}
//	private static long[] jsonArrayToLongArray(JSONArray ja) throws JSONException{
//		long[] ar  = new long[ja.length()];
//		for (int i = 0; i < ar.length; i++) {
//			ar[i] = Long.parseLong(ja.getString(i));
//		}
//		return ar;
//	}
//	private static int[] jsonArrayToIntArray(JSONArray ja) throws JSONException{
//		int[] ar  = new int[ja.length()];
//		for (int i = 0; i < ar.length; i++) {
//			ar[i] = Integer.parseInt(ja.getString(i));
//		}
//		return ar;
//	}
//	
//	private static void updateMeetingId(long oldId, long newID, SQLiteDatabase db){
//		db.execSQL(SQL_1, new String[]{""+newID, ""+oldId});
////		db.execSQL(SQL_2, new String[]{""+oldId, ""+newID});
////		db.execSQL(SQL_3, new String[]{""+oldId, ""+newID});
//	}
	private static final String SQL_1 = "update meetings set remote_id = ? where _id = ? ;";
	private static final String SQL_UPDATE_CREATOR = "update meetings set creator = ? where _id = ? ;";
	
//	private static final String SQL_2 = "update candidate_dates set meeting_id = ? where meeting_id = ? ;";
//	private static final String SQL_3 = "update meeting_connections set meeting_id = ? where meeting_id = ? ;";



	public void decline() {
		SQLiteDatabase db = DBHelper.instance.getWritableDatabase();
		final String sql = "delete from meetings where _id = ? ;";
		db.execSQL(sql,new String[]{""+id});
		db.close();
	}
	@Override
	public String toString() {
		return "Meeting [id=" + id + ", body=" + body
				+ ", estimate=" + estimate + ", remote_id=" + remote_id
				+ ", status=" + status + ", collaborators="
				+ collaborators + duedate+ "]";
	}
	
	private static String DELETE_SQL = "delete from meetings where _id = ? ;";
	public static void deleteById(long id){
		SQLiteDatabase db = DBHelper.instance.getWritableDatabase();
		db.execSQL(DELETE_SQL, new String[]{""+id});
		db.close();
	}
	private static String DELETE_ALL_SQL = "delete from meetings ;";
	public static void deleteAll(){
		SQLiteDatabase db = DBHelper.instance.getWritableDatabase();
		db.execSQL(DELETE_ALL_SQL);
		db.close();
	}
}
