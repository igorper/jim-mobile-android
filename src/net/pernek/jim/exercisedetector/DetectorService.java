package net.pernek.jim.exercisedetector;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;

import net.pernek.jim.exercisedetector.alg.Compress;
import net.pernek.jim.exercisedetector.alg.TrainingPlan;
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

	private String mAccelerationFile;
	private String mDetectedTimestampsFile;
	private String mTrainingMainfestFile;

	private TrainingPlan mCurrentTrainingPlan;

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

		mSettings.saveServiceRunning(true);
	}

	public boolean isCollectingData() {
		return mIsCollectingData;
	}

	public boolean startDataCollection() throws JSONException {
		// if there is no output file we should create one
		// (this means we are starting a new sampling session)
		if (mSettings.getOutputFile().equals("")) {
			mSettings.saveOutputFile(Utils.generateFileName());
		}

		// check if the folder exists and create it if it doesn't
		File folder = Utils.getFullDataFolder();
		folder.mkdir();

		// set training plan JSON file
		mTrainingMainfestFile = new File(folder, Utils.getTrainingManifestFileName(mSettings.getOutputFile())).getPath();
		
		if (new File(mTrainingMainfestFile).exists()) {
			// if the training file is already stored on the reload it
			// (this happened when the service was killed and recovered)
			mCurrentTrainingPlan = TrainingPlan.readFromFile(mTrainingMainfestFile);
		} else {
			// create the current training from the shared preferences
			mCurrentTrainingPlan = TrainingPlan.parseFromJson(mSettings
					.getCurrentTrainingPlan());
			
			// save the file to disk as well (for the case the service crashes)
			mCurrentTrainingPlan.saveToTempFile(mTrainingMainfestFile);
		}

		mAccelerationFile = new File(folder, mSettings.getOutputFile())
				.getPath();
		mDetectedTimestampsFile = new File(folder, mSettings.getOutputFile()
				+ "_tstmps").getPath();

		// hand testing values
		mSensorListener = SensorListener.create(mAccelerationFile,
				mDetectedTimestampsFile, getApplicationContext(), 50000,
				200000, 1000000, 180, 100, 200000, 4);

		// gym detection values
		/*
		 * mSensorListener = SensorListener.create( new File(folder,
		 * mSettings.getOutputFile()), getApplicationContext(), 8000, 30000,
		 * 1000000, 600, 200, 200000, 14);
		 */
		try {
			boolean status = mSensorListener.start();

			if (!status) {
				// do this more gracefully -> the app simply does not need to
				// offer automated functionalities if the sensors are not
				// present
				// (user can still swipe through the user interface)
				Toast.makeText(
						this,
						"Unable to start accelerometer service. Is the sensor really present?",
						Toast.LENGTH_SHORT).show();
				return false;
			}
		} catch (IOException e) {
			// this happens when creating a file fails
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}

		mIsCollectingData = true;
		return true;
	}
	
	public void updateTrainingPlan(){
		mCurrentTrainingPlan = TrainingPlan.readFromFile(mTrainingMainfestFile);
		Log.d(TAG, Integer.toString(mCurrentTrainingPlan.getExercises().get(0).getSeries().get(0).getWeight()));
	}

	// this method should only be called when we want to legally stop sampling
	public boolean stopDataCollection() {
		mIsCollectingData = false;

		// save the id we used for saving our data
		String outputId = mSettings.getOutputFile();

		// only here we can remove the output file (when the users manually
		// selects that he wants to stop)
		mSettings.saveOutputFile("");

		mSensorListener.stop();

		// TODO: compile all collected data to one JSON file
		// this file will be visible in upload activity
		File uploadFolder = new File(Environment.getExternalStorageDirectory(),
				Utils.getUploadDataFolder());
		uploadFolder.mkdir();

		// we are sure exercise files were created and are closed here
		Compress comp = new Compress(new String[] { mAccelerationFile,
				mDetectedTimestampsFile },
				new File(uploadFolder, outputId).getPath());

		return comp.zip();
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
	public int[] moveToNextActivity() {
		int curExercise = mSettings.getCurrentExerciseIndex();
		int curSeries = mSettings.getCurrentSeriesIndex();

		if (curSeries + 1 < mCurrentTrainingPlan.getExercises()
				.get(curExercise).getSeries().size()) {
			mSettings.saveCurrentSeriesIndex(curSeries + 1);
		} else {
			mSettings.saveCurrentSeriesIndex(0);

			// we have move to the next exercise
			if (curExercise + 1 < mCurrentTrainingPlan.getExercises().size()) {
				mSettings.saveCurrentExerciseIndex(curExercise + 1);
			} else {
				// last exercise done
				return null;
			}
		}

		// update sensor listener - used to write to file
		mSensorListener.updateCurrentExerciseInfo(
				mSettings.getCurrentExerciseIndex(),
				mSettings.getCurrentSeriesIndex());

		return new int[] { mSettings.getCurrentExerciseIndex(),
				mSettings.getCurrentSeriesIndex() };
	}

	public TrainingPlan getCurrentTrainingPlan() {
		return mCurrentTrainingPlan;
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

	@Override
	public void onDestroy() {
		Log.d(TAG, "DetectorService onDestroy");

		mSettings.saveServiceRunning(false);

		mSensorListener.stop();

		super.onDestroy();
	}

}
