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

    @Column(name = "user_id", index = true)
    public int userId;

    public CompletedTraining() {
        super();
    }

    public CompletedTraining(String name, String data, int userId) {
        super();
        this.name = name;
        this.data = data;
        this.userId = userId;
    }

    public String getName() { return name; }

    public String getData() { return data; }

    public static List<CompletedTraining> getAll(int userId) {
        return new Select()
                .from(CompletedTraining.class)
                .where("user_id = ?", userId)
                .execute();
    }


    public static List<CompletedTraining> getAll() {
        return new Select()
                .from(CompletedTraining.class)
                .execute();
    }
}