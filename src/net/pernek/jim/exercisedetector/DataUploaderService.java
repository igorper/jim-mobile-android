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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
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
import android.content.Intent;
import android.util.Log;

// TODO: rename file to e.g. server communicator (this service will do
// all the server calls)
public class DataUploaderService extends IntentService {

	private static final String TAG = Utils.getApplicationTag();

	public static final String ACTION_UPLOAD = "action-upload";
	public static final String ACTION_GET_TRAINING_LIST = "action-get-training-list";
	public static final String ACTION_GET_TRAINING = "action-get-training";

	public static final String INTENT_KEY_ACTION = "action";
	public static final String INTENT_KEY_FILE = "file";
	public static final String INTENT_KEY_TRAINING_ID = "training-id";

	// those should be moved to settings
	private static final String TESTING_EMAIL = "igor.pernek@gmail.com";
	private static final String TESTING_PASSWORD = "307 Lakih_Pet";
	private static final String UPLOAD_URL = "https://trainerjim.banda.si/measurements/upload";
	private static final String TRAINING_LIST_URL = "https://dev.trainerjim.com/mapi/training/list";
	private static final String TRAINING_GET_URL = "https://dev.trainerjim.com/mapi/training/get";

	private static final String INPUT_EMAIL_NAME = "measurement_submission[email]";
	private static final String INPUT_PASSWORD_NAME = "measurement_submission[password]";
	private static final String INPUT_FILE_NAME = "measurement_submission[file_upload_data]";

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

		if (action.equals(ACTION_UPLOAD)) {
			String path = intent.getExtras().getString(INTENT_KEY_FILE);

			uploadFile(path);
		} else if (action.equals(ACTION_GET_TRAINING_LIST)) {
			getTrainingList();
			
		} else if(action.equalsIgnoreCase(ACTION_GET_TRAINING)){
			int trainingId = intent.getExtras().getInt(INTENT_KEY_TRAINING_ID);
			
			HttpClient httpClient = getDangerousHttpClient();

			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("email", TESTING_EMAIL));
				params.add(new BasicNameValuePair("password", TESTING_PASSWORD));
				params.add(new BasicNameValuePair("id", Integer.toString(trainingId)));

				HttpGet httpget = new HttpGet(String.format("%s?%s",
						TRAINING_GET_URL,
						URLEncodedUtils.format(params, "utf-8")));

				HttpResponse response = httpClient.execute(httpget);
				int status = response.getStatusLine().getStatusCode();
				
				Intent broadcastIntent = new Intent();
				broadcastIntent
						.setAction(ExerciseDetectorActivity.ResponseReceiver.ACTION_TRAINING_PLAN_DOWNLOADED);
				broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
				broadcastIntent.putExtra(UploadSessionActivity.ResponseReceiver.PARAM_STATUS, status);

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
					String jsonString = builder.toString();
					
					broadcastIntent.putExtra(ExerciseDetectorActivity.ResponseReceiver.PARAM_TRAINING_PLAN, jsonString);
				}

				sendBroadcast(broadcastIntent);
			} catch (Exception e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
		}
	}
	
	private void getTrainingList(){
		HttpClient httpClient = getDangerousHttpClient();

		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", TESTING_EMAIL));
			params.add(new BasicNameValuePair("password", TESTING_PASSWORD));

			HttpGet httpget = new HttpGet(String.format("%s?%s",
					TRAINING_LIST_URL,
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
				String jsonString = builder.toString();
			}
		} catch (Exception e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}

	private void uploadFile(String path) {

		HttpClient httpClient = getDangerousHttpClient();

		try {
			File data = new File(path);
			HttpPost httppost = new HttpPost(UPLOAD_URL);

			MultipartEntity multipartEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			multipartEntity.addPart(INPUT_EMAIL_NAME, new StringBody(
					TESTING_EMAIL));
			multipartEntity.addPart(INPUT_PASSWORD_NAME, new StringBody(
					TESTING_PASSWORD));
			// multipartEntity.addPart("utf8", new StringBody("&#x2713;"));
			// multipartEntity.addPart("authenticity_token", new
			// StringBody("QF2/iLHYEUm+kPU5ktsaw3wJJzqTG559TpLFLh9VpUw="));
			multipartEntity.addPart(INPUT_FILE_NAME, new FileBody(data));
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
