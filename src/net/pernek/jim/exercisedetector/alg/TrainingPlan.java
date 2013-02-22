package net.pernek.jim.exercisedetector.alg;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TrainingPlan {

	private String mName;

	private List<Exercise> mExercises;

	private TrainingPlan() {
	}

	public static TrainingPlan create(String name) {
		TrainingPlan retVal = new TrainingPlan();
		retVal.mName = name;
		retVal.mExercises = new ArrayList<Exercise>();

		return retVal;
	}

	// we assume that a training plan is well formed
	// (has more than zero exercises, series, no data is missing,
	// etc.) - TODO: add error handling for illegal cases
	public static TrainingPlan parseFromJson(String jsonString)
			throws JSONException {
		JSONObject jTrainingPlan = new JSONObject(jsonString);

		TrainingPlan retVal = TrainingPlan.create(jTrainingPlan
				.getString("name"));

		JSONArray jExercises = jTrainingPlan.getJSONArray("exercises");
		for (int i = 0; i < jExercises.length(); i++) {
			JSONObject jCurrentExercise = jExercises.getJSONObject(i);
			JSONArray jSeries = jCurrentExercise.getJSONArray("series");
			Exercise newExercise = Exercise.create(jCurrentExercise
					.getString("name"));
			for (int j = 0; j < jSeries.length(); j++) {
				JSONObject jCurrentSeries = jSeries.getJSONObject(j);
				Series newSeries = Series.create(
						jCurrentSeries.getInt("repeat_count"),
						jCurrentSeries.getInt("weight"));
				newExercise.addSeries(newSeries);
			}
			retVal.addExercise(newExercise);
		}

		return retVal;
	}

	public void addExercise(Exercise newExercise) {
		mExercises.add(newExercise);
	}

	public String getName() {
		return mName;
	}

	public List<Exercise> getExercises() {
		// TODO make immutable
		return mExercises;
	}
}
