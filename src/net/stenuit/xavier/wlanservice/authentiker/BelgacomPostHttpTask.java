package net.stenuit.xavier.wlanservice.authentiker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import net.stenuit.xavier.wlanservice.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
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

public class BelgacomPostHttpTask extends Authentiker {
	public BelgacomPostHttpTask()
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

			// textView.append("creating post object");
			                           
			// Check whether we are redirected
			URL url=new URL("http://www.w3.org");
			HttpURLConnection.setFollowRedirects(false); // Don't trust java redirects - it fails when protocol switches from http to https
			HttpURLConnection cnx=(HttpURLConnection)(url).openConnection();
			boolean mustLogin=false;
					
			String cookies="";
			try
			{
				if(cnx.getURL().getHost().equals(url.getHost()) && (!(cnx.getResponseCode()==302)))
				
				{
					Log.i(getClass().getName(),"No redirection");
					return null; // no need to connect.
				}
				else
				{
					String newUrl=cnx.getHeaderField("Location");
					Log.i(getClass().getName(),"Redirected to : "+newUrl);
					
					cookies=cnx.getHeaderField("Set-Cookie");
					Log.i(getClass().getName(),"Set-Cookie before redirect : "+cookies); // no cookie there
					cnx.disconnect(); // Closing the first connection
					
					
					cnx=(HttpURLConnection)new URL(newUrl).openConnection(); // Open connection on the redirect link
					if(cookies!=null && cookies.length()>0)
					{
						Log.i(getClass().getName(),"Re-sending cookie : "+cookies);
						cnx.addRequestProperty("Set-Cookie", cookies);
					}
					else
					{
						mustLogin=true;
					}
					
					/* Map toto= */ cnx.getHeaderFields(); // reads headers (headers contain cookies)
//					Set titi=toto.keySet();
//					for(Iterator i=titi.iterator();i.hasNext();)
//					{
//						Object o=i.next();
//						Log.i(getClass().getName(),"Read "+o);
//						Log.i(getClass().getName(),toto.get(o).toString());
//					}
					
					cookies=cnx.getHeaderField("Set-Cookie");
					Log.i(getClass().getName(),"Set-Cookie after redirect : "+cookies);
				}
				
			}
			finally
			{
				cnx.disconnect();
			}
			
			if(mustLogin)
			{
				Log.i(getClass().getName(),"Sending login to belgacom");
				
				HttpPost post=new HttpPost("https://belgacom.portal.fon.com/en/login/processLogin");
				List<NameValuePair> pairs=new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("login[user]", login));
				pairs.add(new BasicNameValuePair("login[pass]", password));
				pairs.add(new BasicNameValuePair("commit", "Login"));
				post.setEntity(new UrlEncodedFormEntity(pairs));
				post.addHeader("Cookie",cookies); // With cookies, we get a positive answer... Otherwise 404 
			
				response=client.execute(post);
			
				Log.i(getClass().getName(),"Response : "+response.getStatusLine().getStatusCode());
				InputStream is=response.getEntity().getContent();
				try
				{
					BufferedReader br=new BufferedReader(new InputStreamReader(is));
					String s=br.readLine();
					while(s!=null)
					{
						Log.d(getClass().getName(), s);
						s=br.readLine();
						// todo - if password is wrong - there may be a clue here...
					}
				}
				finally
				{
					is.close();
				}
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
		return "FON_BELGACOM";
	}

}
