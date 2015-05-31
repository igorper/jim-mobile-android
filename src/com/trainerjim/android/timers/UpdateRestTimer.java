package com.trainerjim.android.timers;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;

import com.trainerjim.android.R;
import com.trainerjim.android.TrainingActivity;
import com.trainerjim.android.entities.Exercise;
import com.trainerjim.android.entities.Training;
import com.trainerjim.android.util.Utils;

/**
 * This class encapsulates all the logic for creating and updating the rest timer ui. It communicates
 * with the TrainingActivity, containing all the training state information and performing the ui
 * update logic.
 */
public class UpdateRestTimer implements Runnable {

    /**
     * Reference to the current training activity. This activity exposes all the relevant information
     * such as the current training plan, application context, an ui handler and a method to schedule
     * screen updates.
     */
    private TrainingActivity mTrainingActivity;

    /**
     * Holds the last value of the running rest timer. This field is used
     * when playing a notification at the end of rest interval.
     */
    private long mLastRestTimerValue;

    public UpdateRestTimer(TrainingActivity trainingActivity){
        mTrainingActivity = trainingActivity;
    }

    @Override
    public void run() {
        Training currentTraining = mTrainingActivity.getCurrentTraining();
        Exercise currentExercise = currentTraining.getCurrentExercise();

        int currentRest = currentExercise.getCurrentSeries()
                .getRestTime();
        int currentRestLeft = currentTraining.calculateCurrentRestLeft();

        mTrainingActivity.updateRestTimer(currentRestLeft);

        // play a sound if the rest interval got to 0
        if(!currentTraining.isFirstSeries() && currentRestLeft == 0 && currentRestLeft != mLastRestTimerValue) {
            try {
                MediaPlayer.create(mTrainingActivity.getApplicationContext(), R.raw.alert).start();
                ((Vibrator) mTrainingActivity.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mLastRestTimerValue = currentRestLeft;
        mTrainingActivity.getUiHandler().postDelayed(this, Utils.UI_TIMER_UPDATE_RATE);
    }
}
