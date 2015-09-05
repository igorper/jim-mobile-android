package com.trainerjim.mobile.android.util;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.trainerjim.mobile.android.R;
import com.trainerjim.mobile.android.storage.PermanentSettings;

/**
 * Created by igor on 27.08.15.
 */
public class TutorialHelper implements View.OnClickListener{

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

            initTutorialView("Sync trainings",
                    "Tap here to download new trainings",
                    new PointTarget(getTopRightPoint()));
        }
    }

    public void showExerciseTutorial(){
        if(mSettings.getRestTutorialCount() == 0) {
            mCurrentState = TutorialState.EXERCISE_IMAGE;

            initTutorialView("Exercise image",
                    "Tap here to see the current exercise image",
                    new ViewTarget(mParentActivity.findViewById(R.id.info_button)), 0.6f);
        }
    }

    public void showSaveSeriesTutorial(){
        if(mSettings.getSaveSeriesTutorialCount() == 0) {
            mCurrentState = TutorialState.SAVE_SERIES_OK;

            initTutorialView("Next exercise",
                    "Tap here to move to the next exercise",
                    new ViewTarget(mParentActivity.findViewById(R.id.frag_exe_view_training_weight)), 1.5f);
        }
    }

    public void showExercisesListTutorial() {
        if(mSettings.getExercisesListTutorialCount() == 0) {
            mCurrentState = TutorialState.CHANGE_SKIP_EXERCISE;

            initTutorialView("Change exercise",
                    "To change to another exercise select it from the list. Long press permanently skips the exercise",
                    new ViewTarget(mParentActivity.findViewById(R.id.tvExerciseName)));
        }
    }

    public boolean isTutorialActive(){
        return mCurrentState != TutorialState.NONE;
    }

    /*** Helpers ***/
    private Point getTopRightPoint(){
        Display display = mParentActivity.getWindowManager().getDefaultDisplay();
        Point topRight = new Point();
        display.getSize(topRight);
        topRight.y = 0;

        return topRight;
    }

    private void initTutorialView(String title, String text, Target target){
        initTutorialView(title, text, target, 1f);
    }

    private void initTutorialView(String title, String text, Target target, float scaleMultiplier){
        mCurrentShowcaseView = new ShowcaseView.Builder(mParentActivity, true)
                .setStyle(R.style.JimShowcaseTheme)
                .setOnClickListener(this)
                .setContentTitle(title)
                .setContentText(text)
                .setTarget(target)
                .setScaleMultiplier(scaleMultiplier)
                .build();
    }

    /*** Start training tutorial ***/
    private void createSelectTrainingsTutorial(){
        mCurrentShowcaseView.setScaleMultiplier(1.1f);
        mCurrentShowcaseView.setContentTitle("Select training");
        mCurrentShowcaseView.setContentText("Tap here to select a training.");
        mCurrentShowcaseView.setShowcase(new ViewTarget(mParentActivity.findViewById(R.id.trainingSelector)), true);
    }

    private void createTrainingStartTutorial(){
        mCurrentShowcaseView.setScaleMultiplier(1.7f);
        mCurrentShowcaseView.setContentTitle("Start training");
        mCurrentShowcaseView.setContentText("Tap here to start a training.");
        mCurrentShowcaseView.setShowcase(new ViewTarget(mParentActivity.findViewById(R.id.circularProgress)), true);
    }

    /*** Start exercise tutorial ***/
    private void  createExercisesListTutorial(){
        mCurrentShowcaseView.setScaleMultiplier(0.6f);
        mCurrentShowcaseView.setContentTitle("Exercises list");
        mCurrentShowcaseView.setContentText("Tap here to see the list of exercises to perform.");
        mCurrentShowcaseView.setShowcase(new ViewTarget(mParentActivity.findViewById(R.id.exercises_list_button)), true);
    }

    private void createExerciseStartTutorial(){
        mCurrentShowcaseView.setScaleMultiplier(1.8f);
        mCurrentShowcaseView.setContentTitle("Start exercise");
        mCurrentShowcaseView.setContentText("To start exercising tap here and put the phone to a safe place.");
        mCurrentShowcaseView.setShowcase(new ViewTarget(mParentActivity.findViewById(R.id.circularProgress)), true);
    }

    /*** Exercises list tutorial ***/
    private void createCancelTrainingTutorial(){
        mCurrentShowcaseView.setContentTitle("Cancel training");
        mCurrentShowcaseView.setContentText("Tap here to cancel the training.");
        mCurrentShowcaseView.setShowcase(new PointTarget(getTopRightPoint()), true);
    }

    /*** Save series tutorial ***/
    private void createSaveSeriesChangeTutorial(){
        int margin = (int)mParentActivity.getResources().getDimension(com.github.amlcurran.showcaseview.R.dimen.button_margin);

        RelativeLayout.LayoutParams showcaseLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        showcaseLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        showcaseLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        showcaseLP.setMargins(margin, margin, margin, margin);

        mCurrentShowcaseView.setScaleMultiplier(1f);
        mCurrentShowcaseView.setButtonPosition(showcaseLP);
        mCurrentShowcaseView.setContentTitle("Series change");
        mCurrentShowcaseView.setContentText("If you did not exercise according to the plan tap here to log the change.");
        mCurrentShowcaseView.setShowcase(new ViewTarget(mParentActivity.findViewById(R.id.frag_exe_view_edit)), true);
    }

    @Override
    public void onClick(View view) {
        switch (mCurrentState){
            case SYNC:{
                mCurrentState = TutorialState.SELECT_TRAINING;
                createSelectTrainingsTutorial();
                break;
            }
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
            case EXERCISE_IMAGE: {
                mCurrentState = TutorialState.EXERCISES_LIST;
                createExercisesListTutorial();
                break;
            }
            case EXERCISES_LIST: {
                mCurrentState = TutorialState.START_EXERCISE;
                createExerciseStartTutorial();
                break;
            }
            case START_EXERCISE: {
                mCurrentState = TutorialState.NONE;
                mCurrentShowcaseView.hide();
                mSettings.saveRestTutorialCount(mSettings.getRestTutorialCount() + 1);
                break;
            }
            case CHANGE_SKIP_EXERCISE: {
                mCurrentState = TutorialState.CANCEL_TRAINING;
                createCancelTrainingTutorial();
                break;
            }
            case CANCEL_TRAINING: {
                mCurrentState = TutorialState.NONE;
                mCurrentShowcaseView.hide();
                mSettings.saveExercisesListTutorialCount(mSettings.getExercisesListTutorialCount() + 1);
                break;
            }
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
        }
    }
}
