package net.pernek.jim.exercisedetector.entities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.pernek.jim.exercisedetector.DetectorSettings;
import net.pernek.jim.exercisedetector.database.TrainingContentProvider.TrainingPlan;

import android.content.Context;
import android.database.Cursor;
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
	/**
	 * This one is -1 if exercise is currently not started.
	 */
	private long mExerciseStart;
	private Date mTrainingStarted;
	private Date mTrainingEnded;
	private boolean mIsCurrentRest;
	private int mTrainingRating;
	private String mTrainingComment;
	private boolean mOverviewDissmised;

	private DetectorSettings mStorageHandle;
	private List<SeriesExecution> mSeriesExecutions;

	public void startTraining() {
		mExercisesToDo = new ArrayList<Integer>();
		mSeriesExecutions = new ArrayList<SeriesExecution>();
		mTrainingStarted = Calendar.getInstance().getTime();
		mLastPauseStart = System.currentTimeMillis();
		mExerciseStart = -1;
		mTrainingRating = -1;

		for (int exerciseIndex = 0; exerciseIndex < exercises.size(); exerciseIndex++) {
			mExercisesToDo.add(exerciseIndex);
			exercises.get(exerciseIndex).initializeExercise();
		}
	}
	
	public boolean isCurrentRest() {
		return mExerciseStart == -1;
	}

	public Exercise getCurrentExercise() {
		return mExercisesToDo.size() == 0 ? null : exercises.get(mExercisesToDo
				.get(0));
	}

	/**
	 * Returns the rest time left for this exercise. Negative value means there
	 * is still some time to rest, positive that rest is already over and marks
	 * the overdue seconds.
	 * 
	 * @return
	 */
	public int calculateCurrentRestLeft() {
		long now = System.currentTimeMillis();
		long diff = getCurrentExercise().getCurrentSeries().getRestTime()
				* 1000 - (now - mLastPauseStart);
		return Math.round((float) diff / 1000);
	}

	public void startExercise() {
		mExerciseStart = System.currentTimeMillis();
	}

	/**
	 * Saves data and tries to advance to the next series or exercise.
	 * 
	 * @return
	 */
	public void endExercise() {
		long exerciseEnd = System.currentTimeMillis();
		Exercise currentExercise = exercises.get(mExercisesToDo.get(0));
		Series currentSeries = currentExercise.getCurrentSeries();

		// create series execution
		SeriesExecution currentSeriesExecution = new SeriesExecution();
		currentSeriesExecution.rest_time = calculateDurationInSeconds(
				mLastPauseStart, mExerciseStart);
		currentSeriesExecution.duration = calculateDurationInSeconds(
				mExerciseStart, exerciseEnd);
		currentSeriesExecution.exercise_type_id = currentExercise
				.getExerciseType().getId();
		currentSeriesExecution.num_repetitions = currentSeries
				.getNumberRepetitions();
		currentSeriesExecution.weight = currentSeries.getWeight();
		
		mSeriesExecutions.add(currentSeriesExecution);

		// start new rest
		mLastPauseStart = System.currentTimeMillis();
		mExerciseStart = -1;
	}

	/** Calculates the duration between the start and end timestamp (both in miliseconds)
	 * and rounds it to seconds.
	 * @param startTimeInMs
	 * @param endTimeInMs
	 * @return
	 */
	private static int calculateDurationInSeconds(long startTimeInMs,
			long endTimeInMs) {
		return Math.round((float) (endTimeInMs - startTimeInMs) / 1000);
	}

	/**
	 * Schedules the current exercise to be performed later. Current exercise is
	 * pushed to the end of the exercises queue.
	 */
	public void scheduleExerciseLater() {
		if (mExercisesToDo.size() > 1) {
			int newLastExercise = mExercisesToDo.get(0);
			mExercisesToDo.remove(0);
			mExercisesToDo.add(newLastExercise);
		}
	}

	/**
	 * Moves to the next exercise.
	 */
	public void nextExercise() {
		Exercise current = getCurrentExercise();
		if (current != null) {
			mExercisesToDo.remove(0);
		}
	}

	/**
	 * Moves either to next series or to next exercise, if the current exercise
	 * has no series left.
	 */
	public void nextActivity() {
		Exercise current = getCurrentExercise();
		if (current != null && !current.moveToNextSeries()) {
			mExercisesToDo.remove(0);
		}
	}
	
	public int getTotalSeriesCount(){
		int retVal = 0;
		for (Exercise ex : exercises) {
			retVal += ex.getAllSeriesCount();
		}
		
		return retVal;
	}
	
	public int getSeriesPerformedCount(){
		int retVal = getTotalSeriesCount();
		for (int i = 0; i < mExercisesToDo.size(); i++) {
			retVal -= exercises.get(mExercisesToDo.get(i)).getSeriesLeftCount();
		}
		
		return retVal;
	}
	
	public int getTrainingRating(){
		return mTrainingRating;
	}
	
	public void setTrainingRating(int value){
		mTrainingRating = value;
	}
	
	public void endTraining(){
		mTrainingEnded = Calendar.getInstance().getTime();
	}
	
	public boolean isTrainingEnded(){
		return mTrainingEnded != null;
	}
	
	public void setTrainingComment(String text){
		mTrainingComment = text;
	}
	
	public String getTrainingComment(){
		return mTrainingComment;
	}
	
	public boolean isOverviewDissmised(){
		return mOverviewDissmised;
	}
	
	public void setOverviewDissmised(boolean value){
		mOverviewDissmised = value;
	}
}
