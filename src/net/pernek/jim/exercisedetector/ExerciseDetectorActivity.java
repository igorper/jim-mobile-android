package net.pernek.jim.exercisedetector;

import net.pernek.jim.exercisedetector.alg.ExerciseState;
import android.app.Activity;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class ExerciseDetectorActivity extends Activity {

	private static final String TAG = Utils.getApplicationTag();

	private final static int MENU_UPLOAD = 1;
	
	private static final int TIMER_UPDATE_MS = 500;
	
	private CheckBox mChbToggleService;
	private TextView mTvExerciseState;
	private TextView mTvTimer;
	private DetectorSettings mSettings;
	private DetectorService mDetectorService;

	private Handler mTimerHandler = new Handler();
	private Runnable mRunTimerUpdate = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			long millis = System.currentTimeMillis() - mSettings.getStartTimestamp();
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;

			mTvTimer.setText(String.format("%d:%02d", minutes, seconds));

			mTimerHandler.postDelayed(this, TIMER_UPDATE_MS);
		}
	};

	private ResponseReceiver receiver;

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
				if (!mDetectorService.startDataCollection()) {
					mChbToggleService.setChecked(false);
				}
			}

			Log.w(TAG, "MainActivity onServiceConnected");
		}
	};

	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		menu.add(1, MENU_UPLOAD, 1, "Upload");
		return true;
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_UPLOAD:
			Log.d(TAG, "on menu upload click");

			Intent intent = new Intent(this, UploadSessionActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSettings = DetectorSettings.create(PreferenceManager
				.getDefaultSharedPreferences(this));

		mTvTimer = (TextView) findViewById(R.id.tvTimer);
		mTvExerciseState = (TextView) findViewById(R.id.tvExerciseState);
		mChbToggleService = (CheckBox) findViewById(R.id.chbToggleService);
		mChbToggleService.setChecked(mSettings.isServiceRunning());

		mChbToggleService
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							
							// run GC before starting the service
							// this will be memory intensive
							System.gc();
							
							
							startService(new Intent(
									ExerciseDetectorActivity.this,
									DetectorService.class));
							
							mSettings.saveStartTimestamp(System.currentTimeMillis());
							//mTimerHandler.postDelayed(mRunTimerUpdate, TIMER_UPDATE_MS);

							// NOTE: we will never unbind the service as binding was
							// performed with 0 flag meaning the service had to
							// be started before with start service
							getApplicationContext().bindService(
									new Intent(ExerciseDetectorActivity.this,
											DetectorService.class),
									mDetectorConnection, 0);
						} else {
							if (mDetectorService != null) {
								mDetectorService.stopDataCollection();
							}
							
							mTimerHandler.removeCallbacks(mRunTimerUpdate);
							
							stopService(new Intent(
									ExerciseDetectorActivity.this,
									DetectorService.class));
						}
					}
				});

		resurrectDestroyed();

		IntentFilter filter = new IntentFilter(
				ResponseReceiver.ACTION_EXERCISE_STATE_CHANGED);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new ResponseReceiver();
		registerReceiver(receiver, filter);

		Log.w(TAG, "OnCreate");
	}

	// we could have a method start service, which could call startService again
	// if mSettings.isServiceRunning == true && mDetectorService == null
	// this should be done when we try to to da call to the service but the service is still null
	
	
	// this method resurrects everything, that should be running or visible but was killed by onDestroy 
	private void resurrectDestroyed() {
		if (mSettings.isServiceRunning()){
			//mTimerHandler.postDelayed(mRunTimerUpdate, TIMER_UPDATE_MS);
		}
		
		if (mSettings.isServiceRunning() && mDetectorService == null) {
			getApplication().bindService(
					new Intent(ExerciseDetectorActivity.this,
							DetectorService.class), mDetectorConnection, 0);
		}
	}

	class ResponseReceiver extends BroadcastReceiver {
		public static final String ACTION_EXERCISE_STATE_CHANGED = "exercise.state.changed";

		public static final String PARAM_STATE = "state";
		public static final String PARAM_TIMESTAMP = "timestamp";

		@Override
		public void onReceive(Context context, Intent intent) {

			ExerciseState newState = ExerciseState.values()[intent.getExtras()
					.getInt(PARAM_STATE)];
			long timestamp = intent.getExtras().getLong(PARAM_TIMESTAMP);
			
			
			// reset timer to 0 to start counting time again
			mSettings.saveStartTimestamp(System.currentTimeMillis());
			mTvExerciseState.setText(newState.toString());
		}
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
		mTimerHandler.removeCallbacks(mRunTimerUpdate);
		super.onDestroy();
	}
}
