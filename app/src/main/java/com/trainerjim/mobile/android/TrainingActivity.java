package com.trainerjim.mobile.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.trainerjim.mobile.android.database.CompletedTraining;
import com.trainerjim.mobile.android.database.TrainingPlan;
import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.events.BackPressedEvent;
import com.trainerjim.mobile.android.events.CancelTrainingEvent;
import com.trainerjim.mobile.android.events.DismissProgressEvent;
import com.trainerjim.mobile.android.events.EndDownloadTrainingsEvent;
import com.trainerjim.mobile.android.events.EndExerciseEvent;
import com.trainerjim.mobile.android.events.EndOverviewEvent;
import com.trainerjim.mobile.android.events.EndRateTraining;
import com.trainerjim.mobile.android.events.EndRestEvent;
import com.trainerjim.mobile.android.events.EndUploadCompletedTrainings;
import com.trainerjim.mobile.android.events.ExerciseImageEvent;
import com.trainerjim.mobile.android.events.ExercisesListEvent;
import com.trainerjim.mobile.android.events.ReportProgressEvent;
import com.trainerjim.mobile.android.events.EndTrainingEvent;
import com.trainerjim.mobile.android.events.StartTrainingEvent;
import com.trainerjim.mobile.android.events.ToggleGetReadyEvent;
import com.trainerjim.mobile.android.events.TrainingSelectedEvent;
import com.trainerjim.mobile.android.events.TrainingStateChangedEvent;
import com.trainerjim.mobile.android.fragments.EndTrainingFragment;
import com.trainerjim.mobile.android.fragments.ExerciseViewFragment;
import com.trainerjim.mobile.android.fragments.OverviewTrainingFragment;
import com.trainerjim.mobile.android.fragments.RateTrainingFragment;
import com.trainerjim.mobile.android.fragments.RestViewFragment;
import com.trainerjim.mobile.android.fragments.StartTrainingFragment;
import com.trainerjim.mobile.android.network.ServerCommunicationService;
import com.trainerjim.mobile.android.storage.PermanentSettings;
import com.trainerjim.mobile.android.ui.ExerciseAdapter;
import com.trainerjim.mobile.android.ui.ExerciseImagesPagerAdapter;
import com.trainerjim.mobile.android.util.Analytics;
import com.trainerjim.mobile.android.util.TutorialHelper;
import com.trainerjim.mobile.android.util.Utils;

import de.greenrobot.event.EventBus;

public class TrainingActivity extends Activity {

	private static final String TAG = Utils.getApplicationTag();

	private PermanentSettings mSettings;

	/**
	 * References to the XML defined UI controls.
	 */

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
     * Holds the information if the exercise image is currently visible or not. Determines the behaviour
     * of the back button.
     */
    private boolean mExerciseImageVisible;

    private Analytics mAnalytics;

    private TutorialHelper mTutorialHelper;

    @Override
	protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mAnalytics = Analytics.getInstance(getApplicationContext());

        EventBus.getDefault().register(this);

        mSettings = PermanentSettings.create(PreferenceManager
				.getDefaultSharedPreferences(this));

        mTutorialHelper = new TutorialHelper(this, mSettings);

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

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerExercisesList = (ListView)findViewById(R.id.exercises_list);

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

                mTutorialHelper.showExercisesListTutorial();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

		loadCurrentTraining();

        updateScreen();

        // set action when the user clicks the exercise in the exercises menu
        mDrawerExercisesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(mTutorialHelper.isTutorialActive()){
                return;
            }

            mAnalytics.logExerciseChanged();

            // move to a particular exercise in the training plan
            mCurrentTraining.selectExercise(i);
            mDrawerLayout.closeDrawers();

            updateExercisesList();

            EventBus.getDefault().post(new TrainingStateChangedEvent());
            }
        });

        registerForContextMenu(mDrawerExercisesList);
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

	/*
	 * TODO: Check if stuff in onCreate and onDestroy should be moved to more
	 * appropriate lifecycle methods.
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
        EventBus.getDefault().unregister(this);

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

        if(!mTutorialHelper.isTutorialActive()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            switch (item.getItemId()) {
                case R.id.skip_exercise: {
                    mAnalytics.logExerciseSkipped();

                    mCurrentTraining.removeExercise(info.position);
                    saveCurrentTraining();
                    updateScreen();
                    break;
                }
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

    @Override
    public void onBackPressed() {
        if(mExerciseImageVisible){
            // just notify all listeners (fragments) that the back button was clicked
            EventBus.getDefault().post(new BackPressedEvent());
        } else {
            super.onBackPressed();
        }
    }

    private void updateExercisesList() {
        ExerciseAdapter adapter = new ExerciseAdapter(getApplicationContext(), new ArrayList<>(mCurrentTraining.getExercisesLeft()));
        adapter.setSelectedExercisePosition(mCurrentTraining.getSelectedExercisePosition());

        mDrawerExercisesList.setAdapter(adapter);
    }

    private void showStartTrainingFragment(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_container, new StartTrainingFragment(mCurrentTraining, mTutorialHelper, mAnalytics, mSettings));
        ft.commit();

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        getActionBar().show();
    }

    private void showEndTrainingFragment(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_container, new EndTrainingFragment(mCurrentTraining));
        ft.commit();

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        getActionBar().hide();
    }

    private void showRateTrainingFragment(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_container, new RateTrainingFragment(mCurrentTraining));
        ft.commit();

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        getActionBar().hide();
    }

    private void showOverviewTrainingFragment(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_container, new OverviewTrainingFragment(mCurrentTraining));
        ft.commit();

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        getActionBar().hide();
    }

    private void showRestTrainingFragment(){
        // there are still some exercises to be performed
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_container, new RestViewFragment(mCurrentTraining, mTutorialHelper, mAnalytics));
        ft.commit();

        populateExerciseList();

        getActionBar().hide();
    }

    private void populateExerciseList(){
        ExerciseAdapter adapter = new ExerciseAdapter(getApplicationContext(), new ArrayList<>(mCurrentTraining.getExercisesLeft()));
        adapter.setSelectedExercisePosition(mCurrentTraining.getSelectedExercisePosition());

        mDrawerExercisesList.setAdapter(adapter);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

	private void updateScreen() {
        if (mCurrentTraining == null) {
            showStartTrainingFragment();

		} else if (mCurrentTraining.getCurrentExercise() == null) {
            if (!mCurrentTraining.isTrainingEnded()) {
                // no more exercises, show the done button
                showEndTrainingFragment();

            } else if (mCurrentTraining.getTrainingRating() == -1) {
				// show training rating screen
                showRateTrainingFragment();
			} else {
                // show fragment with training overview
                showOverviewTrainingFragment();
			}

		} else {
			showRestTrainingFragment();
		}
    }

    /**
     * Triggered when the changes the selected training.
     * @param event
     */
    /*public void onEvent(TrainingSelectedEvent event){
        selectTraining(event.getSelectedTrainingId());
    }*/

    /**
     * Triggered on start training click.
     * @param event
     */
    public void onEvent(StartTrainingEvent event){
        //selectTraining(event.getSelectedTrainingId());

        if(mSettings.getSelectedTrainingId() == -1){
            return;
        }

        // should always return a valid training
        TrainingPlan selectedTrainingPlan = TrainingPlan.getByTrainingId(mSettings.getSelectedTrainingId());

        // load to memory
        mCurrentTraining = Utils.getGsonObject().fromJson(selectedTrainingPlan.getData(),
                Training.class);
        mCurrentTraining.startTraining();

        // switch fragments
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        RestViewFragment frag = new RestViewFragment(mCurrentTraining, mTutorialHelper, mAnalytics);
        ft.replace(R.id.main_container, frag);
        ft.commit();

        // rebuild the menu and hide it
        getActionBar().hide();

        populateExerciseList();

        saveCurrentTraining();
    }

    /**
     * Triggered when the current training was canceled.
     * @param event
     */
    public void onEvent(CancelTrainingEvent event){
        mCurrentTraining = null;

        saveCurrentTraining();
        showStartTrainingFragment();
    }

    /**
     * Triggered when the exercise image visible state changes.
     * @param event
     */
    public void onEvent(ExerciseImageEvent event){
        mExerciseImageVisible = event.isVisible();
    }

    /**
     * Triggered when the exercises list visual state changes.
     * @param event
     */
    public void onEvent(ExercisesListEvent event){
        if(mTutorialHelper.isTutorialActive()){
            return;
        }

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    // TODO: rename ToggleGetReadyEvent to GetReadyEvent
    /**
     * Triggered when the get ready event is toggled.
     * @param event
     */
    public void onEvent(ToggleGetReadyEvent event){
        mDrawerLayout.setDrawerLockMode(event.isEnabled() ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED :
                DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    /**
     * Triggered when the user starts to perform an exercise.
     * @param event
     */
    public void onEvent(EndRestEvent event){

        // start the exercise
        mCurrentTraining.startExercise();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ExerciseViewFragment evf = new ExerciseViewFragment(mCurrentTraining, mTutorialHelper);
        ft.replace(R.id.main_container, evf);
        ft.commit();
    }

    /**
     * Triggered when the user ends performing a particular exercise.
     * @param event
     */
    public void onEvent(EndExerciseEvent event){

        // advance to the next activity
        mCurrentTraining.nextActivity();

        saveCurrentTraining();

        updateExercisesList();

        if (mCurrentTraining.getCurrentExercise() == null) {
            showEndTrainingFragment();

        } else {
            // exercise -> rest
            showRestTrainingFragment();
        }
    }

    /**
     * Triggered when the user ends a training.
     * @param event
     */
    public void onEvent(EndTrainingEvent event){
        saveCurrentTraining();

        showRateTrainingFragment();
    }

    /**
     * Triggered when the user saves the training rating.
     * @param event
     */
    public void onEvent(EndRateTraining event){
        saveCurrentTraining();

        showOverviewTrainingFragment();
    }

    /**
     * Triggered when the user dismisses the training overview.
     * @param event
     */
    public void onEvent(EndOverviewEvent event){
        int userId = mSettings.getUserId();
        new CompletedTraining(mCurrentTraining.getTrainingName(), Utils.getGsonObject().toJson(mCurrentTraining.extractMeasurement(userId)), userId).save();

        mCurrentTraining = null;
        saveCurrentTraining();
        showStartTrainingFragment();
    }
}
