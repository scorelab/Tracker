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

    private DBAccess dbAccess;
    private DefaultHttpClient mHttpClient;
    private HttpPost mHttpPost;
    private String deviceId;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        dbAccess = new DBAccess(this);
        dbAccess.open();

        locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.UPDATE_FREQUENCY, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.UPDATE_FREQUENCY, 0, locationListener);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        deviceId = intent.getStringExtra("deviceId");
        foregroundStuff();
        initConnection();
        new Thread(new DataTransferHandle()).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        mHttpClient = new DefaultHttpClient();
        mHttpPost = new HttpPost(Constants.SERVER + Constants.DATA_POST_URL);
    }

    public boolean packAndPost(Location2 location2)
            throws Exception {

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

        StringEntity se = new StringEntity(holder.toString());
        mHttpPost.setEntity(se);
        mHttpPost.setHeader("Accept", "application/json");
        mHttpPost.setHeader("Content-type", "application/json");

        ResponseHandler responseHandler = new BasicResponseHandler();
        try {
            mHttpClient.execute(mHttpPost, responseHandler);
        } catch (Exception e) {
            return false;
        }
        return true;
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
