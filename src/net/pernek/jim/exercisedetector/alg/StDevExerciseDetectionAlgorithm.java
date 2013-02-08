package net.pernek.jim.exercisedetector.alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.pernek.jim.exercisedetector.Statistics;

public class StDevExerciseDetectionAlgorithm implements
		ExerciseDetectionAlgorithm {

	private List<ExerciseDetectionAlgorithmListener> mExerciseDetectionObservers = new ArrayList<ExerciseDetectionAlgorithmListener>();

	private float mFilterCoeff;
	private float mThresholdActive;
	private float mThresholdInactive;
	private float mExpectedMean;
	private int mMinDuration;
	private int mWindowMain;
	private int mStepMain;
	private int mMeanDistanceThreshold;
	private int mWindowRemove;

	
	private List<Boolean> mCandidates = new ArrayList<Boolean>();
	private List<Boolean> mThresholdedX = new ArrayList<Boolean>();
	private List<Boolean> mThresholdedY = new ArrayList<Boolean>();
	private List<Boolean> mThresholdedZ = new ArrayList<Boolean>();
	private List<Float> mMeanZ = new ArrayList<Float>();
	private List<Long> mTstmps = new ArrayList<Long>();
	private List<Float> mSdX = new ArrayList<Float>();
	private List<Float> mSdY = new ArrayList<Float>();
	private List<Float> mSdZ = new ArrayList<Float>();
	private List<Float> mOutputX = new ArrayList<Float>();
	private List<Float> mOutputY = new ArrayList<Float>();
	private List<Float> mOutputZ = new ArrayList<Float>();
	private List<Float> mBufferX = new ArrayList<Float>();
	private List<Float> mBufferY = new ArrayList<Float>();
	private List<Float> mBufferZ = new ArrayList<Float>();
	private List<Long> mBufferTstmps = new ArrayList<Long>();

	private StDevExerciseDetectionAlgorithm() {
	}

	public static StDevExerciseDetectionAlgorithm create(float filterCoeff,
			float thresholdActive, float thresholdInactive, float expectedMean,
			int minDuration, int windowMain, int stepMain,
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

		return retVal;
	}

	@Override
	public void addExerciseDetectionObserver(
			ExerciseDetectionAlgorithmListener observer) {

		if (!mExerciseDetectionObservers.contains(observer)) {
			mExerciseDetectionObservers.add(observer);
		}

	}

	@Override
	public void removeExerciseDetectionObserver(
			ExerciseDetectionAlgorithmListener observer) {
		if (mExerciseDetectionObservers.contains(observer)) {
			mExerciseDetectionObservers.remove(observer);
		}
	}

	private void notifyExerciseDetectinObservers(ExerciseState newState) {
		for (ExerciseDetectionAlgorithmListener observer : mExerciseDetectionObservers) {
			observer.exerciseStateChanged(newState);
		}
	}

	@Override
	public void push(SensorValue newValue) {
		if (mBufferX.size() == 0) {
			mBufferX.add(newValue.getValues()[0]);
			mBufferY.add(newValue.getValues()[1]);
			mBufferZ.add(newValue.getValues()[2]);
			mBufferTstmps.add(newValue.getTimestamp());

			mOutputX.add(newValue.getValues()[0]);
			mOutputY.add(newValue.getValues()[1]);
			mOutputZ.add(newValue.getValues()[2]);

			return;
		}

		int outputLastElementIdx = mOutputX.size() - 1;
		float filteredX = mFilterCoeff * mOutputX.get(outputLastElementIdx)
				+ (1 - mFilterCoeff) * newValue.getValues()[0];
		float filteredY = mFilterCoeff * mOutputY.get(outputLastElementIdx)
				+ (1 - mFilterCoeff) * newValue.getValues()[1];
		float filteredZ = mFilterCoeff * mOutputZ.get(outputLastElementIdx)
				+ (1 - mFilterCoeff) * newValue.getValues()[2];

		mOutputX.add(filteredX);
		mOutputY.add(filteredY);
		mOutputZ.add(filteredZ);

		mBufferX.add(filteredX);
		mBufferY.add(filteredY);
		mBufferZ.add(filteredZ);
		mBufferTstmps.add(newValue.getTimestamp());

		if (mBufferX.size() == mWindowMain) {
			float currentSdX = (float) Statistics.stDev(mBufferX);
			float currentSdY = (float) Statistics.stDev(mBufferY);
			float currentSdZ = (float) Statistics.stDev(mBufferZ);
			long currentTstmp = (long) Statistics.mean(mBufferTstmps);
			
			mSdX.add(currentSdX);
			mSdY.add(currentSdY);
			mSdZ.add(currentSdZ);
			mTstmps.add(currentTstmp);
			mMeanZ.add((float)Statistics.mean(mBufferZ));
			
			for(int a=0; a < mStepMain; a++){
				mBufferX.remove(0);
				mBufferY.remove(0);
				mBufferZ.remove(0);
				mBufferTstmps.remove(0);
			}
			
			boolean currentThresholdedX = currentSdX >= mThresholdInactive;
			boolean currentThresholdedY = currentSdY >= mThresholdInactive;
			boolean currentThresholdedZ = currentSdZ >= mThresholdActive;
			
			mThresholdedX.add(currentThresholdedX);
			mThresholdedY.add(currentThresholdedY);
			mThresholdedZ.add(currentThresholdedZ);
			
			boolean currentCandidate = currentThresholdedZ && (!currentThresholdedX || !currentThresholdedY);
			mCandidates.add(currentCandidate);
			
			int startIdx = mCandidates.size() - 1 - mWindowRemove;
			if(startIdx > 0 && mCandidates.get(startIdx - 1) && !mCandidates.get(startIdx)){
				int j = 2;
				while(startIdx - j >= 0){
					if(!mCandidates.get(startIdx-j) || startIdx - j == 0){
						if(mTstmps.get(startIdx) - mTstmps.get(startIdx - j) < mMinDuration){
							for(int k = startIdx - j; k <= startIdx - 1; k++ ){
								mCandidates.set(k, false);
							}
						}
						
						if(Math.abs(Statistics.mean(mMeanZ.subList(startIdx - j, startIdx - 1)) - mExpectedMean) > mMeanDistanceThreshold){
							for(int k = startIdx - j; k <= startIdx - 1; k++ ){
								mCandidates.set(k, false);
							}
						}
						break;
					}
					j++;
				}
			}
		}
	}

	@Override
	public List<Boolean> getDetectedExercises() {
		
		return Collections.unmodifiableList(mCandidates);
	}

	@Override
	public List<Long> getTimestamps() {
		
		return Collections.unmodifiableList(mTstmps);
	}
}
