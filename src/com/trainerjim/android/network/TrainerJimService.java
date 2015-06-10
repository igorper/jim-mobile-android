package com.trainerjim.android.network;

import com.trainerjim.android.entities.ExercisePhoto;
import com.trainerjim.android.entities.ExerciseType;
import com.trainerjim.android.entities.LoginData;
import com.trainerjim.android.entities.TrainingDescription;

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

    @GET("/{imageUrl}")
    Response getExerciseTypeImage(@Path(value="imageUrl", encode=false) String imageUrl);

    @Multipart
    @POST("/mapi/training/upload")
    Response uploadTraining(@Part("email") TypedString email, @Part("password") TypedString password, @Part("trainingData") TypedFile trainingData);

    @POST("/api/v1/auth/login.json")
    Response login(@Body LoginData loginData);

    @GET("/api/v1/users/{user_id}/trainings/{training_id}/exercise_photos.json")
    List<ExercisePhoto> getExercisePhotos(@Path("user_id") int userId, @Path("training_id") int trainingId);

    @GET("/api/v1/trainings.json")
    List<TrainingDescription> getTrainingsList();

    // TODO: make this function return a domain object (to achieve this we have to change how the
    // trainings are saved to the db (currently a json representation is saved, however think
    // about using an alternative option - check activeandroid?)
    @GET("/api/v1/trainings/{training_id}.json")
    Response getTraining(@Path("training_id") int trainingId);
}
