package com.trainerjim.android.entities;

/** This class contains information about executed series.
 * @author Igor
 *
 */
public class SeriesExecution {

	/***********************
	 * Fields deserialized from server data;
	 ***********************/
	/**
	 * Those two mark the bounding timestamps of the acceleration signal.
	 */
	private long start_timestamp;
	private long end_timestamp;

	public int exercise_type_id;
	public int num_repetitions;
	public int weight;
	public int rest_time;
	private int duration;
	public int rating;

	private SeriesExecution() {

	}

	/**
	 * Creates the series execution object with no timestamps information
	 * available (this means no acceleration sampling was performed).
	 * 
	 * @param exerciseTypeId
	 * @param numRepetitions
	 * @param weight
	 * @param restTime
	 * @param duration
	 * @param rating
	 * @return
	 */
	public static SeriesExecution create(int exerciseTypeId,
			int numRepetitions, int weight, int restTime, int duration,
			int rating) {
		return SeriesExecution.create(-1, -1, exerciseTypeId, numRepetitions,
				weight, restTime, duration, rating);
	}

	/**
	 * Creates the series execution object with acceleration sampling start and
	 * end timestamp available.
	 * 
	 * @param startTimestamp
	 * @param endTimestamp
	 * @param exerciseTypeId
	 * @param numRepetitions
	 * @param weight
	 * @param restTime
	 * @param duration
	 * @param rating
	 * @return
	 */
	public static SeriesExecution create(long startTimestamp,
			long endTimestamp, int exerciseTypeId, int numRepetitions,
			int weight, int restTime, int duration, int rating) {
		SeriesExecution retVal = new SeriesExecution();
		retVal.start_timestamp = startTimestamp;
		retVal.end_timestamp = endTimestamp;
		retVal.exercise_type_id = exerciseTypeId;
		retVal.num_repetitions = numRepetitions;
		retVal.weight = weight;
		retVal.rest_time = restTime;
		retVal.duration = duration;
		retVal.rating = rating;

		return retVal;
	}

	/**
	 * Gets duration of the current series.
	 * 
	 * @return
	 */
	public int getDuration() {
		return duration;
	}
}
