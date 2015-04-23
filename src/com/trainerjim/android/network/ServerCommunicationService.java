package com.trainerjim.android.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trainerjim.android.R;
import com.trainerjim.android.entities.ExerciseTypeImagesItem;
import com.trainerjim.android.entities.Training;
import com.trainerjim.android.entities.TrainingDescription;
import com.trainerjim.android.storage.TrainingContentProvider.CompletedTraining;
import com.trainerjim.android.storage.TrainingContentProvider.TrainingPlan;
import com.trainerjim.android.util.HttpsHelpers;
import com.trainerjim.android.util.Utils;

import retrofit.RestAdapter;
import retrofit.client.Response;

/**
 * This service is the central gateway for all the communication with the remote
 * server.
 * 
 * @author Igor
 * 
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
	 * Instance of the engine used for json serialization.
	 */
	private Gson mJsonEngine = new Gson();

	/**
	 * The number of completed trainings stored in the local database.
	 */
	private int mNumCompletedTrainings = 0;

	/**
	 * Http client instance used for server communication.
	 */
	private HttpClient mHttpClient;

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

		mHttpClient = initializeHttpClient();

    }

    /**
     * This method returns a freshly initialized Http Client.
     * @return
     */
    private HttpClient initializeHttpClient(){
        // TODO: Dangerous client, change ASAP
        return HttpsHelpers.getDangerousHttpClient();
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
		String username = intent.getExtras().getString(INTENT_KEY_USERNAME);
		String password = intent.getExtras().getString(INTENT_KEY_PASSWORD);

        // initialize rest communication channel
        restAdapter = new RestAdapter.Builder().setEndpoint(getResources().getString(R.string.server_url)).build();
        service = restAdapter.create(TrainerJimService.class);


        // this one will hold the status of the service task (each action should
		// return a status)
		Intent statusIntent = new Intent();
		statusIntent.addCategory(Intent.CATEGORY_DEFAULT);

		if (action.equals(ACTION_UPLOAD_COMPLETED_TRAININGS)) {
			int counterSuccess = uploadCompletedTrainings(username, password);

			// prepare the status intent
			statusIntent.setAction(ACTION_UPLOAD_TRAINNGS_COMPLETED);
			statusIntent.putExtra(PARAM_UPLOAD_TRAINING_SUCESS_CNT,
					counterSuccess);
			statusIntent.putExtra(PARAM_UPLOAD_TRAINING_NUM_ALL_ITEMS,
					mNumCompletedTrainings);
		} else if (action.equals(ACTION_CHECK_CREDENTIALS)) {
            String statusMessage = null;

            boolean loginSuccessful = false;
            try {
                loginSuccessful = checkCredentials(username, password);
                if(!loginSuccessful){
                    statusMessage = "Wrong username or password.";
                }

            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
                statusMessage = "Unable to connect to trainerjim server.";
            }

            // prepare the status intent
			statusIntent.setAction(ACTION_LOGIN_COMPLETED);
			statusIntent.putExtra(PARAM_ACTION_SUCCESSFUL, loginSuccessful);
            statusIntent.putExtra(PARAM_ACTION_MSG, statusMessage);

		} else if (action.equals(ACTION_FETCH_TRAININGS)) {
			boolean getTrainingsSucessful = fetchTrainings(username, password);

            // fetch images for all available exercises
            // TODO: at some point this should be constrained only to fetching images
            // for exercises that are contained in the training plains assigned to the user
            if(getTrainingsSucessful){
                fetchImages();
            }

			// send status intent
			statusIntent.setAction(ACTION_FETCH_TRAINNGS_COMPLETED);
			statusIntent.putExtra(PARAM_ACTION_SUCCESSFUL, getTrainingsSucessful);
            statusIntent.putExtra(PARAM_ACTION_MSG, "Unable to fetch trainings.");
		}

		sendBroadcast(statusIntent);
	}

    /**
     * This methods fetches all medium sized images exposed by the backend through
     * 'training/exercise_types.json' API. The images are just saved to the persisted storage.
     *
     * If an error occurs it is gracefully ignored (might be better to handle this differently
     * in the future). Additionally, currently there is no checking if the image already exists,
     * Even if it exists it is still downloaded repeatedly - a possile solution would be hash or
     * filename checking.
     */
    private void fetchImages(){

        HttpGet httpget = new HttpGet(String.format("%s%s",
                getResources().getString(R.string.server_url),
                getResources().getString(R.string.server_path_exercise_types_list)));

        HttpResponse response = null;
        try {
            response = mHttpClient.execute(httpget);

            int status = response.getStatusLine().getStatusCode();

            if(status == HttpStatus.SC_OK){
                String jsonExercisesList = extractJsonFromStream(response.getEntity().getContent());

                Type listType = new TypeToken<ArrayList<ExerciseTypeImagesItem>>(){}.getType();
                List<ExerciseTypeImagesItem> exerciseImagesList = mJsonEngine.fromJson(jsonExercisesList, listType);

                // create a list of exercise images that are not yet downloaded (based on image name)
                List<ExerciseTypeImagesItem> exercisesToDownload = new ArrayList<ExerciseTypeImagesItem>();
                for (int i=0 ; i < exerciseImagesList.size(); i++){
                    if(!new File(Utils.getDataFolderFile(getApplicationContext()), Integer.toString(exerciseImagesList.get(i).getId())).exists()) {
                        exercisesToDownload.add(exerciseImagesList.get(i));
                    }
                }

                int cnt = 0;
                // for each exercise fetch the image and save it to the persistent storage
                for (ExerciseTypeImagesItem item : exercisesToDownload){
                    String imageUrl = String.format("%s%s",
                            getResources().getString(R.string.server_url),
                            item.getMediumImageUrl());

                    HttpGet httpgetImage = new HttpGet(imageUrl);

                    HttpResponse responseImage = mHttpClient.execute(httpgetImage);
                    int statusImage = responseImage.getStatusLine().getStatusCode();

                    Intent broadcastIntent = new Intent();
                    broadcastIntent
                            .setAction(ACTION_REPORT_PROGRESS);
                    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    broadcastIntent.putExtra(
                            PARAM_REPORT_PROGRESS_TEXT, item.getShortName());
                    broadcastIntent.putExtra(PARAM_REPORT_PROGRESS_CURRENT,
                            cnt++);
                    broadcastIntent.putExtra(
                            PARAM_REPORT_PROGRESS_TOTAL,
                            exercisesToDownload.size());
                    sendBroadcast(broadcastIntent);

                    if(statusImage == HttpStatus.SC_OK){
                        InputStream is = responseImage.getEntity().getContent();
                        BufferedInputStream bis = new BufferedInputStream(is);

                       /*
                        * Read bytes to the Buffer until there is nothing more to read(-1).
                        */
                        ByteArrayBuffer baf = new ByteArrayBuffer(5000);
                        int current = 0;
                        while ((current = bis.read()) != -1) {
                            baf.append((byte) current);
                        }

                       /* Convert the Bytes read to a String. */
                        FileOutputStream fos = new FileOutputStream(new File(Utils.getDataFolderFile(getApplicationContext()), Integer.toString(item.getId())));
                        fos.write(baf.toByteArray());
                        fos.flush();
                        fos.close();
                    } else {
                        // TODO: This is a very hacky approach. The problem is that on 404 status
                        // http entity is not fully consumed (calling consumeEntity) does not help
                        // either and the httpclient hangs after 2 successive 404 calls. To overcome
                        // this temporarily a new client is initialized on every unsuccessful http call.
                        mHttpClient = initializeHttpClient();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

	/**
	 * @param username
	 * @param password
	 * @return
	 */
	private boolean fetchTrainings(String username, String password) {

        List<TrainingDescription> trainingsManifest = service.getTrainingsList(username, password);

        if(trainingsManifest != null){
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_GET_TRAINNGS_LIST_COMPLETED);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(PARAM_REPORT_PROGRESS_TOTAL,
                    trainingsManifest.size());
            sendBroadcast(broadcastIntent);

            // clear the database
            getContentResolver().delete(TrainingPlan.CONTENT_URI, null,
                    null);

            for (int i = 0; i < trainingsManifest.size(); i++) {
                TrainingDescription trainingDescription = trainingsManifest.get(i);

                // notify that we are fetching a specific training
                broadcastIntent = new Intent();
                broadcastIntent
                        .setAction(ACTION_REPORT_PROGRESS);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra(
                        PARAM_REPORT_PROGRESS_TEXT, trainingDescription.getName());
                broadcastIntent.putExtra(PARAM_REPORT_PROGRESS_CURRENT,
                        i);
                broadcastIntent.putExtra(
                        PARAM_REPORT_PROGRESS_TOTAL,
                        trainingsManifest.size());
                sendBroadcast(broadcastIntent);

                Response trainingResponse = service.getTraining(username, password, trainingDescription.getId());
                String training = null;

                try {
                    training = extractJsonFromStream(trainingResponse.getBody().in());
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO: implement logging
                }

                if (training != null) {
                    ContentValues trainingPlan = new ContentValues();
                    trainingPlan.put(TrainingPlan._ID, trainingDescription.getId());
                    trainingPlan.put(TrainingPlan.NAME, trainingDescription.getName());
                    trainingPlan.put(TrainingPlan.DATA, training);

                    getContentResolver().insert(TrainingPlan.CONTENT_URI,
                            trainingPlan);
                }
            }
        }

        return true;
	}

	/**
	 * This method gets all the locally stored completed trainings and uploads
	 * them to the remote server. All the successfully uploaded trainings are
	 * deleted from the local database. NOTE: As a side effect this method
	 * changes the mNumCompletedTrainings field to contain the number trainings
	 * initially stored in the local database.
	 * 
	 * @param username
	 * @param password
	 * @return the number of successfully uploaded completed trainings
	 */
	private int uploadCompletedTrainings(String username, String password) {
		// fetch from local database
		String[] projection = { CompletedTraining._ID, CompletedTraining.DATA };
		Cursor trainings = getContentResolver().query(
				CompletedTraining.CONTENT_URI, projection, null, null, null);

		// store the number of completed trainings
		mNumCompletedTrainings = trainings.getCount();

		int counterSuccess = 0;
		if (mNumCompletedTrainings > 0) {
			// there are some trainings to upload, notify that we have started
			// uploading trainings
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ACTION_UPLOAD_TRAININGS_STARTED);
			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			broadcastIntent.putExtra(PARAM_UPLOAD_TRAINING_NUM_ALL_ITEMS,
					mNumCompletedTrainings);
			sendBroadcast(broadcastIntent);

			// this counter is used by the progress dialog to show the upload
			// progress
			int trainingCounter = 0;
			while (trainings.moveToNext()) {
				int trainingId = trainings.getInt(trainings
						.getColumnIndex(TrainingPlan._ID));
				Training trainingToUpload = mJsonEngine.fromJson(
						trainings.getString(trainings
								.getColumnIndex(TrainingPlan.DATA)),
						Training.class);

				// upload each training
				boolean uploadStatus = uploadTraining(trainingId,
						trainingToUpload, username, password);

				// notify the uploading progress
				broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_TRAININGS_ITEM_UPLOADED);
				broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
				broadcastIntent.putExtra(PARAM_UPLOAD_TRAINING_ITEM_NAME,
						trainingToUpload.getTrainingName());
				broadcastIntent.putExtra(PARAM_UPLOAD_TRAINING_CUR_ITEM_CNT,
						trainingCounter);
				broadcastIntent.putExtra(PARAM_UPLOAD_TRAINING_ITEM_STATUS,
						uploadStatus);
				broadcastIntent.putExtra(PARAM_UPLOAD_TRAINING_NUM_ALL_ITEMS,
						mNumCompletedTrainings);
				sendBroadcast(broadcastIntent);

				// increase the counts
				trainingCounter++;
				if (uploadStatus) {
					counterSuccess++;
				}
			}
		}
		return counterSuccess;
	}

	/**
	 * This method uploads a @param training locally specified with the @param
	 * trainingId to the remote server.
	 * 
	 * @param username
	 * @param password
	 * @return <code>true</code> if upload was successful, otherwise
	 *         <code>false</code>.
	 */
	private boolean uploadTraining(int trainingId, Training training,
			String username, String password) {

		// create a zip file that will containt the training to upload
		File trainingZip = training.getZipFile(getApplicationContext());
		training.zipToFile(
				getResources().getString(R.string.training_mainfest_name),
				getResources().getString(R.string.raw_data_name),
                getResources().getBoolean(R.bool.sample_acceleration),getApplicationContext());

		try {
			String url = String.format("%s%s",
					getResources().getString(R.string.server_url),
					getResources().getString(R.string.server_path_upload));

			Log.d(TAG, "Upload training to " + url);

			HttpPost httppost = new HttpPost(url);

			MultipartEntity multipartEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			multipartEntity.addPart(
					getResources().getString(R.string.param_email),
					new StringBody(username));
			multipartEntity.addPart(
					getResources().getString(R.string.param_password),
					new StringBody(password));

			multipartEntity.addPart(
					getResources().getString(R.string.param_zipfile),
					new FileBody(trainingZip));
			httppost.setEntity(multipartEntity);

			HttpResponse response = mHttpClient.execute(httppost);
			int status = response.getStatusLine().getStatusCode();

            if(response.getEntity() != null){
                response.getEntity().consumeContent();
            }

			if (status == HttpStatus.SC_OK) {
				// if upload was successful delete training from the local
				// database
				getContentResolver().delete(
						ContentUris.withAppendedId(
								CompletedTraining.CONTENT_URI, trainingId),
						null, null);

				// delete the raw data
				training.getRawFile(getApplicationContext()).delete();

				if (!getResources().getBoolean(R.bool.research_mode)) {
					// keep the zip file in research mode, otherwise delete it
					training.getZipFile(getApplicationContext()).delete();
				}
			} else {
                return false;
            }

			return true;
		} catch (Exception e) {

			Log.e(TAG, e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * This method returns a JSON encoded training corresponding to the input
	 * trainingID.
	 * 
	 * @param trainingId
	 * @param username
	 * @param password
	 * @return a JSON encoded training string or null if Http return status code
	 *         was not OK.
	 * @throws IOException
	 *             if unable to extract stream from JSON
	 * @throws ClientProtocolException
	 *             could happen during Http comunication
	 */
	private String getTraining(int trainingId, String username, String password)
			throws ClientProtocolException, IOException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", username));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("id", Integer.toString(trainingId)));

		String url = String.format("%s%s?%s",
				getResources().getString(R.string.server_url), getResources()
						.getString(R.string.server_path_training),
				URLEncodedUtils.format(params, "utf-8"));

		Log.d(TAG, "Get training with " + url);

		HttpGet httpget = new HttpGet(url);

		HttpResponse response = mHttpClient.execute(httpget);
		int status = response.getStatusLine().getStatusCode();

		return status == HttpStatus.SC_OK ? extractJsonFromStream(response
				.getEntity().getContent()) : null;
	}

	/**
	 * This method extracts a JSON string from stream.
	 * 
	 * @param content
	 * @return
	 * @throws IOException
	 *             could happen while reading the from stream.
	 */
	private static String extractJsonFromStream(InputStream content)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				content));
		String line;
		StringBuilder builder = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		return builder.toString();
	}

	/**
	 * This method checks if the input @param username and @param password are
	 * valid.
	 * 
	 * @return <code>true</code> if they are, otherwise <code>false</code>.
	 */
	private boolean checkCredentials(String username, String password) {
		return service.checkCredentials(username, password);
	}

	/**
	 * This method returns the list of all the trainings for the user specified
	 * by the @param username.
	 * 
	 * @param username
	 * @param password
	 * @return JSON encoded string of all the trainings or <code>null</code> if
	 *         the list could not be retrieved.
	 */
	private String getTrainingList(String username, String password) {
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", username));
			params.add(new BasicNameValuePair("password", password));

			String url = String.format("%s%s?%s",
					getResources().getString(R.string.server_url),
					getResources()
							.getString(R.string.server_path_training_list),
					URLEncodedUtils.format(params, "utf-8"));

			Log.d(TAG, "Get training list from " + url);

			HttpGet httpget = new HttpGet(url);

			HttpResponse response = mHttpClient.execute(httpget);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// convert the response content to a string (containing JSON
				// encoded list of trainings)
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				StringBuilder builder = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				return builder.toString();
			}
		} catch (Exception e) {
			Log.e(TAG, e.getLocalizedMessage());
		}

		return null;
	}
}
