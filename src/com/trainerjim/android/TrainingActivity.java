package com.trainerjim.android;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.trainerjim.android.entities.Exercise;
import com.trainerjim.android.entities.Series;
import com.trainerjim.android.entities.Training;
import com.trainerjim.android.events.EndExerciseEvent;
import com.trainerjim.android.events.EndRateTraining;
import com.trainerjim.android.events.StartRateTraining;
import com.trainerjim.android.events.StartExerciseEvent;
import com.trainerjim.android.network.ServerCommunicationService;
import com.trainerjim.android.storage.PermanentSettings;
import com.trainerjim.android.storage.TrainingContentProvider.CompletedTraining;
import com.trainerjim.android.storage.TrainingContentProvider.TrainingPlan;
import com.trainerjim.android.ui.CircularProgressControl;
import com.trainerjim.android.ui.ExerciseAdapter;
import com.trainerjim.android.ui.RepetitionAnimation;
import com.trainerjim.android.ui.RepetitionAnimationListener;
import com.trainerjim.android.ui.CircularProgressControl.CircularProgressState;
import com.trainerjim.android.util.Utils;

import de.greenrobot.event.EventBus;

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

	private PermanentSettings mSettings;
	private ResponseReceiver mBroadcastReceiver;

	/**
	 * References to the XML defined UI controls.
	 */
	private CircularProgressControl mCircularProgress;
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
	private LinearLayout mSeriesInformation;
	private TextView mSeriesInfoText;
	private LinearLayout mAnimationRectangle;
	private ImageView mImageArrowSeriesInfo;
	private CheckBox mEditDetailsCheckbox;
    private ImageView mExerciseImage;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerExercisesList;

	private AccelerationRecorder mAccelerationRecorder;

	/**
	 * Reference to the repetition animation.
	 */
	private RepetitionAnimation mRepetitionAnimation;

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
     * Holds the last value of the running exercise timer. This field is used
     * when playing a notification during an exercise timer.
     */
    private long mLastExerciseTimerValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

        EventBus.getDefault().register(this);


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
		mTrainingSelector = (LinearLayout) findViewById(R.id.trainingSelector);
        mLayoutRectTrainingSelector = (LinearLayout) findViewById(R.id.layout_rect_training_selector);
        mLayoutRectLowerLine = (LinearLayout)findViewById(R.id.layout_rect_lower_line);
		mTrainingSelectorText = (TextView) findViewById(R.id.trainingSelectorText);
        mTextRectUpperLine = (TextView)findViewById(R.id.text_rect_upper_line);
        mTextRectLowerLine = (TextView)findViewById(R.id.text_rect_lower_line);
        mTextRectOneLine = (TextView)findViewById(R.id.text_rect_one_line);
		mBottomContainer = (RelativeLayout) findViewById(R.id.bottomContainer);
		mInfoButton = (ImageView) findViewById(R.id.info_button);
		mSeriesInformation = (LinearLayout) findViewById(R.id.seriesInformation);
		mSeriesInfoText = (TextView) findViewById(R.id.nextSeriesText);
		mAnimationRectangle = (LinearLayout) findViewById(R.id.animationRectangle);
		mImageArrowSeriesInfo = (ImageView) findViewById(R.id.imageArrowSeriesInfo);
//		mEditDetailsCheckbox = (CheckBox) findViewById(R.id.checkbox_edit_details);
        mExerciseImage = (ImageView)findViewById(R.id.exerciseImage);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerExercisesList = (ListView)findViewById(R.id.exercises_list);

		updateTrainingSelector(-1);
		loadCurrentTraining();
		updateScreen();

        // add a long click action to the main exercise button
		mCircularProgress.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                changeTrainingPlanState();
                return true;
            }
        });

        // set action when user click the exercise in the exercises menu
        mDrawerExercisesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // move to a particular exercise in the training plan
                mCurrentTraining.selectExercise(i);
                mDrawerLayout.closeDrawers();
                updateScreen();
            }
        });

        registerForContextMenu(mDrawerExercisesList);

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
		filter.addAction(ServerCommunicationService.ACTION_REPORT_PROGRESS);
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


    public boolean onClickExerciseImage(View v){
        toggleInfoButtonVisible(false);
        return true;
    }

	/*
	 * TODO: Check if stuff in onCreate and onDestroy should be moved to more
	 * appropriate lifecycle methods.
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        unregisterReceiver(mBroadcastReceiver);
		mRepetitionAnimation.removeRepetitionAnimationListener(this);

		// remove all periodical tasks
		mUiHandler.removeCallbacks(mUpdateRestTimer);

		super.onDestroy();
	}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.exercise_list_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info  = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch (item.getItemId()){
            case R.id.skip_exercise:{
                mCurrentTraining.removeExercise(info.position);
                saveCurrentTraining();
                updateScreen();
                mDrawerLayout.closeDrawers();
                break;
            }
        }

        return super.onContextItemSelected(item);
    }

	/**
	 * This method loads the currently active training to a memory object from
	 * json string saved in application settings. If the json string is
	 * {@code null} the memory object gets set to {@code null} as well.
	 */
	private void loadCurrentTraining() {
		if (!mSettings.getCurrentTrainingPlan().equals("")) {
			mCurrentTraining = Utils.getGsonObject().fromJson(
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
				: Utils.getGsonObject().toJson(mCurrentTraining));
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

        // show the image view only if there is an image available
        if(mExerciseImage.getDrawable() != null) {
            mExerciseImage.setVisibility(visible ? View.VISIBLE : View.GONE);
            mDrawerLayout.setDrawerLockMode(visible ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
        }
	}

    /**
     * This event is called when a user ends performing a particular exercise.
     * @param event
     */
    public void onEvent(EndExerciseEvent event){
        // stop acceleration sampling and get acceleration timestamps only if acceleration sampling is enabled
        AccelerationRecorder.AccelerationRecordingTimestamps timestamps = getResources().getBoolean(R.bool.sample_acceleration) ? mAccelerationRecorder
                .stopAccelerationSampling() : null;


        // end this exercise (series)
        mCurrentTraining.endExercise(timestamps);

        // advance to the next activity
        mCurrentTraining.nextActivity();

        // TODO: this will most probably have to be communicated outside to the parent activity
        saveCurrentTraining();

        updateScreen();
    }

    public void onEvent(EndRateTraining event){
        saveCurrentTraining();
        updateScreen();
    }

	/**
	 * Triggered on additional info button click.
	 * 
	 * @param v
	 */
	public void onInfoButtonClick(View v) {
		toggleInfoButtonVisible(!mCircularProgress.isInfoVisible());
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
		/*if (mViewDuringExercise.getVisibility() == View.VISIBLE) {
			// exercise has to be rated before doing anything else

		} else */if (mCircularProgress.isInfoVisible()) {
			// if info button is visible close it on tap
			toggleInfoButtonVisible(false);
		} else if (mCurrentTraining == null) {
			// start button was clicked
            Cursor trainings = getAvailableTrainings();

			if (trainings.moveToNext()) {
				String jsonEncodedTraining = trainings.getString(trainings
						.getColumnIndex(TrainingPlan.DATA));

				// load to memory
				mCurrentTraining = Utils.getGsonObject().fromJson(jsonEncodedTraining,
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
						Utils.getGsonObject().toJson(mCurrentTraining));

				getContentResolver().insert(CompletedTraining.CONTENT_URI,
						completedTraining);

				mCurrentTraining = null;
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
     * This method queries the database to return a cursor with available trainings.
     * @return
     */
    private Cursor getAvailableTrainings() {
        String[] projection = { TrainingPlan._ID, TrainingPlan.DATA };
        String selection = String.format("%s == %d", TrainingPlan._ID,
                mSelectedTrainingId);
        return managedQuery(TrainingPlan.CONTENT_URI,
                projection, selection, null, null);
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

            if(getAvailableTrainings().getCount() > 0){
                mTextRectUpperLine.setText("Workout selected:");
                mTextRectUpperLine.setVisibility(View.VISIBLE);
                mTrainingSelector.setVisibility(View.VISIBLE);
                mTextRectOneLine.setVisibility(View.INVISIBLE);
                mLayoutRectTrainingSelector.setVisibility(View.VISIBLE);
                mLayoutRectLowerLine.setVisibility(View.GONE);

            } else {
                mTrainingSelector.setVisibility(View.GONE);
                mTextRectOneLine.setText("NO TRAININGS");
                mTextRectOneLine.setVisibility(View.VISIBLE);
            }


            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			mInfoButton.setVisibility(View.INVISIBLE);

			mBottomContainer.setVisibility(View.VISIBLE);
			mSeriesInformation.setVisibility(View.INVISIBLE);

		} else if (mCurrentTraining.getCurrentExercise() == null) {
			if (!mCurrentTraining.isTrainingEnded()) {
                // no more exercises, show the done button
				mCircularProgress.setCurrentState(CircularProgressState.STOP);
				mSeriesInfoText.setText("hold to finish");
				mBottomContainer.setVisibility(View.INVISIBLE);

			} else if (mCurrentTraining.getTrainingRating() == -1) {
				// show training rating screen
                // TODO: mightr be better to decouple the training here (and not pass it,
                // but rather pass a minimum set of parameters)
                EventBus.getDefault().post(new StartRateTraining(mCurrentTraining, getApplicationContext()));
			} else {
				// show overview
                mTextRectOneLine.setText("ALL DONE");

				mCircularProgress.setNumberTotal(mCurrentTraining
						.getTotalTrainingDuration());
				mCircularProgress.setNumberActive(mCurrentTraining
						.getActiveTrainingDuration());

				mCircularProgress
						.setCurrentState(CircularProgressState.OVERVIEW);

				mSeriesInfoText.setText("hold to close");
				mBottomContainer.setVisibility(View.VISIBLE);
				// TODO: mSwipeControl.setVisibility(View.VISIBLE);
				mTrainingSelector.setVisibility(View.INVISIBLE);
                mTextRectOneLine.setVisibility(View.VISIBLE);
			}

            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			mInfoButton.setVisibility(View.INVISIBLE);
			mSeriesInformation.setVisibility(View.VISIBLE);
			mImageArrowSeriesInfo.setVisibility(View.GONE);
		} else {
			// in general, show no timer message
			mCircularProgress.setTimerMessage("");

			Exercise curExercise = mCurrentTraining.getCurrentExercise();
			// there are still some exercises to be performed
			if (mCurrentTraining.isCurrentRest()) {
                // TODO: encapsulate this or find a better way to do it

                Matrix matrix = new Matrix();

                // we presume that all images will be in landscape mode and thus rotate it.
                // A more intelligent algorithm could be implemented in the future - e.g.
                // checking the dimensions of the image
                matrix.postRotate(90);

                String localImageFileName = curExercise.getExerciseType().getLocalImageFileName();

                if(localImageFileName != null) {
                    Bitmap scaledBitmap = BitmapFactory.decodeFile(new File(Utils.getDataFolderFile(getApplicationContext()),
                            localImageFileName).getAbsolutePath());

                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);

                    mExerciseImage.setImageBitmap(rotatedBitmap);
                } else {
                    mExerciseImage.setImageDrawable(null);
                }

                mCircularProgress.setInfoChairLevel(mCurrentTraining
						.getCurrentExercise().getMachineSetting());

				// first remove all existing callbacks
				mUiHandler.removeCallbacks(mUpdateRestTimer);
				mUiHandler.removeCallbacks(mGetReadyTimer);
               // mUiHandler.removeCallbacks(mUpdateExerciseTimer);

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

                if(curExercise.getGuidanceType().equals(Exercise.GUIDANCE_TYPE_DURATION)){
                    int minutes = curSeries.getNumberTotalRepetitions() / 60;
                    int seconds = curSeries.getNumberTotalRepetitions() - minutes * 60;
                    mSeriesInfoText.setText(
                            String.format(
                                    "Series %d (%d min %d sec, %d kg)",
                                    curExercise.getCurrentSeriesNumber(),
                                    minutes,
                                    seconds,
                                    curSeries.getWeight()));
                } else {
                    mSeriesInfoText.setText(
                            String.format(
                                    "Series %d (%d reps, %d kg)",
                                    curExercise.getCurrentSeriesNumber(),
                                    curSeries.getNumberTotalRepetitions(),
                                    curSeries.getWeight()));
                }


				mSeriesInformation.setVisibility(View.VISIBLE);

                mCircularProgress.setTimerMessage("RESTING");

				// get ready timer was not started yet so show the rest timer
				if (mGetReadyStartTimestamp == -1) {
					int currentRest = curSeries.getRestTime();
					mCircularProgress.setRestMaxProgress(currentRest);
					mCircularProgress.setRestMinProgress(0);

					// also start the periodic timer to update the rest screen
					mUiHandler.postDelayed(mUpdateRestTimer, 0);

                    // only in this state the user should be allowed to see the list of exercises
                    // update the training plan based on the current state
                    ExerciseAdapter adapter = new ExerciseAdapter(getApplicationContext(), new ArrayList<Exercise>(mCurrentTraining.getExercisesLeft()));
                    adapter.setSelectedExercisePosition(mCurrentTraining.getSelectedExercisePosition());
                    
                    mDrawerExercisesList.setAdapter(adapter);
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
				} else {
					mCircularProgress.setRestMaxProgress(mGetReadyInterval);
					mCircularProgress.setRestMinProgress(0);
					mCircularProgress.setTimerMessage("GET READY");

					mUiHandler.postDelayed(mGetReadyTimer, 0);

                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
				}
			} else {
				// otherwise show exercising UI
                mUiHandler.removeCallbacks(mUpdateRestTimer);

                // TODO: mightr be better to decouple the training here (and not pass it,
                // but rather pass a minimum set of parameters)
                EventBus.getDefault().post(new StartExerciseEvent(mCurrentTraining));

                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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

	/**
	 * Starts the select training activity.
	 * 
	 * @param view
	 */
	public void onBottomStrapClick(View view) {
        // if resting toggle the next/prev buttons and the current exercise info
        if(mCircularProgress.getCurrentState() != CircularProgressState.REST){
            Intent intent = new Intent(TrainingActivity.this,
                    TrainingSelectionList.class);
            startActivityForResult(intent, ACTIVITY_REQUEST_TRAININGS_LIST);
        }
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
		//mViewDuringExercise.setVisibility(View.VISIBLE);
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
				/*if (curExercise.getGuidanceType().equals(
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
				} else {*/
					showExerciseRateView();
				//}

                if(getResources().getBoolean(R.bool.sample_acceleration)) {
                    // do acceleration sampling
                    try {
                        mAccelerationRecorder.startAccelerationSampling(
                                mCurrentTraining.getTrainingStartTimestamp(),
                                mCurrentTraining.getRawFile(getApplicationContext()));
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
                    updateScreen();
				}
			} else if (intent
					.getAction()
					.equals(ServerCommunicationService.ACTION_GET_TRAINNGS_LIST_COMPLETED)) {
				// on list with training names fetched

				// calculate progress bar information and set progress
				int totalNumberOfTrainings = intent
						.getExtras()
						.getInt(ServerCommunicationService.PARAM_REPORT_PROGRESS_TOTAL);
				int progress = Math.round(1f / totalNumberOfTrainings * 100f);
				mProgressDialog.setProgress(progress);
			} else if (intent
					.getAction()
					.equals(ServerCommunicationService.ACTION_REPORT_PROGRESS)) {
				// on individual training item downloaded

				// calculate progress bar information and set progress with
				// training name
				int totalNumberOfTrainings = intent
						.getExtras()
						.getInt(ServerCommunicationService.PARAM_REPORT_PROGRESS_TOTAL);
				int trainingCount = intent
						.getExtras()
						.getInt(ServerCommunicationService.PARAM_REPORT_PROGRESS_CURRENT);
				String trainingName = intent
						.getExtras()
						.getString(
								ServerCommunicationService.PARAM_REPORT_PROGRESS_TEXT);
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
