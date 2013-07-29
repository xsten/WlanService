package net.stenuit.xavier.wlanservice.authentiker;

import android.util.Log;

public class AuthentikerFactory {
	private static AuthentikerFactory instance=null;
	
	private AuthentikerFactory()
	{
	}
	
	public static AuthentikerFactory getInstance()
	{
		if(instance==null)
			instance=new AuthentikerFactory();
		
		return instance;
	}
	
	public Authentiker getAuthentiker(String ssid)
	{
		Log.i(getClass().getName(),"Looking for task for ssid : "+ssid);
		if("guest".equals(ssid))return new PostHttpTask();
		if("FON_BELGACOM".equals(ssid))return new BelgacomPostHttpTask();
		Log.i(getClass().getName(),"Not found");
		return null;
	}
}
