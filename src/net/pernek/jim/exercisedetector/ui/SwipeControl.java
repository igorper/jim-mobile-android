package net.pernek.jim.exercisedetector.ui;

import net.pernek.jim.exercisedetector.R;
import net.pernek.jim.exercisedetector.util.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html;
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

/** A horizontal swipe control with textual clues.  
 * Notes:
 * - currently colors not exposed through android:ScrollView attrs
 * are obtained directly from colors.xml resource file 
 * (see those with prefix:'status_rect_' 
 * @author Igor
 *
 */
public class SwipeControl extends HorizontalScrollView {
	private static final String TAG = Utils.getApplicationTag();

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
		
	private int mColorDelay = 200;
	
	private int mFontSize;
	private Typeface mTypeface;

	public SwipeControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		
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

		createScrollerLayout(context, screenWidth,
				offScreenElementWidth);
		
		extractXmlAttrs(context.obtainStyledAttributes(attrs, R.styleable.SwipeContol));

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
	
	public void setLeftOffText(String text){
		mLeftOffText = text;
		tvLeftOff.setText(mLeftOffText);
	}
	
	public void setRightOffText(String text){
		mRightOffText = text;
		tvRightOff.setText(mRightOffText);
	}
	
	public void setCenterText(String left, String right){
		mCenterLeftText = left;
		mCenterRightText = right;
		
		// strip first two characters as getColor includes alpha channel, 
		// as opposed to Html.fromHtml 
		String colorLeft = Integer.toHexString(getResources().getColor(R.color.status_rect_center_left_text)).substring(2);
		String colorRight = Integer.toHexString(getResources().getColor(R.color.status_rect_center_right_text)).substring(2);
		
		tvCenter.setText(Html.fromHtml("<font color='#" + colorLeft + "'>" + mCenterLeftText + 
				"</font><font color='#" + colorRight + "'>" + mCenterRightText + "</font>"));
	}
		
	/** This function extracts xml attributes defined for the {@link SwipeControl} class.
	 * @param ta
	 */
	private void extractXmlAttrs(TypedArray ta) {
		// TODO: handle if any of the attributes is missing
		
		// TODO: remove typeface from attrs
		mTypeface = Typeface.createFromAsset(getContext()
				.getAssets(), ta.getString(R.styleable.SwipeContol_typefacePathAssets));
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
	private void createScrollerLayout(Context context, int screenWidth, int offScreenElementWidth) {
		mSwipeLeftBackgroundColor = String.format("#%X", getResources().getColor(R.color.status_rect_left_swipe_background));
		mSwipeRightBackgroundColor = String.format("#%X", getResources().getColor(R.color.status_rect_right_swipe_background));
		
		tvLeftOff = new TextView(context);
		tvLeftOff.setTextSize(20);
		tvLeftOff.setGravity(Gravity.CENTER);
		tvLeftOff.setTypeface(mTypeface);
		tvLeftOff.setTextColor(getResources().getColor(R.color.status_rect_left_off_text));
		tvLeftOff.setLayoutParams(new LinearLayout.LayoutParams(
				offScreenElementWidth, LayoutParams.MATCH_PARENT));
		
		tvCenter = new TextView(context);
		setCenterText("Next: ", "Biceps");
		tvCenter.setTextSize(20);
		tvCenter.setGravity(Gravity.CENTER);
		tvCenter.setTypeface(mTypeface);
		tvCenter.setLayoutParams(new LinearLayout.LayoutParams(screenWidth,
				LayoutParams.MATCH_PARENT));
		
		tvRightOff = new TextView(context);
		tvRightOff.setTextSize(20);
		tvRightOff.setGravity(Gravity.CENTER);
		tvRightOff.setTypeface(mTypeface);
		tvRightOff.setTextColor(getResources().getColor(R.color.status_rect_right_off_text));
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
		
		if (l == 0) {
			// swipe right
			handler.post(new Runnable() {

				@Override
				public void run() {
					setBackgroundColor(Color.parseColor(mSwipeRightBackgroundColor));
					mSwipeDetected = true;
				}
			});
		} else if (l == mScrollerStart * 2) {
			// swipe left
			handler.post(new Runnable() {

				@Override
				public void run() {
					setBackgroundColor(Color.parseColor(mSwipeLeftBackgroundColor));
					mSwipeDetected = true;
				}
			});
		}

		Log.d(TAG, "Scroll: " + Integer.toString(l) + ", full " + Integer.toString(getScreenWidth(getContext())));
	}
}
