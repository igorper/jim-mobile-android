package net.pernek.jim.exercisedetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import net.pernek.jim.exercisedetector.alg.Exercise;
import net.pernek.jim.exercisedetector.alg.Series;
import net.pernek.jim.exercisedetector.alg.TrainingPlan;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

public class ExpandableListDemo extends ExpandableListActivity {

	private static final String EXERCISE_ITEM = "exercise.item";
	private static final String SERIES_ITEM = "series.item";
	
	private TrainingPlan mTrainingPlan;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
				
		try {
			mTrainingPlan = TrainingPlan.parseFromJson(DetectorSettings.create(
					PreferenceManager.getDefaultSharedPreferences(this))
					.getCurrentTrainingPlan());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "JSON problem", Toast.LENGTH_SHORT).show();
			return;
		}
 
		setListAdapter(createTrainingData());
	}
	
	private SimpleExpandableListAdapter createTrainingData(){
		return new SimpleExpandableListAdapter(
				this, createGroupList(), // Creating group List.
				R.layout.group_row, // Group item layout XML.
				new String[] { EXERCISE_ITEM }, // the key of group item.
				new int[] { R.id.row_name }, // ID of each group item.-Data
												// under the key goes into
												// this TextView.
				createChildList(), // childData describes second-level
										// entries.
				R.layout.child_row, // Layout for sub-level entries(second
									// level).
				new String[] { SERIES_ITEM }, // Keys in childData maps to
												// display.
				new int[] { R.id.grp_child } // Data under the keys above go
												// into these TextViews.
		);
	}

	private List<HashMap<String, String>> createGroupList() {
		List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		List<Exercise> exercises = mTrainingPlan.getExercises();
		for (int i = 0; i < exercises.size(); ++i) {
			HashMap<String, String> m = new HashMap<String, String>();
			String exerciseName = exercises.get(i).getName();
			m.put(EXERCISE_ITEM, exerciseName);
			result.add(m);
		}
		return result;
	}

	private List<List<HashMap<String, String>>> createChildList() {

		List<List<HashMap<String, String>>> result = new ArrayList<List<HashMap<String, String>>>();

		List<Exercise> exercises = mTrainingPlan.getExercises();
		for (int i = 0; i < exercises.size(); ++i) {
			List<Series> series = exercises.get(i).getSeries();
			List<HashMap<String, String>> secList = new ArrayList<HashMap<String, String>>();
			for (int n = 0; n < series.size(); n++) {
				HashMap<String, String> child = new HashMap<String, String>();
				Series currentSeries = series.get(n);
				String seriesString = String.format(
						"Series #%d (rep: %d, wgh: %d)", n + 1,
						currentSeries.getNumRepetitions(),
						currentSeries.getWeight());
				child.put(SERIES_ITEM, seriesString);
				secList.add(child);
			}
			result.add(secList);
		}
		return result;
	}

	public void onContentChanged() {
		super.onContentChanged();
	}

	/* This function is called on each child click */
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Toast.makeText(getApplicationContext(), "Inside onChildClick at groupPosition = "
						+ groupPosition + " Child clicked at position "
						+ childPosition, Toast.LENGTH_SHORT).show();
		
		// experimentally we increase num repetitions and weight for 10
		Series seriesClicked = mTrainingPlan.getExercises().get(groupPosition).getSeries().get(childPosition);
		seriesClicked.setNumRepetitions(seriesClicked.getNumRepetitions() + 10);
		seriesClicked.setWeight(seriesClicked.getWeight() + 10);
		
		// refresh activity adapter
		setListAdapter(createTrainingData());
		
		return true;
	}

	/* This function is called on expansion of the group */
	public void onGroupExpand(int groupPosition) {

	}
}