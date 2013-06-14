package sak.todo.gui.agenda;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class TaskActionsDialog extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setItems(TasksListActivity.TASKS_ACTIONS, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				TasksListActivity tasksListActivity  = (TasksListActivity)getActivity();
				tasksListActivity.tasksActions[which].run();
			}
		});

		// Create the AlertDialog object and return it
		return builder.create();
	}
}
