package sak.todo.gui;

import sak.todo.database.Friend;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddConnection extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_connection);
		addButton = (Button)findViewById(R.id.addFriend);
		addButton.setEnabled(false);
		
		fName = (EditText)findViewById(R.id.friendName);
		fEmail = (EditText)findViewById(R.id.friendEmail);
		
		fName.addTextChangedListener(t);
		fEmail.addTextChangedListener(t);
	}
	
	// listner to diable add button when textEdit is changed.
	private final TextWatcher t = new TextWatcher() {
		
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			addButton.setEnabled(false);
		}
		
		
		public void afterTextChanged(Editable s) {
		}
	};
	
	private Button addButton;
	private EditText fName;
	private EditText fEmail;
	
	Friend f;
	public void addFoundFriend(View view){
		Friend.addFriend(f);
	}
	
	public void findConnection(View view){
		f = Friend.findRemoteFriend(fName.getText().toString(), fEmail.getText().toString());
		if(f == null){
			Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, f.name+" Found", Toast.LENGTH_SHORT).show();
			fName.setText(f.name);
			fEmail.setText("");
			addButton.setEnabled(true);
		}
	}
	
}
