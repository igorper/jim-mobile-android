package com.trainerjim.mobile.android.entities;

/** This class contains information about executed series.
 * @author Igor
 *
 */
public class SeriesExecution {

	/***********************
	 * Fields deserialized from server data;
	 ***********************/
	private int series_id;
    private int num_repetitions;
    private int weight;
    private int rest_time;
	private int duration_seconds;
    private int rating;

	private SeriesExecution() {

	}

	/**
	 * Creates the series execution object with no timestamps information
	 * available (this means no acceleration sampling was performed).
	 * @param seriesId
	 * @param numRepetitions
	 * @param weight
	 * @param restTime
	 * @param duration_seconds
	 * @param rating
	 * @return
	 */
	public static SeriesExecution create(int seriesId, int numRepetitions,
			int weight, int restTime, int duration_seconds, int rating) {
		SeriesExecution retVal = new SeriesExecution();
		retVal.series_id = seriesId;
		retVal.num_repetitions = numRepetitions;
		retVal.weight = weight;
		retVal.rest_time = restTime;
		retVal.duration_seconds = duration_seconds;
		retVal.rating = rating;

		return retVal;
	}

	/**
	 * Gets duration of the current series.
	 * 
	 * @return
	 */
	public int getDurationSeconds() {
		return duration_seconds;
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
