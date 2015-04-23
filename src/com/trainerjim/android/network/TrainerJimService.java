package com.trainerjim.android.network;

import com.trainerjim.android.entities.Training;
import com.trainerjim.android.entities.TrainingDescription;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.GET;
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
}
