package sak.todo.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Friend {

	// these defines states of the frind connection or request
	public static final int NEW_REQUEST = 0; // request by me
	public static final int APPROVED = 1;	// reviewed by the friend and accepted
	public static final int FAILD = 2;		// friend declined the connection request
	public static final int CASHED = 3;		// has been retrieved from meeting
	public static final int WAITING_APPROVAL = 4; // wating to be apporved from me
	
	
	public String name;
	public String email;
	public int status;
	
	public static Friend findRemoteFriend(String name, String email){
		Friend f = new Friend();
		f.name = "saka";
		f.email = "mohamed.elsaka2007@gmail.com";
		f.status = APPROVED;
		return f;
	}
	public static void addFriend(Friend f){
		SQLiteDatabase db = DBHelper.instance.getWritableDatabase();
		
		ContentValues cv = new ContentValues();
		cv.put("user_name", f.name);
		cv.put("email", f.email);
		cv.put("status", f.status);
		
		db.insert("connections", null, cv);
		db.close();
	}
	private static final  String SQL_GET_FRIENDS = "select user_name from connections "+
													"where status = "+APPROVED+" ;";
	public static String[] getFriendsListNames(){
		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
		
		Cursor c = db.rawQuery(SQL_GET_FRIENDS, null);
		
		String [] friendsNames = new String[c.getCount()];
		for (int i = 0; i < friendsNames.length; i++) {
			friendsNames[i] = c.getString(0);
		}
		
		c.close();
		db.close();
		
		return friendsNames;
	}
}
