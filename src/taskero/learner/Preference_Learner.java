package taskero.learner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import com.learner.svm.SVMAdapter;

import sak.todo.database.Task;
import android.util.Log;
import android.widget.Adapter;

public class Preference_Learner {

	/**
	 * @param args
	 */
	private ArrayList<ArrayList<Task>> calendar;

	private HashMap<ArrayList<Task>, CalenderFeatureVector> featureMapping;

	private static final int NUM_OF_TIME_BLOCKS = 5;
	private static final int DAYS = 7;
	private static final int SHORT_DURATION = 1;
	private static final int MEDIUM_DURATION = 2;
	private static final int DURATION = 3;

	private static String[] BLOCKS = { "latepm", "latepm", "latepm", "latepm",
			"latepm", "latepm", "earlyam", "earlyam", "earlyam", "earlyam",
			"earlyam", "lateam", "lateam", "earlypm", "earlypm", "earlypm",
			"earlypm", "lunch", "lunch", "latepm", "latepm", "latepm",
			"latepm", "latepm", "latepm" };

	private static HashMap<String, Integer> ff;
	private Float[] predictions;
	private SVMAdapter svmAdapter;

	private static final int NUM_OF_FEATURES = 77;
	private int numOfRuns;

	static {
		ff = new HashMap<String, Integer>();
		ff.put("earlyam", 0);
		ff.put("lateam", 1);
		ff.put("earlypm", 2);
		ff.put("lunch", 3);
		ff.put("latepm", 4);
	}

	public Preference_Learner(ArrayList<ArrayList<Task>> calendar,
			SVMAdapter svmAdapter, int numOfRuns) {

		this.calendar = calendar;
		this.numOfRuns = numOfRuns;

		this.svmAdapter = svmAdapter;
		featureMapping = new HashMap<ArrayList<Task>, CalenderFeatureVector>();

		// setcalenderFeatVector(calendar);

	}

	public Preference_Learner(SVMAdapter adapter) {
		this.svmAdapter = adapter;
	}

	private static Preference_Learner learner = null;

	public static Preference_Learner getInstance(
			ArrayList<ArrayList<Task>> calendar, SVMAdapter svmAdapter,
			int numOfRuns) {
		// if(learner != null)
		// return learner;
		learner = new Preference_Learner(calendar, svmAdapter, numOfRuns);

		return learner;
	}

	/*
	 * 0 = Sunday, 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 =
	 * Friday, 6 = Saturday
	 */
	/*
	 * earlyam->[5:00-9:59] lateam->[10:00-11:59] lunch->user
	 * earlypm->[12:00-15:59] latepm->[19:00-4:59]
	 */
	/*
	 * short<60 , medium 60-119 long>120
	 */
	/*
	 * none-> 0 few-> [1,5] some-> [6,10] many-> [11,inf]
	 */

	public CalenderFeatureVector[] setcalenderFeatVector(
			ArrayList<ArrayList<Task>> calendar) {

		CalenderFeatureVector[] calenderFeatVector = new CalenderFeatureVector[calendar
				.size()];

		for (int i = 0; i < calendar.size(); i++) {
			CalenderFeatureVector kk = new CalenderFeatureVector();
			kk.setFeatureVector(buildFeatureVector(calendar.get(i)));
			featureMapping.put(calendar.get(i), kk);
			calenderFeatVector[i] = kk;
		}
		return calenderFeatVector;
	}

	public ArrayList<ArrayList<Task>> rank(
			CalenderFeatureVector[] calenderFeatVector) throws IOException {

		// add the qid in the saving testing tuples DON'T FORGET
		for (int i = 0; i < calenderFeatVector.length; i++) {
			String temp = "";
			for (int j = 0; j < calenderFeatVector[i].featureVector.length; j++) {
				temp += (j + 1) + ":" + calenderFeatVector[i].featureVector[j]
						+ " ";
			}
			temp += "\n";
			Log.d("debug", temp);
		}

		svmAdapter.saveTestingTuples(calenderFeatVector, numOfRuns);
		svmAdapter.runClassify();

		predictions = svmAdapter.readPredictions();
		Log.d("Taskero", predictions.toString());

		Collections.sort(calendar, new Comparator<ArrayList<Task>>() {
			public int compare(ArrayList<Task> lhs, ArrayList<Task> rhs) {
				// TODO Auto-generated method stub
				if (predictions[calendar.indexOf(lhs)] > predictions[calendar
						.indexOf(rhs)])
					return 1;
				else if (predictions[calendar.indexOf(lhs)] < predictions[calendar
						.indexOf(rhs)])
					return -1;
				else
					return 0;
			}
		});

		return calendar;
	}

	public void learn(ArrayList<ArrayList<Task>> calendar) throws IOException {

		CalenderFeatureVector[] cal = setcalenderFeatVector(calendar);
		svmAdapter.appendToTrainingTupples(calendar, cal, numOfRuns);

		svmAdapter.runLearning();
	}

	public float evaluate(Date start, Date end) throws IOException {
		float weight = 0;
		HashMap<Integer, Float> weights = svmAdapter.getSVMWeights(); // =
		int duration = 24*(end.getDay() - start.getDay()) + end.getHours() - start.getHours();			
		// svmadapter.getweights
		int[] temp = new int[35];
		for (long i = start.getTime(); i < end.getTime(); i += 60 * 60000) {
			Date d = new Date(i);
			temp[d.getDay() * NUM_OF_TIME_BLOCKS
					+ ff.get(BLOCKS[d.getHours()]).intValue()]++;
		}
		for (int i = 0; i < temp.length; i++) {
			if (weights.get(i + 1) != null)
				weight += temp[i] * weights.get(i + 1)/duration;
		}
		return weight;
	}

	public float evaluate(ArrayList<Task> assignment) throws IOException {
		float value = 0;
		int[] vector = buildFeatureVector(assignment);
		HashMap<Integer, Float> weights = svmAdapter.getSVMWeights(); // =
																		// svmadapter.getweights
		for (int i = 0; i < vector.length; i++) {
			if (weights.get(i) != null)
				value += vector[i] * weights.get(i);
		}
		return value;
	}

	private static int[] buildFeatureVector(ArrayList<Task> assignment) {
		int startHours = 0;
		int endHours = 0;
		int day = 0;
		int duration = 0;
		int index = 0;
		int endMinutes = 0;
		int nextStartMinutes = 0;
		int differentDay = 0;
		int nextStartHours;

		int[] feature = new int[NUM_OF_FEATURES];
		for (int j = 0; j < assignment.size(); j++) {

			// to check the alternating between different blocks
			Task task = assignment.get(j);
			startHours = task.getStartDate().getHours();
			endHours = task.getEndDate().getHours();

			endMinutes = task.getEndDate().getMinutes();
			duration = (int) task.estimate;

			System.out.println(startHours);
			day = task.getStartDate().getDay();

			if (startHours == 0)
				startHours = 24;
			if (endHours == 0)
				endHours = 24;

			index = NUM_OF_TIME_BLOCKS * day + ff.get(BLOCKS[startHours - 1]);
			// calenderFeatVector[i][index]++;
			feature[index]++;

			// setting the free blocks feature eg: short free block on
			// monday
			// no free blocks at the last task
			if (j < assignment.size() - 1) {

				nextStartHours = assignment.get(j + 1).getStartDate()
						.getHours();

				if (assignment.get(j + 1).getStartDate().getHours() == 0)
					nextStartHours = 24;

				nextStartMinutes = assignment.get(j + 1).getStartDate()
						.getMinutes();

				// add the case that the endhours=0 and start hours =any time
				// oin the same day
				if (assignment.get(j + 1).getStartDate().getDay() == day) {
					if (endHours == 24)
						endHours = 0;

					differentDay = 0;
				} else {
					differentDay = 24;
				}

				if (Math.abs(nextStartHours + differentDay
						* (assignment.get(j + 1).getStartDate().getDay() - day)
						- endHours) < SHORT_DURATION
						|| Math.abs(nextStartHours
								+ differentDay
								* (assignment.get(j + 1).getStartDate()
										.getDay() - day) - endHours) == 1
						&& nextStartMinutes - endMinutes < 0) {

					// free block duration
					index = DAYS * NUM_OF_TIME_BLOCKS + day * DURATION + 0;
					// calenderFeatVector[i][index]++;
					feature[index]++;

				}

				else if (SHORT_DURATION <= Math.abs(nextStartHours
						+ differentDay
						* (assignment.get(j + 1).getStartDate().getDay() - day)
						- endHours)
						&& Math.abs(nextStartHours
								+ differentDay
								* (assignment.get(j + 1).getStartDate()
										.getDay() - day) - endHours) < MEDIUM_DURATION
						|| Math.abs(nextStartHours
								+ differentDay
								* (assignment.get(j + 1).getStartDate()
										.getDay() - day) - endHours) == 1
						&& nextStartMinutes - endMinutes > 0) {

					// free block duration
					index = DAYS * NUM_OF_TIME_BLOCKS + day * DURATION + 1;
					// calenderFeatVector[i][index]++;
					feature[index]++;

				}

				else {

					// free block duration
					index = DAYS * NUM_OF_TIME_BLOCKS + day * DURATION + 2;
					// calenderFeatVector[i][index]++;
					feature[index]++;

				}
			}

			// setting the meeting duration feature eg: short meeting on
			// monday

			if (duration < SHORT_DURATION) {

				// meeting duration
				index = DAYS * NUM_OF_TIME_BLOCKS + DAYS * DURATION + day
						* DURATION + 0;
				// calenderFeatVector[i][index]++;
				feature[index]++;

			}

			else if (SHORT_DURATION <= duration && duration < MEDIUM_DURATION) {

				// meeting duration
				index = DAYS * NUM_OF_TIME_BLOCKS + DAYS * DURATION + day
						* DURATION + 1;
				// calenderFeatVector[i][index]++;
				feature[index]++;

			}

			// don't forget to add the free blocks in all the days
			else {

				// meeting duration
				index = DAYS * NUM_OF_TIME_BLOCKS + DAYS * DURATION + day
						* DURATION + 2;
				// calenderFeatVector[i][index]++;
				feature[index]++;

			}
		}
		return feature;
	}

}
