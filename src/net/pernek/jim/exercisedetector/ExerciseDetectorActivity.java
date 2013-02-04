package net.pernek.jim.exercisedetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import net.pernek.jim.common.ExerciseDetectionAlgorithm;
import net.pernek.jim.common.ExerciseDetectionAlgorithmObserver;
import net.pernek.jim.common.ExerciseState;
import net.pernek.jim.common.LinearSensorInterpolator;
import net.pernek.jim.common.SensorInterpolator;
import net.pernek.jim.common.SensorInterpolatorObserver;
import net.pernek.jim.common.SensorValue;
import net.pernek.jim.common.StDevExerciseDetectionAlgorithm;
import net.pernek.jim.common.SensorValue.SensorType;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class ExerciseDetectorActivity extends Activity{
	
	private static final String TAG = "MainActivity";
	
	private CheckBox mChbToggleService;
	private DetectorSettings mSettings;
	private DetectorService mDetectorService;
	
	private ServiceConnection mDetectorConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mDetectorService = null;
			
			Log.w(TAG, "MainActivity onServiceDisconnected");
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDetectorService = ((DetectorService.DetectorServiceBinder)service).getService();
			
			Log.w(TAG, "MainActivity onServiceConnected");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSettings = DetectorSettings.create(PreferenceManager.getDefaultSharedPreferences(this));
		
		mChbToggleService = (CheckBox)findViewById(R.id.chbToggleService);
		mChbToggleService.setChecked(mSettings.isServiceRunning());
				
		mChbToggleService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// store persistently if the service is running
				mSettings.saveServiceRunning(isChecked);
				
				if(isChecked){
					startService(new Intent(ExerciseDetectorActivity.this, DetectorService.class));
									
					bindIfNotBound();
				} else {
					unbindDetectorService();
					
					stopService(new Intent(ExerciseDetectorActivity.this, DetectorService.class));
				}	
			}
		});
		
		bindIfNotBound();
		
		Log.w(TAG, "OnCreate");
	}
	
	private void bindIfNotBound(){
		if(mSettings.isServiceRunning() && mDetectorService == null){
			boolean status = bindService(new Intent(ExerciseDetectorActivity.this, DetectorService.class), mDetectorConnection, 0);
		}
	}
	
	private void unbindDetectorService(){
		// if we are bound to the service first unbind
		if(mDetectorService != null){
			unbindService(mDetectorConnection);
			mDetectorService = null;
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();		
	}
	
	@Override
	protected void onPause() {		
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		unbindDetectorService();
		
		super.onDestroy();
	}
}
