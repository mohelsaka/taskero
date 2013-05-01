package sak.todo.timeline;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ClickDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;

import sak.todo.database.Task;
import sak.todo.database.TasksIterator;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;

/**
 * @author mohamed elsaka
 *	This class contains implementation for Calendar using andeninge library. It is fully customizable and effecient.
 *
 *	Feel free to use or edit this source anywhere, just leave me a message on twitter: mohelsake
 */
public class Calendar extends BaseGameActivity implements
		IOnSceneTouchListener, IClickDetectorListener, IScrollDetectorListener {

	
	public Camera camera;
	public Scene scene;

	// ===========================================================
	// Constants
	// ===========================================================
	protected static int CAMERA_WIDTH = 800;
	protected static int CAMERA_HEIGHT = 1280;

	 
	protected static float HOURS_LINE_WIDTH = 50f;
	protected static float DAY_WIDTH = 100;
	protected static float HOUR_WIDTH = 160f;
	protected static float MONTH_LABEL_HEIGHT = 80f;
	protected static float DAYS_LABEL_HEIGHT = 60f;

	protected static float LINES_WIDTH = 2f;

	protected static float CAMERA_MAX_Y = MONTH_LABEL_HEIGHT + DAYS_LABEL_HEIGHT + HOUR_WIDTH * 24 - CAMERA_HEIGHT / 2;

	// LIMTERS
	float DAYS_LABEL_Y = MONTH_LABEL_HEIGHT + DAYS_LABEL_HEIGHT;
	float DAYS_Y_END = DAYS_LABEL_Y + HOUR_WIDTH * 24;
	
	// ===========================================================
	// Attributes
	// ===========================================================
	public static Calendar currentInstance;

	public static Font font;
	public static Font tasksFont;

//	ArrayList<Task> currentCalendar;
	ArrayList<ArrayList<Task>> assignments;
	
	Entity daysEntity = new Entity();
	Entity hoursEntity = new Entity();
	
	MonthPicker monthPicker;
	CalendarPicker calendarPicker;
	
	Rectangle calendarRect;
	int currentMonth;
	
	HashMap<Long, TaskEntity> movingTasks = new HashMap<Long, TaskEntity>();
	Entity tasksLines = new Entity();
	Entity tasksHolder = new Entity();
	
	private ChangeableText monthNameLabel;
	private ChangeableText calendarAssig;
	private float CAMERA_MAX_X;
	private int numberOfDays;
	
	// click and scroll detectors
	private ClickDetector clickDetector;
	private SurfaceScrollDetector scrollDetector;
	
	public Engine onLoadEngine() {
		// this.camera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT,
		// 100f, 100f, 0.5f);
		this.camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		final EngineOptions engineOptions = new EngineOptions(false,
				ScreenOrientation.PORTRAIT, new FillResolutionPolicy(),
				this.camera);
		engineOptions.getTouchOptions().setRunOnUpdateThread(true);

		final Engine engine = new Engine(engineOptions);
		return engine;
	}

	public void onLoadResources() {
		BitmapTextureAtlas fontTexture = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		font = new Font(fontTexture, Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL), 35, true, Color.BLACK);

		BitmapTextureAtlas taskFontTexture = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tasksFont = new Font(taskFontTexture, Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC), 20, true, Color.BLACK);

		this.mEngine.getTextureManager().loadTexture(fontTexture);
		this.mEngine.getFontManager().loadFont(font);
		this.mEngine.getTextureManager().loadTexture(taskFontTexture);
		this.mEngine.getFontManager().loadFont(tasksFont);
		
		// setting globally accessible instance of Calendar Activity class
		Calendar.currentInstance = this;
		
		// getting assignments from intent extras
		// Note that Assignments may still equal null
		Bundle b = getIntent().getExtras();
		Object assignmentsObject;
		if(b != null && ((assignmentsObject = b.get("assignments")) != null))
			assignments = (ArrayList<ArrayList<Task>>) assignmentsObject;
		
		// set the current month
		this.currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);
	}

	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.scene = new Scene();
		scene.setBackground(new ColorBackground(1f, 1f, 1f));
		scene.setBackgroundEnabled(true);

		scene.setOnSceneTouchListener(this);
		this.scrollDetector = new SurfaceScrollDetector(this);
		this.clickDetector = new ClickDetector(this);

		Line hoursLine = new Line(HOURS_LINE_WIDTH, DAYS_LABEL_Y, HOURS_LINE_WIDTH, DAYS_Y_END, LINES_WIDTH / 2 );
		hoursLine.setColor(0, 0, 0, 0.35f);
		hoursEntity.attachChild(hoursLine);

		Rectangle hoursEntityRec = new Rectangle(0, DAYS_LABEL_Y, HOURS_LINE_WIDTH, HOUR_WIDTH * 24);
		hoursEntityRec.setColor(0.9f, 0.63f, 0.63f, 0.95f);
		hoursEntity.attachChild(hoursEntityRec, 0);
		
		scene.attachChild(hoursEntity);
		scene.attachChild(daysEntity);

		
		monthPicker = new MonthPicker(CAMERA_WIDTH / 4, MONTH_LABEL_HEIGHT, CAMERA_WIDTH / 2, CAMERA_HEIGHT / 3, currentMonth);
		calendarPicker = new CalendarPicker(CAMERA_WIDTH-CAMERA_WIDTH/3, MONTH_LABEL_HEIGHT, CAMERA_WIDTH / 3, CAMERA_HEIGHT/3);

		monthPicker.setVisible(false);
		calendarPicker.setVisible(false);
		
		scene.attachChild(monthPicker);

		// attach calendar picker only if there are calendars to be displayed
		if(assignments != null){
			calendarPicker.init(assignments.size());
			scene.attachChild(calendarPicker);
		}
		
		scene.attachChild(tasksHolder, 0);
		scene.attachChild(tasksLines, 0);

		initCalendar();
		
		return this.scene;

	}

	public void onLoadComplete() {
		// TODO Auto-generated method stub
	}

	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		this.clickDetector.onTouchEvent(pSceneTouchEvent);
		this.scrollDetector.onTouchEvent(pSceneTouchEvent);
		return true;
	}

	public void onScroll(ScrollDetector pScollDetector, TouchEvent pTouchEvent,
			float pDistanceX, float pDistanceY) {
		
		// stop moving the calendar when monthPicker or monthPicker is visible
		if (monthPicker.isVisible() || calendarPicker.isVisible())
			return;

		 pDistanceX*=4;
		 pDistanceY*=4;
		
		// update camera position
		camera.offsetCenter(-pDistanceX, -pDistanceY);
		
		// check camera limits and roll back wrong position
		if (camera.getCenterY() < CAMERA_HEIGHT / 2	|| camera.getCenterY() > CAMERA_MAX_Y)
			camera.offsetCenter(0, pDistanceY);

		if (camera.getCenterX() < CAMERA_WIDTH / 2	|| camera.getCenterX() > CAMERA_MAX_X)
			camera.offsetCenter(pDistanceX, 0);

		setEntitiesRelativePositions();
	}
	
	/**
	 * This function updates entities positions according relatively to camera position,
	 * i.e. some of the entities should be relatively static to camera position e.g. label that shows month name.
	 * */
	private void setEntitiesRelativePositions(){
		daysEntity.setPosition(daysEntity.getX(), camera.getCenterY() - CAMERA_HEIGHT / 2);
		hoursEntity.setPosition(camera.getCenterX() - CAMERA_WIDTH / 2,	hoursEntity.getY());

		monthNameLabel.setPosition(camera.getCenterX() - CAMERA_WIDTH / 2 + CAMERA_WIDTH / 3, monthNameLabel.getY());
		
		if (calendarAssig != null) {
			calendarAssig.setPosition(camera.getCenterX() - CAMERA_WIDTH / 2 + CAMERA_WIDTH-CAMERA_WIDTH/3, calendarAssig.getY());
		}
		
		calendarPicker.setPosition(camera.getCenterX() - CAMERA_WIDTH / 2 , camera.getCenterY() - CAMERA_HEIGHT / 2);
		monthPicker.setPosition(camera.getCenterX() - CAMERA_WIDTH / 2,	camera.getCenterY() - CAMERA_HEIGHT / 2);
	}
	
	
	public void onClick(ClickDetector pClickDetector, TouchEvent pTouchEvent) {
		// TODO Auto-generated method stub
	}
	
	
	/**
	 * Drawing main components of the the calendar UI.
	 * This includes drawing months, days and their labels.
	 * It also loads tasks of the current month and sets camera position at today.
	 * TODO: set current day by today.
	 * */
	public void initCalendar() {
		drawMonth(currentMonth);
		
		// load tasks of current month
		loadTasks(readTasksOfMonth(currentMonth));
		
		// draw assignments if there exist some assignments to be drawn
		if(assignments !=  null){
			drawAssingments();
		}
		
		drawDayHours();
	}

	/**
	 * This function draws main entities that represent a month. It take month number as a parameter to set the correct number of days.
	 * 
	 * @param	month	index of the month, staring from 0 to 11
	 * */
	public void drawMonth(int month) {
		// get Calendar of today
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.set(java.util.Calendar.MONTH, month);
		int year = c.get(java.util.Calendar.YEAR);
		String monthName = c.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, Locale.US);
		this.numberOfDays = c.getActualMaximum(java.util.Calendar.DATE);

		float horizontalLineWidth = HOURS_LINE_WIDTH + DAY_WIDTH * numberOfDays;

		Line monthLabel = new Line(0, MONTH_LABEL_HEIGHT, horizontalLineWidth, MONTH_LABEL_HEIGHT, LINES_WIDTH);
		monthLabel.setColor(0, 0, 0, 0.35f);

		Line daysLabels = new Line(0, DAYS_LABEL_Y, horizontalLineWidth, DAYS_LABEL_Y, LINES_WIDTH);
		daysLabels.setColor(0, 0, 0, 0.35f);

		daysEntity.attachChild(monthLabel);
		daysEntity.attachChild(daysLabels);

		// writing month name
		monthNameLabel = new ChangeableText(CAMERA_WIDTH / 3, 10,
											font,String.format("%s %d", monthName, year),
											HorizontalAlign.CENTER, 50) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionDown()) {
					if (Calendar.currentInstance.monthPicker.isVisible()) {
						Calendar.currentInstance.monthPicker.hideAnimated();
					} else {
						Calendar.currentInstance.monthPicker.showAnimated();
					}
				}
				return true;
			}
		};
		
		daysEntity.attachChild(monthNameLabel);
		scene.registerTouchArea(monthNameLabel);
		
		Rectangle daysEntityRec = new Rectangle(0, 0, horizontalLineWidth, DAYS_LABEL_Y);
		daysEntityRec.setColor(0.75f, 0.75f, 0.75f, 1f);
		daysEntity.attachChild(daysEntityRec, 0);

		// Drawing days
		float x = HOURS_LINE_WIDTH + DAY_WIDTH;
		for (int i = 1; i <= numberOfDays; i++) {
			Line day = new Line(x, DAYS_LABEL_Y, x, DAYS_Y_END, LINES_WIDTH / 3);
			day.setColor(0, 0, 0, 0.35f);
			scene.attachChild(day, 0);
			x += DAY_WIDTH;

			Text dayLabelStrnig = new Text(x - DAY_WIDTH * 1.5f, MONTH_LABEL_HEIGHT + 20, font, "" + i);
			dayLabelStrnig.setAlpha(0.85f);
			daysEntity.attachChild(dayLabelStrnig);
		}
		
		// setting maximum X position for the camera
		CAMERA_MAX_X = HOURS_LINE_WIDTH + DAY_WIDTH * numberOfDays - CAMERA_WIDTH / 2;
	}
	
	
	/**
	 * It draws label shows the assignments and it loads tasks of the first assignment.
	 */
	public void drawAssingments() {
		// create Calendar label
		calendarAssig = new ChangeableText(CAMERA_WIDTH-CAMERA_WIDTH/3, MONTH_LABEL_HEIGHT/2-10, font, "Assignments") {
			
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionDown()) {
					// toggling calendar picker
					// TODO: create parent class AnimatedTogglable, add animation and toggling logic to it.
					// TODO: make calendar and month picker extends AnimatedTogglable class.
					if (Calendar.currentInstance.calendarPicker.isVisible()) {
						Calendar.currentInstance.calendarPicker.hideAnimated();
					} else {
						Calendar.currentInstance.calendarPicker.showAnimated();
					}
				}
				return true;
			}
		};
		calendarAssig.setAlpha(0.85f);
		daysEntity.attachChild(calendarAssig);
		scene.registerTouchArea(calendarAssig);
		
		// load tasks of the first assignments to draw them.
		loadTasksFromAssignment((ArrayList<Task>)assignments.get(0));
	}
	
	/**
	 * It draws 24 horizontal line representing hours and 24 text showing the hours are these horizontal lines
	 * 
	 * TODO: eliminate two for loops into one for loop.
	 * */
	private void drawDayHours() {
		// drawing hours text indicator
		final float charOffset = font.getLetter('0').mHeight / 2;
		float y = DAYS_LABEL_Y;
		for (int i = 0; i < 24; i++) {
			Text dayLabelStrnig = new Text(10, y - charOffset, font, "" + i);
			dayLabelStrnig.setAlpha(0.85f);
			hoursEntity.attachChild(dayLabelStrnig);
			y += HOUR_WIDTH;
		}
		
		// drawing lines
		y = DAYS_LABEL_Y + HOUR_WIDTH;
		float maxLineEnd = HOURS_LINE_WIDTH + DAY_WIDTH * 31; // no more that 31 days per any month
		for (int i = 0; i < 24; i++) {
			Line hour = new Line(HOURS_LINE_WIDTH, y, maxLineEnd, y);
			hour.setLineWidth(LINES_WIDTH / 4);
			hour.setColor(0, 0, 0, 0.20f);
			scene.attachChild(hour, 0);
			y += HOUR_WIDTH;
		}
	}

	
	/**
	 * setting the month to be displayed in the calendar
	 * note that months are zero indexed
	 * */
	public void selectMonth(int monthIndex) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.set(java.util.Calendar.MONTH, monthIndex);
		
		int newNumberOfDays = c.getActualMaximum(java.util.Calendar.DATE);

		// setting the name of the current month
		// TODO: change the current year
		monthNameLabel.setText(c.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, Locale.US) + " 2013");

		// setting the new maximum x for the camera
		CAMERA_MAX_X += DAY_WIDTH * (newNumberOfDays - this.numberOfDays);
		this.numberOfDays = newNumberOfDays;

		monthPicker.setSelectedMonth(monthIndex);

		// resetting the position of the camera
		camera.setCenter(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2);
		setEntitiesRelativePositions();
	}
	
	
	/**
	 * It sets the current assignment of task that should be displayed, used in transition between assignments
	 * */
	public void selectCalendar(int calendarIndex){
		calendarAssig.setText("Calendar"+(calendarIndex+1));
		calendarPicker.setSelectedMonth(calendarIndex);
		
		ArrayList<Task> currentAssignement = ((ArrayList<ArrayList<Task>>)assignments).get(calendarIndex);
		loadTasksFromAssignment(currentAssignement);
	}
	
	public void loadTasksFromAssignment(ArrayList<Task> tasks) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTime(tasks.get(0).duedate);
		int day = c.get(java.util.Calendar.DAY_OF_MONTH);
		int hour = c.get(java.util.Calendar.HOUR);
		int min = c.get(java.util.Calendar.MINUTE);
		
		float _y = DAYS_LABEL_Y + HOUR_WIDTH * hour + HOUR_WIDTH * min / 60f;
		float _x = HOURS_LINE_WIDTH + (day - 1) * DAY_WIDTH;
		
		float cam_x = Math.min(_x, CAMERA_MAX_X);
		float cam_y = Math.min(_y, CAMERA_MAX_Y);
		cam_y = Math.max(cam_y, CAMERA_HEIGHT/2);
		camera.setCenter(cam_x, cam_y);
		setEntitiesRelativePositions();	

		tasksLines.detachChildren();
		for (Task t : tasks) {
			Log.d("TASK", t.toString());

			c.setTime(t.duedate);
			day = c.get(java.util.Calendar.DAY_OF_MONTH);
			hour = c.get(java.util.Calendar.HOUR);
			min = c.get(java.util.Calendar.MINUTE);

			Log.d("TSK", String.format("%d:%d:%d", day, hour, min));

			float x = HOURS_LINE_WIDTH + (day - 1) * DAY_WIDTH;
			float y = DAYS_LABEL_Y + HOUR_WIDTH * hour + HOUR_WIDTH * min / 60f;
			
			Line l = new Line(_x, _y, x, y);
			l.setColor(1f, 0f, 0f);
			l.setLineWidth(3f);
			tasksLines.attachChild(l);
			
			if(movingTasks.containsKey(t.id)){
				TaskEntity te = movingTasks.get(t.id);
				te.setPosition(x, y);
			}else{
				TaskEntity te = new TaskEntity(t, x, y);
				tasksHolder.attachChild(te, 0);
				movingTasks.put(t.id, te);
			}
			_x = x;
			_y = y;
		}
	}
	
	
	/**
	 * It takes ArrayList of tasks and displays them, the main usage of this function is to load the calendar with current month's tasks.
	 * 
	 *  @param	tasks	ArrayList of tasks to be displayed
	 * */
	private void loadTasks(ArrayList<Task> tasks){
		for (Task t : tasks) {
			java.util.Calendar c = java.util.Calendar.getInstance();
			c.setTime(t.duedate);
			int day = c.get(java.util.Calendar.DAY_OF_MONTH);
			int hour = c.get(java.util.Calendar.HOUR);
			int min = c.get(java.util.Calendar.MINUTE);

			// calculate task entity positions
			float x = HOURS_LINE_WIDTH + (day - 1) * DAY_WIDTH;
			float y = DAYS_LABEL_Y + HOUR_WIDTH * hour + HOUR_WIDTH * min / 60f;
			
			tasksHolder.attachChild(new TaskEntity(t, x, y), 0);
		}
	}
	
	private ArrayList<Task> readTasksOfMonth(int month){
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.set(java.util.Calendar.MONTH, month);
		int year = c.get(java.util.Calendar.YEAR);

		c.set(year, month, 0, 0, 0);
		Date startDate = c.getTime();
		
		c.set(year, month, c.getMaximum(java.util.Calendar.DAY_OF_MONTH), 0, 0);
		Date endDate = c.getTime();
		
		TasksIterator ti = Task.getScheduledTasks(startDate, endDate);
		ArrayList<Task> tasks = new ArrayList<Task>();
		Task t = null;
		while ((t = ti.nextTask()) != null)
			tasks.add(t);
		
		return tasks;
	}
}
