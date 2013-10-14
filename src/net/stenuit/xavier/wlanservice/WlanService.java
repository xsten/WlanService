package net.stenuit.xavier.wlanservice;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

public class WlanService extends Service{
	private MyBinder myBinder;
	
	private MyBroadcastReceiver myBroadcastReceiver=null;
	
//	public final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
//
//	static class IncomingHandler extends Handler { // Handler of incoming messages from clients.
//         @Override
//        public void handleMessage(Message msg) {
//        	 Log.i(getClass().getName(),"Message received : "+msg.toString());
//                super.handleMessage(msg);
//        }
//        
//    }
    
//	public class LocalBinder extends Binder {
//		WlanService getService()
//		{
//			return WlanService.this;
//		}
//	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		Log.i(getClass().getName(),"onStartCommand() called");
	
		activateBroadcastReceiver();
	
		return Service.START_STICKY;
	}
	
	public MyBroadcastReceiver getMyBroadcastReceiver()
	{
		return myBroadcastReceiver;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(getClass().getName(),"onBind() called");
		// return mMessenger.getBinder();
		myBinder=new MyBinder();
		myBinder.setService(this);
		return myBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(getClass().getName(),"onCreate() called");
		activateBroadcastReceiver();
		
	}

	@Override
	public void onDestroy() {
		Log.d(getClass().getName(),"onDestroy() called");
		desactivateBroadcastReceiver();
		super.onDestroy();
	}

	public void activateBroadcastReceiver()
	{
		if(myBroadcastReceiver==null)
		{
			myBroadcastReceiver=new MyBroadcastReceiver();
			myBroadcastReceiver.init(getFilesDir());
			Log.d(getClass().getName(),"myBroadcastReceiver instanciated");
		}
		
		Log.d(getClass().getName(),"Registring BroadcastReceiver");
		try
		{	
			this.registerReceiver(myBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
		catch(Exception e){
			Log.e(getClass().getName(), "Caught Exception", e);
		}
	}
	
	public void desactivateBroadcastReceiver()
	{
		this.unregisterReceiver(myBroadcastReceiver);
	}
}
