package adu.app.photobeauty.activity;

import java.util.Timer;
import java.util.TimerTask;

import adu.app.photobeauty.untils.Constant;
import adu.app.photobeauty.untils.Utils;
import adu.app.photobeautystar.full.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

public class SplashActivity extends Activity {
	private long splashDelay = 3000;
	private TextView tvVersion;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Utils.setLocale(this);
		setContentView(R.layout.splash);
		Constant.mAlive = true;
		tvVersion = (TextView) findViewById(R.id.tvVersion);
		tvVersion.setText(getString(R.string.lbl_version) + " "
				+ Utils.getRealAppVer(this));
		// // cheat
		// Utils.createAppFolder();
		// Utils.encryptAllPattern(this);
		// if (true)
		// return;
		// // end cheat
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				finish();
				Intent hackbookIntent = new Intent(SplashActivity.this,
						MenuActivity.class);
				startActivity(hackbookIntent);
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, splashDelay);
	}

	public void onBackPressed() {
	}
}
