package net.pernek.jim.exercisedetector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class DetectorService extends Service {
	private static final String TAG = "MainActivity";
	private int NOTIFICATION_ID = 1;
	
	private NotificationManager mNotificationManager;
	private final IBinder mDetectorBinder = new DetectorServiceBinder();
	
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

		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	
		showNotification();
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
		super.onDestroy();
		
		Log.d(TAG, "DetectorService onDestroy");
		
        mNotificationManager.cancel(NOTIFICATION_ID);

	}

}
