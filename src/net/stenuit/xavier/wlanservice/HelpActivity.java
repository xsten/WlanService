package net.stenuit.xavier.wlanservice;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(getClass().getName(),"onCreate() called");
		
		setContentView(R.layout.helplayout);
		
		checkWlanStatus();
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void okClicked(View v)
	{
		finish();
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
		/*
		if(getPassword()==null)
			toshow+="Password is not set - please set your login/password using menu\n";
		else
			toshow+="Password is set for user "+getLogin();
		*/
		
		// Prints supported SSID
		toshow+=getResources().getString(R.string.supportedModules)+"\n";
		String[] modules=this.getResources().getStringArray(R.array.modules);
		for(String module : modules)
		{
			toshow+=module+"\n";
		}
		showText(toshow);
	}

	private void showText(String txt)
	{
		TextView v=(TextView)findViewById(R.id.textView1);
		v.setText(txt);
	}

}
