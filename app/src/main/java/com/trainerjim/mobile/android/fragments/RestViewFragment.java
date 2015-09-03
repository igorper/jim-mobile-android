package com.trainerjim.mobile.android.fragments;

import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trainerjim.mobile.android.R;
import com.trainerjim.mobile.android.TrainingActivity;
import com.trainerjim.mobile.android.entities.Exercise;
import com.trainerjim.mobile.android.entities.Series;
import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.events.EndExerciseEvent;
import com.trainerjim.mobile.android.events.ToggleGetReadyEvent;
import com.trainerjim.mobile.android.events.EndRestEvent;
import com.trainerjim.mobile.android.events.StartRestEvent;
import com.trainerjim.mobile.android.ui.CircularProgressControl;
import com.trainerjim.mobile.android.ui.ExerciseAdapter;
import com.trainerjim.mobile.android.ui.ExerciseImagesPagerAdapter;
import com.trainerjim.mobile.android.util.TutorialHelper;
import com.trainerjim.mobile.android.util.Utils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by igor on 23.05.15.
 */
public class RestViewFragment extends Fragment implements View.OnClickListener {

    private Training mCurrentTraining;
    private TutorialHelper mTutorialHelper;

    private CircularProgressControl mCircularProgress;
    private ViewPager mViewPager;
    private TextView mTextRectUpperLine;
    private TextView mTextRectLowerLine;
    private LinearLayout mLayoutRectLowerLine;
    private LinearLayout mLayoutRectTrainingSelector;
    private TextView mSeriesInfoText;
    private LinearLayout mSeriesInformation;
    private RelativeLayout mBottomContainer;
    private LinearLayout mTrainingSelector;
    private ImageView mImageArrowSeriesInfo;
    private ImageView mInfoButton;
    private ImageView mExercisesListButton;

    private UpdateRestTimer mUpdateRestTimer = null;

    private GetReadyTimer mGetReadyTimer = null;

    private Handler mUiHandler = new Handler();

    private ExerciseImagesPagerAdapter mExerciseImagesPagerAdapter;

    public RestViewFragment(){}

    public RestViewFragment(StartRestEvent event){
        this.mCurrentTraining = event.getCurrentTraining();
        this.mTutorialHelper = event.getTutorialHelper();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.rest_view_fragment, container, false);

        mCircularProgress = (CircularProgressControl)fragmentView.findViewById(R.id.circularProgress);
        mTextRectUpperLine = (TextView)fragmentView.findViewById(R.id.text_rect_upper_line);
        mTextRectLowerLine = (TextView)fragmentView.findViewById(R.id.text_rect_lower_line);
        mLayoutRectLowerLine = (LinearLayout)fragmentView.findViewById(R.id.layout_rect_lower_line);
        mLayoutRectTrainingSelector = (LinearLayout) fragmentView.findViewById(R.id.layout_rect_training_selector);
        mSeriesInfoText = (TextView) fragmentView.findViewById(R.id.nextSeriesText);
        mSeriesInformation = (LinearLayout) fragmentView.findViewById(R.id.seriesInformation);
        mBottomContainer = (RelativeLayout) fragmentView.findViewById(R.id.bottomContainer);
        mTrainingSelector = (LinearLayout) fragmentView.findViewById(R.id.trainingSelector);
        mImageArrowSeriesInfo = (ImageView) fragmentView.findViewById(R.id.imageArrowSeriesInfo);
        mInfoButton = (ImageView) fragmentView.findViewById(R.id.info_button);
        mExercisesListButton = (ImageView) fragmentView.findViewById(R.id.exercises_button);

        // TODO: this has to move in this fragment out of the activity
        mViewPager = (ViewPager) getActivity().findViewById(R.id.pager);

        mCircularProgress.setOnClickListener(this);

        // TODO: update that we won't need to pass in the TrainingActivity. might be better to pass in
        // just a callback function
        mUpdateRestTimer = new UpdateRestTimer();
        mGetReadyTimer = new GetReadyTimer();

        EventBus.getDefault().register(this);

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateScreen();
    }

    @Override
    public void onPause() {
        super.onPause();

        mUiHandler.removeCallbacks(mGetReadyTimer);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    public void onEvent(EndExerciseEvent event){}

    // TODO: this should contain only the minimum code needed for redrawing the part of the UI
    // connected with get ready/rest timer
    private void updateScreen(){

        // TODO: encapsulate this or find a better way to do it

        Exercise curExercise = mCurrentTraining.getCurrentExercise();
        List<String> photoImages = curExercise.getExerciseType().getPhotoImages();

        mExerciseImagesPagerAdapter = new ExerciseImagesPagerAdapter(this.getActivity(), photoImages);

        // TODO: do we need to remove previous adapter?
        mViewPager.setAdapter(mExerciseImagesPagerAdapter);

        mCircularProgress.setInfoChairLevel(mCurrentTraining
                .getCurrentExercise().getMachineSetting());

        // first remove all existing callbacks
        mUiHandler.removeCallbacks(mUpdateRestTimer);
        mUiHandler.removeCallbacks(mGetReadyTimer);
        // mUiHandler.removeCallbacks(mUpdateExerciseTimer);

        // now show all the common information
        Series curSeries = curExercise.getCurrentSeries();
        mCircularProgress.setCurrentState(CircularProgressControl.CircularProgressState.REST);

        mCircularProgress.setOnClickListener(this);

        mTextRectUpperLine.setVisibility(View.GONE);

        // show short name if available
        String exerciseName = curExercise.getExerciseType().getName();

        // visualize exercise order (useful when traversing exercises)
        mTextRectLowerLine.setText(exerciseName);
        mLayoutRectLowerLine.setVisibility(View.VISIBLE);
        mLayoutRectTrainingSelector.setVisibility(View.GONE);

        if(curExercise.getGuidanceType().equals(Exercise.GUIDANCE_TYPE_DURATION)){
            int minutes = curSeries.getNumberTotalRepetitions() / 60;
            int seconds = curSeries.getNumberTotalRepetitions() - minutes * 60;
            mSeriesInfoText.setText(
                    String.format(
                            "Series %d (%d min %d sec, %d kg)",
                            curExercise.getCurrentSeriesNumber(),
                            minutes,
                            seconds,
                            curSeries.getWeight()));
        } else {
            mSeriesInfoText.setText(
                    String.format(
                            "Series %d (%d reps, %d kg)",
                            curExercise.getCurrentSeriesNumber(),
                            curSeries.getNumberTotalRepetitions(),
                            curSeries.getWeight()));
        }


        mSeriesInformation.setVisibility(View.VISIBLE);

        mCircularProgress.setTimerMessage("RESTING");

        // get ready timer was not started yet so show the rest timer
        if (mGetReadyTimer.isStarted()) {
            mCircularProgress.setRestMaxProgress(Utils.GET_READY_INTERVAL);
            mCircularProgress.setRestMinProgress(0);
            mCircularProgress.setTimerMessage("GET READY");

            mUiHandler.postDelayed(mGetReadyTimer, 0);
            //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        } else {
            int currentRest = curSeries.getRestTime();
            mCircularProgress.setRestMaxProgress(currentRest);
            mCircularProgress.setRestMinProgress(0);

            // also start the periodic timer to update the rest screen
            mUiHandler.postDelayed(mUpdateRestTimer, 0);

            // only in this state the user should be allowed to see the list of exercises
            // update the training plan based on the current state
            ExerciseAdapter adapter = new ExerciseAdapter(getActivity(), new ArrayList<>(mCurrentTraining.getExercisesLeft()));
            adapter.setSelectedExercisePosition(mCurrentTraining.getSelectedExercisePosition());

            //mDrawerExercisesList.setAdapter(adapter);
            //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }


        // set exercise and training progres bars
        mCircularProgress.setTrainingMaxProgress(mCurrentTraining
                .getTotalSeriesCount());
        mCircularProgress.setTrainingMinProgress(0);
        mCircularProgress.setTrainingProgressValue(mCurrentTraining
                .getSeriesPerformedCount());

        mCircularProgress.setExerciseMaxProgress(curExercise
                .getAllSeriesCount());
        mCircularProgress.setExerciseMinProgress(0);
        mCircularProgress.setExerciseProgressValue(curExercise
                .getAllSeriesCount() - curExercise.getSeriesLeftCount());

        // show the info button only if the get ready timer is not running
        mInfoButton.setVisibility(mGetReadyTimer.isStarted() ? View.INVISIBLE : View.VISIBLE);
        mExercisesListButton.setVisibility(mGetReadyTimer.isStarted() ? View.INVISIBLE : View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        mSeriesInformation.setVisibility(View.VISIBLE);
        mTrainingSelector.setVisibility(View.VISIBLE);
        // TODO: mSwipeControl.setVisibility(View.VISIBLE);
        mImageArrowSeriesInfo.setVisibility(View.VISIBLE);

        mTutorialHelper.showExerciseTutorial();
    }

    @Override
    public void onClick(View view) {
        if(mTutorialHelper.isTutorialActive()){
            return;
        }

        switch (view.getId()){
            case R.id.circularProgress: {
                boolean isGetReadyStarted = mGetReadyTimer.toggleGetReadyStartTimestamp();

                EventBus.getDefault().post(new ToggleGetReadyEvent(isGetReadyStarted));

                // TODO: get rid of this
                updateScreen();
                return;
            }
        }
    }

    /**
     * This class encapsulates all the logic for creating and updating the get ready timer ui. It communicates
     * with the TrainingActivity, containing all the training state information and performing the ui
     * update logic.
     */
    class GetReadyTimer implements  Runnable {

        /**
         * Holds the start timestamp for the get ready interval. This one is not
         * stored inside the training plan, as if the activity restarts in the
         * middle of the training plan the get ready timer simply cancels and has to
         * be started again by the user. NOTE: Value -1 means the get reads timer
         * was not started yet.
         */
        private long mGetReadyStartTimestamp = -1;

        /**
         * Starts or stops the get ready timer.
         */
        public boolean toggleGetReadyStartTimestamp() {
            // rest -> exercise
            if (mGetReadyStartTimestamp == -1) {
                // initiate the get ready timer
                mGetReadyStartTimestamp = System.currentTimeMillis();
                return true;
            } else {
                // or cancel it
                mGetReadyStartTimestamp = -1;
                return false;
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
            Training currentTraining = mCurrentTraining;
            int secLeft = Math.round(Utils.GET_READY_INTERVAL
                    - (float) (System.currentTimeMillis() - mGetReadyStartTimestamp)
                    / 1000);

            if (secLeft > 0) {
                updateGetReadyTimer(secLeft);
                mUiHandler.postDelayed(this, Utils.UI_TIMER_UPDATE_RATE);
            } else {
                // mark the the timer is over
                mGetReadyStartTimestamp = -1;

                getReadyTimerOver();
            }
        }

        private void updateGetReadyTimer(int secondsLeft){
            mCircularProgress.setRestProgressValue(secondsLeft);
            mCircularProgress.setTimer(secondsLeft);
        }

        private void getReadyTimerOver(){

            // time is up, start the exercise animation
            mCurrentTraining.startExercise();

            // otherwise show exercising UI
            mUiHandler.removeCallbacks(mUpdateRestTimer);


            // TODO: mightr be better to decouple the training here (and not pass it,
            // but rather pass a minimum set of parameters)
            EventBus.getDefault().post(new EndRestEvent());
        }
    }

    /**
     * This class encapsulates all the logic for creating and updating the rest timer ui. It communicates
     * with the TrainingActivity, containing all the training state information and performing the ui
     * update logic.
     */
    class UpdateRestTimer implements Runnable {

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

        @Override
        public void run() {
            Training currentTraining = mCurrentTraining;
            Exercise currentExercise = currentTraining.getCurrentExercise();

            int currentRest = currentExercise.getCurrentSeries()
                    .getRestTime();
            int currentRestLeft = currentTraining.calculateCurrentRestLeft();

            // TODO: improve this. this one should not have a hard reference to the training activity
            // but should signal redraw in a differnet way (e.g. using callbacks, listeners, events?)
            updateRestTimer(currentRestLeft);

            // play a sound if the rest interval got to 0
            if(!currentTraining.isFirstSeries() && currentRestLeft == 0 && currentRestLeft != mLastRestTimerValue) {
                try {
                    MediaPlayer.create(getActivity().getApplicationContext(), R.raw.alert).start();
                    ((Vibrator) getActivity().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mLastRestTimerValue = currentRestLeft;
            mUiHandler.postDelayed(this, Utils.UI_TIMER_UPDATE_RATE);
        }


        private void updateRestTimer(int restLeft){
            mCircularProgress.setRestProgressValue(restLeft < 0 ? 0
                    : restLeft);

            // if time has run out notify the user that he should
            if(restLeft < 0){
                mCircularProgress.setTimerMessage("");
                mCircularProgress.setNotificationMessage("Tap to exercise");
            }
            mCircularProgress.setTimer(Math.abs(restLeft));
        }
    }
}
