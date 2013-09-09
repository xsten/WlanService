package net.stenuit.xavier.wlanservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class MainActivity extends Activity{
	Intent settingsIntent=null;
	private MyBroadcastReceiver myBroadcastReceiver=null;
	private Intent serviceIntent;
	
	/*
	public String getLogin() {
		return myBroadcastReceiver.getLogin();
	}
	*/
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus)
		{	// We got again the focus, maybe the SettingsActivity has changed the password !
			// Let's reread it
			myBroadcastReceiver.readLoginFromFile();
			
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*
		 * We need to create a service (in order to work in background)
		 * The service will create a broadcastreceiver (in order to receive events from the OS when network is re-/dis-/connected
		 * 
		 * The login/password is stored in the broadcastreceiver
		 * 	- the init() method of broadcastreceiver must be called to let it read the password file
		 * 
		 * We need to know when service is started (once it is started, broadcastreceiver is created, and we need a reference to it for login/pwd)
		 * --> we bind to this service, and read the reference to broadcastreceiver once bound !
		 */
		super.onCreate(savedInstanceState);
		Log.i(getClass().getName(),"onCreate() called");
				
		// Setting the main layout screen
		setContentView(R.layout.mainlayout);
		
		serviceIntent=new Intent(this,WlanService.class);

		bindMyService();
		
		
		// Turns on the "autoregister" button, since the broadcast receiver is active
		// ToggleButton tb=(ToggleButton)findViewById(R.id.toggleButton1);
		// tb.setChecked(true);
		
		
		// Creates another intent for settings screen
		settingsIntent=new Intent(this,SettingsActivity.class);

		LinearLayout ll=(LinearLayout)findViewById(R.id.myLinearLayout);
		String[] modules=this.getResources().getStringArray(R.array.modules);
		for (int i=0;i<modules.length;i++)
		{
			LinearLayout twoButtons=(LinearLayout)getLayoutInflater().inflate(R.layout.twobuttons,null);
			ToggleButton tb=(ToggleButton)twoButtons.getChildAt(0);
			tb.setTextOn(modules[i]+" active");
			tb.setTextOff(modules[i]+" inactive");
			tb.setChecked(true);
			ll.addView(twoButtons);
		}
		
		Log.i(getClass().getName(),"Finished onCreate");

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// avoids leakage : stops referencing everything that references me !
		settingsIntent=null; 
		serviceIntent=null;
		if(myServiceConnection!=null)
		{
			try
			{ 
				unbindService(myServiceConnection);
			}
			catch(RuntimeException e){};  // Sometimes, the service is not bound upon destroy() call 
		}
		myServiceConnection=null;
		
	}
	private void bindMyService() {
		// Starts the service if it was not yet started
		// Without this call, leakage exceptions will be shown
		startService(new Intent(this,WlanService.class));
		if(serviceIntent!=null && myServiceConnection!=null)
			bindService(serviceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void unbindMyService()
	{
		if(myServiceConnection!=null)
			unbindService(myServiceConnection);
	}
	
	private ServiceConnection myServiceConnection=new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(getClass().getName(),"onServiceDisconnected called");
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(getClass().getName(),"onServiceConnected() called");
			// if the service is connected, it means that it is running
			// --> we can now read wht value of myBroadcastListener !!!
			myBroadcastReceiver=WlanService.myBroadcastReceiver;
			if(myBroadcastReceiver==null)
				Log.e(getClass().getName(),"myBroadcastListener is null !");
		
			
			// classCastException
			//wlanService=((WlanService.LocalBinder)service).getService();
			//Log.i(getClass().getName(),"wlanService="+wlanService.toString());
		}
	};
	@Override
	protected void onPause() {
		super.onPause();
		Log.i(getClass().getName(),"onPause() called");

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(getClass().getName(),"onResume() called");
	}


	public void autoClicked(View v)
	{
		ToggleButton tb=(ToggleButton)v;
		try
		{
			LinearLayout lila=(LinearLayout)v.getParent();
			
			Log.d(getClass().getName(),"Clicked : "+((ToggleButton)lila.getChildAt(0)).getText());
		}
		catch(ClassCastException e)
		{
			Log.e(getClass().getName(),"Exception thrown : ",e);
		}
		if(tb.isChecked())
		{
			Log.d(getClass().getName(),"Button checked");
			// bindMyService();
		}
		else
		{
			Log.d(getClass().getName(),"Button cleared");
			// unbindMyService();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if("help".equals(item.getTitleCondensed()))
		{

			Intent helpIntent=new Intent(this,HelpActivity.class);
			startActivity(helpIntent);
		}
		
		return false;
	}
	
	public void settingsClicked(View v)
	{
		try
		{
			LinearLayout lila=(LinearLayout)v.getParent();
			
			Log.d(getClass().getName(),"Clicked : "+((ToggleButton)lila.getChildAt(0)).getText());
		}
		catch(ClassCastException e)
		{
			Log.e(getClass().getName(),"Exception thrown : ",e);
		}

		// Test : random module...
//		String[] modules=this.getResources().getStringArray(R.array.modules);
//		Random r=new Random(System.currentTimeMillis());
//		int idx=r.nextInt(modules.length);
//		
//		settingsIntent.putExtra("DEFAULT_AUTHENTIKER",modules[idx]);
		// end test
		
		startActivity(settingsIntent);
	}	
	
}
