package net.pernek.jim.exercisedetector;

import net.pernek.jim.exercisedetector.SensorValue.SensorType;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity implements SensorEventListener {
	
	private static final String TAG = "MainActivity";
	
	private SensorInterpolator mAccelerationInterpolator;
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// TODO: a quick and hacky way to disabling screen orientation changes (and app reset) 
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		mAccelerationInterpolator = LinearSensorInterpolator.create(40);
		
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		
		Log.w(TAG, "OnCreate");
	}
	
	@Override
	protected void onDestroy() {
		mSensorManager.unregisterListener(this);
		
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
			mAccelerationInterpolator.push(
					SensorValue.create(SensorType.ACCELEROMETER_BUILTIN, event.values, event.timestamp));
		}
	}

}
