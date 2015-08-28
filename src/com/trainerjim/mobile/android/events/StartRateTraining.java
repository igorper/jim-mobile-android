package com.trainerjim.mobile.android.events;

import android.content.Context;

import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.util.TutorialHelper;

/**
 * Created by igor on 28.05.15.
 */
public class StartRateTraining {

    private Training mCurrentTraining;
    private Context mContext;

    public StartRateTraining(Training training, Context context){
        this.mCurrentTraining = training;
        this.mContext = context;
    }

    public Training getCurrentTraining(){
        return mCurrentTraining;
    }

    public Context getApplicationContext(){
        return mContext;
    }

}
