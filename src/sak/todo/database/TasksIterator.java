package sak.todo.database;

import android.database.Cursor;

public interface TasksIterator {
	/**
	 * return the next task in the returned result of the query.
	 * or null if end has been reached
	 * */
	public Task nextTask();
	
	public Cursor getCursor();
}
