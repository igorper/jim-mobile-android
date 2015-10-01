package com.trainerjim.mobile.android.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.trainerjim.mobile.android.events.BackPressedEvent;
import com.trainerjim.mobile.android.events.CancelTrainingEvent;
import com.trainerjim.mobile.android.events.EndExerciseEvent;
import com.trainerjim.mobile.android.events.ExerciseImageEvent;
import com.trainerjim.mobile.android.events.ExercisesListEvent;
import com.trainerjim.mobile.android.events.ToggleGetReadyEvent;
import com.trainerjim.mobile.android.events.EndRestEvent;
import com.trainerjim.mobile.android.events.TrainingStateChangedEvent;
import com.trainerjim.mobile.android.ui.CircularProgressControl;
import com.trainerjim.mobile.android.ui.ExerciseAdapter;
import com.trainerjim.mobile.android.ui.ExerciseImagesPagerAdapter;
import com.trainerjim.mobile.android.util.Analytics;
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
    private Analytics mAnalytics;

    private CircularProgressControl mCircularProgress;
    private TextView mTextExerciseName;
    private TextView mTextSeriesInfo;
    private ImageView mInfoButton;
    private ImageView mExercisesListButton;
    private ViewPager mViewPager;

    private UpdateRestTimer mUpdateRestTimer = null;

    private GetReadyTimer mGetReadyTimer = null;

    private Handler mUiHandler = new Handler();

    private ExerciseImagesPagerAdapter mExerciseImagesPagerAdapter;

    public RestViewFragment(){}

    // TODO: this is not the best pattern as if the fragment is killed and recreated by android
    // this constructor might not be called. In the future think about passing those args through
    // a bundle, but for now this should not be a problem.
    public RestViewFragment(Training training, TutorialHelper tutorialHelper, Analytics analytics){
        this.mCurrentTraining = training;
        this.mTutorialHelper = tutorialHelper;
        this.mAnalytics = analytics;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.rest_view_fragment, container, false);

        mCircularProgress = (CircularProgressControl)fragmentView.findViewById(R.id.circularProgress);
        mTextExerciseName = (TextView)fragmentView.findViewById(R.id.text_rect_lower_line);
        mTextSeriesInfo = (TextView) fragmentView.findViewById(R.id.text_series_info);
        mInfoButton = (ImageView) fragmentView.findViewById(R.id.info_button);
        mExercisesListButton = (ImageView) fragmentView.findViewById(R.id.exercises_list_button);
        mViewPager = (ViewPager) fragmentView.findViewById(R.id.pager);

        // since view pager ignores click event we had to implement a tap gesture detector to recognize
        // the tap gesture and perform the appropriate action (in this case moving to next page of the
        // view pager)
        final GestureDetector tapGestureDetector = new GestureDetector(getActivity(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                if(mViewPager.getCurrentItem() < mViewPager.getAdapter().getCount() - 1){
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                } else {
                    // if last image was shown close the image view
                    toggleInfoButtonVisible(false);
                    mViewPager.setCurrentItem(0);
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
                return false;
            }
        });

        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                tapGestureDetector.onTouchEvent(event);
                return false;
            }
        });

        mCircularProgress.setCurrentState(CircularProgressControl.CircularProgressState.REST);

        mCircularProgress.setOnClickListener(this);
        mInfoButton.setOnClickListener(this);
        mExercisesListButton.setOnClickListener(this);

        mUpdateRestTimer = new UpdateRestTimer();
        mGetReadyTimer = new GetReadyTimer();

        EventBus.getDefault().register(this);

        setHasOptionsMenu(true);

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
        mUiHandler.removeCallbacks(mUpdateRestTimer);
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
        // TODO: think about how this method goes together with onResume (lifecycle)

        // bind photos to the image view
        Exercise curExercise = mCurrentTraining.getCurrentExercise();
        List<String> photoImages = curExercise.getExerciseType().getPhotoImages();
        mExerciseImagesPagerAdapter = new ExerciseImagesPagerAdapter(this.getActivity(), photoImages);
        // TODO: do we need to remove previous adapter?
        mViewPager.setAdapter(mExerciseImagesPagerAdapter);

        // show exercise and series information
        Series curSeries = curExercise.getCurrentSeries();
        String exerciseName = curExercise.getExerciseType().getName();
        mTextExerciseName.setText(exerciseName);

        // TODO: remove
        if(curExercise.getGuidanceType().equals(Exercise.GUIDANCE_TYPE_DURATION)){
            int minutes = curSeries.getNumberTotalRepetitions() / 60;
            int seconds = curSeries.getNumberTotalRepetitions() - minutes * 60;
            mTextSeriesInfo.setText(
                    String.format(
                            "Series %d (%d min %d sec, %d kg)",
                            curExercise.getCurrentSeriesNumber(),
                            minutes,
                            seconds,
                            curSeries.getWeight()));
        } else {
            mTextSeriesInfo.setText(
                    String.format(
                            "Series %d (%d reps, %d kg)",
                            curExercise.getCurrentSeriesNumber(),
                            curSeries.getNumberTotalRepetitions(),
                            curSeries.getWeight()));
        }

        int currentRest = curSeries.getRestTime();
        mCircularProgress.setRestMaxProgress(currentRest);
        mCircularProgress.setRestMinProgress(0);

        if(mGetReadyTimer.isStarted()){
            // the get ready timer is already running
            mUiHandler.postDelayed(mGetReadyTimer, 0);
        } else {
            // if get ready timer is not running yet we should start the rest timer
            mUiHandler.postDelayed(mUpdateRestTimer, 0);
        }

        // set exercise and training progress bars
        mCircularProgress.setTrainingMaxProgress(mCurrentTraining
                .getTotalSeriesCount());
        mCircularProgress.setTrainingMinProgress(0);
        mCircularProgress.setTrainingProgressValue(mCurrentTraining
                .getSeriesPerformedCount());

        mCircularProgress.setExerciseMaxProgress(curExercise
                .getAllSeriesCount());
        mCircularProgress.setExerciseMinProgress(0);
        mCircularProgress.setExerciseProgressValue(curExercise.getAllSeriesCount() - curExercise.getSeriesLeftCount());

        // show the info button only if the get ready timer is not running
        mInfoButton.setVisibility(View.VISIBLE);
        mExercisesListButton.setVisibility(View.VISIBLE);

        mTutorialHelper.showExerciseTutorial();
    }

    private void showGetReadyScreen(){
        mUiHandler.removeCallbacks(mUpdateRestTimer);

        mCircularProgress.setRestMaxProgress(Utils.GET_READY_INTERVAL);
        mCircularProgress.setRestMinProgress(0);
        mCircularProgress.setTimerMessage("GET READY");

        mInfoButton.setVisibility(View.INVISIBLE);
        mExercisesListButton.setVisibility(View.INVISIBLE);
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

                if(isGetReadyStarted){
                    showGetReadyScreen();

                    mUiHandler.postDelayed(mGetReadyTimer, 0);
                } else {
                    mUiHandler.removeCallbacks(mGetReadyTimer);

                    // TODO: this should have a better name. it should be called smth like show
                    // rest screen
                    updateScreen();
                }

                return;
            }
            case R.id.info_button: {
                toggleInfoButtonVisible(true);
                return;
            }
            case R.id.exercises_list_button: {
                EventBus.getDefault().post(new ExercisesListEvent());
                return;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.active_training_actions, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mTutorialHelper.isTutorialActive()) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_cancel:{
                new AlertDialog.Builder(getActivity())
                        .setTitle("Cancel training")
                        .setMessage("Are you sure you would like to cancel the training?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                EventBus.getDefault().post(new CancelTrainingEvent());
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                break;
            }
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Toggles the visibility of additional info circular button overlay and
     * sets the appropriate icon.
     *
     * @param visible
     */
    private void toggleInfoButtonVisible(boolean visible) {

        if(visible){
            mAnalytics.logShowExerciseImage();
        }

        // only show/hide the images when there are some available
        if(mViewPager.getAdapter().getCount() > 0) {
            mViewPager.setVisibility(visible ? View.VISIBLE : View.GONE);
            EventBus.getDefault().post(new ExerciseImageEvent(visible));
        }
    }

    public void onEvent(BackPressedEvent event){
        if(mViewPager.getVisibility() == View.VISIBLE){
            toggleInfoButtonVisible(false);
        }
    }

    public void onEvent(TrainingStateChangedEvent event){
        updateScreen();
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
            mCircularProgress.setNotificationMessage("");
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
            Exercise currentExercise = mCurrentTraining.getCurrentExercise();

            int currentRest = currentExercise.getCurrentSeries()
                    .getRestTime();
            int currentRestLeft = mCurrentTraining.calculateCurrentRestLeft();

            // TODO: improve this. this one should not have a hard reference to the training activity
            // but should signal redraw in a differnet way (e.g. using callbacks, listeners, events?)
            updateRestTimer(currentRestLeft);

            // TODO: for now we simply pass the whole training instance to fragments as we need information
            // such as 'isFirstSeries), etc.
            // play a sound if the rest interval got to 0
            if(!mCurrentTraining.isFirstSeries() && currentRestLeft == 0 && currentRestLeft != mLastRestTimerValue) {
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
            boolean restOver = restLeft <= 0;

            mCircularProgress.setTimerMessage(restOver ? "" : "RESTING");
            mCircularProgress.setNotificationMessage(restOver ? "Tap to exercise" : "");

            mCircularProgress.setTimer(Math.abs(restLeft));
        }
    }
}