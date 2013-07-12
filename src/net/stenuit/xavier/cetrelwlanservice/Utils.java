package net.stenuit.xavier.cetrelwlanservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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

}
