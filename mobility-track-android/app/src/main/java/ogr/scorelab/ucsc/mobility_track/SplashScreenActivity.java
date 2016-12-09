package ogr.scorelab.ucsc.mobility_track;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Jeff on 7/12/2016.
 */
public class SplashScreenActivity extends AppCompatActivity {
	private static long ANIMATION_TIME = 450;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		if (Build.VERSION.SDK_INT >= 16) {
			View logo = findViewById(R.id.score_logo);
			logo.setAlpha(0f);
			logo.setScaleX(0.5f);
			logo.setScaleY(0.5f);
			logo.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMATION_TIME).start();
			View resizeView = findViewById(R.id.splash_text_layout);
			resizeView.setAlpha(0f);
			resizeView.setTranslationY(-50f);
			resizeView.animate().setDuration(ANIMATION_TIME).alpha(1).translationY(0).setStartDelay(ANIMATION_TIME /2).start();
		}
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
				finish();
			}
		}, 1000);
	}
}
