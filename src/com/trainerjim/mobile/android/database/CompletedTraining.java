package com.trainerjim.mobile.android.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "CompletedTrainings")
public class CompletedTraining extends Model {
    @Column(name = "name")
    public String name;

    @Column(name = "data")
    public String data;

    public CompletedTraining() {
        super();
    }

    public CompletedTraining(String name, String data) {
        super();
        this.name = name;
        this.data = data;
    }

    public String getName() { return name; }

    public String getData() { return data; }

    public static List<CompletedTraining> getAll() {
        return new Select()
                .from(CompletedTraining.class)
                .execute();
    }
}