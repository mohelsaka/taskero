package sak.todo.gui.schedules;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import sak.todo.database.Task;
import sak.todo.gui.R;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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
		
		TextView dueDate =  (TextView)layout.findViewById(R.id.task_date);
		Calendar c = Calendar.getInstance();
		long duedate = t.duedate.getTime();
		c.setTimeInMillis(duedate);
		
		dueDate.setText(Task.getFormatedDate(c));
		
		LinearLayout taskPrioityLabel = (LinearLayout)layout.findViewById(R.id.task_priority_label);
		int priorty = t.priority;
//		int color = Color.rgb(25 * priorty, 25 * priorty, 25 * priorty);
		taskPrioityLabel.setBackgroundColor(Task.PRIORITY_COLORS[priorty]);
		
		float duration = t.estimate;
		TextView durationView = (TextView) layout.findViewById(R.id.task_duration);
		durationView.setText(""+duration+ " hours");
		
		if(t.schedulledNow){ 
			layout.setBackgroundColor(Color.GRAY);
		}
		
		return layout;
		
	}
}
