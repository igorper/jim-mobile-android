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

    // TODO: we reuse the showcase view due to memory leaks in its implementation. This way we make
    // sure it is instantiated only once.
    private static ShowcaseView currentShowcaseView;

    public TutorialHelper(Activity parentActivity, PermanentSettings settings){
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
        if(currentShowcaseView == null){
            currentShowcaseView = new ShowcaseView.Builder(activity, true)
                    .setStyle(R.style.JimShowcaseTheme)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setTarget(target)
                    .setScaleMultiplier(scaleMultiplier)
                    .build();
        } else {

            currentShowcaseView.setContentTitle(title);
            currentShowcaseView.setContentText(text);
            currentShowcaseView.setTarget(target);
            currentShowcaseView.setScaleMultiplier(scaleMultiplier);
            currentShowcaseView.show();
        }

        currentShowcaseView.overrideButtonClick(listener);

        return currentShowcaseView;
    }
}
