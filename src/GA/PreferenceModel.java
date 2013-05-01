package GA;

import java.util.ArrayList;
import java.util.HashMap;

public class PreferenceModel {

	public static HashMap<Long, ArrayList<Integer>> PM=new HashMap<Long, ArrayList<Integer>>();
	private static int numPref;
	public PreferenceModel(long id, ArrayList<Integer> pref) {
		// TODO Auto-generated constructor stub
//		numPref=pref.size();
		numPref=2;// now we have only two impressions
		PM.put(id, pref);
	}
	
	public static ArrayList<Integer> pref(long task_id){
		return PM.get(task_id);
	}
	public static int PreferencesNum(){
		return numPref;
	}
}
