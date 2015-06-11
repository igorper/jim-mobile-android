package com.trainerjim.mobile.android;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.trainerjim.mobile.android.util.Utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class AccelerationRecorder implements SensorEventListener {
	private static final String TAG = Utils.getApplicationTag();

	public class AccelerationRecordingTimestamps {

		private long mStartTimestamp;

		private long mEndTimestamp;

		public AccelerationRecordingTimestamps(long startTimestamp,
				long endTimestamp) {
			mStartTimestamp = startTimestamp;
			mEndTimestamp = endTimestamp;
		}

		public long getStartTimestamp() {
			return mStartTimestamp;
		}

		public long getEndTimestamp() {
			return mEndTimestamp;
		}
	}

	private SensorManager mSensorManager;
	private Sensor mSensor;

	private long mSessionStart;
	private long mSeriesStartTimestamp = -1;
	private long mSeriesEndTimestamp = -1;

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

	public void startAccelerationSampling(long trainingStartTimestamp, File outputFile) throws IOException {
		mSessionStart = trainingStartTimestamp;

		mSeriesStartTimestamp = -1;
		mSeriesEndTimestamp = -1;
		
		mAccelerationWritter = new PrintWriter(new BufferedWriter(
				new FileWriter(outputFile, true)));

		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_FASTEST, mThreadHandler);
	}

	public AccelerationRecordingTimestamps stopAccelerationSampling() {
		mSensorManager.unregisterListener(this);
		mProcessThread.quit();
		
		// it might happend that the end timestamp won't be the timestamp of the the
		// last acceleration sample, as sampling runs on another thread
		// (however, that's not a problem so not thread synchronization is
		// performed)
		
		if (mAccelerationWritter != null) {
			mAccelerationWritter.close();
		}

		if (mSeriesStartTimestamp == -1) {
			// no acceleration sampling was performed
			return null;
		} else {
			AccelerationRecordingTimestamps retVal = new AccelerationRecordingTimestamps(
					mSeriesStartTimestamp, mSeriesEndTimestamp);

			return retVal;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (mAccelerationWritter != null) {
			long timestamp = (event.timestamp - mSessionStart) / 1000000;

			if (mSeriesStartTimestamp == -1) {
				mSeriesStartTimestamp = timestamp;
			}
			mSeriesEndTimestamp = timestamp;

			mAccelerationWritter.println(String.format("%f,%f,%f,%d",
					event.values[0], event.values[1], event.values[2],
					timestamp));
		}
	}
}
