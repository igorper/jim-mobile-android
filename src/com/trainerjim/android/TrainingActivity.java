package com.trainerjim.android;

import java.io.IOException;

import net.pernek.jim.exercisedetector.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
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
import com.trainerjim.android.AccelerationRecorder.AccelerationRecordingTimestamps;
import com.trainerjim.android.entities.Exercise;
import com.trainerjim.android.entities.Series;
import com.trainerjim.android.entities.Training;
import com.trainerjim.android.storage.PermanentSettings;
import com.trainerjim.android.storage.TrainingContentProvider.CompletedTraining;
import com.trainerjim.android.storage.TrainingContentProvider.TrainingPlan;
import com.trainerjim.android.ui.CircularProgressControl;
import com.trainerjim.android.ui.RepetitionAnimation;
import com.trainerjim.android.ui.RepetitionAnimationListener;
import com.trainerjim.android.ui.SwipeControl;
import com.trainerjim.android.ui.SwipeListener;
import com.trainerjim.android.ui.CircularProgressControl.CircularProgressState;
import com.trainerjim.android.util.Utils;

public class TrainingActivity extends Activity implements SwipeListener,
		RepetitionAnimationListener {

	private static final String TAG = Utils.getApplicationTag();

	private final static int MENU_SYNC = Menu.FIRST;
	private final static int MENU_UPLOAD = Menu.FIRST + 1;
	private final static int MENU_LOGOUT = Menu.FIRST + 2;
	private final static int MENU_FULL_RATE_TOGGLE = Menu.FIRST + 3;

	private static final int ACTIVITY_REQUEST_TRAININGS_LIST = 0;

	/**
	 * In ms.
	 */
	private static final int REST_PROGRESS_UPDATE_RATE = 300;

	private PermanentSettings mSettings;
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
	private ImageView mImageArrowSeriesInfo;
	private LinearLayout mViewRateExercise;
	private ImageView mIconWeight;

	private AccelerationRecorder mAccelerationRecorder;

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
	 * The get ready interval in seconds. TODO: this value could be moved to the
	 * training plan (and set on the web or somehow made configurable).
	 */
	private int mGetReadyInterval = 5;

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
			R.drawable.sm_2_ns, R.drawable.sm_3_ns };

	/**
	 * Contains IDs of training rating images in selected (touched) state.
	 */
	private static int[] TRAINING_RATING_SELECTED_IMAGES = { R.drawable.sm_1_s,
			R.drawable.sm_2_s, R.drawable.sm_3_s };

	/**
	 * References to the ImageViews hosting training rating images.
	 */
	private ImageView[] mTrainingRatingImages;

	/**
	 * References to the ImageViews hosting exercise rating images.
	 */
	private ImageView[] mExerciseRatingImages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		mSettings = PermanentSettings.create(PreferenceManager
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
		mImageArrowSeriesInfo = (ImageView) findViewById(R.id.imageArrowSeriesInfo);
		mViewRateExercise = (LinearLayout) findViewById(R.id.viewRateExercise);
		mIconWeight = (ImageView) findViewById(R.id.iconWeight);

		updateTrainingSelector(-1);
		initializeTrainingRatings();
		initializeExerciseRatings();
		loadCurrentTraining();
		updateScreen();

		mCircularProgress.setOnClickListener(mCircularButtonClick);

		mSwipeControl.addSwipeListener(this);

		mRepetitionAnimation = new RepetitionAnimation(mAnimationRectangle,
				mUiHandler);
		mRepetitionAnimation.addRepetitionAnimationListener(this);

		mAccelerationRecorder = AccelerationRecorder
				.create(getApplicationContext());

		// TODO: We could create a class called JimActivity which could handle
		// all the
		// communication logic (ResponseReciver for different intents) and
		// define all (e.g.
		// DetectiorSettings, TAG, etc)
		IntentFilter filter = new IntentFilter(
				ServerCommunicationService.ACTION_FETCH_TRAINNGS_COMPLETED);
		filter.addAction(ServerCommunicationService.ACTION_GET_TRAINNGS_LIST_COMPLETED);
		filter.addAction(ServerCommunicationService.ACTION_FETCH_TRAINNG_ITEM_COMPLETED);
		filter.addAction(ServerCommunicationService.ACTION_UPLOAD_TRAININGS_STARTED);
		filter.addAction(ServerCommunicationService.ACTION_TRAININGS_ITEM_UPLOADED);
		filter.addAction(ServerCommunicationService.ACTION_UPLOAD_TRAINNGS_COMPLETED);

		filter.addCategory(Intent.CATEGORY_DEFAULT);
		mBroadcastReceiver = new ResponseReceiver();
		registerReceiver(mBroadcastReceiver, filter);

		// try to fetch trainings if not available
		if (isUserLoggedIn() && !areTrainingsAvailable()) {
			runTrainingsSync();
		}
	}

	/**
	 * Returns <code>true</code> if user is currently logged in, otherwise
	 * <code>false</code>.
	 * 
	 * @return
	 */
	private boolean isUserLoggedIn() {
		return !mSettings.getUsername().equals("");
	}

	/**
	 * Creates a mapping of exercise rating images and exercise rattings.
	 */
	private void initializeExerciseRatings() {
		mExerciseRatingImages = new ImageView[TRAINING_RATING_IMAGES.length];
		mExerciseRatingImages[0] = (ImageView) findViewById(R.id.exerciseRating1);
		mExerciseRatingImages[1] = (ImageView) findViewById(R.id.exerciseRating2);
		mExerciseRatingImages[2] = (ImageView) findViewById(R.id.exerciseRating3);
	}

	/**
	 * Invoked when a specific exercise rating image is clicked.
	 * 
	 * @param v
	 *            contains the referense to the clicked exercise rating image.
	 */
	public void onExerciseRatingSelected(View v) {
		ImageView trainingRatingSelected = (ImageView) v;

		// loop through training ratings image views and set the appropriate
		// image
		for (int i = 0; i < mTrainingRatingImages.length; i++) {
			if (mExerciseRatingImages[i] == trainingRatingSelected) {
				mCurrentTraining.setCurrentSeriesExecutionRating(i);
				mViewRateExercise.setVisibility(View.GONE);
				changeTrainingPlanState();
				break;
			}
		}
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

		mTrainingRatingImages = new ImageView[TRAINING_RATING_IMAGES.length];
		mTrainingRatingImages[0] = (ImageView) findViewById(R.id.trainingRating1);
		mTrainingRatingImages[1] = (ImageView) findViewById(R.id.trainingRating2);
		mTrainingRatingImages[2] = (ImageView) findViewById(R.id.trainingRating3);

		int imagePadding = getResources().getDimensionPixelSize(
				R.dimen.training_rating_smile_padding);

		for (int i = 0; i < mTrainingRatingImages.length; i++) {
			mTrainingRatingImages[i]
					.setImageResource(TRAINING_RATING_IMAGES[i]);
			mTrainingRatingImages[i].setPadding(imagePadding, imagePadding,
					imagePadding, imagePadding);
		}
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

	private boolean areTrainingsAvailable() {
		String[] projection = { TrainingPlan._ID };
		Cursor trainings = managedQuery(TrainingPlan.CONTENT_URI, projection,
				null, null, null);

		return trainings.moveToNext();

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
			menu.add(1, MENU_UPLOAD, 1, "Upload");
		}
		menu.add(1, MENU_FULL_RATE_TOGGLE, 1, "Rate full");
		menu.add(1, MENU_LOGOUT, 3, "Logout");
		return true;
	};

	// TODO: delete this
	int menuToggle = 1;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SYNC: {
			runTrainingsSync();

			break;
		}
		case MENU_UPLOAD: {
			runUploadTrainings();

			break;
		}
		case MENU_LOGOUT: {
			mSettings.saveUsername("");
			mSettings.savePassword("");

			// TODO: should also delete everything from the local database

			finish();

			break;
		}
		case MENU_FULL_RATE_TOGGLE: {
			if (menuToggle % 3 == 1) {
				// show full screen
				mViewRateExercise.setVisibility(View.VISIBLE);
				mIconWeight.setVisibility(View.VISIBLE);
				mViewRateExercise.setBackgroundColor(getResources().getColor(
						R.color.rate_exercise_background));
			} else if (menuToggle % 3 == 2) {
				mViewRateExercise.setVisibility(View.VISIBLE);
				mIconWeight.setVisibility(View.INVISIBLE);
				mViewRateExercise.setBackgroundColor(0x00000000);
			} else if (menuToggle % 3 == 0) {
				mViewRateExercise.setVisibility(View.GONE);
			}

			menuToggle++;

			break;
		}
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Initiates the upload of completed trainings.
	 */
	private void runUploadTrainings() {
		Intent intent = new Intent(this, ServerCommunicationService.class);
		intent.putExtra(ServerCommunicationService.INTENT_KEY_ACTION,
				ServerCommunicationService.ACTION_UPLOAD_COMPLETED_TRAININGS);
		intent.putExtra(ServerCommunicationService.INTENT_KEY_USERNAME,
				mSettings.getUsername());
		intent.putExtra(ServerCommunicationService.INTENT_KEY_PASSWORD,
				mSettings.getPassword());
		startService(intent);
	}

	/**
	 * Initiates the training sync process.
	 */
	private void runTrainingsSync() {
		Intent intent = new Intent(this, ServerCommunicationService.class);
		intent.putExtra(ServerCommunicationService.INTENT_KEY_ACTION,
				ServerCommunicationService.ACTION_FETCH_TRAININGS);
		intent.putExtra(ServerCommunicationService.INTENT_KEY_USERNAME,
				mSettings.getUsername());
		intent.putExtra(ServerCommunicationService.INTENT_KEY_PASSWORD,
				mSettings.getPassword());
		startService(intent);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMessage("Fetching training list ...");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.show();
	}

	private void changeTrainingPlanState() {
		if (mViewRateExercise.getVisibility() == View.VISIBLE) {
			// exercise has to be rated before doing anything else

		} else if (mCircularProgress.isInfoVisible()) {
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
				// I'm done was clicked
				mCurrentTraining.endTraining();
			} else {
				// overview button was clicked

				// store training to the database
				ContentValues completedTraining = new ContentValues();
				completedTraining.put(CompletedTraining.NAME,
						mCurrentTraining.getTrainingName());
				completedTraining.put(CompletedTraining.DATA,
						mGsonInstance.toJson(mCurrentTraining));

				getContentResolver().insert(CompletedTraining.CONTENT_URI,
						completedTraining);

				mCurrentTraining = null;
				initializeTrainingRatings();
			}

		} else if (mCurrentTraining.isCurrentRest()) {
			// rest -> exrcise
			if (mGetReadyStartTimestamp == -1) {
				// initiate the get ready timer
				mGetReadyStartTimestamp = System.nanoTime();
			} else {
				// or cancel it
				mGetReadyStartTimestamp = -1;

				// stop acceleration sampling
				mAccelerationRecorder.stopAccelerationSampling();
			}
		} else {
			// exercise -> rest

			AccelerationRecordingTimestamps timestamps = mAccelerationRecorder
					.stopAccelerationSampling();
			mCurrentTraining.endExercise(timestamps);

			if (mRepetitionAnimation.isAnimationRunning()) {
				mRepetitionAnimation.cancelAnimation();
			}

			// TODO: user should rate the series on canceled (maybe the series
			// was canceled becuase it was too hard)

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
		if (mCurrentTraining == null) {
			// no training started yet, show the start button
			mCircularProgress.setCurrentState(CircularProgressState.START);

			mInfoButton.setVisibility(View.INVISIBLE);

			mBottomContainer.setVisibility(View.VISIBLE);
			mSeriesInformation.setVisibility(View.INVISIBLE);
			mTrainingSelector.setVisibility(View.VISIBLE);
			mSwipeControl.setVisibility(View.INVISIBLE);
		} else if (mCurrentTraining.getCurrentExercise() == null) {
			if (!mCurrentTraining.isTrainingEnded()) {
				// no more exercises, show the done button
				mCircularProgress.setCurrentState(CircularProgressState.STOP);
				mSeriesInfoText.setText("tap to finish");
				mBottomContainer.setVisibility(View.INVISIBLE);

			} else if (mCurrentTraining.getTrainingRating() == -1) {
				// show training rating screen
				mViewFlipper.showNext();
			} else {
				// show overview

				mCircularProgress.setNumberTotal(mCurrentTraining
						.getTotalTrainingDuration());
				mCircularProgress.setNumberActive(mCurrentTraining
						.getActiveTrainingDuration());

				mCircularProgress
						.setCurrentState(CircularProgressState.OVERVIEW);

				mSeriesInfoText.setText("tap to close");
				mBottomContainer.setVisibility(View.VISIBLE);
				mSwipeControl.setVisibility(View.VISIBLE);
				mTrainingSelector.setVisibility(View.INVISIBLE);
				mSwipeControl.setCenterText("", "GREAT JOB!");
			}

			mInfoButton.setVisibility(View.INVISIBLE);
			mSeriesInformation.setVisibility(View.VISIBLE);
			mSwipeControl.setSwipeEnabled(false);
			mImageArrowSeriesInfo.setVisibility(View.GONE);
		} else {
			// in general, show no timer message
			mCircularProgress.setTimerMessage("");

			Exercise curExercise = mCurrentTraining.getCurrentExercise();
			// there are still some exercises to be performed
			if (mCurrentTraining.isCurrentRest()) {
				mCircularProgress.setInfoChairLevel(mCurrentTraining
						.getCurrentExercise().getMachineSetting());

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
					mCircularProgress.setRestMaxProgress(mGetReadyInterval);
					mCircularProgress.setRestMinProgress(0);
					mCircularProgress.setTimerMessage("Get ready!");

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

			mInfoButton.setVisibility(View.VISIBLE);
			mBottomContainer.setVisibility(View.VISIBLE);
			mSeriesInformation.setVisibility(View.VISIBLE);
			mTrainingSelector.setVisibility(View.INVISIBLE);
			mSwipeControl.setVisibility(View.VISIBLE);
			mSwipeControl.setSwipeEnabled(true);
			mImageArrowSeriesInfo.setVisibility(View.VISIBLE);
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
		// don't do anything if exercise can not be scheduled for later
		if (!mCurrentTraining.canScheduleLater()) {
			return;
		}

		if (!mCurrentTraining.isCurrentRest()) {
			AccelerationRecordingTimestamps timestamps = mAccelerationRecorder
					.stopAccelerationSampling();
			mCurrentTraining.endExercise(timestamps);

			// TODO: user should optionally rate the exercise here (scheduling
			// the exercise for later because it was too hard)
		}

		// disable the get ready timer
		mGetReadyStartTimestamp = -1;

		// cancel the repetition animation if running
		if (mRepetitionAnimation.isAnimationRunning()) {
			mRepetitionAnimation.cancelAnimation();
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
			AccelerationRecordingTimestamps timestamps = mAccelerationRecorder
					.stopAccelerationSampling();
			mCurrentTraining.endExercise(timestamps);
		}

		// disable the get ready timer
		mGetReadyStartTimestamp = -1;

		// cancel the repetition animation if running
		if (mRepetitionAnimation.isAnimationRunning()) {
			mRepetitionAnimation.cancelAnimation();
		}

		// TODO: user should optionally rate the exercise here (e.g. skipping in
		// the middle of the series because it was maybe to hard)

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
		mViewRateExercise.setVisibility(View.VISIBLE);
		mIconWeight.setVisibility(View.INVISIBLE);
		mViewRateExercise.setBackgroundColor(0x00000000);
		// changeTrainingPlanState();
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
			int secLeft = Math.round(mGetReadyInterval
					- (float) (System.nanoTime() - mGetReadyStartTimestamp)
					/ 1000000000);

			if (secLeft > 0) {
				mCircularProgress.setRestProgressValue(secLeft);
				mCircularProgress.setTimer(secLeft);

				mUiHandler.postDelayed(this, REST_PROGRESS_UPDATE_RATE);
			} else {
				// mark the the timer is over
				mGetReadyStartTimestamp = -1;

				// time is up, start the exercise animation
				mCurrentTraining.startExercise();

				// TODO: support duration timer as well
				Exercise curExercise = mCurrentTraining.getCurrentExercise();
				if (curExercise.getGuidanceType().equals(Exercise.GUIDANCE_TYPE_TEMPO)) {
					mRepetitionAnimation.startAnimation(
							mViewFlipper.getMeasuredHeight(),
							(int)(curExercise.getRepetitionDurationUp() * 1000),
							(int)(curExercise.getRepetitionDurationDown() * 1000),
							(int)(curExercise.getRepetitionDurationMiddle() * 1000),
							(int)(curExercise.getRepetitionDurationAfter() * 1000),
							mCurrentTraining.getTotalRepetitions());
				} else {
					mViewRateExercise.setVisibility(View.VISIBLE);
					mIconWeight.setVisibility(View.VISIBLE);
					mViewRateExercise.setBackgroundColor(getResources()
							.getColor(R.color.rate_exercise_background));
				}

				// do acceleration sampling
				try {
					mAccelerationRecorder
							.startAccelerationSampling(mCurrentTraining
									.getTrainingStartTimestamp(), mCurrentTraining.getRawFile());
				} catch (IOException e) {
					Log.e(TAG, "Unable to start acceleration sampling: " + e.getMessage());
				}

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
					ServerCommunicationService.ACTION_FETCH_TRAINNGS_COMPLETED)) {
				// on finished fetching trainings

				mProgressDialog.dismiss();

				boolean getTrainingSuccessful = intent.getExtras().getBoolean(
						ServerCommunicationService.PARAM_LOGIN_SUCCESSFUL);

				if (getTrainingSuccessful) {
					updateTrainingSelector(-1);
				}
			} else if (intent
					.getAction()
					.equals(ServerCommunicationService.ACTION_GET_TRAINNGS_LIST_COMPLETED)) {
				// on list with training names fetched

				// calculate progress bar information and set progress
				int totalNumberOfTrainings = intent
						.getExtras()
						.getInt(ServerCommunicationService.PARAM_FETCH_TRAINNGS_NUM_ALL_ITEMS);
				int progress = Math.round(1f / totalNumberOfTrainings * 100f);
				mProgressDialog.setProgress(progress);
			} else if (intent
					.getAction()
					.equals(ServerCommunicationService.ACTION_FETCH_TRAINNG_ITEM_COMPLETED)) {
				// on individual training item downloaded

				// calculate progress bar information and set progress with
				// training name
				int totalNumberOfTrainings = intent
						.getExtras()
						.getInt(ServerCommunicationService.PARAM_FETCH_TRAINNGS_NUM_ALL_ITEMS);
				int trainingCount = intent
						.getExtras()
						.getInt(ServerCommunicationService.PARAM_FETCH_TRAINNGS_CUR_ITEM_CNT);
				String trainingName = intent
						.getExtras()
						.getString(
								ServerCommunicationService.PARAM_FETCH_TRAINNGS_CUR_ITEM_NAME);
				int progress = Math.round((1f + trainingCount)
						/ totalNumberOfTrainings * 100f);
				mProgressDialog.setMessage("Fetching " + trainingName);
				mProgressDialog.setProgress(progress);
			} else if (intent.getAction().equals(
					ServerCommunicationService.ACTION_UPLOAD_TRAININGS_STARTED)) {
				// started upload the trainings

				mProgressDialog = new ProgressDialog(TrainingActivity.this);
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setMessage("Uploading completed trainings ...");
				mProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.show();

				// calculate progress bar information and set progress
				int totalNumberOfTrainings = intent
						.getExtras()
						.getInt(ServerCommunicationService.PARAM_UPLOAD_TRAINING_NUM_ALL_ITEMS);
				int progress = Math.round(1f / totalNumberOfTrainings * 100f);
				mProgressDialog.setProgress(progress);
			} else if (intent.getAction().equals(
					ServerCommunicationService.ACTION_TRAININGS_ITEM_UPLOADED)) {
				// on individual completed training

				// calculate progress bar information and set progress with
				// training name
				int totalNumberOfTrainings = intent
						.getExtras()
						.getInt(ServerCommunicationService.PARAM_UPLOAD_TRAINING_NUM_ALL_ITEMS);
				int trainingCount = intent
						.getExtras()
						.getInt(ServerCommunicationService.PARAM_UPLOAD_TRAINING_CUR_ITEM_CNT);
				String trainingName = intent
						.getExtras()
						.getString(
								ServerCommunicationService.PARAM_UPLOAD_TRAINING_ITEM_NAME);
				boolean uploadStatus = intent
						.getExtras()
						.getBoolean(
								ServerCommunicationService.PARAM_UPLOAD_TRAINING_ITEM_STATUS);
				int progress = Math.round((1f + trainingCount)
						/ totalNumberOfTrainings * 100f);
				mProgressDialog.setMessage("Uploading " + trainingName + " "
						+ (uploadStatus ? "OK" : "FAILED"));
				mProgressDialog.setProgress(progress);
			} else if (intent
					.getAction()
					.equals(ServerCommunicationService.ACTION_UPLOAD_TRAINNGS_COMPLETED)) {
				// after the all the completed trainings were uploaded (or at
				// least attempted to upload)

				int successfulItems = intent
						.getExtras()
						.getInt(ServerCommunicationService.PARAM_UPLOAD_TRAINING_SUCESS_CNT);
				int totalNumberOfTrainings = intent
						.getExtras()
						.getInt(ServerCommunicationService.PARAM_UPLOAD_TRAINING_NUM_ALL_ITEMS);

				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}

				Toast.makeText(
						getApplicationContext(),
						String.format("Uploaded %d of %d items.",
								successfulItems, totalNumberOfTrainings),
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
