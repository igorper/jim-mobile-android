package com.trainerjim.android.entities;

/**
 * Contains series information.
 * 
 * @author Igor
 * 
 */
public class Series {
	
	/***********************
	 * Fields deserialized from server data;
	 ***********************/
	private int id;
	private int repeat_count;
	private int rest_time;
	private int weight;

	/***********************
	 * Fields deserialized from local data;
	 ***********************/
	private int mCurrentRepetition;

	/**
	 * Gets the planned number of repetitions for this series.
	 * 
	 * @return
	 */
	public int getNumberTotalRepetitions() {
		return repeat_count;
	}

	/**
	 * Gets the planned rest time for this series.
	 * 
	 * @return
	 */
	public int getRestTime() {
		return rest_time;
	}

	/**
	 * Gets the planned weight for this series.
	 * 
	 * @return
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Gets the current repetition number.
	 * 
	 * @return
	 */
	public int getCurrentRepetition() {
		return mCurrentRepetition;
	}

	/**
	 * Increases the current repetition number by one.
	 */
	public void increaseCurrentRepetition() {
		mCurrentRepetition++;
	}

	/**
	 * Initializes each series at the beginning of the training.
	 */
	public void initialize() {
		mCurrentRepetition = 1;
	}
}
