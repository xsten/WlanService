package net.stenuit.xavier.wlanservice.authentiker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Locale;

import net.stenuit.xavier.wlanservice.R;
import net.stenuit.xavier.wlanservice.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class FreeHotspotDotComAuthentiker extends Authentiker {

	public FreeHotspotDotComAuthentiker()
	{
		super();
		
		Log.i(getClass().getName(),"Changing cookie manager policy");
		
		// CookieHandler.setDefault(new CookieManager(null,CookiePolicy.ACCEPT_ALL));
	}
	
	@SuppressLint("DefaultLocale")
	@Override
	protected Object doInBackground(Object... arg0) {
		super.doInBackground(arg0);

		HttpResponse response=null;
		
		Resources res=((Context)arg0[0]).getResources();
		KeyStore localKeyStore;
		//String login=super.getCredentials().get("login");
		//String password=super.getCredentials().get("password");
		
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
			BasicCookieStore cookieStore=new BasicCookieStore();
			client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
			client.setCookieStore(cookieStore);
			
			client.setRedirectHandler(new DontFollowRedirectHandler());
			
			// Follows all redirections !
			String redirectURL="http://home.bt.com"; // starting point
			boolean redirected=false;
			
			
			/* The final goal is to find the following parameters
	  <input type="hidden" name="adid" value="210748259">
	  <input type="hidden" name="hs" value="8271">
	  <input type="hidden" name="lang" value="francais">
	  <input type="hidden" name="box" value="48:5B:39:E7:D4:E5">
	  <input type="hidden" name="user" value="38:59:F9:05:93:D7">
	  <input type="hidden" name="subsvc" value="192.168.200.1:81">
	  <input type="hidden" name="fpses" value="1a8e4892e9df30000011485b39e7d4e5">
	  <input type="hidden" name="url" value="http://www.quick.lu">
	  <input type="hidden" name="noAds" value="0">
      <input type="hidden" name="dev" value="smallscreen">
      	
      	Those inputs come after several redirections...
      	We parse all the HTML until we find it.
      	If redirections are found, we reconnect to the new site indicated
			 */
			
			final HashMap<String, String> htmlInputs=new HashMap<String,String>();
					
			while(redirectURL!=null)
			{
				String s;
				
				HttpGet httpget=new HttpGet(redirectURL);
				response=client.execute(httpget);
				
				if(response.getStatusLine().getStatusCode()==302)
				{
					Log.d(getClass().getName(),"Redirected !!!");
					redirected=true;
					String work=response.getHeaders("Location")[0].getValue();
					if(!work.contains("parms="))
					{
						redirectURL=work;
					}
					else
					{
						String part1=work.substring(0,work.indexOf("parms="));
						// part2 is "parms="
						String part3=URLEncoder.encode(work.substring(work.indexOf("parms=")+6));
						redirectURL=part1+"parms="+part3;
						Log.d(getClass().getName(),"redirection with reencoded params : "+redirectURL);
					}
					continue;
				}
				
				HttpEntity entity=response.getEntity();
				
				Log.d(getClass().getName(),"Login form get :"+response.getStatusLine() );
								
				InputStream is=entity.getContent();
				BufferedReader br=new BufferedReader(new InputStreamReader(is));
			
				s=br.readLine();
				redirectURL=null;
				while(s!=null)
				{
					Log.d(getClass().getName(),s);
					if(s.toUpperCase(Locale.ENGLISH).contains("<META HTTP-EQUIV=\"REFRESH\""))
					{
						redirected=true;
						redirectURL=s.substring(s.indexOf("URL=")+4,s.length()-2);		
						redirectURL=redirectURL.replaceAll("&amp;", "&");
						break;
					}
					if(s.contains("<input type=\"hidden\" name="))
					{
						if(!s.contains("sclot")&&!s.contains("scValue"))
						{
							String[] parsed=Utils.parseInputLine(s);
							htmlInputs.put(parsed[0],parsed[1]);
						}
					}
					
					s=br.readLine();
				}
				br.close();
				if(entity!=null)entity.consumeContent();
			}			
			
			if(!redirected)
			{
				Log.d(getClass().getName(),"Not redirected - we are already logged in !");
				return null;
			}
			
			Log.d(getClass().getName(),"Reached login page ! - just need to post OK");
			// String s="https://has.anacapa.biz/cgi-bin/bld_um_rel.pm";
			String s="http://mw.anacapa.biz/release";
			
			for (String k : htmlInputs.keySet()) {
				s+="&";
				s+=k+"="+htmlInputs.get(k);
			}
			s=s.replaceFirst("&", "?");
			
			Log.d(getClass().getName(),"Now logging in to :"+s);
		
			HttpGet request=new HttpGet(s);			
			response=client.execute(request);
			HttpEntity entity=response.getEntity();
			InputStream is=entity.getContent();
			BufferedReader br=new BufferedReader(new InputStreamReader(is));
			s=br.readLine();
			while(s!=null)
			{
				Log.d(getClass().getName(),s);
				s=br.readLine();
			}
			br.close();
			is.close();
			client.getConnectionManager().shutdown();
			
		}
		catch(Exception e)
		{
			Log.e(getClass().getName(),"Exception thrown",e);
			// Toast.makeText(ctx, "Problem connecting to "+getPropertiesKey(), Toast.LENGTH_SHORT).show();
		}
		
		// TODO provide an httpresponse
		return response;
	}

	@Override
	public String getPropertiesKey() {
		return "free-hotspot.com";
	}

}
