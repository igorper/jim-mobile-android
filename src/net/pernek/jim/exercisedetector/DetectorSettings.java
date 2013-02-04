package net.pernek.jim.exercisedetector;

import android.content.SharedPreferences;

public class DetectorSettings {
	
	private static final String KEY_SERVICE_RUNNING = "service_running";

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
	public void saveServiceRunning(boolean running){
		SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(KEY_SERVICE_RUNNING, running);
        editor.commit();
	}
	
	public boolean isServiceRunning(){
		return mPreferences.getBoolean(KEY_SERVICE_RUNNING, false);
	}
	 
}
