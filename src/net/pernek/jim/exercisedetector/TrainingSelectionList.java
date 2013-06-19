package net.pernek.jim.exercisedetector;

import net.pernek.jim.exercisedetector.R;
import net.pernek.jim.exercisedetector.database.TrainingContentProvider.TrainingPlan;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TrainingSelectionList extends ListActivity {
	public static final String INTENT_EXTRA_SELECTED_TRAINING_KEY = "selected_training";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		String[] projection = { TrainingPlan._ID, TrainingPlan.NAME};
	    String[] uiBindFrom = { TrainingPlan.NAME };
	    int[] uiBindTo = { R.id.textTrainingName };
	    Cursor trainings = managedQuery(
	            TrainingPlan.CONTENT_URI, projection, null, null, null);
	    CursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.list_row, trainings,
	            uiBindFrom, uiBindTo);
	    setListAdapter(adapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Toast.makeText(getApplicationContext(), Long.toString(id), Toast.LENGTH_SHORT).show();
		Intent data = new Intent();
		data.putExtra(INTENT_EXTRA_SELECTED_TRAINING_KEY,id);
		setResult(RESULT_OK, data);
		finish();
	}
}
