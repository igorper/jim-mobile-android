package com.trainerjim.android.entities;

/**
 * Contains series information.
 * 
 * @author Igor
 * 
 */
public class Series {
	
	public static final String GUIDANCE_TYPE_DURATION = "duration";
	public static final String GUIDANCE_TYPE_TEMPO = "tempo";
	public static final String GUIDANCE_TYPE_MANUAL = "manual";

	/***********************
	 * Fields deserialized from server data;
	 ***********************/
	private int id;
	private int repeat_count;
	private int rest_time;
	private int weight;
	private int duration_after_repetition;
	private int duration_up_repetition;
	private int duration_middle_repetition;
	private int duration_down_repetition;

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

	/**
	 * Returns <code>true</code> if repetition duration is specified, otherwise
	 * <code>false</code>. Repetition duration is specified when both repetition
	 * duration up and repetition duration are set (not 0). Repetition duration
	 * after and middle are optional and thus irrelevant for repetition duration
	 * specification status.
	 * 
	 * @return
	 */
	public boolean hasRepetitionDuration() {
		return duration_up_repetition != 0 && duration_down_repetition != 0;
	}
	
	/** Returns repetition up duration in ms or <code>0</code> if no duration up was set. 
	 * @return
	 */
	public int getRepetitionDurationUp(){
		return duration_up_repetition;
	}
	
	/** Returns repetition down duration in ms or <code>0</code> if no duration down was set.
	 * @return
	 */
	public int getRepetitionDurationDown(){
		return duration_down_repetition;
	}
	
	/** Returns ms duration after the repetition (between two successive repeititons).
	 * @return
	 */
	public int getRepetitionDurationAfter(){
		return duration_after_repetition;
	}
	
	/** Returns ms duration between up and down repetition duration.
	 * @return
	 */
	public int getRepetitionDurationMiddle(){
		return duration_middle_repetition;
	}
	
	/** Returns guidance type for this series.
	 * @return
	 */
	public String getGuidanceType(){
		// TODO: Add guidance type duration.
		return hasRepetitionDuration() ? GUIDANCE_TYPE_TEMPO : GUIDANCE_TYPE_MANUAL;
	}
}
