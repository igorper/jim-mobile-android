package net.pernek.jim.exercisedetector.alg;

public class SensorValue {
	
	public enum SensorType {
		ACCELEROMETER_BUILTIN
	}
	
	private long mTimestamp;
	
	// object as values can also be null for compressed sensor data
	private Float[] mValues;
	private SensorType mSensorType;
	
	private SensorValue(){}
	
	public static SensorValue create(SensorType sensorType, Float[] values, long timestamp){
		if(sensorType == null || values == null 
				|| values.length == 0 || timestamp < 0){
			throw new IllegalArgumentException("Illegal arguments supplied to SensorValue.");
		}
		// we are now sure our object will contain only legal values so we do not have to
		// check them later
		
		SensorValue retVal = new SensorValue();
		
		retVal.mSensorType = sensorType;
		retVal.mValues = values;
		retVal.mTimestamp = timestamp;
		
		return retVal;
	}
	
	// an array of objects to be able to hold NULL if a sensor value is missing
	public Float[] getValues(){
		return mValues;
	}
	
	public SensorType getSensorType(){
		return mSensorType;
	}
	
	public long getTimestamp(){
		return mTimestamp;
	}
	
	public String getCsvString(){ 
		return mTimestamp + "," +
				(mValues[0] == null ? "" : String.format("%.0f", mValues[0]*10000)) + "," + 
				   (mValues[1] == null ? "" : String.format("%.0f", mValues[1]*10000)) + "," + 
				   (mValues[2] == null ? "" : String.format("%.0f", mValues[2]*10000)); 
	}
	
	public String getFullCsvString(){ 
		return mTimestamp + "," +
				(mValues[0] == null ? "" : String.format("%f", mValues[0])) + "," + 
				   (mValues[1] == null ? "" : String.format("%f", mValues[1])) + "," + 
				   (mValues[2] == null ? "" : String.format("%f", mValues[2])); 
	}
	
	@Override
	public String toString() {
		return String.format("X:%.2f,Y:%.2f,Z:%.2f,T:%d", mValues[0], mValues[1], mValues[2], mTimestamp);
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
		
		SensorValue sv = (SensorValue)obj;
		
		// check sensor values array has length
		if(sv.mValues.length != mValues.length){
			return false;
		}
		
		// check sensor values array values
		for(int i=0; i < mValues.length; i++){
			// both values are null, which is legal
			if(sv.mValues[i] == null && mValues[i] == null){
				continue;
			}
			
			// if just one of the values is null or if they are different
			// return not equal
			if((sv.mValues[i] == null && mValues[i] != null) ||
			   (sv.mValues[i] != null && mValues[i] == null) ||	
				!(Float.compare(sv.mValues[i], mValues[i]) == 0)) {
				return false;
			}
		}
		
		// check sensor type and timestamp
		return  sv.mSensorType == mSensorType && 
				sv.mTimestamp == mTimestamp;
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + mSensorType.hashCode();
		result = 31 * result + (int)(mTimestamp^(mTimestamp>>>32));
		for(int i=0; i < mValues.length; i++) {
			if(mValues[i] != null){
				result = 31 * result + Float.floatToIntBits(mValues[i]);
			}
		}
		
		return result;
	}
}
