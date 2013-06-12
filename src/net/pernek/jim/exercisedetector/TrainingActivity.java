package net.pernek.jim.exercisedetector;

import java.util.Hashtable;

import net.pernek.jim.exercisedetector.CircularProgressControl.CircularProgressState;
import net.pernek.jim.exercisedetector.ui.TrainingSelectionList;
import net.pernek.jim.exercisedetector.util.Utils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.DropBoxManager.Entry;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class TrainingActivity extends Activity {
	
	private static final String TAG = Utils.getApplicationTag();
	
	private static final int ACTIVITY_REQUEST_TRAININGS_LIST = 0;

	private DetectorSettings mSettings;
	
	private CircularProgressControl mCircularProgress;
	private ViewFlipper mViewFlipper;

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
	 * Contains IDs of training rating images in non-selected (non-clicked) state.
	 */
	private static int[] TRAINING_RATING_IMAGES = { R.drawable.sm_1_ns,
			R.drawable.sm_2_ns,
			R.drawable.sm_3_ns,
			R.drawable.sm_4_ns};
	
	/**
	 * Holds the ID of the currently selected training rating (or -1 if no
	 * rating was yet selected). 
	 */
	private int mTrainingRatingSelectedID = -1;
	
	/**
	 * Contains IDs of training rating images in selected (touched) state. 
	 */
	private static int[] TRAINING_RATING_SELECTED_IMAGES = { R.drawable.sm_1_s,
		R.drawable.sm_2_s,
		R.drawable.sm_3_s,
		R.drawable.sm_4_s};
		
	/**
	 * References to the ImageViews hosting training rating images. 
	 */
	private ImageView[] mTrainingRatingImages;

	/**
	 * Initializes a list of training rating ImageViews references. Additionally,
	 * sets the non-selected images.  
	 */
	private void initializeTrainingRatings() {
		mTrainingRatingImages = new ImageView[4];
		mTrainingRatingImages[0] = (ImageView)findViewById(R.id.trainingRating1);
		mTrainingRatingImages[1] = (ImageView)findViewById(R.id.trainingRating2);
		mTrainingRatingImages[2] = (ImageView)findViewById(R.id.trainingRating3);
		mTrainingRatingImages[3] = (ImageView)findViewById(R.id.trainingRating4);
		
		for(int i=0; i < mTrainingRatingImages.length; i++){
			mTrainingRatingImages[i].setImageResource(TRAINING_RATING_IMAGES[i]);
		}
	}

	private int stateCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		mSettings = DetectorSettings.create(PreferenceManager
				.getDefaultSharedPreferences(this));
		
		// if we are not logged in yet show the login activity first
		if(mSettings.getUsername().equals("")){
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
						.values()[++stateCount % 4]);

				if (mCircularProgress.getCurrentState() == CircularProgressState.EXERCISE) {
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
	}

	public void testClick(View v) {
		mViewFlipper.showPrevious();
	}

	/** Triggered when clicked on a specific training rating icon.
	 * @param v The view hosting the clicked icon (of type ImageView).
	 */
	public void onTrainingRatingSelected(View v) {
		ImageView trainingRatingSelected = (ImageView) v;

		int imageSelectedPadding = getResources().
				getDimensionPixelSize(R.dimen.training_rating_smile_selected_padding);
		int imagePadding = getResources().
				getDimensionPixelSize(R.dimen.training_rating_smile_padding);
		
		// loop through training ratings image views and set the appropriate image
		for(int i=0; i < mTrainingRatingImages.length; i++){
			if(mTrainingRatingImages[i] == trainingRatingSelected){
				mTrainingRatingImages[i].setImageResource(TRAINING_RATING_SELECTED_IMAGES[i]);
				mTrainingRatingImages[i].setPadding(imageSelectedPadding, imageSelectedPadding, 
						imageSelectedPadding, imageSelectedPadding);
				mTrainingRatingSelectedID = i;
			} else {
				mTrainingRatingImages[i].setImageResource(TRAINING_RATING_IMAGES[i]);
				mTrainingRatingImages[i].setPadding(imagePadding, imagePadding, 
						imagePadding, imagePadding);
			}
		}
	}
	
	public void onSelectTrainingClick(View view){
		Intent intent = new Intent(TrainingActivity.this, TrainingSelectionList.class);
        startActivityForResult(intent, ACTIVITY_REQUEST_TRAININGS_LIST);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case ACTIVITY_REQUEST_TRAININGS_LIST:{
			if(data != null && data.hasExtra(TrainingSelectionList.INTENT_EXTRA_SELECTED_TRAINING_KEY)){
				Toast.makeText(this, data.getStringExtra(TrainingSelectionList.INTENT_EXTRA_SELECTED_TRAINING_KEY), Toast.LENGTH_LONG).show();
			}
			break;
		}
		default:
			Log.d(TAG, "onActivityResult default switch.");
			break;
		}
		
	}
}
