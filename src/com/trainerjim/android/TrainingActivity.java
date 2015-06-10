package com.trainerjim.android;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
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
import com.trainerjim.android.timers.GetReadyTimer;
import com.trainerjim.android.timers.UpdateRestTimer;
import com.trainerjim.android.ui.CircularProgressControl;
import com.trainerjim.android.ui.ExerciseAdapter;
import com.trainerjim.android.ui.CircularProgressControl.CircularProgressState;
import com.trainerjim.android.util.Utils;

import de.greenrobot.event.EventBus;

public class TrainingActivity extends Activity {

	private static final String TAG = Utils.getApplicationTag();

	private final static int MENU_SYNC = Menu.FIRST;
	private final static int MENU_UPLOAD = Menu.FIRST + 1;
    private final static int MENU_LOGOUT = Menu.FIRST + 2;
    private final static int MENU_CANCEL = Menu.FIRST + 3;

	private static final int ACTIVITY_REQUEST_TRAININGS_LIST = 0;

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
	private ImageView mImageArrowSeriesInfo;
	private CheckBox mEditDetailsCheckbox;
    private ImageView mExerciseImage;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerExercisesList;

    private ActionBarDrawerToggle mDrawerToggle;

	private AccelerationRecorder mAccelerationRecorder;

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
     * This runnable updates the screen during the rest state.It calls itself
     * recursively until externally stopped or until there are no more exercises
     * left.
     */
    private UpdateRestTimer mUpdateRestTimer = null;


    private GetReadyTimer mGetReadyTimer = null;

    @Override
	protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);


        // TODO: We should move this to event bus
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

        Picasso.with(getApplicationContext()).setIndicatorsEnabled(true);

        mSettings = PermanentSettings.create(PreferenceManager
				.getDefaultSharedPreferences(this));

		// if we are not logged in yet show the login activity first
		if (mSettings.getUsername().equals("")) {
			startActivity(new Intent(TrainingActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // used this pattern for displaying the action bar in this activity
        // http://stackoverflow.com/questions/8500283/how-to-hide-action-bar-before-activity-is-created-and-then-show-it-again
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        getActionBar().setDisplayHomeAsUpEnabled(false);

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
		mImageArrowSeriesInfo = (ImageView) findViewById(R.id.imageArrowSeriesInfo);
//		mEditDetailsCheckbox = (CheckBox) findViewById(R.id.checkbox_edit_details);
        mExerciseImage = (ImageView)findViewById(R.id.exerciseImage);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerExercisesList = (ListView)findViewById(R.id.exercises_list);

        mUpdateRestTimer = new UpdateRestTimer(this);
        mGetReadyTimer = new GetReadyTimer(this);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.jim_launcher,
                R.string.app_name,  /* "open drawer" description */
                R.string.app_name /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                // don't hide the action bar if we are in the training select screen
                // TODO: add convenience methods for determining the current screen
                if(mCurrentTraining != null) {
                    getActionBar().hide();
                }
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().show();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

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

		mAccelerationRecorder = AccelerationRecorder
				.create(getApplicationContext());

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
                // TODO: think about a better way to consistently remove the update timer callback
                mUiHandler.removeCallbacks(mUpdateRestTimer);
                mCurrentTraining.removeExercise(info.position);
                saveCurrentTraining();
                updateScreen();
                break;
            }
        }

        return super.onContextItemSelected(item);
    }

    public Training getCurrentTraining() {
        return mCurrentTraining;
    }

    public Handler getUiHandler(){
        return mUiHandler;
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
        //if(mExerciseImage.getDrawable() != null) {
            mExerciseImage.setVisibility(visible ? View.VISIBLE : View.GONE);
            mDrawerLayout.setDrawerLockMode(visible ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
        //}
	}

    /**
     * This event is called when a user ends performing a particular exercise.
     * @param event
     */
    public void onEvent(EndExerciseEvent event){
        mUiHandler.removeCallbacks(mUpdateRestTimer);

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
        MenuInflater inflater = getMenuInflater();

        if(mCurrentTraining == null) {
            inflater.inflate(R.menu.inactive_training_actions, menu);
        } else {
            inflater.inflate(R.menu.active_training_actions, menu);
        }
        return super.onCreateOptionsMenu(menu);
	};

    private void cancelCurrentTraining() {
        mUiHandler.removeCallbacks(mUpdateRestTimer);
        mUiHandler.removeCallbacks(mGetReadyTimer);

        mCurrentTraining = null;

        saveCurrentTraining();
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

		switch (item.getItemId()) {
		case R.id.action_sync: {
            runUploadTrainings();

			break;
		}
        case R.id.action_cancel:{
            new AlertDialog.Builder(this)
                    .setTitle("Cancel training")
                    .setMessage("Are you sure you would like to cancel the training?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            cancelCurrentTraining();
                            updateScreen();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
            break;
        }
		case R.id.action_logout: {
            cancelCurrentTraining();

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

        // TODO: think about where to make sure this is not negative (invalid) - we will also have
        // to check the if the session cookie is set
        intent.putExtra(ServerCommunicationService.INTENT_KEY_USER_ID,
                mSettings.getUserId());
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
			mGetReadyTimer.toggleGetReadyStartTimestamp();
		} else {
			// exercise -> rest

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

            // show the action bar
            getActionBar().show();

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

            getActionBar().hide();
		} else {
			// in general, show no timer message
			mCircularProgress.setTimerMessage("");

			Exercise curExercise = mCurrentTraining.getCurrentExercise();
			// there are still some exercises to be performed
			if (mCurrentTraining.isCurrentRest()) {
                // TODO: encapsulate this or find a better way to do it
                Picasso.with(this)
                        .load(String.format("%s%s",
                                getResources().getString(R.string.server_url),
                                curExercise.getExerciseType().getImageUrl()))
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .transform(new Transformation() {

                            @Override
                            public Bitmap transform(Bitmap source) {
                                /**
                                 * This code rotates the image if it's height is bigger than width
                                 * (as the app is used in the portrait orientation only)
                                 */
                                int targetWidth = source.getWidth();
                                int targetHeight = source.getHeight();

                                if (targetHeight < targetWidth) {
                                    Matrix matrix = new Matrix();

                                    matrix.postRotate(90);


                                    Bitmap rotatedBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

                                    if (rotatedBitmap != source) {
                                        source.recycle();
                                    }

                                    return rotatedBitmap;
                                }

                                return source;
                            }

                            @Override
                            public String key() {
                                return "transformation" + " desiredWidth";
                            }
                        })
                        .into(mExerciseImage);

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
				if (mGetReadyTimer.isStarted()) {
                    mCircularProgress.setRestMaxProgress(Utils.GET_READY_INTERVAL);
                    mCircularProgress.setRestMinProgress(0);
                    mCircularProgress.setTimerMessage("GET READY");

                    mUiHandler.postDelayed(mGetReadyTimer, 0);
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

				} else {
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

            getActionBar().hide();
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

	private void showExerciseRateView() {
		//mViewDuringExercise.setVisibility(View.VISIBLE);
	}

    public void updateGetReadyTimer(int secondsLeft){
        mCircularProgress.setRestProgressValue(secondsLeft);
        mCircularProgress.setTimer(secondsLeft);
    }

    public void updateRestTimer(int restLeft){
        mCircularProgress.setRestProgressValue(restLeft < 0 ? 0
                : restLeft);
        mCircularProgress.setTimer(Math.abs(restLeft));
    }

    public void getReadyTimerOver(){

        // time is up, start the exercise animation
        mCurrentTraining.startExercise();

        showExerciseRateView();

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

                // once trainings are uploaded, sync new trainings as well
                runTrainingsSync();

				Toast.makeText(
						getApplicationContext(),
						String.format("Uploaded %d of %d items.",
								successfulItems, totalNumberOfTrainings),
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
