package com.trainerjim.mobile.android.util;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.trainerjim.mobile.android.R;
import com.trainerjim.mobile.android.storage.PermanentSettings;

/**
 * Created by igor on 27.08.15.
 */
public class TutorialHelper implements OnShowcaseEventListener{

    private enum TutorialState {
        NONE,
        SYNC,
        SELECT_TRAINING,
        START_TRAINING,
        START_EXERCISE,
        EXERCISE_IMAGE,
        EXERCISES_LIST,
        SAVE_SERIES_OK,
        SAVE_SERIES_CHANGE,
        CHANGE_SKIP_EXERCISE,
        CANCEL_TRAINING
    }

    private TutorialState mCurrentState = TutorialState.NONE;

    private Activity mParentActivity;

    private ShowcaseView mCurrentShowcaseView;

    // TODO: In the feature think about a more decoupled patter (e.g the parent activity could
    // be the master writting the permanent settings while the tutorial helper could message
    // the parent activity (via callbacks or custom listeners) that tutorial is done an the permanent
    // settings should be changed
    private PermanentSettings mSettings;

    public TutorialHelper(Activity parentActivity, PermanentSettings settings){
        mParentActivity = parentActivity;
        mSettings = settings;
    }

    public void showMainPageTutorial(){
        // TODO: for now just show the tutorial if it has not been shown yet. In the future
        // we can think about showing the tutorial more than once (hence the number and not
        // a boolean flag)
        if(mSettings.getMainPageTutorialCount() == 0) {
            mCurrentState = TutorialState.SYNC;
            mCurrentShowcaseView = createSyncTutorial(this);
        }
    }

    public void showExerciseTutorial(){
        if(mSettings.getRestTutorialCount() == 0) {
            mCurrentState = TutorialState.EXERCISE_IMAGE;
            mCurrentShowcaseView = createExerciseImageTutorial(this);
        }
    }

    public void showSaveSeriesTutorial(){
        if(mSettings.getSaveSeriesTutorialCount() == 0) {
            mCurrentState = TutorialState.SAVE_SERIES_OK;
            mCurrentShowcaseView = createSaveSeriesOkTutorial(this);
        }
    }

    public void showExercisesListTutorial() {
        if(mSettings.getExercisesListTutorialCount() == 0) {
            mCurrentState = TutorialState.CHANGE_SKIP_EXERCISE;
            mCurrentShowcaseView = createChangeSkipExerciseTutorial(this);
        }
    }

    public boolean isTutorialActive(){
        return mCurrentState != TutorialState.NONE;
    }

    private Point getTopRightPoint(){
        Display display = mParentActivity.getWindowManager().getDefaultDisplay();
        Point topRight = new Point();
        display.getSize(topRight);
        topRight.y = 0;

        return topRight;
    }

    private ShowcaseView createChangeSkipExerciseTutorial(OnShowcaseEventListener eventListener){
        return new ShowcaseView.Builder(mParentActivity)
                .setStyle(R.style.JimShowcaseTheme)
                .setTarget(new ViewTarget(mParentActivity.findViewById(R.id.tvExerciseName)))
                .setContentTitle("Change exercise")
                .setContentText("To change to another exercise select it from the list. Long press permanently skips the exercise")
                .setShowcaseEventListener(eventListener)
                .build();
    }

    private ShowcaseView createCancelTrainingTutorial(OnShowcaseEventListener eventListener){
        return new ShowcaseView.Builder(mParentActivity)
                .setStyle(R.style.JimShowcaseTheme)
                .setTarget(new PointTarget(getTopRightPoint()))
                .setContentTitle("Cancel training")
                .setContentText("Tap here to cancel the training")
                .setShowcaseEventListener(eventListener)
                .build();
    }

    private ShowcaseView createSaveSeriesOkTutorial(OnShowcaseEventListener eventListener){
        return new ShowcaseView.Builder(mParentActivity)
                .setStyle(R.style.JimShowcaseTheme)
                .setTarget(new ViewTarget(mParentActivity.findViewById(R.id.frag_exe_view_training_weight)))
                .setContentTitle("Next exercise")
                .setContentText("Once you stop exercising tap here to move to the next exercise")
                .setShowcaseEventListener(eventListener)
                .build();
    }

    private ShowcaseView createExercisesListTutorial(OnShowcaseEventListener eventListener){
        return new ShowcaseView.Builder(mParentActivity)
                .setStyle(R.style.JimShowcaseTheme)
                .setTarget(new ViewTarget(mParentActivity.findViewById(R.id.exercises_button)))
                .setContentTitle("Exercises list")
                .setContentText("Here you can see the list of exercises to perform")
                .setShowcaseEventListener(eventListener)
                .build();
    }

    private ShowcaseView createExerciseImageTutorial(OnShowcaseEventListener eventListener){
        return new ShowcaseView.Builder(mParentActivity)
                .setStyle(R.style.JimShowcaseTheme)
                .setTarget(new ViewTarget(mParentActivity.findViewById(R.id.info_button)))
                .setContentTitle("Exercise image")
                .setContentText("Here you can see the current exercise image")
                .setShowcaseEventListener(eventListener)
                .build();
    }

    private ShowcaseView createSyncTutorial(OnShowcaseEventListener eventListener){
        return new ShowcaseView.Builder(mParentActivity)
                .setStyle(R.style.JimShowcaseTheme)
                .setTarget(new PointTarget(getTopRightPoint()))
                .setContentTitle("Sync trainings")
                .setContentText("Press the icon to upload completed and download new trainings")
                .setShowcaseEventListener(eventListener)
                .build();
    }

    private ShowcaseView createSelectTrainingsTutorial(OnShowcaseEventListener eventListener){
        return new ShowcaseView.Builder(mParentActivity)
                .setStyle(R.style.JimShowcaseTheme)
                .setTarget(new ViewTarget(mParentActivity.findViewById(R.id.trainingSelector)))
                .setContentTitle("Select training")
                .setContentText("Tap to select a training")
                .setShowcaseEventListener(eventListener)
                .build();
    }

    private ShowcaseView createTrainingStartTutorial(OnShowcaseEventListener eventListener){
        return new ShowcaseView.Builder(mParentActivity)
                .setStyle(R.style.JimShowcaseTheme)
                .setTarget(new ViewTarget(mParentActivity.findViewById(R.id.circularProgress)))
                .setContentTitle("Start training")
                .setContentText("Tap to start a training")
                .setShowcaseEventListener(eventListener)
                .build();
    }

    private ShowcaseView createSaveSeriesChangeTutorial(OnShowcaseEventListener eventListener){
        return new ShowcaseView.Builder(mParentActivity)
                .setStyle(R.style.JimShowcaseTheme)
                .setTarget(new ViewTarget(mParentActivity.findViewById(R.id.frag_exe_view_edit)))
                .setContentTitle("Series change")
                .setContentText("If you did not exercise according to the plan tap here to log the change")
                .setShowcaseEventListener(eventListener)
                .build();
    }

    private ShowcaseView createExerciseStartTutorial(OnShowcaseEventListener eventListener){
        return new ShowcaseView.Builder(mParentActivity)
                .setStyle(R.style.JimShowcaseTheme)
                .setTarget(new ViewTarget(mParentActivity.findViewById(R.id.circularProgress)))
                .setContentTitle("Start exercise")
                .setContentText("To start exercising tap here and put the phone to a safe place")
                .setShowcaseEventListener(eventListener)
                .build();
    }

    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {
        switch (mCurrentState){
            case SYNC: {
                mCurrentState = TutorialState.SELECT_TRAINING;
                mCurrentShowcaseView = createSelectTrainingsTutorial(this);
                break;
            }
            case SELECT_TRAINING: {
                mCurrentState = TutorialState.START_TRAINING;
                mCurrentShowcaseView = createTrainingStartTutorial(this);
                break;
            }
            case START_TRAINING: {
                mCurrentState = TutorialState.NONE;
                mSettings.saveMainPageTutorialCount(mSettings.getMainPageTutorialCount() + 1);
                break;
            }
            case EXERCISE_IMAGE:{
                mCurrentState = TutorialState.EXERCISES_LIST;
                mCurrentShowcaseView = createExercisesListTutorial(this);
                break;
            } case EXERCISES_LIST: {
                mCurrentState = TutorialState.START_EXERCISE;
                mCurrentShowcaseView = createExerciseStartTutorial(this);
                break;
            } case START_EXERCISE: {
                mCurrentState = TutorialState.NONE;
                mSettings.saveRestTutorialCount(mSettings.getRestTutorialCount() + 1);
                break;
            } case SAVE_SERIES_OK: {
                mCurrentState = TutorialState.SAVE_SERIES_CHANGE;
                mCurrentShowcaseView = createSaveSeriesChangeTutorial(this);
                break;
            } case SAVE_SERIES_CHANGE: {
                mCurrentState = TutorialState.NONE;
                mSettings.saveRestTutorialCount(mSettings.getSaveSeriesTutorialCount() + 1);
                break;
            }  case CHANGE_SKIP_EXERCISE: {
                mCurrentState = TutorialState.CANCEL_TRAINING;
                mCurrentShowcaseView = createCancelTrainingTutorial(this);
                break;
            } case CANCEL_TRAINING: {
                mCurrentState = TutorialState.NONE;
                mSettings.saveExercisesListTutorialCount(mSettings.getExercisesListTutorialCount() + 1);
                break;
            }
        }

    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {

    }
}
