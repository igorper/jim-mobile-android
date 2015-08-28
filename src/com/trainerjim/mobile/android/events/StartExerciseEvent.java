package com.trainerjim.mobile.android.events;

import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.util.TutorialHelper;

/**
 * Created by igor on 23.05.15.
 */
public class StartExerciseEvent {

    private Training currentTraining;

    private TutorialHelper mTutorialHelper;

    public StartExerciseEvent(Training training, TutorialHelper tutorialHelper){
        this.currentTraining = training;
        this.mTutorialHelper = tutorialHelper;
    }

    public Training getCurrentTraining(){
        return this.currentTraining;
    }

    public TutorialHelper getTutorialHelper() {return  mTutorialHelper;}
}
