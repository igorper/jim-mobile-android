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
	private static final String KEY_IS_EXERCISE_STATE = "is_exercise_state";
	private static final String KEY_CURRENT_TRAINING_PLAN = "current_training_plan";
	private static final String KEY_CURRENT_EXERCISE = "current_exercise";
	private static final String KEY_CURRENT_SERIES = "current_series";

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
	/*
	public void saveServiceRunning(boolean running){
		SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(KEY_SERVICE_RUNNING, running);
        editor.commit();
	}
	
	public boolean isServiceRunning(){
		return mPreferences.getBoolean(KEY_SERVICE_RUNNING, false);
	}*/
	
	public void saveOutputFile(String output){
		SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(KEY_OUTPUT_FILE, output);
        editor.commit();
	}
	
	public String getOutputFile(){
		return mPreferences.getString(KEY_OUTPUT_FILE, "");
	}
	
	public void saveIsExerciseState(boolean isExerciseState){
		SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(KEY_IS_EXERCISE_STATE, isExerciseState);
        editor.commit();
	}
	
	public boolean isExerciseState(){
		return mPreferences.getBoolean(KEY_IS_EXERCISE_STATE, false);
	}
	
	public void saveCurrentTrainingPlan(String jsonString){
		SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(KEY_CURRENT_TRAINING_PLAN, jsonString);
        editor.commit();
	}
	
	// returns current training plan encoded as json string
	public String getCurrentTrainingPlan(){
		return mPreferences.getString(KEY_CURRENT_TRAINING_PLAN, "");
	}
	
	public void saveCurrentExerciseIndex(int exerciseIndex){
		SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(KEY_CURRENT_EXERCISE, exerciseIndex);
        editor.commit();
	}
	
	// returns current exercise name
	public int getCurrentExerciseIndex(){
		// 0 because we start with exercise on index zero
		return mPreferences.getInt(KEY_CURRENT_EXERCISE, 0);
	}
	
	public void saveCurrentSeriesIndex(int seriesIndex){
		SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(KEY_CURRENT_SERIES, seriesIndex);
        editor.commit();
	}
	
	// returns current series as the position inside the series array of exercise
	public int getCurrentSeriesIndex(){
		// 0 because we start with series on index zero
		return mPreferences.getInt(KEY_CURRENT_SERIES, 0);
	}
}
