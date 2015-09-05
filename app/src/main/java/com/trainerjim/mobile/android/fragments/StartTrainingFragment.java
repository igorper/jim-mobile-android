package com.trainerjim.mobile.android.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trainerjim.mobile.android.R;
import com.trainerjim.mobile.android.TrainingActivity;
import com.trainerjim.mobile.android.TrainingSelectionList;
import com.trainerjim.mobile.android.database.TrainingPlan;
import com.trainerjim.mobile.android.entities.Exercise;
import com.trainerjim.mobile.android.entities.Series;
import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.events.BackPressedEvent;
import com.trainerjim.mobile.android.events.EndExerciseEvent;
import com.trainerjim.mobile.android.events.EndRestEvent;
import com.trainerjim.mobile.android.events.ExerciseImageEvent;
import com.trainerjim.mobile.android.events.ExercisesListEvent;
import com.trainerjim.mobile.android.events.StartTrainingEvent;
import com.trainerjim.mobile.android.events.ToggleGetReadyEvent;
import com.trainerjim.mobile.android.events.TrainingSelectedEvent;
import com.trainerjim.mobile.android.storage.PermanentSettings;
import com.trainerjim.mobile.android.ui.CircularProgressControl;
import com.trainerjim.mobile.android.ui.ExerciseImagesPagerAdapter;
import com.trainerjim.mobile.android.util.Analytics;
import com.trainerjim.mobile.android.util.TutorialHelper;
import com.trainerjim.mobile.android.util.Utils;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by igor on 23.05.15.
 */
public class StartTrainingFragment extends Fragment implements View.OnClickListener {

    private static final int ACTIVITY_REQUEST_TRAININGS_LIST = 0;

    private Training mCurrentTraining;
    private TutorialHelper mTutorialHelper;
    private Analytics mAnalytics;
    private PermanentSettings mSettings;


    private CircularProgressControl mCircularProgress;
    private RelativeLayout mBottomContainer;
    private TextView mTrainingSelectorText;
    private LinearLayout mTrainingSelector;
    private TextView mTextRectOneLine;

    private Handler mUiHandler = new Handler();
    public StartTrainingFragment(){}

    // TODO: this is not the best pattern as if the fragment is killed and recreated by android
    // this constructor might not be called. In the future think about passing those args through
    // a bundle, but for now this should not be a problem.
    public StartTrainingFragment(Training training, TutorialHelper tutorialHelper, Analytics analytics,
                                 PermanentSettings permanentSettings){
        this.mCurrentTraining = training;
        this.mTutorialHelper = tutorialHelper;
        this.mAnalytics = analytics;
        this.mSettings = permanentSettings;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.start_view_fragment, container, false);

        mCircularProgress = (CircularProgressControl)fragmentView.findViewById(R.id.circularProgress);
        mCircularProgress.setCurrentState(CircularProgressControl.CircularProgressState.START);

        mCircularProgress.setOnClickListener(this);

        mBottomContainer = (RelativeLayout) fragmentView.findViewById(R.id.bottomContainer);
        mTrainingSelectorText = (TextView) fragmentView.findViewById(R.id.trainingSelectorText);
        mTrainingSelector = (LinearLayout) fragmentView.findViewById(R.id.trainingSelector);
        mTextRectOneLine = (TextView) fragmentView.findViewById(R.id.text_rect_one_line);

        mBottomContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setOnClickListener(this);

        if(TrainingPlan.getAll(mSettings.getUserId()).size() > 0){
            // saved selected training plan ID should always be a valid ID.
            updateSelectedTrainingText(mSettings.getSelectedTrainingId());

            mTutorialHelper.showMainPageTutorial();

        } else {
            mTrainingSelector.setVisibility(View.GONE);
            mTextRectOneLine.setText("NO TRAININGS");
            mTextRectOneLine.setVisibility(View.VISIBLE);
        }

        EventBus.getDefault().register(this);

        return fragmentView;
    }

    private void updateSelectedTrainingText(long trainingId){
        TrainingPlan selectedTrainingPlan = TrainingPlan.getByTrainingId(trainingId);
        mTrainingSelectorText.setText(selectedTrainingPlan.getName());
        //mTextRectUpperLine.setVisibility(View.VISIBLE);
        mTrainingSelector.setVisibility(View.VISIBLE);
        mTextRectOneLine.setVisibility(View.INVISIBLE);
        //mLayoutRectTrainingSelector.setVisibility(View.VISIBLE);
        //mLayoutRectLowerLine.setVisibility(View.GONE);

    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        if(mTutorialHelper.isTutorialActive()){
            return;
        }

        switch (view.getId()){
            case R.id.circularProgress: {
                EventBus.getDefault().post(new StartTrainingEvent(-1));

                return;
            }
            case R.id.bottomContainer: {
                Intent intent = new Intent(getActivity(),
                        TrainingSelectionList.class);
                startActivityForResult(intent, ACTIVITY_REQUEST_TRAININGS_LIST);

                return;
            }
        }
    }

    public void onEvent(final TrainingSelectedEvent event){
        updateSelectedTrainingText(event.getSelectedTrainingId());
    }


    /*
     * Gets the results from activities started from this activity.
     *
     * @see android.app.Activity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_REQUEST_TRAININGS_LIST: {
                // on training selected from the list of trainings
                if (data != null
                        && resultCode == getActivity().RESULT_OK
                        && data.hasExtra(TrainingSelectionList.INTENT_EXTRA_SELECTED_TRAINING_KEY)) {
                    final long trainingId = data
                            .getExtras()
                            .getLong(
                                    TrainingSelectionList.INTENT_EXTRA_SELECTED_TRAINING_KEY,
                                    -1);

                    mUiHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO: DB
                            //updateTrainingSelector(trainingId);
                            updateSelectedTrainingText(trainingId);
                        }
                    });
                }
                break;
            }
            default:
                Log.d(Utils.getApplicationTag(), "onActivityResult default switch.");
                break;
        }
    }


}
