package net.stenuit.xavier.wlanservice;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.stenuit.xavier.wlanservice.authentiker.Authentiker;
import net.stenuit.xavier.wlanservice.authentiker.AuthentikerFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask.Status;
import android.util.Log;

public class MyBroadcastReceiver extends BroadcastReceiver {
	private Map<String,String> login=new HashMap<String, String>();
	private Map<String,String> password=new HashMap<String,String>();
	public static File SettingsFile=null;
	private HashMap<String, Boolean> authentikerActive=new HashMap<String,Boolean>();
	
	public String getLogin(String key) {
		if(key.contains(".")) // removes the trailing .com
			key=key.substring(0,key.indexOf('.'));
		return login.get(key);
	}

	public void setLogin(Map<String,String>login) {
		this.login=login;
	}

	public String getPassword(String key) {
		if(key.contains(".")) // removes the trailing .com
			key=key.substring(0,key.indexOf('.'));
		return password.get(key);
	}

	public void setPassword(Map<String,String> password) {
		this.password=password;
	}

	public void init(File homedir)
	{
		// Setting File to the correct file path
		SettingsFile=new File(homedir,"settings.dat");
		readLoginFromFile();
	}
	
	@Override
	public void onReceive(Context ctx, Intent intent) {		
		Log.d(getClass().getName(),"onReceive called");
		
		Log.d(getClass().getName(),"list of authentikers : ");
		// Lists the active authentikers
		for(String s:authentikerActive.keySet())
		{
			Log.d(getClass().getName(),"Authentiker "+s+" status:"+authentikerActive.get(s));
		}
		
		// Checks connected WIFI
		String ssid=Utils.getSSID(ctx);
		if(ssid==null) return; // not connected
		
		if(authentikerActive.containsKey(ssid))
		{
			if(authentikerActive.get(ssid)==false)
			{
				Log.d(getClass().getName(),"Authentiker "+ssid+" is not active - ignoring");
				return;
			}
		}
		
		Authentiker authentiker=AuthentikerFactory.getInstance().getAuthentiker(ssid);
		if(authentiker==null)
		{
			// Toast.makeText(ctx,"Connected to "+ssid+", not sending credentials",Toast.LENGTH_SHORT).show();
		}
		else
		{
			authentiker.setContext(ctx); // initialize properly
			
			Map<String,String>logins=Utils.readLoginFromFile(SettingsFile);
			Map<String,String>passwords=Utils.readPasswordFromFile(SettingsFile);
			
			Map<String,String> credentials=new HashMap<String, String>();
			credentials.put("login",logins.get(authentiker.getPropertiesKey()));
			credentials.put("password",passwords.get(authentiker.getPropertiesKey()));
			authentiker.setCredentials(credentials);
			try
			{
				if(!(authentiker.getStatus()==Status.RUNNING))
				{
					Log.d(getClass().getName(),"Starting authentiker");
					authentiker.execute(ctx);
				}
				else
				{
					Log.d(getClass().getName(),"Don't start authentiker - already running");
				}
					
			}
			catch(RuntimeException rte)
			{
				Log.e(getClass().getName(),"authentiker already running ?",rte);
			}
			
		}
	}

	public void readLoginFromFile() {
		try
		{
			login=new HashMap<String, String>();
			password=new HashMap<String, String>();
			
			setLogin(Utils.readLoginFromFile(SettingsFile));
			setPassword(Utils.readPasswordFromFile(SettingsFile));
		}
		catch(Exception e)
		{
			Log.i(getClass().getName(),"Exception thrown",e);
			setLogin(null);
			setPassword(null);
		}
	}

	/**
	 * Activates or desactivates authentikers
	 * @param chosenAuthentiker
	 * @param checked
	 */
	public void setAuthentikerStatus(String chosenAuthentiker, boolean checked) {
		authentikerActive.put(chosenAuthentiker, checked);
		
		for(String s:authentikerActive.keySet())
		{
			Log.d(getClass().getName(),"authentiker "+s+":"+authentikerActive.get(s));
		}
	}
}

