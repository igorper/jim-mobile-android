package net.pernek.jim.exercisedetector;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;

import net.pernek.jim.exercisedetector.entities.TrainingPlan;
import net.pernek.jim.exercisedetector.util.Compress;
import net.pernek.jim.exercisedetector.util.Utils;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
	private final IBinder mDetectorBinder = new DetectorServiceBinder();

	private SensorListener mSensorListener;
	private DetectorSettings mSettings;

	public class DetectorServiceBinder extends Binder {
		DetectorService getService() {
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

		mSettings = DetectorSettings.create(PreferenceManager
				.getDefaultSharedPreferences(this));
	}

	public boolean isCollectingData() {
		return mIsCollectingData;
	}

	public boolean startDataCollection() throws JSONException {
		// if there is no output file we should create one
		// (this means we are starting a new sampling session)
		if (mSettings.getOutputFile().equals("")) {
			mSettings.saveOutputFile(Utils.generateFileName());

			// also reset current exercise and series index
			mSettings.saveCurrentExerciseIndex(0);
			mSettings.saveCurrentSeriesIndex(0);
		}

		// check if the folder exists and create it if it doesn't
		File folder = Utils.getDataFolderFile();
		folder.mkdir();

		// hand testing values
		/*
		 * mSensorListener = SensorListener.create(mSettings.getOutputFile(),
		 * getApplicationContext(), 50000, 200000, 1000000, 180, 100, 200000, 4,
		 * mSettings.getCurrentTrainingPlan(),
		 * mSettings.getCurrentExerciseIndex(),
		 * mSettings.getCurrentSeriesIndex());
		 */
		// gym detection values

		mSensorListener = SensorListener.create(mSettings.getOutputFile(),
				getApplicationContext(), 8000, 30000, 1000000, 600, 200,
				200000, 14, mSettings.getCurrentTrainingPlan(),
				mSettings.getCurrentExerciseIndex(),
				mSettings.getCurrentSeriesIndex());

		try {
			mSensorListener.openOutputFiles();
		} catch (IOException e) {
			// this happens when creating a file fails
			Toast.makeText(this, "Unable to open output files.",
					Toast.LENGTH_SHORT).show();
			return false;
		}

		boolean status = mSensorListener.startAccelerationSampling();
		if (!status) {
			// can also do this more gracefully -> the app simply does not need
			// to
			// offer automated functionalities if the sensors are not
			// present
			// (user can still swipe through the user interface)
			Toast.makeText(
					this,
					"Unable to start accelerometer service. Is the sensor really present?",
					Toast.LENGTH_SHORT).show();
			return false;
		}

		mIsCollectingData = true;
		return true;
	}

	public void updateTrainingPlan() {
		mSensorListener.updateTrainingPlan();
	}

	// this method should only be called when we want to legally stop sampling
	public boolean stopDataCollection() {
		mIsCollectingData = false;

		// save the id we used for saving our data
		String outputId = mSettings.getOutputFile();

		// only here we can remove the output file (when the users manually
		// selects that he wants to stop)
		mSettings.saveOutputFile("");

		mSensorListener.closeOutputFiles();

		mSensorListener.stopAccelerationSampling();

		// compile all collected data to one JSON file
		// this file will be visible in upload activity
		File uploadFolder = Utils.getUploadDataFolderFile();
		uploadFolder.mkdir();

		return mSensorListener.compileForUpload(outputId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "DetectorService onStartCommand");

		pushServiceToForeground();

		return START_STICKY;
	}

	// TODO: data can be returned in a custom object later
	// retVal[0] - new exercise id
	// retVal[1] - new series id
	// returns null if this was the last exercise/series to perform
	public boolean moveToNextActivity() {
		boolean status = mSensorListener.moveToNextActivity();

		// update shared preferecenes (just for the case the application breaks
		// down in the middle)
		if (status) {
			mSettings.saveCurrentExerciseIndex(mSensorListener
					.getCurrentExerciseIdx());
			mSettings.saveCurrentSeriesIndex(mSensorListener
					.getCurrentSeriesIdx());
		}

		return status;
	}

	public TrainingPlan getCurrentTrainingPlan() {
		return mSensorListener.getCurrentTrainingPlan();
	}

	private void pushServiceToForeground() {
		String message = "Detector service on.";

		Notification notification = new Notification(R.drawable.ic_launcher,
				message, System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, ExerciseDetectorActivity.class), 0);

		notification.setLatestEventInfo(this, "status", message, contentIntent);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(NOTIFICATION_ID, notification);
	}

	public int getCurrentExerciseIdx() {
		return mSensorListener.getCurrentExerciseIdx();
	}

	public int getCurrentSeriesIdx() {
		return mSensorListener.getCurrentSeriesIdx();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "DetectorService onDestroy");

		mSensorListener.stopAccelerationSampling();

		super.onDestroy();
	}

}
