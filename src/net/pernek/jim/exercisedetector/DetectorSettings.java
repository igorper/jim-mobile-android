package net.pernek.jim.exercisedetector;

import android.content.SharedPreferences;

// isServiceRunning and output path should be two parameters
// (output path should only be removed when the user manually stops data collection - 
// this measure is roboust to breaking the app in the middle of the sensing session - if
// this happens the service will simply be rerun with the 
public class DetectorSettings {
	
	private static final String KEY_SERVICE_RUNNING = "service_running";
	private static final String KEY_OUTPUT_FILE = "output_file";
	private static final String KEY_START_TIMESTAMP = "start_timestamp"; 

	private SharedPreferences mPreferences;
	
	private DetectorSettings(){}
	
	public static DetectorSettings create(){
		DetectorSettings ds = new DetectorSettings();
		
		return ds;
	}
	
	public static DetectorSettings create(SharedPreferences preferences){
		DetectorSettings ds = new DetectorSettings();
		ds.mPreferences = preferences;
		
		return ds;
	}
	
	
	// Internal
	public void saveStartTimestamp(long startTimestamp){
		SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(KEY_START_TIMESTAMP, startTimestamp);
        editor.commit();
	}
	
	public long getStartTimestamp(){
		return mPreferences.getLong(KEY_START_TIMESTAMP, 0);
	}
	
	public void saveServiceRunning(boolean running){
		SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(KEY_SERVICE_RUNNING, running);
        editor.commit();
	}
	
	public boolean isServiceRunning(){
		return mPreferences.getBoolean(KEY_SERVICE_RUNNING, false);
	}
	
	public void saveOutputFile(String output){
		SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(KEY_OUTPUT_FILE, output);
        editor.commit();
	}
	
	public String getOutputFile(){
		return mPreferences.getString(KEY_OUTPUT_FILE, "");
	}
	 
}
