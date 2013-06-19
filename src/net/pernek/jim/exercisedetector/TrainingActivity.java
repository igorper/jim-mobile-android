package net.pernek.jim.exercisedetector;

import java.util.List;

import com.google.gson.Gson;

import net.pernek.jim.exercisedetector.CircularProgressControl.CircularProgressState;
import net.pernek.jim.exercisedetector.database.TrainingContentProvider.TrainingPlan;
import net.pernek.jim.exercisedetector.entities.Exercise;
import net.pernek.jim.exercisedetector.entities.Training;
import net.pernek.jim.exercisedetector.ui.SwipeControl;
import net.pernek.jim.exercisedetector.ui.SwipeListener;
import net.pernek.jim.exercisedetector.ui.TrainingSelectionList;
import net.pernek.jim.exercisedetector.util.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class TrainingActivity extends Activity implements SwipeListener {

	private static final String TAG = Utils.getApplicationTag();

	private final static int MENU_SYNC = Menu.FIRST;
	private final static int MENU_SWIPE = Menu.FIRST + 1;
	private final static int MENU_LOGOUT = Menu.FIRST + 2;
	private final static int MENU_FETCH_17 = Menu.FIRST + 3;

	private static final int ACTIVITY_REQUEST_TRAININGS_LIST = 0;

	private DetectorSettings mSettings;

	private ResponseReceiver mBroadcastReceiver;

	private CircularProgressControl mCircularProgress;
	private ViewFlipper mViewFlipper;
	private SwipeControl mSwipeControl;
	private LinearLayout mTrainingSelector;
	private ProgressDialog mProgressDialog;
	private TextView mTrainingSelectorText;

	private int mTrainingCounter = 0;
	private int mExerciseCounter = 0;
	private int mRestCounter = 100;

	private Handler mUiHandler = new Handler();
	private Runnable mRunTimerUpdate = new Runnable() {

		@Override
		public void run() {
			mCircularProgress.setTrainingProgressValue(mTrainingCounter++);
			mCircularProgress.setExerciseProgressValue(mExerciseCounter++);
			mCircularProgress.setRestProgressValue(mRestCounter--);

			mUiHandler.postDelayed(this, 100);
		}
	};

	/**
	 * Contains IDs of training rating images in non-selected (non-clicked)
	 * state.
	 */
	private static int[] TRAINING_RATING_IMAGES = { R.drawable.sm_1_ns,
			R.drawable.sm_2_ns, R.drawable.sm_3_ns, R.drawable.sm_4_ns };

	/**
	 * Holds the ID of the currently selected training rating (or -1 if no
	 * rating was yet selected).
	 */
	private int mTrainingRatingSelectedID = -1;

	/**
	 * Contains IDs of training rating images in selected (touched) state.
	 */
	private static int[] TRAINING_RATING_SELECTED_IMAGES = { R.drawable.sm_1_s,
			R.drawable.sm_2_s, R.drawable.sm_3_s, R.drawable.sm_4_s };

	/**
	 * References to the ImageViews hosting training rating images.
	 */
	private ImageView[] mTrainingRatingImages;

	/**
	 * Initializes a list of training rating ImageViews references.
	 * Additionally, sets the non-selected images.
	 */
	private void initializeTrainingRatings() {
		mTrainingRatingImages = new ImageView[4];
		mTrainingRatingImages[0] = (ImageView) findViewById(R.id.trainingRating1);
		mTrainingRatingImages[1] = (ImageView) findViewById(R.id.trainingRating2);
		mTrainingRatingImages[2] = (ImageView) findViewById(R.id.trainingRating3);
		mTrainingRatingImages[3] = (ImageView) findViewById(R.id.trainingRating4);

		for (int i = 0; i < mTrainingRatingImages.length; i++) {
			mTrainingRatingImages[i]
					.setImageResource(TRAINING_RATING_IMAGES[i]);
		}
	}

	private int stateCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		mSettings = DetectorSettings.create(PreferenceManager
				.getDefaultSharedPreferences(this));

		// if we are not logged in yet show the login activity first
		if (mSettings.getUsername().equals("")) {
			startActivity(new Intent(TrainingActivity.this, LoginActivity.class));
			finish();
		}

		// TODO: Use ViewFlipper to change between button circular button view,
		// and rate training view
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training);

		initializeTrainingRatings();

		mCircularProgress = (CircularProgressControl) findViewById(R.id.circularProgress);
		mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		mSwipeControl = (SwipeControl) findViewById(R.id.swipeControl);
		mTrainingSelector = (LinearLayout) findViewById(R.id.trainingSelector);
		mTrainingSelectorText = (TextView) findViewById(R.id.trainingSelectorText);

		updateTrainingSelector(-1);

		mCircularProgress.setRestMaxProgress(100);
		mCircularProgress.setRestMinProgress(0);

		CircularProgressState currentState = CircularProgressState.values()[stateCount];

		mCircularProgress.setCurrentState(currentState);
		mCircularProgress.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				Toast.makeText(getApplicationContext(), "test",
						Toast.LENGTH_LONG).show();
				mCircularProgress.setCurrentState(CircularProgressState
						.values()[++stateCount % (CircularProgressState.values().length)]);

				if (mCircularProgress.getCurrentState() == CircularProgressState.REST) {
					mTrainingCounter = 0;
					mExerciseCounter = 0;
					mRestCounter = 100;

					mUiHandler.removeCallbacks(mRunTimerUpdate);
					mUiHandler.postDelayed(mRunTimerUpdate, 100);
				}
				return true;
			}
		});

		mCircularProgress.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mViewFlipper.showNext();

			}
		});
		
		mSwipeControl.addSwipeListener(this);

		// TODO: We could create a class called JimActivity which could handle
		// all the
		// communication logic (ResponseReciver for different intents) and
		// define all (e.g.
		// DetectiorSettings, TAG, etc)
		IntentFilter filter = new IntentFilter(
				DataUploaderService.ACTION_FETCH_TRAINNGS_DONE);
		filter.addAction(DataUploaderService.ACTION_FETCH_TRAINNGS_LIST_DOWNLOADED);
		filter.addAction(DataUploaderService.ACTION_FETCH_TRAINNGS_ITEM_DOWNLOADED);

		filter.addCategory(Intent.CATEGORY_DEFAULT);

		mBroadcastReceiver = new ResponseReceiver();
		registerReceiver(mBroadcastReceiver, filter);
	}

	/**
	 * This method updates the training selector text to the first training plan
	 * in the database or to the plan corresponding with the @param trainingPlanID.
	 */
	/**
	 * @param trainingPlanID
	 */
	private void updateTrainingSelector(long trainingPlanID) {
		String[] projection = { TrainingPlan._ID, TrainingPlan.NAME };
		String selection = trainingPlanID == -1 ? null : String.format(
				"%s == %d", TrainingPlan._ID, trainingPlanID);
		Cursor trainings = managedQuery(TrainingPlan.CONTENT_URI, projection,
				selection, null, null);

		if (trainings.moveToNext()) {
			String trainingName = trainings.getString(trainings
					.getColumnIndex(TrainingPlan.NAME));
			mTrainingSelectorText.setText(trainingName);
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		mSwipeControl.removeSwipeListener(this);

		super.onDestroy();
	}

	public void testClick(View v) {
		mViewFlipper.showPrevious();
	}

	/**
	 * Triggered when clicked on a specific training rating icon.
	 * 
	 * @param v
	 *            The view hosting the clicked icon (of type ImageView).
	 */
	public void onTrainingRatingSelected(View v) {
		ImageView trainingRatingSelected = (ImageView) v;

		int imageSelectedPadding = getResources().getDimensionPixelSize(
				R.dimen.training_rating_smile_selected_padding);
		int imagePadding = getResources().getDimensionPixelSize(
				R.dimen.training_rating_smile_padding);

		// loop through training ratings image views and set the appropriate
		// image
		for (int i = 0; i < mTrainingRatingImages.length; i++) {
			if (mTrainingRatingImages[i] == trainingRatingSelected) {
				mTrainingRatingImages[i]
						.setImageResource(TRAINING_RATING_SELECTED_IMAGES[i]);
				mTrainingRatingImages[i].setPadding(imageSelectedPadding,
						imageSelectedPadding, imageSelectedPadding,
						imageSelectedPadding);
				mTrainingRatingSelectedID = i;
			} else {
				mTrainingRatingImages[i]
						.setImageResource(TRAINING_RATING_IMAGES[i]);
				mTrainingRatingImages[i].setPadding(imagePadding, imagePadding,
						imagePadding, imagePadding);
			}
		}
	}

	public void onSelectTrainingClick(View view) {
		Intent intent = new Intent(TrainingActivity.this,
				TrainingSelectionList.class);
		startActivityForResult(intent, ACTIVITY_REQUEST_TRAININGS_LIST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_REQUEST_TRAININGS_LIST: {
			if (data != null
					&& resultCode == RESULT_OK
					&& data.hasExtra(TrainingSelectionList.INTENT_EXTRA_SELECTED_TRAINING_KEY)) {
				final long trainingId = data
						.getExtras()
						.getLong(TrainingSelectionList.INTENT_EXTRA_SELECTED_TRAINING_KEY,
								-1);

				mUiHandler.post(new Runnable() {

					@Override
					public void run() {
						updateTrainingSelector(trainingId);
					}
				});
			}
			break;
		}
		default:
			Log.d(TAG, "onActivityResult default switch.");
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		// add sync option only if the user is logged in
		if (!mSettings.getUsername().equals("")) {
			menu.add(1, MENU_SYNC, 1, "Sync");
		}
		menu.add(1, MENU_SWIPE, 2, "Swipe");
		menu.add(1, MENU_LOGOUT, 3, "Logout");
		menu.add(1, MENU_FETCH_17, 3, "Fetch 17");
		return true;
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SYNC: {
			Intent intent = new Intent(this, DataUploaderService.class);
			intent.putExtra(DataUploaderService.INTENT_KEY_ACTION,
					DataUploaderService.ACTION_FETCH_TRAININGS);
			intent.putExtra(DataUploaderService.INTENT_KEY_USERNAME,
					mSettings.getUsername());
			intent.putExtra(DataUploaderService.INTENT_KEY_PASSWORD,
					mSettings.getPassword());
			startService(intent);

			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMessage("Fetching training list ...");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.show();

			break;
		}
		case MENU_SWIPE: {
			if (mSwipeControl.getVisibility() == View.VISIBLE) {
				mSwipeControl.setVisibility(View.INVISIBLE);
				mTrainingSelector.setVisibility(View.VISIBLE);
			} else {
				mSwipeControl.setVisibility(View.VISIBLE);
				mTrainingSelector.setVisibility(View.INVISIBLE);
			}
			break;
		}
		case MENU_LOGOUT: {
			mSettings.saveUsername("");
			mSettings.savePassword("");
			finish();

			break;
		}
		case MENU_FETCH_17: {
//			Intent intent = new Intent(this, DataUploaderService.class);
//			intent.putExtra(DataUploaderService.INTENT_KEY_ACTION,
//					DataUploaderService.ACTION_GET_TRAINING);
//			intent.putExtra(DataUploaderService.INTENT_KEY_USERNAME,
//					mSettings.getUsername());
//			intent.putExtra(DataUploaderService.INTENT_KEY_PASSWORD,
//					mSettings.getPassword());
//			intent.putExtra(DataUploaderService.INTENT_KEY_TRAINING_ID, 17);
//			startService(intent);
			
			String[] projection = { TrainingPlan._ID, TrainingPlan.NAME, TrainingPlan.DATA };
			String selection = String.format(
					"%s == %d", TrainingPlan._ID, 17);
			Cursor trainings = managedQuery(TrainingPlan.CONTENT_URI, projection,
					selection, null, null);

			if (trainings.moveToNext()) {
				String trainingData = trainings.getString(trainings
						.getColumnIndex(TrainingPlan.DATA));
				
				Gson gson = new Gson();
				Training t = gson.fromJson(trainingData, Training.class);
				
				List<Exercise> exerr = t.getExercises();
				int z=0;
				z++;
			}

			break;
		}
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private class ResponseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					DataUploaderService.ACTION_FETCH_TRAINNGS_DONE)) {
				boolean getTrainingSuccessful = intent.getExtras().getBoolean(
						DataUploaderService.PARAM_OP_SUCCESSFUL);

				mProgressDialog.dismiss();

				if (getTrainingSuccessful) {
					Toast.makeText(getApplicationContext(),
							"Wuhu! Trainings downloaded. Let's go!",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(
							getApplicationContext(),
							"Ups. Unable to fetch trainings. There should be a nicer UI for this message!",
							Toast.LENGTH_SHORT).show();
				}
			} else if (intent.getAction().equals(
					DataUploaderService.ACTION_FETCH_TRAINNGS_LIST_DOWNLOADED)) {
				int totalNumberOfTrainings = intent.getExtras().getInt(
						DataUploaderService.PARAM_FETCH_TRAINNGS_NUM_ITEMS);
				int progress = Math.round(1f / totalNumberOfTrainings * 100f);
				mProgressDialog.setProgress(progress);
			} else if (intent.getAction().equals(
					DataUploaderService.ACTION_FETCH_TRAINNGS_ITEM_DOWNLOADED)) {
				int totalNumberOfTrainings = intent.getExtras().getInt(
						DataUploaderService.PARAM_FETCH_TRAINNGS_NUM_ITEMS);
				int trainingCount = intent.getExtras().getInt(
						DataUploaderService.PARAM_FETCH_TRAINNGS_CUR_ITEM_CNT);
				String trainingName = intent.getExtras().getString(
						DataUploaderService.PARAM_FETCH_TRAINNGS_CUR_ITEM_NAME);
				int progress = Math.round((1f + trainingCount)
						/ totalNumberOfTrainings * 100f);
				mProgressDialog.setMessage("Fetching " + trainingName);
				mProgressDialog.setProgress(progress);
			}
		}
	}

	@Override
	public void onSwipeLeft() {
		mCircularProgress.setCurrentRepetition(mCircularProgress.getCurrentRepetition() + 1);
	}

	@Override
	public void onSwipeRight() {
		// TODO Auto-generated method stub
		
	}
}
