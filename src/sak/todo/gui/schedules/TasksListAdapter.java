package sak.todo.gui.schedules;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import sak.todo.database.Task;
import sak.todo.gui.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TasksListAdapter extends ArrayAdapter<Task>{


	public TasksListAdapter(Context context, int resource,
			int textViewResourceId, List<Task> objects) {
		super(context, resource, textViewResourceId, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return setViewValue(inflater.inflate(R.layout.task_list_item, null), getItem(position));
	}
	
	private View setViewValue(View view, Task data) {
		Task t = (Task) data; 
		LinearLayout layout = (LinearLayout) view;
		TextView body =  (TextView)layout.findViewById(R.id.task_body);
		body.setText(t.body);
		
		TextView dueDatae =  (TextView)layout.findViewById(R.id.task_date);
		Calendar c = Calendar.getInstance();
		long duedate = t.duedate.getTime();
		c.setTimeInMillis(duedate);
		
		String s = String.format("%s %s %d %2d:%2d %s",
				c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US),
				c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US),
				c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.HOUR),
				c.get(Calendar.MINUTE),
				c.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US)
				);
		dueDatae.setText(s);
		
		LinearLayout taskPrioityLabel = (LinearLayout)layout.findViewById(R.id.task_priority_label);
		int priorty = t.priority;
		int color = Color.rgb(25 * priorty, 25 * priorty, 25 * priorty);
		taskPrioityLabel.setBackgroundColor(color);
		
		float duration = t.estimate;
		TextView durationView = (TextView) layout.findViewById(R.id.task_duration);
		durationView.setText(""+duration+ " hours");
		
		return layout;
	}
}
