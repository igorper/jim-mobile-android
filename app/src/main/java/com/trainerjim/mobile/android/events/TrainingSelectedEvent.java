package com.trainerjim.mobile.android.events;

import com.trainerjim.mobile.android.database.TrainingPlan;

/**
 * Created by igor on 13.06.15.
 */
public class TrainingSelectedEvent {

    private int selectedTrainingId;

    public TrainingSelectedEvent(int selectedTrainingId){
        this.selectedTrainingId = selectedTrainingId;
    }

    public int getSelectedTrainingId() {
        return selectedTrainingId;
    }
}
