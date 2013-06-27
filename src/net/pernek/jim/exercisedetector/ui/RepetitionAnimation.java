package net.pernek.jim.exercisedetector.ui;

import android.opengl.Visibility;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

public class RepetitionAnimation {
	private LinearLayout mAnimationObject;
	private float mExpandedHeight;
	private Handler mUiHandler;

	private int mUpDuration;
	private int mDownDuration;
	private int mInterRepetitionRestDuration = 1000;
	private int mAfterRepetitionDuration = 500;

	private int mTotalRepetitions;
	private int mCurrentRepetition = 0;

	private boolean mAnimationRunning;

	private ScaleAnimation mAnimationUp;
	private ScaleAnimation mAnimationDown;

	public RepetitionAnimation(LinearLayout animationObject, Handler uiHandler) {
		mAnimationObject = animationObject;
		mUiHandler = uiHandler;

		mAnimationRunning = false;
	}

	public boolean isAnimationRunning() {
		return mAnimationRunning;
	}

	public void startAnimation(int fullHeight, int upDuration,
			int downDuration, int numRepetitions) {
		mExpandedHeight = fullHeight;
		mUpDuration = upDuration;
		mDownDuration = downDuration;
		mTotalRepetitions = numRepetitions;

		mAnimationObject.setVisibility(View.VISIBLE);
		mAnimationRunning = true;

		runAnimationUp();
	}

	public void stopAnimation() {
		mAnimationRunning = false;
		mAnimationObject.clearAnimation();
		mUiHandler.removeCallbacks(mAfterRepetitionRestRunnable);
		mUiHandler.removeCallbacks(mInterRepetitionRestRunnable);

		mCurrentRepetition = 0;

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 1);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mAnimationObject.setLayoutParams(lp);

		mAnimationObject.setVisibility(View.GONE);
	}

	private void runAnimationUp() {
		if (mCurrentRepetition < mTotalRepetitions) {

			mAnimationUp = new ScaleAnimation(1f, 1f, 1f, 2 * mExpandedHeight,
					Animation.RELATIVE_TO_SELF, (float) 0.5,
					Animation.RELATIVE_TO_SELF, (float) 0.5);
			mAnimationUp.setFillAfter(true);
			mAnimationUp.setDuration(mUpDuration);
			mAnimationUp.setAnimationListener(mAnimationUpListener);

			if (mCurrentRepetition == 0) {
				mAnimationObject.startAnimation(mAnimationUp);
			} else {
				mUiHandler.postDelayed(mAfterRepetitionRestRunnable, mAfterRepetitionDuration);
			}
		} else {
			stopAnimation();
		}
	}
	
	private Runnable mAfterRepetitionRestRunnable = new Runnable() {
		@Override
		public void run() {
			mAnimationObject.startAnimation(mAnimationUp);
		}
	};
	
	private Runnable mInterRepetitionRestRunnable = new Runnable() {
		@Override
		public void run() {
			runAnimationDown();
		}
	};

	private void runAnimationDown() {
		mAnimationDown = new ScaleAnimation(1f, 1f, 2 * mExpandedHeight, 1f,
				Animation.RELATIVE_TO_SELF, (float) 0.5,
				Animation.RELATIVE_TO_SELF, (float) 0.5);
		mAnimationDown.setDuration(mDownDuration);
		mAnimationDown.setAnimationListener(mAnimationDownListener);
		mAnimationObject.startAnimation(mAnimationDown);
	}

	private AnimationListener mAnimationUpListener = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			// optional rest between up and down should be set here
			mUiHandler.postDelayed(mInterRepetitionRestRunnable, mInterRepetitionRestDuration);
		}
	};

	private AnimationListener mAnimationDownListener = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mAnimationObject.setVisibility(View.GONE);
			mCurrentRepetition++;

			runAnimationUp();
		}
	};

}
