package com.trainerjim.mobile.android.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.trainerjim.mobile.android.R;
import com.trainerjim.mobile.android.TrainingSelectionList;
import com.trainerjim.mobile.android.database.TrainingPlan;
import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.events.DismissProgressEvent;
import com.trainerjim.mobile.android.events.EndDownloadTrainingsEvent;
import com.trainerjim.mobile.android.events.EndUploadCompletedTrainings;
import com.trainerjim.mobile.android.events.ReportProgressEvent;
import com.trainerjim.mobile.android.events.StartTrainingEvent;
import com.trainerjim.mobile.android.events.TrainingSelectedEvent;
import com.trainerjim.mobile.android.network.ServerCommunicationService;
import com.trainerjim.mobile.android.storage.PermanentSettings;
import com.trainerjim.mobile.android.ui.CircularProgressControl;
import com.trainerjim.mobile.android.util.Analytics;
import com.trainerjim.mobile.android.util.TutorialHelper;
import com.trainerjim.mobile.android.util.TutorialState;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by igor on 23.05.15.
 */
public class StartTrainingFragment extends Fragment implements View.OnClickListener {

    private static final int ACTIVITY_REQUEST_TRAININGS_LIST = 0;

    private Training mCurrentTraining;
    private Analytics mAnalytics;
    private PermanentSettings mSettings;

    private ProgressDialog mProgressDialog;
    private CircularProgressControl mCircularProgress;
    private RelativeLayout mBottomContainer;
    private TextView mTrainingSelectorText;
    private LinearLayout mTrainingSelector;
    private TextView mTextNoTrainings;

    private TutorialState mCurrentState = TutorialState.NONE;
    private ShowcaseView mCurrentShowcaseView;

    private Handler mUiHandler = new Handler();
    public StartTrainingFragment(){}

    // TODO: this is not the best pattern as if the fragment is killed and recreated by android
    // this constructor might not be called. In the future think about passing those args through
    // a bundle, but for now this should not be a problem.
    public StartTrainingFragment(Training training, Analytics analytics,
                                 PermanentSettings permanentSettings){
        this.mCurrentTraining = training;
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

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        mBottomContainer.setVisibility(View.VISIBLE);
        mTrainingSelector.setOnClickListener(this);

        EventBus.getDefault().register(this);

        return fragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();

        // always upload the trainings on start
        runTrainingsSync();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean trainingsAvailable = TrainingPlan.getAll(mSettings.getUserId()).size() > 0;
        if(trainingsAvailable){
            updateSelectedTrainingText();
        } else {
            runTrainingsSync();
        }
    }

    /**
     * Shows the training name or the label stating that no trainings are currently available.
     */
    private void updateSelectedTrainingText(){
        TrainingPlan selectedTrainingPlan = TrainingPlan.getByTrainingId(mSettings.getSelectedTrainingId());
        boolean trainingsAvailable = selectedTrainingPlan != null;

        if(trainingsAvailable) {
            mTrainingSelectorText.setText(selectedTrainingPlan.getName());
            showMainPageTutorial();

        }

        mTrainingSelector.setVisibility(trainingsAvailable ? View.VISIBLE : View.GONE);
        mTextNoTrainings.setVisibility(trainingsAvailable ? View.GONE : View.VISIBLE);

    }

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
                case R.id.circularProgress: {
                    EventBus.getDefault().post(new StartTrainingEvent(mSettings.getSelectedTrainingId()));

                    return;
                }
                case R.id.trainingSelector: {
                    Intent intent = new Intent(getActivity(),
                            TrainingSelectionList.class);
                    startActivityForResult(intent, ACTIVITY_REQUEST_TRAININGS_LIST);

                    return;
                }
            }
        } else {
            // tutorial is running, determine the next action
            switch (mCurrentState) {
                case SELECT_TRAINING: {
                    mCurrentState = TutorialState.START_TRAINING;
                    createTrainingStartTutorial();
                    break;
                }
                case START_TRAINING: {
                    mCurrentState = TutorialState.NONE;
                    mCurrentShowcaseView.hide();
                    mCurrentShowcaseView = null;
                    mSettings.saveMainPageTutorialCount(mSettings.getMainPageTutorialCount() + 1);
                    break;
                }
                default:
                    break;
            }
        }
    }

    public void onEvent(final TrainingSelectedEvent event){
        mSettings.saveSelectedTrainingId(event.getSelectedTrainingId());
        updateSelectedTrainingText();
    }

    public void onEvent(final ReportProgressEvent event){
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.setMessage(event.message);
                mProgressDialog.setMax(event.maxProgress);
                mProgressDialog.setProgress(event.currentProgress);
                mProgressDialog.show();
            }
        }, 0);
    }

    public void onEvent(final EndUploadCompletedTrainings event){
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().setProgressBarIndeterminateVisibility(false);
            }
        }, 0);
    }

    public void onEvent(final DismissProgressEvent event){
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.hide();
            }
        }, 0);
    }

    public void onEvent(final EndDownloadTrainingsEvent event){

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (event.getStatus()) {
                    // once the trainings are downloaded select the first one by default (or -1
                    // if no training exists)
                    List<TrainingPlan> plans = TrainingPlan.getAll(mSettings.getUserId());
                    int trainingId = -1;
                    if (plans.size() > 0) {
                        trainingId = plans.get(0).getTrainingId();
                    }

                    mSettings.saveSelectedTrainingId(trainingId);
                    updateSelectedTrainingText();
                } else {
                    Toast.makeText(getActivity(), "Download trainings: " + event.getErrorMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, 0);


        // after training were downloaded run the upload as well
        runUploadTrainings();
    }

    private void showMainPageTutorial(){
        // TODO: for now just show the tutorial if it has not been shown yet. In the future
        // we can think about showing the tutorial more than once (hence the number and not
        // a boolean flag)
        if(mCurrentState == TutorialState.NONE && mSettings.getMainPageTutorialCount() == 0) {
            mCurrentState = TutorialState.SELECT_TRAINING;

            mCurrentShowcaseView = TutorialHelper.initTutorialView(getActivity(), this, "Select training",
                    "Tap here to select a training.",
                    new ViewTarget(getView().findViewById(R.id.trainingSelector)), 1.1f);
        }
    }

    private void createTrainingStartTutorial(){
        mCurrentShowcaseView.setScaleMultiplier(1.7f);
        mCurrentShowcaseView.setContentTitle("Start training");
        mCurrentShowcaseView.setContentText("Tap here to start a training.");
        mCurrentShowcaseView.setShowcase(new ViewTarget(getView().findViewById(R.id.circularProgress)), true);
    }

    /**
     * Initiates the upload of completed trainings.
     */
    private void runUploadTrainings() {
        Intent intent = new Intent(getActivity(), ServerCommunicationService.class);
        intent.putExtra(ServerCommunicationService.INTENT_KEY_ACTION,
                ServerCommunicationService.ACTION_UPLOAD_COMPLETED_TRAININGS);

        // TODO: think about where to make sure this is not negative (invalid) - we will also have
        // to check the if the session cookie is set
        intent.putExtra(ServerCommunicationService.INTENT_KEY_USER_ID,
                mSettings.getUserId());
        getActivity().startService(intent);
    }

    /**
     * Initiates the training sync process.
     */
    private void runTrainingsSync() {
        Intent intent = new Intent(getActivity(), ServerCommunicationService.class);
        intent.putExtra(ServerCommunicationService.INTENT_KEY_ACTION,
                ServerCommunicationService.ACTION_FETCH_TRAININGS);

        // TODO: think about where to make sure this is not negative (invalid) - we will also have
        // to check the if the session cookie is set
        intent.putExtra(ServerCommunicationService.INTENT_KEY_USER_ID,
                mSettings.getUserId());
        getActivity().startService(intent);
    }
}
