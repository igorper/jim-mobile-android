package com.trainerjim.android.ui;

/** Implement this class if you need to listen to {@code SwipeControl} events.
 * @author Igor
 *
 */
public interface SwipeListener {
	
	/**
	 * Triggered on swipe left event.
	 */
	public void onSwipeLeft(); 
	
	/**
	 * Triggered on swipe right event.
	 */
	public void onSwipeRight();
}
