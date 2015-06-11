package com.trainerjim.mobile.android.entities;

import java.util.Date;
import java.util.List;

/**
 * This class contains information about a performed training.
 * 
 * @author Igor
 * 
 */
public class Measurement {

	/***********************
	 * Fields deserialized from server data;
	 ***********************/
	private int training_id;
	private Date start_time;
	private Date end_time;
	private int rating;
	private String comment;
	private List<SeriesExecution> series_executions;

	public static Measurement create(String trainingComment,
			Date trainingStartedTime, Date trainingEndedTime,
			int trainingRating, List<SeriesExecution> seriesExecutions,
			int trainingId) {
		Measurement retVal = new Measurement();
		retVal.comment = trainingComment;
		retVal.start_time = trainingStartedTime;
		retVal.end_time = trainingEndedTime;
		retVal.rating = trainingRating;
		retVal.series_executions = seriesExecutions;
		retVal.training_id = trainingId;

		return retVal;

	}
}
