package com.trainerjim.android;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.trainerjim.android.util.Utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;

public class AccelerationRecorder implements SensorEventListener {
	private static final String TAG = Utils.getApplicationTag();

	private SensorManager mSensorManager;
	private Sensor mSensor;

	private long mSessionStart;

	private PrintWriter mAccelerationWritter;
	private Context mApplicationContext;
	private HandlerThread mProcessThread;
	private Handler mThreadHandler;

	private AccelerationRecorder() {

	}

	public static AccelerationRecorder create(Context applicationContext) {
		AccelerationRecorder retVal = new AccelerationRecorder();
		retVal.mApplicationContext = applicationContext;
		retVal.mSensorManager = (SensorManager) retVal.mApplicationContext
				.getSystemService(Context.SENSOR_SERVICE);
		retVal.mSensor = retVal.mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// create a handler thread to which sampled acceleration will
		// be delivered
		retVal.mProcessThread = new HandlerThread("ProcessThread",
				Thread.MAX_PRIORITY);

		return retVal;
	}

	public void openOutput(File outputFile) throws IOException {
		mAccelerationWritter = new PrintWriter(new BufferedWriter(
				new FileWriter(outputFile,
						true)));
	}

	public void closeOutput() {
		if (mAccelerationWritter != null) {
			mAccelerationWritter.close();
		}
	}

	public void startAccelerationSampling(long sessionStart) {
		mSessionStart = sessionStart;

		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_FASTEST, mThreadHandler);
	}

	public void stopAccelerationSampling() {
		mSensorManager.unregisterListener(this);
		mProcessThread.quit();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (mAccelerationWritter != null) {
			long timestamp = (event.timestamp - mSessionStart) / 1000000;
			
			mAccelerationWritter.println(String.format("%f,%f,%f,%d",
					event.values[0], event.values[1], event.values[2],
					timestamp));
		}
	}

}