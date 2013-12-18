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
		if("guest".equals(ssid))return new GuestAuthentiker();
		if("FON_BELGACOM".equals(ssid)||
				"SFR WiFi FON".equals(ssid))return new BelgacomPostHttpTask();
		if("free-hotspot.com".equals(ssid))return new FreeHotspotDotComAuthentiker();
		if("GratWiFi".equals(ssid))return new GratwifiAuthentiker();
		// if("clear-guest".equals(ssid))return new ClearGuestAuthentiker();
		Log.i(getClass().getName(),"Not found");
		return null;
	}
	
}
