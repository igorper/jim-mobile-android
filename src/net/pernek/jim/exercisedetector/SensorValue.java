package net.pernek.jim.exercisedetector;

public class SensorValue {
	
	public enum SensorType {
		ACCELEROMETER_BUILTIN
	}
	
	private long timestamp;
	private float[] values;
	private SensorType sensorType;
	
	private SensorValue(){}
	
	public static SensorValue create(SensorType sensorType, float[] values, long timestamp){
		if(sensorType == null || values == null 
				|| values.length == 0 || timestamp < 0){
			throw new IllegalArgumentException("Illegal arguments supplied to SensorValue.");
		}
		// we are now sure our object will contain only legal values so we do not have to
		// check them later
		
		SensorValue retVal = new SensorValue();
		
		retVal.sensorType = sensorType;
		retVal.values = values;
		retVal.timestamp = timestamp;
		
		return retVal;
	}
	
	public float[] getValues(){
		return values;
	}
	
	public SensorType getSensorType(){
		return sensorType;
	}
	
	public long getTimestamp(){
		return timestamp;
	}
}
