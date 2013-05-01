package sak.todo.timeline;

import java.util.StringTokenizer;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.VerticalAlign;

import android.widget.Toast;

import sak.todo.database.Task;

public class TaskEntity extends Entity{
	public static float TASK_WIDTH = Calendar.DAY_WIDTH; 
	public static float DURATION_MINUTES_TO_HEIGHT_RATION = Calendar.HOUR_WIDTH;
	
	public Task task;
	
	private static final int TEXT_PADING = 6;
	public float height;
	
	private Rectangle rec;
	private AlignedText body;
	public TaskEntity(Task t, float x, float y) {
		this.task = t;
		this.height = t.estimate * DURATION_MINUTES_TO_HEIGHT_RATION;
		
		// TODO: task needs to have a label
		rec = new Rectangle(x, y, TASK_WIDTH, height){
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if(pSceneTouchEvent.isActionDown()){
					Calendar.currentInstance.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(Calendar.currentInstance, TaskEntity.this.task.body, Toast.LENGTH_SHORT).show();
						}
					});
				}
				return true;
			}
		};
		rec.setColor(0.2f, 0.85f, 0.14f, 0.6f);
		this.attachChild(rec);
		
		Calendar.currentInstance.scene.registerTouchArea(rec);
		
		body = new AlignedText(x + TEXT_PADING, y + TEXT_PADING, Calendar.tasksFont, fitToWidth(t.body), HorizontalAlign.LEFT, VerticalAlign.CENTER, (int)TASK_WIDTH, (int)height);
		
		body.setSize(TASK_WIDTH, height);
		this.attachChild(body);
		
	}
	
	/**
	 * This function returns new String that can fit in max line size of 15 char/line
	 * Simply, it adds '\n' char after each 15 char, but it takes care of spaces and special chars.
	 * 
	 * TODO: add some unit test for this function as it returns some incorrect output.
	 * */
	private String fitToWidth(String input){
		StringTokenizer st = new StringTokenizer(input, " ");
		StringBuilder sb = new StringBuilder(input.length() + st.countTokens());
		
		int maxSize = 15;
		int lineSize = 0;
		while (st.hasMoreElements()) {
			String word = st.nextToken();
			
			if(lineSize == 0){
				sb.append(word + ' ');
				lineSize = word.length() + 1;
			}
			if(lineSize + word.length() > maxSize){
				sb.append('\n');
				sb.append(word+' ');
				lineSize = word.length() + 1;
			}else{
				sb.append(word+' ');
				lineSize += word.length() + 1;
			}
		}
		
		return input;
	}
	
	
	@Override
	public void setPosition(final float pX, final float pY) {
//		super.setPosition(pX, pY);
//		final float step_x = (rec.getX() - pX) / 10;
//		final float step_y = (rec.getY() - pY) / 10;
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				float x = rec.getX();
//				float y = rec.getY();
//				for (int i = 0; i < 10; i++) {
//					x += step_x;
//					y += step_y;
//				
//					rec.setPosition(x, y);
//					body.setPosition(x + TEXT_PADING, y + TEXT_PADING);
//					try {
//						Thread.sleep(100);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
				
				rec.setPosition(pX, pY);
				body.setPosition(pX + TEXT_PADING, pY + TEXT_PADING);
//			}
//		}).start();

	}
	
}
