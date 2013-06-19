package net.pernek.jim.exercisedetector;

import net.pernek.jim.exercisedetector.entities.TrainingPlanOld;
import net.pernek.jim.exercisedetector.util.Utils;

import org.json.JSONException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ExerciseDetectorActivity extends Activity {

	private static final String TAG = Utils.getApplicationTag();

	private final static int MENU_UPLOAD = 1;
	private final static int MENU_GET_TRAINING_LIST = 2;
	private final static int MENU_GET_TRAINING = 3;
	private final static int MENU_EXERCISE_MANIFEST = 4;

	private static final int TIMER_UPDATE_MS = 500;

	private LinearLayout mLlTrainingPlan;
	private CheckBox mChbToggleService;
	private TextView mTvExerciseState;
	private TextView mTvTimer;
	private TextView mTvCurrentTraining;
	private TextView mTvCurrentExercise;
	private TextView mTvCurrentSeries;
	private TextView mTvExpectedRepetitions;
	private TextView mTvExpectedWeight;
	private Button mBtnNext;

	private DetectorSettings mSettings;
	private DetectorService mDetectorService;

	private Handler mUiHandler = new Handler();
	private Runnable mRunTimerUpdate = new Runnable() {

		@Override
		public void run() {
			long millis = System.currentTimeMillis()
					- mSettings.getStartTimestamp();
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;

			mTvTimer.setText(String.format("%d:%02d", minutes, seconds));

			mUiHandler.postDelayed(this, TIMER_UPDATE_MS);
		}
	};

	private ResponseReceiver receiver;

	private boolean isDetectorServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (DetectorService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private ServiceConnection mDetectorConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mDetectorService = null;

			Log.w(TAG, "MainActivity onServiceDisconnected");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDetectorService = ((DetectorService.DetectorServiceBinder) service)
					.getService();
			if (!mDetectorService.isCollectingData()) {
				try {
					if (!mDetectorService.startDataCollection()) {
						mChbToggleService.setChecked(false);
						Toast.makeText(
								getApplicationContext(),
								"Unable to start data collection - sd card problem?",
								Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "JSON exception",
							Toast.LENGTH_SHORT).show();
				}
			}

			updateTrainingInfoUI();

			Log.w(TAG, "MainActivity onServiceConnected");
		}
	};

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		menu.add(1, MENU_UPLOAD, 1, "Upload");
		menu.add(1, MENU_GET_TRAINING_LIST, 1, "Get training list");
		menu.add(1, MENU_GET_TRAINING, 1, "Get training");
		menu.add(1, MENU_EXERCISE_MANIFEST, 1, "Show exercises");
		return true;
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_UPLOAD: {
			Log.d(TAG, "on menu upload click");

			Intent intent = new Intent(this, UploadSessionActivity.class);
			startActivity(intent);
			break;
		}
		case MENU_GET_TRAINING_LIST: {
			Log.d(TAG, "get training list");

			Intent intent = new Intent(this, DataUploaderService.class);
			intent.putExtra(DataUploaderService.INTENT_KEY_ACTION,
					DataUploaderService.ACTION_FETCH_TRAININGS);
			startService(intent);

			break;
		}
		case MENU_GET_TRAINING: {
			Log.d(TAG, "get training");

			// TODO: Hardcoded, should be picked by the person exercising
			int trainingId = 1;

			Intent intent = new Intent(this, DataUploaderService.class);
			intent.putExtra(DataUploaderService.INTENT_KEY_ACTION,
					DataUploaderService.ACTION_GET_TRAINING);
			intent.putExtra(DataUploaderService.INTENT_KEY_TRAINING_ID,
					trainingId);
			startService(intent);

			break;
		}
		case MENU_EXERCISE_MANIFEST: {
			Log.d(TAG, "exercise manifest selected");

			startActivityForResult(new Intent(this,
					TrainingManifestActivity.class), MENU_EXERCISE_MANIFEST);

			break;
		}
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case MENU_EXERCISE_MANIFEST: {
			// update service training plan
			if (mDetectorService != null) {
				Log.d(TAG, "call update training plan");
				mDetectorService.updateTrainingPlan();

				// current exercise and series index should always be obtained
				// from the service
				updateTrainingInfoUI();
			}

			break;
		}
		default:
			break;
		}
	}

	public void onNextClick(View v) {
		if (mDetectorService != null) {
			boolean status = mDetectorService.moveToNextActivity();
			if (status) {
				updateTrainingInfoUI();
			} else {
				// disable the button as there are no more exercises to perform
				mBtnNext.setEnabled(false);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSettings = DetectorSettings.create(PreferenceManager
				.getDefaultSharedPreferences(this));

		mBtnNext = (Button) findViewById(R.id.btnNext);
		mTvCurrentTraining = (TextView) findViewById(R.id.tvCurrentTraining);
		mTvCurrentExercise = (TextView) findViewById(R.id.tvCurrentExercise);
		mTvCurrentSeries = (TextView) findViewById(R.id.tvCurrentSeries);
		mTvExpectedRepetitions = (TextView) findViewById(R.id.tvExpectedRepetitions);
		mTvExpectedWeight = (TextView) findViewById(R.id.tvExpectedWeight);
		mTvTimer = (TextView) findViewById(R.id.tvTimer);
		mTvExerciseState = (TextView) findViewById(R.id.tvExerciseState);
		mChbToggleService = (CheckBox) findViewById(R.id.chbToggleService);
		mChbToggleService.setChecked(isDetectorServiceRunning());
		mLlTrainingPlan = (LinearLayout) findViewById(R.id.llTrainingPlan);

		mChbToggleService
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// this is simulates the start training action
						if (isChecked) {
							// run GC before starting the service
							// as it will be memory intensive
							// might be this can be improved
							System.gc();

							if (mSettings.getCurrentTrainingPlan().equals("")) {
								// open popup to download a training plan
							}

							// on start reset exercise state to REST
							mSettings.saveIsExerciseState(false);
							mTvExerciseState
									.setText(getExerciseString(mSettings
											.isExerciseState()));

							startService(new Intent(
									ExerciseDetectorActivity.this,
									DetectorService.class));

							mSettings.saveStartTimestamp(System
									.currentTimeMillis());

							mUiHandler.postDelayed(mRunTimerUpdate,
									TIMER_UPDATE_MS);

							// NOTE: we will never unbind the service as binding
							// was performed with 0 flag meaning the service had
							// to be started before with start service call
							getApplicationContext().bindService(
									new Intent(ExerciseDetectorActivity.this,
											DetectorService.class),
									mDetectorConnection, 0);
						} else {
							if (mDetectorService != null) {
								mDetectorService.stopDataCollection();
								mDetectorService = null;
							}

							mUiHandler.removeCallbacks(mRunTimerUpdate);

							stopService(new Intent(
									ExerciseDetectorActivity.this,
									DetectorService.class));

							// on end reset exercise state to REST
							mSettings.saveIsExerciseState(false);
							mTvExerciseState
									.setText(getExerciseString(mSettings
											.isExerciseState()));

							updateTrainingInfoUI();
						}
					}
				});

		resurrectDestroyed();

		receiver = new ResponseReceiver();

		IntentFilter stateChangedfilter = new IntentFilter(
				ResponseReceiver.ACTION_EXERCISE_STATE_CHANGED);
		stateChangedfilter.addCategory(Intent.CATEGORY_DEFAULT);

		IntentFilter planDownloadedfilter = new IntentFilter(
				ResponseReceiver.ACTION_TRAINING_PLAN_DOWNLOADED);
		planDownloadedfilter.addCategory(Intent.CATEGORY_DEFAULT);

		registerReceiver(receiver, stateChangedfilter);
		registerReceiver(receiver, planDownloadedfilter);

		Log.w(TAG, "OnCreate");
	}

	// we could have a method start service, which could call startService again
	// if mSettings.isServiceRunning == true && mDetectorService == null
	// this should be done when we try to to da call to the service but the
	// service is still null

	// this method resurrects everything, that should be running or visible but
	// was killed by onDestroy
	private void resurrectDestroyed() {
		// ressurect current exercise state
		mTvExerciseState
				.setText(getExerciseString(mSettings.isExerciseState()));

		boolean isServiceRunning = isDetectorServiceRunning();

		// resurrect the timer
		if (isServiceRunning) {
			mUiHandler.postDelayed(mRunTimerUpdate, TIMER_UPDATE_MS);
		}

		// rebind the service
		if (isServiceRunning && mDetectorService == null) {
			getApplication().bindService(
					new Intent(ExerciseDetectorActivity.this,
							DetectorService.class), mDetectorConnection, 0);
		}
	}

	private String getExerciseString(boolean isExercise) {
		return isExercise ? "EXERCISE" : "REST";
	}

	class ResponseReceiver extends BroadcastReceiver {
		public static final String ACTION_EXERCISE_STATE_CHANGED = "exercise.state.changed";
		public static final String ACTION_TRAINING_PLAN_DOWNLOADED = "training.plan.downloaded";

		public static final String PARAM_STATE = "state";
		public static final String PARAM_TIMESTAMP = "timestamp";
		public static final String PARAM_TRAINING_PLAN = "training.plan";

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (action.equals(ACTION_EXERCISE_STATE_CHANGED)) {

				boolean isExercise = intent.getExtras().getBoolean(PARAM_STATE);
				int timestamp = intent.getExtras().getInt(PARAM_TIMESTAMP);

				mSettings.saveIsExerciseState(isExercise);

				mUiHandler.post(new Runnable() {

					@Override
					public void run() {
						mTvExerciseState.setText(getExerciseString(mSettings
								.isExerciseState()));
					}
				});

				// reset timer to 0 to start counting time again
				mSettings.saveStartTimestamp(System.currentTimeMillis());
			} else if (action.equals(ACTION_TRAINING_PLAN_DOWNLOADED)) {
				int status = intent.getExtras().getInt(
						UploadSessionActivity.ResponseReceiver.PARAM_STATUS);

				if (status == 200) {
					String trainingPlan = intent.getExtras().getString(
							PARAM_TRAINING_PLAN);
					mSettings.saveCurrentTrainingPlan(trainingPlan);

					updateTrainingInfoUI();
				} else {
					Toast.makeText(
							getApplicationContext(),
							"Download training status "
									+ Integer.toString(status),
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	private void updateTrainingInfoUI() {
//		if (mDetectorService != null) {
//			int exerciseIndex = mDetectorService.getCurrentExerciseIdx();
//			int seriesIndex = mDetectorService.getCurrentSeriesIdx();
//			TrainingPlanOld curTraining = mDetectorService
//					.getCurrentTrainingPlan();
//			Exercise curExercise = curTraining.getExercises()
//					.get(exerciseIndex);
//			Series curSeries = curExercise.getSeries().get(seriesIndex);
//
//			mTvCurrentTraining.setText(curTraining.getName());
//			mTvCurrentExercise.setText(curExercise.getName());
//			mTvCurrentSeries.setText(Integer.toString(seriesIndex));
//			mTvExpectedRepetitions.setText(Integer.toString(curSeries
//					.getNumRepetitions()));
//			mTvExpectedWeight.setText(Integer.toString(curSeries.getWeight()));
//
//			mLlTrainingPlan.setVisibility(View.VISIBLE);
//		} else {
//			mLlTrainingPlan.setVisibility(View.INVISIBLE);
//		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		mUiHandler.removeCallbacks(mRunTimerUpdate);
		super.onDestroy();
	}
}
