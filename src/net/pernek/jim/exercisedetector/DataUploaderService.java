package net.pernek.jim.exercisedetector;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import net.pernek.jim.exercisedetector.UploadSessionActivity.ResponseReceiver;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class DataUploaderService extends IntentService {

	private static final String TAG = Utils.getApplicationTag();

	public static final String INTENT_KEY_FILE = "file";
	public static final String PARAM_STATUS = "status";

	// those should be moved to settings
	private static final String TESTING_EMAIL = "igor.pernek@gmail.com";
	private static final String TESTING_PASSWORD = "307 Lakih_Pet";
	private static final String UPLOAD_URL = "https://trainerjim.banda.si/measurements/upload";

	private static final String INPUT_EMAIL_NAME = "measurement_submission[email]";
	private static final String INPUT_PASSWORD_NAME = "measurement_submission[password]";
	private static final String INPUT_FILE_NAME = "measurement_submission[file_upload_data]";

	public DataUploaderService() {
		super("DataUploaderService");
	}
	
	// This SSLSocketFactory does not check if a certificate issued is secured and
	// just simply accepts it. 
	// TODO: Remove it when or certificate will be issued by a trusted authority
	class DangeroursSSLSocketFactory extends SSLSocketFactory {
	    SSLContext sslContext = SSLContext.getInstance("TLS");

	    public DangeroursSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
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
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
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
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new DangeroursSSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		String path = intent.getExtras().getString(INTENT_KEY_FILE);
		
		HttpClient httpClient = getDangerousHttpClient();

		try {
			/*
			 * HTTP GET TESTING
			String untrustedURL = "https://trainerjim.banda.si/measurements/list";
			String trustedURL = "https://www.google.com";
			HttpGet httpGet = new HttpGet(untrustedURL);
			HttpResponse responseG = httpClient.execute(httpGet);
			int statusG = responseG.getStatusLine().getStatusCode();
			*/
			
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

			// signalize the status (and something else if needed) to everyone
			// interested
			// (most probably only to the main activity to update the screen)
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ResponseReceiver.ACTION_UPLOAD_DONE);
			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			broadcastIntent.putExtra(PARAM_STATUS, status);
			sendBroadcast(broadcastIntent);

			// the file could be also deleted here
		} catch (Exception e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}
}
