package net.pernek.jim.exercisedetector.alg;

public class DetectedEvent {
	
	private boolean mIsExercise;
	private int mTimestamp;
	
	public DetectedEvent(boolean isExercise, int timestamp){
		mIsExercise = isExercise;
		mTimestamp = timestamp;
	}
	
	public boolean isExercise(){
		return mIsExercise;
	}
	
	public int getTimestamp(){
		return mTimestamp;
	}

}
