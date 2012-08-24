package com.google.cachesync;

import java.io.PrintStream;

import com.code4bones.utils.NetLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class BootReceiver extends BroadcastReceiver {

	public final NetLog gLog = NetLog.getInstance("CacheSync","CacheSync.log.txt", true); 

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
    	NetLog.v("Devices booted\r\n");
		Intent service = new Intent(context,CacheSyncService.class);
    	context.startService(service);
	}

}
