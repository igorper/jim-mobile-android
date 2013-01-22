package net.pernek.jim.exercisedetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import net.pernek.jim.exercisedetector.SensorValue.SensorType;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity implements SensorEventListener, ExerciseDetectionAlgorithmObserver, SensorInterpolatorObserver {
	
	private static final String TAG = "MainActivity";
	
	private SensorInterpolator mAccelerationInterpolator;
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	private ExerciseDetectionAlgorithm mExerciseDetectionAlgorithm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// TODO: a quick and hacky way to disabling screen orientation changes (and app reset) 
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		mExerciseDetectionAlgorithm = StDevExerciseDetectionAlgorithm.create(0.8F, 0.1F, 0.3F, 10.11F, 15000, 120, 20, 5, 10);
		mExerciseDetectionAlgorithm.addExerciseDetectionObserver(this);
		
		mAccelerationInterpolator = LinearSensorInterpolator.create(40);
		mAccelerationInterpolator.addSensorInterpolatorObserver(this);
		
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		
		
		Log.w(TAG, "OnCreate");
	}
	
	@Override
	protected void onDestroy() {
		mSensorManager.unregisterListener(this);
		mExerciseDetectionAlgorithm.removeExerciseDetectionObserver(this);
		mAccelerationInterpolator.removeSensorInterpolatorObserver(this);
		
		// TODO Auto-generated method stub
		super.onDestroy();
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			// convert timestamp to ms
			mAccelerationInterpolator.push(
					SensorValue.create(SensorType.ACCELEROMETER_BUILTIN, event.values, event.timestamp/1000000));
		}
	}

	@Override
	public void exerciseStateChanged(ExerciseState newState) {
		// TODO Notify new state to UI
		Log.w(TAG, "Test");
	}

	@Override
	public void onNewValue(SensorValue newValue) {
		mExerciseDetectionAlgorithm.push(newValue);
		
	}
}
