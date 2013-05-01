package com.egle;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import sak.todo.database.DBHelper;
import sak.todo.database.Task;
import sak.todo.gui.CreateTaskActivity;
import sak.todo.gui.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class Artist extends SurfaceView implements SurfaceHolder.Callback {

	private Context context;
	private ArtistThread thread;

	private Rect bounds;
	private Calendar cal = Calendar.getInstance();
	private Iterator<Entry> itr;

	// private Bitmap previousBitmap;
	// private Bitmap currentBitmap;
	// private Bitmap nextBitmap;
	private Bitmap background;

	private DayView pre;
	private DayView ne;
	private DayView cur;

	public DayLine dayline;
	int topLeftCornerX, topLeftCornerY;
	int dx, dy;

	RectF displayRect;
	Rect scrollRect;

	RectF bgdisplayRect;
	Rect bgscrollRect;

	public Date CurrentDay;
	public int CurrentHour;
	// Entry[][] entries;

	// private LinkedList<Entry> previous;
	// private LinkedList<Entry> current;
	// private LinkedList<Entry> next;
	// private LinkedList<Entry> backLog;

	float startX, startY;
	float scrollByX, scrollByY;

	private int scrollRectX = 0;
	private int scrollRectY = 0;

	public Artist(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.CurrentDay = Calendar.getInstance().getTime();
		cal.setTime(CurrentDay);
		this.cur = new DayView(CurrentDay);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		this.pre = new DayView(cal.getTime());
		cal.add(Calendar.DAY_OF_MONTH, 2);
		this.ne = new DayView(cal.getTime());
		this.CurrentHour = 0;
		this.background = BitmapFactory.decodeResource(getResources(),
				R.drawable.day);
		this.bounds = new Rect();
		this.cal = Calendar.getInstance();
		// Canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		// this.backLog = new LinkedList<Entry>();
		/**
		 * Go Fetch the current week 0-0 , 1-1 , 2-2 , 3-3 and so on from the
		 * database.
		 */

		System.out.println(a.screenHeight + "X" + a.screenWidth);

		this.dayline = new DayLine(a.xMargin + 0);

		this.topLeftCornerY = 0;
		this.topLeftCornerX = 0;

		this.startX = 0;
		this.startY = 0;
		this.scrollByX = 0;
		this.scrollByY = 0;

		this.displayRect = new RectF(0, a.TOP_SCREEN_OFFSET, a.screenWidth,
				a.screenHeight + a.TOP_SCREEN_OFFSET);
		this.scrollRect = new Rect(0, 0, a.screenWidth, a.screenHeight);

		this.bgdisplayRect = new RectF(0, 0, a.screenWidth, a.screenHeight);
		this.bgscrollRect = new Rect(0, 0, a.screenWidth, a.screenHeight);

		getHolder().addCallback(this);
		thread = new ArtistThread(getHolder(), this);
		setFocusable(true);
	}

	public void exit() {
		System.out.println("Exiting . . . .");
		thread.setRunning(false);
		thread.setAlive(false);
	}

	public void ProcessTouchEvent(float x, float y) {
		// Click Position relative to the canvas.
		float dx = x + topLeftCornerX;
		float dy = y + topLeftCornerY - a.TOP_SCREEN_OFFSET - a.TOP;
		// Check off-screen Clicks.
		if (x < a.screenWidth && y > a.TOP_SCREEN_OFFSET && y < a.screenHeight) {
			if (x > a.xMargin) {
				itr = cur.entries.iterator();
				Entry e;
				int pos = 0;
				while (itr.hasNext()) {
					e = itr.next();
					if (e.Contains(dx, dy)) {
						getDialog(e.taskTag, e.taskId, pos).show();
					}
					pos++;
				}
			} else {
				Intent i = new Intent(context, CreateTaskActivity.class);

				Calendar c = Calendar.getInstance();
				int hour = (int) (dy / a.HOUR_GAP);
				int minute = (int) ((dy % a.HOUR_GAP) / a.MINUTE_GAP);
				Date choosen = new Date(c.get(Calendar.YEAR),
						c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
						hour, minute);
				Toast.makeText(context,
						choosen.getHours() + " : " + choosen.getMinutes(),
						Toast.LENGTH_SHORT).show();
				i.putExtra("Date", choosen.getTime());
				context.startActivity(i);

			}
		} else if (y > 0 && y < a.TOP_SCREEN_OFFSET) {
			// Intent i = new Intent(context, ShowTasksActivity.class);
			// i.putExtra("Schedulled", false);
			// context.startActivity(i);
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (thread.is_Running()) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				startX = event.getRawX();
				startY = event.getRawY();
				scrollByX = 0;
				scrollByY = 0;
				ProcessTouchEvent(startX, startY);
			}
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				float xRaw = event.getRawX();
				float yRaw = event.getRawY();
				scrollByX = xRaw - startX;
				scrollByY = yRaw - startY;
				startX = xRaw;
				startY = yRaw;
			}
		}
		return true;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	public void surfaceCreated(SurfaceHolder holder) {
		// Starting the Worker thread.
		// Dummy();
		// Dummy2();
		pre.entries = getEntriesOfDay(pre.date);
		cur.entries = getEntriesOfDay(cur.date);
		ne.entries = getEntriesOfDay(ne.date);

		pre.bitmap = drawDay(pre.entries, pre.date);
		cur.bitmap = drawDay(cur.entries, cur.date);
		ne.bitmap = drawDay(ne.entries, ne.date);
		thread.setAlive(true);
		thread.setRunning(true);
		thread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		thread.setRunning(false);
		thread.setAlive(false);
	}

	public void update() {

	}

	public void render(Canvas canvas) {
		if (canvas == null)
			return;
		// canvas.drawRGB(255, 255, 255);
		// Do the rest of rendering.
		int ScrollRectX = (scrollRectX + (int) scrollByX);
		int newScrollRectX = 0;
		int newScrollRectY = (scrollRectY - (int) scrollByY);
		if (ScrollRectX > a.HORIZONTAL_SCROLL_THRESHOLD) {
			ScrollRectX = 0;
			scrollByX = 0;
			scrollByY = 0;
			// previousBitmap = currentBitmap;
			// currentBitmap = nextBitmap;
			// nextBitmap = previousBitmap;

			pre.bitmap = cur.bitmap;
			cur.bitmap = ne.bitmap;
			ne.bitmap = pre.bitmap;
			System.out.println("Swap X-Axis to another day: " + ScrollRectX);
		} else if (-ScrollRectX > a.HORIZONTAL_SCROLL_THRESHOLD) {
			ScrollRectX = 0;
			scrollByX = 0;
			scrollByY = 0;
			// previousBitmap = currentBitmap;
			// currentBitmap = nextBitmap;
			// nextBitmap = previousBitmap;

			pre.bitmap = cur.bitmap;
			cur.bitmap = ne.bitmap;
			ne.bitmap = pre.bitmap;
			System.out.println("Swap X-Axis to another day: " + ScrollRectX);
		}
		// Don't scroll off the top or bottom edges of the bitmap.
		if (newScrollRectY < 0)
			newScrollRectY = 0;
		else if (newScrollRectY > (a.imageHeight - a.screenHeight))
			newScrollRectY = (a.imageHeight - a.screenHeight);

		scrollRect.set(newScrollRectX, newScrollRectY, newScrollRectX
				+ a.screenWidth, newScrollRectY + a.screenHeight);
		// mCamera.save();
		// mCamera.rotateX(2);
		// mCamera.rotateZ(2);
		// mCamera.applyToCanvas(canvas);
		DrawBackground(canvas);
		// canvas.drawBitmap(background, 0, 0, null);
		canvas.drawBitmap(cur.bitmap, scrollRect, displayRect, null);
		// mCamera.restore();
		// canvas.drawBitmap(bitmap, newScrollRectY, newScrollRectX, null);
		// canvas.drawBitmap(bitmap, newScrollRectX, newScrollRectY, null);
		// Reset current scroll coordinates to reflect the latest updates,
		// so we can repeat this update process.
		scrollRectX = newScrollRectX;
		scrollRectY = newScrollRectY;
		topLeftCornerX = newScrollRectX;
		topLeftCornerY = newScrollRectY;
	}

	private void DrawBackground(Canvas canvas) {
		int xOffset = topLeftCornerY * background.getWidth() / a.imageHeight;
		if (xOffset < 0)
			xOffset = 0;
		else if (xOffset > background.getWidth() - a.screenWidth)
			xOffset = background.getWidth() - a.screenWidth;
		bgscrollRect.set(xOffset, 0, xOffset + a.imageWidth, a.screenHeight);
		canvas.drawBitmap(background, bgscrollRect, bgdisplayRect, null);
	}

	private LinkedList<Entry> getEntriesOfDay(Date date) {
		LinkedList<Entry> list = new LinkedList<Entry>();
		Task[] tasks = Task.getTasksOfDayFull(date);
		long id;
		String tag;
		Date mdate;
		for (int i = 0; i < tasks.length; i++) {
			id = tasks[i].id;
			tag = tasks[i].body;
			mdate = tasks[i].duedate;
			list.add(new Entry(id,
					tag.substring(0, Math.min(15, tag.length())), mdate
							.getHours(), mdate.getMinutes()));
		}
		return list;
	}

	private Bitmap drawDay(LinkedList<Entry> list, Date current) {
		Bitmap bitmap = Bitmap.createBitmap(a.imageWidth, a.imageHeight,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		dayline.DrawDayLine(canvas, a.linePainter, current);
		itr = list.iterator();
		Entry e;
		while (itr.hasNext()) {
			e = itr.next();
			a.textPainter.getTextBounds(e.taskTag, 0, e.taskTag.length(),
					bounds);
			e.Draw(canvas, a.entryPainter, a.textPainter, a.fontMatrix, bounds);
		}
		return bitmap;
	}

	private void UpdateBitmap(LinkedList<Entry> list, Bitmap bitmap) {
		Canvas canvas = new Canvas(bitmap);
		// canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		// dayline.DrawDayLine(canvas, a.linePainter);
		Paint p = new Paint();
		p.setColor(Color.TRANSPARENT);
		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		canvas.drawRect(a.xMargin - 3, 0, a.imageWidth, a.imageHeight, p);
		itr = list.iterator();
		Entry e;
		while (itr.hasNext()) {
			e = itr.next();
			a.textPainter.getTextBounds(e.taskTag, 0, e.taskTag.length(),
					bounds);
			e.Draw(canvas, a.entryPainter, a.textPainter, a.fontMatrix, bounds);
		}
	}

	private Entry removeTaskFromCanvas(int position) {
		Entry temp = cur.entries.remove(position);
		// UpdateBitmap(current, currentBitmap);
		UpdateBitmap(cur.entries, cur.bitmap);
		return temp;
	}

	private void deleteTask(long id, int position) {
		removeTaskFromCanvas(position);
		Task.deleteById(id);
	}

	public void Dummy2() {
		ne.bitmap = Bitmap.createBitmap(a.imageWidth, a.imageHeight,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(ne.bitmap);
		dayline.DrawDayLine(canvas, a.linePainter, ne.date);
		ne.entries = new LinkedList<Entry>();
		pre.entries = new LinkedList<Entry>();
		String tag;
		for (int i = 0; i < 24; i++) {
			tag = "# Hello Man...Fine :)" + 2 * i;
			ne.entries.add(new Entry(i, tag, (short) i, (short) 0));
		}
		ne.bitmap = drawDay(ne.entries, ne.date);
	}

	/************************** INTIALIZERS ******************************/

	public Dialog getDialog(String title, final long id, final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.time);
		builder.setTitle(title);
		CharSequence[] items = { "Edit Task.", "Re-schedule Task.",
				"Delete Task." };
		builder.setItems(items, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					Intent i = new Intent(context, CreateTaskActivity.class);
					i.putExtra("task_id", id);
					context.startActivity(i);
					break;
				case 1:
					removeTaskFromCanvas(position);
					Task.updateSinglField(id, DBHelper.COLUMN_DUE_DATE_NUM, "0");
					break;
				case 2:
					YesNoDialog(id, position).show();
				default:
					break;
				}
			}
		});
		AlertDialog dialog = builder.create();
		return dialog;
	}

	public Dialog YesNoDialog(final long id, final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.question);
		builder.setTitle("Delete task!");
		builder.setMessage("Are you sure ...?\nThis process can't be reversed..!");
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Delete button clicked
					// removeTaskFromCanvas(position);
					// removeTaskFromBitmap(position);
					deleteTask(id, position);
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// Keep button clicked
					break;
				}
			}
		};
		builder.setPositiveButton("Delete", dialogClickListener);
		builder.setNegativeButton("Keep", dialogClickListener);
		AlertDialog dialog = builder.create();
		return dialog;

	}

	// Less resource consuming and much faster has restriction as it could
	// remove other tasks part of entry.
	private Entry removeTaskFromBitmap(final int position) {
		Paint p = new Paint();
		p.setColor(Color.TRANSPARENT);
		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		Canvas c = new Canvas(cur.bitmap);
		Entry e = cur.entries.get(position);
		c.drawRect(e.container, p);
		return e;
	}

	// Delete function utility that uses the removeTaskFromBitmap().
	@SuppressWarnings("unused")
	private Entry DeleteTask(final long id, final int position) {
		Task.deleteById(id);
		return removeTaskFromBitmap(position);
	}

	public Dialog setNewTaskDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.stat_neutral);
		builder.setTitle("Add new Task");
		CharSequence[] items = { "Got started with it.", "Re-schedule it.",
				"Delete it." };
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					Toast.makeText(context, "Got started with task",
							Toast.LENGTH_SHORT).show();
					break;
				case 1:
					Toast.makeText(context, "Re-shedule it.",
							Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(context, "Delete it.", Toast.LENGTH_SHORT)
							.show();
				default:
					break;
				}
			}
		});
		AlertDialog dialog = builder.create();
		// dialog.getWindow()
		// .addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		return dialog;
	}
}
