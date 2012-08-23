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

	private static PrintStream ps = null; 
	private static String sLogFile = null;
	private static String TAG = null;
	
	public static PrintStream getPrintStream() {
		return NetLog.ps;
	}
	
	public static PrintStream Init(String tag,String sLogFileName,boolean removeIfExists) {
		try {
			TAG = tag;
			sLogFile = Environment.getExternalStorageDirectory() + "/" + sLogFileName;
			File file = new File(sLogFile);
			//if ( file.exists() && removeIfExists ) {
			//	Log.v(TAG,"file removed");
			//	file.delete();
			//}
			
			if ( ps == null ) {
				ps = new PrintStream(new FileOutputStream(file,removeIfExists == false));
				NetLog.w("********* LOGGER STARTED ********* \n");
			} else
				NetLog.w("********* LOGGER ACQUIRED ********* \n");

			
			return ps;
		} catch ( Exception e ) {
			Log.v(TAG,"NetLog Error " + e.toString());
		}
		return null;
	}
	
	public static String getTimeStamp(String dateFormat) {
        Date currentTime = new Date();
		SimpleDateFormat df = new SimpleDateFormat(dateFormat == null?"dd.MM HH:mm:ss ":dateFormat);
		return df.format(currentTime);
	}

	public static void writeToFile(String severety,String fmt,Object ... args) {
		if ( NetLog.ps == null ) 
			return;
		synchronized (NetLog.ps) {
			
		NetLog.ps.printf("%s | %s | ",NetLog.getTimeStamp(null),severety);
		String msg = String.format(fmt, args);
		NetLog.ps.printf(fmt,args);
		if ( !msg.endsWith("\n") )
			NetLog.ps.printf("\n");
		}
	}
	
	public static void v(String fmt,Object ... args) {
		NetLog.writeToFile("V",fmt,args);
		Log.v(TAG,String.format(fmt, args));
	}

	public static void e(String fmt,Object ... args) {
		NetLog.writeToFile("E",fmt,args);
		Log.e(TAG,String.format(fmt, args));
	}

	public static void w(String fmt,Object ... args) {
		NetLog.writeToFile("W",fmt,args);
		Log.w(TAG,String.format(fmt, args));
	}

	public static void i(String fmt,Object ... args) {
		NetLog.writeToFile("I",fmt,args);
		Log.i(TAG,String.format(fmt, args));
	}
	
	
	public static void Dump() {
		File file = new File(sLogFile);
		try {
			String line = null;
			BufferedReader br = new BufferedReader( new InputStreamReader(new FileInputStream(file)));
			while ((line = br.readLine()) != null ) 
				Log.v(TAG,line);
			br.close();
		} catch ( Exception e ) {
			Log.v(TAG,"NetLog.Dump()=>"+e.toString());
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
