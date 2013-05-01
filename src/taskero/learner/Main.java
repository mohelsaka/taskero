
package taskero.learner;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import sak.todo.database.Task;

import android.os.Bundle;
import android.app.Activity;

public class Main extends Activity {
	
	static{
		System.loadLibrary("svm-learn");
	}
	
//	private native void helloLog(String logThis);
//	private native int sum(int x, int y);
//	private native void nativeRead();
//	private native void rankLearn();
//	private native void rankClassify();
//	private native String stringFromJNI();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);
//		TextView hh=(TextView) findViewById(R.id.text);
//		hh.setText("hello there");
//		helloLog("This will log to LogCat via the native call.");
//		int x  = sum(1, 1);
//		Log.d("TEST", ""+x);
		
		// saving training data file
		/*
		try {
			FileOutputStream f = openFileOutput("train.dat", Activity.MODE_PRIVATE);
			String s = "# query 1\n\0" + 
					"3 qid:1 1:0 2:1 3:0 4:0.2 5:0\n\0" + 
					"2 qid:1 1:1 2:0 3:1 4:0.1 5:1\n\0" + 
					"1 qid:1 1:3 2:1 3:0 4:0.4 5:0\n\0" + 
					"1 qid:1 1:4 2:0 3:1 4:0.3 5:0\n\0" + 
					"# query 2\n\0" + 
					"1 qid:2 1:0 2:0 3:1 4:0.2 5:0\n\0" + 
					"2 qid:2 1:1 2:0 3:1 4:0.4 5:0\n\0" + 
					"1 qid:2 1:5 2:0 3:1 4:0.1 5:0\n\0" + 
					"1 qid:2 1:8 2:0 3:1 4:0.2 5:0\n\0" + 
					"# query 3\n\0" + 
					"2 qid:3 1:0 2:0 3:1 4:0.1 5:1\n\0" + 
					"3 qid:3 1:1 2:1 3:0 4:0.3 5:0\n\0" + 
					"4 qid:3 1:1 2:0 3:0 4:0.4 5:1\n\0" + 
					"1 qid:3 1:0 2:1 3:1 4:0.5 5:0\n\0";
			
			f.write(s.getBytes(), 0, s.length());
			f.flush();
			f.close();
		} catch (Exception e) {
			Log.e("train.dat", e.getMessage(), e);
		}
//		nativeRead();
 */
//		Log.e("ERROR", "HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
//		Log.d("TASKERO", "HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
//		Vector<Vector<Task>> calender = new Vector<Vector<Task>>();
//		Task t1 = new Task();
//		t1.start.setHours(10);
//		t1.day = 0;
//		t1.end.setHours(12);
//
//		Task t2 = new Task();
//		t2.start.setHours(14);
//		t2.day = 0;
//		t2.end.setHours(16);
//
//		Vector<Task> v1 = new Vector<Task>();
//		Vector<Task> v2 = new Vector<Task>();
//		v1.add(t1);
//		v1.add(t2);
//
//		Task t3 = new Task();
//		t3.start.setHours(17);
//		t3.day = 0;
//		t3.end.setHours(20);
//
//		Task t4 = new Task();
//		t4.start.setHours(21);
//		t4.day = 1;
//		t4.end.setHours(23);
//		v2.add(t3);
//		v2.add(t4);
//
//		calender.add(v1);
//		calender.add(v2);
//
//		Preference_Learner pl = new Preference_Learner(calender,this);
////		pl.setcalenderFeatVector();
//		try {
//			calender=pl.rank();
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.d("TASKERO", "YA RABBBBB");
//		}
//		Log.d("TASKERO", calender.get(0).get(0).start.getHours()+"");
//		String h="";
//		for (int i = 0; i < pl.calenderFeatVector.length; i++) {
//			for (int j = 0; j < pl.calenderFeatVector[i].featureVector.length; j++) {
//				h+=pl.calenderFeatVector[i].featureVector[j] + " ";
//				
//			}
//			Log.d("TASKERO", h);
//		}
//
//		
//		rankLearn();
//		rankClassify();
//		try {
////			appendToTrainingTupples(CFV);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	/*
	 * This function returns an array of the predictions.
	 * */
	Float[] readPredictions() throws IOException{
		FileInputStream in = openFileInput("predictions.train");
		Scanner sc = new Scanner(in);
		
		ArrayList<Float> ar = new ArrayList<Float>();
		
		while(sc.hasNext()){
			ar.add(sc.nextFloat());
		}
		sc.close();
		in.close();
		
		Float [] out = new Float[ar.size()];
		ar.toArray(out);
		return out;
	}
	
	/*
	 * saving the data to be classified in a specific place that will be used later
	 * in classification.
	 * 
	 * TODO: objects will be later of type CalenderFeatureVector and it has to implement a convenient toString method
	 * */
	void saveTestingTuples(CalenderFeatureVector[] ar) throws IOException{
		FileOutputStream f = openFileOutput("test.dat", Activity.MODE_PRIVATE);
		
		StringBuilder str = new StringBuilder();
		for(CalenderFeatureVector o : ar)
			str.append(o.toString() + '\n');
		
		f.write(str.toString().getBytes(), 0, str.length() - 1);
		f.flush();
		f.close();
	}
	
	/*
	 * This function is used to append the information of the new scheduling session to the current
	 * training data.
	 * 
	 * TODO: objects will be later of type CalenderFeatureVector and it has to implement a convenient toString method
	 * */
	void appendToTrainingTupples(Vector<Vector<Task>> calendar, HashMap<Vector<Task>,CalenderFeatureVector > featureMapping) throws IOException{
		FileOutputStream f = openFileOutput("train.dat", Activity.MODE_APPEND);
		
		StringBuilder str = new StringBuilder();
//		str.append("1 qid:1");
		for(int i=0;i<calendar.size();i++)
		{
			if(i==0)
				str.append("1 qid:"+i+featureMapping.get(calendar.get(i)).toString() + '\n');
			else
				str.append("0 qid:"+i+featureMapping.get(calendar.get(i)).toString() + '\n');
		}
		f.write(str.toString().getBytes(), 0, str.length() - 1);
		f.flush();
		f.close();
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_main, menu);
//		return true;
//	}

}
