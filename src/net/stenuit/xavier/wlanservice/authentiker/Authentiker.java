package net.stenuit.xavier.wlanservice.authentiker;

import java.util.Map;

import net.stenuit.xavier.wlanservice.R;

import org.apache.http.HttpResponse;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public abstract class Authentiker extends AsyncTask<Object/*params*/, Object/*progress*/, Object/*result*/> {
	private Map<String, String> credentials;
	protected Context context;
	private boolean active=true;
	
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
	
	@Override
	protected void onPostExecute(Object result) {
		if(result==null)
			result="null";
		
		Log.d(getClass().getName(),"On Post execute, argument is a "+result.getClass().getName());
		
		if(result instanceof HttpResponse)
		{
			HttpResponse resp=(HttpResponse)result;
			Log.d(getClass().getName(),"Status code : "+resp.getStatusLine().getStatusCode()+"\n");
			Log.d(getClass().getName(),"Object ID : "+this.toString());
			
			// String txt=context.getResources().getConfiguration().locale.
			if(resp.getStatusLine().getStatusCode()==200)
			{
				Log.d(getClass().getName(),"Sending positive answer");
				Toast.makeText(context, context.getString(R.string.registeredon)+getPropertiesKey(), Toast.LENGTH_SHORT).show();
			}
			else if(resp.getStatusLine().getStatusCode()==401)
			{
				Log.d(getClass().getName(),"Sending unauthenticated answer");
				Toast.makeText(context, getPropertiesKey()+" - "+context.getString(R.string.loginfail), Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			// Toast.makeText(context, "Error connecting to login server", Toast.LENGTH_SHORT).show();
			Log.d(getClass().getName(),"null answer");
		}
		
	}
}
