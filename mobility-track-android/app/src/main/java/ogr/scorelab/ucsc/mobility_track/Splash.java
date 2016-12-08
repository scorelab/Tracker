package ogr.scorelab.ucsc.mobility_track;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Daniel on 2016-12-07.
 */

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //remove actionbar to make it look better
        try {
            getSupportActionBar().hide();
        }catch(Exception i){}

        //display something on screen
        setContentView(R.layout.activity_splash);

        //wait 2 seconds
        new CountDownTimer(2*1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {}//not used, but mandatory

            @Override
            public void onFinish() {
                //go onto the next activity
                startActivity(new Intent(Splash.this,MainActivity.class));
                //close this activity
                finish();
            }
        }.start();
    }
}
