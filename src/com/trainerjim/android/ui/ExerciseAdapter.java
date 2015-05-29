package com.trainerjim.android.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.trainerjim.android.R;
import com.trainerjim.android.entities.Exercise;
import com.trainerjim.android.entities.Training;

import java.util.ArrayList;

/**
 * Created by igor on 26.05.15.
 */
public class ExerciseAdapter extends ArrayAdapter<Exercise> {
    private int mSelectedExercisePosition;

    public ExerciseAdapter(Context context, ArrayList<Exercise> exercises) {
        super(context, 0, exercises);
    }

    public void setSelectedExercisePosition(int position){
        mSelectedExercisePosition = position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Exercise exercise = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.exercise_list_row, parent, false);
        }

        if(position == mSelectedExercisePosition){
            convertView.setBackgroundResource(R.color.exercise_menu_selected_background);
        } else {
            convertView.setBackgroundResource(R.color.exercise_menu_background);
        }

        // Lookup view for data population
        TextView tvExerciseName = (TextView) convertView.findViewById(R.id.tvExerciseName);
        TextView tvSeriesInfo = (TextView) convertView.findViewById(R.id.tvSeriesInfo);

        // Populate the data into the template view using the data object
        tvExerciseName.setText(exercise.getExerciseType().getShortName());
        tvSeriesInfo.setText(String.format("%d / %d", exercise.getCurrentSeriesNumber() - 1, exercise.getAllSeriesCount()));

        // Return the completed view to render on screen
        return convertView;
    }
}
