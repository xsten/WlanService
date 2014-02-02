package net.stenuit.xavier.wlanservice.authentiker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import net.stenuit.xavier.wlanservice.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

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
		
		// Check whether we are redirected
		if(!isCaptivePortal())return null;			
		
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

			DefaultHttpClient client = new DefaultHttpClient(cm, params);
	        
			HttpGet httpGet=new HttpGet("http://www.w3.org");
			HttpResponse resp=client.execute(httpGet);
			
			org.apache.http.client.CookieStore store=client.getCookieStore();
			List<Cookie> cookieList=store.getCookies();
			
			if(cookieList!=null)
			{
				for(Cookie c:cookieList)
				{
					Log.d(getClass().getName(),"Cookie:"+c.getName());
				//	store.addCookie(c);
				}
			}
				Log.i(getClass().getName(),"Sending login to belgacom");
				
				HttpContext  ctx=new BasicHttpContext();
				ctx.setAttribute(ClientContext.COOKIE_STORE, store);
				
				HttpPost post=new HttpPost("https://belgacom.portal.fon.com/en/login/processLogin");
				List<NameValuePair> pairs=new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("login[user]", login));
				pairs.add(new BasicNameValuePair("login[pass]", password));
				pairs.add(new BasicNameValuePair("login_reminder","false")); // has been added by Belgacom
				pairs.add(new BasicNameValuePair("commit", "Login"));
				post.setEntity(new UrlEncodedFormEntity(pairs));
				
				response=client.execute(post,ctx);
			
				Log.i(getClass().getName(),"Response : "+response.getStatusLine().getStatusCode());
				InputStream is=response.getEntity().getContent();
				try
				{
					BufferedReader br=new BufferedReader(new InputStreamReader(is));
					Log.d(getClass().getName(),"Please check the resulting login page here !");
					String s=br.readLine();
					while(s!=null)
					{
						Log.d(getClass().getName(), s);
						
						if("There has been an error. Please try again later.".equals(s.trim()))
						{
							// if wrong, we shall respond with "unauthorized=401" http response
							// simulates this behaviour by forging this answer
							response.getEntity().consumeContent();
							is.close();
							response=new BasicHttpResponse(HttpVersion.HTTP_1_1,401,"Unauthorized");
						}
						
						s=br.readLine();
					}
					
				}
				finally
				{
					is.close();
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
