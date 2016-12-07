package ogr.scorelab.ucsc.mobility_track;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView mapsAttrTxt = ((TextView) findViewById(R.id.mapAttributionTxt));
        mapsAttrTxt.setText(GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(this));
    }

}
