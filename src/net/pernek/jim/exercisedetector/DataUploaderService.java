package net.pernek.jim.exercisedetector;

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

import net.pernek.jim.exercisedetector.database.TrainingContentProvider.CompletedTraining;
import net.pernek.jim.exercisedetector.database.TrainingContentProvider.TrainingPlan;
import net.pernek.jim.exercisedetector.entities.Measurement;
import net.pernek.jim.exercisedetector.entities.Training;
import net.pernek.jim.exercisedetector.util.HttpsHelpers;
import net.pernek.jim.exercisedetector.util.Utils;

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

// TODO: rename file to e.g. server communicator (this service will do
// all the server calls)
public class DataUploaderService extends IntentService {

	private static final String TAG = Utils.getApplicationTag();

	public static final String ACTION_DONE = "action_done";
	public static final String ACTION_FETCH_TRAINNGS_DONE = "fetch_trainings_done";
	public static final String ACTION_FETCH_TRAINNGS_LIST_DOWNLOADED = "fetch_trainings_list_downloaded";
	public static final String ACTION_FETCH_TRAINNGS_ITEM_DOWNLOADED = "fetch_trainings_item_downloaded";
	public static final String ACTION_UPLOAD_TRAINNGS_DONE = "upload_trainings_done";
	public static final String ACTION_UPLOAD_TRAININGS_STARTED = "upload_completed_trainings_started";
	public static final String ACTION_UPLOAD_TRAININGS_ITEM_UPLOADED = "upload_completed_trainings_item_uploaded";
	public static final String PARAM_OP_SUCCESSFUL = "operation_successful";
	public static final String PARAM_UPLOAD_TRAINING_NUM_ITEMS = "upload_completed_training_num_items";
	public static final String PARAM_UPLOAD_TRAINING_ITEM_NAME = "upload_training_item_name";
	public static final String PARAM_UPLOAD_TRAINING_STATUS = "upload_training_status";
	public static final String PARAM_UPLOAD_TRAINING_CUR_ITEM_CNT = "upload_training_cur_item_count";
	public static final String PARAM_UPLOAD_TRAINING_SUCESS_CNT = "upload_training_sucess_count";
	public static final String PARAM_FETCH_TRAINNGS_NUM_ITEMS = "fetch_trainings_num_items";
	public static final String PARAM_FETCH_TRAINNGS_CUR_ITEM_NAME = "fetch_trainings_cur_item_name";
	public static final String PARAM_FETCH_TRAINNGS_CUR_ITEM_CNT = "fetch_trainings_cur_item_cnt";
	public static final String PARAM_GET_TRAINING_SUCCESSFUL = "get_training_successful";

	public static final String ACTION_UPLOAD = "action-upload";
	public static final String ACTION_FETCH_TRAININGS = "action-get-training-list";
	public static final String ACTION_GET_TRAINING = "action-get-training";
	public static final String ACTION_LOGIN = "action-login";

	public static final String INTENT_KEY_ACTION = "action";
	public static final String INTENT_KEY_FILE = "file";
	public static final String INTENT_KEY_TRAINING_ID = "training-id";
	public static final String INTENT_KEY_USERNAME = "username";
	public static final String INTENT_KEY_PASSWORD = "password";

	private Gson jsonParser = new Gson();

	public DataUploaderService() {
		super("DataUploaderService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getExtras().getString(INTENT_KEY_ACTION);
		String username = intent.getExtras().getString(INTENT_KEY_USERNAME);
		String password = intent.getExtras().getString(INTENT_KEY_PASSWORD);

		if (action.equals(ACTION_UPLOAD)) {
			String[] projection = { CompletedTraining._ID,
					CompletedTraining.DATA };
			Cursor trainings = getContentResolver()
					.query(CompletedTraining.CONTENT_URI, projection, null,
							null, null);

			int numOfTrainings = trainings.getCount();

			int counterSuccess = 0;
			if (numOfTrainings > 0) {
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_UPLOAD_TRAININGS_STARTED);
				broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
				broadcastIntent.putExtra(PARAM_UPLOAD_TRAINING_NUM_ITEMS,
						numOfTrainings);
				sendBroadcast(broadcastIntent);

				int trainingCounter = 0;
				while (trainings.moveToNext()) {
					int trainingId = trainings.getInt(trainings
							.getColumnIndex(TrainingPlan._ID));
					Training trainingToUpload = jsonParser.fromJson(trainings
							.getString(trainings
									.getColumnIndex(TrainingPlan.DATA)),
							Training.class);
					
					broadcastIntent = new Intent();
					broadcastIntent
							.setAction(ACTION_UPLOAD_TRAININGS_ITEM_UPLOADED);
					broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
					broadcastIntent.putExtra(PARAM_UPLOAD_TRAINING_ITEM_NAME,
							trainingToUpload.getName());
					broadcastIntent
							.putExtra(PARAM_UPLOAD_TRAINING_CUR_ITEM_CNT,
									trainingCounter);
					broadcastIntent.putExtra(PARAM_UPLOAD_TRAINING_NUM_ITEMS,
							numOfTrainings);
					sendBroadcast(broadcastIntent);

					boolean uploadStatus = uploadTraining(trainingId,
							trainingToUpload, username, password);

					if (uploadStatus) {
						counterSuccess++;
					}

					trainingCounter++;
				}
			}

			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ACTION_UPLOAD_TRAINNGS_DONE);
			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			broadcastIntent.putExtra(PARAM_UPLOAD_TRAINING_SUCESS_CNT,
					counterSuccess);
			broadcastIntent.putExtra(PARAM_UPLOAD_TRAINING_NUM_ITEMS,
					numOfTrainings);
			sendBroadcast(broadcastIntent);

			// uploadFile(path);

			// get all completed trainings from the database

			// send them to the server
		} else if (action.equals(ACTION_LOGIN)) {
			boolean loginSuccessful = checkCredentials(username, password);

			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ACTION_DONE);
			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			broadcastIntent.putExtra(PARAM_OP_SUCCESSFUL, loginSuccessful);
			sendBroadcast(broadcastIntent);

		} else if (action.equals(ACTION_FETCH_TRAININGS)) {
			boolean getTrainingsSucessful = false;
			try {
				// get list of all trainings for the user
				String jsonTrainingPlans = getTrainingList(username, password);
				getTrainingsSucessful = jsonTrainingPlans != null;

				JSONArray jaTrainingsPlans = null;
				if (getTrainingsSucessful) {
					jaTrainingsPlans = new JSONArray(jsonTrainingPlans);
					Intent broadcastIntent = new Intent();
					broadcastIntent
							.setAction(ACTION_FETCH_TRAINNGS_LIST_DOWNLOADED);
					broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
					broadcastIntent.putExtra(PARAM_FETCH_TRAINNGS_NUM_ITEMS,
							jaTrainingsPlans.length());
					sendBroadcast(broadcastIntent);

				}

				// download each training and save all the information to be
				// later
				// written to the database
				List<Integer> trainingIds = new ArrayList<Integer>();
				List<String> trainingNames = new ArrayList<String>();
				List<String> fetchedTrainings = new ArrayList<String>();

				for (int i = 0; i < jaTrainingsPlans.length(); i++) {
					JSONObject joTrainingPlan = (JSONObject) jaTrainingsPlans
							.get(i);

					int trainingId = joTrainingPlan.getInt("id");
					String trainingName = joTrainingPlan.getString("name");

					Intent broadcastIntent = new Intent();
					broadcastIntent
							.setAction(ACTION_FETCH_TRAINNGS_ITEM_DOWNLOADED);
					broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
					broadcastIntent.putExtra(
							PARAM_FETCH_TRAINNGS_CUR_ITEM_NAME, trainingName);
					broadcastIntent.putExtra(PARAM_FETCH_TRAINNGS_CUR_ITEM_CNT,
							i);
					broadcastIntent.putExtra(PARAM_FETCH_TRAINNGS_NUM_ITEMS,
							jaTrainingsPlans.length());
					sendBroadcast(broadcastIntent);

					String jsonTraining = getTraining(trainingId, username,
							password);
					if (jsonTraining == null) {
						getTrainingsSucessful = false;
						break;
					}

					trainingIds.add(trainingId);
					trainingNames.add(trainingName);
					fetchedTrainings.add(jsonTraining);
				}

				if (getTrainingsSucessful) {
					// clear the database
					getContentResolver().delete(TrainingPlan.CONTENT_URI, null,
							null);

					// store fetched trainings to the database
					for (int i = 0; i < fetchedTrainings.size(); i++) {
						ContentValues trainingPlan = new ContentValues();
						trainingPlan.put(TrainingPlan._ID, trainingIds.get(i));
						trainingPlan.put(TrainingPlan.NAME,
								trainingNames.get(i));
						trainingPlan.put(TrainingPlan.DATA,
								fetchedTrainings.get(i));

						getContentResolver().insert(TrainingPlan.CONTENT_URI,
								trainingPlan);
					}

				}
			} catch (Exception e) {
				getTrainingsSucessful = false;
				e.printStackTrace();
				Log.e(TAG, "Get training list json exeception");
			}

			// send status intent
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ACTION_FETCH_TRAINNGS_DONE);
			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			broadcastIntent
					.putExtra(PARAM_OP_SUCCESSFUL, getTrainingsSucessful);
			sendBroadcast(broadcastIntent);

		} else if (action.equalsIgnoreCase(ACTION_GET_TRAINING)) {
			int trainingId = intent.getExtras().getInt(INTENT_KEY_TRAINING_ID);

			String jsonTraining = null;
			try {
				jsonTraining = getTraining(trainingId, username, password);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				Log.e(TAG, e.getLocalizedMessage());
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, e.getLocalizedMessage());
			}

			Intent broadcastIntent = new Intent();
			broadcastIntent
					.setAction(ExerciseDetectorActivity.ResponseReceiver.ACTION_TRAINING_PLAN_DOWNLOADED);
			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			if (jsonTraining != null) {
				broadcastIntent
						.putExtra(
								ExerciseDetectorActivity.ResponseReceiver.PARAM_TRAINING_PLAN,
								jsonTraining);
			}
			broadcastIntent.putExtra(
					UploadSessionActivity.ResponseReceiver.PARAM_STATUS,
					jsonTraining != null);
			sendBroadcast(broadcastIntent);
		}
	}

	private boolean uploadTraining(int trainingId, Training training,
			String username, String password) {

		File trainingZip = new File(Utils.getUploadDataFolderFile(),
				training.getZipFilename());

		// first write training to disc
		Measurement measurement = training.extractMeasurement();
		measurement.zipToFile(trainingZip);

		HttpClient httpClient = HttpsHelpers.getDangerousHttpClient();

		try {
			String url = String.format("%s%s",
					getResources().getString(R.string.server_url),
					getResources().getString(R.string.server_path_upload));

			Log.d(TAG, "Upload training with " + url);

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

			HttpResponse response = httpClient.execute(httppost);
			int status = response.getStatusLine().getStatusCode();

			if (status == HttpStatus.SC_OK) {
				// delete from the local database
				getContentResolver().delete(
						ContentUris.withAppendedId(
								CompletedTraining.CONTENT_URI, trainingId),
						null, null);

				// TODO also delete the file from disk here
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
		HttpClient httpClient = HttpsHelpers.getDangerousHttpClient();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", username));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("id", Integer.toString(trainingId)));

		String url = String.format(
				"%s?%s",
				getResources().getString(R.string.server_url)
						+ getResources().getString(
								R.string.server_path_training),
				URLEncodedUtils.format(params, "utf-8"));

		Log.d(TAG, "Get training with " + url);

		HttpGet httpget = new HttpGet(url);

		HttpResponse response = httpClient.execute(httpget);
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

	private boolean checkCredentials(String username, String password) {
		HttpClient httpClient = HttpsHelpers.getDangerousHttpClient();
		boolean loginSuccessful;

		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", username));
			params.add(new BasicNameValuePair("password", password));

			String url = getResources().getString(R.string.server_url)
					+ getResources().getString(R.string.server_path_auth);

			Log.d(TAG, "Check credentials on " + url);

			HttpGet httpget = new HttpGet(String.format("%s?%s", url,
					URLEncodedUtils.format(params, "utf-8")));

			HttpResponse response = httpClient.execute(httpget);

			int status = response.getStatusLine().getStatusCode();

			if (status == HttpStatus.SC_OK) {
				loginSuccessful = Boolean
						.parseBoolean(extractJsonFromStream(response
								.getEntity().getContent()));
			} else {
				loginSuccessful = false;
			}

		} catch (Exception e) {
			loginSuccessful = false;
			Log.e(TAG, e.getLocalizedMessage());
		}

		return loginSuccessful;
	}

	private String getTrainingList(String username, String password) {
		HttpClient httpClient = HttpsHelpers.getDangerousHttpClient();

		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", username));
			params.add(new BasicNameValuePair("password", password));

			String url = getResources().getString(R.string.server_url)
					+ getResources().getString(
							R.string.server_path_training_list);

			Log.d(TAG, "Get training list from " + url);

			HttpGet httpget = new HttpGet(String.format("%s?%s", url,
					URLEncodedUtils.format(params, "utf-8")));

			HttpResponse response = httpClient.execute(httpget);
			int status = response.getStatusLine().getStatusCode();

			if (status == 200) {
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
