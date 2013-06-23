/**
 * @author M.Elsaka
 * 
 * */

package sak.todo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	// Database name and version
	private static final String DATABASE_NAME = "tasks.db";
	private static final int DATABASE_VERSION = 1;

	// table name
	public static final String TABLE_TASKS = "tasks";

	// Task Attributes

	public static final String[] TASK_ATTR_NAMES = {"_id", "body", "duedate", "impression", "estimate", "priority", "deadline"};
	
	public static final int COLUMN_ID_NUM = 0;
	public static final int COLUMN_BODY_NUM = 1;
	public static final int COLUMN_DUE_DATE_NUM = 2;
	public static final int COLUMN_IMPRESSION_NUM = 3;
	public static final int COLUMN_ESTIMATE_NUM = 4;
	public static final int COLUMN_PRIORITY_NUM = 5;
	public static final int COLUMN_DEADLINE_NUM = 6;
	
	
	// Task Logger table declaration
	public static final String TABLE_TASK_LOG = "task_log";
	public static final String[] LOG_ATTR_NAMES = {"task_id", "att_num", "new_val"};
	public static final int COLUMN_TASK_ATTR_NUM = 1;
	public static final int COLUMN_TASK_NEW_VALUE = 2;
	
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE " + TABLE_TASKS + "(" 
				+ TASK_ATTR_NAMES[COLUMN_ID_NUM]	+ " INTEGER	PRIMARY KEY AUTOINCREMENT," 
				+ TASK_ATTR_NAMES[COLUMN_BODY_NUM] + " TEXT NOT NULL,"
				+ TASK_ATTR_NAMES[COLUMN_DUE_DATE_NUM] + " INTEGER," 
				+ TASK_ATTR_NAMES[COLUMN_IMPRESSION_NUM] + " INTEGER," 
				+ TASK_ATTR_NAMES[COLUMN_ESTIMATE_NUM] + " REAL,"
				+ TASK_ATTR_NAMES[COLUMN_PRIORITY_NUM] + " INTEGER,"
				+ TASK_ATTR_NAMES[COLUMN_DEADLINE_NUM] + " INTEGER"
				+ ");";
		
		db.execSQL(sql);
		
		// creating task logger
		sql = "create table task_log(" + 
				"task_id INTEGER," + 
				"att_num INTEGER," + 
				"new_val text" + 
				");";
		db.execSQL(sql);
		
		// creating task logger index
		sql = "CREATE INDEX task_log_idx ON task_log(task_id, att_num);";
		db.execSQL(sql);
		
		// create meeting table
		sql = "create table meetings (" + 
				"_id integer PRIMARY KEY AUTOINCREMENT," + 
				"body text, " +
				"status INTEGER, "+
				"estimate REAL,"+
				"duedate INTEGER,"+
				"remote_id INTEGER,"+
				"collaborators text"+
				");";
		db.execSQL(sql);
		
		// creating candidate_dates table
		sql="create table candidate_dates(" + 
				"meeting_id integer," + 
				"_date	INTEGER," + 
				"votes	INTEGER," +
				"stared INTEGER,"+
				"FOREIGN KEY(meeting_id) REFERENCES meeting(_id)"+
				");";
		db.execSQL(sql);
		
		// creating task candidate_dates
		sql = "CREATE INDEX candidate_dates_idx ON candidate_dates(meeting_id, _date);";
		db.execSQL(sql);
		
		
		// creating connections table
		sql="create table connections(" + 
				"user_name text PRIMARY KEY," + 
				"email text," + 
				"status INTEGER" + 
				");";
		db.execSQL(sql);
		
		// creating meeting_connections table
		sql="create table meeting_connections(" + 
				"meeting_id INTEGER," + 
				"user_name text," +
				"FOREIGN KEY(user_name) REFERENCES connections(user_name),"+
				"FOREIGN KEY(meeting_id) REFERENCES meeting(_id)"+
				")";
		db.execSQL(sql);
		
		// creating task candidate_dates
		sql = "CREATE INDEX meeting_connections_idx ON meeting_connections(meeting_id, user_name);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		throw new UnsupportedOperationException("Date base has no upgrade function");
	}

	public static DBHelper instance = null;

	/**
	 * This Function initializes the DataBaseHelper with a context
	 * 
	 * NOTE: Initializing the database is essentially for accessing it OR Null
	 * Pointer Exception will be thrown.
	 * */
	public static void initialize(Context context) {
		if(instance == null)
			instance = new DBHelper(context);
	}
	
}
