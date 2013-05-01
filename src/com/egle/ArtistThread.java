package com.egle;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class ArtistThread extends Thread {
	private boolean running, alive;
	private SurfaceHolder surfaceHolder;
	private Artist artist;

	public ArtistThread(SurfaceHolder surfaceHolder, Artist artist) {
		super();
		this.surfaceHolder = surfaceHolder;
		this.artist = artist;
		this.running = false;
		this.alive = false;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public boolean is_Running() {
		return running;
	}

	public boolean is_Alive() {
		return alive;
	}

	@Override
	public void run() {
		Canvas canvas;
		while (alive) {
			while (running) {
				canvas = null;
				try {
					canvas = surfaceHolder.lockCanvas();
					synchronized (surfaceHolder) {
						artist.update();
						artist.render(canvas);
					}
				} finally {
					if (canvas != null) {
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
	}
}
