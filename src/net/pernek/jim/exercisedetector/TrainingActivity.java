package net.pernek.jim.exercisedetector;

import net.pernek.jim.exercisedetector.CircularProgressControl.CircularProgressState;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Toast;

public class TrainingActivity extends Activity {
	
	private CircularProgressControl mCircularProgress;
	
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
	
	private int stateCount = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// TODO: Use ViewFlipper to change between button circular button view, and rate training view
		super.onCreate(savedInstanceState);
		setContentView(R.layout.training_activity);
		mCircularProgress = (CircularProgressControl)findViewById(R.id.circularProgress);
		mCircularProgress.setRestMaxProgress(100);
		mCircularProgress.setRestMinProgress(0);
		
		CircularProgressState currentState = CircularProgressState.values()[stateCount];
		
		mCircularProgress.setCurrentState(currentState);
		mCircularProgress.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_LONG).show();
				mCircularProgress.setCurrentState(
						CircularProgressState.values()[++stateCount % 4]);
				
				if(mCircularProgress.getCurrentState() == CircularProgressState.EXERCISE){
					mTrainingCounter = 0;
					mExerciseCounter = 0;
					mRestCounter = 100;
					
					mUiHandler.removeCallbacks(mRunTimerUpdate);
					mUiHandler.postDelayed(mRunTimerUpdate, 100);
				}
				return false;
			}
		});
	}

}
