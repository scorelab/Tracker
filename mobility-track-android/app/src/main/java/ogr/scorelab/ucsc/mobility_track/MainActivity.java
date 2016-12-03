package ogr.scorelab.ucsc.mobility_track;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ogr.scorelab.ucsc.mobility_track.net.DataTransferHandler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private TextView txtMac, txtDeviceId;

    private String deviceId = null;

    private boolean versionIs21;
    
    private ImageView playPauseButton;

    private MapFragment map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        getDeviceId();
    }

    private void init(){
        versionIs21 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);

        txtMac = (TextView) findViewById(R.id.txtMac);
        txtDeviceId = (TextView) findViewById(R.id.txtDeviceId);
        playPauseButton = ((ImageView) findViewById(R.id.playPauseButton));

        playPauseButton.setOnClickListener(this);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        map.getMapAsync(this);

        getDeviceId();

    }

    private void getDeviceId() {
        try {
            String deviceMAC = getDeviceMAC();
            if (deviceMAC == null) {
                txtMac.setText("Device don't have mac address or wi-fi is disabled");
            } else {
                txtMac.setText(deviceMAC);
                new GetDeviceConfigs().execute(deviceMAC);
            }
        } catch (IOException e) {
            Log.e("TRACKER", e.getLocalizedMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_refresh_device_id) {
            getDeviceId();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* start background service */
    public void start(View v) {
        if (deviceId == null) {
            Toast.makeText(this, R.string.device_unregistered, Toast.LENGTH_LONG).show();
            return;
        }
        // Save device id in Shared Preferences.
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_device_id), deviceId);
        editor.apply();

        Intent intent = new Intent(this, LocationUpdates.class);
        DataTransferHandler.isThisActive = true;
        startService(intent);
    }

    /* stop background service */
    public void stop(View v) {
        Intent intent = new Intent(this, LocationUpdates.class);
        stopService(intent);
        DataTransferHandler.isThisActive = false;
    }

    private String getDeviceMAC() throws IOException {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getConnectionInfo().getMacAddress() == null) {
            return null;
        }

        String mac = "";
        for (String block : wifiManager.getConnectionInfo().getMacAddress().split(":"))
            mac += block.toUpperCase();
        return mac;
    }

    /*public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }*/

    private class GetDeviceConfigs extends AsyncTask<String, Void, String> {

        private HttpURLConnection httpConnection;

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http", Constants.SERVER, 3000, Constants.GET_DEVICE_ID_URL + params[0]);
                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setDoInput(true);
                return inputStreamToString(httpConnection.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                if (s == null || s.isEmpty()) {
                    txtDeviceId.setText(R.string.device_unregistered);
                    return;
                }
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    if (jsonArray.length() == 0) {
                        txtDeviceId.setText(R.string.device_unregistered);
                        return;
                    }
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    deviceId = jsonObject.getString("_id");
                    txtDeviceId.setText(deviceId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } finally {
                httpConnection.disconnect();
            }
        }

        private String inputStreamToString(InputStream in) throws IOException {
            String ret = "";

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                ret += line;
            bufferedReader.close();
            in.close();

            return ret;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {


        }
    }

    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }
}
