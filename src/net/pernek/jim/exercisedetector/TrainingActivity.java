package net.pernek.jim.exercisedetector;

import net.pernek.jim.exercisedetector.database.TrainingContentProvider.TrainingPlan;
import net.pernek.jim.exercisedetector.entities.Exercise;
import net.pernek.jim.exercisedetector.entities.Series;
import net.pernek.jim.exercisedetector.entities.Training;
import net.pernek.jim.exercisedetector.ui.CircularProgressControl;
import net.pernek.jim.exercisedetector.ui.CircularProgressControl.CircularProgressState;
import net.pernek.jim.exercisedetector.ui.RepetitionAnimation;
import net.pernek.jim.exercisedetector.ui.RepetitionAnimationListener;
import net.pernek.jim.exercisedetector.ui.SwipeControl;
import net.pernek.jim.exercisedetector.ui.SwipeListener;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.gson.Gson;

public class TrainingActivity extends Activity implements SwipeListener,
		RepetitionAnimationListener {

	private static final String TAG = Utils.getApplicationTag();

	private final static int MENU_SYNC = Menu.FIRST;
	private final static int MENU_LOGOUT = Menu.FIRST + 1;

	private static final int ACTIVITY_REQUEST_TRAININGS_LIST = 0;

	/**
	 * In ms.
	 */
	private static final int REST_PROGRESS_UPDATE_RATE = 300;

	private DetectorSettings mSettings;
	private ResponseReceiver mBroadcastReceiver;

	/**
	 * References to the XML defined UI controls.
	 */
	private CircularProgressControl mCircularProgress;
	private ViewFlipper mViewFlipper;
	private SwipeControl mSwipeControl;
	private LinearLayout mTrainingSelector;
	private ProgressDialog mProgressDialog;
	private TextView mTrainingSelectorText;
	private RelativeLayout mBottomContainer;
	private ImageView mInfoButton;
	private LinearLayout mSeriesInformation;
	private TextView mSeriesInfoText;
	private TextView mTrainingCommentText;
	private LinearLayout mAnimationRectangle;

	/**
	 * Reference to the repetition animation.
	 */
	private RepetitionAnimation mRepetitionAnimation;

	/**
	 * Reference to JSON2Object converter.
	 */
	private Gson mGsonInstance = new Gson();

	/**
	 * Holds the currently active training or {@code null} if no training is
	 * active;
	 */
	private Training mCurrentTraining;

	/**
	 * UI thread handler.
	 */
	private Handler mUiHandler = new Handler();

	/**
	 * Holds the ID of the currently selected training rating (or -1 if no
	 * rating was yet selected).
	 */
	private int mTrainingRatingSelectedID = -1;

	/**
	 * Holds ID of the training plan currently selected in the training
	 * selector.
	 */
	private int mSelectedTrainingId = -1;

	/**
	 * The get ready interval in ms. TODO: this value could be moved to the
	 * training plan (and set on the web or somehow made configurable).
	 */
	private int mGetReadyInterval = 5000;

	/**
	 * Holds the start timestamp for the get ready interval. This one is not
	 * stored inside the training plan, as if the activity restarts in the
	 * middle of the training plan the get ready timer simply cancels and has to
	 * be started again by the user. NOTE: Value -1 means the get reads timer
	 * was not started yet.
	 */
	private long mGetReadyStartTimestamp = -1;

	/**
	 * Contains IDs of training rating images in non-selected (non-clicked)
	 * state.
	 */
	private static int[] TRAINING_RATING_IMAGES = { R.drawable.sm_1_ns,
			R.drawable.sm_2_ns, R.drawable.sm_3_ns, R.drawable.sm_4_ns };

	/**
	 * Contains IDs of training rating images in selected (touched) state.
	 */
	private static int[] TRAINING_RATING_SELECTED_IMAGES = { R.drawable.sm_1_s,
			R.drawable.sm_2_s, R.drawable.sm_3_s, R.drawable.sm_4_s };

	/**
	 * References to the ImageViews hosting training rating images.
	 */
	private ImageView[] mTrainingRatingImages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		mSettings = DetectorSettings.create(PreferenceManager
				.getDefaultSharedPreferences(this));

		// if we are not logged in yet show the login activity first
		if (mSettings.getUsername().equals("")) {
			startActivity(new Intent(TrainingActivity.this, LoginActivity.class));
			finish();
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training);

		mCircularProgress = (CircularProgressControl) findViewById(R.id.circularProgress);
		mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		mSwipeControl = (SwipeControl) findViewById(R.id.swipeControl);
		mTrainingSelector = (LinearLayout) findViewById(R.id.trainingSelector);
		mTrainingSelectorText = (TextView) findViewById(R.id.trainingSelectorText);
		mBottomContainer = (RelativeLayout) findViewById(R.id.bottomContainer);
		mInfoButton = (ImageView) findViewById(R.id.info_button);
		mSeriesInformation = (LinearLayout) findViewById(R.id.seriesInformation);
		mSeriesInfoText = (TextView) findViewById(R.id.nextSeriesText);
		mTrainingCommentText = (TextView) findViewById(R.id.textTrainingComment);
		mAnimationRectangle = (LinearLayout) findViewById(R.id.animationRectangle);

		updateTrainingSelector(-1);
		initializeTrainingRatings();
		loadCurrentTraining();
		updateScreen();

		mCircularProgress.setOnClickListener(mCircularButtonClick);

		mSwipeControl.addSwipeListener(this);

		mRepetitionAnimation = new RepetitionAnimation(mAnimationRectangle,
				mUiHandler);
		mRepetitionAnimation.addRepetitionAnimationListener(this);

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

	/*
	 * TODO: Check if stuff in onCreate and onDestroy should be moved to more
	 * appropriate lifecycle methods.
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		mSwipeControl.removeSwipeListener(this);
		mRepetitionAnimation.removeRepetitionAnimationListener(this);

		// remove all periodical tasks
		mUiHandler.removeCallbacks(mUpdateRestTimer);

		super.onDestroy();
	}

	/**
	 * Initializes a list of training rating ImageViews references.
	 * Additionally, sets the non-selected images.
	 */
	private void initializeTrainingRatings() {
		mTrainingRatingSelectedID = -1;
		mTrainingCommentText.setText("");

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

	/**
	 * This method either shows the training selector control and hides the
	 * swipe control or vice versa.
	 * 
	 * @param visible
	 */
	private void setTrainingSelectorVisible(boolean visible) {
		// the container should be visible in either case
		mBottomContainer.setVisibility(View.VISIBLE);
		mInfoButton.setVisibility(View.VISIBLE);

		mTrainingSelector
				.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		mSwipeControl.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
	}

	/**
	 * This method loads the currently active training to a memory object from
	 * json string saved in application settings. If the json string is
	 * {@code null} the memory object gets set to {@code null} as well.
	 */
	private void loadCurrentTraining() {
		if (!mSettings.getCurrentTrainingPlan().equals("")) {
			Gson gson = new Gson();
			mCurrentTraining = gson.fromJson(
					mSettings.getCurrentTrainingPlan(), Training.class);
		} else {
			mCurrentTraining = null;
		}

	}

	/**
	 * Saves current state of the training plan to permanent storage. TODO:
	 * Currently, we serialize the whole training after each change. We could do
	 * this a bit lighter in the future.
	 */
	private void saveCurrentTraining() {
		mSettings.saveCurrentTrainingPlan(mCurrentTraining == null ? ""
				: mGsonInstance.toJson(mCurrentTraining));
	}

	/**
	 * This method updates the training selector text to the first training plan
	 * in the database or to the plan corresponding with the @param
	 * trainingPlanID. NOTE: Additionally, this method is responsible for
	 * settings the mSelectedTrainingId variable, which holds the ID of the
	 * currently selected training.
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
			mSelectedTrainingId = trainings.getInt(trainings
					.getColumnIndex(TrainingPlan._ID));
			String trainingName = trainings.getString(trainings
					.getColumnIndex(TrainingPlan.NAME));
			mTrainingSelectorText.setText(trainingName);
		}
	}

	/**
	 * Toggles the visibility of additional info circular button overlay and
	 * sets the appropriate icon.
	 * 
	 * @param visible
	 */
	private void toggleInfoButtonVisible(boolean visible) {
		mCircularProgress.setInfoVisible(visible);

		mInfoButton.setImageResource(visible ? R.drawable.chair_ico_selected
				: R.drawable.chair_ico);
	}

	/**
	 * Triggered on additional info button click.
	 * 
	 * @param v
	 */
	public void onInfoButtonClick(View v) {
		toggleInfoButtonVisible(!mCircularProgress.isInfoVisible());
	}

	/**
	 * Triggered on finish button click in the training rating screen.
	 * 
	 * @param v
	 */
	public void onFinishClick(View v) {
		if (mTrainingRatingSelectedID == -1) {
			Toast.makeText(getApplicationContext(),
					"You should rate the training.", Toast.LENGTH_SHORT).show();
		} else {
			mCurrentTraining.setTrainingRating(mTrainingRatingSelectedID);
			mCurrentTraining.setTrainingComment(mTrainingCommentText.getText()
					.toString());

			saveCurrentTraining();

			updateScreen();
			mViewFlipper.showPrevious();
		}
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

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		// add sync option only if the user is logged in (or even better,
		// disable sync if no user is logged in)
		if (!mSettings.getUsername().equals("")) {
			menu.add(1, MENU_SYNC, 1, "Sync");
		}
		menu.add(1, MENU_LOGOUT, 3, "Logout");
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
		case MENU_LOGOUT: {
			mSettings.saveUsername("");
			mSettings.savePassword("");
			finish();

			break;
		}
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void changeTrainingPlanState() {
		if (mCircularProgress.isInfoVisible()) {
			// if info button is visible close it on tap
			toggleInfoButtonVisible(false);
		} else if (mCurrentTraining == null) {
			// start button was clicked
			String[] projection = { TrainingPlan._ID, TrainingPlan.DATA };
			String selection = String.format("%s == %d", TrainingPlan._ID,
					mSelectedTrainingId);
			Cursor trainings = managedQuery(TrainingPlan.CONTENT_URI,
					projection, selection, null, null);

			if (trainings.moveToNext()) {
				String jsonEncodedTraining = trainings.getString(trainings
						.getColumnIndex(TrainingPlan.DATA));

				// load to memory
				mCurrentTraining = mGsonInstance.fromJson(jsonEncodedTraining,
						Training.class);
				mCurrentTraining.startTraining();
			}
		} else if (mCurrentTraining.getCurrentExercise() == null) {
			if (!mCurrentTraining.isTrainingEnded()) {
				mCurrentTraining.endTraining();
			} else {
				mCurrentTraining = null;
				initializeTrainingRatings();
			}

		} else if (mCurrentTraining.isCurrentRest()) {
			// we will initiate exercise start here
			mGetReadyStartTimestamp = System.currentTimeMillis();
		} else {
			mCurrentTraining.endExercise();

			if (mRepetitionAnimation.isAnimationRunning()) {
				mRepetitionAnimation.cancelAnimation();
			}

			// advance to the next activity
			mCurrentTraining.nextActivity();
		}

		saveCurrentTraining();

		updateScreen();
	}

	/**
	 * This is the sole method responsible for setting the activity UI (apart
	 * from runnables, which also have to be registered in this method, and are
	 * responsible for periodic screen changes)
	 */
	private void updateScreen() {
		setTrainingSelectorVisible(mCurrentTraining == null);
		if (mCurrentTraining == null) {
			// no training started yet, show the start button
			mCircularProgress.setCurrentState(CircularProgressState.START);
			mInfoButton.setVisibility(View.INVISIBLE);
			mSeriesInformation.setVisibility(View.INVISIBLE);
		} else if (mCurrentTraining.getCurrentExercise() == null) {
			if (!mCurrentTraining.isTrainingEnded()) {
				// no more exercises, show the done button
				mCircularProgress.setCurrentState(CircularProgressState.STOP);

			} else if (mCurrentTraining.getTrainingRating() == -1) {
				// show training rating screen
				mViewFlipper.showNext();
			} else {
				// show overview
				mCircularProgress
						.setCurrentState(CircularProgressState.OVERVIEW);
			}

			// also hide the bottom container
			mBottomContainer.setVisibility(View.INVISIBLE);
			mInfoButton.setVisibility(View.INVISIBLE);
			mSeriesInformation.setVisibility(View.INVISIBLE);
		} else {
			Exercise curExercise = mCurrentTraining.getCurrentExercise();
			// there are still some exercises to be performed
			if (mCurrentTraining.isCurrentRest()) {
				// first remove all existing callbacks
				mUiHandler.removeCallbacks(mUpdateRestTimer);
				mUiHandler.removeCallbacks(mGetReadyTimer);

				// now show all the common information
				Series curSeries = curExercise.getCurrentSeries();
				mCircularProgress.setCurrentState(CircularProgressState.REST);
				mSwipeControl.setCenterText("Next: ", curExercise
						.getExerciseType().getName());
				mSeriesInfoText.setText(String.format(
						"Series %d (%d reps, %d kg)",
						curExercise.getCurrentSeriesNumber(),
						curSeries.getNumberTotalRepetitions(),
						curSeries.getWeight()));
				mSeriesInformation.setVisibility(View.VISIBLE);

				// get ready timer was not started yet so show the rest timer
				if (mGetReadyStartTimestamp == -1) {
					int currentRest = curSeries.getRestTime();
					mCircularProgress.setRestMaxProgress(currentRest);
					mCircularProgress.setRestMinProgress(0);

					// also start the periodic timer to update the rest screen
					mUiHandler.postDelayed(mUpdateRestTimer, 0);
				} else {
					mCircularProgress.setRestProgressValue(0);

					mUiHandler.postDelayed(mGetReadyTimer, 0);
				}
			} else {
				// otherwise show exercising UI
				mCircularProgress.setCurrentRepetition(mCurrentTraining
						.getCurrentRepetition());
				mCircularProgress.setTotalRepetitions(mCurrentTraining
						.getTotalRepetitions());
				mCircularProgress.setCurrentSeries(mCurrentTraining
						.getCurrentSeriesNumber());
				mCircularProgress.setTotalSeries(mCurrentTraining
						.getTotalSeriesForCurrentExercise());
				mCircularProgress
						.setCurrentState(CircularProgressState.EXERCISE);
				mSwipeControl.setCenterText(
						"",
						String.format("%s %d", mCurrentTraining
								.getCurrentExercise().getExerciseType()
								.getName(), mCurrentTraining
								.getCurrentExercise().getCurrentSeries()
								.getWeight()));

				mSeriesInformation.setVisibility(View.INVISIBLE);

				// remove any periodic rest timers
				mUiHandler.removeCallbacks(mUpdateRestTimer);
			}

			// set exercise and training progres bars
			mCircularProgress.setTrainingMaxProgress(mCurrentTraining
					.getTotalSeriesCount());
			mCircularProgress.setTrainingMinProgress(0);
			mCircularProgress.setTrainingProgressValue(mCurrentTraining
					.getSeriesPerformedCount());

			mCircularProgress.setExerciseMaxProgress(curExercise
					.getAllSeriesCount());
			mCircularProgress.setExerciseMinProgress(0);
			mCircularProgress.setExerciseProgressValue(curExercise
					.getAllSeriesCount() - curExercise.getSeriesLeftCount());
		}
	}

	/**
	 * Starts the select training activity.
	 * 
	 * @param view
	 */
	public void onSelectTrainingClick(View view) {
		Intent intent = new Intent(TrainingActivity.this,
				TrainingSelectionList.class);
		startActivityForResult(intent, ACTIVITY_REQUEST_TRAININGS_LIST);
	}

	/*
	 * Schedule exercise later. Ends the current exercise if in the middle of
	 * exercising.
	 * 
	 * @see net.pernek.jim.exercisedetector.ui.SwipeListener#onSwipeRight()
	 */
	@Override
	public void onSwipeRight() {
		if (!mCurrentTraining.isCurrentRest()) {
			mCurrentTraining.endExercise();
		}

		mCurrentTraining.scheduleExerciseLater();
		saveCurrentTraining();
		toggleInfoButtonVisible(false);
		updateScreen();
	}

	/*
	 * Skip exercise. Ends the current exercise if in the middle of exercising.
	 * 
	 * @see net.pernek.jim.exercisedetector.ui.SwipeListener#onSwipeLeft()
	 */
	@Override
	public void onSwipeLeft() {
		if (!mCurrentTraining.isCurrentRest()) {
			mCurrentTraining.endExercise();
		}

		mCurrentTraining.nextExercise();
		saveCurrentTraining();
		toggleInfoButtonVisible(false);
		updateScreen();
	}

	/*
	 * Repetition animation has legally ended so we should advance the training
	 * plan.
	 * 
	 * @see net.pernek.jim.exercisedetector.ui.RepetitionAnimationListener#
	 * onAnimationEnded()
	 */
	@Override
	public void onAnimationEnded() {
		changeTrainingPlanState();
	}

	/*
	 * Triggered after each individual repetition is executed.
	 * 
	 * @see net.pernek.jim.exercisedetector.ui.RepetitionAnimationListener#
	 * onRepetitionCompleted()
	 */
	@Override
	public void onRepetitionCompleted() {
		mCurrentTraining.increaseCurrentRepetition();
		updateScreen();
	}

	/**
	 * Defines what should happen on circular button click based on the state of
	 * the current training plan (mCurrentTraining).
	 */
	private View.OnClickListener mCircularButtonClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			changeTrainingPlanState();
		}
	};

	private Runnable mGetReadyTimer = new Runnable() {

		@Override
		public void run() {
			long msLeft = System.currentTimeMillis() - mGetReadyStartTimestamp;

			if (msLeft < mGetReadyInterval) {
				mCircularProgress.setRestProgressValue(0);
				mCircularProgress.setTimer(Math.round((float) msLeft / 1000));

				mUiHandler.postDelayed(this, REST_PROGRESS_UPDATE_RATE);
			} else {
				// mark the the timer is over
				mGetReadyStartTimestamp = -1;
				
				// time is up, start the exercise animation
				mCurrentTraining.startExercise();
				// TODO: here we could decide to either show the count down,
				// repetition animation or just an empty exercise screen

				// TODO: change repetition duration with actual number once
				// stored
				// in the training plan
				// start animation
				mRepetitionAnimation.startAnimation(
						mViewFlipper.getMeasuredHeight(), 1000, 400, 500, 2000,
						mCurrentTraining.getTotalRepetitions());
				
				updateScreen();
			}
		}
	};

	/**
	 * This runnable updates the screen during the rest state.It calls itself
	 * recursively until externally stopped or until there are no more exercises
	 * left.
	 */
	private Runnable mUpdateRestTimer = new Runnable() {

		@Override
		public void run() {
			Exercise currentExercise = mCurrentTraining.getCurrentExercise();
			if (currentExercise != null) {
				int currentRest = currentExercise.getCurrentSeries()
						.getRestTime();
				int currentRestLeft = mCurrentTraining
						.calculateCurrentRestLeft();
				mCircularProgress.setRestProgressValue(currentRestLeft < 0 ? 0
						: currentRestLeft);
				mCircularProgress.setTimer(Math.abs(currentRestLeft));
				// Log.d(TAG, String.format("Update screen: %d, %d",
				// currentRest,
				// currentRestLeft));

				mUiHandler.postDelayed(this, REST_PROGRESS_UPDATE_RATE);
			}
		}
	};

	/*
	 * Gets the results from activities started from this activity.
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_REQUEST_TRAININGS_LIST: {
			// on training selected from list of trainings
			if (data != null
					&& resultCode == RESULT_OK
					&& data.hasExtra(TrainingSelectionList.INTENT_EXTRA_SELECTED_TRAINING_KEY)) {
				final long trainingId = data
						.getExtras()
						.getLong(
								TrainingSelectionList.INTENT_EXTRA_SELECTED_TRAINING_KEY,
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

	/**
	 * This class is used to listen to broadcasts from other services and
	 * activities.
	 * 
	 * @author Igor
	 * 
	 */
	private class ResponseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					DataUploaderService.ACTION_FETCH_TRAINNGS_DONE)) {
				// on finished fetching trainings

				mProgressDialog.dismiss();

				boolean getTrainingSuccessful = intent.getExtras().getBoolean(
						DataUploaderService.PARAM_OP_SUCCESSFUL);

				if (getTrainingSuccessful) {
					updateTrainingSelector(-1);
				}
			} else if (intent.getAction().equals(
					DataUploaderService.ACTION_FETCH_TRAINNGS_LIST_DOWNLOADED)) {
				// on list with training names fetched

				// calculate progress bar information and set progress
				int totalNumberOfTrainings = intent.getExtras().getInt(
						DataUploaderService.PARAM_FETCH_TRAINNGS_NUM_ITEMS);
				int progress = Math.round(1f / totalNumberOfTrainings * 100f);
				mProgressDialog.setProgress(progress);
			} else if (intent.getAction().equals(
					DataUploaderService.ACTION_FETCH_TRAINNGS_ITEM_DOWNLOADED)) {
				// on individual training item downloaded

				// calculate progress bar information and set progress with
				// training name
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
}
