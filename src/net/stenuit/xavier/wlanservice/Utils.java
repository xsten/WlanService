package net.stenuit.xavier.wlanservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class Utils {
	public static String readLoginFromFile(File SettingsFile) {
		BufferedReader reader=null;
		String ret=null;
		
		try
		{
			reader=new BufferedReader(new FileReader(SettingsFile));
			ret=reader.readLine();
			reader.close();
		}
		catch(Exception e)
		{
			Log.i(Utils.class.getName(),"Exception thrown",e);
		}
		return ret;
	}
	public static String readPasswordFromFile(File SettingsFile) {
		BufferedReader reader=null;
		String ret=null;
		try
		{
			reader=new BufferedReader(new FileReader(SettingsFile));
			reader.readLine(); // login line - skips it
			ret=reader.readLine();
		}
		catch(Exception e)
		{
			Log.i(Utils.class.getName(),"Exception thrown",e);
		}
		return ret;
	}
	/** Gets SSID of the network
	 * 
	 * @return SSID of network connected, or null if not connected
	 */
	public static String getSSID(Context ctx)
	{
		WifiManager wifiManager=(WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo=wifiManager.getConnectionInfo();
		return(wifiInfo.getSSID());
	}

}
