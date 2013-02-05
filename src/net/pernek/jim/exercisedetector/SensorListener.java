package net.pernek.jim.exercisedetector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorListener implements SensorEventListener {
	private static final String TAG = Utils.getApplicationTag();
	
	private PrintWriter mOutputWriter;
	private SensorManager mSensorManager;
    private Sensor mSensor;
	private File mOutputFile;
    
	private SensorListener() { }
	
	public static SensorListener create(File outputFile, SensorManager sensorManager) {
		SensorListener retVal = new SensorListener();
		retVal.mSensorManager = sensorManager;
		retVal.mOutputFile = outputFile;
		retVal.mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if(retVal.mSensor == null){
			// this could actually be changed so the app will offer only limited functionality - without automation
			// (or functionality based on some other sensor)
			Log.e(TAG, "No acceleration sensor present, the app can not run.");
			return null;
		}
				
		return retVal;
	}
	
	public boolean start() throws IOException{	
		mOutputWriter = new PrintWriter(new BufferedWriter(new FileWriter(mOutputFile, true)));
		
		return mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	public boolean stop(){
		mSensorManager.unregisterListener(this);
		mOutputWriter.close();
		
		// this API is created with future implementations in mind (making it and interface and allowing
		// implementations where stop can fail)
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			mOutputWriter.println(String.format("%d,%.3f,%.3f,%.3f", event.timestamp, event.values[0], event.values[1], event.values[2]));
			// check performance implications of the following
			mOutputWriter.flush();
		}
	}

}
