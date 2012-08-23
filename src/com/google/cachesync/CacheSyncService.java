package com.google.cachesync;

import com.code4bones.utils.NetLog;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class CacheSyncService extends Service implements Runnable {

	public void run() {
	}

	@Override
	public void onCreate() 
	{
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent,int flags, int startId)
	{
		super.onStartCommand(intent,flags, startId);
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
