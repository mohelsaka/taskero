package GA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import sak.todo.database.Task;
import android.graphics.Point;
import android.util.Log;

public class HandleUnscheduledTasks {


	
	boolean successfully = false;
	double Cost;
	double []tasksCost;
	static Point[] constraints;
	ArrayList<Integer> timeSlotsTaken;
	public HandleUnscheduledTasks(Task[] tasks, TimeSlot[] timeSlots,
			int preferenceIndex,Point[] constraints) {
		Log.d("debug", "ereeeee");
		timeSlotsTaken=new ArrayList<Integer>();
		HandleUnscheduledTasks.constraints=constraints;
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
			Log.d("debug", "index: "+assignment[i]);
			timeSlotsTaken.add(assignment[i]);
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
				if (timeSlots[j].getDuration() < tasks[i].estimate*60
						|| timeSlots[j].getStart().after(tasks[i].deadline) 
						|| timeSlots[j].getDuration()==-1) {
					cost[i][j] = M;
				}

				else
					/*
					 * Equation cost= |Pi - Vj| + (Dj - Ti) * Vj where Pi:
					 * preference i, Vj: focus rate j, Dj: duration of time slot
					 * Ti: duration of task. tasks[i].impression should be
					 * replace in GA with preferences1.
					 */
					cost[i][j] = Math.abs(tasks[i].priority*3 - timeSlots[j].getFocusRate())
							+ ((timeSlots[j].getDuration() - tasks[i].estimate*60)/60f)
							* timeSlots[j].getFocusRate();
				
			}
		}
		/* updated part (Constraints between tasks)
		 * Constraint between Taskbefore => Taskafter.
		 * Cost'[Taskbefore] = Cost[Taskbefore] / Min( Cost[TaskAfter][j] ), for all j timeslots.
		 * To ensure task before happens before task after.
		 */
		for (int i = 0; i < constraints.length; i++) {
			int taskBefore=constraints[i].x;
			int taskAfter=constraints[i].y;
			double min=Double.MAX_VALUE;
			for (int j = 0; j < cost[taskAfter].length; j++) {
				min=Math.min(min, cost[taskAfter][j]);
				
			}
			
			for (int j = 0; j < cost[taskBefore].length; j++) {
				if(cost[taskBefore][j]>=M)continue;
				cost[taskBefore][j]=(double) cost[taskBefore][j]/min;
			}
			
			for (int j = 1; j < cost[taskAfter].length; j++) {
				if(cost[taskAfter][j-1]<M){
					cost[taskAfter][j]=cost[taskAfter][j-1]+1;
				}
				if(cost[taskAfter][j-1]<M){
					cost[taskBefore][j]=cost[taskBefore][j-1]+2;
				}
				
			}
			
		}
		// end updated. 
		return cost;
	}

}
