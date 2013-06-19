package net.pernek.jim.exercisedetector.entities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.text.format.Time;

/**
 * @author Igor
 * 
 */
public class Training {

	/**
	 * Fields deserialized from local data;
	 */
	private int id;
	private String name;
	private List<Exercise> exercises;

	/**
	 * Fields deserialized from local data.
	 */
	private int mCurrentExerciseToDoIndex;
	private int mCurrentSeriesIndex;
	private List<Integer> mExercisesToDo;
	private long mLastPauseStart;
	private Date mTrainingStarted;

	public Training() {

	}

	public void startTraining() {
		mCurrentExerciseToDoIndex = 0;
		mCurrentSeriesIndex = 0;
		mExercisesToDo = new ArrayList<Integer>();
		mTrainingStarted = Calendar.getInstance().getTime();
		mLastPauseStart = System.currentTimeMillis();

		for (int exerciseIndex = 0; exerciseIndex < exercises.size(); exerciseIndex++) {
			mExercisesToDo.add(exerciseIndex);
		}
	}

	/**
	 * Returns the rest time for the current series.
	 * 
	 * @return
	 */
	public int getCurrentRest() {
		return exercises.get(mExercisesToDo.get(mCurrentExerciseToDoIndex))
				.getSeries().get(mCurrentSeriesIndex).getRestTime();
	}

	/**
	 * Returns the rest time left for this exercise. Negative value means there
	 * is still some time to rest, positive that rest is already over and marks
	 * the overdue seconds.
	 * 
	 * @return
	 */
	public int getCurrentRestLeft() {
		long now = System.currentTimeMillis();
		long diff = getCurrentRest() * 1000 - (now - mLastPauseStart);
		return Math.round((float)diff / 1000);
	}

	public boolean moveToNextExercise() {
		mCurrentExerciseToDoIndex++;

		return true;
	}

	public boolean moveToNextSeries() {
		mCurrentSeriesIndex++;

		return true;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Exercise> getExercises() {
		return Collections.unmodifiableList(exercises);
	}
}
