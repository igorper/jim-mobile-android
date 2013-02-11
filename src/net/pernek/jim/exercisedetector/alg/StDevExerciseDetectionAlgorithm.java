package net.pernek.jim.exercisedetector.alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.pernek.jim.exercisedetector.Statistics;

public class StDevExerciseDetectionAlgorithm implements
		ExerciseDetectionAlgorithm {

	private List<ExerciseDetectionAlgorithmListener> mExerciseDetectionListeners = new ArrayList<ExerciseDetectionAlgorithmListener>();

	private float mFilterCoeff;
	private float mThresholdActive;
	private float mThresholdInactive;
	private float mExpectedMean;
	private int mMinDuration;
	private int mWindowMain;
	private int mStepMain;
	private int mMeanDistanceThreshold;
	private int mWindowRemove;
	
	private LimitedSizeQueue<Float> mBufferX;
	private LimitedSizeQueue<Float> mBufferY;
	private LimitedSizeQueue<Float> mBufferZ;
	private LimitedSizeQueue<Long> mBufferTstmp;
	private LimitedSizeQueue<Long> mTstmpsQueue;
	private LimitedSizeQueue<Boolean> mCandidatesQueue;
	private LimitedSizeQueue<Float> mMeanQueue;
	
	private long mPossibleActivityStart = -1;
	
	private StDevExerciseDetectionAlgorithm() {
	}

	public static StDevExerciseDetectionAlgorithm create(float filterCoeff,
			float thresholdActive, float thresholdInactive, float expectedMean,
			int minDuration, final int windowMain, int stepMain,
			int meanDistanceThreshold, int windowRemove) {
		StDevExerciseDetectionAlgorithm retVal = new StDevExerciseDetectionAlgorithm();
		retVal.mFilterCoeff = filterCoeff;
		retVal.mThresholdActive = thresholdActive;
		retVal.mThresholdInactive = thresholdInactive;
		retVal.mExpectedMean = expectedMean;
		retVal.mMinDuration = minDuration;
		retVal.mWindowMain = windowMain;
		retVal.mStepMain = stepMain;
		retVal.mMeanDistanceThreshold = meanDistanceThreshold;
		retVal.mWindowRemove = windowRemove;
		
		// initialize buffers
		retVal.mBufferX = LimitedSizeQueue.<Float>create(windowMain);
		retVal.mBufferY = LimitedSizeQueue.<Float>create(windowMain);
		retVal.mBufferZ = LimitedSizeQueue.<Float>create(windowMain);
		retVal.mBufferTstmp = LimitedSizeQueue.<Long>create(windowMain);
		retVal.mTstmpsQueue = LimitedSizeQueue.<Long>create(windowRemove);
		retVal.mCandidatesQueue = LimitedSizeQueue.<Boolean>create(windowRemove);
		retVal.mMeanQueue = LimitedSizeQueue.<Float>create(windowRemove);
		
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
	public void push(SensorValue newValue) {
		if(mBufferX.isEmpty()){
			mBufferX.add(newValue.getValues()[0]);
			mBufferY.add(newValue.getValues()[1]);
			mBufferZ.add(newValue.getValues()[2]);
			mBufferTstmp.add(newValue.getTimestamp());
			return;
		}
		
		float filtX = mFilterCoeff * mBufferX.getLast() + (1 - mFilterCoeff) * newValue.getValues()[0];
		float filtY = mFilterCoeff * mBufferY.getLast() + (1 - mFilterCoeff) * newValue.getValues()[1];
		float filtZ = mFilterCoeff * mBufferZ.getLast() + (1 - mFilterCoeff) * newValue.getValues()[2];
		
		mBufferX.add(filtX);
		mBufferY.add(filtY);
		mBufferZ.add(filtZ);
		mBufferTstmp.add(newValue.getTimestamp());
		
		if(mBufferX.isFull()){
			double curSdX = Statistics.stDev(new ArrayList<Float>(mBufferX));
			double curSdY = Statistics.stDev(new ArrayList<Float>(mBufferY));
			double curSdZ = Statistics.stDev(new ArrayList<Float>(mBufferZ));
			long curTstmp = (long)Statistics.mean(new ArrayList<Long>(mBufferTstmp));
			
			mMeanQueue.add((float)Statistics.mean(new ArrayList<Float>(mBufferZ)));
			
			mBufferX.removeFromHead(mStepMain);
			mBufferY.removeFromHead(mStepMain);
			mBufferZ.removeFromHead(mStepMain);
			mBufferTstmp.removeFromHead(mStepMain);
			
			boolean binaryX = curSdX >= mThresholdInactive;
			boolean binaryY = curSdY >= mThresholdInactive;
			boolean binaryZ = curSdZ >= mThresholdActive;
			
			boolean binaryCandidate = binaryZ && (!binaryX || !binaryY);
			
			if(mCandidatesQueue.isFull()){
				boolean lastCandidate = mCandidatesQueue.poll();
				mCandidatesQueue.add(binaryCandidate);
				mTstmpsQueue.add(curTstmp);
				
				if(!lastCandidate && mCandidatesQueue.peek()){
					Boolean[] tempCand = mCandidatesQueue.toArray(new Boolean[0]);
					int detectedPositives = 0;
					for (Boolean val : tempCand) {
						detectedPositives += val ? 1 : 0;
					}
					double meanZ = Statistics.mean(new ArrayList<Float>(mMeanQueue));
					if(detectedPositives == mCandidatesQueue.getMaxSize() && 
							(Math.abs(meanZ - mExpectedMean) < mMeanDistanceThreshold)){
						mPossibleActivityStart = 1;
						notifyExerciseDetectionListeners(ExerciseStateChange.create(ExerciseState.EXERCISE, mTstmpsQueue.peek()));
					}
				}
			} else {
				mCandidatesQueue.add(binaryCandidate);
				mTstmpsQueue.add(curTstmp);
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
}
