package net.stenuit.xavier.wlanservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	public static File SettingsFile=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		File homedir=getFilesDir();
		Log.d(getClass().getName(), "file directory is "+homedir.getAbsolutePath());
		SettingsFile=new File(homedir,getResources().getString(R.string.filename));
		
		Spinner s=(Spinner)findViewById(R.id.propertieskey);
		
		ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.modules, R.layout.spinnerlayout);
		s.setAdapter(adapter);
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// Fills the correct login and password in the appropriate views
		Log.d(getClass().getName(),"onWindowFocusChanged called");
		if(hasFocus)
		{
			Spinner keyView=(Spinner)findViewById(R.id.propertieskey);
			TextView loginView=(TextView)findViewById(R.id.loginField);
			
			loginView.setText(Utils.readLoginFromFile(SettingsFile).get(keyView.getSelectedItem()));

			TextView passwordView=(TextView)findViewById(R.id.editText2);
			passwordView.setText(Utils.readPasswordFromFile(SettingsFile).get(keyView.getSelectedItem()));
		}
		super.onWindowFocusChanged(hasFocus);
	}

	public void OkButtonClicked(View v)
	{
		Log.i(getClass().getName(), "OKButtonClicked called");
		
		try
		{

			 // Utils.clearFile(SettingsFile);
			
			Map<String,String> loginmap=Utils.readLoginFromFile(SettingsFile);
			loginmap.put(((Spinner)findViewById(R.id.propertieskey)).getSelectedItem().toString(), ((TextView)findViewById(R.id.loginField)).getText().toString());
			Map<String,String> passwdmap=Utils.readPasswordFromFile(SettingsFile);
			passwdmap.put(((Spinner)findViewById(R.id.propertieskey)).getSelectedItem().toString(),((TextView)findViewById(R.id.editText2)).getText().toString());

			BufferedWriter writer=new BufferedWriter(new FileWriter(SettingsFile,false));

			Iterator<String> i=loginmap.keySet().iterator();
			while(i.hasNext())
			{
				String k=i.next();
				String login=loginmap.get(k);
				writer.write(k+".login="+login);
				writer.write("\n");
				String pwd=passwdmap.get(k);
				writer.write(k+".password="+pwd);
				Log.i(getClass().getName(),"Writing to file : "+login+","+pwd);
				writer.write("\n");
			}
			writer.flush();
			writer.close();
			
			Log.d(getClass().getName(),"File saved");
		}
		catch(Exception e)
		{
			Log.e(getClass().getName(),"Exception thrown",e);
		}
		
		finish();
		
	}
	
	public void CancelButtonClicked(View v)
	{
		Log.i(getClass().getName(), "CancelButtonClicked called");
		finish(); 
	}

	public File getSettingsFile() {
		return SettingsFile;
	}

	public void setSettingsFile(File settingsFile) {
		SettingsFile = settingsFile;
	}
	
}
