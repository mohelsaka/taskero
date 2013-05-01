package GA;

import java.util.Arrays;

import sak.todo.database.Task;

public class Individual implements Comparable<Individual>{
	
	
	double fitness; // cost of Hungarian.
	Task[] tasks; // Scheduled tasks.
	boolean feasible; // feasible solutions
	double[] tasksCost;
	public Individual(Task[] ts) {
		// TODO Auto-generated constructor stub
		tasks=new Task[ts.length];
		for (int i = 0; i < ts.length; i++) {
			tasks[i]=new Task();
			tasks[i].body=ts[i].body;
			tasks[i].duedate=ts[i].duedate;
			tasks[i].deadline=ts[i].deadline;
			tasks[i].id=ts[i].id;
			tasks[i].impression=ts[i].impression;
			tasks[i].priority=ts[i].priority;
			tasks[i].estimate=ts[i].estimate;
		}
		tasksCost=new double[tasks.length];
		
	}
	public int compareTo(Individual another) {
		// TODO Auto-generated method stub
		if(this.fitness>another.fitness) return 1;
		else if(this.fitness<another.fitness) return -1;
		return 0;
	}
	
	
}

