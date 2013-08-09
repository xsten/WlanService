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
import java.util.HashMap;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import net.stenuit.xavier.wlanservice.R;
import net.stenuit.xavier.wlanservice.Utils;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class FreeHotspotDotComAuthentiker extends Authentiker {

	public FreeHotspotDotComAuthentiker()
	{
		super();
		
		Log.i(getClass().getName(),"Changing cookie manager policy");
		
		CookieHandler.setDefault(new CookieManager(null,CookiePolicy.ACCEPT_ALL));
	}
	
	@SuppressLint("DefaultLocale")
	@Override
	protected Object doInBackground(Object... arg0) {
		super.doInBackground(arg0);
		
		Resources res=((Context)arg0[0]).getResources();
		Context ctx=(Context)arg0[0];
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

			// Follows all redirections !
			String redirectURL="http://www.herm25.com/src/login.php"; // starting point
			
			
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
				
				URL myurl=new URL(redirectURL);
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection cnx=(HttpURLConnection)(myurl).openConnection();
				
				
				InputStream is=cnx.getInputStream(); // this starts effectively the connexion
				BufferedReader br=new BufferedReader(new InputStreamReader(is));
				// Yes, there are cookies, but nothing is sent through POST - so why bother ?
				// if(cnx.getHeaderField("Set-Cookie")!=null)
				//	cookies=cnx.getHeaderField("Set-Cookie");
				
				
				// freehotspot.com works with lot of redirects in the form of META HTTP-EQUIV="REFRESH"
				// The last destination is a <form> (get) pointing to an https site, with plenty of hidden data
				// We collect the hidden data on the way, and build a valid "get" URL to call
				s=br.readLine();
				redirectURL=null;
				while(s!=null)
				{
					Log.d(getClass().getName(),s);
					if(s.toUpperCase(Locale.ENGLISH).contains("<META HTTP-EQUIV=\"REFRESH\""))
					{
						redirectURL=s.substring(s.indexOf("URL=")+4,s.length()-2);
						break;
					}
					if(s.contains("<input type=\"hidden\" name="))
					{
						String[] parsed=Utils.parseInputLine(s);
						htmlInputs.put(parsed[0],parsed[1]);
					}
					
					s=br.readLine();
				}
				br.close();
				cnx.disconnect();
			}			
			
			Log.d(getClass().getName(),"Reached login page ! - just need to post OK");
			String s="https://has.anacapa.biz/cgi-bin/bld_um_rel.pm";
			
			for (String k : htmlInputs.keySet()) {
				s+="&";
				s+=k+"="+htmlInputs.get(k);
			}
			s.replaceFirst("&", "?");
			
			Log.d(getClass().getName(),"Now logging in to :"+s);
			URL url=new URL(s);
			HttpsURLConnection cnx=(HttpsURLConnection)url.openConnection();
			InputStream is=cnx.getInputStream();
			BufferedReader br=new BufferedReader(new InputStreamReader(is));
			
			s=br.readLine();
			while(s!=null)
			{
				Log.d(getClass().getName(),s);
			}
			br.close();
			is.close();
			
			Toast.makeText(ctx, "Connected to "+getPropertiesKey(),Toast.LENGTH_SHORT).show();
		}
		catch(Exception e)
		{
			Log.e(getClass().getName(),"Exception thrown",e);
			Toast.makeText(ctx, "Problem connecting to "+getPropertiesKey(), Toast.LENGTH_SHORT).show();
		}
		
		return null;
	}

	@Override
	public String getPropertiesKey() {
		return "free-hotspot.com";
	}

}
