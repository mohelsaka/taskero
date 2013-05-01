package sak.todo.gui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FriendsList extends Activity {
	private ArrayAdapter<String> adapter;
	private ListView listView;
	public static final int SELECTED_FRIENDS_RESULT_ID = 100 ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_list);
		
		listView = (ListView) findViewById(R.id.mylist);

		// retrieve friends list
		retrieveFriendsList();
		
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice, values);

		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}
	
	public void captureSelectedFriends(View view){
		long[] _selectedPeople = listView.getCheckItemIds();
		Intent data = new Intent();
		data.putExtra("selected_ids", _selectedPeople);
		setResult(SELECTED_FRIENDS_RESULT_ID, data);
		this.finish();
	}
	
	public static long[] selectedPeople;
	
	static String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
			"Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
			"Linux", "OS/2" };
	
	public static String getFriendName(long id){
		return values[(int)id];
	}
	
	public void addConnection(View view){
		startActivity(new Intent(this, AddConnection.class));
	}
	
	public static void retrieveFriendsList(){
		values = new String[]{"saka", "saka2", "test1"};
//		values = Friend.getFriendsListNames();
	}
}
