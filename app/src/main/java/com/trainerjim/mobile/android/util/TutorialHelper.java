package com.trainerjim.mobile.android.util;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;
import android.view.View;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.trainerjim.mobile.android.R;
import com.trainerjim.mobile.android.storage.PermanentSettings;

/**
 * Created by igor on 27.08.15.
 */
public class TutorialHelper {

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
}
