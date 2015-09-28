package com.trainerjim.mobile.android.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trainerjim.mobile.android.R;
import com.trainerjim.mobile.android.TrainingSelectionList;
import com.trainerjim.mobile.android.database.TrainingPlan;
import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.events.StartTrainingEvent;
import com.trainerjim.mobile.android.events.TrainingSelectedEvent;
import com.trainerjim.mobile.android.storage.PermanentSettings;
import com.trainerjim.mobile.android.ui.CircularProgressControl;
import com.trainerjim.mobile.android.util.Analytics;
import com.trainerjim.mobile.android.util.TutorialHelper;
import com.trainerjim.mobile.android.util.Utils;

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

    private int mSelectedTrainingId = -1;


    private CircularProgressControl mCircularProgress;
    private RelativeLayout mBottomContainer;
    private TextView mTrainingSelectorText;
    private LinearLayout mTrainingSelector;
    private TextView mTextNoTrainings;

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
        mTextNoTrainings = (TextView) fragmentView.findViewById(R.id.text_no_trainings);

        mBottomContainer.setVisibility(View.VISIBLE);
        mTrainingSelector.setOnClickListener(this);

        boolean trainingsAvailable = TrainingPlan.getAll(mSettings.getUserId()).size() > 0;
        if(trainingsAvailable){
            mSelectedTrainingId = mSettings.getSelectedTrainingId();
            // saved selected training plan ID should always be a valid ID.
            updateSelectedTrainingText();

            mTutorialHelper.showMainPageTutorial();

        }

        mTrainingSelector.setVisibility(trainingsAvailable ? View.VISIBLE : View.GONE);
        mTextNoTrainings.setVisibility(trainingsAvailable ? View.GONE : View.VISIBLE);

        EventBus.getDefault().register(this);

        return fragmentView;
    }

    private void updateSelectedTrainingText(){
        TrainingPlan selectedTrainingPlan = TrainingPlan.getByTrainingId(mSelectedTrainingId);
        mTrainingSelectorText.setText(selectedTrainingPlan.getName());
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
                EventBus.getDefault().post(new StartTrainingEvent(mSelectedTrainingId));

                return;
            }
            case R.id.trainingSelector: {
                Intent intent = new Intent(getActivity(),
                        TrainingSelectionList.class);
                startActivityForResult(intent, ACTIVITY_REQUEST_TRAININGS_LIST);

                return;
            }
        }
    }

    public void onEvent(final TrainingSelectedEvent event){
        mSelectedTrainingId = event.getSelectedTrainingId();
        updateSelectedTrainingText();
    }
}
