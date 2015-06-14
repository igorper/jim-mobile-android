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
    private int trainee_id;
    private Integer trainer_id;
	private int training_id;
	private Date start_time;
	private Date end_time;
	private int rating;
	private String comment;
	private List<SeriesExecution> series_executions;

	public static Measurement create(Training training, int traineeId) {
		Measurement retVal = new Measurement();
		retVal.comment = training.getTrainingComment();
		retVal.start_time = training.getTrainingStarted();
		retVal.end_time = training.getTrainingEnded();
		retVal.rating = training.getTrainingRating();
		retVal.series_executions = training.getSeriesExecutions();
		retVal.training_id = training.getTrainingId();
        retVal.trainee_id = traineeId;

		return retVal;

	}
}
