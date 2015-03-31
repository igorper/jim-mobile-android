package com.trainerjim.android;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.trainerjim.android.entities.SeriesExecution;
import com.trainerjim.android.entities.Training;
import com.trainerjim.android.network.ServerCommunicationService;
import com.trainerjim.android.storage.PermanentSettings;
import com.trainerjim.android.storage.TrainingContentProvider.CompletedTraining;
import com.trainerjim.android.storage.TrainingContentProvider.TrainingPlan;
import com.trainerjim.android.ui.CircularProgressControl;
import com.trainerjim.android.ui.RepetitionAnimation;
import com.trainerjim.android.ui.RepetitionAnimationListener;
import com.trainerjim.android.ui.CircularProgressControl.CircularProgressState;
import com.trainerjim.android.util.Utils;

public class TrainingActivity extends Activity implements RepetitionAnimationListener {

	private static final String TAG = Utils.getApplicationTag();

	private final static int MENU_SYNC = Menu.FIRST;
	private final static int MENU_UPLOAD = Menu.FIRST + 1;
    private final static int MENU_LOGOUT = Menu.FIRST + 2;
    private final static int MENU_CANCEL = Menu.FIRST + 3;

	private static final int ACTIVITY_REQUEST_TRAININGS_LIST = 0;

	/**
	 * In ms.
	 */
	private static final int REST_PROGRESS_UPDATE_RATE = 300;

    /**
     * Default series rating used when the user quickly finishes the series (doesn't
     * edit series details information). Additionally as a default selection in
     * edit series details view.
     *
     * Note: Numbers correspond to TRAINING_RATING_IMAGES and TRAINING_RATING_SELECTED_IMAGES
     * arrays.
     */
    private static final int DEFAULT_SERIES_RATING = 1;

	private PermanentSettings mSettings;
	private ResponseReceiver mBroadcastReceiver;

	/**
	 * References to the XML defined UI controls.
	 */
	private CircularProgressControl mCircularProgress;
	private ViewFlipper mViewFlipper;
	private LinearLayout mTrainingSelector;
    private LinearLayout mLayoutRectTrainingSelector;
    private LinearLayout mLayoutRectLowerLine;
	private ProgressDialog mProgressDialog;
	private TextView mTrainingSelectorText;
    private TextView mTextRectUpperLine;
    private TextView mTextRectLowerLine;
    private TextView mTextRectOneLine;
	private RelativeLayout mBottomContainer;
	private ImageView mInfoButton;
    private ImageView mSkipButton;
    private ImageView mNextButton;
    private ImageView mPrevButton;
	private LinearLayout mSeriesInformation;
	private TextView mSeriesInfoText;
	private TextView mTrainingCommentText;
	private LinearLayout mAnimationRectangle;
	private ImageView mImageArrowSeriesInfo;
	private LinearLayout mViewDuringExercise;
	private CheckBox mEditDetailsCheckbox;
	private RelativeLayout mEditDetailsView;
	private EditText mEditRepetitionsValue;
	private EditText mEditWeightValue;
	private EditText mEditCommentValue;
    private ImageView mExerciseImage;

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
     * Holds the ID of the currently selected exercise rating (or -1 if no
     * rating was yet selected).
     */
    private int mExerciseRatingSelectedID = -1;

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
     * Controls the visibility of the edit series details view. If true, edit
     * series view will be shown to the users.
     */
    private Boolean mEditSeriesDetails = false;

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
		mTrainingSelector = (LinearLayout) findViewById(R.id.trainingSelector);
        mLayoutRectTrainingSelector = (LinearLayout) findViewById(R.id.layout_rect_training_selector);
        mLayoutRectLowerLine = (LinearLayout)findViewById(R.id.layout_rect_lower_line);
		mTrainingSelectorText = (TextView) findViewById(R.id.trainingSelectorText);
        mTextRectUpperLine = (TextView)findViewById(R.id.text_rect_upper_line);
        mTextRectLowerLine = (TextView)findViewById(R.id.text_rect_lower_line);
        mTextRectOneLine = (TextView)findViewById(R.id.text_rect_one_line);
		mBottomContainer = (RelativeLayout) findViewById(R.id.bottomContainer);
		mInfoButton = (ImageView) findViewById(R.id.info_button);
        mSkipButton = (ImageView) findViewById(R.id.skip_button);
        mNextButton = (ImageView) findViewById(R.id.next_button);
        mPrevButton = (ImageView) findViewById(R.id.prev_button);
		mSeriesInformation = (LinearLayout) findViewById(R.id.seriesInformation);
		mSeriesInfoText = (TextView) findViewById(R.id.nextSeriesText);
		mTrainingCommentText = (TextView) findViewById(R.id.textTrainingComment);
		mAnimationRectangle = (LinearLayout) findViewById(R.id.animationRectangle);
		mImageArrowSeriesInfo = (ImageView) findViewById(R.id.imageArrowSeriesInfo);
		mViewDuringExercise = (LinearLayout) findViewById(R.id.viewDuringExercise);
//		mEditDetailsCheckbox = (CheckBox) findViewById(R.id.checkbox_edit_details);
		mEditDetailsView = (RelativeLayout)findViewById(R.id.editDetailsView);
		mEditRepetitionsValue = (EditText)findViewById(R.id.editRepetitionsValue);
		mEditWeightValue  =(EditText)findViewById(R.id.editWeightValue);
		mEditCommentValue  =(EditText)findViewById(R.id.editCommentValue);
        mExerciseImage = (ImageView)findViewById(R.id.exerciseImage);

		updateTrainingSelector(-1);
		initializeTrainingRatings();
		initializeExerciseRatings();
		loadCurrentTraining();
		updateScreen();

		mEditDetailsView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// only used to handle all the clicks while the edit details
				// view is visible
				return true;
			}
		});

        // add a long click action to skip exercise button (so the user won't
        // activate this by mistake)
        mSkipButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                skipExercise();
                return true;
            }
        });

        // add a long click action to previous exercise button
        mPrevButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mCurrentTraining.moveToPreviousExercise();
                saveCurrentTraining();
                toggleInfoButtonVisible(false);
                updateScreen();
                return true;
            }
        });


        // add a long click action to next exercise button
        mNextButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // don't do anything if exercise can not be scheduled for later
                if (!mCurrentTraining.canScheduleLater()) {
                    return false;
                }

                if (!mCurrentTraining.isCurrentRest()) {

                    AccelerationRecordingTimestamps timestamps = getResources().getBoolean(R.bool.sample_acceleration) ? mAccelerationRecorder
                            .stopAccelerationSampling() : null;
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

                return false;
            }
        });

        // add a long click action to the main exercise button
		mCircularProgress.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                changeTrainingPlanState();
                return true;
            }
        });

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

    public boolean onClickExerciseImage(View v){
        toggleInfoButtonVisible(false);
        return true;
    }

	/**
	 * Invoked when a specific exercise rating image is clicked.
	 * 
	 * @param v contains the reference to the clicked exercise rating image.
	 */
	public void onExerciseRatingSelected(View v) {
		ImageView exerciseRatingImage = (ImageView) v;

        selectSeriesRatingIcon(exerciseRatingImage);
	}

    /**
     * Manages series rating image views (smile icons).
     * Selects the input imageView and deselect all the other rating image views.
     * @param ratingImageSelected is the selected imageView
     */
    private void selectSeriesRatingIcon(ImageView ratingImageSelected){
        // different padding as we make the selected image a bit larger
        int imageSelectedPadding = getResources().getDimensionPixelSize(
                R.dimen.training_rating_smile_selected_padding);
        int imagePadding = getResources().getDimensionPixelSize(
                R.dimen.training_rating_smile_padding);

        // loop through training ratings image views and set the appropriate
        // image
        for (int i = 0; i < mExerciseRatingImages.length; i++) {
            if (mExerciseRatingImages[i] == ratingImageSelected) {
                mExerciseRatingImages[i]
                        .setImageResource(TRAINING_RATING_SELECTED_IMAGES[i]);
                mExerciseRatingImages[i].setPadding(imageSelectedPadding,
                        imageSelectedPadding, imageSelectedPadding,
                        imageSelectedPadding);
                // set global information about the selected rating (this will also be used
                // to store the rating to the corresponding SeriesExecution)
                mExerciseRatingSelectedID = i;
            } else {
                mExerciseRatingImages[i]
                        .setImageResource(TRAINING_RATING_IMAGES[i]);
                mExerciseRatingImages[i].setPadding(imagePadding, imagePadding,
                        imagePadding, imagePadding);
            }
        }
    }

    /**
     * Finish series and set the input rating.
     */
    private void finishSeries(){
        // store the default series rating (for quick entry). In case of edit series details
        // the rating can be changed later on (in edit series details view).
        mCurrentTraining.setCurrentSeriesExecutionRating(DEFAULT_SERIES_RATING);

        // hide the screen that is shown during exercising
        mViewDuringExercise.setVisibility(View.GONE);

        // stop acceleration sampling and get acceleration timestamps only if acceleration sampling is enabled
        AccelerationRecordingTimestamps timestamps = getResources().getBoolean(R.bool.sample_acceleration) ? mAccelerationRecorder
                .stopAccelerationSampling() : null;

        // end this exercise (series)
        mCurrentTraining.endExercise(timestamps);

        // advance to the next activity
        mCurrentTraining.nextActivity();

        saveCurrentTraining();

        updateScreen();
    }

    /**
     * Invoked when the user is done with a series.
     *
     * @param v
     *            contains the reference to the clicked imageview
     */
    public void onSeriesDoneClick(View v) {
        ImageView trainingRatingSelected = (ImageView) v;

        finishSeries();
    }

    /**
     * Invoked when the user wants to edit series details.
     *
     * @param v
     *            contains the reference to the clicked imageview
     */
    public void onSeriesDetailsClick(View v) {
        // select default rating icon on screen
        selectSeriesRatingIcon(mExerciseRatingImages[DEFAULT_SERIES_RATING]);

        // we will want to show the view for editing this series
        mEditSeriesDetails = true;

        // finish series with the default series rating
        finishSeries();
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
            mTrainingSelector.setVisibility(View.VISIBLE);
            mTextRectOneLine.setVisibility(View.GONE);
		} else {
            mTrainingSelector.setVisibility(View.GONE);
            mTextRectOneLine.setText("NO TRAININGS.");
            mTextRectOneLine.setVisibility(View.VISIBLE);
        }
	}

	/**
	 * Toggles the visibility of additional info circular button overlay and
	 * sets the appropriate icon.
	 * 
	 * @param visible
	 */
	private void toggleInfoButtonVisible(boolean visible) {
		// This code was used before showing the full screen image (to show the exercise info
        // circle view).
		/*
		mCircularProgress.setInfoVisible(visible);

		mInfoButton.setImageResource(visible ? R.drawable.chair_ico_selected
				: R.drawable.chair_ico);
				*/

        mExerciseImage.setVisibility(visible ? View.VISIBLE : View.GONE);
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
     * Triggered on skip button click.
     * @param v
     */
    public void onSkipButtonClick(View v){
        // TODO: Show help popup
        /*
        Help popup could be show inside the circular button (in another color). It
        should just outline the action results and explain that the action can be used
        by long click.
        The same help popup could be used for other cases.
         */
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
		if (mCurrentTraining == null && !mSettings.getUsername().equals("")) {
			menu.add(1, MENU_SYNC, 1, "Sync");
			menu.add(1, MENU_UPLOAD, 1, "Upload");
		}

        if(mCurrentTraining != null){
            menu.add(1, MENU_CANCEL, 1, "Cancel");
        }

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
        case MENU_CANCEL:{
            mUiHandler.removeCallbacks(mUpdateRestTimer);
            mUiHandler.removeCallbacks(mGetReadyTimer);

            mCurrentTraining = null;

            saveCurrentTraining();
            updateScreen();
            break;
        }
		case MENU_LOGOUT: {
			mSettings.saveUsername("");
			mSettings.savePassword("");

			// TODO: should also delete everything from the local database

			finish();

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
		if (mViewDuringExercise.getVisibility() == View.VISIBLE) {
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
				mGetReadyStartTimestamp = System.currentTimeMillis();
			} else {
				// or cancel it
				mGetReadyStartTimestamp = -1;
			}
		} else {
			// exercise -> rest

			if (mRepetitionAnimation.isAnimationRunning()) {
				mRepetitionAnimation.cancelAnimation();
				showExerciseRateView();
			}
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
        // those should never be visible on updated screen (can be visible only on a special action)
        mSkipButton.setVisibility(View.INVISIBLE);
        mNextButton.setVisibility(View.INVISIBLE);
        mPrevButton.setVisibility(View.INVISIBLE);

		if (mCurrentTraining == null) {
			// no training started yet, show the start button
			mCircularProgress.setCurrentState(CircularProgressState.START);

            mTextRectUpperLine.setText("Workout selected:");
            mTextRectUpperLine.setVisibility(View.VISIBLE);

			mInfoButton.setVisibility(View.INVISIBLE);

			mBottomContainer.setVisibility(View.VISIBLE);
			mSeriesInformation.setVisibility(View.INVISIBLE);
			mTrainingSelector.setVisibility(View.VISIBLE);
            mTextRectOneLine.setVisibility(View.INVISIBLE);
            mLayoutRectTrainingSelector.setVisibility(View.VISIBLE);
            mLayoutRectLowerLine.setVisibility(View.GONE);
		} else if (mCurrentTraining.getCurrentExercise() == null) {
			if (!mCurrentTraining.isTrainingEnded()) {
                // no more exercises, show the done button
				showEditDetailsViewIfDemanded();

				mCircularProgress.setCurrentState(CircularProgressState.STOP);
				mSeriesInfoText.setText("tap to finish");
				mBottomContainer.setVisibility(View.INVISIBLE);

			} else if (mCurrentTraining.getTrainingRating() == -1) {
				// show training rating screen
				mViewFlipper.showNext();
			} else {
				// show overview
                mTextRectOneLine.setText("ALL DONE");

				mCircularProgress.setNumberTotal(mCurrentTraining
						.getTotalTrainingDuration());
				mCircularProgress.setNumberActive(mCurrentTraining
						.getActiveTrainingDuration());

				mCircularProgress
						.setCurrentState(CircularProgressState.OVERVIEW);

				mSeriesInfoText.setText("tap to close");
				mBottomContainer.setVisibility(View.VISIBLE);
				// TODO: mSwipeControl.setVisibility(View.VISIBLE);
				mTrainingSelector.setVisibility(View.INVISIBLE);
                mTextRectOneLine.setVisibility(View.VISIBLE);
			}

			mInfoButton.setVisibility(View.INVISIBLE);
			mSeriesInformation.setVisibility(View.VISIBLE);
			mImageArrowSeriesInfo.setVisibility(View.GONE);
		} else {
			// in general, show no timer message
			mCircularProgress.setTimerMessage("");

			Exercise curExercise = mCurrentTraining.getCurrentExercise();
			// there are still some exercises to be performed
			if (mCurrentTraining.isCurrentRest()) {
                try {

                    // TODO: encapsulate this or find a better way to do it

                    Matrix matrix = new Matrix();

                    // we presume that all images will be in landscape mode and thus rotate it.
                    // A more intelligent algorithm could be implemented in the future - e.g.
                    // checking the dimensions of the image
                    matrix.postRotate(90);

                    Bitmap scaledBitmap = BitmapFactory.decodeFile(new File(Utils.getDataFolderFile(),
                            Integer.toString(curExercise.getExerciseType().getId())).getAbsolutePath());

                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);

                    mExerciseImage.setImageBitmap(rotatedBitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to set current exercise image: " + e.getMessage());
                }

                mCircularProgress.setInfoChairLevel(mCurrentTraining
						.getCurrentExercise().getMachineSetting());

				showEditDetailsViewIfDemanded();

				// first remove all existing callbacks
				mUiHandler.removeCallbacks(mUpdateRestTimer);
				mUiHandler.removeCallbacks(mGetReadyTimer);

				// now show all the common information
				Series curSeries = curExercise.getCurrentSeries();
				mCircularProgress.setCurrentState(CircularProgressState.REST);

                mTextRectUpperLine.setText("- " + Integer.toString(curExercise.getOrder() + 1) + " -");

                // show short name if available
                String exerciseName = curExercise.getExerciseType().getShortName() != null ? curExercise
                        .getExerciseType().getShortName() : curExercise.getExerciseType().getName();

                // visualize exercise order (useful when traversing exercises)
                mTextRectLowerLine.setText(exerciseName);
                mLayoutRectLowerLine.setVisibility(View.VISIBLE);
                mLayoutRectTrainingSelector.setVisibility(View.GONE);
				mSeriesInfoText.setText(String.format(
						"Series %d (%d reps, %d kg)",
						curExercise.getCurrentSeriesNumber(),
						curSeries.getNumberTotalRepetitions(),
						curSeries.getWeight()));
				mSeriesInformation.setVisibility(View.VISIBLE);

                mCircularProgress.setTimerMessage("RESTING");

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
					mCircularProgress.setTimerMessage("GET READY");

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
			mTrainingSelector.setVisibility(View.VISIBLE);
			// TODO: mSwipeControl.setVisibility(View.VISIBLE);
			mImageArrowSeriesInfo.setVisibility(View.VISIBLE);
		}

        invalidateOptionsMenu();
	}

	private void showEditDetailsViewIfDemanded() {
		SeriesExecution lastSe = mCurrentTraining.getLastSeriesExecution();
		if (mEditSeriesDetails && lastSe != null) {
			mEditRepetitionsValue.setText(Integer.toString(lastSe.getRepetitions()));
			mEditWeightValue.setText(Integer.toString(lastSe.getWeight()));

			mEditDetailsView.setVisibility(View.VISIBLE);
            mEditSeriesDetails = false;
		}
	}
	
	/** Save the (edited) series execution values and hides the edit details view.
	 * @param v
	 */
	public void onEditDetailsDoneClick(View v){
		// this is possible (however not very secure) as we are not creating a
		// defensive copy of the series execution in training
		SeriesExecution lastSe = mCurrentTraining.getLastSeriesExecution();
		if(lastSe != null){
            lastSe.setRating(mExerciseRatingSelectedID);
			lastSe.setWeight(Integer.parseInt(mEditWeightValue.getText().toString()));
			lastSe.setRepetitions(Integer.parseInt(mEditRepetitionsValue.getText().toString()));
		}

		mEditDetailsView.setVisibility(View.GONE);
	}

	/**
	 * Starts the select training activity.
	 * 
	 * @param view
	 */
	public void onBottomStrapClick(View view) {
        // if resting toggle the next/prev buttons and the current exercise info
        if(mCircularProgress.getCurrentState() == CircularProgressState.REST){
            int visibilityControls = mPrevButton.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE;

            mPrevButton.setVisibility(visibilityControls);
            mNextButton.setVisibility(visibilityControls);
            mSkipButton.setVisibility(visibilityControls);
        } else {

            Intent intent = new Intent(TrainingActivity.this,
                    TrainingSelectionList.class);
            startActivityForResult(intent, ACTIVITY_REQUEST_TRAININGS_LIST);
        }
	}

    /**
     * Skips the current exercise. All planned executions of this exercise are removed
     * from the training plan.
     */
    private void skipExercise(){
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
	 * @see com.trainerjim.android.ui.RepetitionAnimationListener#
	 * onAnimationEnded()
	 */
	@Override
	public void onAnimationEnded() {
		showExerciseRateView();
	}

	private void showExerciseRateView() {
		mViewDuringExercise.setVisibility(View.VISIBLE);
	}

	/*
	 * Triggered after each individual repetition is executed.
	 *
	 * @see com.trainerjim.android.ui.RepetitionAnimationListener#
	 * onRepetitionCompleted()
	 */
	@Override
	public void onRepetitionCompleted() {
		mCurrentTraining.increaseCurrentRepetition();
		updateScreen();
	}

	private Runnable mGetReadyTimer = new Runnable() {

		@Override
		public void run() {
			int secLeft = Math.round(mGetReadyInterval
					- (float) (System.currentTimeMillis() - mGetReadyStartTimestamp)
					/ 1000);

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
				if (curExercise.getGuidanceType().equals(
						Exercise.GUIDANCE_TYPE_TEMPO)) {
					mRepetitionAnimation
							.startAnimation(
									mViewFlipper.getMeasuredHeight(),
									(int) (curExercise
											.getRepetitionDurationUp() * 1000),
									(int) (curExercise
											.getRepetitionDurationDown() * 1000),
									(int) (curExercise
											.getRepetitionDurationMiddle() * 1000),
									(int) (curExercise
											.getRepetitionDurationAfter() * 1000),
									mCurrentTraining.getTotalRepetitions());
				} else {
					showExerciseRateView();
				}

                if(getResources().getBoolean(R.bool.sample_acceleration)) {
                    // do acceleration sampling
                    try {
                        mAccelerationRecorder.startAccelerationSampling(
                                mCurrentTraining.getTrainingStartTimestamp(),
                                mCurrentTraining.getRawFile());
                    } catch (IOException e) {
                        Log.e(TAG,
                                "Unable to start acceleration sampling: "
                                        + e.getMessage());
                    }
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
						ServerCommunicationService.PARAM_ACTION_SUCCESSFUL);

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
