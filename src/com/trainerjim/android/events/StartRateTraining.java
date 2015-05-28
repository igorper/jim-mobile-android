package com.trainerjim.android.events;

import android.content.Context;

import com.trainerjim.android.entities.Training;

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
