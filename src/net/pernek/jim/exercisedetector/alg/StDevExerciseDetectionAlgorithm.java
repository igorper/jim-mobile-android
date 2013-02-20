package net.pernek.jim.exercisedetector.alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import android.util.Log;

import net.pernek.jim.exercisedetector.Statistics;
import net.pernek.jim.exercisedetector.Utils;

public class StDevExerciseDetectionAlgorithm implements
		ExerciseDetectionAlgorithm {
	
	private static final String TAG = Utils.getApplicationTag();

	private List<ExerciseDetectionAlgorithmListener> mExerciseDetectionListeners = new ArrayList<ExerciseDetectionAlgorithmListener>();

	private float mFilterCoeff;
	private float mThresholdActive;
	private float mThresholdInactive;
	private float mExpectedMean;
	private int mWindowMain;
	private int mStepMain;
	private int mMeanDistanceThreshold;
	private int mWindowRemove;
	
	private CircularArrayFloat mBufferX;
	private CircularArrayFloat mBufferY;
	private CircularArrayFloat mBufferZ;
	private CircularArrayLong mBufferTstmp;
	private CircularArrayLong mTstmpsQueue;
	private CircularArrayBoolean mCandidatesQueue;
	private CircularArrayFloat mMeanQueue;
		
	private long mPossibleActivityStart = -1;
	
	private StDevExerciseDetectionAlgorithm() {
	}

	public static StDevExerciseDetectionAlgorithm create(float filterCoeff,
			float thresholdActive, float thresholdInactive, float expectedMean,
			final int windowMain, int stepMain,
			int meanDistanceThreshold, int windowRemove) {
		StDevExerciseDetectionAlgorithm retVal = new StDevExerciseDetectionAlgorithm();
		retVal.mFilterCoeff = filterCoeff;
		retVal.mThresholdActive = thresholdActive;
		retVal.mThresholdInactive = thresholdInactive;
		retVal.mExpectedMean = expectedMean;
		retVal.mWindowMain = windowMain;
		retVal.mStepMain = stepMain;
		retVal.mMeanDistanceThreshold = meanDistanceThreshold;
		retVal.mWindowRemove = windowRemove;
		
		// initialize buffers
		retVal.mBufferX = new CircularArrayFloat(windowMain);
		retVal.mBufferY = new CircularArrayFloat(windowMain);
		retVal.mBufferZ = new CircularArrayFloat(windowMain);
		retVal.mBufferTstmp = new CircularArrayLong(windowMain);
		retVal.mTstmpsQueue = new CircularArrayLong(windowRemove);
		retVal.mCandidatesQueue = new CircularArrayBoolean(windowRemove);
		retVal.mMeanQueue = new CircularArrayFloat(windowRemove);
		
		return retVal;
	}

	@Override
	public void addExerciseDetectionListener(
			ExerciseDetectionAlgorithmListener listener) {

		if (!mExerciseDetectionListeners.contains(listener)) {
			mExerciseDetectionListeners.add(listener);
		}

	}

	@Override
	public void removeExerciseDetectionListener(
			ExerciseDetectionAlgorithmListener listener) {
		if (mExerciseDetectionListeners.contains(listener)) {
			mExerciseDetectionListeners.remove(listener);
		}
	}

	private void notifyExerciseDetectionListeners(ExerciseStateChange newState) {
		for (ExerciseDetectionAlgorithmListener listener : mExerciseDetectionListeners) {
			listener.exerciseStateChanged(newState);
		}
	}
	
	// TODO: Test this algorithm
	@Override
	public void push(SensorValue newValue) throws Exception {
		//Log.d(TAG, "Alg.push: " + Long.toString(newValue.getTimestamp()));
		if(mBufferX.isEmpty()){
			mBufferX.enqueue(newValue.getValues()[0]);
			mBufferY.enqueue(newValue.getValues()[1]);
			mBufferZ.enqueue(newValue.getValues()[2]);
			mBufferTstmp.enqueue(newValue.getTimestamp());
					
			SensorValue.storeToReuse(newValue);
			
			return;
		}
		
		float filtX = mFilterCoeff * mBufferX.last() + (1 - mFilterCoeff) * newValue.getValues()[0];
		float filtY = mFilterCoeff * mBufferY.last() + (1 - mFilterCoeff) * newValue.getValues()[1];
		float filtZ = mFilterCoeff * mBufferZ.last() + (1 - mFilterCoeff) * newValue.getValues()[2];
		
		mBufferX.enqueue(filtX);
		mBufferY.enqueue(filtY);
		mBufferZ.enqueue(filtZ);
		mBufferTstmp.enqueue(newValue.getTimestamp());
		
		SensorValue.storeToReuse(newValue);
		
		if(mBufferX.isFull()){
			double curSdX = Statistics.stDev(mBufferX.getFullValues());
			double curSdY = Statistics.stDev(mBufferY.getFullValues());
			double curSdZ = Statistics.stDev(mBufferZ.getFullValues());
			long curTstmp = Statistics.mean(mBufferTstmp.getFullValues());
			
			mMeanQueue.enqueue(Statistics.mean(mBufferZ.getFullValues()));
			
			mBufferX.removeFromHead(mStepMain);
			mBufferY.removeFromHead(mStepMain);
			mBufferZ.removeFromHead(mStepMain);
			mBufferTstmp.removeFromHead(mStepMain);
			
			boolean binaryX = curSdX >= mThresholdInactive;
			boolean binaryY = curSdY >= mThresholdInactive;
			boolean binaryZ = curSdZ >= mThresholdActive;
			
			boolean binaryCandidate = binaryZ && (!binaryX || !binaryY);
			
			if(mCandidatesQueue.isFull()){
				boolean lastCandidate = mCandidatesQueue.dequeue();
				mCandidatesQueue.enqueue(binaryCandidate);
				mTstmpsQueue.enqueue(curTstmp);
				
				if(!lastCandidate && mCandidatesQueue.first()){
					boolean[] tempCand = mCandidatesQueue.getFullValues();
					int detectedPositives = 0;
					for (int i = tempCand.length - 1; i >= 0; i--){
						detectedPositives += tempCand[i] ? 1 : 0;
					}
				
					float meanZ = Statistics.mean(mMeanQueue.getFullValues());
					if(detectedPositives == mCandidatesQueue.size() && 
							(Math.abs(meanZ - mExpectedMean) < mMeanDistanceThreshold)){
						mPossibleActivityStart = 1;
						notifyExerciseDetectionListeners(ExerciseStateChange.create(ExerciseState.EXERCISE, mTstmpsQueue.first()));
					}
				}
			} else {
				mCandidatesQueue.enqueue(binaryCandidate);
				mTstmpsQueue.enqueue(curTstmp);
			}
			
			if(mPossibleActivityStart >= 0 && !binaryCandidate){
				notifyExerciseDetectionListeners(ExerciseStateChange.create(ExerciseState.REST, curTstmp));
				mPossibleActivityStart = -1;
			}
			
			
		}
	}

	@Override
	public List<Boolean> getDetectedExercises() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> getTimestamps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkIfProcessingIdle(Long currentTstmp) {
		/*if(currentTstmp - mTstmpsQueue.getLast() > 1000){
			// we could generate idle acc values here
			// this would be the cleanest approach for maintaining the alg
			
		}*/
		
	}

	@Override
	public Long getLastTimestamp() throws Exception {
		return mTstmpsQueue == null ? null : mTstmpsQueue.last();
	}
}
