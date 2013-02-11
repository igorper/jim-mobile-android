package net.pernek.jim.exercisedetector.alg;

public class ExerciseStateChange {
	
	private ExerciseState mNewState;
	private long mChangeTimestamp;
	
	private ExerciseStateChange() {}
	
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
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		
		if(obj == this){
			return true;
		}
		
		if(obj.getClass() != getClass()){
			return false;
		}
		
		ExerciseStateChange esc = (ExerciseStateChange)obj;
		
		return esc.mNewState == mNewState && esc.mChangeTimestamp == mChangeTimestamp;
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + mNewState.hashCode();
		result = 31 * result + (int)(mChangeTimestamp^(mChangeTimestamp>>>32));
		
		return result;
	}
}
