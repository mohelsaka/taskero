package sak.todo.timeline;

import java.util.Calendar;
import java.util.Locale;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.VerticalAlign;

public class MonthPicker extends Entity{
	float width;
	float height;
	float x;
	float y;
	int selectedMonth;
	Rectangle selected;
	float monthWidth;
	float monthHeight;
	
	public MonthPicker(float _x, float _y,float _width, float _hegith, int _selectedMonth) {
		this.width = _width;
		this.height = _hegith;
		this.x = _x;
		this.y = _y;
		
		this.selectedMonth = _selectedMonth;
		
		// colored rectangle forms the background of this entity
		Rectangle rec = new Rectangle(x, y, width, height);
		rec.setColor(0.24f, 0.96f, 0.75f, 0.56f);
		attachChild(rec);
		
		
		this.monthWidth = this.width / 4;
		this.monthHeight = this.height / 3;
		
		// drawing months
		Calendar c = Calendar.getInstance();
		for (int i = 0; i < 12; i++) {
			c.set(Calendar.MONTH, i);
			String name = c.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.SHORT, Locale.US);
			
			int row = i / 4;
			int col = i % 4;
			
			_x = this.x + monthWidth * col;
			_y = this.y + monthHeight * row;
			
			final int monthNumber = i;
			
			AlignedText month = new AlignedText(_x, _y, sak.todo.timeline.Calendar.font,
					name,
					HorizontalAlign.CENTER,
					VerticalAlign.CENTER,
					(int)monthWidth, (int)monthHeight){
				
				@Override
				public boolean onAreaTouched(
						TouchEvent pSceneTouchEvent,
						float pTouchAreaLocalX, float pTouchAreaLocalY) {
					
					if(MonthPicker.this.isVisible() &&
							pSceneTouchEvent.isActionDown()){
						
						sak.todo.timeline.Calendar.currentInstance.monthPicker.hideAnimated();
						sak.todo.timeline.Calendar.currentInstance.selectMonth(monthNumber);
					}
					return true;
				}
			};
			sak.todo.timeline.Calendar.currentInstance.scene.registerTouchArea(month);
			
			attachChild(month);
			
			// set the selected month
			if(i == selectedMonth){
				selected = new Rectangle(_x, _y, monthWidth, monthHeight);
				selected.setColor(0.01f, 0.74f, 0.95f, 0.2f);
				attachChild(selected);
			}
		}
		
	}
	
	public void showAnimated() {
		
		new Thread(new Runnable() {
			public void run() {
				float scale = 0.05f;
				MonthPicker.this.setScaleY(scale);
				MonthPicker.this.setVisible(true);
				while(scale <= 1.05){
					try {
						Thread.sleep(15);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					MonthPicker.this.setScaleY(scale);
					scale += 0.05f;
				}
			}
		}).start();
	}
	public void hideAnimated() {
		
		new Thread(new Runnable() {
			public void run() {
				float scale = 1f;
				MonthPicker.this.setScaleY(scale);
				while(scale > 0.5){
					try {
						Thread.sleep(15);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					MonthPicker.this.setScaleY(scale);
					scale -= 0.05f;
				}
				MonthPicker.this.setVisible(false);
			}
		}).start();
	}
	
	public void setSelectedMonth(int monthNumber){
		int row = selectedMonth / 4;
		int col = selectedMonth % 4;
		
		int nrow = monthNumber / 4;
		int ncol = monthNumber % 4;
		
		float dy = (nrow - row) * monthHeight;
		float dx = (ncol - col) * monthWidth;
		selected.setPosition(selected.getX() + dx, selected.getY() + dy);
		selectedMonth = monthNumber;
	}
}
