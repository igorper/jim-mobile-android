package com.trainerjim.mobile.android.entities;

/** This class contains information about executed series.
 * @author Igor
 *
 */
public class SeriesExecution {

	/***********************
	 * Fields deserialized from server data;
	 ***********************/
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
	 * @param seriesId
	 * @param numRepetitions
	 * @param weight
	 * @param restTime
	 * @param duration
	 * @param rating
	 * @return
	 */
	public static SeriesExecution create(int seriesId, int numRepetitions,
			int weight, int restTime, int duration, int rating) {
		SeriesExecution retVal = new SeriesExecution();
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
