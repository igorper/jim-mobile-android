package net.pernek.jim.exercisedetector.alg;

public class ExerciseStateChange {
	
	private ExerciseState mNewState;
	private long mChangeTimestamp;
	
	public static ExerciseStateChange create(ExerciseState newState, long timestamp){
		ExerciseStateChange retVal = new ExerciseStateChange();
		retVal.mNewState = newState;
		retVal.mChangeTimestamp = timestamp;
		
		return retVal;
	}
	
	public ExerciseState getNewState(){
		return mNewState;
	}

	public long getStateChangeTimestamp(){
		return mChangeTimestamp;
	}
}
