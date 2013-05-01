package sak.todo.gui;

import com.egle.a;

import sak.todo.gui.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class EagleVisionActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		int width = getWindowManager().getDefaultDisplay().getWidth();
		int height = getWindowManager().getDefaultDisplay().getHeight();
		// LinearLayout layout = (LinearLayout) findViewById(R.id.Mainlayout);

		// RelativeLayout layout = new RelativeLayout(this);
		a.Intialize(width, height);
		// layout.addView(artist, width, height);
		setContentView(R.layout.time_line);
		// ScrollView sv = new ScrollView(this);
		// sv.addView(artist);
		// setContentView(R.layout.main);
	}

	// See onSizeChanged to make it oriented.
}