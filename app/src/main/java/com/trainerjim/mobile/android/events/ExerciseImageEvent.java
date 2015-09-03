package com.trainerjim.mobile.android.events;

/**
 * Created by igor on 02.09.15.
 */
public class ExerciseImageEvent {
    private boolean visible;

    public ExerciseImageEvent(boolean visible){
        this.visible = visible;
    }

    public boolean isVisible(){
        return visible;
    }
}
