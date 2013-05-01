package GA;

import java.util.Date;

public class TimeSlot {
	
	private long duration;
	private Date start;
	private Date end;
	private float focusRate;

	public TimeSlot(Date s,Date e, long d,float f) {
		// TODO Auto-generated constructor stub
		duration=d;
		start=s;
		end=e;
		focusRate=f;
	}
	public long getDuration() {
		return duration;
	}
	public Date getEnd() {
		return end;
	}
	public Date getStart() {
		return start;
	}
	
	public float getFocusRate() {
		return focusRate;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public void setFocusRate(float focusRate) {
		this.focusRate = focusRate;
	}
}
