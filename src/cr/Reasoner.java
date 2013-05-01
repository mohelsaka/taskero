package cr;

import java.util.ArrayList;
import java.util.Date;

import sak.todo.database.Task;

public class Reasoner {

	long[][] stp;
	public static long INFINITY = Long.MAX_VALUE;
	ArrayList<Task> tasks;

	private Reasoner() {
	}

	private static Reasoner instance = null;

	public static Reasoner instance() {
		if (instance == null)
			instance = new Reasoner();
		return instance;
	}

	public ArrayList<ArrayList<Task>> schedule(ArrayList<Task> tasks)
			throws STPNotConnsistentException {
		this.tasks = tasks;
		stp = new long[tasks.size()][tasks.size()];
		for (int i = 0; i < tasks.size(); i++) {
			Task task = tasks.get(i);
			for (int k = 0; k < task.before.size(); k++) {
				int j = find(tasks, task.before.get(k));
				stp[j][i] = task.beforeIntervals.get(k).max;
				stp[i][j] = -task.beforeIntervals.get(k).min;
			}
			for (int k = 0; k < task.after.size(); k++) {
				int j = find(tasks, task.after.get(k));
				stp[i][j] = task.afterIntervals.get(k).max;
				stp[j][i] = -task.afterIntervals.get(k).min;
			}
		}
		for (int i = 0; i < stp.length; i++) {
			for (int j = 0; j < stp.length; j++)
				System.out.print(stp[i][j] + "   ");
			System.out.println();
		}
		System.out.println("Now solve stp");

		stp = solveSTP();
		if (stp == null) {
			throw new STPNotConnsistentException();
		}
		try {
			return assignTimes();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	long[][] solveSTP() {
		int n = stp.length;
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				if (i == j)
					stp[i][j] = 0;
		// else if (stp[i][j] == 0)
		// stp[i][j] = INFINITY;
		for (int k = 0; k < n; k++) {
			System.out.println("using " + k);
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++) {
					if (i != j && !(stp[i][k] == INFINITY || stp[k][j] == INFINITY))
						stp[i][j] = Math.min(stp[i][k] + stp[k][j]
								+ (long) tasks.get(k).estimate * 60 * 60000,
								stp[i][j]);
					// }
					for (int l = 0; l < stp.length; l++) {
						for (int m = 0; m < stp.length; m++)
							System.out.print(stp[l][m] + "   ");
						System.out.println();
					}
				}
		}
		for (int i = 0; i < n; i++)
			if (stp[i][i] < 0)
				return null;
		return stp;
	}

	boolean[] visited;

	ArrayList<ArrayList<Task>> assignTimes() throws CloneNotSupportedException {
		visited = new boolean[tasks.size()];
		ArrayList<ArrayList<Task>> assignments = new ArrayList<ArrayList<Task>>();
		int count = 8;
		int mul = 0;
		visited[0] = true;
		for (int l = 0; l < count; l++) {
			// Log.d("DTPPROBLEM", ""+mul);
			for (int k = 1; k < tasks.size(); k++)
				visited[k] = false;
			ArrayList<Task> assignment = new ArrayList<Task>();
			int current = 0;
			Task prev = null;
			long now = 0;
			for (int i = 1; i < stp.length; i++) {
				int j = findMinDeadLine(current, mul);
				Task t = tasks.get(j).clone();
				long add = mul * (stp[current][j] + stp[j][current]) / count;
				t.setDueDate(new Date(now + Math.abs(stp[j][current]) + add));
				assignment.add(t);
				visited[j] = true;
				current = j;
				prev = t;
				now = prev.end.getTime();
			}
			assignments.add(assignment);
			mul++;
		}
		return assignments;
	}

	int findMinDeadLine(int source, int mul) {
		Long min = INFINITY;
		int minIndex = -1;
		for (int i = 1; i < stp.length; i++) {
			Date l = tasks.get(i).deadline;
			System.out.println(l);
			if (!visited[i] && min >= l.getTime() && stp[source][i] >= 0) { // stp[source][i]
																	// to be
																	// sure we
																	// can
																	// go there
																	// from here
				minIndex = i;
				min = tasks.get(i).deadline.getTime();
			}
		}
		return minIndex;
	}

	int find(ArrayList<Task> tasks, Task task) {
		for (int i = 0; i < tasks.size(); i++)
			if (task == tasks.get(i))
				return i;
		return -1;
	}

	public static void main(String[] args) {
		System.out.println("time now is "
				+ new Date(System.currentTimeMillis()));
		ArrayList<Task> tasks = new ArrayList<Task>();
		tasks.add(Task.NULLTASK);
		tasks.add(new Task("task 1", 1, new Date(2013 - 1900, 3, 30, 13, 0)));
		tasks.add(new Task("task 2", 1, new Date(2013 - 1900, 3, 30, 15, 0)));
		tasks.add(new Task("task 3", 1, new Date(2013 - 1900, 3, 30, 17, 0)));
		tasks.add(new Task("task 4", 1, new Date(2013 - 1900, 3, 30, 19, 0)));
		tasks.add(new Task("task 5", 1, new Date(2013 - 1900, 3, 30, 21, 0)));
		tasks.get(1).addAfter(tasks.get(2),
				new Interval(15 * 60 * 1000, 15 * 60 * 1000));
		Task.NULLTASK = null;
		System.out.println(tasks.get(0));
		// new Reasoner(tasks);
		// tasks.get(3).addBefore(tasks.get(4), new Interval(1, 2));
		// tasks.get(4).addAfter(tasks.get(5), new Interval(2, 5));
		// tasks.get(5).addAfter(tasks.get(6), new Interval(6, 9));

		try {
			Reasoner r = Reasoner.instance();
			ArrayList<ArrayList<Task>> assignments = r.schedule(tasks);
			for (ArrayList<Task> arrayList : assignments) {
				System.out.println("other assignment");
				for (Task task : arrayList) {
					System.out.println(task);
				}
			}
		} catch (STPNotConnsistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
