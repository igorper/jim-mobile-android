package net.pernek.jim.exercisedetector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import net.pernek.jim.exercisedetector.alg.ExerciseDetectionAlgorithm;
import net.pernek.jim.exercisedetector.alg.ExerciseDetectionAlgorithmListener;
import net.pernek.jim.exercisedetector.alg.ExerciseStateChange;
import net.pernek.jim.exercisedetector.alg.LinearSensorInterpolator;
import net.pernek.jim.exercisedetector.alg.Pla;
import net.pernek.jim.exercisedetector.alg.SensorInterpolator;
import net.pernek.jim.exercisedetector.alg.SensorInterpolatorListener;
import net.pernek.jim.exercisedetector.alg.SensorValue;
import net.pernek.jim.exercisedetector.alg.SlidingWindowPla;
import net.pernek.jim.exercisedetector.alg.StDevExerciseDetectionAlgorithm;
import net.pernek.jim.exercisedetector.alg.SensorValue.SensorType;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class SensorListener implements SensorEventListener, SensorInterpolatorListener, ExerciseDetectionAlgorithmListener {
	private static final String TAG = Utils.getApplicationTag();
	
	private static final int SAMPLING_SLEEP_MS = 20;
	
	private SensorManager mSensorManager;
	private Sensor mSensor;
	
	private long mSessionStart;
	
	private static final int SAMPLING_MS = 10;
	private static final double PLA_ERROR = 3;

	private static final float LAZY_FILTER_COEF = 0.8F;
	private static final float THRESHOLD_ACTIVE = 0.1F;
	private static final float THRESHOLD_INACTIVE = 5.3F; // 0.3 real; 5.3
															// testing
	private static final float EXPECTED_MEAN = 10.11F;
	private static final int WINDOW_MAIN = 160;
	private static final int STEP_MAIN = 80;
	private static final int MEAN_DISTANCE_THRESHOLD = 2;
	private static final int WINDOW_REMOVE = 2;
	

	// TODO those writers here are only used for testing
	// purposes and should be deleted
	private PrintWriter mDetectedTimestampsWriter;
	private PrintWriter mInterpolatedWriter;
	private PrintWriter mOutputWriter;
	private PrintWriter mFullWriter;
	
	private File mOutputFile;
	private Pla mPlaWritter;
	private SensorInterpolator mSensorInterpolator;
	private ExerciseDetectionAlgorithm mExerciseDetection;
	private Context mApplicationContext;
	
	private HandlerThread mProcessThread;
	private Handler mThreadHandler;
	
	private boolean mIsProcessing;
			
	private SensorListener() {
	}

	public static SensorListener create(File outputFile, Context context) {
		SensorListener retVal = new SensorListener();
		retVal.mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		retVal.mSensor = retVal.mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		retVal.mSensorInterpolator = LinearSensorInterpolator
				.create(SAMPLING_MS);
		retVal.mSensorInterpolator.addSensorInterpolatorListener(retVal);
		retVal.mPlaWritter = SlidingWindowPla.create(PLA_ERROR);
		retVal.mApplicationContext = context;
		retVal.mOutputFile = outputFile;	
		retVal.mProcessThread = new HandlerThread("ProcessThread", Thread.MAX_PRIORITY);
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
		mOutputWriter = new PrintWriter(new BufferedWriter(new FileWriter(
				mOutputFile, true)));
		String interpolatedFile = mOutputFile.getParent() + "/"
				+ mOutputFile.getName() + "_i";
		String plaFile = mOutputFile.getParent() + "/" + mOutputFile.getName()
				+ "_p";
		String fullFile = mOutputFile.getParent() + "/" + mOutputFile.getName()
				+ "_f";
		String detectedTimestampsFile = mOutputFile.getParent() + "/"
				+ mOutputFile.getName() + "_tstmps";

		mFullWriter = new PrintWriter(new BufferedWriter(new FileWriter(
				fullFile, true)));
		mInterpolatedWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(interpolatedFile, true)));
		mDetectedTimestampsWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(detectedTimestampsFile, true)));
		mPlaWritter.setOutputFile(plaFile);
		
		// this values are hardcoded for now (should be made more flexible
		// later)
		mExerciseDetection = StDevExerciseDetectionAlgorithm.create(
				LAZY_FILTER_COEF, THRESHOLD_ACTIVE, THRESHOLD_INACTIVE,
				EXPECTED_MEAN, WINDOW_MAIN, STEP_MAIN, MEAN_DISTANCE_THRESHOLD,
				WINDOW_REMOVE);
		mExerciseDetection.addExerciseDetectionListener(this);
		
		mProcessThread.start();
		
		mThreadHandler = new Handler(mProcessThread.getLooper()){
			public void handleMessage(Message msg) {
				int w = msg.what;
				// check here when the last acceleration was sampled
				
				Log.d(TAG, "SensorListener.pingHandle: " + Integer.toString(w) + " " + Long.toString(Thread.currentThread().getId()));
				
				//  check how those 'what' work
				if(w==1131){
					long currentTimestamp = System.currentTimeMillis() - mSessionStart;
					Long lastTimestamp = mExerciseDetection.getLastTimestamp();
					if(lastTimestamp != null && currentTimestamp - lastTimestamp > 500){
						/*mSensorInterpolator.push(
								SensorValue.create(SensorType.ACCELEROMETER_BUILTIN, 
										new Float[]{0f, 0f, 0f}, lastTimestamp + 1));
						mSensorInterpolator.push(
								SensorValue.create(SensorType.ACCELEROMETER_BUILTIN, 
										new Float[]{0f, 0f, 0f}, currentTimestamp));
										*/
					}
				}
				
				// repeat
				sendEmptyMessageDelayed(1131, 1000);
			};
		};
		mThreadHandler.sendEmptyMessageDelayed(1131, 1000);
		
		mIsProcessing = true;	
		
		// rework this (if false is returned no file should be created)
		return mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL, mThreadHandler);
	}

	public boolean stop() {
		mSensorManager.unregisterListener(this);
				
		mIsProcessing = false;
		mOutputWriter.close();
		mFullWriter.close();
		mPlaWritter.closeOutputFile();
		mInterpolatedWriter.close();
		mDetectedTimestampsWriter.close();
		
		mProcessThread.getLooper().quit();
		
		mExerciseDetection.removeExerciseDetectionListener(this);
		
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
			SensorValue newValue = SensorValue.create(
					SensorType.ACCELEROMETER_BUILTIN,
					new Float[] { Float.valueOf(event.values[0]),
							Float.valueOf(event.values[1]),
							Float.valueOf(event.values[2]) },
					(event.timestamp - mSessionStart) / 1000000);
			
			// we would need an external timer that would call the sensor processor to check if the 
			// e.g. there has been more than one second from the last acceleration event and change
			// the activity to rest (maybe, this can be implemented using messages and handler)
			// TODO: the problem is that accelerometer values are
			// not delivered when the acceleromter is not working
			// (a possible solution for this would be to check the 
			// current timestamp in this thread and generate empty
			// accelerometer values
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			mSensorInterpolator.push(newValue);
			
			// TODO: experimentally added timestamp when this message was processed
			mOutputWriter.println(newValue.getCsvString() + "," + Long.toString(System.currentTimeMillis()));
			mFullWriter.println(newValue.getFullCsvString());

			// check performance implications of the following
			mOutputWriter.flush();
			mFullWriter.flush();

			Log.d(TAG, "SensorListener.onSensorChanged: " + Long.toString(Thread.currentThread().getId()));
		}
		
		// SOME ADDITIONAL FUNCTIONALITY NOTES
		// we are detecting to states - exercise and rest
		// we are only showing a counter for the rest interval, so there is 
		// no problem if during the exercise state, while the user pauses, rest is detected
		// there is also no problem if rest is detected with some small lag - user won't be observing
		// the screen while exercising
		// thus 1 sec no sensor data threshold could easily be used for setting the activity to REST
	}

	@Override
	public void exerciseStateChanged(ExerciseStateChange newState) {
		mDetectedTimestampsWriter.println(newState.getNewState().toString()
				+ ", " + Long.toString(newState.getStateChangeTimestamp()));
		// broadcast current state to change the UI
		Intent broadcastIntent = new Intent();
		broadcastIntent
				.setAction(ExerciseDetectorActivity.ResponseReceiver.ACTION_EXERCISE_STATE_CHANGED);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(
				ExerciseDetectorActivity.ResponseReceiver.PARAM_STATE, newState
						.getNewState().ordinal());
		broadcastIntent.putExtra(
				ExerciseDetectorActivity.ResponseReceiver.PARAM_TIMESTAMP,
				newState.getStateChangeTimestamp());
		mApplicationContext.sendBroadcast(broadcastIntent);

		Log.d(TAG, "SensorProcessor.exerciseStatCh: " + Long.toString(Thread.currentThread().getId()));
	}

	@Override
	public void onNewInterpolatedValue(SensorValue newValue) {
		mExerciseDetection.push(newValue);
		mInterpolatedWriter.println(newValue.getCsvString());
		mPlaWritter.process(newValue);
		//Log.d(TAG, "SensorProcessor.onNewInterpVal: " + Long.toString(Thread.currentThread().getId()));
		
	}
}
