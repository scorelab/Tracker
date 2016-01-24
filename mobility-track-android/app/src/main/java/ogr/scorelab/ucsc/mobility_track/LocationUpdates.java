package ogr.scorelab.ucsc.mobility_track;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ogr.scorelab.ucsc.mobility_track.net.DataTransferHandler;

public class LocationUpdates extends Service {

    private LocationManager locationManager;
    private MyLocationListener locationListener;

    private DataTransferHandler dataHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.UPDATE_FREQUENCY, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.UPDATE_FREQUENCY, 0, locationListener);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
Log.i("TRACKER", "Service on start.");
        // Get device id from Shared Preferences.
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        String defaultValue = getString(R.string.saved_device_id_default);
        String deviceId = sharedPref.getString(getString(R.string.saved_device_id), defaultValue);

        foregroundStuff();

        dataHandler = new DataTransferHandler(this, deviceId);
        new Thread(dataHandler).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("TRACKER", "Service on destroy.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // notification
    protected void foregroundStuff() {
        Notification notification = new Notification();
        startForeground(1, notification);
    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(final Location location) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        JSONObject jsonDataPacket = dataHandler.getJsonObject(location);

                        if (!dataHandler.sendJsonToServer(jsonDataPacket))
                        {
                            dataHandler.pushToDatabase(location);
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}
