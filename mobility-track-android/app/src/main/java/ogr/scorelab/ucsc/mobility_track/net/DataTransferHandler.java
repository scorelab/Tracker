package ogr.scorelab.ucsc.mobility_track.net;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import ogr.scorelab.ucsc.mobility_track.Constants;
import ogr.scorelab.ucsc.mobility_track.DBAccess;
import ogr.scorelab.ucsc.mobility_track.Location2;

/**
 * Created by dinush on 1/24/16.
 */
public class DataTransferHandler
{

    // Is this service active or not. Used to control the data transfer loop.
    public static boolean isThisActive = true;

    private int inCacheCount;   // Number of items in the database

    private DBAccess dbAccess;
    private HttpURLConnection httpConnection;
    private String deviceId;

    public DataTransferHandler(Context context, String deviceId)
    {
        dbAccess = new DBAccess(context);
        dbAccess.open();
        this.deviceId = deviceId;

        inCacheCount = dbAccess.getCount();

        uploadCachedDataToServer();
    }

    public synchronized void uploadCachedDataToServer()
    {
        if (inCacheCount == 0)
            return;                 // Cache is empty

        // Upload in separate thread
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (inCacheCount > 0)
                {
                    try {
                        Location2 location2 = dbAccess.get();
                        JSONObject jsonDataPacket = getJsonObject(location2);

                        if (sendJsonToServer(jsonDataPacket))
                        {
                            dbAccess.delete(location2.timestamp);
                            inCacheCount--;
                        }
                        else
                        {
                            return;         // Will retry later
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private boolean initConnection ()
    {
        try
        {
            URL url = new URL("http", Constants.SERVER,3000,Constants.DATA_POST_URL);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("Accept", "application/json");
            httpConnection.setRequestProperty("Content-type", "application/json");
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);

            httpConnection.connect();
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public JSONObject getJsonObject(Location location) throws JSONException {
        Location2 location2 = new Location2();

        location2.direction = location.getBearing();
        location2.latitude = location.getLatitude();
        location2.longitude = location.getLongitude();
        location2.speed = location.getSpeed();
        location2.timestamp = System.currentTimeMillis();

        return getJsonObject(location2);
    }

    private JSONObject getJsonObject(Location2 location2) throws JSONException
    {
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

        return holder;
    }

    public synchronized boolean sendJsonToServer (JSONObject dataHolder)
    {
        boolean ret = true;     // Return value

        if (!initConnection())
            return false;

        try
        {
            DataOutputStream out = new DataOutputStream(httpConnection.getOutputStream());
            out.write(dataHolder.toString().getBytes("UTF-8"));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            // Data send failed
            ret = false;
        }
        finally
        {
            try
            {
                Log.d("TRACKER", httpConnection.getResponseMessage());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            httpConnection.disconnect();
        }
        return ret;
    }

    public synchronized void pushToDatabase(Location location)
    {
        dbAccess.push(location);

        inCacheCount++;
    }
}
