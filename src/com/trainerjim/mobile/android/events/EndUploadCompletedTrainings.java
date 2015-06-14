package com.trainerjim.mobile.android.events;

/**
 * Created by igor on 15.06.15.
 */
public class EndUploadCompletedTrainings {

    private boolean status;

    public EndUploadCompletedTrainings(boolean status){
        this.status = status;
    }

    public boolean getStatus(){
        return  status;
    }
}
