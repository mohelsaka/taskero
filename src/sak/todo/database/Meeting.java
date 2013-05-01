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
		cv.put("creator", this.creator);
		cv.put("estimate", this.estimate);
		cv.put("remote_id", this.remote_id);
		
		this.updated_at = System.currentTimeMillis();
		cv.put("updated_at", this.updated_at);

		// saving the meeting;
		SQLiteDatabase db = DBHelper.instance.getWritableDatabase();
		this.id = db.replace("meetings", null, cv);

		// saving candidate due dates
		// TODO: code refactoring
		for (int i = 0; i < this.collaborators.length; i++) {
			cv.clear();
			cv.put("meeting_id", this.id);
			cv.put("user_name", collaborators[i]);
			db.replace("meeting_connections", null, cv);
		}
		
		// saving candidate_dates
		for (int i = 0; i < this.dueDates.length; i++) {
			cv.clear();
			cv.put("meeting_id", this.id);
			cv.put("_date", dueDates[i]);
			cv.put("votes", votes[i]);
			cv.put("stared", stared[i]);
			db.replace("candidate_dates", null, cv);
		}
		
		db.close();
		return true;
	}
	public void saveNewDatesAndVotes(){
		SQLiteDatabase db = DBHelper.instance.getWritableDatabase();
		ContentValues cv = new ContentValues();
		// saving candidate_dates
		String [] args = new String[2]; 
		for (int i = 0; i < this.dueDates.length; i++) {
			cv.clear();
			
			// updates
			cv.put("votes", votes[i]);
			cv.put("stared", stared[i]);
			args[0] = ""+this.id;
			args[1] = ""+dueDates[i];
			int affectedRaws = db.update("candidate_dates", cv, "meeting_id = ? and _date = ?", args);
			
			// or create new
			if(affectedRaws == 0){
				cv.put("meeting_id", this.id);
				cv.put("_date", dueDates[i]);
				db.replace("candidate_dates", null, cv);
			}
		}
		// update meeting time stamp
		db.execSQL("update meetings set updated_at = "+System.currentTimeMillis()+" where _id = "+this.id+"; ");
		
		db.close();
	}
	

	public static Meeting findById(long id){
		Meeting m = new Meeting();
		m.id = id;
		m.load();
		return m;
	}
	
	public long load() {
		if (this.id < 0) {
			return -1;
		}
		
		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
		Cursor c = db.rawQuery("Select * from meetings Where _id = ?", new String[] { "" + this.id });

		c.moveToFirst();
		if (c.getCount() != 1) {
			Log.d(TAG, "Can't be loaded info: mteeting_id = " + this.id);
			return -1;
		}

		this.id = c.getLong(0);
		this.body = c.getString(1);
		this.creator = c.getString(2);
		this.status = c.getInt(3);
		this.updated_at = c.getLong(4);
		this.estimate = c.getFloat(5); 
		this.remote_id = c.getInt(6);
		c.close();
		
		// loading the candidate_dates
		c = db.rawQuery("Select * from candidate_dates Where meeting_id = ?", new String[] { "" + this.id });
		
		c.moveToFirst();
		int rowsCount = c.getCount();
		this.dueDates = new long[rowsCount];
		this.votes = new int[rowsCount];
		this.stared = new int[rowsCount];
		for (int i = 0; i < rowsCount; i++) {
			dueDates[i] = c.getLong(1);
			votes[i] = c.getInt(2);
			stared[i] = c.getInt(3);
			c.moveToNext();
		}
		c.close();
		
		// loading the collaboratos names
		if(collaborators == null){
			c = db.rawQuery("Select * from meeting_connections Where meeting_id = ?", new String[] { "" + this.id });
			
			c.moveToFirst();
			rowsCount = c.getCount();
			this.collaborators = new String[rowsCount];
			for (int i = 0; i < rowsCount; i++) {
				collaborators[i] = c.getString(1);
				c.moveToNext();
			}
			c.close();
		}

		
		db.close();
		return this.id;
	}
	
	// returns list of pair of undeclined meetings
	public static ArrayList<Pair<Long, String> > getAllMeetingsBody(){
		ArrayList<Pair<Long, String> > output = new ArrayList<Pair<Long, String> >();
		
		final String sql = "Select _id,body from meetings Where status = "+UNDECLINED;
		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		int rowsCount = c.getCount();
		
		Log.d(TAG, "meetings # "+rowsCount);
		for (int i = 0; i < rowsCount; i++) {
			output.add(new Pair<Long, String>(c.getLong(0), c.getString(1)));
			c.moveToNext();
		}
		
		c.close();
		db.close();
		return output;
	}
	
	/**
	 * Dirty meeting is:
	 * 1- Meetings created by the user and needs to sent to the server.
	 * 2- Meeting has been modified by the user:
	 * 			1- Added new Candidate Date.
	 * 			2- Voted up or down for candidate date.
	 * 3- Declined meetings.
	 * That's it.
	 * 
	 * @return JSON object contains data about the new object
	 * @param the last sync date where dirty meetings should be updated after it
	 * */
	public static JSONObject getDirtyMeetings(long lastSync) throws JSONException{
		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
		
		// getting the new meeting requests
		String sql = "select _id from meetings where updated_at > "+lastSync +" and remote_id = 0 ;";
		Cursor c = db.rawQuery(sql, null);
		int rawsCount = c.getCount();
		c.moveToFirst();
		
		Meeting temp = new Meeting();
		JSONArray newMeetingRequests = new JSONArray();
		for (int i = 0; i < rawsCount; i++) {
			temp.id = c.getLong(0);
			temp.load();
			newMeetingRequests.put(temp.toJson());
			c.moveToNext();
		}
		c.close();
		
		// getting the new updated requests
		sql = "select _id,remote_id from meetings where updated_at > "+lastSync+" and status = "+UNDECLINED 
				+" and remote_id != 0 ;";
		db = DBHelper.instance.getReadableDatabase();
		c = db.rawQuery(sql, null);
		rawsCount = c.getCount();
		c.moveToFirst();
		
		JSONArray newDueDatesAndVotes = new JSONArray();
		sql = "select * from candidate_dates where meeting_id = ?";
		for (int i = 0; i < rawsCount; i++) {
			Cursor c_dates = db.rawQuery(sql, new String[]{""+c.getLong(0)});
			c_dates.moveToFirst();
			
			JSONArray ar = new JSONArray();
			while (!c_dates.isAfterLast()) {
				ar.put(candidateDateToJSON(c_dates));
				c_dates.moveToNext();
			}
			JSONObject o = new JSONObject();
			o.put("meeting_id", c.getLong(1));// the remote id
			o.put("dates", ar);
			newDueDatesAndVotes.put(o);
			c.moveToNext();
		}
		c.close();
		
		// declined meetings
		// declined meetings will be deleted after the sycn process
		sql = "select remote_id from meetings where status = "+DECLINED+" ;";
		JSONArray declinedMeetings = new JSONArray();
		c = db.rawQuery(sql, null);
		while (!c.isAfterLast()) {
			declinedMeetings.put(c.getLong(0));
			c.moveToNext();
		}
		c.close();
		
		// closing the database object as we don't need it any more
		db.close();
		
		// preparing the json object
		JSONObject o = new JSONObject();
		o.put("declined", declinedMeetings);
		o.put("newRequests", newMeetingRequests);
		o.put("newUpdates", newDueDatesAndVotes);
		
		return o;
	}
	public JSONObject toJson() throws JSONException{
		JSONObject j = new JSONObject();
		j.put("id", this.id);
		j.put("body", body);
//		j.put("creator", this.creator);
		j.put("estimate", estimate);
		j.put("collaborators", new JSONArray(Arrays.asList(collaborators)));
		JSONArray temp = new JSONArray();
		for (long l : dueDates) {
			temp.put(l);
		}
		j.put("dueDates", temp);
//		j.put("votes", new JSONArray(Arrays.asList(votes)));
		temp = new JSONArray();
		for (int i : stared) {
			temp.put(i);
		}
		j.put("stared", temp);
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
	public String creator;
	public float estimate;
	
	private long remote_id = 0;
	
	// saved to differentiate between declined and undeclined meetings;
	public int status = UNDECLINED;
	public static final int DECLINED = 1;
	public static final int UNDECLINED = 0;
	public static final int NEWLY_CREATED = 2;
	
	public String [] collaborators;
	
	// due dates and their rating
	// decomposing {duedate, rate, stared}
	public long [] dueDates;
	public int [] votes;
	public int [] stared; // indicates whether this user check on this date or not (1 or 0)
	
	// time_stamp
	public long updated_at;
	
	/**
	 * This function takes an JSON object, parses into a meeting and saves it.
	 * @return the id of the saved meeting
	 * */
	
	public static long saveMeetingFromJSON(JSONObject meeting) throws JSONException{
		Meeting m = new Meeting();
//		m.id = meeting.getLong("id");
		m.body = meeting.getString("body");
		m.status = UNDECLINED;
		m.creator = meeting.getString("creator");
		m.estimate = meeting.getLong("estimate");
		m.collaborators = jsonArrayToStringArray(meeting.getJSONArray("collaborators"));
		m.dueDates = jsonArrayToLongArray(meeting.getJSONArray("due_dates"));
		m.votes = jsonArrayToIntArray(meeting.getJSONArray("votes"));
		m.stared = new int[m.votes.length];
		m.remote_id = meeting.getLong("remote_id");
		m.save();
		return m.id;
	}
	/**
	 * Server response may contain:
	 * 1- New meeting requests that needs to be reviewed by me.
	 * 2- New ratings for meetings which i supposed to contribute in.
	 * 3- The global id for meetings that i have just created.
	 * returns information for notifications
	 * Consequences:
	 * 1- Deleting the declined meetings.
	 * 2- Setting the user name (creator) for some meetings  
	 * @throws JSONException 
	 * */
	public static void processServerResponse(JSONObject response, String userName) throws JSONException{
		// parsing new requests
		JSONArray newRequests = response.getJSONArray("newRequests");
		int length = newRequests.length();
		for (int i = 0; i < length; i++) {
			JSONObject o = newRequests.getJSONObject(i);
			saveMeetingFromJSON(o);
		}
		
		
		// processing new rates
		// [{meeting_id,rating},..]  e.g. [{id:15,rating:[[date,rank],[1254287,5]...]}, ... ]
		SQLiteDatabase db = DBHelper.instance.getWritableDatabase();
		JSONArray newRating = response.getJSONArray("newRating");
		length = newRating.length();
		
		// the update statement 
		String sql = "update candidate_dates set votes = ? where "+
				"meeting_id = ? and _date = ? ;";
		String sqlForId = "select _id from meetings where remote_id = ?";
		for (int i = 0; i < length; i++) {
			JSONObject o = newRating.getJSONObject(i);
			// e.g.{id:15,rating:[[date,rank],[1254287,5]...]} NOTE: id is the remote id

			// extracting date from the JSON object
			long id = o.getLong("id"); //remote_id
			
			// getting the local id
			final Cursor c_id = db.rawQuery(sqlForId, new String[]{""+id});
			c_id.moveToFirst();
			id = c_id.getLong(0);
			
			JSONArray rating = o.getJSONArray("rating");
			//e.g. [[date,rank],[1254287,5]...]
			
			// update the rating by executing the SQL statement for each id and date 
			int count = rating.length();
			for (int j = 0; j < count; j++) {
				JSONArray dateVotePair = rating.getJSONArray(j);
				db.execSQL(sql, new String[]{""+dateVotePair.getInt(1), ""+id, ""+dateVotePair.getLong(0)});
			}
		}
		
		
		// updating the remote_id
		// server will send a map between the ids [[client,server],[5,15024]...]
		JSONArray idMap = response.getJSONArray("idMap");
		length = idMap.length();
		for (int i = 0; i < length; i++) {
			JSONArray ar = idMap.getJSONArray(i);
			updateMeetingId(ar.getLong(0), ar.getLong(1), db);
			db.execSQL(SQL_UPDATE_CREATOR, new String[]{userName});
		}
		
		// deleting declined meetings ... not needed any more as it has been reported to the server
		db.execSQL("delete from meetings where status = "+DECLINED+" ;");
		
		db.close();
	}
	
	
	private static String[] jsonArrayToStringArray(JSONArray ja) throws JSONException{
		String[] ar  = new String[ja.length()];
		for (int i = 0; i < ar.length; i++) {
			ar[i] = ja.getString(i);
		}
		return ar;
	}
	private static long[] jsonArrayToLongArray(JSONArray ja) throws JSONException{
		long[] ar  = new long[ja.length()];
		for (int i = 0; i < ar.length; i++) {
			ar[i] = Long.parseLong(ja.getString(i));
		}
		return ar;
	}
	private static int[] jsonArrayToIntArray(JSONArray ja) throws JSONException{
		int[] ar  = new int[ja.length()];
		for (int i = 0; i < ar.length; i++) {
			ar[i] = Integer.parseInt(ja.getString(i));
		}
		return ar;
	}
	
	private static void updateMeetingId(long oldId, long newID, SQLiteDatabase db){
		db.execSQL(SQL_1, new String[]{""+newID, ""+oldId});
//		db.execSQL(SQL_2, new String[]{""+oldId, ""+newID});
//		db.execSQL(SQL_3, new String[]{""+oldId, ""+newID});
	}
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
		return "Meeting [id=" + id + ", body=" + body + ", creator=" + creator
				+ ", estimate=" + estimate + ", remote_id=" + remote_id
				+ ", status=" + status + ", collaborators="
				+ Arrays.toString(collaborators) + ", dueDates="
				+ Arrays.toString(dueDates) + ", votes="
				+ Arrays.toString(votes) + ", stared="
				+ Arrays.toString(stared) + ", updated_at=" + updated_at + "]";
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
