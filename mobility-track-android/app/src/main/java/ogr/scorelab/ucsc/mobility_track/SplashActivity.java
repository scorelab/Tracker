package ogr.scorelab.ucsc.mobility_track;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Orko on 11/30/16.
 */

public class SplashActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread closeActivity = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.getLocalizedMessage();
                }
            }
        });

        Intent intent = new Intent(this, MainActivity.class);
        closeActivity.run();
        startActivity(intent);
        finish();
    }

}
