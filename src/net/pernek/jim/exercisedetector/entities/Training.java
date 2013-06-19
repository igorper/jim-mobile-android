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
	private List<Integer> mExercisesToDo;
	private long mLastPauseStart;
	private Date mTrainingStarted;

	public void startTraining() {
		mExercisesToDo = new ArrayList<Integer>();
		mTrainingStarted = Calendar.getInstance().getTime();
		mLastPauseStart = System.currentTimeMillis();

		for (int exerciseIndex = 0; exerciseIndex < exercises.size(); exerciseIndex++) {
			mExercisesToDo.add(exerciseIndex);
			exercises.get(exerciseIndex).initializeExercise();
		}
	}

	/**
	 * Returns the rest time for the current series.
	 * 
	 * @return
	 */
	public int getCurrentRest() {
		Exercise currentExercise = exercises.get(mExercisesToDo.get(0));
		return currentExercise.getSeries().get(currentExercise.getCurrentSeriesId()).getRestTime();
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
	
	public boolean hasNextExercise(){
		return mExercisesToDo.size() > 0;
	}

	public void moveToNextExercise() {
		if(mExercisesToDo.size() > 1){
			mExercisesToDo.remove(0);
		}
	}

	public boolean moveToNextSeriesOrExercise() {

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
