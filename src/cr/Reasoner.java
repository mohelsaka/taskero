package cr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;


import sak.todo.database.Task;
import taskero.learner.Preference_Learner;

public class Reasoner {

	private final int POOLSIZE = 50;
	public static long INFINITY = Long.MAX_VALUE;
	public static long FIFTEENMINUTES = 900000;
	public static long HOUER = 3600000;

	private long[][] stp;
	private ArrayList<Task> tasks;
	private ArrayList<Task> presetTasks;
	private ArrayListWithEvaluation currentAssignment;
	private PriorityQueue<ArrayListWithEvaluation> assignmentQueue;
	private ArrayList<Task> taskPool;
	private Preference_Learner learner;

	// begin added
	class ArrayListWithEvaluation extends ArrayList<Task> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public float value;

		public boolean equals(ArrayListWithEvaluation another) {
			for (int i = 0; i < another.size(); i++)
				if (get(i).duedate.getTime() != another.get(i).duedate
						.getTime())
					return false;
			return true;
		}
	}

	// end added
	public Reasoner(Preference_Learner learner) {
		// begin added
		this.learner = learner;
		presetTasks = new ArrayList<Task>();
		taskPool = new ArrayList<Task>();
		for (int i = 0; i < POOLSIZE; i++)
			taskPool.add(new Task());
		assignmentQueue = new PriorityQueue<ArrayListWithEvaluation>(10,
				new Comparator<ArrayListWithEvaluation>() {

					public int compare(ArrayListWithEvaluation o1,
							ArrayListWithEvaluation o2) {
						return o1.value > o2.value ? 1
								: o1.value == o2.value ? 0 : -1;
					}
				});
		// end added
	}


	public void setPresetTasks(ArrayList<Task> preset) {
		this.presetTasks = preset;
	}

	public ArrayList<ArrayList<Task>> schedule(ArrayList<Task> tasks)
			throws STPNotConnsistentException {
		this.tasks = tasks;
		stp = new long[tasks.size()][tasks.size()];
		int n = stp.length;
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				if (i == j)
					stp[i][j] = 0;
				else if (stp[i][j] == 0)
					stp[i][j] = INFINITY;
		for (int i = 0; i < tasks.size(); i++) {
			Task task = tasks.get(i);
			for (int k = 0; k < task.before.size(); k++) {
				int j = find(tasks, task.before.get(k));
				if (j < 0)
					continue;
				stp[j][i] = task.beforeIntervals.get(k).max;
				stp[i][j] = -task.beforeIntervals.get(k).min;
			}
			for (int k = 0; k < task.after.size(); k++) {
				int j = find(tasks, task.after.get(k));
				if (j < 0)
					continue;
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
			return assignTimesNew();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	long[][] solveSTP() {
		int n = stp.length;

		for (int l = 0; l < stp.length; l++) {
			for (int m = 0; m < stp.length; m++)
				System.out.print(stp[l][m] + "   ");
			System.out.println();
		}
		for (int k = 0; k < n; k++) {
			System.out.println("using " + k);
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++) {
					if (!(stp[i][k] == INFINITY || stp[k][j] == INFINITY))
						stp[i][j] = Math.min(stp[i][k] + stp[k][j]
								+ (long) tasks.get(k).estimate * HOUER,
								stp[i][j]);
					// }
				}
			for (int l = 0; l < stp.length; l++) {
				for (int m = 0; m < stp.length; m++)
					System.out.print(stp[l][m] + "   ");
				System.out.println();
			}
		}
		for (int i = 0; i < n; i++)
			if (stp[i][i] < 0)
				return null;
		return stp;
	}

	boolean[] visited;

	boolean isAvailable(long date, long duration) {
		if (presetTasks != null)
			for (Task task : presetTasks) {
				if ((date >= task.duedate.getTime() && date <= task.duedate
						.getTime() + task.estimate * HOUER)
						|| (task.duedate.getTime() >= date && task.duedate
								.getTime() <= date + duration))
					return false;
			}
		if (currentAssignment != null)
			for (Task task : currentAssignment) {
				if ((date >= task.duedate.getTime() && date <= task.duedate
						.getTime() + task.estimate * HOUER)
						|| (task.duedate.getTime() >= date && task.duedate
								.getTime() <= date + duration))
					return false;
			}
		return true;
	}

	private void appendPreset() {
		if (presetTasks != null)
			for (Task task : presetTasks) {
				currentAssignment.add(task);
			}
	}

	// begin added

	void addAssignment() {
		 try {
			currentAssignment.value = learner.evaluate(currentAssignment);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("[");
		for (int i = 0; i < tasks.size() - 1; i++)
			System.out.print(additions[i] + " ");
		System.out.println("]");
		for (ArrayListWithEvaluation a : assignmentQueue) {
			if (currentAssignment.equals(a))
				return;
		}
		if (assignmentQueue.size() >= 10) {
			if (currentAssignment.value > assignmentQueue.peek().value) {
				assignmentQueue.remove();
				assignmentQueue.add(currentAssignment);
			}
		} else
			assignmentQueue.add(currentAssignment);
	}

	int[] additions;

	int[] nextAdditions() {
		if (additions == null)
			return new int[tasks.size() - 1];
		for (int i = 0; i < additions.length; i++) {
			if (++additions[i] > 8) {
				additions[i] = 0;
			} else
				return additions;
		}
		return null;
	}

	// ArrayList<ArrayList<Task>> assignTimesNew()
	// throws CloneNotSupportedException, STPNotConnsistentException {
	// visited = new boolean[tasks.size()];
	// while ((additions = nextAdditions()) != null) {
	// for (int k = 1; k < tasks.size(); k++)
	// visited[k] = false;
	// currentAssignment = new ArrayListWithEvaluation();
	// appendPreset();
	// int i = 0, j;
	// for (i = 1; i < tasks.size(); i++) {
	// j = findMinDeadLine();
	// visited[j] = true;
	// if(taskPool.isEmpty())
	// for(int z = 0; z < POOLSIZE; z++)
	// taskPool.add(new Task());
	// Task t = tasks.get(j).clone();
	// // t.deadline = new Date(stp[0][j]);
	// long date1 = (-stp[j][0] + additions[j - 1]
	// * (stp[0][j] + stp[j][0]) / 8)
	// / FIFTEENMINUTES * FIFTEENMINUTES;
	// long date2 = date1 + FIFTEENMINUTES;
	// if (isAvailable(date2, (long) t.estimate * HOUER)) {
	// if (date2 + t.estimate * HOUER <= t.deadline.getTime())
	// if (date2 > System.currentTimeMillis()) {
	// t.setDueDate(date2);
	// currentAssignment.add(t);
	// }
	// } else if (isAvailable(date1, (long) t.estimate * HOUER))
	// if (date1 + t.estimate * HOUER <= t.deadline.getTime())
	// if (date1 > System.currentTimeMillis()) {
	// t.setDueDate(date1);
	// currentAssignment.add(t);
	// }
	// }
	// if (currentAssignment.size() == presetTasks.size() + tasks.size()
	// - 1)
	// addAssignment();
	// else
	// for (Task task : currentAssignment)
	// taskPool.add(task);
	// // else invalid assignment
	// }
	// ArrayList<ArrayList<Task>> assignments = new
	// ArrayList<ArrayList<Task>>();
	// for (ArrayList<Task> arrayList : assignmentQueue) {
	// assignments.add(arrayList);
	// }
	// return assignments;
	// }
	ArrayList<ArrayList<Task>> assignTimesNew()
			throws CloneNotSupportedException, STPNotConnsistentException {
		int[] order = sortTasks();
		while ((additions = nextAdditions()) != null) {
			currentAssignment = new ArrayListWithEvaluation();
			appendPreset();
			long now = -stp[order[0]][0];
			int i = 0, prev = 0, j;
			for (i = 0; i < order.length; i++) {
				j = order[i];
				if (taskPool.isEmpty())
					for (int z = 0; z < POOLSIZE; z++)
						taskPool.add(new Task());
				Task t = tasks.get(j).clone(taskPool.remove(0));
				// t.deadline = new Date(stp[0][j]);
				long date1 = (now + additions[j - 1]
						* (stp[prev][j] - Math.abs(stp[j][prev])) / 8)
						/ FIFTEENMINUTES * FIFTEENMINUTES;
				long date2 = date1 + FIFTEENMINUTES;
				if (isAvailable(date2, (long) t.estimate * HOUER)
						&& date2 + t.estimate * HOUER <= t.deadline.getTime()
						&& date2 > System.currentTimeMillis()) {
					t.setDueDate(date2);
					now = date2+(long)t.estimate*HOUER;
					currentAssignment.add(t);
					prev = j;
				} else if (isAvailable(date1, (long) t.estimate * HOUER)
						&& date1 + t.estimate * HOUER <= t.deadline.getTime()
						&& date1 > System.currentTimeMillis()) {
					t.setDueDate(date1);
					now = date1+(long)t.estimate*HOUER;
					currentAssignment.add(t);
					prev = j;
				} else
					break;
			}
			if (currentAssignment.size() == presetTasks.size() + tasks.size()
					- 1)
				addAssignment();
			else
				for (int k = presetTasks.size(); k < currentAssignment.size(); k++)
					taskPool.add(currentAssignment.get(k));
			// else invalid assignment
		}
		ArrayList<ArrayList<Task>> assignments = new ArrayList<ArrayList<Task>>();
		for (ArrayList<Task> arrayList : assignmentQueue) {
			assignments.add(arrayList);
		}
		return assignments;
	}

	int[] sortTasks() {
		int[] degrees = new int[tasks.size() - 1];
		int[] order = new int[tasks.size() - 1];
		int i, j;
		for (i = 0; i < degrees.length; i++) {
			order[i] = i + 1;
			for (j = 1; j < stp.length; j++) {
				if (stp[i + 1][j] > 0 && i + 1 != j && stp[i+1][j] > stp[j][i+1])
					degrees[i]++;
			}
		}
		for (i = 0; i < order.length; i++) {
			for (j = i + 1; j < order.length; j++) {
				if (degrees[j] > degrees[i]) {
					int temp = order[i];
					int temp2 = degrees[i];
					order[i] = order[j];
					degrees[i] = degrees[j];
					order[j] = temp;
					degrees[j] = temp2;
				}
			}
		}
		return order;
	}

	// end added

	int find(ArrayList<Task> tasks, Task task) {
		for (int i = 0; i < tasks.size(); i++)
			if (task == tasks.get(i))
				return i;
		return -1;
	}

	// handle json objects


	
//	public static void main(String[] args) throws IOException,
//			STPNotConnsistentException {
//		// BufferedReader reader = new BufferedReader(new
//		// FileReader("tests.txt"));
//		// String line = reader.readLine();
//		// ArrayList<Task> tasks = new ArrayList<Task>();
//		// PrintWriter writer = new PrintWriter(new File("result.txt"));
//		// int z = 0;
//		// while (line != null) {
//		// tasks.clear();
//		// Task.NULLTASK = new Task(null, 0);
//		// tasks.add(Task.NULLTASK);
//		// String[] taskInfo = line.split(",");
//		// for (String string : taskInfo) {
//		// System.out.println(string);
//		// String[] splits = string.split(":");
//		// String[] dateInfo = splits[2].split("/");
//		// tasks.add(new Task("task " + splits[0], Float
//		// .parseFloat(splits[1]), new Date(Integer
//		// .parseInt(dateInfo[2]) - 1900, Integer
//		// .parseInt(dateInfo[1]), Integer.parseInt(dateInfo[0]),
//		// Integer.parseInt(dateInfo[3]), Integer
//		// .parseInt(dateInfo[4]), Integer
//		// .parseInt(dateInfo[5]))));
//		// }
//		// line = reader.readLine();
//		// while (!line.equals("****")) {
//		// System.out.println(line);
//		// String[] constraints = line.split("-");
//		// tasks.get(Integer.parseInt(constraints[0])).addAfter(
//		// tasks.get(Integer.parseInt(constraints[1])),
//		// new Interval(60 * 60000*(Long.parseLong(constraints[2])),
//		// constraints[3].equals("inf") ? INFINITY : 60 * 60000*(Long
//		// .parseLong(constraints[3]))));
//		// line = reader.readLine();
//		// }
//		// line = reader.readLine();
//		// try {
//		// Reasoner r = Reasoner.instance();
//		// ArrayList<ArrayList<Task>> assignments = r.schedule(tasks);
//		// writer.write("test case "+z++);
//		// for (ArrayList<Task> arrayList : assignments) {
//		// writer.write("other assignment\n");
//		// System.out.println("other assignment");
//		// for (Task task : arrayList) {
//		// System.out.println(task);
//		// writer.write(task.toString()+"\n");
//		// }
//		// }
//		// } catch (STPNotConnsistentException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// }
//		// }
//		//
//		// writer.close();
//		Task t1 = new Task(new Date(113, 5, 27, 20, 30, 0), 1, "t1", 1);
//		Task t2 = new Task(new Date(113, 5, 26, 23, 30, 0), 1, "t1", 2);
//		ArrayList<Task> preset = new ArrayList<Task>();
//		preset.add(t1);
//		preset.add(t2);
//		ArrayList<Task> tasks = new ArrayList<Task>();
//		Task t4 = new Task("t4", 1, new Date(113, 5, 27, 18, 40, 0));
//		Task t5 = new Task("t5", 1, new Date(113, 5, 27, 20, 40, 0));
//		Task t6 = new Task("t6", 1, new Date(113, 5, 27, 20, 40, 0));
//		tasks.add(Task.NULLTASK);
//		tasks.add(t4);
//		tasks.add(t5);
//		tasks.add(t6);
//		t4.addAfter(t5, new Interval(0, INFINITY));
//		t5.addAfter(t6, new Interval(0, INFINITY));
//		Reasoner.instance().setPresetTasks(preset);
//		ArrayList<ArrayList<Task>> ass = Reasoner.instance().schedule(tasks);
//		int i = 0;
//		for (ArrayList<Task> arrayList : ass) {
//			System.out.println("Assignment" + ++i);
//			for (Task task : arrayList) {
//				System.out.println(task);
//			}
//		}
//		//
//		// Task t1 = new Task(new Date(113, 5, 24, 12, 30, 0), 1, "t1", 1);
//		// Task t2 = new Task(new Date(113, 5, 24, 13, 30, 0), 1, "t1", 2);
//		// JSONArray jArray = new JSONArray();
//		// JSONObject obj = new JSONObject();
//		// obj.put("duration", t1.estimate);
//		// obj.put("duedate", t1.duedate.getTime());
//		// jArray.add(obj);
//		// JSONObject obj2 = new JSONObject();
//		// obj2.put("duration", t2.estimate);
//		// obj2.put("duedate", t2.duedate.getTime());
//		// jArray.add(obj2);
//		// Reasoner.instance().addPreset(jArray);
//		// Task t5 = new Task("t5", 1, new Date(113, 5, 30, 17, 40, 0));
//		// JSONObject meeting = new JSONObject();
//		// meeting.put("body", t5.body);
//		// meeting.put("deadline", t5.deadline.getTime());
//		// meeting.put("duration", t5.estimate);
//		// JSONArray ret = Reasoner.instance().scheduleMeeting(meeting);
//		// for (Object object : ret) {
//		// System.out.println("new assignment");
//		// for (Object o : (JSONArray)object) {
//		// System.out.println(o);
//		// }
//		//
//		// }
//	}
}
