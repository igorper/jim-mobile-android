package net.pernek.jim.exercisedetector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.pernek.jim.exercisedetector.alg.CircularArrayBoolean;
import net.pernek.jim.exercisedetector.alg.CircularArrayInt;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class SensorListener implements SensorEventListener {
	private static final String TAG = Utils.getApplicationTag();

	private SensorManager mSensorManager;
	private Sensor mSensor;

	private long mSessionStart;

	private static final int PRECISION = 100000;

	private int mThresholdActive;
	private int mThresholdInactive;
	private int mExpectedMean;
	private int mWindowMain;
	private int mStepMain;
	private int mMeanDistanceThreshold;
	private int mWindowRemove;

	// TODO those writers here are only used for testing
	// purposes and should be deleted
	private PrintWriter mDetectedTimestampsWriter;
	private PrintWriter mInterpolatedWriter;
	private PrintWriter mOutputWriter;

	private File mOutputFile;
	private Context mApplicationContext;

	private HandlerThread mProcessThread;
	private Handler mThreadHandler;

	private int mSamplingInterval = 10;
	private int[] mLastSensorValues;
	private int mLastTimestamp;

	int mPossibleActivityStart = -1;

	private CircularArrayInt mBufferX;
	private CircularArrayInt mBufferY;
	private CircularArrayInt mBufferZ;
	private CircularArrayInt mBufferTstmp;
	private CircularArrayInt mTstmpsQueue;
	private CircularArrayInt mMeanQueue;
	private CircularArrayBoolean mCandidatesQueue;

	private SensorListener() {
	}

	public static SensorListener create(File outputFile, Context context) {
		SensorListener retVal = new SensorListener();
		retVal.mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		retVal.mSensor = retVal.mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		retVal.mApplicationContext = context;
		retVal.mOutputFile = outputFile;
		retVal.mProcessThread = new HandlerThread("ProcessThread",
				Thread.MAX_PRIORITY);
		
		if (retVal.mSensor == null) {
			// this could actually be changed so the app will offer only limited
			// functionality - without automation
			// (or functionality based on some other sensor)
			Log.e(TAG, "No acceleration sensor present, the app can not run.");
			return null;
		}

		return retVal;
	}

	public boolean start(int thresholdActive, int thresholdInactive,
			int expectedMean, int windowMain, int stepMain,
			int meanDistanceThreshold, int windowRemove) throws IOException {
		mSessionStart = 0;
		
		mOutputWriter = new PrintWriter(new BufferedWriter(new FileWriter(
				mOutputFile, true)));
		String interpolatedFile = mOutputFile.getParent() + "/"
				+ mOutputFile.getName() + "_i";
		String detectedTimestampsFile = mOutputFile.getParent() + "/"
				+ mOutputFile.getName() + "_tstmps";

		mInterpolatedWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(interpolatedFile, true)));
		mDetectedTimestampsWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(detectedTimestampsFile, true)));

		mThresholdActive = thresholdActive;
		mThresholdInactive = thresholdInactive;
		mExpectedMean = expectedMean;
		mWindowMain = windowMain;
		mStepMain = stepMain;
		mMeanDistanceThreshold = meanDistanceThreshold;
		mWindowRemove = windowRemove;
		
		initializeAlgorithmStructures();

		// TODO: rework this (if false is returned no file should be created)
		return mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_FASTEST, mThreadHandler);
	}

	private void initializeAlgorithmStructures() {
		mBufferX = new CircularArrayInt(mWindowMain);
		mBufferY = new CircularArrayInt(mWindowMain);
		mBufferZ = new CircularArrayInt(mWindowMain);
		mBufferTstmp = new CircularArrayInt(mWindowMain);
		mTstmpsQueue = new CircularArrayInt(mWindowRemove);
		mMeanQueue = new CircularArrayInt(mWindowRemove);
		mCandidatesQueue = new CircularArrayBoolean(mWindowRemove);
	}

	public boolean stop() {
		mSensorManager.unregisterListener(this);

		if (mOutputWriter != null) {
			mOutputWriter.close();
		}

		if (mInterpolatedWriter != null) {
			mInterpolatedWriter.close();
		}

		if (mDetectedTimestampsWriter != null) {
			mDetectedTimestampsWriter.close();
		}

		mProcessThread.quit();

		// this API is created with future implementations in mind (making it
		// and interface and allowing
		// implementations where stop can fail)
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public Queue<int[]> interpolate(int[] values, int timestamp) {
		int valSize = values.length;
		// interpolate values
		Queue<int[]> processingBuffer = new LinkedList<int[]>();
		if (mLastSensorValues != null) {
			int x = mLastTimestamp / mSamplingInterval * mSamplingInterval;
			if (x == mLastTimestamp) {
				x += mSamplingInterval;
			}

			while (x < timestamp) {
				// val + 1: timestamp added as well
				int[] newValues = new int[valSize + 1];
				int i;
				for (i = 0; i < valSize; i++) {
					newValues[i] = mLastSensorValues[i]
							+ (values[i] - mLastSensorValues[i])
							* (x - mLastTimestamp)
							/ (timestamp - mLastTimestamp);
				}
				newValues[i] = x;
				processingBuffer.add(newValues);

				// store those values to a buffer as well
				x += mSamplingInterval;
			}
		} 
		
		int[] lastElementToAdd = new int[valSize + 1];
		System.arraycopy(values, 0, lastElementToAdd, 0, valSize);
		lastElementToAdd[valSize] = timestamp; 
		
		processingBuffer.add(lastElementToAdd);

		mLastSensorValues = values;
		mLastTimestamp = timestamp;

		return processingBuffer;
	}

	public void onSensorChanged(int[] values, int timestamp) throws Exception {
		if (mBufferX.isEmpty()) {
			mBufferX.enqueue(values[0]);
			mBufferY.enqueue(values[1]);
			mBufferZ.enqueue(values[2]);
			mBufferTstmp.enqueue(timestamp);

			return;
		}

		// TODO: lazy integrator could be used here for some filtering
		int filtX = values[0];// (int)(LAZY_FILTER_COEF * mBufferX.last() + (1 -
								// LAZY_FILTER_COEF) * value[0]);
		int filtY = values[1];// (int)(LAZY_FILTER_COEF * mBufferY.last() + (1 -
								// LAZY_FILTER_COEF) * value[1]);
		int filtZ = values[2];// (int)(LAZY_FILTER_COEF * mBufferZ.last() + (1 -
								// LAZY_FILTER_COEF) * value[2]);

		mBufferX.enqueue(filtX);
		mBufferY.enqueue(filtY);
		mBufferZ.enqueue(filtZ);
		mBufferTstmp.enqueue(timestamp);

		if (mBufferX.isFull()) {
			int curSdX = (int) Statistics.stDev(mBufferX.getFullValues());
			int curSdY = (int) Statistics.stDev(mBufferY.getFullValues());
			int curSdZ = (int) Statistics.stDev(mBufferZ.getFullValues());
			int curTstmp = (int) Statistics.mean(mBufferTstmp.getFullValues());

			mMeanQueue.enqueue((int) Statistics.mean(mBufferZ.getFullValues()));

			mBufferX.removeFromHead(mStepMain);
			mBufferY.removeFromHead(mStepMain);
			mBufferZ.removeFromHead(mStepMain);
			mBufferTstmp.removeFromHead(mStepMain);

			boolean binaryX = curSdX >= mThresholdInactive;
			boolean binaryY = curSdY >= mThresholdInactive;
			boolean binaryZ = curSdZ >= mThresholdActive;

			boolean binaryCandidate = binaryZ && (!binaryX || !binaryY);

			if (mCandidatesQueue.isFull()) {
				boolean lastCandidate = mCandidatesQueue.dequeue();
				mCandidatesQueue.enqueue(binaryCandidate);
				mTstmpsQueue.enqueue(curTstmp);

				if (!lastCandidate && mCandidatesQueue.first()) {
					boolean[] tempCand = mCandidatesQueue.getFullValues();
					int detectedPositives = 0;
					for (int i = tempCand.length - 1; i >= 0; i--) {
						detectedPositives += tempCand[i] ? 1 : 0;
					}

					int meanZ = (int) Statistics.mean(mMeanQueue
							.getFullValues());
					if (detectedPositives == mCandidatesQueue.size()
							&& (Math.abs(meanZ - mExpectedMean) < mMeanDistanceThreshold)) {
						mPossibleActivityStart = 1;
						exerciseStateChanged(true, mTstmpsQueue.first());
					}
				}
			} else {
				mCandidatesQueue.enqueue(binaryCandidate);
				mTstmpsQueue.enqueue(curTstmp);
			}

			if (mPossibleActivityStart >= 0 && !binaryCandidate) {
				exerciseStateChanged(false, curTstmp);
				mPossibleActivityStart = -1;
			}
		}

		Log.d(TAG,
				"SensorListener.onSensorChanged: "
						+ Long.toString(Thread.currentThread().getId()));
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		try {
			Sensor sensor = event.sensor;
			if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				if (mSessionStart == 0) {
					mSessionStart = event.timestamp;
				}

				// convert values to integers
				int valSize = event.values.length;
				int[] values = new int[valSize];
				for (int i = 0; i < valSize; i++) {
					values[i] = (int) (event.values[i] * PRECISION);
				}
				int timestamp = (int) ((event.timestamp - mSessionStart) / 1000000);

				// first save the value without any interpolation
				mOutputWriter.println(String.format("%d,%d,%d,%d", values[0],
						values[1], values[2], timestamp));

				Queue<int[]> processingBuffer = interpolate(values, timestamp);

				while (!processingBuffer.isEmpty()) {
					int[] value = processingBuffer
							.poll();
					//processingBuffer.remove(processingBuffer.size() - 1);

					mInterpolatedWriter.println(String.format("%d,%d,%d,%d",
							value[0], value[1], value[2], value[3]));

					int[] sensorValues = new int[3];
					System.arraycopy(value, 0, sensorValues, 0, 3);

					onSensorChanged(sensorValues, value[3]);
				}
			}
		} catch (Exception ex) {
			Log.w(TAG, ex.getMessage());
		}
	}

	private void exerciseStateChanged(boolean isExercise, int timestamp) {
		mDetectedTimestampsWriter.println(Boolean.toString(isExercise) + ", "
				+ Integer.toString(timestamp));

		// broadcast current state to change the UI
		Intent broadcastIntent = new Intent();
		broadcastIntent
				.setAction(ExerciseDetectorActivity.ResponseReceiver.ACTION_EXERCISE_STATE_CHANGED);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(
				ExerciseDetectorActivity.ResponseReceiver.PARAM_STATE,
				isExercise);
		broadcastIntent.putExtra(
				ExerciseDetectorActivity.ResponseReceiver.PARAM_TIMESTAMP,
				timestamp);
		mApplicationContext.sendBroadcast(broadcastIntent);

		Log.d(TAG,
				"SensorProcessor.exerciseStatCh: "
						+ Long.toString(Thread.currentThread().getId()));
	}
}
