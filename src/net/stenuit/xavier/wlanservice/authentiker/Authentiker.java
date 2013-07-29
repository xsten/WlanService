package net.stenuit.xavier.wlanservice.authentiker;

import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;

public abstract class Authentiker extends AsyncTask<Object/*params*/, Object/*progress*/, Object/*result*/> {
	private Map<String, String> credentials;
	protected Context context;
	
	/**
	 * In the properties file, Login will be prepended with an unique key.
	 * 
	 * for example : for key "fon"
	 * fon.login=someuser
	 * fon.password=somepassword
	 * 
	 * @return the key (fon in the example above)
	 */
	public abstract String getPropertiesKey(); 
	
	public Map<String,String>getCredentials()
	{
		return credentials;
	}
	public void setCredentials(Map<String, String> m)
	{
		credentials=m;
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {
		context=(Context)arg0[0];
		
		return null;
	}

	public void setContext(Context c)
	{
		context=c;
	}
}
