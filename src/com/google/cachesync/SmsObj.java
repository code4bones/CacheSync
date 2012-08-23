package com.google.cachesync;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.code4bones.utils.NetLog;

import android.database.Cursor;

public class SmsObj extends Object implements Comparable<SmsObj> {

	final public static int IN = 1;
	final public static int OUT = 2;
	
	public String phone;
	public String name;
	public String message;
	public String   date;
	public long  occured;
	public long  thread;
	public long  id;
	public long type;
	public long status;
	final public SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy' 'HH:mm:ss");
	
	public SmsObj(Cursor cur) {
		phone      = ContactObj.NormalizePhone(cur.getString(2));
		name       = phone;
		occured   = cur.getLong(4);
		date 		 = df.format(new Date(occured));
		thread	     = cur.getLong(1);
		id 			 = cur.getLong(0);
		type 		 = cur.getLong(8);
		status 		 = cur.getLong(7);
		message  = cur.getString(11);
	}

	public void Dump() {
		NetLog.v("%d : %s %s | %s %s\r\n",id,date,name,type == IN?"IN":"OUT",message);
	}

	public SmsObj assignIfNew(SmsObj old) {

		if ( old == null )
			return this;
		
		if ( old.occured < this.occured ) {
			return this;
		}
		return old;
	}
	
	public int compareTo(SmsObj rhs) {
		if ( thread < rhs.thread )
			return -1;
		else if ( thread > rhs.thread )
			return 1;
		return 0;
	}

}
