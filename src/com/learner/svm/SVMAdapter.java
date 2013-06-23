package com.learner.svm;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import sak.todo.database.Task;

import taskero.learner.CalenderFeatureVector;
import taskero.learner.Preference_Learner;
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
	
	public static final String train_file = "train.dat";
	public static final String model_file = "model.dat";
	public static final String test_file = "test.dat";
	public static final String predictions_file = "predictions";
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

//		String s = "# query 1\n"
//				+ "3 qid:1 1:0 2:1 3:0 4:0.2 5:0\n"
//				+ "2 qid:1 1:1 2:0 3:1 4:0.1 5:1\n"
//				+ "1 qid:1 1:3 2:1 3:0 4:0.4 5:0\n"
//				+ "1 qid:1 1:4 2:0 3:1 4:0.3 5:0\n"
//				+ "# query 2\n"
//				+ "1 qid:2 1:0 2:0 3:1 4:0.2 5:0\n"
//				+ "2 qid:2 1:1 2:0 3:1 4:0.4 5:0\n"
//				+ "1 qid:2 1:5 2:0 3:1 4:0.1 5:0\n"
//				+ "1 qid:2 1:8 2:0 3:1 4:0.2 5:0\n"
//				+ "# query 3\n"
//				+ "2 qid:3 1:0 2:0 3:1 4:0.1 5:1\n"
//				+ "3 qid:3 1:1 2:1 3:0 4:0.3 5:0\n"
//				+ "4 qid:3 1:1 2:0 3:0 4:0.4 5:1\n"
//				+ "1 qid:3 1:0 2:1 3:1 4:0.5 5:0\n";

//		f.write(s.getBytes(), 0, s.length());
//		f.flush();
//		f.close();
		
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
		
		// check if the model file exists or not before trying to create new one.
		if (context.getFileStreamPath(model_file).exists()) {
			return;
		}
		
		FileOutputStream f = context.openFileOutput(model_file, Context.MODE_PRIVATE);

		String s = "SVM-light Version V6.20\n"+
					"0 # kernel type\n"+
					"3 # kernel parameter -d\n"+ 
					"1 # kernel parameter -g\n"+ 
					"1 # kernel parameter -s\n"+ 
					"1 # kernel parameter -r\n"+ 
					"empty# kernel parameter -u\n"+ 
					"78 # highest feature index\n"+ 
					"12 # number of training documents\n"+ 
					"2 # number of support vectors plus 1\n"+ 
					"0 # threshold b, each following line is a SV (starting with alpha*y)\n"+
					"1 1:-0.036026154 3:-0.49825519 11:-0.49825519 15:-0.49825519 16:0.058761824 20:0.49825519 21:-0.41316158 22:-0.41316158 23:-0.41316158 49:0.93308568 51:-0.41316158 56:-0.35439977 58:0.10782927 60:0.78923023 62:-0.10782927 65:-0.74016279 67:-0.10782927 68:0.17628546 72:0.0096943667 75:-0.28771085 77:0.5375455 #";
		
		f.write(s.getBytes(), 0, s.length());
		f.flush();
		f.close();
	}
	
	/**
	 * wrapper for native function rankLearn
	 * */
	public void runLearning() {
		
		try {
//			Log.d("SVM", dumpFile(test_file));
//			Log.d("SVM", dumpFile(predictions_file));
			Log.d("SVM", dumpFile(train_file));
			Log.d("SVM", dumpFile(model_file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rankLearn(data_dir + train_file, data_dir + model_file);
	}
	
	/**
	 * wrapper for native function rankClassify
	 * */
	public void runClassify(){
		try {
			Log.d("SVM", dumpFile(test_file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		FileOutputStream f = context.openFileOutput(test_file, Activity.MODE_PRIVATE);
		
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
		FileOutputStream f = context.openFileOutput(train_file, Activity.MODE_APPEND);
		
		StringBuilder str = new StringBuilder();
		for(int i=0;i<calendar.size();i++)
		{
			if(i==0)
				str.append("1 qid:"+numOfRuns+cal[i].toString() + '\n');
			else
				str.append("0 qid:"+numOfRuns+cal[i].toString() + '\n');
		}
		
		f.write(str.toString().getBytes(), 0, str.length());
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
	
	/**
	 * 
	 * reads the model file and parses svm weights as an array of floats
	 * @throws IOException 
	 * 
	 * */
	public HashMap<Integer, Float> getSVMWeights() throws IOException{
		String model = dumpFile(model_file);
		HashMap<Integer, Float> weights = new HashMap<Integer, Float>();
		
		int indexOfLastLine = model.lastIndexOf('\n');
		String lastLine = model.substring(indexOfLastLine + 3); // skipping some chars
		String[] splits = lastLine.split(" ");
		for (String weightPair : splits) {
			String [] pair = weightPair.split(":");
			weights.put(Integer.parseInt(pair[0]), Float.parseFloat(pair[1]));
		}
		
		return weights;
	}
}
