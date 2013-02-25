package net.pernek.jim.exercisedetector.entities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.pernek.jim.exercisedetector.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.Html.TagHandler;
import android.util.Log;

public class TrainingPlan {
	
	private static final String TAG = Utils.getApplicationTag();

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

	public boolean saveToTempFile(String path) {
		// first if file already exists remove it
		boolean status = new File(path).delete();

		// compile training plan to JSON
		JSONObject jTraining = new JSONObject();
		try {
			jTraining.put("name", mName);
			JSONArray jaExercies = new JSONArray();
			for (int i = 0; i < mExercises.size(); i++) {
				Exercise curExercise = mExercises.get(i);
				JSONObject jExercise = new JSONObject();
				jExercise.put("name", curExercise.getName());
				JSONArray jaSeries = new JSONArray();
				for (int j = 0; j < curExercise.getSeries().size(); j++) {
					Series curSeries = curExercise.getSeries().get(j);
					JSONObject jSeries = new JSONObject();
					jSeries.put("weight", curSeries.getWeight());
					jSeries.put("repeat_count", curSeries.getNumRepetitions());
					jaSeries.put(jSeries);
				}
				jExercise.put("series", jaSeries);
				jaExercies.put(jExercise);

			}
			jTraining.put("exercises", jaExercies);

		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}

		// save to file
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
			pw.append(jTraining.toString());
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	// make this method consistent with parse from JSON (it's another factory method)
	public static TrainingPlan readFromFile(String path) {
		// training plan not saved yet
		if (!new File(path).exists()) {
			return null;
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				// TODO: this may replace \r\n with \n
				sb.append("\n");
			}
			
			Log.d(TAG, sb.toString());
			
			// return parsed object
			return TrainingPlan.parseFromJson(sb.toString());

		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "read from file exception");
			return null;
		}
	}
}
