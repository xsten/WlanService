package net.stenuit.xavier.wlanservice;

import android.os.Binder;;

public class MyBinder extends Binder {
	private WlanService service;
	
	public WlanService getService()
	{
		return service;
	}
	
	public void setService(WlanService s)
	{
		this.service=s;
	}
}
