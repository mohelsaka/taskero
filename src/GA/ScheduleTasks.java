package GA;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import sak.todo.database.DBHelper;
import sak.todo.database.Task;
import sak.todo.database.TasksIterator;
import android.util.Log;

public class ScheduleTasks {
	
	/*
	 * input from user.. Focus rate of 24-hours of the day ..
	 */
	static int[] FocusRate=new int[]{8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,2,2,2,2,2};
	static Random random=new Random();
	/**
	 * Constructor for scheduling tasks.
	 */
	public ScheduleTasks(Task[] tasks) {
		// TODO Auto-generated constructor stub
		
		Population pop=new Population();
		pop.Generation=1; 
		
//		Task[] tasks = Task.getBackLogTasks();
		if(tasks.length==0)return;


		float maxDuration = -1;
		Date maxdeadline=getTimeNow();
		for (int j = 0; j < tasks.length; j++) {
			maxDuration = Math.max(tasks[j].estimate, maxDuration);
			
			if(maxdeadline.before(tasks[j].deadline)){
				maxdeadline=tasks[j].deadline;
			}
		}
		
		TimeSlot[] timeSlots = getFreeTimeSlots(maxDuration,maxdeadline);
		
		for (int i = 0; i < PreferenceModel.PreferencesNum(); i++) {
			Individual individual=new Individual(tasks);
			
			
			HandleUnscheduledTasks h=new HandleUnscheduledTasks(tasks,timeSlots,i);
			
			
			for (int j = 0; j < tasks.length; j++) {
				individual.tasks[j].duedate=tasks[j].duedate;
				individual.tasksCost[j]=h.tasksCost[j];
			}
			individual.fitness=h.Cost;
			individual.feasible=h.successfully;
			if(!individual.feasible)return;// infeasible root ==> Game over!	
			pop.individuals.add(individual);
		}
		Log.d("debug", pop.individuals.size()+"");
		
		// GA implementation
		do{
			
			Collections.sort(pop.individuals);
			int len=pop.individuals.size();
			
			// Select pair to mate from best ranked individuals for Crossover
			Individual inv1=pop.individuals.get(len-1);
			Individual inv2=pop.individuals.get(len-2);
			Individual indCross=pop.Crossover(inv1, inv2);
			
			
			// Mutation on random individual.
			int index=random.nextInt(len);
			Individual indMut=pop.Mutation(pop.individuals.get(index));
			
			
			
			
			pop.individuals.add(indCross);
			pop.individuals.add(indMut);
			pop.Generation++;
			
			
		}while(!pop.StoppingCriteriaReached());
		
		ArrayList<Individual> individuals= pop.individuals;
		double MinCost=Integer.MAX_VALUE;
		int index=-1;
		for (int i = 0; i < individuals.size(); i++) {
			if(MinCost>individuals.get(i).fitness){
				MinCost=individuals.get(i).fitness;
				index=i;
			}
		}
		Log.d("debug", "size: "+individuals.size());
		Log.d("debug", "min finess: "+MinCost);
		for (int i = 0; i < tasks.length; i++) {
			Task.updateSinglField(tasks[i].id, DBHelper.COLUMN_DUE_DATE_NUM, individuals.get(index).tasks[i].duedate.getTime()+"");
			Log.d("debug", "best: "+tasks[i].id+": "+individuals.get(index).tasks[i].duedate);
		}
		
		
		
		
	}
	public static TimeSlot[] getFreeTimeSlots(float maxDuration, Date maxDeadline) {
		// TODO Auto-generated method stub
		ArrayList<TimeSlot> list=new ArrayList<TimeSlot>();
		Calendar c=Calendar.getInstance();
		
		TasksIterator ti =Task.getScheduledTasks(getTimeNow(), maxDeadline);
		int count = ti.getCursor().getCount();
		Task[] tasks = new Task[count];
		Task t = ti.nextTask();
		int k = 0;
		while (t != null) {
			tasks[k++] = t;
			t = ti.nextTask();
		}
		
		if(count==0){
			list.add(addTimeSlot(getTimeNow(),maxDeadline));
			
		}
		else{
			list.add(addTimeSlot(getTimeNow(), tasks[0].duedate));
			
			
			for (int i = 0; i < tasks.length-1; i++) {
				c.setTime(tasks[i].duedate);
				c.add(Calendar.MINUTE, (int) tasks[i].estimate);
				list.add(addTimeSlot(c.getTime(), tasks[i+1].duedate));
			}
			c.setTime(tasks[tasks.length-1].duedate);
			c.add(Calendar.MINUTE, (int) tasks[tasks.length-1].estimate);
			list.add(addTimeSlot(c.getTime(), maxDeadline));	
		}
		
		
		/*
		 * Adjusting Time slots to fit tasks 
		 */
		for (int i = 0; i < list.size(); i++) {
			TimeSlot ts=list.get(i);
			
			if(ts.getDuration()>maxDuration){
				ts.setDuration((long) maxDuration);
				c.setTime(ts.getStart());
				c.add(Calendar.MINUTE,(int) maxDuration);
				
				list.add(addTimeSlot(c.getTime(), ts.getEnd()));
				
				ts.setEnd(c.getTime());
				
			}
		}
		
		TimeSlot[] result=new TimeSlot[list.size()];
		k=0;
		while(list.size()>0){
			result[k++]=list.remove(0);
			result[k-1].setFocusRate(updateFocusRate(result[k-1]));
		}
		
//		for (int i = 0; i < result.length; i++) {
//			Log.d("debug", result[i].getStart().toString()+": "+result[i].getDuration());
//		}
		
		return result;
	}

	private static Date getTimeNow(){
		Calendar now=Calendar.getInstance();
		now.set(Calendar.SECOND, 0);
		int min=((now.get(Calendar.MINUTE)/15)+1)*15;
		now.set(Calendar.MINUTE, min);
		return now.getTime();
	}
	

	private static float updateFocusRate(TimeSlot ts) {
		// TODO Auto-generated method stub
		Calendar c=Calendar.getInstance();
		c.setTime(ts.getStart());
		float avg=0f;
		int k=0;
		while(c.getTime().before(ts.getEnd())){
			avg+=FocusRate[c.get(Calendar.HOUR_OF_DAY)];
			c.add(Calendar.HOUR_OF_DAY,1);
			k++;
		}
		
		return avg/k;
	}

	
	private static TimeSlot addTimeSlot(Date d1, Date d2) {
		// TODO Auto-generated method stub
		long duration=TimeUnit.MILLISECONDS.toMinutes(d2.getTime()-d1.getTime());
		
		return new TimeSlot(d1, d2, duration, 0);
	}

	

}
