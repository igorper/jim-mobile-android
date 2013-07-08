package com.trainerjim.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.pernek.jim.exercisedetector.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;
import com.trainerjim.android.database.TrainingContentProvider.CompletedTraining;
import com.trainerjim.android.database.TrainingContentProvider.TrainingPlan;
import com.trainerjim.android.entities.Measurement;
import com.trainerjim.android.entities.Training;
import com.trainerjim.android.util.HttpsHelpers;
import com.trainerjim.android.util.Utils;

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
	 * This ID marks if the login operation was successful or not.
	 */
	public static final String PARAM_LOGIN_SUCCESSFUL = "login_successful";

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
	public static final String ACTION_FETCH_TRAINNG_ITEM_COMPLETED = "fetch_trainings_item_downloaded";

	/**
	 * This ID marks the number of all trainings to fetch from the server.
	 */
	public static final String PARAM_FETCH_TRAINNGS_NUM_ALL_ITEMS = "fetch_trainings_num_items";

	/**
	 * This ID marks the name of the item being currently fetched from the
	 * server.
	 */
	public static final String PARAM_FETCH_TRAINNGS_CUR_ITEM_NAME = "fetch_trainings_cur_item_name";

	/**
	 * This ID marks the current count of the trainings downloaded from the
	 * server (used for progress visualization).
	 */
	public static final String PARAM_FETCH_TRAINNGS_CUR_ITEM_CNT = "fetch_trainings_cur_item_cnt";

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

	public ServerCommunicationService() {
		super("DataUploaderService");

		// TODO: Dangerous client, change ASAP
		mHttpClient = HttpsHelpers.getDangerousHttpClient();
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
			boolean loginSuccessful = checkCredentials(username, password);

			// prepare the status intent
			statusIntent.setAction(ACTION_LOGIN_COMPLETED);
			statusIntent.putExtra(PARAM_LOGIN_SUCCESSFUL, loginSuccessful);

		} else if (action.equals(ACTION_FETCH_TRAININGS)) {
			boolean getTrainingsSucessful = fetchTrainings(username, password);

			// send status intent
			statusIntent.setAction(ACTION_FETCH_TRAINNGS_COMPLETED);
			statusIntent
					.putExtra(PARAM_LOGIN_SUCCESSFUL, getTrainingsSucessful);

		}

		sendBroadcast(statusIntent);
	}

	/**
	 * @param username
	 * @param password
	 * @return
	 */
	private boolean fetchTrainings(String username, String password) {
		boolean wasSucessful;
		try {
			// get list of all trainings for the user
			String jsonTrainingPlans = getTrainingList(username, password);
			wasSucessful = jsonTrainingPlans != null;

			JSONArray jaTrainingsPlans = null;
			if (wasSucessful) {
				// notify that the trainings list was successfully downloaded
				jaTrainingsPlans = new JSONArray(jsonTrainingPlans);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_GET_TRAINNGS_LIST_COMPLETED);
				broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
				broadcastIntent.putExtra(PARAM_FETCH_TRAINNGS_NUM_ALL_ITEMS,
						jaTrainingsPlans.length());
				sendBroadcast(broadcastIntent);
			}

			// download each training and save the information for later
			// database storage
			List<Integer> trainingIds = new ArrayList<Integer>();
			List<String> trainingNames = new ArrayList<String>();
			List<String> fetchedTrainings = new ArrayList<String>();

			for (int i = 0; i < jaTrainingsPlans.length(); i++) {
				JSONObject joTrainingPlan = (JSONObject) jaTrainingsPlans
						.get(i);

				int trainingId = joTrainingPlan.getInt("id");
				String trainingName = joTrainingPlan.getString("name");

				// notify that we are fetching a specific training
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_FETCH_TRAINNG_ITEM_COMPLETED);
				broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
				broadcastIntent.putExtra(PARAM_FETCH_TRAINNGS_CUR_ITEM_NAME,
						trainingName);
				broadcastIntent.putExtra(PARAM_FETCH_TRAINNGS_CUR_ITEM_CNT, i);
				broadcastIntent.putExtra(PARAM_FETCH_TRAINNGS_NUM_ALL_ITEMS,
						jaTrainingsPlans.length());
				sendBroadcast(broadcastIntent);

				// break on error (get all or nothing)
				String jsonTraining = getTraining(trainingId, username,
						password);
				if (jsonTraining == null) {
					wasSucessful = false;
					break;
				}

				trainingIds.add(trainingId);
				trainingNames.add(trainingName);
				fetchedTrainings.add(jsonTraining);
			}

			if (wasSucessful) {
				// clear the database
				getContentResolver().delete(TrainingPlan.CONTENT_URI, null,
						null);

				// store fetched trainings to the database
				for (int i = 0; i < fetchedTrainings.size(); i++) {
					ContentValues trainingPlan = new ContentValues();
					trainingPlan.put(TrainingPlan._ID, trainingIds.get(i));
					trainingPlan.put(TrainingPlan.NAME, trainingNames.get(i));
					trainingPlan
							.put(TrainingPlan.DATA, fetchedTrainings.get(i));

					getContentResolver().insert(TrainingPlan.CONTENT_URI,
							trainingPlan);
				}

			}
		} catch (Exception e) {
			wasSucessful = false;
			e.printStackTrace();
			Log.e(TAG, "Get training list json exeception");
		}
		return wasSucessful;
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
						trainingToUpload.getName());
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
		File trainingZip = training.getZipFile();
		training.zipToFile(
				getResources().getString(R.string.training_mainfest_name),
				getResources().getString(R.string.raw_data_name));

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

			if (status == HttpStatus.SC_OK) {
				// if upload was successful delete training from the local
				// database
				getContentResolver().delete(
						ContentUris.withAppendedId(
								CompletedTraining.CONTENT_URI, trainingId),
						null, null);

				// delete the raw data
				training.getRawFile().delete();

				if (!getResources().getBoolean(R.bool.research_mode)) {
					// keep the zip file in research mode, otherwise delete it
					training.getZipFile().delete();
				}
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
		boolean loginSuccessful;

		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", username));
			params.add(new BasicNameValuePair("password", password));

			String url = String.format("%s%s?%s",
					getResources().getString(R.string.server_url),
					getResources().getString(R.string.server_path_auth),
					URLEncodedUtils.format(params, "utf-8"));

			Log.d(TAG, "Check credentials on " + url);

			HttpGet httpget = new HttpGet(url);

			HttpResponse response = mHttpClient.execute(httpget);

			int status = response.getStatusLine().getStatusCode();

			loginSuccessful = (status == HttpStatus.SC_OK) ? Boolean
					.parseBoolean(extractJsonFromStream(response.getEntity()
							.getContent())) : false;

		} catch (Exception e) {
			loginSuccessful = false;
			Log.e(TAG, e.getLocalizedMessage());
		}

		return loginSuccessful;
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
