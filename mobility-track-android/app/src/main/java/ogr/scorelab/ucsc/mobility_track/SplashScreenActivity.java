package ogr.scorelab.ucsc.mobility_track;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Jeff on 7/12/2016.
 */
public class SplashScreenActivity extends AppCompatActivity {
	private static long DELAY_TIME = 1000;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
				finish();
			}
		}, DELAY_TIME);
	}
}
