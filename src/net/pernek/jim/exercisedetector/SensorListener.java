package net.pernek.jim.exercisedetector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import net.pernek.jim.exercisedetector.UploadSessionActivity.ResponseReceiver;
import net.pernek.jim.exercisedetector.alg.ExerciseDetectionAlgorithm;
import net.pernek.jim.exercisedetector.alg.ExerciseDetectionAlgorithmListener;
import net.pernek.jim.exercisedetector.alg.ExerciseStateChange;
import net.pernek.jim.exercisedetector.alg.LinearSensorInterpolator;
import net.pernek.jim.exercisedetector.alg.Pla;
import net.pernek.jim.exercisedetector.alg.SensorInterpolator;
import net.pernek.jim.exercisedetector.alg.SensorInterpolatorListener;
import net.pernek.jim.exercisedetector.alg.SensorValue;
import net.pernek.jim.exercisedetector.alg.SensorValue.SensorType;
import net.pernek.jim.exercisedetector.alg.SlidingWindowPla;
import net.pernek.jim.exercisedetector.alg.StDevExerciseDetectionAlgorithm;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorListener implements SensorEventListener {
	private static final String TAG = Utils.getApplicationTag();
	
	private static final int SAMPLING_SLEEP_MS = 20;
	
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private SensorProcessor mSensorProcessor;
	
	private long mSessionStart;
	
	private SensorListener() {
	}

	public static SensorListener create(File outputFile, Context context) {
		SensorListener retVal = new SensorListener();
		retVal.mSensorProcessor = SensorProcessor.create(outputFile, context);
		retVal.mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		retVal.mSensor = retVal.mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (retVal.mSensor == null) {
			// this could actually be changed so the app will offer only limited
			// functionality - without automation
			// (or functionality based on some other sensor)
			Log.e(TAG, "No acceleration sensor present, the app can not run.");
			return null;
		}

		return retVal;
	}

	public boolean start() throws IOException {
		mSensorProcessor.start();
		
		// rework this (if false is returned no file should be created)
		return mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public boolean stop() {
		mSensorManager.unregisterListener(this);
		mSensorProcessor.stop();
		mSessionStart = 0;
		
		// this API is created with future implementations in mind (making it
		// and interface and allowing
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
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if (mSessionStart == 0) {
				mSessionStart = event.timestamp;
			}

			// normalize time to ms
			SensorValue val = SensorValue.create(
					SensorType.ACCELEROMETER_BUILTIN,
					new Float[] { Float.valueOf(event.values[0]),
							Float.valueOf(event.values[1]),
							Float.valueOf(event.values[2]) },
					(event.timestamp - mSessionStart) / 1000000);
			
			mSensorProcessor.push(val);
			try {
				Thread.sleep(SAMPLING_SLEEP_MS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Log.d(TAG, "SensorListener.onSensorChanged: " + Long.toString(Thread.currentThread().getId()));
		}
	}

}
