package net.pernek.jim.exercisedetector;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.pernek.jim.exercisedetector.database.TrainingContentProvider.CompletedTraining;
import net.pernek.jim.exercisedetector.database.TrainingContentProvider.TrainingPlan;
import net.pernek.jim.exercisedetector.entities.Measurement;
import net.pernek.jim.exercisedetector.entities.Training;
import net.pernek.jim.exercisedetector.util.Compress;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import com.google.gson.Gson;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.text.Html;
import android.util.Log;
import android.webkit.JsPromptResult;

// TODO: rename file to e.g. server communicator (this service will do
// all the server calls)
public class DataUploaderService extends IntentService {

	private static final String TAG = Utils.getApplicationTag();

	public static final String ACTION_DONE = "action_done";
	public static final String ACTION_FETCH_TRAINNGS_DONE = "fetch_trainings_done";
	public static final String ACTION_FETCH_TRAINNGS_LIST_DOWNLOADED = "fetch_trainings_list_downloaded";
	public static final String ACTION_FETCH_TRAINNGS_ITEM_DOWNLOADED = "fetch_trainings_item_downloaded";
	public static final String PARAM_OP_SUCCESSFUL = "operation_successful";
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

	// those should be moved to settings
	private static final String TESTING_EMAIL = "igor.pernek@gmail.com";
	private static final String TESTING_PASSWORD = "307 Lakih_Pet";
	private static final String UPLOAD_URL = "https://dev.trainerjim.com/mapi/training/upload";
	// private static final String TRAINING_LIST_URL = "/mapi/training/list";
	// private static final String TRAINING_GET_URL = "/mapi/training/get";

	private static final String HTTP_PARAM_EMAIL_NAME = "email";
	private static final String HTTP_PARAM_PASSWORD_NAME = "password";
	private static final String HTTP_PARAM_FILE_NAME = "trainingData";

	private Gson jsonParser = new Gson();

	public DataUploaderService() {
		super("DataUploaderService");
	}

	// This SSLSocketFactory does not check if a certificate issued is secured
	// and
	// just simply accepts it.
	// TODO: Remove it when or certificate will be issued by a trusted authority
	class DangeroursSSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public DangeroursSSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

	// this client simply accepts all certificats
	// TODO: change it before serious production
	public HttpClient getDangerousHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new DangeroursSSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
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

			while (trainings.moveToNext()) {
				int trainingId = trainings.getInt(trainings
						.getColumnIndex(TrainingPlan._ID));
				String jsonEncodedTraining = trainings.getString(trainings
						.getColumnIndex(TrainingPlan.DATA));

				uploadTraining(trainingId, jsonEncodedTraining, username,
						password);
			}

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

	private void uploadTraining(int trainingId, String jsonEncodedTraining,
			String username, String password) {
		Training training = jsonParser.fromJson(jsonEncodedTraining,
				Training.class);

		File trainingZip = new File(Utils.getUploadDataFolderFile(),
				Utils.generateFileName(training.getStartDate()) + ".zip");

		// first write training to disc
		Measurement measurement = training.extractMeasurement();
		measurement.zipToFile(trainingZip);

		HttpClient httpClient = getDangerousHttpClient();

		try {
			HttpPost httppost = new HttpPost(UPLOAD_URL);

			MultipartEntity multipartEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			multipartEntity.addPart(HTTP_PARAM_EMAIL_NAME, new StringBody(
					username));
			multipartEntity.addPart(HTTP_PARAM_PASSWORD_NAME, new StringBody(
					password));
			// multipartEntity.addPart("utf8", new StringBody("&#x2713;"));
			// multipartEntity.addPart("authenticity_token", new
			// StringBody("QF2/iLHYEUm+kPU5ktsaw3wJJzqTG559TpLFLh9VpUw="));
			multipartEntity.addPart(HTTP_PARAM_FILE_NAME, new FileBody(
					trainingZip));
			httppost.setEntity(multipartEntity);

			HttpResponse response = httpClient.execute(httppost);
			int status = response.getStatusLine().getStatusCode();
			
			if(status == HttpStatus.SC_OK){
				// delete from the local database
				getContentResolver().delete(ContentUris.withAppendedId(CompletedTraining.CONTENT_URI, trainingId) , null, null);
			}

			// signalize the status (and something else if needed) to
			// everyone
			// interested
			// (most probably only to the main activity to update the
			// screen)
//			Intent broadcastIntent = new Intent();
//			broadcastIntent
//					.setAction(UploadSessionActivity.ResponseReceiver.ACTION_UPLOAD_DONE);
//			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
//			broadcastIntent
//					.putExtra(
//							UploadSessionActivity.ResponseReceiver.PARAM_STATUS,
//							status);
//			sendBroadcast(broadcastIntent);

			// the file could be also deleted here
		} catch (Exception e) {

			Log.e(TAG, e.getLocalizedMessage());
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
		HttpClient httpClient = getDangerousHttpClient();

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
		HttpClient httpClient = getDangerousHttpClient();
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
		HttpClient httpClient = getDangerousHttpClient();

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

	private void uploadFile(String path) {

		HttpClient httpClient = getDangerousHttpClient();

		try {
			File data = new File(path);
			HttpPost httppost = new HttpPost(UPLOAD_URL);

			MultipartEntity multipartEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			multipartEntity.addPart(HTTP_PARAM_EMAIL_NAME, new StringBody(
					TESTING_EMAIL));
			multipartEntity.addPart(HTTP_PARAM_PASSWORD_NAME, new StringBody(
					TESTING_PASSWORD));
			// multipartEntity.addPart("utf8", new StringBody("&#x2713;"));
			// multipartEntity.addPart("authenticity_token", new
			// StringBody("QF2/iLHYEUm+kPU5ktsaw3wJJzqTG559TpLFLh9VpUw="));
			multipartEntity.addPart(HTTP_PARAM_FILE_NAME, new FileBody(data));
			httppost.setEntity(multipartEntity);

			HttpResponse response = httpClient.execute(httppost);
			int status = response.getStatusLine().getStatusCode();

			// signalize the status (and something else if needed) to
			// everyone
			// interested
			// (most probably only to the main activity to update the
			// screen)
			Intent broadcastIntent = new Intent();
			broadcastIntent
					.setAction(UploadSessionActivity.ResponseReceiver.ACTION_UPLOAD_DONE);
			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			broadcastIntent
					.putExtra(
							UploadSessionActivity.ResponseReceiver.PARAM_STATUS,
							status);
			sendBroadcast(broadcastIntent);

			// the file could be also deleted here
		} catch (Exception e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}
}
