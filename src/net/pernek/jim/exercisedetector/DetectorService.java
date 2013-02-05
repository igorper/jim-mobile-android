package net.pernek.jim.exercisedetector;

import java.io.File;
import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

// this service should take care for staying awake and always sampling
public class DetectorService extends Service {
	private static final String TAG = Utils.getApplicationTag();
	private int NOTIFICATION_ID = 1;
	
	private boolean mIsCollectingData = false;
	private NotificationManager mNotificationManager;
	private final IBinder mDetectorBinder = new DetectorServiceBinder();
	
	private SensorListener mSensorListener;
	private DetectorSettings mSettings;
	
	public class DetectorServiceBinder extends Binder {
		DetectorService getService(){
			return DetectorService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mDetectorBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG, "DetectorService onCreate");
		
		// here we should read the output file for current acceleration data and create the acceleration listener 

		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	
		showNotification();
		
		mSettings = DetectorSettings.create(PreferenceManager.getDefaultSharedPreferences(this));
		
		mSettings.saveServiceRunning(true);
	}
	
	public boolean isCollectingData(){
		return mIsCollectingData;
	}
	
	public boolean startDataCollection(){
		String testOutput = mSettings.getOutputFile();
		// output file should always be set here
		assert !mSettings.getOutputFile().equals("");
		
		// check if the folder exists and create it
		File folder = new File(Environment.getExternalStorageDirectory(), Utils.getDataFolder());
		folder.mkdir();
		
		mSensorListener = SensorListener.create(new File(folder, mSettings.getOutputFile()), (SensorManager)getSystemService(SENSOR_SERVICE));
		assert mSensorListener != null;
		try {
			if(!mSensorListener.start()){
				Toast.makeText(this, "Unable to start accelerometer service. Is the sensor really present?", Toast.LENGTH_SHORT).show();
				return false;
			}
		} catch (IOException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		mIsCollectingData = true;
		return true;
	}
	
	public boolean stopDataCollection(){
		mIsCollectingData = false;
		return mSensorListener.stop();
	}
	
	private void showNotification() {
		String message = "Detector service on.";
		
        Notification notification = new Notification(R.drawable.ic_launcher, message,
                System.currentTimeMillis());

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ExerciseDetectorActivity.class), 0);

        notification.setLatestEventInfo(this, "status", message, contentIntent);

        mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.d(TAG, "DetectorService onStartCommand");
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "DetectorService onDestroy");
		
		mSettings.saveServiceRunning(false);
		
        mNotificationManager.cancel(NOTIFICATION_ID);
        
		assert mSensorListener.stop();
		
		super.onDestroy();
	}

}
