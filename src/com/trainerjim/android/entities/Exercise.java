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

	/***********************
	 * Fields deserialized from server data;
	 ***********************/
	private int id;
	private int order;
	private String machine_setting;
	private ExerciseType exercise_type;
	private List<Series> series;

	/***********************
	 * Fields deserialized from local data;
	 ***********************/

	/**
	 * A list of series indices that still have to be performed.
	 */
	private List<Integer> mSeriesToDo;

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
}
