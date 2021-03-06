package com.trainerjim.mobile.android.entities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.util.Log;

import com.trainerjim.mobile.android.util.Utils;

/**
 * This class holds the Training information and is used for management of all
 * training data.
 * 
 * @author Igor
 * 
 */
public class Training {

	private static final String TAG = Utils.getApplicationTag();

	/**
	 * Size of the buffer used for copying data between streams.
	 */
	private static final int COPY_BUFFER = 512000;

	/***********************
	 * Fields deserialized from server data;
	 ***********************/
	private int id;
	private String name;
	private List<Exercise> exercises;
    private boolean is_circular;

	/************************
	 * Fields deserialized from local data.
	 ************************/
    private int traineeId;

	/**
	 * Rating for the current series execution.
	 */
	private int mCurrentSeriesExecutionRating = -1;

	/**
	 * Holds the nanosec timestamp of the training start. Used for normalizing
	 * acceleration sensor data.
	 */
	private long mTrainingStartTimestamp;

	/**
	 * Holds the ms timestamp (obtained by System.currentTimeMillis()) of the
	 * last start of the rest between exercises.
	 */
	private long mLastPauseStart;

	/**
	 * Holds the nanosec timestamp (obtained by System.nanoTime()) of the start
	 * of the current exercise. It is set to -1 if exercise is currently not
	 * started (if we are resting).
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
     * Position of the currently selected exercise in the exercises list.
     */
    private int mSelectedExercisePosition;

    private List<Exercise> mExercisesLeft;

    public List<Exercise> getExercisesLeft(){ return mExercisesLeft; }

    public List<Exercise> getExercises(){ return exercises; }

    public Date getTrainingStarted() {
        return  mTrainingStarted;
    }

    public Date getTrainingEnded(){
        return  mTrainingEnded;
    }

    public List<SeriesExecution> getSeriesExecutions(){
        return mSeriesExecutions;
    }

    public int getTrainingId(){
        return id;
    }

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
        mSelectedExercisePosition = 0;
        mExercisesLeft = new ArrayList<Exercise>(exercises);
		mSeriesExecutions = new ArrayList<SeriesExecution>();
		mTrainingStarted = Calendar.getInstance().getTime();
		mLastPauseStart = System.currentTimeMillis();
		mExerciseStart = -1;
		mTrainingRating = -1;
		mTrainingStartTimestamp = System.nanoTime();

		// add all exercises to the ToDo list and initialize them
		for (int exerciseIndex = 0; exerciseIndex < exercises.size(); exerciseIndex++) {
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
		return mExercisesLeft.size() == 0 ? null : mExercisesLeft.get(mSelectedExercisePosition);
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

        // if this is the first exercises we should not count down any rest
		long diff = (isFirstSeries() ? 0 : getCurrentExercise().getCurrentSeries().getRestTime())
				* 1000 - (now - mLastPauseStart);
		return Math.round((float) diff / 1000);
	}

    public int calculateDurationLeft() {
        long now = System.currentTimeMillis();
        long diff = getCurrentExercise().getCurrentSeries().getNumberTotalRepetitions()
                * 1000 - (now - mExerciseStart);
        return Math.round((float) diff / 1000);
    }

    /**
     * This method tells if no series have yet been performed for this training plan and the plan is
     * still empty.
     * @return <code>true</code> if no series have yet been executed, otherwise <code>false</code>.
     */
    public boolean isFirstSeries(){
        return mSeriesExecutions.size() == 0;
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
		Exercise currentExercise = mExercisesLeft.get(mSelectedExercisePosition);
		Series currentSeries = currentExercise.getCurrentSeries();

        int duration = calculateDurationInSeconds(mExerciseStart,
                        exerciseEnd);

        // if the exercise was manual
        int executedRepetitions = currentExercise.getGuidanceType().equals(
                Exercise.GUIDANCE_TYPE_MANUAL) ?
                currentSeries.getNumberTotalRepetitions() :
                duration;

		// create series execution
        mSeriesExecutions.add(SeriesExecution
                .create(currentSeries.getId(),
                        executedRepetitions,
                        currentSeries.getWeight(),
                        calculateDurationInSeconds(mLastPauseStart,
                                mExerciseStart),
                        duration,
                        mCurrentSeriesExecutionRating));

		// reset the series execution rating for the next exercise
		mCurrentSeriesExecutionRating = -1;

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

    public void selectExercise(int exercisePosition) {
        mSelectedExercisePosition = exercisePosition;
    }

	/**
	 * Moves either to next series or to next exercise, if the current exercise
	 * has no series left. Nothing happens if there are no more exercises and
	 * series left.
	 */
	public void nextActivity() {
		Exercise current = getCurrentExercise();

        // no more exercises, nothing to do
        if(current == null){
            return;
        }

        boolean hasNextSeries = current.moveToNextSeries();

        // remove current exercise if it does not have any more series
		if (!hasNextSeries) {
            removeExercise(mSelectedExercisePosition);
		}

        if(is_circular){
            /**
             * If circular training move forward the next exercise and loop back to the first once
             * the last one is finished. The logic:
             * - if there is only one exericse -> just select it (to prevent divide by 0)
             * - if the exercise does not have any more series we don't need to do anything (the next
             * exercise becomes the current), except if the current exercise was at the last position
             * (in that case we need to loop through - to do first one)
             * - otherwise just increase the selected exercise count by one (or fall through to the
             * first position)
             */
            if(mExercisesLeft.size() == 1){
                mSelectedExercisePosition = 0;
            } else {
                boolean isLastExercise = mSelectedExercisePosition == (mExercisesLeft.size() -1);
                if(hasNextSeries || isLastExercise){
                    mSelectedExercisePosition =  (mSelectedExercisePosition % (mExercisesLeft.size() - 1) +
                            (isLastExercise ? 0 : 1));
                }

            }
        }

    }


    public void removeExercise(int position) {
        mExercisesLeft.remove(position);

        // if the last exercise was removed and there are still some exercises left we should
        // decrease the currently selected exercise position
        if(mSelectedExercisePosition > mExercisesLeft.size() - 1){
            mSelectedExercisePosition--;
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
		for (int i = 0; i < mExercisesLeft.size(); i++) {
			retVal -= mExercisesLeft.get(i).getSeriesLeftCount();
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

	/**
	 * Get the number of expected repetitions for the current series.
	 * 
	 * @return
	 */
	public int getTotalRepetitions() {
		return getCurrentExercise().getCurrentSeries()
				.getNumberTotalRepetitions();
	}

	/**
	 * Gets the current repetition number.
	 * 
	 * @return
	 */
	public int getCurrentRepetition() {
		return getCurrentExercise().getCurrentSeries().getCurrentRepetition();
	}

	/**
	 * Increases the current repetition number by 1.
	 */
	public void increaseCurrentRepetition() {
		getCurrentExercise().getCurrentSeries().increaseCurrentRepetition();
	}

	/**
	 * Gets the current series number for the current exercise.
	 * 
	 * @return
	 */
	public int getCurrentSeriesNumber() {
		return getCurrentExercise().getCurrentSeriesNumber();
	}

	/**
	 * Gets the total series number for the current exercise.
	 * 
	 * @return
	 */
	public int getTotalSeriesForCurrentExercise() {
		return getCurrentExercise().getAllSeriesCount();
	}

	/**
	 * Gets the total training duration in seconds.
	 * 
	 * @return
	 */
	public int getTotalTrainingDuration() {
		int durationInSec = Math
				.round((float) (mTrainingEnded.getTime() - mTrainingStarted
						.getTime()) / 1000);
		return durationInSec;
	}

	/**
	 * Gets the duration of the active time during training.
	 * 
	 * @return
	 */
	public int getActiveTrainingDuration() {
		int activeDuration = 0;
		for (SeriesExecution se : mSeriesExecutions) {
			activeDuration += se.getDurationSeconds();
		}

		return activeDuration;
	}

	/**
	 * Gets the name of the training.
	 * 
	 * @return
	 */
	public String getTrainingName() {
		return name;
	}

	/**
	 * Creates the measurement object containing the training information to be
	 * uploaded to the server.
	 * 
	 * @return
	 */
	public Measurement extractMeasurement(int traineeId) {
		Measurement retVal = Measurement.create(this, traineeId);

		return retVal;
	}

	/**
	 * Gets the filename of the corresponding zip file containing all the
	 * tranining data.
	 * 
	 * @return
	 */
	private String getZipFilename() {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(mTrainingStarted)
				+ ".zip";
	}

	/**
	 * Gets the filename of the file containing raw accelerometer data.
	 * 
	 * @return
	 */
	private String getRawFilename() {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(mTrainingStarted)
				+ ".csv";
	}

	/**
	 * Gets the handle to the zip file containing training information.
	 * 
	 * @return
	 */
	public File getZipFile(Context context) {
		return new File(Utils.getDataFolderFile(context), getZipFilename());
	}

	/**
	 * Gets the handle of the file containing raw accelerometer data.
	 * 
	 * @return
	 */
	public File getRawFile(Context context) {
		return new File(Utils.getDataFolderFile(context), getRawFilename());
	}

	/**
	 * Gets the timestamp of training start.
	 * 
	 * @return
	 */
	public long getTrainingStartTimestamp() {
		return mTrainingStartTimestamp;
	}

	/**
	 * Set the rating for the current series execution.
	 * 
	 * @param rating
	 */
	public void setCurrentSeriesExecutionRating(int rating) {
		mCurrentSeriesExecutionRating = rating;
	}

	/**
	 * Writes the training to a zip file. The handle to the output file is
	 * returned by the getZipFile method.
	 * 
	 * @param trainingManifestPartName
	 *            is the name of the training description part in the zip file
	 * @param rawDataPartName
	 *            is the name of the raw acceleration data part in the zip file
	 * @return
	 */
	public boolean zipToFile(String trainingManifestPartName,
			String rawDataPartName, boolean sampleAcceleration, Context context) {
		try {
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					new FileOutputStream(getZipFile(context))));

			byte trainingData[] = null;/*Utils.getGsonObject().toJson(extractMeasurement())
					.getBytes();*/


			ZipEntry entry = new ZipEntry(trainingManifestPartName);
			out.putNextEntry(entry);

			out.write(trainingData, 0, trainingData.length);

			entry = new ZipEntry(rawDataPartName);
			out.putNextEntry(entry);

            // copy file with acceleration values to zip if acceleration sampling is enabled
            // (otherwise, raw data part will simply be empty)
            // TODO: think about totaly removing the raw data part if no acceleration sampling
            // is enabled
            if(sampleAcceleration) {
                BufferedInputStream rawData = new BufferedInputStream(
                        new FileInputStream(getRawFile(context)));
                // FileInputStream rawData = new FileInputStream(getRawFile());
                byte[] data = new byte[COPY_BUFFER];

                Log.d(TAG, "Getting ready to copy raw data to zip!");
                int totalBytes = 0;
                int count;
                while ((count = rawData.read(data, 0, COPY_BUFFER)) != -1) {
                    out.write(data, 0, count);
                    totalBytes += count;
                }

                Log.d(TAG, String.format("Written %d bytes to raw zip", totalBytes));

                rawData.close();
            }
			out.close();

			return true;
		} catch (Exception e) {
			Log.e(TAG, "Error zipping training: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/** Returns the last series execution object if available.
	 * @return
	 */
	public SeriesExecution getLastSeriesExecution() {
		if(mSeriesExecutions == null){
			return null;
		}
		
		int sizeSe = mSeriesExecutions.size();
		return sizeSe == 0 ? null : mSeriesExecutions.get(sizeSe - 1);
	}

    public int getSelectedExercisePosition() {
        return mSelectedExercisePosition;
    }
}