package GA;

import java.util.Arrays;

import sak.todo.database.Task;
import android.util.Log;

public class HandleUnscheduledTasks {


	
	boolean successfully = false;
	double Cost;
	double []tasksCost;
	public HandleUnscheduledTasks(Task[] tasks, TimeSlot[] timeSlots,
			int preferenceIndex) {
		tasksCost=new double[tasks.length];
		double[][] array = FillHungarianTable(tasks, timeSlots, preferenceIndex);

		for (int i = 0; i < array.length; i++) {
			Log.d("debug", Arrays.toString(array[i]));
		}
		HungarianMethod hg = new HungarianMethod(array);
		int[] assignment = hg.getAssignment();
		double cost = hg.getCost();
		Cost = cost;
		Log.d("debug", "Cost " + cost);

		successfully = true;
		for (int i = 0; i < assignment.length; i++) {
			tasks[i].duedate = timeSlots[assignment[i]].getStart();
			tasksCost[i]=array[i][assignment[i]];
			if (array[i][assignment[i]] >= M) { // invalid option.
				successfully = false;
			}

		}

	}
	

	// large value. MAX.s
	static final double M = 2000;

	public static double[][] FillHungarianTable(Task[] tasks,
			TimeSlot[] timeSlots, int index) {
		// TODO Auto-generated method stub

		double[][] cost = new double[tasks.length][timeSlots.length];
		for (int i = 0; i < cost.length; i++) {
			for (int j = 0; j < cost[i].length; j++) {

				/*
				 * check invalid options (duration of task does not fit on time
				 * slot Or, time slot starts after deadline of task.
				 */
				if (timeSlots[j].getDuration() < tasks[i].estimate
						|| timeSlots[j].getStart().after(tasks[i].deadline)) {
					cost[i][j] = M;
				}

				else
					/*
					 * Equation cost= |Pi - Vj| + (Dj - Ti) * Vj where Pi:
					 * preference i, Vj: focus rate j, Dj: duration of time slot
					 * Ti: duration of task. tasks[i].impression should be
					 * replace in GA with preferences1.
					 */
					cost[i][j] = Math.abs(PreferenceModel.PM.get(tasks[i].id)
							.get(index) - timeSlots[j].getFocusRate())
							+ ((timeSlots[j].getDuration() - tasks[i].estimate)/60f)
							* timeSlots[j].getFocusRate();
				
			}
		}
		return cost;
	}

}
