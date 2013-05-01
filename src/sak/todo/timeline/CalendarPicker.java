package sak.todo.timeline;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.VerticalAlign;

import android.util.Log;

import sak.todo.database.Task;

public class CalendarPicker extends Entity {

	private float width;
	private float height;

	private float x;
	private float y;

	private int selectedCalendar;
	private Rectangle selected;

	private float assignmentHeight;
	private float assignmentWidth;
	

	public CalendarPicker(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
		
		// colored rectangle forms the background of this entity
		Rectangle rec = new Rectangle(x, y, width, height);
		rec.setColor(0.82f, 0.82f, 0.82f, 1f);
		attachChild(rec);

		this.assignmentWidth = this.width;
	}
	
	public void init(int numOfCalendars){
		this.assignmentHeight = this.height / numOfCalendars;

		Random rand = new Random();
		for (int i = 0; i < numOfCalendars; i++) {
			String name = "Calendar" + (i + 1);
			
			
			int row = i;

			float x = this.x;
			float y = this.y + assignmentHeight * row;

			Rectangle rectangle=new Rectangle(x, y, assignmentWidth, assignmentHeight);
			rectangle.setColor(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(),1f);
			attachChild(rectangle);
			
			final int calendarNumber = i;
			AlignedText calendar = new AlignedText(x, y,
					sak.todo.timeline.Calendar.font, name,
					HorizontalAlign.CENTER, VerticalAlign.CENTER,
					(int) assignmentWidth, (int) assignmentHeight) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
						float pTouchAreaLocalX, float pTouchAreaLocalY) {

					if (CalendarPicker.this.isVisible() &&
							pSceneTouchEvent.isActionDown()) {
						
						sak.todo.timeline.Calendar.currentInstance.calendarPicker.hideAnimated();
						sak.todo.timeline.Calendar.currentInstance.selectCalendar(calendarNumber);
					}
					return true;
				}
			};
			sak.todo.timeline.Calendar.currentInstance.scene.registerTouchArea(calendar);
			
			Line l = new Line(x, y, x+assignmentWidth, y,3);
			l.setColor(1, 1, 1);
			attachChild(calendar);
			attachChild(l);
			
			// set the selected month
			if (i == selectedCalendar) {
				selected = new Rectangle(x, y, assignmentWidth, assignmentHeight);
				selected.setColor(0.01f, 0.74f, 0.95f, 0.2f);
				attachChild(selected);
			}
		}		
	}
	
	public void setSelectedMonth(int selectedCalendar) {
		Log.d("CAL_PIC", "CalendarPicker:setSelectedMonth");
		this.selectedCalendar = selectedCalendar;
		Calendar.currentInstance.loadTasksFromAssignment(Calendar.currentInstance.assignments.get(selectedCalendar));
	}

	public void showAnimated() {

		new Thread(new Runnable() {
			public void run() {
				float scale = 0.05f;
				CalendarPicker.this.setScaleY(scale);
				CalendarPicker.this.setVisible(true);
				while (scale <= 1.05) {
					try {
						Thread.sleep(15);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					CalendarPicker.this.setScaleY(scale);
					scale += 0.05f;
				}
			}
		}).start();
	}

	public void hideAnimated() {

		new Thread(new Runnable() {
			public void run() {
				float scale = 1f;
				CalendarPicker.this.setScaleY(scale);
				while (scale > 0.5) {
					try {
						Thread.sleep(15);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					CalendarPicker.this.setScaleY(scale);
					scale -= 0.05f;
				}
				CalendarPicker.this.setVisible(false);
			}
		}).start();
	}

}
