package com.learner.svm;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import sak.todo.database.Task;

import taskero.learner.CalenderFeatureVector;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class SVMAdapter {
	static {
		System.loadLibrary("svm-learn");
	}
	// native functions
	private native void rankLearn(String train, String model);
	private native void rankClassify(String test, String model, String predictions);
	
	Context context;

	String data_dir;
	
	public String train_file = "train.dat";
	public String model_file = "model";
	public String test_file = "test.dat";
	public String predictions_file = "predictions";
	private int numOfRuns;
		
	public void setNumOfRuns(int numOfRuns) {
		this.numOfRuns = numOfRuns;
	}
	public SVMAdapter(Context _context) throws NameNotFoundException {
		this.context = _context; 
		final String package_name = context.getApplicationContext().getPackageName();
		data_dir = context.getPackageManager().getPackageInfo(package_name, 0).applicationInfo.dataDir + "/files/";
		Log.d("DATA", data_dir);
	}

	public void init() throws IOException {
		// testing with a data file
		FileOutputStream f = context.openFileOutput(train_file, Context.MODE_PRIVATE);

		String s = "# query 1\n"
				+ "3 qid:1 1:0 2:1 3:0 4:0.2 5:0\n"
				+ "2 qid:1 1:1 2:0 3:1 4:0.1 5:1\n"
				+ "1 qid:1 1:3 2:1 3:0 4:0.4 5:0\n"
				+ "1 qid:1 1:4 2:0 3:1 4:0.3 5:0\n"
				+ "# query 2\n"
				+ "1 qid:2 1:0 2:0 3:1 4:0.2 5:0\n"
				+ "2 qid:2 1:1 2:0 3:1 4:0.4 5:0\n"
				+ "1 qid:2 1:5 2:0 3:1 4:0.1 5:0\n"
				+ "1 qid:2 1:8 2:0 3:1 4:0.2 5:0\n"
				+ "# query 3\n"
				+ "2 qid:3 1:0 2:0 3:1 4:0.1 5:1\n"
				+ "3 qid:3 1:1 2:1 3:0 4:0.3 5:0\n"
				+ "4 qid:3 1:1 2:0 3:0 4:0.4 5:1\n"
				+ "1 qid:3 1:0 2:1 3:1 4:0.5 5:0\n";

		f.write(s.getBytes(), 0, s.length());
		f.flush();
		f.close();
		
		// writing for testing file
//		f = context.openFileOutput(test_file, Context.MODE_WORLD_READABLE);
//		s = "0 qid:4 1:1 2:0 3:0 4:0.2 5:1\n"+
//			"0 qid:4 1:1 2:1 3:0 4:0.3 5:0\n"+
//			"0 qid:4 1:0 2:0 3:0 4:0.2 5:1\n"+
//			"0 qid:4 1:0 2:0 3:1 4:0.2 5:0\n";
//		
//		f.write(s.getBytes(), 0, s.length());
//		f.flush();
//		f.close();

//		f = context.openFileOutput(model_file, Context.MODE_WORLD_READABLE);
//		s = "SVM-light Version V6.20\n"+
//						"0 # kernel type\n"+
//						"3 # kernel parameter -d \n"+
//						"1 # kernel parameter -g \n"+
//						"1 # kernel parameter -s \n"+
//						"1 # kernel parameter -r \n"+
//						"empty# kernel parameter -u \n"+
//						"78 # highest feature index \n"+
//						"7 # number of training documents \n"+
//						"2 # number of support vectors plus 1 \n"+
//						"0 # threshold b, each following line is a SV (starting with alpha*y)\n"+
//						"1 1:-0.5011691777 2:-1.0033824507 3:2.020214461 4:5.032343138 5:0.619348547 6:0.5080857845 7:0.330916763 8:0.372772063 9:2.0020895251 12:2.016171569 13:2.0080857845 17:-1.0020024232 18:-4.0013349488 20:-1.0081842439 22:-0.2065146969 23:0.8 24:-0.3081842439 27:-3.0070150662 28:-4.02117171 29:5.003383785 32:-0.622489389 33:5.0013448098 34:4.0056223474 35:-8.0063938452 36:7.019555448 37:8.065991521 38:0.07822179 40:-0.9840048463 42:-9.015367749 44:8.050876547 45:7.0097777238 46:-8.0013349488 48:-8.0023383554 49:-9.0056223474 50:-8.01353514 51:7.033017363 56:-9.017814266 57:-8.010151355 58:-0.99101274 60:9.030525928 63:-0.9013349488 64:0.7740428923 65:-5.011244695 66:-5.016867042 68:-0.712128677 70:-0.615367749 73:0.719555448 75:-0.9911691777 76:-0.7723383554 77:-0.922746585 #";
//		
//		f.write(s.getBytes(), 0, s.length());
//		f.flush();
//		f.close();
		
//		runLearning();
	}
	
	/**
	 * wrapper for native function rankLearn
	 * */
	public void runLearning() {
		rankLearn(data_dir + train_file, data_dir + model_file);
	}
	
	/**
	 * wrapper for native function rankClassify
	 * */
	public void runClassify(){
		rankClassify(data_dir+test_file,
				data_dir + model_file,
				data_dir + predictions_file);

		try {
			Log.d("SVM", dumpFile(test_file));
			Log.d("SVM", dumpFile(predictions_file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/*
	 * This function returns an array of the predictions.
	 * */
	public Float[] readPredictions() throws IOException{
		FileInputStream in = context.openFileInput(predictions_file);
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
	
	/**
	 * saving the data to be classified in a specific place that will be used later
	 * in classification.
	 * 
	 * TODO: objects will be later of type Calendar and it has to implement a convenient toString method
	 * */
	
	public void saveTestingTuples(CalenderFeatureVector[] ar,int numOFRuns) throws IOException{
		FileOutputStream f = context.openFileOutput("test.dat", Activity.MODE_PRIVATE);
		
		StringBuilder str = new StringBuilder();
		for(CalenderFeatureVector o : ar)
			str.append("0 qid:"+numOFRuns+o.toString() + '\n');
		
		f.write(str.toString().getBytes(), 0, str.length() - 1);
		f.flush();
		f.close();
	}
	
	/**
	 * This function is used to append the information of the new scheduling session to the current
	 * training data.
	 * 
	 * TODO: objects will be later of type Calendar and it has to implement a convenient toString method
	 * */
	public void appendToTrainingTupples(ArrayList<ArrayList<Task>> calendar, CalenderFeatureVector[]cal,int numOfRuns) throws IOException{
		FileOutputStream f = context.openFileOutput("train.dat", Activity.MODE_APPEND);
		
		StringBuilder str = new StringBuilder();
//		str.append("1 qid:1");
		for(int i=0;i<calendar.size();i++)
		{
			if(i==0)
				str.append("1 qid:"+numOfRuns+cal[i].toString() + '\n');
			else
				str.append("0 qid:"+numOfRuns+cal[i].toString() + '\n');
		}
		
		f.write(str.toString().getBytes(), 0, str.length() - 1);
		f.flush();
		f.close();
	}
	
	/**
	 * This function returns the content of a file specified by filename
	 * @param filenName
	 *  	the name of the file, it has to be one of {train_file, model_file, test_file, predictions_file}
	 *  	e.g. dumpFile(svm.train_file)
	 *  @return 
	 *  	String contains the content of the file
	 * */
	public String dumpFile(String fileName) throws IOException{
		FileInputStream f = context.openFileInput(fileName);
		byte[] buffer = new byte[1024 * 1024];
		int count = f.read(buffer);
		return new String(buffer, 0, count);
	}
}
