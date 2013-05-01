package com.egle;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;

public class a {

	public static final short DAYS_OF_WEEK = 7;
	public static final short HOURS_PER_DAY = 24;
	public static final short MINUTES_PER_HOUR = 60;
	public static final short HALF_HOUR = 30 * MINUTES_PER_HOUR;
	public static final short QUARTER_HOUR = 15 * MINUTES_PER_HOUR;

	public static final short NUMBER_OF_TASKS_PAGE = 5;
	public static short VERTICAL_SCROLL_THRESHOLD;
	public static short HORIZONTAL_SCROLL_THRESHOLD;

	public static short TOP = 25;

	public static final short TOP_PAD_OF_TASK = 10;
	public static final short LEFT_PAD_OF_TASK = 5;

	public static short TOP_SCREEN_OFFSET;

	public static int screenWidth, screenHeight;
	public static int imageWidth, imageHeight;
	public static short yMargin, xMargin;

	public static float HOUR_GAP;
	public static float MINUTE_GAP;
	public static int ENTRY_HEIGHT;
	public static int STROKE_WIDTH;

	public static int HOUR_PEEK_WIDTH;
	public static int HOUR_PEEK_HIEGHT;

	public static int HALF_HOUR_PEEK_WIDTH;
	public static int HALF_HOUR_PEEK_HIEGHT;

	public static void Intialize(int screenWidth, int screenHeight) {
		a.screenWidth = screenWidth;
		a.screenHeight = screenHeight;

		imageWidth = screenWidth;
		imageHeight = screenHeight
				* ((HOURS_PER_DAY + 1) / NUMBER_OF_TASKS_PAGE);

		xMargin = (short) (screenWidth / 6);
		yMargin = (short) (screenHeight / 10);

		TOP_SCREEN_OFFSET = (short) (screenHeight / 10);
		TOP_SCREEN_OFFSET = 0;

		HOUR_GAP = (imageHeight / a.HOURS_PER_DAY);
		MINUTE_GAP = (HOUR_GAP / a.MINUTES_PER_HOUR);
		ENTRY_HEIGHT = (int) (HOUR_GAP / 4);
		STROKE_WIDTH = a.screenWidth / 50;

		HOUR_PEEK_WIDTH = ENTRY_HEIGHT / 4;
		HOUR_PEEK_HIEGHT = ENTRY_HEIGHT / 2;

		HALF_HOUR_PEEK_WIDTH = HOUR_PEEK_WIDTH / 2;
		HALF_HOUR_PEEK_HIEGHT = HOUR_PEEK_HIEGHT / 2;

		HORIZONTAL_SCROLL_THRESHOLD = (short) (screenWidth / 7);
		VERTICAL_SCROLL_THRESHOLD = (short) (screenHeight / 20);

		setFontMatrix();
		setPainters();
	}

	private static void setPainters() {
		setEntryPainter();
		setNewEntryPainter();
		setTextPainter();
		setLinePainter();
		setLineTextPainter();
	}

	/**************************** Painters ******************************/

	public static Paint entryPainter;
	public static Paint textPainter;
	public static Paint newEntryPainter;
	public static Paint linePainter;
	public static Paint lineTextPainter;

	public static FontMetrics fontMatrix;

	/************************* Initialize Painters **********************/
	private static void setEntryPainter() {
		// Entry Painter.
		entryPainter = new Paint();
		entryPainter.setAntiAlias(true);
		entryPainter.setShadowLayer(3.0f, 2f, 0f,
				Color.argb(150, 255, 255, 255));
		entryPainter.setARGB(100, 120, 120, 120);
	}

	private static void setNewEntryPainter() {
		// Entry Painter.
		newEntryPainter = new Paint();
		newEntryPainter.setAntiAlias(true);
		newEntryPainter.setARGB(150, 200, 10, 10);
	}

	private static void setTextPainter() {
		// Text Painter.
		textPainter = new Paint();
		textPainter.setAntiAlias(true);
		textPainter.setShadowLayer(4.0f, 2f, 2f, Color.argb(150, 10, 10, 10));
		textPainter.setARGB(200, 10, 10, 10);
		textPainter.setTextSize(ENTRY_HEIGHT / 2);
		textPainter.setFakeBoldText(true);
		textPainter.setTypeface(Typeface.MONOSPACE);
		// Font Matrix Initialization.
		textPainter.setTextAlign(Paint.Align.CENTER);
		textPainter.getFontMetrics(fontMatrix);
	}

	private static void setLinePainter() {
		// Line Painter.
		linePainter = new Paint();
		linePainter.setAntiAlias(true);
		linePainter.setStyle(Paint.Style.STROKE);
		linePainter.setStrokeCap(Paint.Cap.SQUARE);
		linePainter.setStrokeJoin(Paint.Join.ROUND);

		linePainter.setStrokeWidth(STROKE_WIDTH);
		// linePainter.setShadowLayer(xMargin, -(xMargin + STROKE_WIDTH / 2)
		// + STROKE_WIDTH, 0f, Color.argb(255, 255, 255, 255));
		linePainter.setARGB(200, 0, 0, 0);
	}

	private static void setFontMatrix() {
		fontMatrix = new FontMetrics();
	}

	private static void setLineTextPainter() {
		lineTextPainter = new Paint();
		lineTextPainter.setAntiAlias(true);
		lineTextPainter.setShadowLayer(2.0f, 0f, 0f,
				Color.argb(200, 120, 120, 120));
		lineTextPainter.setARGB(200, 10, 10, 10);
		lineTextPainter.setTextSize(15);
		lineTextPainter.setTypeface(Typeface.MONOSPACE);
		lineTextPainter.setTextAlign(Paint.Align.LEFT);
	}

}
