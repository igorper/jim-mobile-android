package com.trainerjim.mobile.android.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.trainerjim.mobile.android.entities.TrainingDescription;

import java.util.List;

@Table(name = "TrainingPlans")
public class TrainingPlan extends Model {
    @Column(name = "name")
    public String name;

    @Column(name = "data")
    public String data;

    @Column(name = "training_id", index = true)
    public int trainingId;

    @Column(name = "updated_timestamp")
    public long updatedTimestamp;

    @Column(name = "user_id", index = true)
    public int userId;

    public TrainingPlan() {
        super();
    }

    public TrainingPlan(String name, String data, int trainingId, long updatedTimestamp, int userId) {
        super();
        this.name = name;
        this.data = data;
        this.trainingId = trainingId;
        this.updatedTimestamp = updatedTimestamp;
        this.userId = userId;
    }

    public String getName() { return name; }

    public String getData() { return data; }

    public int getTrainingId(){
        return trainingId;
    }

    public long getUpdatedTimestamp() { return  updatedTimestamp; }

    public int getUserId() { return userId; }

    public void update(String name, long updateTimestamp, String data) {
        this.name = name;
        this.updatedTimestamp = updateTimestamp;
        this.data = data;
    }

    public static List<TrainingPlan> getAll(int userId) {
        return new Select()
                .from(TrainingPlan.class)
                .where("user_id = ?", userId)
                .execute();
    }

    public static List<TrainingPlan> getAll() {
        return new Select()
                .from(TrainingPlan.class)
                .execute();
    }

    public static TrainingPlan getByTrainingId(long trainingId){
        return new Select()
                .from(TrainingPlan.class)
                .where("training_id = ?", trainingId)
                .executeSingle();
    }


}