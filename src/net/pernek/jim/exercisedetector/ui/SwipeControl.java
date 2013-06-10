package net.pernek.jim.exercisedetector.ui;

import net.pernek.jim.exercisedetector.R;
import net.pernek.jim.exercisedetector.util.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
	private static String DEFAULT_COLOR_MISSING = "#ff55bbaa";

	private Handler handler;

	private int mScrollerStart;

	private boolean mSwipeEnded = true;
	private boolean mSwipeDetected = false;
	
	private String mSwipeLeftColor;
	private String mSwipeRightColor;
	private Drawable mBackgroundDrawable;
	
	private int mColorDelay = 200;
	
	private int mFontSize;

	public SwipeControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		extractXmlAttrs(context.obtainStyledAttributes(attrs, R.styleable.SwipeContol));
		
		// background drawable will be used to change back the background color
		// after the swipe gesture
		mBackgroundDrawable = getBackground();

		// ui thread handler, used to post and schedule all visual changes
		handler = new Handler();

		// get the dimensions to define the layout of the contol
		int screenWidth = getScreenWidth(context);
		int offScreenElementWidth = screenWidth / 3 * 2;

		// this is the initial scroller position (we will always scroll
		// the scroller to this position)
		mScrollerStart = offScreenElementWidth;

		createScrollerLayout(screenWidth,
				offScreenElementWidth);

		getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
						
						// scroll to initial position at the beginning
						scrollTo(mScrollerStart, 0);
					}
				});
		setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// using this allows us to stop reporting touch
					// events after a swipe has ended, and resume at
					// the beginning of each swipe
					mSwipeEnded = false;
				} else if (mSwipeDetected
						|| event.getAction() == MotionEvent.ACTION_UP
						|| event.getAction() == MotionEvent.ACTION_CANCEL) {
					// scroll to initial position after the swipe or on
					// swipe release/cancel
					smoothScrollTo(mScrollerStart, 0);
					
					// change back the control color after the swipe (with a delay)
					postDelayed(new Runnable() {
						
						@Override
						public void run() {
							setBackgroundDrawable(mBackgroundDrawable);
							
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

	
	/** This function extracts xml attributes defined for the {@link SwipeControl} class.
	 * @param ta
	 */
	private void extractXmlAttrs(TypedArray ta) {
		mSwipeLeftColor = ta.getString(R.styleable.SwipeContol_swipeLeftColor);
		mSwipeRightColor = ta.getString(R.styleable.SwipeContol_swipeRightColor);
		
		ta.recycle();
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
	 */
	private void createScrollerLayout(int screenWidth, int offScreenElementWidth) {
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
