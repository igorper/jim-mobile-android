package com.trainerjim.mobile.android.events;

import android.content.Context;

import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.util.TutorialHelper;

/**
 * Created by igor on 28.05.15.
 */
public class EndTrainingEvent {

    private Training mCurrentTraining;

    public EndTrainingEvent(Training training){
        this.mCurrentTraining = training;
    }

    public Training getCurrentTraining(){
        return mCurrentTraining;
    }


}
