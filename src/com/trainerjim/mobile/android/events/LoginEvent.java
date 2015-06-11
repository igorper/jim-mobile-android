package com.trainerjim.mobile.android.events;

/**
 * Created by igor on 09.06.15.
 */
public class LoginEvent {

    private int mUserId;

    private String mStatusMessage;

    public LoginEvent(int userId, String statusMessage){
        mUserId = userId;
        mStatusMessage = statusMessage;
    }

    public int getUserId(){
        return mUserId;
    }

    public String getStatusMessage(){
        return mStatusMessage;
    }
}
