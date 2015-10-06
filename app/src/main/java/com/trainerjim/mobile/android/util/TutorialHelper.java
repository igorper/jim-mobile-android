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

    public enum TutorialState {
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

    public void showSaveSeriesTutorial(){
        if(mSettings.getSaveSeriesTutorialCount() == 0) {
            mCurrentState = TutorialState.SAVE_SERIES_OK;

            mCurrentShowcaseView = initTutorialView(mParentActivity, this, "Next exercise",
                    "Tap here to move to the next exercise",
                    new ViewTarget(mParentActivity.findViewById(R.id.frag_exe_view_training_weight)), 1.5f);
        }
    }

    public boolean isTutorialActive(){
        return mCurrentState != TutorialState.NONE;
    }

    /*** Helpers ***/
    public static Point getTopRightPoint(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point topRight = new Point();
        display.getSize(topRight);
        topRight.y = 0;

        return topRight;
    }

    public static ShowcaseView initTutorialView(Activity activity, View.OnClickListener listener, String title, String text, Target target){
        return initTutorialView(activity, listener, title, text, target, 1f);
    }

    public static ShowcaseView initTutorialView(Activity activity, View.OnClickListener listener, String title, String text, Target target, float scaleMultiplier){
        return new ShowcaseView.Builder(activity, true)
                .setStyle(R.style.JimShowcaseTheme)
                .setOnClickListener(listener)
                .setContentTitle(title)
                .setContentText(text)
                .setTarget(target)
                .setScaleMultiplier(scaleMultiplier)
                .build();
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
