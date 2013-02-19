package net.pernek.jim.exercisedetector.alg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class LinearSensorInterpolator implements SensorInterpolator {
	
	private List<SensorInterpolatorListener> mInterpolatorObservers = new ArrayList<SensorInterpolatorListener>();
	
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
			long x = mLastSensorValue.getTimestamp() / mSamplingInterval * mSamplingInterval;
			if(x == mLastSensorValue.getTimestamp()){
				x += mSamplingInterval;
			}
			
			if(x < 0){
				long lt = mLastSensorValue.getTimestamp();
				long sp = lt/ mSamplingInterval;
				long mp = sp * mSamplingInterval;
				//long x = (int)Math.ceil((double)mLastSensorValue.getTimestamp() / (double)mSamplingInterval) * mSamplingInterval;
				int br=0;
				br++;
			}
			
			while(x <= sensorValue.getTimestamp()){
				int numValues = mLastSensorValue.getValues().length;
				Float[] newValues = new Float[numValues];
				for(int i = 0; i < numValues; i++){
					newValues[i] = mLastSensorValue.getValues()[i] + (sensorValue.getValues()[i] - mLastSensorValue.getValues()[i]) * 
							(x - mLastSensorValue.getTimestamp()) / (sensorValue.getTimestamp() - mLastSensorValue.getTimestamp());
				}
				
				SensorValue newValue = SensorValue.create(sensorValue.getSensorType(), newValues, x);
				notifySensorInterpolatorObservers(newValue);
				x += mSamplingInterval;
			}
		}	
		
		SensorValue.storeToReuse(mLastSensorValue);
		mLastSensorValue = sensorValue;
	}

	@Override
	public void addSensorInterpolatorListener(
			SensorInterpolatorListener listener) {
		
		if(!mInterpolatorObservers.contains(listener)){
			mInterpolatorObservers.add(listener);
		}
		
	}

	@Override
	public void removeSensorInterpolatorListener(
			SensorInterpolatorListener listener) {
		
		if(mInterpolatorObservers.contains(listener)){
			mInterpolatorObservers.remove(listener);
		}
		
	}
	
	private void notifySensorInterpolatorObservers(SensorValue newSensorValue){
		for (SensorInterpolatorListener observer : mInterpolatorObservers) {
			observer.onNewInterpolatedValue(newSensorValue);
		}
	}
	// TODO additionally a callback to which different listeners could register and do whatever they want with the data
	// (visualize it, process it, write it to a file, etc.)

}
