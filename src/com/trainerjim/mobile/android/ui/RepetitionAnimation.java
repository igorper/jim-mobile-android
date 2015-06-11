package com.trainerjim.mobile.android.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

/**
 * This class visualizes the repetition guidance animation.
 * 
 * @author Igor
 * 
 */
public class RepetitionAnimation {

	/**
	 * Object to be animated.
	 */
	private LinearLayout mAnimationObject;

	/**
	 * Handler to the activity UI thread.
	 */
	private Handler mUiHandler;

	/**
	 * The expanded (full) height of the animation object in pixels. The
	 * animation object will be scaled from 1 px to this height and vice versa,
	 * using this value as a scaling factor.
	 */
	private float mExpandedHeight;

	/**
	 * Duration of repetition up movement.
	 */
	private int mUpDuration;

	/**
	 * Duration of repetition down movement.
	 */
	private int mDownDuration;

	/**
	 * Duration of rest between the repetition up and down movement in ms.
	 */
	private int mInterRepetitionRestDuration = 1000;

	/**
	 * Duration after each repetition in ms (the rest between two repetitions).
	 */
	private int mAfterRepetitionDuration;

	/**
	 * Total number of repetitions to be performed.
	 */
	private int mTotalRepetitions;

	/**
	 * Number of already performed repetitions.
	 */
	private int mCurrentRepetition = 0;

	/**
	 * Tells if the animation is currently in progress or not.
	 */
	private boolean mAnimationRunning;

	/**
	 * ScaleAnimation for the repetition up movement.
	 */
	private ScaleAnimation mAnimationUp;

	/**
	 * ScaleAnimation for the repetition down movement.
	 */
	private ScaleAnimation mAnimationDown;

	/**
	 * All registered repetition animation listeners.
	 */
	private List<RepetitionAnimationListener> mRepetitionAnimationListeners = new ArrayList<RepetitionAnimationListener>();

	/**
	 * @param animationObject
	 *            contains the reference to the vizual control that will be
	 *            animated.
	 * @param uiHandler
	 *            handler to the calling activity ui thread used to schedule
	 *            screen changes.
	 */
	public RepetitionAnimation(LinearLayout animationObject, Handler uiHandler) {
		mAnimationObject = animationObject;
		mUiHandler = uiHandler;

		mAnimationRunning = false;
	}

	/**
	 * Registers a {@link RepetitionAnimationListener}.
	 * 
	 * @param listener
	 */
	public void addRepetitionAnimationListener(
			RepetitionAnimationListener listener) {
		mRepetitionAnimationListeners.add(listener);
	}

	/**
	 * Removes registered {@link RepetitionAnimationListener}.
	 * 
	 * @param listener
	 */
	public void removeRepetitionAnimationListener(
			RepetitionAnimationListener listener) {
		mRepetitionAnimationListeners.remove(listener);
	}

	/**
	 * Tells if the animation is currently running (in progress) or not.
	 * 
	 * @return
	 */
	public boolean isAnimationRunning() {
		return mAnimationRunning;
	}

	/**
	 * Starts the animation with the default inter and after repetition rest (0
	 * ms).
	 * 
	 * @param fullHeight
	 *            is the expanded height of the animation object.
	 * @param upDuration
	 *            is the duration of the repetition up movement in ms.
	 * @param downDuration
	 *            is the duration of the repetition down movement in ms.
	 * @param numRepetitions
	 *            is the number of animation repetitions.
	 */
	public void startAnimation(int fullHeight, int upDuration,
			int downDuration, int numRepetitions) {
		startAnimation(fullHeight, upDuration, downDuration, 0, 0,
				numRepetitions);
	}

	/**
	 * Starts the animation with the default inter and after repetition rest (0
	 * ms).
	 * 
	 * @param fullHeight
	 *            is the expanded height of the animation object.
	 * @param upDuration
	 *            is the duration of the repetition up movement in ms.
	 * @param downDuration
	 *            is the duration of the repetition down movement in ms.
	 * @param interRepRestDuration
	 *            is the duration of the rest between repetition up and down
	 *            movement in ms.
	 * @param afterRepRestDuration
	 *            is the duration of the rest between two repetitions (after
	 *            each repetition).
	 * @param numRepetitions
	 *            is the number of animation repetitions.
	 */
	public void startAnimation(int fullHeight, int upDuration,
			int downDuration, int interRepRestDuration,
			int afterRepRestDuration, int numRepetitions) {

		// set the animation values
		mExpandedHeight = fullHeight;
		mUpDuration = upDuration;
		mDownDuration = downDuration;
		mTotalRepetitions = numRepetitions;
		mInterRepetitionRestDuration = interRepRestDuration;
		mAfterRepetitionDuration = afterRepRestDuration;

		// initialize the current number of repetitions and the initial height
		// of the animation object.
		mCurrentRepetition = 0;
		initializeAnimationObject();

		// show the animation object and mark the animation as running
		mAnimationObject.setVisibility(View.VISIBLE);
		mAnimationRunning = true;

		initializeAnimations();

		// animate repetition up
		runRepetitionAnimation();
	}

	/**
	 * Cancels the animation. Additionally, remove all the pending animations if
	 * stop was called in the middle of the animation.
	 */
	public void cancelAnimation() {
		clearAnimation();
	}

	/**
	 * Clears the animation and makes it ready for the next one.
	 */
	private void clearAnimation() {
		mAnimationRunning = false;
		mAnimationObject.clearAnimation();
		mUiHandler.removeCallbacks(mAfterRepetitionRestRunnable);
		mUiHandler.removeCallbacks(mInterRepetitionRestRunnable);

		// that's a very very ugly hack to hide the animation on cancel - it
		// seems it works (on cancel in the middle of the repetition animation
		// the animation hung randomly in the middle, leaving the linear layout
		// partially scaled; additionally, the animated object did not went
		// invisible)
		// inspiration for the solution:
		// http://stackoverflow.com/questions/8690029/why-doesnt-setvisibility-work-after-a-view-is-animated
		Animation mAnimationHide = new ScaleAnimation(1f, 1f, 1f, 1f,
				Animation.RELATIVE_TO_SELF, (float) 0.5,
				Animation.RELATIVE_TO_SELF, (float) 0.5);
		mAnimationHide.setFillAfter(false);
		mAnimationHide.setDuration(0);
		mAnimationObject.startAnimation(mAnimationHide);

		mAnimationObject.setVisibility(View.INVISIBLE);
	}

	/**
	 * Notifies all the registered {@link RepetitionAnimationListener} that the
	 * animation has ended.
	 */
	private void notifyRepetitionAnimationEnded() {
		for (RepetitionAnimationListener listener : mRepetitionAnimationListeners) {
			listener.onAnimationEnded();
		}
	}

	/**
	 * Notifies all the registered {@link RepetitionAnimationListener} that the
	 * current repetition was completed.
	 */
	private void notifyRepetitionCompleted() {
		for (RepetitionAnimationListener listener : mRepetitionAnimationListeners) {
			listener.onRepetitionCompleted();
		}
	}

	/**
	 * This method sets the height of the animation object to 1 px.
	 */
	private void initializeAnimationObject() {
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 1);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mAnimationObject.setLayoutParams(lp);
	}

	/**
	 * Initializes the repetition up and down movement animations.
	 */
	private void initializeAnimations() {
		mAnimationUp = new ScaleAnimation(1f, 1f, 1f, 2 * mExpandedHeight,
				Animation.RELATIVE_TO_SELF, (float) 0.5,
				Animation.RELATIVE_TO_SELF, (float) 0.5);
		// set this one to keep the object extended after the animation
		mAnimationUp.setFillAfter(true);
		mAnimationUp.setDuration(mUpDuration);
		mAnimationUp.setAnimationListener(mAnimationUpListener);

		mAnimationDown = new ScaleAnimation(1f, 1f, 2 * mExpandedHeight, 1f,
				Animation.RELATIVE_TO_SELF, (float) 0.5,
				Animation.RELATIVE_TO_SELF, (float) 0.5);
		mAnimationDown.setDuration(mDownDuration);
		mAnimationDown.setAnimationListener(mAnimationDownListener);
	}

	/**
	 * Runs the repetition animation if the current number of repetitions did
	 * not reach the total number of repetitions yet.
	 */
	private void runRepetitionAnimation() {
		if (mCurrentRepetition < mTotalRepetitions) {
			// start the first repetition immediately and delay all the others
			// be the after repetition rest
			mAnimationObject.startAnimation(mAnimationUp);
		} else {
			notifyRepetitionAnimationEnded();

			// clears the animation when over
			clearAnimation();
		}
	}

	/**
	 * This runnable restarts the repetition animation.
	 */
	private Runnable mAfterRepetitionRestRunnable = new Runnable() {
		@Override
		public void run() {
			if (mCurrentRepetition < mTotalRepetitions) {
				notifyRepetitionCompleted();
			}
			
			runRepetitionAnimation();
		}
	};

	/**
	 * This runnable starts the repetition down animation.
	 */
	private Runnable mInterRepetitionRestRunnable = new Runnable() {
		@Override
		public void run() {
			mAnimationObject.startAnimation(mAnimationDown);
		}
	};

	/**
	 * Animation listener for the repetition up animation.
	 */
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
			mUiHandler.postDelayed(mInterRepetitionRestRunnable,
					mInterRepetitionRestDuration);
		}
	};

	/**
	 * Animation listener for the repetition down animation.
	 */
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
			// after each repetition hide the animation object (in this case, if
			// the after repetition animation is long, there won't be any
			// pixels, corresponding to the repetition animation, visible.
			mAnimationObject.setVisibility(View.GONE);
			mCurrentRepetition++;

			mUiHandler.postDelayed(mAfterRepetitionRestRunnable,
					mCurrentRepetition == mTotalRepetitions ? 0
							: mAfterRepetitionDuration);
		}
	};

}
