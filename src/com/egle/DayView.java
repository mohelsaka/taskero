package com.egle;

import java.util.Date;
import java.util.LinkedList;

import android.graphics.Bitmap;

public class DayView {

	public Bitmap bitmap;
	public LinkedList<Entry> entries;
	public Date date;
	
	public DayView(Date date){
		this.date = date;
	}
}
