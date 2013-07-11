package com.trainerjim.android.ui;

import java.util.ArrayList;
import java.util.List;

import net.pernek.jim.exercisedetector.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trainerjim.android.util.Utils;

/**
 * A horizontal swipe control with textual clues. Notes: - currently colors are
 * not exposed through android:ScrollView attrs and are obtained directly from
 * colors.xml resource file (see those with prefix:'status_rect_'
 * 
 * @author Igor
 * 
 */
public class SwipeControl extends HorizontalScrollView {
	private static final String TAG = Utils.getApplicationTag();

	private static final String SWIPE_LEFT = "swipe_left";
	private static final String SWIPE_RIGHT = "swipe_right";

	private Handler handler;

	private int mScrollerStart;

	private boolean mSwipeEnded = true;
	private boolean mSwipeDetected = false;

	private String mSwipeLeftBackgroundColor;
	private String mSwipeRightBackgroundColor;

	private Drawable mBackgroundDrawable;
	private String mLeftOffText;
	private String mRightOffText;
	private String mCenterLeftText;
	private String mCenterRightText;
	private TextView tvLeftOff;
	private TextView tvCenter;
	private TextView tvRightOff;
	private boolean mIsSwipeEnabled;

	private List<SwipeListener> mSwipeListeners = new ArrayList<SwipeListener>();

	private int mColorDelay = 200;

	private Typeface mTypeface;

	public SwipeControl(Context context, AttributeSet attrs) {
		super(context, attrs);

		// the swipe gesture is enabled by default
		mIsSwipeEnabled = true;

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

		createScrollerLayout(context, screenWidth, offScreenElementWidth);

		extractXmlAttrs(context.obtainStyledAttributes(attrs,
				R.styleable.SwipeContol));

		setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// if swipe was disabled and there is no detected swipe pending
				// stop any further processing
				if (!mIsSwipeEnabled && !mSwipeDetected) {
					return true;
				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// using this allows us to stop reporting touch
					// events after a swipe has ended, and resume at
					// the beginning of each swipe
					mSwipeEnded = false;
				} else if (mSwipeDetected
						|| event.getAction() == MotionEvent.ACTION_UP
						|| event.getAction() == MotionEvent.ACTION_CANCEL) {

					// change back the control color after the swipe (with a
					// delay)
					postDelayed(new Runnable() {

						@Override
						public void run() {
							setBackgroundDrawable(mBackgroundDrawable);

						}
					}, mColorDelay);

					mSwipeDetected = false;
					mSwipeEnded = true;
				}

				return mSwipeEnded;
			}
		});
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);

		// scroll to initial position on view init, after the swipe or on
		// swipe release/cancel
		smoothScrollTo(mScrollerStart, 0);
	}

	/**
	 * Sets if the swipe gesture can be used or not (by default the swipe
	 * gesture is enabled).
	 * 
	 * @param value
	 */
	public void setSwipeEnabled(boolean value) {
		mIsSwipeEnabled = value;
	}

	public boolean getSwipeEnabled() {
		return mIsSwipeEnabled;
	}

	public void addSwipeListener(SwipeListener listener) {
		mSwipeListeners.add(listener);
	}

	public void removeSwipeListener(SwipeListener listener) {
		mSwipeListeners.remove(listener);
	}

	public void setLeftOffText(String text) {
		mLeftOffText = text;
		tvLeftOff.setText(mLeftOffText);
	}

	public void setRightOffText(String text) {
		mRightOffText = text;
		tvRightOff.setText(mRightOffText);
	}

	public void setCenterText(String left, String right) {
		mCenterLeftText = left;
		mCenterRightText = right;

		// strip first two characters as getColor includes alpha channel,
		// as opposed to Html.fromHtml
		String colorLeft = Integer.toHexString(
				getResources().getColor(R.color.status_rect_center_left_text))
				.substring(2);
		String colorRight = Integer.toHexString(
				getResources().getColor(R.color.status_rect_center_right_text))
				.substring(2);

		tvCenter.setText(Html.fromHtml("<font color='#" + colorLeft + "'>"
				+ mCenterLeftText + "</font><font color='#" + colorRight + "'>"
				+ mCenterRightText + "</font>"));
	}

	private void onSwipeLeft() {
		for (SwipeListener listener : mSwipeListeners) {
			listener.onSwipeLeft();
		}
	}

	private void onSwipeRight() {
		for (SwipeListener listener : mSwipeListeners) {
			listener.onSwipeRight();
		}
	}

	/**
	 * This function extracts xml attributes defined for the
	 * {@link SwipeControl} class.
	 * 
	 * @param ta
	 */
	private void extractXmlAttrs(TypedArray ta) {
		// TODO: handle if any of the attributes is missing

		// TODO: remove typeface from attrs
		mTypeface = Typeface.createFromAsset(getContext().getAssets(),
				ta.getString(R.styleable.SwipeContol_typefacePathAssets));
		setLeftOffText(ta.getString(R.styleable.SwipeContol_textLeftOff));
		setRightOffText(ta.getString(R.styleable.SwipeContol_textRightOff));

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
	private void createScrollerLayout(Context context, int screenWidth,
			int offScreenElementWidth) {
		mSwipeLeftBackgroundColor = String.format("#%X", getResources()
				.getColor(R.color.status_rect_left_swipe_background));
		mSwipeRightBackgroundColor = String.format("#%X", getResources()
				.getColor(R.color.status_rect_right_swipe_background));

		tvLeftOff = new TextView(context);
		tvLeftOff.setTextSize(getResources().getDimension(
				R.dimen.swipe_control_text_size));
		tvLeftOff.setGravity(Gravity.CENTER);
		tvLeftOff.setTypeface(mTypeface);
		tvLeftOff.setTextColor(getResources().getColor(
				R.color.status_rect_left_off_text));
		tvLeftOff.setLayoutParams(new LinearLayout.LayoutParams(
				offScreenElementWidth, LayoutParams.MATCH_PARENT));

		tvCenter = new TextView(context);
		setCenterText("Next: ", "Biceps");
		tvCenter.setTextSize(getResources().getDimension(
				R.dimen.swipe_control_text_size));
		tvCenter.setGravity(Gravity.CENTER);
		tvCenter.setTypeface(mTypeface);
		tvCenter.setLayoutParams(new LinearLayout.LayoutParams(screenWidth,
				LayoutParams.MATCH_PARENT));

		tvRightOff = new TextView(context);
		tvRightOff.setTextSize(getResources().getDimension(
				R.dimen.swipe_control_text_size));
		tvRightOff.setGravity(Gravity.CENTER);
		tvRightOff.setTypeface(mTypeface);
		tvRightOff.setTextColor(getResources().getColor(
				R.color.status_rect_right_off_text));
		tvRightOff.setLayoutParams(new LinearLayout.LayoutParams(
				offScreenElementWidth, LayoutParams.MATCH_PARENT));

		LinearLayout scrollHost = new LinearLayout(context);
		scrollHost.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		scrollHost.addView(tvLeftOff);
		scrollHost.addView(tvCenter);
		scrollHost.addView(tvRightOff);

		addView(scrollHost);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);

		if (l == 0 && !handler.hasMessages(0, SWIPE_RIGHT) && !mSwipeDetected) {
			// swipe right
			handler.postAtTime(new Runnable() {

				@Override
				public void run() {
					// report event on UI thread
					onSwipeRight();

					setBackgroundColor(Color
							.parseColor(mSwipeRightBackgroundColor));
					mSwipeDetected = true;
				}
			}, SWIPE_RIGHT, SystemClock.uptimeMillis());
		} else if (l == mScrollerStart * 2
				&& !handler.hasMessages(0, SWIPE_LEFT) && !mSwipeDetected) {
			// swipe left
			handler.postAtTime(new Runnable() {

				@Override
				public void run() {

					// report event on UI thread
					onSwipeLeft();

					setBackgroundColor(Color
							.parseColor(mSwipeLeftBackgroundColor));
					mSwipeDetected = true;
				}
			}, SWIPE_LEFT, SystemClock.uptimeMillis());
		}

//		Log.d(TAG,
//				"Scroll: " + Integer.toString(l) + ", full "
//						+ Integer.toString(getScreenWidth(getContext())));
	}
}
