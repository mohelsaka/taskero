/**
 * @author M.Elsaka
 * */

package sak.todo.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import cr.Interval;

import sak.todo.database.DBHelper;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

/**
 * This class represents the task and provides all functionalities to manipulate
 * the task from a logical point of view. It hides all database manipulation
 * operations.
 * 
 * NOTE: No much validation is represented her nor in the DBMS. So, be careful
 * when assigning values to tasks attributes
 * 
 * ToDO: adding more verifications and error handlers
 * */
public class Task implements Comparable<Task>, Parcelable, Cloneable {
	private static String TAG = "Tasks";


	public Date end;
	public ArrayList<Task> before;

	public ArrayList<Task> after;
//	public Date deadLine;
	public ArrayList<Interval> beforeIntervals;

	public ArrayList<Interval> afterIntervals;
	public static Task NULLTASK = new Task(null, 0);
	

	public Task() {
		after = new ArrayList<Task>();
		afterIntervals = new ArrayList<Interval>();
		before = new ArrayList<Task>();
		beforeIntervals = new ArrayList<Interval>();
		deadline = new Date();
		setDueDate(0);
	}
	Task(String body, float estimate){
		this();
		this.body = body;
		this.estimate = estimate;
	}
	public Task(String body, float estimate, Date deadLine){
		this(body, estimate);
		this.deadline = deadLine;
		NULLTASK.addAfter(this, new Interval(System.currentTimeMillis(), deadLine.getTime()-(long)estimate*60*60*1000));
	}
	
	public void setDueDate(long d){
		duedate = new Date(d);
		end = new Date((long) (d+estimate*60*60*1000));
	}
	public void addBefore(Task task, Interval interval){
		before.add(task);
		beforeIntervals.add(interval);
	}
	
	public void addAfter(Task task, Interval interval){
		after.add(task);
		afterIntervals.add(interval);
	}
	public void setBeforeConstraints(ArrayList<Task> before, ArrayList<Interval> beforeIntervals) {
		this.before = before;
		this.beforeIntervals = beforeIntervals;
	}
	public void setAfterConstraints(ArrayList<Task> after, ArrayList<Interval> afterIntervals) {
		this.after = after;
		this.afterIntervals = afterIntervals;
	}
	
	
	public int getDay() {
		return duedate.getDay();
	}


	
	
	
	// -VE: deleted or newly created, +VE: saved
	public long id = -1;

	public String body;

	public Date duedate;
	public Date deadline;
	public float estimate; // estimate in hours
	public int impression; // 0...10
	public int priority; // 0...10

	/**
	 * Saves this task in the database: if the task is a newly created one (id =
	 * 0), it will create new on in the database. if the task is just has been
	 * fetched from the database (id > 0), it will update its copy in the
	 * database.
	 * 
	 * If you want to save only one single field which has been updated, user
	 * updateSingleField instead.
	 * 
	 * @return boolean true if save completed successfully
	 * */
	public boolean save() {
		ContentValues cv = new ContentValues();

		if (this.id >= 0)
			cv.put(DBHelper.TASK_ATTR_NAMES[DBHelper.COLUMN_ID_NUM], id);

		cv.put(DBHelper.TASK_ATTR_NAMES[DBHelper.COLUMN_BODY_NUM], body);
		cv.put(DBHelper.TASK_ATTR_NAMES[DBHelper.COLUMN_DUE_DATE_NUM],
				duedate != null ? duedate.getTime() : 0);
		cv.put(DBHelper.TASK_ATTR_NAMES[DBHelper.COLUMN_IMPRESSION_NUM],
				impression);
		cv.put(DBHelper.TASK_ATTR_NAMES[DBHelper.COLUMN_ESTIMATE_NUM], estimate);
		cv.put(DBHelper.TASK_ATTR_NAMES[DBHelper.COLUMN_PRIORITY_NUM], priority);
		cv.put(DBHelper.TASK_ATTR_NAMES[DBHelper.COLUMN_DEADLINE_NUM],
				deadline != null ? deadline.getTime() : 0);

		SQLiteDatabase db = DBHelper.instance.getWritableDatabase();
		long result = db.replace(DBHelper.TABLE_TASKS, null, cv);
		db.close();

		if (result > 0) {
			this.id = result;
			Log.d(TAG, "task: " + this.body + " is saved.");
			return true;
		}
		Log.d(TAG, "Error while saving task: " + this.body);
		return false;
	}

	/**
	 * This function loads 'this' task from the database
	 * 
	 * @return the loaded id of the task (instead of calling task.id again!) or
	 *         -1 if the task can't be loaded.
	 * */
	public long load() {
		if (this.id < 0) {
			return -1;
		}
		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
		Cursor c = db.rawQuery("Select * from " + DBHelper.TABLE_TASKS
				+ " Where _id = ?", new String[] { "" + this.id });

		c.moveToFirst();
		if (c.getCount() != 1) {
			Log.d(TAG, "Can't be loaded info: Task_id = " + this.id);
			return -1;
		}

		this.id = c.getLong(DBHelper.COLUMN_ID_NUM);
		this.body = c.getString(DBHelper.COLUMN_BODY_NUM);

		long d = c.getLong(DBHelper.COLUMN_DUE_DATE_NUM);
		this.duedate = (d == 0 ? null : new Date(d));

		this.estimate = c.getFloat(DBHelper.COLUMN_ESTIMATE_NUM);
		this.impression = c.getInt(DBHelper.COLUMN_IMPRESSION_NUM);
		this.priority = c.getInt(DBHelper.COLUMN_PRIORITY_NUM);

		d = c.getLong(DBHelper.COLUMN_DEADLINE_NUM);
		this.deadline = (d == 0 ? null : new Date(d));

		c.close();
		db.close();
		return this.id;
	}

	/**
	 * This function deletes 'this' task from the database and sets its id to
	 * null.
	 * 
	 * @return return true if the element was successfully deleted, false
	 *         otherwise.
	 * 
	 *         TODO: this function would be deprecated soon!
	 * */
	public boolean delete() {
		return deleteById(this.id);
	}

	/**
	 * Deletes a task determined by its id.
	 * 
	 * @return return true if the element was successfully deleted, false
	 *         otherwise.
	 * */
	public static boolean deleteById(long id) {
		SQLiteDatabase db = DBHelper.instance.getWritableDatabase();
		long result = db.delete(DBHelper.TABLE_TASKS, "_id = ?",
				new String[] { "" + id });
		db.close();

		if (result == 1) {
			Log.d(TAG, "Task : " + id + " was deleted.");
			return true;
		}
		return false;
	}

	/**
	 * This function returns a task with a specified id. It loads it from the
	 * database.
	 * 
	 * @return returns the loaded task or null if loading failed
	 */
	public static Task findById(long id) {
		Task t = new Task();
		t.id = id;
		if (t.load() == -1)
			return null;
		return t;
	}

	/**
	 * This function return an array of ids of the tasks that has been scheduled
	 * in a specific day.
	 * 
	 * @param Date
	 *            day: specifying the day of the tasks
	 * @return long[]: ids of the tasks that is scheduled in this day.
	 * 
	 *         TODO: test this function.
	 * */
	public static long[] getTasksOfDay(Date day) {
		// setting the day time to 00:00 am
		day.setHours(0);
		day.setMinutes(0);
		day.setSeconds(0);

		// getting the start and the end of the day
		long start = day.getTime();
		// milliseconds in one day = 24*60*60*1000
		long end = start + 86400000;

		// performing the query on the database
		String dueDate = DBHelper.TASK_ATTR_NAMES[DBHelper.COLUMN_DUE_DATE_NUM];
		String sql = "select _id from " + DBHelper.TABLE_TASKS + " where "
				+ dueDate + " > " + start + " AND " + dueDate + " < " + end;

		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
		Cursor c = db.rawQuery(sql, new String[] {});
		c.moveToFirst();

		// copying ids from the cursor to and array to be returned
		int rowsCount = c.getCount();
		long[] ids = new long[rowsCount];

		for (int i = 0; i < ids.length; i++) {
			ids[i] = c.getLong(0);
			c.moveToNext();
		}
		c.close();
		db.close();
		return ids;
	}

	/**
	 * This function return an iterator on the scheduled tasks in a specific
	 * period
	 * 
	 * i.e. The scheduled taks are tasks have (startDate < Task.deudate <
	 * endDate)
	 * 
	 * @param startDate
	 *            specifing the start date
	 * @param endDate
	 *            specifing the end date
	 * @return : iterator on the tasks.
	 * 
	 *         TODO: test this function.
	 * */
	public static TasksIterator getScheduledTasks(Date startDate, Date endDate) {

		// getting the start and the end
		long start = startDate.getTime();
		long end = endDate.getTime();

		// preparing the query on the database GETTING SCEDHULED TASKS (duedate
		// == 0)
		String duedate = DBHelper.TASK_ATTR_NAMES[DBHelper.COLUMN_DUE_DATE_NUM];
		String sql = "select * from " + DBHelper.TABLE_TASKS + " where "
				+ duedate + " > " + start + " AND " + duedate + " < " + end
				+ " order by " + duedate + " ASC " + ";";

		// performing the query
		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
		Cursor c = db.rawQuery(sql, new String[] {});
		c.moveToFirst();

		// returing new task iterator and return it
		return new TasksIteratorImp(c, db);
	}

	public static TasksIterator getAllTasks() {
		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
		String duedate = DBHelper.TASK_ATTR_NAMES[DBHelper.COLUMN_DUE_DATE_NUM];
		String sql = "select * from " + DBHelper.TABLE_TASKS + " where "
				+ duedate + " != 0 " + " order by " + duedate + " ASC " + ";";
		Cursor c = db.rawQuery(sql, new String[] {});
		c.moveToFirst();
		return new TasksIteratorImp(c, db);
	}
	
	public static Cursor getAllTasksPointedAtToday(){
		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
		String duedate = DBHelper.TASK_ATTR_NAMES[DBHelper.COLUMN_DUE_DATE_NUM];
		String sql = "select * from " + DBHelper.TABLE_TASKS + " where "
				+ duedate + " != 0 " + " order by duedate ASC;";
		
		Cursor c = db.rawQuery(sql, new String[] {});
		
		// set the cursor to be at the tasks of today
		sql = "select * from " + DBHelper.TABLE_TASKS + " where "
				+ duedate + " < ?  ;";
		
		Cursor oldTasks = db.rawQuery(sql, new String[] {"" + System.currentTimeMillis()});
		
		int numOfOldTasks = oldTasks.getCount();
		c.move(numOfOldTasks);
		
		return c;
	}

	/**
	 * This function returns the tasks in the backlog
	 * 
	 * i.e. backlog tasks are tasks have NULL duedate
	 * */
	public static TasksIterator getBackLogtask() {
		// preparing the query on the database GETTING UNSCEDHULED TASKS
		// (duedate == 0)
		String duedate = DBHelper.TASK_ATTR_NAMES[DBHelper.COLUMN_DUE_DATE_NUM];
		String sql = "select * from " + DBHelper.TABLE_TASKS + " where "
				+ duedate + " = 0";

		// performing the query
		SQLiteDatabase db = DBHelper.instance.getReadableDatabase();
		Cursor c = db.rawQuery(sql, new String[] {});
		c.moveToFirst();

		// returing new task iterator and return it
		return new TasksIteratorImp(c, db);
	}

	@Override
	public String toString() {
		return "Task [id=" + id + ", body=" + body + ", duedate=" + duedate
				+ ", impression=" + impression + ", estimate=" + estimate
				+ ", priority=" + priority + ", deadline=" + deadline + "]";
	}

	public static class TasksIteratorImp implements TasksIterator {

		private Cursor c;
		private boolean end;
		SQLiteDatabase db;

		TasksIteratorImp(Cursor _c, SQLiteDatabase _db) {
			this.c = _c;
			this.db = _db;
			end = c.getCount() == 0 ? true : false;
			if (end) {
				c.close();
				db.close();
			}
		}

		public Task nextTask() {
			if (end) {
				c.close();
				db.close();
				return null;
			}
			if (c.isLast()) {
				end = true;
			}

			Task t = new Task();
			t.id = c.getLong(DBHelper.COLUMN_ID_NUM);
			t.body = c.getString(DBHelper.COLUMN_BODY_NUM);

			long d = c.getLong(DBHelper.COLUMN_DUE_DATE_NUM);
			t.duedate = (d == 0 ? null : new Date(d));

			t.estimate = c.getFloat(DBHelper.COLUMN_ESTIMATE_NUM);
			t.impression = c.getInt(DBHelper.COLUMN_IMPRESSION_NUM);
			t.priority = c.getInt(DBHelper.COLUMN_PRIORITY_NUM);

			d = c.getLong(DBHelper.COLUMN_DEADLINE_NUM);
			t.deadline = (d == 0 ? null : new Date(d));

			c.moveToNext();

			return t;
		}

		public Cursor getCursor() {
			// TODO Auto-generated method stub
			return c;
		}
	}

	/**
	 * Updating single value in the task. Calling this function to update a
	 * single attribute in the task is better than changin the value directly
	 * and calling task.save function
	 * 
	 * e.g. use: Task.updateSingleField(t.id, DBHelper.COLUMN_BODY_NUM, new
	 * body) instead of: t.body = "new body" t.save
	 * */
	public static boolean updateSinglField(long ID, int fieldNum,
			String newValue) {
		SQLiteDatabase db = DBHelper.instance.getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put(DBHelper.TASK_ATTR_NAMES[fieldNum], newValue);

		int rowsAffected = db.update(DBHelper.TABLE_TASKS, cv, "_id = ?",
				new String[] { "" + ID });
		db.close();

		return rowsAffected == 1;
	}

	public static Task[] getBackLogTasks() {
		// TODO Auto-generated method stub
		TasksIterator ti = Task.getBackLogtask();
		int count = ti.getCursor().getCount();
		Task[] tasks = new Task[count];
		Task t = ti.nextTask();
		int k = 0;
		while (t != null) {
			tasks[k++] = t;
			t = ti.nextTask();
		}
		return tasks;
	}

	public int compareTo(Task another) {
		// TODO Auto-generated method stub
		if (this.duedate.before(another.duedate))
			return -1;
		else
			return 1;
	}

	public static Task[] getTasksOfDayFull(Date day) {
		long ids[] = getTasksOfDay(day);
		int rowsCount = ids.length;
		Task[] tasks = new Task[rowsCount];
		for (int i = 0; i < ids.length; i++) {
			tasks[i] = findById(ids[i]);
		}
		return tasks;
	}
	
	public Task(Date _duedate, float _estimate, String _body, long _id){
		this.duedate = _duedate;
		this.estimate = _estimate;
		this.body = _body;
		this.id = _id;
	}
	
	public static Vector<Task> getTasksForCalendar() {
		Vector<Task> v = new Vector<Task>();
		Task t = new Task();
		t.duedate = new Date(2013, 3, 0, 7, 30);
		t.estimate = 3.5f;
		t.body = "this task is to solve AI sheet DR Marwan bta3 doctor marwan!";
		v.add(t);

		t = new Task();
		t.duedate = new Date(2013, 3, 1, 7, 0);
		t.estimate = 1f;
		t.body = "Solving AI sheet";
		v.add(t);

		t = new Task();
		t.duedate = new Date(2013, 3, 4, 12, 30);
		t.estimate = 1.25f;
		t.body = "Solving AI sheet";
		v.add(t);

		t = new Task();
		t.duedate = new Date(2013, 3, 2, 20, 0);
		t.estimate = 1.5f;
		t.body = "Solving AI sheet";
		v.add(t);

		t = new Task();
		t.duedate = new Date(2013, 3, 6, 7, 30);
		t.estimate = 0.5f;
		t.body = "Solving AI sheet";
		v.add(t);

		t = new Task();
		t.duedate = new Date(2013, 3, 5, 0, 0);
		t.estimate = 1.0f;
		t.body = "Solving AI sheet";
		v.add(t);

		t = new Task();
		t.duedate = new Date(2013, 3, 3, 12 + 6, 30);
		t.estimate = 2.5f;
		t.body = "Solving AI sheet";
		v.add(t);

		return v;
	}
	
	@Override
	public Task clone() throws CloneNotSupportedException {
		Task t = new Task((Date)this.duedate.clone(), this.estimate, this.body, this.id);
		t.priority = this.priority;
		t.deadline = this.deadline;
		return t;
	}

	public Task(Parcel in) {
		this();
		readFromParcel(in);
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {

		// We just need to write each field into the
		// parcel. When we read from parcel, they
		// will come back in the same order
		if (duedate != null) {
			dest.writeLong(duedate.getTime());
			dest.writeLong(end.getTime());
			dest.writeLong(deadline.getTime());
			dest.writeLong(id);
			dest.writeFloat(estimate);
			dest.writeString(body);	
		}
	}

	private void readFromParcel(Parcel in) {

		// We just need to read back each
		// field in the order that it was
		// written to the parcel
		duedate = new Date(in.readLong());
		end = new Date(in.readLong());
		deadline = new Date(in.readLong());
		id = in.readLong();
		estimate = in.readFloat();
		body = in.readString();
		
	}

	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public Task createFromParcel(Parcel in) {
			return new Task(in);
		}

		public Task[] newArray(int size) {
			return new Task[size];
		}
	};


	public Date getStartDate() {
		return duedate;
	}
	public Date getEndDate() {
		return end;
	}
	
	public static String getFormatedDate(Calendar c){
		return String.format("%s %s %d %2d:%2d %s",
				c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US),
				c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US),
				c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.HOUR),
				c.get(Calendar.MINUTE),
				c.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US)
				);
	}
	
	public static final int[] PRIORITY_COLORS = new int[]{Color.rgb(185, 255, 255), Color.rgb(205, 255, 255), Color.rgb(147, 100, 255)};
}