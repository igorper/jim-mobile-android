package com.trainerjim.mobile.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
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

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;
import com.trainerjim.mobile.android.database.CompletedTraining;
import com.trainerjim.mobile.android.database.TrainingPlan;
import com.trainerjim.mobile.android.entities.Exercise;
import com.trainerjim.mobile.android.entities.Series;
import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.events.DismissProgressEvent;
import com.trainerjim.mobile.android.events.EndDownloadTrainingsEvent;
import com.trainerjim.mobile.android.events.EndExerciseEvent;
import com.trainerjim.mobile.android.events.EndRateTraining;
import com.trainerjim.mobile.android.events.EndUploadCompletedTrainings;
import com.trainerjim.mobile.android.events.ReportProgressEvent;
import com.trainerjim.mobile.android.events.StartRateTraining;
import com.trainerjim.mobile.android.events.StartExerciseEvent;
import com.trainerjim.mobile.android.events.TrainingSelectedEvent;
import com.trainerjim.mobile.android.network.ServerCommunicationService;
import com.trainerjim.mobile.android.storage.PermanentSettings;
import com.trainerjim.mobile.android.timers.GetReadyTimer;
import com.trainerjim.mobile.android.timers.UpdateRestTimer;
import com.trainerjim.mobile.android.ui.CircularProgressControl;
import com.trainerjim.mobile.android.ui.ExerciseImagesPagerAdapter;
import com.trainerjim.mobile.android.ui.ExerciseAdapter;
import com.trainerjim.mobile.android.ui.CircularProgressControl.CircularProgressState;
import com.trainerjim.mobile.android.util.Analytics;
import com.trainerjim.mobile.android.util.Utils;

import de.greenrobot.event.EventBus;

public class TrainingActivity extends Activity {

	private static final String TAG = Utils.getApplicationTag();

	private final static int MENU_SYNC = Menu.FIRST;
	private final static int MENU_UPLOAD = Menu.FIRST + 1;
    private final static int MENU_LOGOUT = Menu.FIRST + 2;
    private final static int MENU_CANCEL = Menu.FIRST + 3;

	private static final int ACTIVITY_REQUEST_TRAININGS_LIST = 0;

	private PermanentSettings mSettings;

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
     * This runnable updates the screen during the rest state.It calls itself
     * recursively until externally stopped or until there are no more exercises
     * left.
     */
    private UpdateRestTimer mUpdateRestTimer = null;

    private GetReadyTimer mGetReadyTimer = null;

    private ExerciseImagesPagerAdapter mExerciseImagesPagerAdapter;

    private ViewPager mViewPager;

    private Analytics mAnalytics;

    @Override
	protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mAnalytics = Analytics.getInstance(getApplicationContext());

        EventBus.getDefault().register(this);

        Picasso.with(getApplicationContext()).setIndicatorsEnabled(true);

        mSettings = PermanentSettings.create(PreferenceManager
				.getDefaultSharedPreferences(this));

		// if we are not logged in yet show the login activity first
		if (!isUserLoggedIn()) {
			startActivity(new Intent(TrainingActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // used this pattern for displaying the action bar in this activity
        // http://stackoverflow.com/questions/8500283/how-to-hide-action-bar-before-activity-is-created-and-then-show-it-again
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

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
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mUpdateRestTimer = new UpdateRestTimer(this);
        mGetReadyTimer = new GetReadyTimer(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

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

                mAnalytics.logMenuShow();

                getActionBar().show();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

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

                mAnalytics.logExerciseChanged();

                // move to a particular exercise in the training plan
                mCurrentTraining.selectExercise(i);
                mDrawerLayout.closeDrawers();
                updateScreen();
            }
        });

        registerForContextMenu(mDrawerExercisesList);

		// try to fetch trainings if not available
		// TODO: DB
        if (isUserLoggedIn() && TrainingPlan.getAll(mSettings.getUserId()).size() == 0) {
			runTrainingsSync();
		}

        // always upload the trainings on start
        runUploadTrainings();
	}

	/**
	 * Returns <code>true</code> if user is currently logged in, otherwise
	 * <code>false</code>.
	 * 
	 * @return
	 */
	private boolean isUserLoggedIn() {
		return mSettings.getUserId() != -1;
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
                mAnalytics.logExerciseSkipped();

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

	/**
	 * Toggles the visibility of additional info circular button overlay and
	 * sets the appropriate icon.
	 * 
	 * @param visible
	 */
	private void toggleInfoButtonVisible(boolean visible) {

        if(visible){
            mAnalytics.logShowExerciseImage();
        }

        mViewPager.setVisibility(visible ? View.VISIBLE : View.GONE);
        mDrawerLayout.setDrawerLockMode(visible ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
	}

    @Override
    public void onBackPressed() {
        if(mViewPager.getVisibility() == View.VISIBLE){
            toggleInfoButtonVisible(false);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * This event is called when a user ends performing a particular exercise.
     * @param event
     */
    public void onEvent(EndExerciseEvent event){
        mUiHandler.removeCallbacks(mUpdateRestTimer);

        // end this exercise (series)
        mCurrentTraining.endExercise();

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
            runTrainingsSync();

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
            new AlertDialog.Builder(this)
                    .setTitle("Log out")
                    .setMessage("Are you sure you would like to log out?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            cancelCurrentTraining();
                            mSettings.saveUserId(-1);
                            finish();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();

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
        // show the progress bar
        setProgressBarIndeterminateVisibility(true);

		Intent intent = new Intent(this, ServerCommunicationService.class);
		intent.putExtra(ServerCommunicationService.INTENT_KEY_ACTION,
				ServerCommunicationService.ACTION_UPLOAD_COMPLETED_TRAININGS);

        // TODO: think about where to make sure this is not negative (invalid) - we will also have
        // to check the if the session cookie is set
        intent.putExtra(ServerCommunicationService.INTENT_KEY_USER_ID,
                mSettings.getUserId());
		startService(intent);
	}

	/**
	 * Initiates the training sync process.
	 */
	private void runTrainingsSync() {
        setProgressBarIndeterminateVisibility(true);

		Intent intent = new Intent(this, ServerCommunicationService.class);
		intent.putExtra(ServerCommunicationService.INTENT_KEY_ACTION,
				ServerCommunicationService.ACTION_FETCH_TRAININGS);

        // TODO: think about where to make sure this is not negative (invalid) - we will also have
        // to check the if the session cookie is set
        intent.putExtra(ServerCommunicationService.INTENT_KEY_USER_ID,
                mSettings.getUserId());
		startService(intent);
	}

	private void changeTrainingPlanState() {
		/*if (mViewDuringExercise.getVisibility() == View.VISIBLE) {
			// exercise has to be rated before doing anything else

		} else */if (mCircularProgress.isInfoVisible()) {
			// if info button is visible close it on tap
			toggleInfoButtonVisible(false);
		} else if (mCurrentTraining == null) {
			// start button was clicked

            // should always return a valid training
            TrainingPlan selectedTrainingPlan = TrainingPlan.getByTrainingId(mSettings.getSelectedTrainingId());

            // load to memory
            mCurrentTraining = Utils.getGsonObject().fromJson(selectedTrainingPlan.getData(),
                    Training.class);
            mCurrentTraining.startTraining();
		} else if (mCurrentTraining.getCurrentExercise() == null) {
			if (!mCurrentTraining.isTrainingEnded()) {
				// I'm done was clicked
				mCurrentTraining.endTraining();
			} else {
				// overview button was clicked

				// store training to the database
                int userId = mSettings.getUserId();
                new CompletedTraining(mCurrentTraining.getTrainingName(), Utils.getGsonObject().toJson(mCurrentTraining.extractMeasurement(userId)), userId).save();

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
	 * This is the sole method responsible for setting the activity UI (apart
	 * from runnables, which also have to be registered in this method, and are
	 * responsible for periodic screen changes)
	 */
	private void updateScreen() {
        // deregister current exercise list adapter as we will register an updated one
        mDrawerExercisesList.setAdapter(null);

		if (mCurrentTraining == null) {
			// no training started yet, show the start button
			mCircularProgress.setCurrentState(CircularProgressState.START);

            //// TODO: DB if(getAvailableTrainings().getCount() > 0){
            if(TrainingPlan.getAll(mSettings.getUserId()).size() > 0){
                // saved selected training plan ID should always be a valid ID.
                TrainingPlan selectedTrainingPlan = TrainingPlan.getByTrainingId(mSettings.getSelectedTrainingId());
                mTrainingSelectorText.setText(selectedTrainingPlan.getName());
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
				mSeriesInfoText.setText("hold circle to finish");
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

				mSeriesInfoText.setText("hold circle to close");
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

                List<String> photoImages = curExercise.getExerciseType().getPhotoImages();

                mExerciseImagesPagerAdapter = new ExerciseImagesPagerAdapter(this, photoImages);

                // TODO: do we need to remove previous adapter?
                mViewPager.setAdapter(mExerciseImagesPagerAdapter);

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

            // show the info button only if the get ready timer is not running
            mInfoButton.setVisibility(mGetReadyTimer.isStarted() ? View.INVISIBLE : View.VISIBLE);
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

        updateScreen();
    }

    public void onEvent(final TrainingSelectedEvent event){
        mUiHandler.post(new Runnable() {

            @Override
            public void run() {
                selectTraining(event.getSelectedTrainingId());
                updateScreen();
            }
        });
    }

    /**
     * This function saves the currently selected training id to persistent storage. It makes sure
     * that selected training id is a legal (existing). If the input training id is not valid training
     * id from the first database training is used.
     * @param trainingId
     */
    private void selectTraining(int trainingId){
        mSettings.saveSelectedTrainingId(trainingId == -1 ? TrainingPlan.getAll(mSettings.getUserId()).get(0).getTrainingId() : trainingId);
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
			// on training selected from the list of trainings
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
                        // TODO: DB
						//updateTrainingSelector(trainingId);
                        updateScreen();
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


    public void onEvent(final ReportProgressEvent event){
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.setMessage(event.message);
                mProgressDialog.setMax(event.maxProgress);
                mProgressDialog.setProgress(event.currentProgress);
                mProgressDialog.show();
            }
        }, 0);
    }

    public void onEvent(final EndUploadCompletedTrainings event){
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setProgressBarIndeterminateVisibility(false);
            }
        }, 0);
    }

    public void onEvent(final DismissProgressEvent event){
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.hide();
            }
        }, 0);
    }

    public void onEvent(final EndDownloadTrainingsEvent event){

        mUiHandler.postDelayed(new Runnable() {
           @Override
           public void run() {
               setProgressBarIndeterminateVisibility(false);
               if(event.getStatus()) {
                   selectTraining(-1);
                   updateScreen();
               } else {
                   Toast.makeText(getApplicationContext(), "Download trainings: " + event.getErrorMessage(), Toast.LENGTH_LONG).show();
               }
           }
        }, 0);


        // after training were downloaded run the upload as well
        runUploadTrainings();
	}
}
