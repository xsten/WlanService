package net.stenuit.xavier.wlanservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class Utils {
	public static Map<String,String> readLoginFromFile(File SettingsFile) {
		BufferedReader reader=null;
		String ret=null;
		HashMap<String,String>hm=new HashMap<String, String>();
		try
		{
			reader=new BufferedReader(new FileReader(SettingsFile));
			ret=reader.readLine();
			while(ret!=null)
			{
				if(ret.contains("login")&&ret.contains(".")&&ret.contains("="))
				{ // seems to be a login
					String key=ret.substring(0, ret.indexOf((int)'.'));
					String login=ret.substring(ret.indexOf('=')+1);
					hm.put(key,login);
				}
				ret=reader.readLine();
			}
			reader.close();
		}
		catch(Exception e)
		{
			Log.i(Utils.class.getName(),"Exception thrown",e);
		}
		return hm;
	}
	
	public static void clearFile(File settingsFile)
	{
		settingsFile.delete();
		
	}
	public static HashMap<String,String> readPasswordFromFile(File SettingsFile) {
		BufferedReader reader=null;
		String ret=null;
		HashMap<String,String>hm=new HashMap<String, String>();
		try
		{
			reader=new BufferedReader(new FileReader(SettingsFile));
			ret=reader.readLine();
			while(ret!=null)
			{
				if(ret.contains("password")&&ret.contains(".")&&ret.contains("="))
				{ // seems to be a login
					String key=ret.substring(0, ret.indexOf((int)'.'));
					String pwd=ret.substring(ret.indexOf('=')+1);
					hm.put(key,pwd);
				}
				ret=reader.readLine();
			}
			reader.close();
		}
		catch(Exception e)
		{
			Log.i(Utils.class.getName(),"Exception thrown",e);
		}
		return hm;
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
