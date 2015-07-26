package ogr.scorelab.ucsc.mobility_track;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            String deviceMAC = getDeviceMAC();
            new GetDeviceConfigs().execute(deviceMAC);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* start background service */
    public void start(View v) {
        Intent intent = new Intent(this, LocationUpdates.class);
        startService(intent);
    }

    /* stop background service */
    public void stop(View v) {
        Intent intent = new Intent(this, LocationUpdates.class);
        stopService(intent);
    }

    private String getDeviceMAC() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Constants.RMNET0_ADDRESS_FILE_PATH));
        String mac = "";
        for (String block : reader.readLine().split(":")) {
            mac += block.toUpperCase();
        }
        reader.close();
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

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            try {
                HttpResponse httpResponse = httpClient.execute(new HttpGet(Constants.SERVER + Constants.GET_DEVICE_ID_URL + params[0]));
                InputStream inputStream = httpResponse.getEntity().getContent();
                return inputStreamToString(inputStream);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        private String inputStreamToString (InputStream in) throws IOException {
            String ret = "";

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                ret += line;
            in.close();

            return ret;
        }
    }
}
