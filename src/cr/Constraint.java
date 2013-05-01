package cr;

import sak.todo.database.Task;

public class Constraint {
	
	Task first, second;
	Interval interval;
	
	public Constraint(Task first, Task second, Interval interval) {
		this.first = first;
		this.second = second;
		this.interval = interval;
	}

}
