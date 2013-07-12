package net.stenuit.xavier.cetrelwlanservice;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class WlanService extends Service{
	public static MyBroadcastReceiver myBroadcastReceiver=null;
	
	final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
    static class IncomingHandler extends Handler { // Handler of incoming messages from clients.
         @Override
        public void handleMessage(Message msg) {
                super.handleMessage(msg);
        }
    }
    
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		Log.i(getClass().getName(),"Started");
	  
		Log.i(getClass().getName(),"Leaving onStartCommand()");
		return Service.START_NOT_STICKY;
	}
	  
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(getClass().getName(),"onBind() called");
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(getClass().getName(),"onCreate() called");

		try
		{
			if(myBroadcastReceiver==null)
			{
				myBroadcastReceiver=new MyBroadcastReceiver();
				myBroadcastReceiver.init(getFilesDir());
				Log.d(getClass().getName(),"myBroadcastReceiver instanciated");
			}
			
			this.registerReceiver(myBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
		catch(Exception e){}
	}

}
