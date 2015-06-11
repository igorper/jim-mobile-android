package com.trainerjim.mobile.android.entities;

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
	private Long start_timestamp;
	private Long end_timestamp;

	public int series_id;
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
	 * @param seriesId
	 * @param numRepetitions
	 * @param weight
	 * @param restTime
	 * @param duration
	 * @param rating
	 * @return
	 */
	public static SeriesExecution create(int seriesId,
			int numRepetitions, int weight, int restTime, int duration,
			int rating) {
		return SeriesExecution.create(null, null, seriesId, numRepetitions,
				weight, restTime, duration, rating);
	}

	/**
	 * Creates the series execution object with acceleration sampling start and
	 * end timestamp available.
	 * 
	 * @param startTimestamp
	 * @param endTimestamp
	 * @param seriesId
	 * @param numRepetitions
	 * @param weight
	 * @param restTime
	 * @param duration
	 * @param rating
	 * @return
	 */
	public static SeriesExecution create(Long startTimestamp,
			Long endTimestamp, int seriesId, int numRepetitions,
			int weight, int restTime, int duration, int rating) {
		SeriesExecution retVal = new SeriesExecution();
		retVal.start_timestamp = startTimestamp;
		retVal.end_timestamp = endTimestamp;
		retVal.series_id = seriesId;
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
	
	public void setWeight(int weight){
		this.weight = weight;
	}
	
	public int getWeight(){
		return weight;
	}
	
	public void setRepetitions(int repetitions){
		this.num_repetitions = repetitions;
	}
	
	public int getRepetitions(){
		return num_repetitions;
	}

    public void setRating(int rating){
        this.rating = rating;
    }
}
