package com.trainerjim.mobile.android.events;

/**
 * Created by igor on 05.09.15.
 */
public class StartTrainingEvent {

    private int selectedTrainingId;

    public StartTrainingEvent(int selectedTrainingId){
        this.selectedTrainingId = selectedTrainingId;
    }

    public int getSelectedTrainingId(){
        return selectedTrainingId;
    }
}
