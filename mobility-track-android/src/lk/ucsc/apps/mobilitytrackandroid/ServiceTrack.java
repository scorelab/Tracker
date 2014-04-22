package lk.ucsc.apps.mobilitytrackandroid;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ServiceTrack extends Service{
	private static ScheduledExecutorService scheduleTaskExecutor;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
System.out.println("started");
		scheduleTaskExecutor = Executors.newScheduledThreadPool(2);
		Runs runner=new Runs(this);
	    scheduleTaskExecutor.scheduleAtFixedRate(runner, 0, 10, TimeUnit.SECONDS);
	}


}
