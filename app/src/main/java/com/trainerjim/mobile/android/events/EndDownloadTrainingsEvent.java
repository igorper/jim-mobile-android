package com.trainerjim.mobile.android.events;

/**
 * Created by igor on 14.06.15.
 */
public class EndDownloadTrainingsEvent {

    private boolean status;
    private String errorMessage;

    public EndDownloadTrainingsEvent(boolean status){
        this(status, null);
    }

    public EndDownloadTrainingsEvent(boolean status, String errorMessage){
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public boolean getStatus(){
        return status;
    }

    public String getErrorMessage() {return errorMessage; }
}
