package lk.ucsc.apps.mobilitytrackandroid;

import android.content.Context;

public class Runs implements Runnable {
	private GPS loc;

	public Runs(Context context) {
		super();
		this.loc = new GPS(context);
	}

	@Override
	public void run() {
		System.out.println(loc.getLatitude());
		System.out.println(loc.getLongitude());

	}

}
