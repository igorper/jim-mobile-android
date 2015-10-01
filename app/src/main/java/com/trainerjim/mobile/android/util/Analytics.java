package com.trainerjim.mobile.android.util;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.trainerjim.mobile.android.BuildConfig;

/**
 * Created by igor on 22.06.15.
 */
public class Analytics {

    private static Analytics sAnalytics;

    private GoogleAnalytics mAnalytics;
    private Tracker mTracker;

    private Analytics(Context applicationContext){

        mAnalytics = GoogleAnalytics.getInstance(applicationContext);
        mAnalytics.setLocalDispatchPeriod(100);
        mAnalytics.setDryRun(!BuildConfig.DEBUG);

        mTracker = mAnalytics.newTracker("UA-64326228-1"); // Replace with actual tracker/property Id
        //mTracker.enableExceptionReporting(true);
        mTracker.enableAdvertisingIdCollection(true);
        mTracker.enableAutoActivityTracking(true);
    }

    public synchronized static Analytics getInstance(Context applicationContext) {
        if(sAnalytics == null){
            sAnalytics = new Analytics(applicationContext);
        }

        return sAnalytics;
    }


    public void logMenuShow() {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("training")
                .setAction("exercise menu")
                .build());
    }

    public void logExerciseChanged() {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("training")
                .setAction("exercise changed")
                .build());
    }

    public void logExerciseSkipped() {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("training")
                .setAction("exercise skip")
                .build());
    }

    public void logShowExerciseImage() {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("training")
                .setAction("exercis image")
                .build());
    }
}
