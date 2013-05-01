package com.egle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.RectF;

public class Entry {

	public long taskId;
	public String taskTag;
	public RectF container;
	public short hour;
	public short minute;

	public Entry(long taskId, String taskTag, int hour, int minute) {
		this.taskId = taskId;
		this.taskTag = taskTag;
		this.hour = (short) hour;
		this.minute = (short) minute;
	}

	public void Draw(Canvas canvas, Paint entryPaint, Paint textPaint,
			FontMetrics fm, Rect bounds) {
		// Get the Starting TOP position.
		float Y = (hour * a.HOUR_GAP) + (minute * a.MINUTE_GAP);
		System.out.println("Y-OFFSET : " + Y);
		// Initialize the Rectangle containing the text.
		this.container = new RectF(a.xMargin, Y, a.xMargin + bounds.right
				+ (3 * a.LEFT_PAD_OF_TASK), Y + a.ENTRY_HEIGHT);
		/*********** Print the Rectangle info. **********************************/
		System.out.println("BOUNDS: " + bounds.toShortString());
		System.out.println("RECTF: " + container.left + " , " + container.top
				+ " , " + container.right + " , " + container.bottom);
		/*********** ************************* **********************************/
		// Painting.
		canvas.drawRoundRect(container, 5, 5, entryPaint);
		canvas.drawText(taskTag, container.centerX(), container.centerY()
				+ -(fm.ascent + fm.descent) / 2, textPaint);
		// canvas.drawPoint(Artist.xMargin - 10, container.top
		// + Artist.entry_height / 2, Artist.dotPainter);
		/************************************************************************/
	}

	public boolean Contains(float x, float y) {
		return container.contains(x, y);
	}
}
