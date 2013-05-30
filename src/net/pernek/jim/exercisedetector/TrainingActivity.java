package net.pernek.jim.exercisedetector;

import net.pernek.jim.exercisedetector.CircularProgressControl.CircularProgressState;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class TrainingActivity extends Activity {
	
	private CircularProgressControl mCircularProgress;
	
	private int mTrainingCounter = 0;
	private int mExerciseCounter = 100;
	private int mRestCounter = 0;
	
	private Handler mUiHandler = new Handler();
	private Runnable mRunTimerUpdate = new Runnable() {

		@Override
		public void run() {
			mCircularProgress.setTrainingProgressValue(mTrainingCounter++);
			mCircularProgress.setExerciseProgressValue(mExerciseCounter++);
			mCircularProgress.setRestProgressValue(mRestCounter);
			
			mRestCounter += 2;
			
			mUiHandler.postDelayed(this, 100);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.training_activity);
		
		mCircularProgress = (CircularProgressControl)findViewById(R.id.circularProgress);
		mCircularProgress.setExerciseMaxProgress(200);
		mCircularProgress.setExerciseMinProgress(100);
		mCircularProgress.setRestMaxProgress(200);
		mCircularProgress.setCurrentState(CircularProgressState.OVERVIEW);
		
		mUiHandler.postDelayed(mRunTimerUpdate, 100);
	}

}
