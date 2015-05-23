package com.trainerjim.android.events;

import com.trainerjim.android.entities.Training;

/**
 * Created by igor on 23.05.15.
 */
public class StartExerciseEvent {

    private Training currentTraining;

    public StartExerciseEvent(Training training){
        this.currentTraining = training;
    }

    public Training getCurrentTraining(){
        return this.currentTraining;
    }
}
