package net.pernek.jim.exercisedetector;

import java.io.File;

import net.pernek.jim.exercisedetector.UploadSessionActivity.ResponseReceiver;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

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
	private static final String UPLOAD_URL = "http://trainerjim.banda.si/measurements/upload";
	
	private static final String INPUT_EMAIL_NAME = "measurement_submission[email]";
	private static final String INPUT_PASSWORD_NAME = "measurement_submission[password]";
	private static final String INPUT_FILE_NAME = "measurement_submission[file_upload_data]";
	
	public DataUploaderService() {
		super("DataUploaderService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String path = intent.getExtras().getString(INTENT_KEY_FILE);
		
		HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		HttpClient httpClient = new DefaultHttpClient(params);
		
		 try {
			 	File data = new File(path);
	            HttpPost httppost = new HttpPost(UPLOAD_URL);

	            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
	            multipartEntity.addPart(INPUT_EMAIL_NAME, new StringBody(TESTING_EMAIL));
	            multipartEntity.addPart(INPUT_PASSWORD_NAME, new StringBody(TESTING_PASSWORD));
	            //multipartEntity.addPart("utf8", new StringBody("&#x2713;"));
	            //multipartEntity.addPart("authenticity_token", new StringBody("QF2/iLHYEUm+kPU5ktsaw3wJJzqTG559TpLFLh9VpUw="));
	            multipartEntity.addPart(INPUT_FILE_NAME, new FileBody(data));
	            httppost.setEntity(multipartEntity);

	            HttpResponse response = httpClient.execute(httppost);
	            int status = response.getStatusLine().getStatusCode();
	            
	            // signalize the status (and something else if needed) to everyone interested
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
