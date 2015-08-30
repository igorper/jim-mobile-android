package com.trainerjim.mobile.android.network;

import com.trainerjim.mobile.android.entities.ExercisePhoto;
import com.trainerjim.mobile.android.entities.ExerciseType;
import com.trainerjim.mobile.android.entities.LoginData;
import com.trainerjim.mobile.android.entities.Measurement;
import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.entities.TrainingDescription;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by igor on 23.04.15.
 */
public interface  TrainerJimService {
    @GET("/training/exercise_types.json")
    List<ExerciseType> getExerciseTypes();

    @Multipart
    @POST("/mapi/training/upload")
    Response uploadTraining(@Part("email") TypedString email, @Part("password") TypedString password, @Part("trainingData") TypedFile trainingData);

    @POST("/api/v1/auth/login.json")
    Response login(@Body LoginData loginData);

    @GET("/api/v1/users/{user_id}/trainings/{training_id}/exercise_photos.json")
    List<ExercisePhoto> getExercisePhotos(@Path("user_id") int userId, @Path("training_id") int trainingId);

    @GET("/api/v1/trainings.json")
    List<TrainingDescription> getTrainingsList();

    @GET("/api/v1/trainings/{training_id}.json")
    Training getTraining(@Path("training_id") int trainingId);

    @POST("/api/v1/measurements.json")
    Response uploadMeasurement(@Body Measurement measurement);

}
