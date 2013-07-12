package net.stenuit.xavier.cetrelwlanservice;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyBroadcastReceiver extends BroadcastReceiver {
	private String login;
	private String password;
	public static File SettingsFile=null;
	
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
		
		PostHttpTask tsk=new PostHttpTask();
		tsk.execute(ctx,null,getLogin(),getPassword());
		
	}

	public void readLoginFromFile() {
		try
		{
			setLogin(null);
			setPassword(null);
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

