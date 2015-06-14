package com.trainerjim.mobile.android.events;

/**
 * Created by igor on 12.06.15.
 */
public class ReportProgressEvent {
    public int currentProgress;
    public int maxProgress;
    public String message;

    public ReportProgressEvent(int currentProgress, int maxProgress, String message){
        this.currentProgress = currentProgress;
        this.maxProgress = maxProgress;
        this.message = message;
    }
}
