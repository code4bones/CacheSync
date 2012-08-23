package com.google.cachesync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;


public class APNHelper {

	public class APN {
		public String MMSCenterUrl = "";
		public String MMSPort = "";
		public String MMSProxy = ""; 
	}

	private Context context;
	
	public APNHelper(final Context context) {
	    this.context = context;
	}   

public List<APN> getMMSApns() {     
	
    final Cursor apnCursor = this.context.getContentResolver().query(Uri.parse("content://telephony/carriers/preferapn"),null, null, null, null);
if ( apnCursor == null ) {
        NetLog.v("Null\n");
		return null;
    } else {
        final List<APN> results = new ArrayList<APN>(); 
            if ( apnCursor.moveToFirst() ) {
            for ( int i = 0; i < apnCursor.getColumnCount() ;i++)
            	NetLog.v("%d %s %s\n",i,apnCursor.getColumnName(i),apnCursor.getString(i));
            do {
        	//SendReq f;
            //final String type = apnCursor.getString(apnCursor.getColumnIndex(Telephony.Carriers.TYPE));
                /*
            	final String mmsc = apnCursor.getString(apnCursor.getColumnIndex("mmsc"));
                final String mmsProxy = apnCursor.getString(apnCursor.getColumnIndex(Telephony.Carriers.MMSPROXY));
                final String port = apnCursor.getString(apnCursor.getColumnIndex(Telephony.Carriers.MMSPORT));                  
                final APN apn = new APN();
                apn.MMSCenterUrl = mmsc;
                apn.MMSProxy = mmsProxy;
                apn.MMSPort = port;
                results.add(apn);
                }*/
            
        } while ( apnCursor.moveToNext() ); 
             }              
        apnCursor.close();
        return results;
    }
}

}