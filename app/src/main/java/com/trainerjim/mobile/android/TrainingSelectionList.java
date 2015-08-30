package com.trainerjim.mobile.android;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.trainerjim.mobile.android.database.TrainingPlan;
import com.trainerjim.mobile.android.events.ReportProgressEvent;
import com.trainerjim.mobile.android.events.TrainingSelectedEvent;
import com.trainerjim.mobile.android.storage.PermanentSettings;
import com.trainerjim.mobile.android.ui.TrainingAdapter;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class TrainingSelectionList extends ListActivity {
	public static final String INTENT_EXTRA_SELECTED_TRAINING_KEY = "selected_training";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        PermanentSettings settings = PermanentSettings.create(PreferenceManager
                .getDefaultSharedPreferences(this));

	    setListAdapter(new TrainingAdapter(getApplicationContext(), new ArrayList<TrainingPlan>(TrainingPlan.getAll(settings.getUserId()))));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
        EventBus.getDefault().post(new TrainingSelectedEvent(((TrainingPlan)l.getAdapter().getItem(position)).getTrainingId()));
        finish();
	}
}
