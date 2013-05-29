package net.pernek.jim.exercisedetector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.pernek.jim.exercisedetector.alg.CircularArrayInt;
import net.pernek.jim.exercisedetector.alg.DetectedEvent;
import net.pernek.jim.exercisedetector.entities.TrainingPlan;
import net.pernek.jim.exercisedetector.util.Compress;
import net.pernek.jim.exercisedetector.util.Statistics;
import net.pernek.jim.exercisedetector.util.Utils;

import org.json.JSONException;

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

	private Context mApplicationContext;

	private HandlerThread mProcessThread;
	private Handler mThreadHandler;

	private int mSamplingInterval = 10;
	private int[] mLastSensorValues;
	private int mLastTimestamp;

	// we start with exercise and series 0
	private int mCurrentExerciseIdx = 0;
	private int mCurrentSeriesIdx = 0;

	private int mPossibleActivityStart = -1;

	private CircularArrayInt mBufferX;
	private CircularArrayInt mBufferY;
	private CircularArrayInt mBufferZ;
	private CircularArrayInt mBufferTstmp;
	private CircularArrayInt mTstmpsQueue;
	private CircularArrayInt mMeanQueue;
	private CircularArrayInt mCandidatesQueue;

	private List<DetectedEvent> mDetectedEvents;

	private String mSessionId;

	private TrainingPlan mCurrentTrainingPlan;

	private SensorListener() {
	}

	public static SensorListener create(String sessionId, Context context,
			int thresholdActive, int thresholdInactive, int expectedMean,
			int windowMain, int stepMain, int meanDistanceThreshold,
			int windowRemove, String jsonEncodedTrainingPlan,
			int currentExerciseIdx, int currentSeriesIdx) {
		SensorListener retVal = new SensorListener();
		// initialize sensor
		retVal.mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		retVal.mSensor = retVal.mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// store context for sending intents
		retVal.mApplicationContext = context;

		// store the exercise id used for specifying the underlying
		// files for saving the data
		retVal.mSessionId = sessionId;

		// create or read the training manifest
		String tempTrainingMainfestPath = Utils.getTrainingManifestFile(
				sessionId).getPath();

		if (new File(tempTrainingMainfestPath).exists()) {
			// if the training file is already stored on the reload it
			// (this happened when the service was killed and recovered)
			retVal.mCurrentTrainingPlan = TrainingPlan
					.readFromFile(tempTrainingMainfestPath);
		} else {
			// create the current training from the shared preferences
			try {
				retVal.mCurrentTrainingPlan = TrainingPlan
						.parseFromJson(jsonEncodedTrainingPlan);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}

			// save the file to disk as well (for the case the service crashes)
			retVal.mCurrentTrainingPlan
					.saveToTempFile(tempTrainingMainfestPath);
		}

		// create a handler thread to which sampled acceleration will
		// be delivered
		retVal.mProcessThread = new HandlerThread("ProcessThread",
				Thread.MAX_PRIORITY);

		// set algorithm parameters
		retVal.mThresholdActive = thresholdActive;
		retVal.mThresholdInactive = thresholdInactive;
		retVal.mExpectedMean = expectedMean;
		retVal.mWindowMain = windowMain;
		retVal.mStepMain = stepMain;
		retVal.mMeanDistanceThreshold = meanDistanceThreshold;
		retVal.mWindowRemove = windowRemove;
		retVal.mDetectedEvents = new ArrayList<DetectedEvent>();

		// set current series and exercise (for the ressurection case)
		retVal.mCurrentExerciseIdx = currentExerciseIdx;
		retVal.mCurrentSeriesIdx = currentSeriesIdx;

		retVal.initializeAlgorithmStructures();

		if (retVal.mSensor == null) {
			// TODO: this could actually be changed so the app will offer only
			// limited
			// functionality - without automation
			// (or functionality based on some other sensor)
			Log.e(TAG, "No acceleration sensor present, the app can not run.");
			return null;
		}

		return retVal;
	}

	public void openOutputFiles() throws IOException {
		
		// the files are initialized with autoflush on
		// (if facing performance problems some optimizations can be performed
		// here)
		mOutputWriter = new PrintWriter(new BufferedWriter(new FileWriter(
				Utils.getAccelerationFile(mSessionId), true)), true);

		mDetectedTimestampsWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(Utils.getTimestampsFile(mSessionId), true)), true);

		mInterpolatedWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(Utils
						.getInterpolatedAccelerationFile(mSessionId), true)), true);
	}

	public boolean startAccelerationSampling() {
		mSessionStart = 0;
		mDetectedEvents.clear();

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
		mCandidatesQueue = new CircularArrayInt(mWindowRemove);
	}

	public void closeOutputFiles() {
		if (mOutputWriter != null) {
			mOutputWriter.close();
		}

		if (mInterpolatedWriter != null) {
			mInterpolatedWriter.close();
		}

		if (mDetectedTimestampsWriter != null) {
			mDetectedTimestampsWriter.close();
		}
	}

	public boolean stopAccelerationSampling() {
		mSensorManager.unregisterListener(this);

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

	public void detectExerciseState(int[] values, int timestamp)
			throws Exception {
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
				boolean lastCandidate = mCandidatesQueue.dequeue() == 1;
				mCandidatesQueue.enqueue(binaryCandidate ? 1 : 0);
				mTstmpsQueue.enqueue(curTstmp);

				if (!lastCandidate && mCandidatesQueue.first() == 1) {
					int[] tempCand = mCandidatesQueue.getFullValues();
					int detectedPositives = 0;
					for (int i = tempCand.length - 1; i >= 0; i--) {
						detectedPositives += tempCand[i];
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
				mCandidatesQueue.enqueue(binaryCandidate ? 1 : 0);
				mTstmpsQueue.enqueue(curTstmp);
			}

			if (mPossibleActivityStart >= 0 && !binaryCandidate) {
				exerciseStateChanged(false, curTstmp);
				mPossibleActivityStart = -1;
			}
		}
	}

	public void onSensorChanged(int[] values, int timestamp) {
		try {
			// first save the value without any interpolation
			mOutputWriter.println(String.format("%d,%d,%d,%d", values[0],
					values[1], values[2], timestamp));

			Queue<int[]> processingBuffer = interpolate(values, timestamp);

			while (!processingBuffer.isEmpty()) {
				int[] value = processingBuffer.poll();

				mInterpolatedWriter.println(String.format("%d,%d,%d,%d",
						value[0], value[1], value[2], value[3]));

				int[] sensorValues = new int[3];
				System.arraycopy(value, 0, sensorValues, 0, 3);

				detectExerciseState(sensorValues, value[3]);
			}
		} catch (Exception ex) {
			Log.w(TAG, ex.getMessage());
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

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

			// convert timestamp to miliseconds
			int timestamp = (int) ((event.timestamp - mSessionStart) / 1000000);

			onSensorChanged(values, timestamp);
		}
	}

	public List<DetectedEvent> getDetectedEvents() {
		return Collections.unmodifiableList(mDetectedEvents);
	}

	private void exerciseStateChanged(boolean isExercise, int timestamp) {

		// save to memory
		mDetectedEvents.add(new DetectedEvent(isExercise, timestamp));

		// write to file
		// TODO: this if is only used because unit testing fails without
		// it (start is never called during unit testing as we don't want the
		// accelerometer data to be collected) - in the future I should think
		// this
		// through one more time
		if (mDetectedTimestampsWriter != null) {
			mDetectedTimestampsWriter.println(Boolean.toString(isExercise)
					+ "," + Integer.toString(timestamp) + ","
					+ Integer.toString(mCurrentExerciseIdx) + ","
					+ Integer.toString(mCurrentSeriesIdx));
		}

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

		/*
		 * Log.d(TAG, "SensorProcessor.exerciseStatCh: " +
		 * Long.toString(Thread.currentThread().getId()));
		 */
	}

	public boolean compileForUpload(String sessionId) {
		Compress comp = new Compress(new String[] {
				Utils.getAccelerationFile(sessionId).getPath(),
				Utils.getTimestampsFile(sessionId).getPath() }, new File(
				Utils.getUploadDataFolderFile(), sessionId).getPath());

		// TODO: when the compilation is finished compiled files can be deleted

		return comp.zip();
	}

	public void updateTrainingPlan() {
		mCurrentTrainingPlan = TrainingPlan.readFromFile(Utils
				.getTrainingManifestFile(mSessionId).getPath());
	}

	// returns true if moving to the next activity is possible otherwise false
	public boolean moveToNextActivity() {
		if (mCurrentSeriesIdx + 1 < mCurrentTrainingPlan.getExercises()
				.get(mCurrentExerciseIdx).getSeries().size()) {
			mCurrentSeriesIdx++;
		} else {
			mCurrentSeriesIdx = 0;

			// we have move to the next exercise
			if (mCurrentExerciseIdx + 1 < mCurrentTrainingPlan.getExercises()
					.size()) {
				mCurrentExerciseIdx++;
			} else {
				// last exercise done
				return false;
			}
		}

		return true;
	}

	public TrainingPlan getCurrentTrainingPlan() {
		return mCurrentTrainingPlan;
	}

	public int getCurrentExerciseIdx() {
		return mCurrentExerciseIdx;
	}
	
	public int getCurrentSeriesIdx() {
		return mCurrentSeriesIdx;
	}
}
