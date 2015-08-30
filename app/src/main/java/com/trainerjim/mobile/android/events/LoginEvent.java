package com.trainerjim.mobile.android.events;

/**
 * Created by igor on 09.06.15.
 */
public class LoginEvent {

    private boolean status;
    private int userId = -1;
    private String statusMessage;

    public LoginEvent(boolean status, int userId){
        this.status = status;
        this.userId = userId;
    }

    public LoginEvent(boolean status, String statusMessage){
        this.status = status;
        this.statusMessage = statusMessage;
    }

    public boolean getStatus() { return status; }

    public int getUserId(){
        return userId;
    }

    public String getStatusMessage(){
        return statusMessage;
    }
}
