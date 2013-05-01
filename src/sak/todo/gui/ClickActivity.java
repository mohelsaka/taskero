package sak.todo.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

public class ClickActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.stat_neutral);
		builder.setTitle("Alarm!");
		CharSequence[] items = { "Got started with it.", "Re-schedule it.",
				"Delete it." };
		builder.setItems(items, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					Toast.makeText(getApplicationContext(),
							"Got started with task", Toast.LENGTH_SHORT).show();
					break;
				case 1:
					Toast.makeText(getApplicationContext(), "Re-shedule it.",
							Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(getApplicationContext(), "Delete it.",
							Toast.LENGTH_SHORT).show();
					break;

				default:
					break;
				}
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
