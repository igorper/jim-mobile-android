package com.trainerjim.mobile.android.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.trainerjim.mobile.android.R;
import com.trainerjim.mobile.android.database.TrainingPlan;
import com.trainerjim.mobile.android.entities.Exercise;

import java.util.ArrayList;

/**
 * Created by igor on 26.05.15.
 */
public class TrainingAdapter extends ArrayAdapter<TrainingPlan> {
    private int mSelectedExercisePosition;

    public TrainingAdapter(Context context, ArrayList<TrainingPlan> trainingPlans) {
        super(context, 0, trainingPlans);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        TrainingPlan trainingPlan = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_row, parent, false);
        }

        // Lookup view for data population
        TextView tvExerciseName = (TextView) convertView.findViewById(R.id.textTrainingName);

        // Populate the data into the template view using the data object
        tvExerciseName.setText(trainingPlan.getName());

        // Return the completed view to render on screen
        return convertView;
    }
}
