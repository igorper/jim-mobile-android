package com.trainerjim.android.timers;

import android.util.Log;

import com.trainerjim.android.R;
import com.trainerjim.android.TrainingActivity;
import com.trainerjim.android.entities.Training;
import com.trainerjim.android.util.Utils;

import java.io.IOException;

/**
 * This class encapsulates all the logic for creating and updating the get ready timer ui. It communicates
 * with the TrainingActivity, containing all the training state information and performing the ui
 * update logic.
 */
public class GetReadyTimer implements  Runnable {

    /**
     * Holds the start timestamp for the get ready interval. This one is not
     * stored inside the training plan, as if the activity restarts in the
     * middle of the training plan the get ready timer simply cancels and has to
     * be started again by the user. NOTE: Value -1 means the get reads timer
     * was not started yet.
     */
    private long mGetReadyStartTimestamp = -1;

    /**
    * Reference to the current training activity. This activity exposes all the relevant information
    * such as the current training plan, application context, an ui handler and a method to schedule
    * screen updates.
    */
    private TrainingActivity mTrainingActivity;

    public GetReadyTimer(TrainingActivity trainingActivity){
        mTrainingActivity = trainingActivity;
    }

    /**
     * Starts or stops the get ready timer.
     */
    public void toggleGetReadyStartTimestamp() {
        // rest -> exercise
        if (mGetReadyStartTimestamp == -1) {
            // initiate the get ready timer
            mGetReadyStartTimestamp = System.currentTimeMillis();
        } else {
            // or cancel it
            mGetReadyStartTimestamp = -1;
        }
    }

    /**
     * Returns true if get ready timer is started, otherwise false.
     * @return
     */
    public boolean isStarted(){
        return mGetReadyStartTimestamp != -1;
    }

    @Override
    public void run() {
        Training currentTraining = mTrainingActivity.getCurrentTraining();
        int secLeft = Math.round(Utils.GET_READY_INTERVAL
                - (float) (System.currentTimeMillis() - mGetReadyStartTimestamp)
                / 1000);

        if (secLeft > 0) {
            mTrainingActivity.updateGetReadyTimer(secLeft);
            mTrainingActivity.getUiHandler().postDelayed(this, Utils.UI_TIMER_UPDATE_RATE);
        } else {
            // mark the the timer is over
            mGetReadyStartTimestamp = -1;

            mTrainingActivity.getReadyTimerOver();
        }
    }
}
