package net.stenuit.xavier.wlanservice.authentiker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import net.stenuit.xavier.wlanservice.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
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
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

/**
 * 
 * @author sx0419
 *
 * posts login/password to a given URL
 * 
 * 
 */
public class GuestAuthentiker extends Authentiker {
	
	@Override
	protected Object doInBackground(Object... arg0) {
		
		Resources res=((Context)arg0[0]).getResources();
		KeyStore localKeyStore;
		HttpResponse response=null;
		// textView=(TextView)arg0[1];
		// String login=(String)arg0[2];
		// String password=(String)arg0[3];
		String login=super.getCredentials().get("login");
		String password=super.getCredentials().get("password");
		
		try
		{
			Log.d(getClass().getName(),"Sending credentials to server : "+login+" "+password);
			
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

		// Seems not working anymore since 20130905
		 /*
			HttpPost post=new HttpPost("https://1.1.1.1/login.html");	
			List<NameValuePair> pairs=new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("username", login));
			pairs.add(new BasicNameValuePair("redirect_url", "http://www.google.com"));
			pairs.add(new BasicNameValuePair("password", password));
			pairs.add(new BasicNameValuePair("info_msg", ""));
			pairs.add(new BasicNameValuePair("info_flag", "0"));
			pairs.add(new BasicNameValuePair("err_msg", ""));
			pairs.add(new BasicNameValuePair("err_flag", "0"));
			pairs.add(new BasicNameValuePair("buttonClicked", "4"));
		*/
			// New login method since 20130905
			HttpPost post=new HttpPost("https://belgacom.portal.fon.com/en/login/processLogin");
			List<NameValuePair> pairs=new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("login[user]",login));
			pairs.add(new BasicNameValuePair("login[pass]", password));
			pairs.add(new BasicNameValuePair("commit","Login"));
			// textView.append("Posting everyting");
			post.setEntity(new UrlEncodedFormEntity(pairs));

		
			response=client.execute(post);
			
			InputStream is=response.getEntity().getContent();
			BufferedReader br=new BufferedReader(new InputStreamReader(is));
			String s=br.readLine();
			while(s!=null)
			{
				// Log.d(getClass().getName(),s);
				if(s.contains("<INPUT TYPE=\"hidden\" NAME=\"err_flag"))
				{
					int idx=s.indexOf("VALUE=");
					String value=s.substring(idx+7, idx+8);
					Log.d(getClass().getName(),"s="+s);
					Log.d(getClass().getName(),"value="+value);
					if("0"!=value)
					{
						response.getEntity().consumeContent();
						response=new BasicHttpResponse(HttpVersion.HTTP_1_1,401,"Unauthorized");
						Log.e(getClass().getName(),"Invalid Password ?");
						br.close();
						is.close();
						break;
					}
				}
				s=br.readLine();
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
		
		return "guest";
	}
}
