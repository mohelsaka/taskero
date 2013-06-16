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
		int n = stp.length;
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				if (i == j)
					stp[i][j] = 0;
//				else if (stp[i][j] == 0)
//					stp[i][j] = INFINITY;
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
								+ (long) tasks.get(k).estimate * 60 * 60000,
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

	long fifteenMinutes = 900000;
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
//				System.out.println(j);
				Task t = tasks.get(j).clone();
				long add = mul * (stp[current][j] - Math.abs(stp[j][current])) / count;
				System.out.println(add);
				System.out.println(Math.abs(stp[j][current]));
				System.out.println(new Date(now));
				long date1 = (now + Math.abs(stp[j][current]) + add)/fifteenMinutes*fifteenMinutes;
				long date2 = (now + Math.abs(stp[j][current]) + add)/fifteenMinutes*fifteenMinutes+fifteenMinutes;
				t.setDueDate(new Date(date2+t.estimate*60*60*1000>t.deadline.getTime()?date1:date2));
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
		long min = INFINITY;
		int minIndex = -1;
		for (int i = 1; i < stp.length; i++) {
			Date l = tasks.get(i).deadline;
			// System.out.println(l);
			if (!visited[i] && min >= l.getTime() && stp[source][i] >= 0) { // stp[source][i]
				// to be
				// sure we
				// can
				// go there
				// from here
				boolean b = true;
				for (int j = 0; j < stp.length; j++) {
					if (stp[j][i] > stp[i][j] && !visited[j]){
						b = false;
						break;
					}
				}
				if (b) {
					minIndex = i;
					min = tasks.get(i).deadline.getTime();
				}
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

	/*
	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader("tests.txt"));
		String line = reader.readLine();
		ArrayList<Task> tasks = new ArrayList<Task>();
		PrintWriter writer = new PrintWriter(new File("result.txt"));
		int z = 0;
		while (line != null) {
			tasks.clear();
			 Task.NULLTASK = new Task(null, 0);
			 tasks.add(Task.NULLTASK);
			String[] taskInfo = line.split(",");
			for (String string : taskInfo) {
				System.out.println(string);
				String[] splits = string.split(":");
				String[] dateInfo = splits[2].split("/");
				tasks.add(new Task("task " + splits[0], Float
						.parseFloat(splits[1]), new Date(Integer
						.parseInt(dateInfo[2]) - 1900, Integer
						.parseInt(dateInfo[1]), Integer.parseInt(dateInfo[0]),
						Integer.parseInt(dateInfo[3]), Integer
								.parseInt(dateInfo[4]), Integer
								.parseInt(dateInfo[5]))));
			}
			line = reader.readLine();
			while (!line.equals("****")) {
				System.out.println(line);
				String[] constraints = line.split("-");
				tasks.get(Integer.parseInt(constraints[0])).addAfter(
						tasks.get(Integer.parseInt(constraints[1])),
						new Interval(60 * 60000*(Long.parseLong(constraints[2])),
								constraints[3].equals("inf") ? INFINITY : 60 * 60000*(Long
										.parseLong(constraints[3]))));
				line = reader.readLine();
			}
			line = reader.readLine();
			try {
				Reasoner r = Reasoner.instance();
				ArrayList<ArrayList<Task>> assignments = r.schedule(tasks);
				writer.write("test case "+z++);
				for (ArrayList<Task> arrayList : assignments) {
					writer.write("other assignment\n");
					System.out.println("other assignment");
					for (Task task : arrayList) {
						System.out.println(task);
						writer.write(task.toString()+"\n");
					}
				}
			} catch (STPNotConnsistentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		writer.close();
	}
	*/
}
