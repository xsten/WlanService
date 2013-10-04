package net.stenuit.xavier.wlanservice.authentiker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import net.stenuit.xavier.wlanservice.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class GratwifiAuthentiker extends Authentiker {

	public GratwifiAuthentiker()
	{
		super();
		
		Log.i(getClass().getName(),"Changing cookie manager policy");
		
		CookieHandler.setDefault(new CookieManager(null,CookiePolicy.ACCEPT_ALL));
	}
	
	
	@Override
	protected Object doInBackground(Object... arg0) {
		super.doInBackground(arg0);
		
		Resources res=((Context)arg0[0]).getResources();
		KeyStore localKeyStore;
		HttpResponse response=null;
		// textView=(TextView)arg0[1];
		String login=super.getCredentials().get("login");
		String password=super.getCredentials().get("password");
		
		try
		{
			
			localKeyStore=KeyStore.getInstance("BKS");
			InputStream in=res.openRawResource(R.raw.mykeystore);
			localKeyStore.load(in,"password".toCharArray());
			
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			org.apache.http.conn.ssl.SSLSocketFactory sslSocketFactory = new org.apache.http.conn.ssl.SSLSocketFactory(localKeyStore);
			schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
			HttpParams params = new BasicHttpParams();
			ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

			HttpClient client = new DefaultHttpClient(cm, params);
			
			// Check whether we are redirected
			URL url=new URL("http://www.w3.org");
			HttpURLConnection.setFollowRedirects(false); // Don't trust java redirects - it fails when protocol switches from http to https
			HttpURLConnection cnx=(HttpURLConnection)(url).openConnection();
			boolean mustLogin=false;
					
			String cookies="";
			try
			{ 
				Log.d(getClass().getName(),"cnx.getResponseCode()="+cnx.getResponseCode());
				Log.d(getClass().getName(),"cnx.getResponsetxt ()="+cnx.getResponseMessage());
				// cnx.getResponseCode()=511 for gratwifi (Redirect to login page)
				if(!(cnx.getResponseCode()==511))
				{ // not redirected - we can just exit
					Log.i(getClass().getName(),"No redirection");
					return null; // no need to connect.
				}
				else
				{ // Finds redirection
					String newUrl="";
					BufferedReader br=new BufferedReader(new InputStreamReader(cnx.getInputStream()));
					try
					{
						Log.d(getClass().getName(),"Reading redirection information : ");
						String s=br.readLine();
						while(s!=null)
						{
							Log.d(getClass().getName(),s);
							if(s.contains("http-equiv=\"refresh\""))
							{  // found the redirection
								newUrl=s.substring(s.indexOf("url=")+4,s.indexOf("\">"));
								
							}
							s=br.readLine();
						}
						// If we reach the end of the stream without having found the http-equiv="refresh"
						// then newUrl will be null, and an NPE will be thrown
					}
					finally
					{
						br.close();
						br=null;
					}
					cookies=cnx.getHeaderField("Set-Cookie");
					Log.i(getClass().getName(),"Set-Cookie before redirect : "+cookies); // no cookie there
					cnx.disconnect(); // Closing the first connection
					cnx=null;
					
					cnx=(HttpURLConnection)new URL(URLEncoder.encode(newUrl)).openConnection(); // Open connection on the redirect link
					Log.d(getClass().getName(),"Connected to : "+cnx.getURL());
					
					// Todo - check if the string "You have been penalised for exceeding the allowed data-limit"
					// And send an authentication problem if it occurs
					
					try
					{
						br=new BufferedReader(new InputStreamReader(cnx.getInputStream()));
						Log.d(getClass().getName(),"SECOND CALL");
						String s=br.readLine();
						while(s!=null)
						{
							Log.d(getClass().getName(),s);
							s=br.readLine();
						}
					}
					finally
					{
						br.close();
					}
					
					Log.d(getClass().getName(),"Response code (should be 301): "+cnx.getResponseCode());
					
					newUrl=cnx.getHeaderField("Location");
					Log.i(getClass().getName(),"Redirected to : "+newUrl);
					
					cookies=cnx.getHeaderField("Set-Cookie");
					Log.i(getClass().getName(),"Set-Cookie before redirect : "+cookies); // no cookie there
					cnx.disconnect(); // Closing the previous connection
					
					
					cnx=(HttpURLConnection)new URL(newUrl).openConnection(); // Open connection on the redirect link
					if(cookies!=null && cookies.length()>0)
					{
						Log.i(getClass().getName(),"Re-sending cookie : "+cookies);
						cnx.addRequestProperty("Set-Cookie", cookies);
					}
					
					List<NameValuePair> pairs=new ArrayList<NameValuePair>();
					try
					{
						br=new BufferedReader(new InputStreamReader(cnx.getInputStream()));
						Log.d(getClass().getName(),"THIRD CALL");
						String s=br.readLine();
						while(s!=null)
						{
							Log.d(getClass().getName(),s);
							if(s.contains("<input type=\"hidden\" value=\""))
							{
								Log.d(getClass().getName(),"name="+s.substring(s.indexOf("name=\"")+6,s.indexOf(s.indexOf("\"/>"))));
								Log.d(getClass().getName(),"valu="+s.substring(s.indexOf("value=\"")+7,s.indexOf("\" name=")));
								pairs.add(new BasicNameValuePair(s.substring(s.indexOf("name=\"")+6,s.indexOf(s.indexOf("\"/>"))),s.substring(s.indexOf("value=\"")+7,s.indexOf("\" name="))));
							}
							s=br.readLine();
						}
					}
					finally
					{
						br.close();
					}

				}
				
			}
			finally
			{
				if(cnx!=null)cnx.disconnect();
			}
		}
		catch(Exception e)
		{
			Log.e(getClass().getName(),"Exception thrown",e);
		}
		return response;
	}


	@Override
	public String getPropertiesKey() {
		return "GratWiFi";
	}

}
