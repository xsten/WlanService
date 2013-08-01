package net.stenuit.xavier.wlanservice;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.stenuit.xavier.wlanservice.authentiker.Authentiker;
import net.stenuit.xavier.wlanservice.authentiker.AuthentikerFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {
	private Map<String,String> login=new HashMap<String, String>();
	private Map<String,String> password=new HashMap<String,String>();
	public static File SettingsFile=null;
	
	public String getLogin(String key) {
		return login.get(key);
	}

	public void setLogin(Map<String,String>login) {
		this.login=login;
	}

	public String getPassword(String key) {
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
		/*
		Log.i(getClass().getName(),"received broadcast, intent:"+arg1.getAction());
		Toast.makeText(ctx,"received broadcast:"+arg1.getAction(), Toast.LENGTH_SHORT).show();
		*/
		
		Log.d(getClass().getName(),"onReceive called");
		// Checks connected WIFI
		String ssid=Utils.getSSID(ctx);
		if(ssid==null) return; // not connected
		
		Authentiker authentiker=AuthentikerFactory.getInstance().getAuthentiker(ssid);
		if(authentiker==null)
		{
			// Toast.makeText(ctx,"Connected to "+ssid+", not sending credentials",Toast.LENGTH_SHORT).show();
		}
		else
		{
			authentiker.setContext(ctx); // initialize properly
			Toast.makeText(ctx, "Connected to "+ssid,Toast.LENGTH_SHORT).show();
			Map<String,String>logins=Utils.readLoginFromFile(SettingsFile);
			Map<String,String>passwords=Utils.readPasswordFromFile(SettingsFile);
			
			Map<String,String> credentials=new HashMap<String, String>();
			credentials.put("login",logins.get(authentiker.getPropertiesKey()));
			credentials.put("password",passwords.get(authentiker.getPropertiesKey()));
			authentiker.setCredentials(credentials);
			authentiker.execute(ctx);
			
			// Todo - change the getLogin() and getPassword() - use getCredentials() instead
		}
		/*
		if(ssid.equals(ctx.getResources().getString(R.string.SupportedSSID)))
		{
			PostHttpTask tsk=new PostHttpTask();
			tsk.execute(ctx,null,getLogin(),getPassword());
		}
		if(ssid.equals("FON_BELGACOM"))
		{
			Toast.makeText(ctx, "FON_BELGACOM detected",Toast.LENGTH_SHORT).show();
			BelgacomPostHttpTask tsk=new BelgacomPostHttpTask();
			tsk.execute(ctx);
		}
		else
		{ 
			// Toast.makeText(ctx, "Connected to network "+ssid, Toast.LENGTH_SHORT).show();
		}
		*/
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
}

