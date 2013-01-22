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

	@Override
	public void push(SensorValue sensorValue) {
		if(sensorValue.getTimestamp() % mSamplingInterval == 0){
			mLastSensorValue = sensorValue;
			
			notifySensorInterpolatorObservers(sensorValue);
			return;
		}
		
		if(mLastSensorValue == null){
			mLastSensorValue = sensorValue;
		}
		else {
			int countLast = (int)Math.floor(mLastSensorValue.getTimestamp() / mSamplingInterval);
			int countCurrent = (int)Math.floor(sensorValue.getTimestamp() / mSamplingInterval);
			
			int valuesToCaluclate = countCurrent - countLast;
			
			long x1 = mLastSensorValue.getTimestamp();
			long x2 = sensorValue.getTimestamp();

			// get the first x to calculate
			int x = (int)Math.ceil((double)mLastSensorValue.getTimestamp() / (double)mSamplingInterval) * mSamplingInterval;
			while(valuesToCaluclate > 0){
				float[] currentValues = sensorValue.getValues();
				float[] lastValues = mLastSensorValue.getValues();
				float[] newValues = new float[lastValues.length];
				for(int i = 0; i < lastValues.length; i++){
					newValues[i] = lastValues[i] + (currentValues[i] - lastValues[i]) * 
							(x - mLastSensorValue.getTimestamp()) / (sensorValue.getTimestamp() - mLastSensorValue.getTimestamp());
				}
				
				
				SensorValue newValue = SensorValue.create(sensorValue.getSensorType(), newValues, x);
				notifySensorInterpolatorObservers(newValue);
				x += mSamplingInterval;
				valuesToCaluclate--;
			}
			
		}		
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
