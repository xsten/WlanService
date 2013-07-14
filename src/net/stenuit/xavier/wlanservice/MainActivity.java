package net.stenuit.xavier.wlanservice;

import net.stenuit.xavier.wlanservice.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity{
	Intent settingsIntent=null;
	public static Intent intent=null;
	private MyBroadcastReceiver myBroadcastReceiver=null;
	private Intent serviceIntent;
	
	public String getLogin() {
		return myBroadcastReceiver.getLogin();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus)
		{	// We got again the focus, maybe the SettingsActivity has changed the password !
			// Let's reread it
			myBroadcastReceiver.readLoginFromFile();
			
		}
	}

	public void setLogin(String login) {
		myBroadcastReceiver.setLogin(login);
	}

	public String getPassword() {
		return myBroadcastReceiver.getPassword();
	}

	public void setPassword(String password) {
		myBroadcastReceiver.setPassword(password);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*
		 * We need to create a service (in order to work in background)
		 * The service will create a broadcastreceiver (in order to receive events from the OS when network is re-/dis-/connected
		 * 
		 * The login/password is stored in the broadcastreceiver
		 * 	- the init() method of broadcastreceiver must be called to let it read the password file
		 * 
		 * We need to know when service is started (once it is started, broadcastreceiver is created, and we need a reference to it for login/pwd)
		 * --> we bind to this service, and read the reference to broadcastreceiver once bound !
		 */
		super.onCreate(savedInstanceState);
		Log.i(getClass().getName(),"onCreate() called");
				
		// Setting the main layout screen
		setContentView(R.layout.mainlayout);
		
		serviceIntent=new Intent(this,WlanService.class);
		if(startService(serviceIntent)!=null)
		{ // service has been started (or was already running) --> binds to it
			bindService(serviceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
			// once bound we will be able to read myBroadcastReceiver
			/*myBroadcastReceiver=WlanService.myBroadcastReceiver;
			if(myBroadcastReceiver==null)
				Log.e(getClass().getName(),"myBroadcastListener is null !");*/
		}
		
		// Turns on the "autoregister" button, since the broadcast receiver is active
		ToggleButton tb=(ToggleButton)findViewById(R.id.toggleButton1);
		tb.setChecked(true);
		
		
		// Creates another intent for settings screen
		settingsIntent=new Intent(this,SettingsActivity.class);
		intent=this.getIntent();
				
		Log.i(getClass().getName(),"Finished onCreate");
		// finish(); // will hide GUI
	}
	private ServiceConnection myServiceConnection=new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(getClass().getName(),"onServiceConnected() called");
			// if the service is connected, it means that it is running
			// --> we can now read wht value of myBroadcastListener !!!
			myBroadcastReceiver=WlanService.myBroadcastReceiver;
			if(myBroadcastReceiver==null)
				Log.e(getClass().getName(),"myBroadcastListener is null !");
		}
	};
	@Override
	protected void onPause() {
		super.onPause();
		Log.i(getClass().getName(),"onPause() called");
//		unregisterReceiver(myBroadcastReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(getClass().getName(),"onResume() called");
		
	}

	public void statusClicked(View v)
	{
		checkWlanStatus();
	}
	public void registerClicked(View v)
	{
		registerWlan();
		
	}
	public void autoClicked(View v)
	{
		ToggleButton tb=(ToggleButton)v;
		if(tb.isChecked())
		{
			Log.d(getClass().getName(),"Button checked");
			startService(serviceIntent);
		}
		else
		{
			Log.d(getClass().getName(),"Button cleared");
			stopService(serviceIntent);
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if("setLogin".equals(item.getTitleCondensed()))
		{
			startActivity(settingsIntent);
		}
		
		return false; // TODO : what if we return true ???
	}

	private void registerWlan() {
		String ssid=Utils.getSSID(this);
		if(ssid==null) ssid="ssid was not returned";
		if(!ssid.equals(getResources().getString(R.string.SupportedSSID)))
		{
			showText("Please connect to network '"+getResources().getString(R.string.SupportedSSID)+"' first !");
			return;
		}
		
		PostHttpTask tsk=new PostHttpTask();
		tsk.execute(getApplicationContext(),null,myBroadcastReceiver.getLogin(),myBroadcastReceiver.getPassword());
	}

	private void checkWlanStatus() {
		String toshow;
		
		try
		{
			String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			toshow="WLAN login service - v"+versionName+"\n";
		}
		catch (NameNotFoundException e) {
			toshow="Could not read version\n";
		}
		
		ConnectivityManager cman=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifiState=cman.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		String state="UNKNOWN";
		if(wifiState==NetworkInfo.State.CONNECTED) state="CONNECTED";
		if(wifiState==NetworkInfo.State.DISCONNECTED) state="DISCONNECTED";
		
		toshow+="Wifi State : "+state+"\n";
		
		if("CONNECTED".equals(state))
		{
			WifiManager wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo=wifiManager.getConnectionInfo();
			String ssid=wifiInfo.getSSID();
			toshow+=("SSID:"+ssid+"\n");
		}
		if(getPassword()==null)
			toshow+="Password is not set - please set your login/password using menu\n";
		else
			toshow+="Password is set for user "+getLogin();
		showText(toshow);
	}

	private void showText(String txt)
	{
		TextView v=(TextView)findViewById(R.id.textView1);
		v.setText(txt);
	}
	
	
}
