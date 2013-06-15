package sak.todo.gui.agenda;

import java.util.Calendar;
import java.util.Locale;

import sak.todo.database.DBHelper;
import sak.todo.database.Task;
import sak.todo.gui.R;
import android.R.color;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TasksAgendaAdapter extends CursorAdapter{
	
	public TasksAgendaAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		LinearLayout layout = (LinearLayout) view;
		final TextView body =  (TextView)layout.findViewById(R.id.task_body);
		body.setText(cursor.getString(DBHelper.COLUMN_BODY_NUM));
		
		TextView dueDatae =  (TextView)layout.findViewById(R.id.task_date);
		Calendar c = Calendar.getInstance();
		long duedate = cursor.getLong(DBHelper.COLUMN_DUE_DATE_NUM);
		c.setTimeInMillis(duedate);
		
		dueDatae.setText(Task.getFormatedDate(c));
		
		LinearLayout taskPrioityLabel = (LinearLayout)layout.findViewById(R.id.task_priority_label);
		int priorty = cursor.getInt(DBHelper.COLUMN_PRIORITY_NUM);
		taskPrioityLabel.setBackgroundColor(Task.PRIORITY_COLORS[priorty]);
		
		float duration = cursor.getFloat(DBHelper.COLUMN_ESTIMATE_NUM);
		TextView durationView = (TextView) layout.findViewById(R.id.task_duration);
		durationView.setText(""+duration+ " hours");
		
		if(duedate < System.currentTimeMillis()){
			layout.setBackgroundColor(Color.LTGRAY);
		}else{
			layout.setBackgroundColor(Color.WHITE);
		}
		
//		layout.setOnClickListener(new View.OnClickListener() {
//			
//			public void onClick(View v) {
//				body.setText("This is a very very very very long discreption fot this task what a ver y very very very very very very very very very very long task");
//			}
//		});
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.task_list_item, null);
	}
	
}
