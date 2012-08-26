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
	public void onDestroy() {
		NetLog.w("SERVICE EXIT...");
	}
	
	@Override
	public void onCreate() 
	{
		gLog.Init("SrvCacheSync","CacheSync.log.txt",true);

		super.onCreate();
		NetLog.v("Service Created");
	    
		BackgroundTask<Boolean,Void> task = new BackgroundTask<Boolean,Void>(this,false) {
	        	public void onComplete(Boolean isInitialized) {
	                
	        		if ( !isInitialized ) {
	                	NetLog.v("Service is not initialzed propely, command interface is not active...");
	                	CacheSyncService.this.stopSelf();
	                	return;
	                } else 
	                	NetLog.v("Service successfuly initialized");
	        			// TODO: Remove that on distribution 
	        			TestPool.TEST(cmdPool);
	        	}
	        	@Override
	        	protected Boolean doInBackground(Void ... arg0) {
	                try {
						cmdPool.Init("Service",CacheSyncService.this);
					} catch (Exception e) {
						NetLog.e("Failed to initialize command pool: %s ",e.getMessage());
						cmdPool.Release();
						return false;
					}
	                return true;
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
