package com.trainerjim.android.network;

import com.trainerjim.android.entities.ExerciseType;
import com.trainerjim.android.entities.Training;
import com.trainerjim.android.entities.TrainingDescription;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by igor on 23.04.15.
 */
public interface  TrainerJimService {

    @GET("/mapi/users/authenticate")
    Boolean checkCredentials(@Query("email") String email, @Query("password") String password);

    @GET("/mapi/training/list")
    List<TrainingDescription> getTrainingsList(@Query("email") String email, @Query("password") String password);

    @GET("/mapi/training/get")
    Response getTraining(@Query("email") String email, @Query("password") String password, @Query("id") int id);

    @GET("/training/exercise_types.json")
    List<ExerciseType> getExerciseTypes();

    @GET("/{imageUrl}")
    Response getExerciseTypeImage(@Path(value="imageUrl", encode=false) String imageUrl);
}
