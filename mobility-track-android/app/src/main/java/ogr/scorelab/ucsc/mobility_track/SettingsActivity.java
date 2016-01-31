package ogr.scorelab.ucsc.mobility_track;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        showCurrentIp();
    }

    //Returns to the ActivityMain
    public void cancel(View view){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    //Shows current IP
    public void showCurrentIp(){
        TextView currentIpTextView = (TextView) findViewById(R.id.current_ip);
        String currentIp = getString(R.string.current);
        currentIpTextView.setText(currentIp + " " + getIpAddress(getApplication()));

    }

    //Gets new IP and saves it using Shared Preferences
    public void ipChanged(View view){
        EditText ipEditor = (EditText) findViewById(R.id.ip_editor);
        String ipAddress =  ipEditor.getText().toString();
        if (!ipAddress.isEmpty()){
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplication());
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("Ip", ipAddress);
            editor.commit();
            Toast.makeText(this,"Saved Successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        } else{
            Toast.makeText(this,"IP can't be empty", Toast.LENGTH_SHORT).show();
        }
    }

    //Returns the Actual IP Address
    public static String getIpAddress (Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString("Ip", Constants.SERVER);
    }

}

