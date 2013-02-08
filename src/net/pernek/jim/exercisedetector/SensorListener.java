package net.pernek.jim.exercisedetector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import net.pernek.jim.exercisedetector.alg.LinearSensorInterpolator;
import net.pernek.jim.exercisedetector.alg.Pla;
import net.pernek.jim.exercisedetector.alg.SensorInterpolator;
import net.pernek.jim.exercisedetector.alg.SensorInterpolatorListener;
import net.pernek.jim.exercisedetector.alg.SensorValue;
import net.pernek.jim.exercisedetector.alg.SensorValue.SensorType;
import net.pernek.jim.exercisedetector.alg.SlidingWindowPla;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorListener implements SensorEventListener, SensorInterpolatorListener {
	private static final String TAG = Utils.getApplicationTag();
	private static final int SAMPLING_MS = 10;
	private static final double PLA_ERROR = 3;
	
	// TODO those writers here are only used for testing
	// purposes and should be deleted
	private PrintWriter mInterpolatedWriter;
	private PrintWriter mOutputWriter;
	private PrintWriter mFullWriter;
	private SensorManager mSensorManager;
    private Sensor mSensor;
	private File mOutputFile;
	private Pla mPlaWritter;
	private SensorInterpolator mSensorInterpolator;
	private long mSessionStart;
    
	private SensorListener() { }
	
	public static SensorListener create(File outputFile, SensorManager sensorManager) {
		SensorListener retVal = new SensorListener();
		retVal.mSensorInterpolator = LinearSensorInterpolator.create(SAMPLING_MS);
		retVal.mSensorInterpolator.addSensorInterpolatorListener(retVal);
		retVal.mPlaWritter = SlidingWindowPla.create(PLA_ERROR);
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
		String interpolatedFile = mOutputFile.getParent() + "/" + mOutputFile.getName() + "_i";
		String plaFile = mOutputFile.getParent() + "/" + mOutputFile.getName() + "_p";
		String fullFile = mOutputFile.getParent() + "/" + mOutputFile.getName() + "_f";
		
		mFullWriter = new PrintWriter(new BufferedWriter(new FileWriter(fullFile, true)));
		mInterpolatedWriter = new PrintWriter(new BufferedWriter(new FileWriter(interpolatedFile, true)));
		mPlaWritter.setOutputFile(plaFile);
		
		// rework this (if false is returned no file should be created)
		return mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	public boolean stop(){
		mSensorManager.unregisterListener(this);
		mOutputWriter.close();
		mFullWriter.close();
		mPlaWritter.closeOutputFile();
		mInterpolatedWriter.checkError();
		
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
			if(mSessionStart == 0){
				mSessionStart = event.timestamp;
			}
			
			// normalize time to ms
			SensorValue val = SensorValue.create(SensorType.ACCELEROMETER_BUILTIN, new Float[]{Float.valueOf(event.values[0]), Float.valueOf(event.values[1]), Float.valueOf(event.values[2])}, (event.timestamp - mSessionStart)/1000000);
			mSensorInterpolator.push(val);
			mOutputWriter.println(val.getCsvString());
			mFullWriter.println(val.getFullCsvString());
			
			// check performance implications of the following
			mOutputWriter.flush();
			mFullWriter.flush();
		}
	}

	@Override
	public void onNewValue(SensorValue newValue) {
		mInterpolatedWriter.println(newValue.getCsvString());
		mPlaWritter.process(newValue);
	}

}
