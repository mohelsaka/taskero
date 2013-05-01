package com.egle;

import java.util.Calendar;
import java.util.Date;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class DayLine {

	private Path path;
	private Path textPath;
	private Path horizontalPath;

	public DayLine(int startXpos) {
		getHourPath(startXpos - 20);
	}

	private void getHourPath(int x) {
		path = new Path();
		textPath = new Path();
		horizontalPath = new Path();
		path.moveTo(x, 0);
		textPath.moveTo(x, 0);
		horizontalPath.moveTo(x, 0);
		int shift = a.ENTRY_HEIGHT / 2;
		float halfGap = a.HOUR_GAP / 2;
		for (int i = 0; i < a.HOURS_PER_DAY; i++) {
			// One Hour Mark.
			path.lineTo(x, (i * a.HOUR_GAP) - a.HOUR_PEEK_WIDTH + shift);
			path.lineTo(x + a.HOUR_PEEK_HIEGHT, (i * a.HOUR_GAP) + shift);
			path.lineTo(x, (i * a.HOUR_GAP) + a.HOUR_PEEK_WIDTH + shift);
			// Half Hour Mark.
			if (i < a.HOURS_PER_DAY - 1) {
				path.lineTo(x, (i * a.HOUR_GAP) + halfGap
						- a.HALF_HOUR_PEEK_WIDTH + shift);
				path.lineTo(x + a.HALF_HOUR_PEEK_HIEGHT, (i * a.HOUR_GAP)
						+ halfGap + shift);
				path.lineTo(x, (i * a.HOUR_GAP) + halfGap
						+ a.HALF_HOUR_PEEK_WIDTH + shift);
			}
		}
		path.lineTo(x, a.imageHeight);
		textPath.lineTo(x, a.imageHeight);
		horizontalPath.lineTo(a.screenWidth, 0);
	}

	public void DrawDayLine(Canvas canvas, Paint linePainter, Date current) {
		canvas.drawRect(0, 0, a.xMargin - 20, a.imageHeight, a.newEntryPainter);
		canvas.drawPath(path, linePainter);
		canvas.drawPath(horizontalPath, linePainter);
		Calendar c = Calendar.getInstance();
		c.setTime(current);
		canvas.drawTextOnPath(
				+c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.MONTH)
						+ "/" + c.get(Calendar.YEAR), horizontalPath, 100, 20,
				a.lineTextPainter);
		float shift = a.ENTRY_HEIGHT / 2;
		canvas.drawTextOnPath("" + 1 + ":am", textPath, 0, 15,
				a.lineTextPainter);
		for (int i = 2; i <= 12; i++) {
			canvas.drawTextOnPath("" + i + ":am", textPath, (i - 1)
					* a.HOUR_GAP - shift, 15, a.lineTextPainter);
		}
		for (int i = 1; i <= 12; i++) {
			canvas.drawTextOnPath("" + i + ":pm", textPath, (12 + i - 1)
					* a.HOUR_GAP - shift, 15, a.lineTextPainter);
		}
	}
}
