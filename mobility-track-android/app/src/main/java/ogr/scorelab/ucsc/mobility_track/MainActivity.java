package ogr.scorelab.ucsc.mobility_track;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

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

    private MapFragment map;

    private ImageView toggleTrackerIcon, refreshIdIcon;
    private AnimatedVectorDrawable startDrawable, stopDrawable, refreshIdDrawable;

    private AnimatorSet connectionInfoAnimator;
    private ObjectAnimator connectionInfoFade;
    private TextView connectionInfo;

    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        getDeviceId();
    }

    @SuppressLint("NewApi")
    private void init() {
        versionIs21 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);

        resources = getResources();

        txtMac = (TextView) findViewById(R.id.txtMac);
        txtDeviceId = (TextView) findViewById(R.id.device_id);
        toggleTrackerIcon = ((ImageView) findViewById(R.id.toggle_icon));
       // refreshIdIcon = ((ImageView) findViewById(R.id.refreshId_icon));
        connectionInfo = ((TextView) findViewById(R.id.connection_info));

        //Animators are separated so listener can be set directly to fade animator. Setting listener on set never calls onRepeat
        connectionInfoAnimator = new AnimatorSet();
        connectionInfoFade = ((ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.connection_info_fade));
        connectionInfoAnimator.playTogether(AnimatorInflater.loadAnimator(this, R.animator.connection_info_slide), connectionInfoFade);
        connectionInfoAnimator.setTarget(connectionInfo);

        findViewById(R.id.tracker_toggle).setOnClickListener(this);

     //   refreshIdIcon.setOnClickListener(this);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        map.getMapAsync(this);

        //Drawable animation is only played if API >= 21
        if (versionIs21) {
            startDrawable = ((AnimatedVectorDrawable) getDrawable(R.drawable.toggle_start_icon_animation));
            stopDrawable = ((AnimatedVectorDrawable) getDrawable(R.drawable.toggle_stop_icon_animation));
            refreshIdDrawable = ((AnimatedVectorDrawable) getDrawable(R.drawable.refresh_id_animation));
        }

        getDeviceId();

    }

    @SuppressLint("NewApi")
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
        //Animate
//        if (versionIs21) {
//            refreshIdIcon.setImageDrawable(refreshIdDrawable);
//            refreshIdDrawable.start();
//        }
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

        return super.onOptionsItemSelected(item);
    }

    /* start background service */
    @SuppressLint("NewApi")
    public void start() {
        if (deviceId == null) {
            animateConnectionInfo(R.string.device_unregistered, true);
            return;
        }
        // Save device id in Shared Preferences.
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_device_id), deviceId);
        editor.apply();

        //Animate UI
        if (versionIs21) {
            toggleTrackerIcon.setImageDrawable(startDrawable);
            startDrawable.start();
        } else {
            toggleTrackerIcon.setImageResource(R.drawable.ic_pause);
        }
        animateConnectionInfo(R.string.connected_txt, false);

        Intent intent = new Intent(this, LocationUpdates.class);
        DataTransferHandler.isThisActive = true;
        startService(intent);
    }

    /* stop background service */
    @SuppressLint("NewApi")
    public void stop() {
        Intent intent = new Intent(this, LocationUpdates.class);
        stopService(intent);
        DataTransferHandler.isThisActive = false;

        //Animate UI
        if (versionIs21) {
            toggleTrackerIcon.setImageDrawable(stopDrawable);
            stopDrawable.start();
        } else {
            toggleTrackerIcon.setImageResource(R.drawable.ic_play);
        }
        animateConnectionInfo(R.string.not_connected_txt, false);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tracker_toggle:
                toggleTracker();
                break;
//            case R.id.refreshId_icon:
//                getDeviceId();
//                break;

        }
    }

    private void toggleTracker() {
        if (!DataTransferHandler.isThisActive) {
            start();
        } else {
            stop();
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    private void animateConnectionInfo(final int stringID, final boolean isError) {
        connectionInfoFade.removeAllListeners();
        connectionInfoFade.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                connectionInfo.setText(stringID);
                if (isError) {
                    connectionInfo.setTextColor(resources.getColor(R.color.text_red));
                } else {
                    connectionInfo.setTextColor(resources.getColor(R.color.text_white));
                }
            }
        });
        connectionInfoAnimator.start();
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
}
