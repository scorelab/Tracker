package ogr.scorelab.ucsc.mobility_track;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class LocationUpdates extends Service {

    public LocationManager locationManager;
    public MyLocationListener locationListener;

    private DefaultHttpClient mHttpClient;
    private HttpPost mHttpPost;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        foregroundStuff();
        initConnection();
        Log.d("tracker", "service started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        Log.d("tracker", "destroying service");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    protected void foregroundStuff() {
        Notification notification = new Notification();
        startForeground(1, notification);
    }
    
    private void initConnection () {
        mHttpClient = new DefaultHttpClient();
        mHttpPost = new HttpPost(Constants.SERVER_PATH);
    }

    public String packAndPost(Location location)
            throws Exception {

        JSONObject holder = new JSONObject();

        String key = "id";
        String data = "TRK456789";
        holder.put(key, data);

        key = "status";
        holder.put(key, 1);

        holder.put("timestamp", Long.valueOf("1422955082989"));

        JSONArray dataArray = new JSONArray();
        JSONObject dataObj = new JSONObject();
        dataObj.put("latitude", location.getLatitude());
        dataObj.put("longitude", location.getLongitude());
        dataObj.put("direction", location.getBearing());
        dataObj.put("speed", location.getSpeed());
        dataObj.put("timestamp", System.currentTimeMillis());
        dataArray.put(dataObj);
        holder.put("data", dataArray);


        StringEntity se = new StringEntity(holder.toString());
        mHttpPost.setEntity(se);
        mHttpPost.setHeader("Accept", "application/json");
        mHttpPost.setHeader("Content-type", "application/json");

        ResponseHandler responseHandler = new BasicResponseHandler();
        String response = (String) mHttpClient.execute(mHttpPost, responseHandler);

        return response;
    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(final Location location) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String res = null;
                    try {
                        res = packAndPost(location);
                        Log.d("tracker", "location changed " + res);
                    } catch (Exception e) {
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
