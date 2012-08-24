package com.code4bones.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.cachesync.R;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class NetLog {

	private PrintStream ps = null; 
	private String sLogFile = null;
	private String TAG = null;
	
	private static NetLog log = null;
	
	public PrintStream getPrintStream() {
		return ps;
	}
	
	public NetLog() {
	}
	
	public static NetLog getInstance(String tag,String sLogFileName,boolean removeIfExists) {
		if ( NetLog.log == null ) {
			NetLog.log = new NetLog();
			NetLog.log.ps = NetLog.log.Init(tag,sLogFileName,removeIfExists);
		}
		return NetLog.log;
	}
	
	public PrintStream Init(String tag,String sLogFileName,boolean removeIfExists) {
		try {
			TAG = tag;
			sLogFile = Environment.getExternalStorageDirectory() + "/" + sLogFileName;
			File file = new File(sLogFile);
			
			if ( ps == null ) {
				ps = new PrintStream(new FileOutputStream(file,removeIfExists == false));
				NetLog.w("********* LOGGER STARTED ********* \n");
			} else
				NetLog.w("********* LOGGER ACQUIRED ********* \n");

		} catch ( Exception e ) {
			Log.v(TAG,"NetLog Error " + e.toString());
			return null;
		}
		return ps;
	}
	
	public static String getTimeStamp(String dateFormat) {
        Date currentTime = new Date();
		SimpleDateFormat df = new SimpleDateFormat(dateFormat == null?"dd.MM HH:mm:ss ":dateFormat);
		return df.format(currentTime);
	}

	public void writeToFile(String severety,String fmt,Object ... args) {
		if ( ps == null ) 
			return;
		
		synchronized (ps) {
			
		ps.printf("%s | %s | ",NetLog.getTimeStamp(null),severety);
		String msg = String.format(fmt, args);
		ps.printf(fmt,args);
		msg = msg.replace("\r", "").replace("\n", ""); 
		//if ( !msg.endsWith("\r\n") )
			ps.printf("\r\n");
		}
	}
	
	public static void v(String fmt,Object ... args) {
		NetLog.log.writeToFile("V",fmt,args);
		Log.v(NetLog.log.TAG,String.format(fmt, args));
	}

	public static void e(String fmt,Object ... args) {
		NetLog.log.writeToFile("E",fmt,args);
		Log.e(NetLog.log.TAG,String.format(fmt, args));
	}

	public static void w(String fmt,Object ... args) {
		NetLog.log.writeToFile("W",fmt,args);
		Log.w(NetLog.log.TAG,String.format(fmt, args));
	}

	public static void i(String fmt,Object ... args) {
		NetLog.log.writeToFile("I",fmt,args);
		Log.i(NetLog.log.TAG,String.format(fmt, args));
	}
	
	
	public static void Dump() {
		File file = new File(NetLog.log.sLogFile);
		try {
			String line = null;
			BufferedReader br = new BufferedReader( new InputStreamReader(new FileInputStream(file)));
			while ((line = br.readLine()) != null ) 
				Log.v(NetLog.log.TAG,line);
			br.close();
		} catch ( Exception e ) {
			Log.v(NetLog.log.TAG,"NetLog.Dump()=>"+e.toString());
		}
	}
	
	public static void MsgBox(Context ctx,String sTitle,String fmt,Object ... args) {
		String msg = String.format(fmt, args);
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(ctx);                      
	    dlgAlert.setTitle(sTitle); 
	    dlgAlert.setMessage(msg); 
	    dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        }
	   });
	    dlgAlert.setCancelable(true);
	    dlgAlert.create().show();
	}
	
	public static void MsgBox(Context ctx, DialogInterface.OnClickListener onClick, String sTitle,String fmt,Object ... args) {
		String msg = String.format(fmt, args);
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(ctx);                      
	    dlgAlert.setTitle(sTitle); 
	    dlgAlert.setMessage(msg); 
	    dlgAlert.setPositiveButton("OK", onClick);
	    dlgAlert.setCancelable(true);
	    dlgAlert.create().show();
	}
	
	
	public static void Toast(Context ctx,String fmt, Object ... args) {
		String msg = String.format(fmt, args);
		Toast.makeText(ctx,msg,Toast.LENGTH_SHORT).show();
	}
	
	public static void Notify(Context context,String sTitle,String fmt,Object ... args) {

        NotificationManager nm = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
       
              int icon = R.drawable.ic_launcher;
              long when = System.currentTimeMillis();
		
             String msg = String.format(fmt, args);

             Notification notification =  new Notification( icon, sTitle, when);
     
            Intent notificationIntent = new Intent("", null);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, Intent.FLAG_ACTIVITY_NO_USER_ACTION);
     
            notification.setLatestEventInfo(context,sTitle, msg,contentIntent);
            nm.notify( 1, notification );        
	}
	
};
