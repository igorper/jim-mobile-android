package net.pernek.jim.exercisedetector.ui;

import net.pernek.jim.exercisedetector.util.Utils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SwipeControl extends HorizontalScrollView {
	private static final String TAG = Utils.getApplicationTag();

	private Handler handler;

	private int mScrollerStart;
	private int mScrollerWidth;

	private boolean mSwipeEnded = true;
	private boolean mSwipeDetected = false;
	
	private String mSwipeLeftColor = "#ff0080d0";
	private String mSwipeRightColor = "#ffe1563e";
	
	private int mColorDelay = 200;

	public SwipeControl(Context context, AttributeSet attrs) {
		super(context, attrs);

		handler = new Handler();

		int screenWidth = getScreenWidth(context);
		int offScreenElementWidth = screenWidth / 3 * 2;
		
		mScrollerStart = offScreenElementWidth;

		mScrollerWidth = createScrollerLayout(screenWidth,
				offScreenElementWidth);

		getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
						// scroll to initial position
						scrollTo(mScrollerStart, 0);
					}
				});
		setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mSwipeEnded = false;
				} else if (mSwipeDetected
						|| event.getAction() == MotionEvent.ACTION_UP
						|| event.getAction() == MotionEvent.ACTION_CANCEL) {
					// scroll to initial position
					smoothScrollTo(mScrollerStart, 0);
					postDelayed(new Runnable() {
						
						@Override
						public void run() {
							setBackgroundColor(Color.parseColor("#00ff00"));
							
						}
					}, mColorDelay);
					
					mSwipeDetected = false;
					mSwipeEnded = true;
					return true;
				}

				return mSwipeEnded;
			}
		});
	}

	private int getScreenWidth(Context context) {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		int screenWidth = displaymetrics.widthPixels;
		return screenWidth;
	}

	/**
	 * Adds three TextViews to the scroller and returns the scroll width.
	 * 
	 * @param screenWidth
	 *            is the width of the central TextView.
	 * @param offScreenElementWidth
	 *            is the width of left and right off screen TextViews.
	 * @return the scroll width.
	 */
	private int createScrollerLayout(int screenWidth, int offScreenElementWidth) {
		TextView tvLeft = new TextView(getContext());
		tvLeft.setText("TLEVI");
		tvLeft.setTextSize(20);
		tvLeft.setGravity(Gravity.CENTER);
		tvLeft.setLayoutParams(new LinearLayout.LayoutParams(
				offScreenElementWidth, LayoutParams.MATCH_PARENT));
		TextView tvCenter = new TextView(getContext());
		tvCenter.setText("TCENTER");
		tvCenter.setTextSize(20);
		tvCenter.setGravity(Gravity.CENTER);
		tvCenter.setLayoutParams(new LinearLayout.LayoutParams(screenWidth,
				LayoutParams.MATCH_PARENT));
		TextView tvRight = new TextView(getContext());
		tvRight.setText("RIGHT");
		tvRight.setTextSize(20);
		tvRight.setGravity(Gravity.CENTER);
		tvRight.setLayoutParams(new LinearLayout.LayoutParams(
				offScreenElementWidth, LayoutParams.MATCH_PARENT));

		LinearLayout scrollHost = new LinearLayout(getContext());
		scrollHost.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		scrollHost.addView(tvLeft);
		scrollHost.addView(tvCenter);
		scrollHost.addView(tvRight);

		addView(scrollHost);

		// return total scroller width
		return screenWidth;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		
		if (l == 0) {
			// swipe right

			handler.post(new Runnable() {

				@Override
				public void run() {
					setBackgroundColor(Color.parseColor(mSwipeRightColor));
					mSwipeDetected = true;
				}
			});
		} else if (l == mScrollerStart * 2) {
			// swipe left
			handler.post(new Runnable() {

				@Override
				public void run() {
					setBackgroundColor(Color.parseColor(mSwipeLeftColor));
					mSwipeDetected = true;
				}
			});
		}

		Log.d(TAG, "Scroll: " + Integer.toString(l) + ", full " + Integer.toString(getScreenWidth(getContext())));
	}
}
