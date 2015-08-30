package com.trainerjim.mobile.android.entities;

import java.util.Date;

/**
 * Created by igor on 23.04.15.
 */
public class TrainingDescription {
    private int id;
    private String name;
    private Date updated_at;


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getUpdatedDate() {
        return updated_at;
    }
}
