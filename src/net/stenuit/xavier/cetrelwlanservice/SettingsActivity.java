package net.stenuit.xavier.cetrelwlanservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import net.stenuit.xavier.wlanservice.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// Fills the correct login and password in the appropriate views
		Log.d(getClass().getName(),"onWindowFocusChanged called");
		if(hasFocus)
		{
			TextView loginView=(TextView)findViewById(R.id.loginField);
			loginView.setText(Utils.readLoginFromFile(SettingsFile));

			TextView passwordView=(TextView)findViewById(R.id.editText2);
			passwordView.setText(Utils.readPasswordFromFile(SettingsFile));
		}
		super.onWindowFocusChanged(hasFocus);
	}

	public void OkButtonClicked(View v)
	{
		Log.i(getClass().getName(), "OKButtonClicked called");
		
		try
		{
			BufferedWriter writer=new BufferedWriter(new FileWriter(SettingsFile,false));
			
			String login=((TextView)findViewById(R.id.loginField)).getText().toString();
			writer.write(login);
			writer.write("\n");
			String pwd=((EditText)findViewById(R.id.editText2)).getText().toString();
			Log.i(getClass().getName(),"Writing to file : "+login+","+pwd);
			writer.write(pwd);
			writer.write("\n");
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
