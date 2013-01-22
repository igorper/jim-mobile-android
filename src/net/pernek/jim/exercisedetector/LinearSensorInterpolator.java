package net.pernek.jim.exercisedetector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LinearSensorInterpolator implements SensorInterpolator {
	
	private List<SensorInterpolatorObserver> mInterpolatorObservers = new ArrayList<SensorInterpolatorObserver>();
	
	private int mSamplingInterval;
	private SensorValue mLastSensorValue;
	
	private LinearSensorInterpolator(){}

	public static SensorInterpolator create(int msSamplingInterval) {

		LinearSensorInterpolator retVal = new LinearSensorInterpolator();
		retVal.mSamplingInterval = msSamplingInterval;

		return retVal;
	}

	// very simple implementation, interpolates desired values based on its 1NN, other values are simply dropped
	@Override
	public void push(SensorValue sensorValue) {
		if(mLastSensorValue != null){
			// get the first x to calculate
			long x = (int)Math.ceil((double)mLastSensorValue.getTimestamp() / (double)mSamplingInterval) * mSamplingInterval;
			if(x == mLastSensorValue.getTimestamp()){
				x += mSamplingInterval;
			}
			
			while(x <= sensorValue.getTimestamp()){
				int numValues = mLastSensorValue.getValues().length;
				float[] newValues = new float[numValues];
				for(int i = 0; i < numValues; i++){
					newValues[i] = mLastSensorValue.getValues()[i] + (sensorValue.getValues()[i] - mLastSensorValue.getValues()[i]) * 
							(x - mLastSensorValue.getTimestamp()) / (sensorValue.getTimestamp() - mLastSensorValue.getTimestamp());
				}
				
				SensorValue newValue = SensorValue.create(sensorValue.getSensorType(), newValues, x);
				notifySensorInterpolatorObservers(newValue);
				x += mSamplingInterval;
			}
		}	
		
		mLastSensorValue = sensorValue;
	}

	@Override
	public void addSensorInterpolatorObserver(
			SensorInterpolatorObserver observer) {
		
		if(!mInterpolatorObservers.contains(observer)){
			mInterpolatorObservers.add(observer);
		}
		
	}

	@Override
	public void removeSensorInterpolatorObserver(
			SensorInterpolatorObserver observer) {
		
		if(mInterpolatorObservers.contains(observer)){
			mInterpolatorObservers.remove(observer);
		}
		
	}
	
	private void notifySensorInterpolatorObservers(SensorValue newSensorValue){
		for (SensorInterpolatorObserver observer : mInterpolatorObservers) {
			observer.onNewValue(newSensorValue);
		}
	}
	// TODO additionally a callback to which different listeners could register and do whatever they want with the data
	// (visualize it, process it, write it to a file, etc.)

}
