package com.trainerjim.mobile.android.fragments;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.trainerjim.mobile.android.R;
import com.trainerjim.mobile.android.entities.Exercise;
import com.trainerjim.mobile.android.entities.SeriesExecution;
import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.events.EndExerciseEvent;
import com.trainerjim.mobile.android.events.EndRestEvent;
import com.trainerjim.mobile.android.storage.PermanentSettings;
import com.trainerjim.mobile.android.util.TutorialHelper;
import com.trainerjim.mobile.android.util.TutorialState;
import com.trainerjim.mobile.android.util.Utils;

import de.greenrobot.event.EventBus;

/**
 * Created by igor on 23.05.15.
 */
public class ExerciseViewFragment extends Fragment implements View.OnClickListener {

    /**
     * Default series rating used when the user quickly finishes the series (doesn't
     * edit series details information). Additionally as a default selection in
     * edit series details view.
     *
     * Note: Numbers correspond to EXERCISE_RATING_IMAGES and EXERCISE_RATING_SELECTED_IMAGES
     * arrays.
     */
    private static final int DEFAULT_SERIES_RATING = 1;

    /**
     * In ms.
     */
    private static final int UI_TIMER_UPDATE_RATE = 300;

    /**
     * Contains IDs of training rating images in non-selected (non-clicked)
     * state.
     */
    private static int[] EXERCISE_RATING_IMAGES = { R.drawable.sm_1_ns,
            R.drawable.sm_2_ns, R.drawable.sm_3_ns };

    /**
     * Contains IDs of training rating images in selected (touched) state.
     */
    private static int[] EXERCISE_RATING_SELECTED_IMAGES = { R.drawable.sm_1_s,
            R.drawable.sm_2_s, R.drawable.sm_3_s };

    private Training mCurrentTraining;

    /**
     * References to the ImageViews hosting exercise rating images.
     */
    private ImageView[] mExerciseRatingImages;

    /**
     * Holds the last value of the running exercise timer. This field is used
     * when playing a notification during an exercise timer.
     */
    private long mLastExerciseTimerValue;

    /**
     * Holds the ID of the currently selected exercise rating (or -1 if no
     * rating was yet selected).
     */
    private int mExerciseRatingSelectedID = -1;

    /**
     * UI thread handler.
     */
    private Handler mUiHandler = new Handler();

    /**
     * Controls the visibility of the edit series details view. If true, edit
     * series view will be shown to the users.
     */
    private Boolean mEditSeriesDetails = false;

    private TextView mExerciseTimer;
    private LinearLayout mLlEditReps;
    private NumberPicker mEditRepetitionsNumPick;
    private NumberPicker mEditWeightNumPick;
    private RelativeLayout mEditDetailsView;
    private EditText mEditCommentValue;

    private TutorialState mCurrentState = TutorialState.NONE;

    private ShowcaseView mCurrentShowcaseView;

    private PermanentSettings mSettings;

    public ExerciseViewFragment(){}
    public ExerciseViewFragment(Training currentTrainig, PermanentSettings settings){
        this.mCurrentTraining = currentTrainig;
        this.mSettings = settings;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.exercise_view_fragment, container, false);

        mExerciseTimer = (TextView)fragmentView.findViewById(R.id.frag_exe_view_tv_exercise_timer);
        mLlEditReps = (LinearLayout)fragmentView.findViewById(R.id.ll_edit_reps);
        mEditRepetitionsNumPick = (NumberPicker)fragmentView.findViewById(R.id.edit_reps_num_pick);
        mEditWeightNumPick = (NumberPicker)fragmentView.findViewById(R.id.edit_weight_num_pick);
        mEditDetailsView = (RelativeLayout)fragmentView.findViewById(R.id.editDetailsView);
        mEditCommentValue  =(EditText)fragmentView.findViewById(R.id.editCommentValue);


        mEditRepetitionsNumPick.setMinValue(0);
        mEditRepetitionsNumPick.setMaxValue(100);

        mEditWeightNumPick.setMinValue(0);
        mEditWeightNumPick.setMaxValue(300);

        fragmentView.findViewById(R.id.frag_exe_view_training_weight).setOnClickListener(this);
        fragmentView.findViewById(R.id.frag_exe_view_edit).setOnClickListener(this);
        fragmentView.findViewById(R.id.frag_exe_view_btn_save_series).setOnClickListener(this);
        fragmentView.findViewById(R.id.exerciseRating1).setOnClickListener(this);
        fragmentView.findViewById(R.id.exerciseRating2).setOnClickListener(this);
        fragmentView.findViewById(R.id.exerciseRating3).setOnClickListener(this);

        EventBus.getDefault().register(this);

        mEditDetailsView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // only used to handle all the clicks while the edit details
                // view is visible
                return true;
            }
        });

        initializeExerciseRatings(fragmentView);

        mExerciseTimer.setVisibility(mCurrentTraining.getCurrentExercise().getGuidanceType().equals(Exercise.GUIDANCE_TYPE_DURATION) ? View.VISIBLE : View.GONE);

        mUiHandler.postDelayed(mUpdateExerciseTimer, 0);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showSaveSeriesTutorial();
    }

    private void showSaveSeriesTutorial(){
        if(mSettings.getSaveSeriesTutorialCount() == 0) {
            mCurrentState = TutorialState.SAVE_SERIES_OK;

            mCurrentShowcaseView = TutorialHelper.initTutorialView(getActivity(), this, getString(R.string.tutorial_next_exercise_title),
                    getString(R.string.tutorial_next_exercise_message),
                    new ViewTarget(getView()
                            .findViewById(R.id.frag_exe_view_training_weight)), 1.2f);
        }
    }

    public void onEvent(EndRestEvent event){}

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        if(mCurrentState == TutorialState.NONE){
            // currently no tutorial is in shown, capture button actions
            switch (view.getId()){
                case R.id.frag_exe_view_training_weight:{
                    onSeriesDoneClick();
                    return;
                }
                case R.id.frag_exe_view_edit: {
                    onSeriesDetailsClick();
                    return;
                }
                case R.id.frag_exe_view_btn_save_series: {
                    onEditDetailsDoneClick();
                    return;
                }
                case R.id.exerciseRating1:
                case R.id.exerciseRating2:
                case R.id.exerciseRating3:
                {
                    onExerciseRatingSelected(view);
                    return;
                }
            }
        } else {
            // tutorial is running, determine the next action
            switch (mCurrentState){
                case SAVE_SERIES_OK: {
                    mCurrentState = TutorialState.SAVE_SERIES_CHANGE;
                    createSaveSeriesChangeTutorial();
                    break;
                }
                case SAVE_SERIES_CHANGE: {
                    mCurrentState = TutorialState.NONE;
                    mCurrentShowcaseView.hide();
                    mSettings.saveSeriesTutorialCount(mSettings.getSaveSeriesTutorialCount() + 1);
                    break;
                }
                default:
                    break;
            }
        }
    }

    /** Save the (edited) series execution values and hides the edit details view.
     */
    public void onEditDetailsDoneClick(){
        // this is possible (however not very secure) as we are not creating a
        // defensive copy of the series execution in training
        SeriesExecution lastSe = mCurrentTraining.getLastSeriesExecution();
        if(lastSe != null){
            lastSe.setRating(mExerciseRatingSelectedID);
            lastSe.setWeight(mEditWeightNumPick.getValue());
            lastSe.setRepetitions(mEditRepetitionsNumPick.getValue());
        }

        mEditDetailsView.setVisibility(View.GONE);

        EventBus.getDefault().post(new EndExerciseEvent());
    }

    /**
     * Invoked when a specific exercise rating image is clicked.
     *
     * @param v contains the reference to the clicked exercise rating image.
     */
    public void onExerciseRatingSelected(View v) {
        ImageView exerciseRatingImage = (ImageView) v;

        selectSeriesRatingIcon(exerciseRatingImage);
    }

    private void createSaveSeriesChangeTutorial(){
        int margin = (int)getActivity().getResources().getDimension(com.github.amlcurran.showcaseview.R.dimen.button_margin);

        RelativeLayout.LayoutParams showcaseLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        showcaseLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        showcaseLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        showcaseLP.setMargins(margin, margin, margin, margin);

        mCurrentShowcaseView.setScaleMultiplier(1f);
        mCurrentShowcaseView.setButtonPosition(showcaseLP);
        mCurrentShowcaseView.setContentTitle(getString(R.string.tutorial_edit_exercise_title));
        mCurrentShowcaseView.setContentText(getString(R.string.tutorial_edit_exercise_message));
        mCurrentShowcaseView.setShowcase(new ViewTarget(getView().findViewById(R.id.frag_exe_view_edit)), true);
    }

    /**
     * Creates a mapping of exercise rating images and exercise rattings.
     */
    private void initializeExerciseRatings(View fragmentView) {
        mExerciseRatingImages = new ImageView[EXERCISE_RATING_IMAGES.length];
        mExerciseRatingImages[0] = (ImageView) fragmentView.findViewById(R.id.exerciseRating1);
        mExerciseRatingImages[1] = (ImageView) fragmentView.findViewById(R.id.exerciseRating2);
        mExerciseRatingImages[2] = (ImageView) fragmentView.findViewById(R.id.exerciseRating3);
    }

    /**
     * Invoked when the user is done with a series and does not want to edit series details.
     *
     */
    private void onSeriesDoneClick() {
        finishSeries(true);
    }

    /**
     * Invoked when the user wants to edit series details.
     *
     */
    private void onSeriesDetailsClick() {
        // select default rating icon on screen
        selectSeriesRatingIcon(mExerciseRatingImages[DEFAULT_SERIES_RATING]);

        // we will want to show the view for editing this series
        mEditSeriesDetails = true;
        mLlEditReps.setVisibility(mCurrentTraining.getCurrentExercise().getGuidanceType().equals(Exercise.GUIDANCE_TYPE_DURATION) ? View.GONE : View.VISIBLE);

        // finish series with the default series rating
        finishSeries(false);

        showEditDetailsViewIfDemanded();
    }

    /**
     * Manages series rating image views (smile icons).
     * Selects the input imageView and deselect all the other rating image views.
     * @param ratingImageSelected is the selected imageView
     */
    private void selectSeriesRatingIcon(ImageView ratingImageSelected){
        // different padding as we make the selected image a bit larger
        int imageSelectedPadding = getResources().getDimensionPixelSize(
                R.dimen.training_rating_smile_selected_padding);
        int imagePadding = getResources().getDimensionPixelSize(
                R.dimen.training_rating_smile_padding);

        // loop through training ratings image views and set the appropriate
        // image
        for (int i = 0; i < mExerciseRatingImages.length; i++) {
            if (mExerciseRatingImages[i] == ratingImageSelected) {
                mExerciseRatingImages[i]
                        .setImageResource(EXERCISE_RATING_SELECTED_IMAGES[i]);
                mExerciseRatingImages[i].setPadding(imageSelectedPadding,
                        imageSelectedPadding, imageSelectedPadding,
                        imageSelectedPadding);
                // set global information about the selected rating (this will also be used
                // to store the rating to the corresponding SeriesExecution)
                mExerciseRatingSelectedID = i;
            } else {
                mExerciseRatingImages[i]
                        .setImageResource(EXERCISE_RATING_IMAGES[i]);
                mExerciseRatingImages[i].setPadding(imagePadding, imagePadding,
                        imagePadding, imagePadding);
            }
        }
    }

    private void showEditDetailsViewIfDemanded() {
        SeriesExecution lastSe = mCurrentTraining.getLastSeriesExecution();
        if (mEditSeriesDetails && lastSe != null) {
            mEditRepetitionsNumPick.setValue(lastSe.getRepetitions());
            mEditWeightNumPick.setValue(lastSe.getWeight());

            mEditDetailsView.setVisibility(View.VISIBLE);
            mEditSeriesDetails = false;
        }
    }

    /**
     * Finish series and set the input rating.
     */
    private void finishSeries(boolean closeView){
        // store the default series rating (for quick entry). In case of edit series details
        // the rating can be changed later on (in edit series details view).
        mCurrentTraining.setCurrentSeriesExecutionRating(DEFAULT_SERIES_RATING);

        // end this exercise (series)
        mCurrentTraining.endExercise();

        // hide the screen that is shown during exercising
        if(closeView) {
            EventBus.getDefault().post(new EndExerciseEvent());
        }

        mUiHandler.removeCallbacks(mUpdateExerciseTimer);
    }

    /**
     */
    private Runnable mUpdateExerciseTimer = new Runnable() {

        @Override
        public void run() {
            Exercise currentExercise = mCurrentTraining.getCurrentExercise();
            if (currentExercise != null && currentExercise.getGuidanceType().equals(Exercise.GUIDANCE_TYPE_DURATION)) {
                int currentDuration = currentExercise.getCurrentSeries()
                        .getNumberTotalRepetitions();
                int currentDurationLeft = mCurrentTraining
                        .calculateDurationLeft();
                int absDurationLeft = Math.abs(currentDurationLeft);

                int minutes = absDurationLeft / 60;
                int seconds = absDurationLeft - minutes * 60;

                mExerciseTimer.setText(String.format("%02d:%02d", minutes, seconds));

                // play a notification on duration due (and another one after 10 seconds if
                // the user missed it)
                if(currentDurationLeft == 0 || currentDurationLeft == -10){
                    // play the notification only once per second (event)
                    if(mLastExerciseTimerValue != currentDurationLeft){
                        try {
                            MediaPlayer mPlayer = MediaPlayer.create(getActivity(), R.raw.alert);
                            mPlayer.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                mLastExerciseTimerValue = currentDurationLeft;
                mUiHandler.postDelayed(this, UI_TIMER_UPDATE_RATE);
                Log.d(Utils.getApplicationTag(), String.format("%02d:%02d", minutes, seconds));
            }
        }
    };
}
