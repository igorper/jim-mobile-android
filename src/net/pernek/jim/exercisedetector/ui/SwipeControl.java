package net.pernek.jim.exercisedetector.ui;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnTouchListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SwipeControl extends HorizontalScrollView {

	private int mScrollerStart;

	public SwipeControl(Context context, AttributeSet attrs) {
		super(context, attrs);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		int screenWidth = displaymetrics.widthPixels;
		int offScreenElementWidth = screenWidth / 2; 
		
		mScrollerStart = offScreenElementWidth;

		createSliderElements(screenWidth, offScreenElementWidth);
		
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
			   @Override
			   public void onGlobalLayout(){
			      getViewTreeObserver().removeGlobalOnLayoutListener(this);
			      scrollTo(mScrollerStart, 0);
			   }
			});
		setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP
						|| event.getAction() == MotionEvent.ACTION_CANCEL) {
					smoothScrollTo(mScrollerStart, 0);
					return true;
				}

				return false;
			}
		});
	}
	
	private void createSliderElements(int screenWidth, int offScreenElementWidth){
		TextView tvLeft = new TextView(getContext());
		tvLeft.setText("TLEVI");
		tvLeft.setLayoutParams(new LinearLayout.LayoutParams(offScreenElementWidth, LayoutParams.MATCH_PARENT));
		TextView tvCenter = new TextView(getContext());
		tvCenter.setText("TCENTER");
		tvCenter.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, LayoutParams.MATCH_PARENT));
		TextView tvRight = new TextView(getContext());
		tvRight.setText("RIGHT");
		tvRight.setLayoutParams(new LinearLayout.LayoutParams(offScreenElementWidth, LayoutParams.MATCH_PARENT));

		LinearLayout scrollHost = new LinearLayout(getContext());
		scrollHost.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		scrollHost.addView(tvLeft);
		scrollHost.addView(tvCenter);
		scrollHost.addView(tvRight);

		addView(scrollHost);
	}

}
