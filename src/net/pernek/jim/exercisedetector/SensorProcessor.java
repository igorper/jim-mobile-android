package net.pernek.jim.exercisedetector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SensorProcessor implements SensorInterpolatorListener, ExerciseDetectionAlgorithmListener {
	
	private static final String TAG = Utils.getApplicationTag();
	
	private Queue<SensorValue> mWorkingQueue = new ConcurrentLinkedQueue<SensorValue>();
	
	private static final int SAMPLING_MS = 20;
	private static final double PLA_ERROR = 3;

	private static final float LAZY_FILTER_COEF = 0.8F;
	private static final float THRESHOLD_ACTIVE = 0.1F;
	private static final float THRESHOLD_INACTIVE = 5.3F; // 0.3 real; 5.3
															// testing
	private static final float EXPECTED_MEAN = 10.11F;
	private static final int WINDOW_MAIN = 320;
	private static final int STEP_MAIN = 160;
	private static final int MEAN_DISTANCE_THRESHOLD = 2;
	private static final int WINDOW_REMOVE = 1;
	
	private static final int PROCESSING_SLEEP_MS = 30;

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
	
	private Thread mWorkerThread;
	private boolean mIsProcessing;
	private Runnable mWorkerRunnable = new Runnable() {
		
		@Override
		public void run() {
			while(mIsProcessing){
				Log.d(TAG, "SensorProcessor.toProcess: " + Integer.toString(mWorkingQueue.size()));
				SensorValue newValue = mWorkingQueue.poll();
				if(newValue == null){
					try {
						Thread.sleep(PROCESSING_SLEEP_MS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				
				// TODO: the problem is that accelerometer values are
				// not delivered when the acceleromter is not working
				// (a possible solution for this would be to check the 
				// current timestamp in this thread and generate empty
				// accelerometer values
				
				mSensorInterpolator.push(newValue);
				mOutputWriter.println(newValue.getCsvString());
				mFullWriter.println(newValue.getFullCsvString());

				// check performance implications of the following
				mOutputWriter.flush();
				mFullWriter.flush();
				
				//Log.d(TAG, "SensorProcessor.run: " + Long.toString(Thread.currentThread().getId()));
			}
			
		}
	};

	private SensorProcessor(){}
	
	public static SensorProcessor create(File outputFile, Context context){
		SensorProcessor retVal = new SensorProcessor();
		retVal.mSensorInterpolator = LinearSensorInterpolator
				.create(SAMPLING_MS);
		retVal.mSensorInterpolator.addSensorInterpolatorListener(retVal);
		retVal.mPlaWritter = SlidingWindowPla.create(PLA_ERROR);
		retVal.mApplicationContext = context;
		retVal.mOutputFile = outputFile;
		
		return retVal;
	}
	
	public void start() throws IOException{
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
		
		mIsProcessing = true;
		
		mWorkerThread = new Thread(mWorkerRunnable);
		mWorkerThread.start();
	}
	
	public void stop(){
		mIsProcessing = false;
		mOutputWriter.close();
		mFullWriter.close();
		mPlaWritter.closeOutputFile();
		mInterpolatedWriter.close();
		mDetectedTimestampsWriter.close();

		mExerciseDetection.removeExerciseDetectionListener(this);
	
	}
	
	public void push(SensorValue newValue){
		mWorkingQueue.add(newValue);
	}

	@Override
	public void onNewInterpolatedValue(SensorValue newValue) {
		mExerciseDetection.push(newValue);
		mInterpolatedWriter.println(newValue.getCsvString());
		mPlaWritter.process(newValue);
		//Log.d(TAG, "SensorProcessor.onNewInterpVal: " + Long.toString(Thread.currentThread().getId()));
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

}
