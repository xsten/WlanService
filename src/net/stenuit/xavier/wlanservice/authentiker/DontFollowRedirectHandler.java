package net.stenuit.xavier.wlanservice.authentiker;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class DontFollowRedirectHandler implements RedirectHandler
{

	@Override
	public URI getLocationURI(HttpResponse arg0, HttpContext arg1)
			throws ProtocolException {
		Log.d(getClass().getName(),"getLocationURI() called");
		return null;
	}

	@Override
	public boolean isRedirectRequested(HttpResponse arg0, HttpContext arg1) {
		Log.d(getClass().getName(),"isRedirectRequested() called");
		
		return false;
	}
	
}