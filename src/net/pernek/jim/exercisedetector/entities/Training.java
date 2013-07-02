package net.pernek.jim.exercisedetector.entities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This class holds the Training information and is used for management of all
 * training data.
 * 
 * @author Igor
 * 
 */
public class Training {

	/***********************
	 * Fields deserialized from server data;
	 ***********************/
	private int id;
	private String name;
	private List<Exercise> exercises;

	/************************
	 * Fields deserialized from local data.
	 ************************/

	/**
	 * Holds {@code exercise} indices for exercise that still have to be
	 * performed. The first element is the index of the current exercise. If
	 * this list is empty this means there are no more exercises planned for
	 * this training.
	 */
	private List<Integer> mExercisesToDo;

	/**
	 * Holds the ms timestamp (obtained by System.currentTimeMillis()) of the
	 * last start of the rest between exercises.
	 */
	private long mLastPauseStart;

	/**
	 * Holds the ms timestamp (obtained by System.currentTimeMillis()) of the
	 * start of the current exercise. It is set to -1 if exercise is currently
	 * not started (if we are resting).
	 */
	private long mExerciseStart;

	/**
	 * Date when the training was started. null if the training was not started
	 * yet.
	 */
	private Date mTrainingStarted;

	/**
	 * Date when the training was ended. null if the training was not started
	 * yet.
	 */
	private Date mTrainingEnded;

	/**
	 * Users rating of this training.
	 */
	private int mTrainingRating;

	/**
	 * Users comment of this training.
	 */
	private String mTrainingComment;

	/**
	 * A list of all currently executed series.
	 */
	private List<SeriesExecution> mSeriesExecutions;

	/**
	 * This method is called to start a new training. It initializes all
	 * exercises, timestamps, etc. NOTE: Nothing happens, if this method is
	 * called after the training was already started.
	 */
	public void startTraining() {
		if (mTrainingStarted != null) {
			return;
		}

		// initialize support structures, dates and timestamps
		mExercisesToDo = new ArrayList<Integer>();
		mSeriesExecutions = new ArrayList<SeriesExecution>();
		mTrainingStarted = Calendar.getInstance().getTime();
		mLastPauseStart = System.currentTimeMillis();
		mExerciseStart = -1;
		mTrainingRating = -1;

		// add all exercises to the ToDo list and initialize them
		for (int exerciseIndex = 0; exerciseIndex < exercises.size(); exerciseIndex++) {
			mExercisesToDo.add(exerciseIndex);
			exercises.get(exerciseIndex).initializeExercise();
		}
	}

	/**
	 * This method ends the current training (stores the end timestamp).
	 */
	public void endTraining() {
		mTrainingEnded = Calendar.getInstance().getTime();
	}

	/**
	 * Tells if we are currently in the rest state or not.
	 * 
	 * @return
	 */
	public boolean isCurrentRest() {
		return mExerciseStart == -1;
	}

	/**
	 * Returns the currently active exercise or null, if there are no more
	 * exercises left.
	 * 
	 * @return
	 */
	public Exercise getCurrentExercise() {
		return mExercisesToDo.size() == 0 ? null : exercises.get(mExercisesToDo
				.get(0));
	}

	/**
	 * Returns the rest time left for this exercise. Negative value means there
	 * is still some time to rest, positive that rest is already over and
	 * corresponds to overdue seconds.
	 * 
	 * @return
	 */
	public int calculateCurrentRestLeft() {
		long now = System.currentTimeMillis();
		long diff = getCurrentExercise().getCurrentSeries().getRestTime()
				* 1000 - (now - mLastPauseStart);
		return Math.round((float) diff / 1000);
	}

	/**
	 * Called to start each exercise. Only marks the exercise start timestamp.
	 */
	public void startExercise() {
		mExerciseStart = System.currentTimeMillis();
	}

	/**
	 * Called to end each exercise. Creates the SeriesExecution object for the
	 * current series.
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
		currentSeriesExecution.setDuration(calculateDurationInSeconds(
				mExerciseStart, exerciseEnd));
		currentSeriesExecution.exercise_type_id = currentExercise
				.getExerciseType().getId();
		currentSeriesExecution.num_repetitions = currentSeries
				.getNumberTotalRepetitions();
		currentSeriesExecution.weight = currentSeries.getWeight();

		mSeriesExecutions.add(currentSeriesExecution);

		// start new rest
		mLastPauseStart = System.currentTimeMillis();
		mExerciseStart = -1;
	}

	/**
	 * Calculates the duration between the start and end timestamp (both in
	 * miliseconds) and rounds it to seconds.
	 * 
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
	 * pushed to the end of the exercises queue. Nothing happens if there is
	 * only one exercise left.
	 */
	public void scheduleExerciseLater() {
		if (mExercisesToDo.size() > 1) {
			int newLastExercise = mExercisesToDo.get(0);
			mExercisesToDo.remove(0);
			mExercisesToDo.add(newLastExercise);
		}
	}
	
	/**
	 * @return <code>true</code> if the exercise can be scheduled for later, otherwise <code>false</code>.
	 */
	public boolean canScheduleLater(){
		return mExercisesToDo.size() > 1;
	}

	/**
	 * Moves to the next exercise. Nothing happens if there are no more
	 * exercises left.
	 */
	public void nextExercise() {
		Exercise current = getCurrentExercise();
		if (current != null) {
			mExercisesToDo.remove(0);
		}
	}

	/**
	 * Moves either to next series or to next exercise, if the current exercise
	 * has no series left. Nothing happens if there are no more exercises and
	 * series left.
	 */
	public void nextActivity() {
		Exercise current = getCurrentExercise();
		if (current != null && !current.moveToNextSeries()) {
			mExercisesToDo.remove(0);
		}
	}

	/**
	 * Gets the total number of series in this training.
	 * 
	 * @return
	 */
	public int getTotalSeriesCount() {
		int retVal = 0;
		for (Exercise ex : exercises) {
			retVal += ex.getAllSeriesCount();
		}

		return retVal;
	}

	/**
	 * Gets the number of series already performed in this training.
	 * 
	 * @return
	 */
	public int getSeriesPerformedCount() {
		int retVal = getTotalSeriesCount();
		for (int i = 0; i < mExercisesToDo.size(); i++) {
			retVal -= exercises.get(mExercisesToDo.get(i)).getSeriesLeftCount();
		}

		return retVal;
	}

	/**
	 * Gets the users training rating number.
	 * 
	 * @return
	 */
	public int getTrainingRating() {
		return mTrainingRating;
	}

	/**
	 * Sets the users training rating number.
	 * 
	 * @param value
	 */
	public void setTrainingRating(int value) {
		mTrainingRating = value;
	}

	/**
	 * Tells if training has already ended.
	 * 
	 * @return
	 */
	public boolean isTrainingEnded() {
		return mTrainingEnded != null;
	}

	/**
	 * Sets the users training comment.
	 * 
	 * @param text
	 */
	public void setTrainingComment(String text) {
		mTrainingComment = text;
	}

	/**
	 * Gets the users training comment.
	 * 
	 * @return
	 */
	public String getTrainingComment() {
		return mTrainingComment;
	}
	
	public int getTotalRepetitions(){
		return getCurrentExercise().getCurrentSeries().getNumberTotalRepetitions();
	}
	
	/** Gets the current repetition number.
	 * @return
	 */
	public int getCurrentRepetition(){
		return getCurrentExercise().getCurrentSeries().getCurrentRepetition();
	}
	
	/**
	 * Increases the current repetition number by 1.
	 */
	public void increaseCurrentRepetition(){
		getCurrentExercise().getCurrentSeries().increaseCurrentRepetition();
	}

	/** Gets the current series number for the current exercise.
	 * @return
	 */
	public int getCurrentSeriesNumber() {
		return getCurrentExercise().getCurrentSeriesNumber();
	}
	
	/** Gets the total series number for the current exercise.
	 * @return
	 */
	public int getTotalSeriesForCurrentExercise(){
		return getCurrentExercise().getAllSeriesCount();
	}
	
	/** Gets the total training duration in seconds.
	 * @return
	 */
	public int getTotalTrainingDuration(){
		int durationInSec = Math.round(((float)mTrainingEnded.getTime() - mTrainingStarted.getTime()) / 1000); 
		return durationInSec;
	}
	
	public int getActiveTrainingDuration(){
		int activeDuration = 0;
		for (SeriesExecution se : mSeriesExecutions) {
			activeDuration += se.getDuration();
		}
		
		return activeDuration;
	}
	
	public String getName(){
		return name;
	}

	public Measurement extractMeasurement() {
		Measurement retVal = new Measurement();
		retVal.comment = mTrainingComment;
		retVal.end_time = mTrainingEnded;
		retVal.rating = mTrainingRating;
		retVal.series_executions = mSeriesExecutions;
		retVal.start_time = mTrainingStarted;
		retVal.training_id = id;
		
		return retVal;
	}
	
	public Date getStartDate(){
		return mTrainingStarted;
	}
}