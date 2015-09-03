package com.trainerjim.mobile.android.events;

/**
 * Created by igor on 02.09.15.
 */
public class ToggleGetReadyEvent {
    private boolean isEnabled;

    public ToggleGetReadyEvent(boolean isEnabled){
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled(){
        return isEnabled;
    }
}
