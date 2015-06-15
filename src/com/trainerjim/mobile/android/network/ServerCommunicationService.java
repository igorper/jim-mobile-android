package com.trainerjim.mobile.android.network;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.preference.PreferenceManager;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.squareup.okhttp.internal.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.trainerjim.mobile.android.R;
import com.trainerjim.mobile.android.database.CompletedTraining;
import com.trainerjim.mobile.android.database.TrainingPlan;
import com.trainerjim.mobile.android.entities.Exercise;
import com.trainerjim.mobile.android.entities.ExercisePhoto;
import com.trainerjim.mobile.android.entities.ExerciseType;
import com.trainerjim.mobile.android.entities.LoginData;
import com.trainerjim.mobile.android.entities.LoginResponseData;
import com.trainerjim.mobile.android.entities.Measurement;
import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.entities.TrainingDescription;
import com.trainerjim.mobile.android.events.DismissProgressEvent;
import com.trainerjim.mobile.android.events.EndDownloadTrainingsEvent;
import com.trainerjim.mobile.android.events.EndExerciseEvent;
import com.trainerjim.mobile.android.events.EndUploadCompletedTrainings;
import com.trainerjim.mobile.android.events.LoginEvent;
import com.trainerjim.mobile.android.events.ReportProgressEvent;
import com.trainerjim.mobile.android.storage.PermanentSettings;
import com.trainerjim.mobile.android.util.Utils;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.client.Header;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * This service is the central gateway for all the communication with the remote
 * server.
 *
 * @author Igor
 */
public class ServerCommunicationService extends IntentService {

    private static final String TAG = Utils.getApplicationTag();

    /***********************
     * LOGIN TASK ACTIONS AND PARAMETERS
     ***********************/

    /**
     * This ID marks the login completed action.
     */
    public static final String ACTION_LOGIN_COMPLETED = "action_done";

    /**
     * This ID marks if the executed operation was successful or not.
     */
    public static final String PARAM_ACTION_SUCCESSFUL = "action_successful";

    /**
     * This ID marks if the executed operation status messages.
     */
    public static final String PARAM_ACTION_MSG = "action_msg";

    /***********************
     * FETCH TRAININGS TASK ACTIONS AND PARAMETERS
     ***********************/

    /**
     * This ID marks the fetch trainings completed action.
     */
    public static final String ACTION_FETCH_TRAINNGS_COMPLETED = "fetch_trainings_done";

    /**
     * This ID marks the get trainings list completed action.
     */
    public static final String ACTION_GET_TRAINNGS_LIST_COMPLETED = "fetch_trainings_list_downloaded";

    /**
     * This ID marks that the individual training item was successfully fetched.
     */
    public static final String ACTION_REPORT_PROGRESS = "fetch_trainings_item_downloaded";

    /**
     * This ID marks the number of all trainings to fetch from the server.
     */
    public static final String PARAM_REPORT_PROGRESS_TOTAL = "fetch_trainings_num_items";

    /**
     * This ID marks the name of the item being currently fetched from the
     * server.
     */
    public static final String PARAM_REPORT_PROGRESS_TEXT = "fetch_trainings_cur_item_name";

    /**
     * This ID marks the current count of the trainings downloaded from the
     * server (used for progress visualization).
     */
    public static final String PARAM_REPORT_PROGRESS_CURRENT = "fetch_trainings_cur_item_cnt";

    /***********************
     * UPLOAD TRAININGS TASK ACTIONS AND PARAMETERS
     ***********************/

    /**
     * This ID marks the upload trainings started action.
     */
    public static final String ACTION_UPLOAD_TRAININGS_STARTED = "upload_completed_trainings_started";

    /**
     * This ID marks the upload trainings completed action.
     */
    public static final String ACTION_UPLOAD_TRAINNGS_COMPLETED = "upload_trainings_done";

    /**
     * This ID marks the trainings item uploaded action.
     */
    public static final String ACTION_TRAININGS_ITEM_UPLOADED = "upload_completed_trainings_item_uploaded";

    /**
     * This ID marks the number of all items to be uploaded.
     */
    public static final String PARAM_UPLOAD_TRAINING_NUM_ALL_ITEMS = "upload_completed_training_num_items";

    /**
     * This ID marks the name of the particular completed training to be
     * uploaded.
     */
    public static final String PARAM_UPLOAD_TRAINING_ITEM_NAME = "upload_training_item_name";

    /**
     * This ID marks the upload status for a particular completed training item.
     */
    public static final String PARAM_UPLOAD_TRAINING_ITEM_STATUS = "upload_training_status";

    /**
     * This ID marks the current count of processed completed trainings (and is
     * primarly used for visualizing upload progress).
     */
    public static final String PARAM_UPLOAD_TRAINING_CUR_ITEM_CNT = "upload_training_cur_item_count";

    /**
     * This ID marks the number of successfully uploaded completed training
     * items.
     */
    public static final String PARAM_UPLOAD_TRAINING_SUCESS_CNT = "upload_training_sucess_count";

    /***********************
     * Keys used by the IntentService.
     ***********************/

    /**
     * This ID marks the action to be performed by this intent service.
     */
    public static final String INTENT_KEY_ACTION = "action";

    /**
     * This ID marks the authentication username.
     */
    public static final String INTENT_KEY_USERNAME = "username";

    /**
     * This ID marks the authentication password.
     */
    public static final String INTENT_KEY_PASSWORD = "password";

    /**
     * This ID marks the authenticated user id.
     */
    public static final String INTENT_KEY_USER_ID = "user-id";

    /**
     * This ID marks the upload completed trainings action.
     */
    public static final String ACTION_UPLOAD_COMPLETED_TRAININGS = "action-upload";

    /**
     * This ID marks the fetch trainings from the server action.
     */
    public static final String ACTION_FETCH_TRAININGS = "action-get-training-list";

    /**
     * This ID marks the login action.
     */
    public static final String ACTION_CHECK_CREDENTIALS = "action-login";

    /**
     * The number of completed trainings stored in the local database.
     */
    private int mNumCompletedTrainings = 0;

    /**
     * Rest adapter for creating services. Needed by the "Retrofit" library.
     */
    private RestAdapter restAdapter;

    /**
     * Service for accessing the Trainerjim backend methods.
     */
    private TrainerJimService service;

    public ServerCommunicationService() {
        super("DataUploaderService");
    }

    /*
             * Central method receiving all the server communication requests.
             * (non-Javadoc)
             *
             * @see android.app.IntentService#onHandleIntent(android.content.Intent)
             */
    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getExtras().getString(INTENT_KEY_ACTION);

        // initialize rest communication channel
        restAdapter = new RestAdapter.Builder().setEndpoint(getResources().getString(R.string.server_url))
                .setConverter(new GsonConverter(Utils.getGsonObject()))
                .setRequestInterceptor(ApiHeaders.getInstance(this)).build();
        service = restAdapter.create(TrainerJimService.class);

        if (action.equals(ACTION_UPLOAD_COMPLETED_TRAININGS)) {
            EventBus.getDefault().post(uploadCompletedTrainings());
        } else if (action.equals(ACTION_CHECK_CREDENTIALS)) {

            String username = intent.getExtras().getString(INTENT_KEY_USERNAME);
            String password = intent.getExtras().getString(INTENT_KEY_PASSWORD);


            EventBus.getDefault().post(checkCredentials(username, password));

        } else if (action.equals(ACTION_FETCH_TRAININGS)) {
            int userId = intent.getExtras().getInt(INTENT_KEY_USER_ID);
            EventBus.getDefault().post(fetchTrainings(userId));
        }

    }

    /**
     * @return
     */
    private EndDownloadTrainingsEvent fetchTrainings(int userId) {
        ActiveAndroid.beginTransaction();
        try {

            // fetch server trainings and create a lookup table
            List<TrainingDescription> trainingsManifest = service.getTrainingsList();
            Map<Integer, TrainingDescription> trainingsManifestLookup = new HashMap<Integer, TrainingDescription>();
            for (TrainingDescription trainingDescription : trainingsManifest) {
                trainingsManifestLookup.put(trainingDescription.getId(), trainingDescription);
            }

            // keep only a list of remote trainings that have changed (to be fetched), remove trainings
            // from the local database that are not present in the list of remote trainings
            List<TrainingPlan> localTrainings = TrainingPlan.getAll();
            for (TrainingPlan localTraining : localTrainings) {
                int localTrainingId = localTraining.getTrainingId();
                if (trainingsManifestLookup.containsKey(localTrainingId) &&
                        trainingsManifestLookup.get(localTrainingId).getUpdatedDate().getTime() == localTraining.getUpdatedTimestamp()) {
                    // remote training has the same timestamp, no need to download it
                    trainingsManifestLookup.remove(localTrainingId);
                } else {
                    localTraining.delete();
                }
            }

            // fetch only the trainings that are new or have been changed
            trainingsManifest = new ArrayList<TrainingDescription>(trainingsManifestLookup.values());
            for (int i = 0; i < trainingsManifest.size(); i++) {
                TrainingDescription trainingDescription = trainingsManifest.get(i);

                // notify that we are fetching a specific training
                EventBus.getDefault().post(new ReportProgressEvent(i, trainingsManifest.size(), "Fetching training " + trainingDescription.getName()));

                // fetch individual training
                Training training = service.getTraining(trainingDescription.getId());

                HashMap<Integer, List<String>> exercisePhotoLookup = new HashMap<Integer, List<String>>();
                try {
                    // download exercise photos. we do this by precaching them using the picasso library.
                    // later, when we use the images, we set a flag saying they should always be loaded
                    // from cache (thus the images are not transferred and no data transfer should happen)
                    List<ExercisePhoto> exercisePhotos = service.getExercisePhotos(userId, trainingDescription.getId());
                    for (ExercisePhoto photo : exercisePhotos) {
                        Picasso.with(this)
                                .load(String.format("%s%s",
                                        getResources().getString(R.string.server_url),
                                        photo.medium_image_url))
                                .fetch();

                        // create an empty list for each exercise type
                        if (!exercisePhotoLookup.containsKey(photo.exercise_type_id)) {
                            exercisePhotoLookup.put(photo.exercise_type_id, new ArrayList<String>());
                        }

                        // add photo urls to the list
                        exercisePhotoLookup.get(photo.exercise_type_id).add(photo.medium_image_url);
                    }

                    for(Exercise exercise : training.getExercises()) {
                        ExerciseType exerciseType = exercise.getExerciseType();

                        // if no photos are available for the exercise just add an empty list
                        exerciseType.setPhotoImages(exercisePhotoLookup.containsKey(exerciseType.getId()) ? exercisePhotoLookup.get(exercise.getExerciseType().getId()) : new ArrayList<String>());
                    }

                } catch (RetrofitError er) {
                    // this is the unauthorized status
                    if (er.getResponse().getStatus() == 401) {
                        // TODO: we should think what shall happen it the user is not authorized
                        // (we need a mechanizm that will be used through the application)
                    }
                }

                // save training to db
                new TrainingPlan(trainingDescription.getName(), Utils.getGsonObject().toJson(training), trainingDescription.getId(), trainingDescription.getUpdatedDate().getTime()).save();
            }
            EventBus.getDefault().post(new DismissProgressEvent());
            ActiveAndroid.setTransactionSuccessful();
            return new EndDownloadTrainingsEvent(true);
        } catch(Exception ex){
            return new EndDownloadTrainingsEvent(false, ex.getMessage());
       } finally {
            ActiveAndroid.endTransaction();
        }
    }

    /**
     * This method gets all the locally stored completed trainings and uploads
     * them to the remote server. All the successfully uploaded trainings are
     * deleted from the local database.
     *
     * @return
     */
    private EndUploadCompletedTrainings uploadCompletedTrainings() {
        List<CompletedTraining> completedTrainings = CompletedTraining.getAll();

        for(CompletedTraining training : completedTrainings){
            Measurement measurement = Utils.getGsonObject().fromJson(training.getData(), Measurement.class);
            try {
                Response response = service.uploadMeasurement(measurement);
                if(response.getStatus()==200){
                    training.delete();
                }
            }catch (Exception ex){
                // this might fail in the middle (some uploads might succeed, others fail).
                // in such cases the user should just try to run this once more
                new EndUploadCompletedTrainings(false);
            }

        }

		return new EndUploadCompletedTrainings(true);
    }

    /**
     * This method checks if the input @param username and @param password are
     * valid.
     *
     * @return <code>true</code> if they are, otherwise <code>false</code>.
     */
    private LoginEvent checkCredentials(String username, String password) {
        try {
            LoginData loginData = new LoginData();
            loginData.email = username;
            loginData.password = password;
            Response cred = service.login(loginData);
            for (Header header : cred.getHeaders()) {
                if (header.getName().equals("Set-Cookie") && header.getValue().startsWith("_trainerjim_session")) {
                    ApiHeaders.getInstance(this).setSessionId(header.getValue());
                }
            }

            LoginResponseData loginResponseData = (LoginResponseData) new GsonConverter(Utils.getGsonObject()).fromBody(cred.getBody(), LoginResponseData.class);
            return new LoginEvent(true, loginResponseData.id);
        } catch (Exception ex) {
            // TODO: add more descriptive error messages
            return new LoginEvent(false, "Login: " + ex.getMessage());
        }
    }
}
