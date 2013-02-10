package net.pernek.jim.exercisedetector.alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

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
	
		return retVal;
	}

	@Override
	public void addExerciseDetectionObserver(
			ExerciseDetectionAlgorithmListener observer) {

		if (!mExerciseDetectionListeners.contains(observer)) {
			mExerciseDetectionListeners.add(observer);
		}

	}

	@Override
	public void removeExerciseDetectionObserver(
			ExerciseDetectionAlgorithmListener observer) {
		if (mExerciseDetectionListeners.contains(observer)) {
			mExerciseDetectionListeners.remove(observer);
		}
	}

	private void notifyExerciseDetectionListeners(ExerciseStateChange newState) {
		for (ExerciseDetectionAlgorithmListener listener : mExerciseDetectionListeners) {
			listener.exerciseStateChanged(newState);
		}
	}
	
	
	@Override
	public void push(SensorValue newValue) {
		// implement a limited size queue
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
