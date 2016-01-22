package ogr.scorelab.ucsc.mobility_track;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationUpdates extends Service {

    public LocationManager locationManager;
    public MyLocationListener locationListener;

    private DBAccess dbAccess;
    private HttpURLConnection httpConnection;
    private String deviceId;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        dbAccess = new DBAccess(this);
        dbAccess.open();

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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.UPDATE_FREQUENCY, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.UPDATE_FREQUENCY, 0, locationListener);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        deviceId = intent.getStringExtra("deviceId");
        foregroundStuff();
        new Thread(new DataTransferHandle()).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
    
    private void initConnection () {
        try {
            URL url = new URL("http",Constants.SERVER,3000,Constants.DATA_POST_URL);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("Accept", "application/json");
            httpConnection.setRequestProperty("Content-type", "application/json");
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);

            httpConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean packAndPost(Location2 location2)
            throws Exception {
        boolean ret = true; // Return value

        // Make connection to the server here.
        initConnection();

        JSONObject holder = new JSONObject();

        String key = "id";
        String data = deviceId;
        holder.put(key, data);

        key = "status";
        holder.put(key, 1);

        holder.put("timestamp", location2.timestamp);

        JSONArray dataArray = new JSONArray();
        JSONObject dataObj = new JSONObject();
        dataObj.put("latitude", location2.latitude);
        dataObj.put("longitude", location2.longitude);
        dataObj.put("direction", location2.direction);
        dataObj.put("speed", location2.speed);
        dataObj.put("timestamp", location2.timestamp);
        dataArray.put(dataObj);
        holder.put("data", dataArray);

        try {
            DataOutputStream out = new DataOutputStream(httpConnection.getOutputStream());
            out.write(holder.toString().getBytes("UTF-8"));
            out.flush();
            out.close();
        } catch (Exception e) {
            // Data send failed
            ret = false;
        } finally {
            httpConnection.disconnect();
        }
        return ret;
    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(final Location location) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    dbAccess.push(location);
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

    private class DataTransferHandle implements Runnable {

        @Override
        public void run() {
            Location2 l2;
            while (true) {
                l2 = dbAccess.get();
                if (l2 == null) {   // if db is empty
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    continue;
                }
                try {
                    if (packAndPost(l2)) {  // if data posted to the server successfully
                        dbAccess.delete(l2.timestamp);
                    } else {
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
