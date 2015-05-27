package com.trainerjim.android.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains exercise information.
 * 
 * @author Igor
 * 
 */
public class Exercise {
	
	public static final String GUIDANCE_TYPE_DURATION = "duration";
	public static final String GUIDANCE_TYPE_TEMPO = "tempo";
	public static final String GUIDANCE_TYPE_MANUAL = "manual";

	/***********************
	 * Fields deserialized from server data;
	 ***********************/
	private int id;
	private int order;
	private String machine_setting;
	private ExerciseType exercise_type;
	private List<Series> series;
	private float duration_after_repetition;
	private float duration_up_repetition;
	private float duration_middle_repetition;
	private float duration_down_repetition;
	private String guidance_type;

	/***********************
	 * Fields deserialized from local data;
	 ***********************/

	/**
	 * A list of series indices that still have to be performed.
	 */
	private List<Integer> mSeriesToDo;

    public int getId(){
        return id;
    }

	/**
	 * Gets Exercise type information.
	 * 
	 * @return
	 */
	public ExerciseType getExerciseType() {
		return exercise_type;
	}

	/**
	 * Gets the number of all series for this exercise.
	 * 
	 * @return
	 */
	public int getAllSeriesCount() {
		return series.size();
	}

	/**
	 * Gets the number of series left for this exercise.
	 * 
	 * @return
	 */
	public int getSeriesLeftCount() {
		return mSeriesToDo.size();
	}

	/**
	 * Initializes the exercise (creates support structures, adds series to the
	 * ToDo list).
	 */
	public void initializeExercise() {
		mSeriesToDo = new ArrayList<Integer>();

		for (int i = 0; i < series.size(); i++) {
			series.get(i).initialize();
			mSeriesToDo.add(i);
		}
	}

	/**
	 * Gets the current series. Returns null if there are no more series for
	 * this exercise.
	 * 
	 * @return
	 */
	public Series getCurrentSeries() {
		return mSeriesToDo.size() == 0 ? null : series.get(mSeriesToDo.get(0));
	}

	/**
	 * Moves to the next series. Returns true if this exercise contains some
	 * more series, otherwise false.
	 * 
	 * @return
	 */
	public boolean moveToNextSeries() {
		if (mSeriesToDo.size() == 0) {
			return false;
		}

		mSeriesToDo.remove(0);

		return mSeriesToDo.size() > 0;
	}

	/**
	 * Gets the number of the current series. Throws an IndexOutOfBounds
	 * exception if there are no more series for this exercise left.
	 * 
	 * @return
	 */
	public int getCurrentSeriesNumber() {
		return mSeriesToDo.get(0) + 1;
	}

	/**
	 * Returns the machine setting for this exercise or <code>null</code> if not
	 * set.
	 * 
	 * @return
	 */
	public String getMachineSetting() {
		return machine_setting;
	}
	
	/** Returns repetition up duration in seconds or <code>0</code> if no duration up was set. 
	 * @return
	 */
	public float getRepetitionDurationUp(){
		return duration_up_repetition;
	}
	
	/** Returns repetition down duration in seconds or <code>0</code> if no duration down was set.
	 * @return
	 */
	public float getRepetitionDurationDown(){
		return duration_down_repetition;
	}
	
	/** Returns duration after the repetition in seconds (between two successive repetitions).
	 * @return
	 */
	public float getRepetitionDurationAfter(){
		return duration_after_repetition;
	}
	
	/** Returns duration between up and down repetition in seconds.
	 * @return
	 */
	public float getRepetitionDurationMiddle(){
		return duration_middle_repetition;
	}
	
	/** Returns guidance type for this exercise.
	 * @return
	 */
	public String getGuidanceType(){
		return guidance_type;
	}

    /**
     * Returns the order of the exercise.
     * @return
     */
    public int getOrder() { return order; }
}
