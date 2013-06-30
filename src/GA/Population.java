package GA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

import android.util.Log;

public class Population {

	ArrayList<Individual> individuals; // feasible solutions 
	int Generation; // Number of Generation.
	static int M=2000;
	static int MaxGenerations=10;
	static long timeElapes;
	static long maxTime=2000;
//	static double MinimumFitness;
	static double minimumThreshold;
	static Random r=new Random();
	public Population() {
		// TODO Auto-generated constructor stub
		individuals=new ArrayList<Individual>();
		Generation=0;
		
	}
	
	public Individual Crossover(Individual inv1,Individual inv2){
		Individual result=new Individual(inv1.tasks);
		minimumThreshold=2.5f*result.tasks.length; // training .. 
		double fit=0;
		// Here we swap only duedates of two individuals(parents) into child (new individual) 
		for (int i = 0; i < inv1.tasks.length; i++) {
			boolean swap=r.nextBoolean();
			if(swap){
				result.tasks[i].duedate=inv1.tasks[i].duedate;
				result.tasksCost[i]=inv1.tasksCost[i];
				fit+=inv1.tasksCost[i];
			}
			else{
				result.tasks[i].duedate=inv2.tasks[i].duedate;
				result.tasksCost[i]=inv2.tasksCost[i];
				fit+=inv2.tasksCost[i];
			}
//			Log.d("debug", "crossover: "+result.tasks[i].duedate);
		}
		result.feasible=true;
		Arrays.sort(result.tasks);
		for (int i = 0; i < result.tasks.length; i++) {
//			Log.d("debug", "sortcross: "+result.tasks[i].duedate);
		}
		for (int i = 0; i < result.tasks.length-1; i++) {
			Calendar c=Calendar.getInstance();
			c.setTime(result.tasks[i].duedate);
			c.add(Calendar.HOUR_OF_DAY, (int) result.tasks[i].estimate);
			if(c.getTime().after(result.tasks[i+1].duedate))result.feasible=false;
		}
		
		result.fitness=fit;
//		MinimumFitness=Math.min(result.fitness, MinimumFitness);
		return result;
	}
	
	public Individual Mutation(Individual inv){
		Individual result=new Individual(inv.tasks);
		for (int i = 0; i < inv.tasks.length; i++) {
			result.tasks[i].duedate=inv.tasks[i].duedate;
		}
		int index=r.nextInt(inv.tasks.length);
		
		TimeSlot[]ts=ScheduleTasks.getFreeTimeSlots(result.tasks[index].estimate, result.tasks[index].deadline);
		
		
		// updated 
				
		int winner=r.nextInt(ts.length);
		result.tasks[index].duedate = ts[winner].getStart();
		
		if (ts[winner].getDuration() < result.tasks[index].estimate*60
				|| ts[winner].getStart().after(result.tasks[index].deadline) 
				|| ts[winner].getDuration()==-1) {
			result.tasksCost[index] = M;
		}

		
		result.fitness=inv.fitness-inv.tasksCost[index]+result.tasksCost[index];
		if(result.fitness<M)result.feasible=true;
//		MinimumFitness=Math.min(MinimumFitness, result.fitness);
//		Log.d("debug", "finish mutation "+ result.feasible);
		
		// finish updated
		
//		double[][]cost=HandleUnscheduledTasks.FillHungarianTable(inv.tasks, ts, r.nextInt(2));
//		HungarianMethod hg=new HungarianMethod(cost);
//		int[] assignment = hg.getAssignment();		
//		result.tasks[index].duedate = ts[assignment[index]].getStart();
//		result.tasksCost[index]=cost[index][assignment[index]];
//		result.fitness=inv.fitness-inv.tasksCost[index]+result.tasksCost[index];
//		if(result.fitness<M)result.feasible=true;
////		MinimumFitness=Math.min(MinimumFitness, result.fitness);
		
		return result;
	}
	public boolean StoppingCriteriaReached(){
		/*
		 * Number of Generations
		 * Time elapsed
		 * Minimum threshold achieved.
		 */
		// just now
		if(Generation==MaxGenerations)return true;
		if(timeElapes>=maxTime)return true;
//		if(MinimumFitness<=minimumThreshold) return true;
		return false;
	}
}
