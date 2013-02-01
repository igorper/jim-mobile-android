package net.pernek.jim.exercisedetector;

import net.pernek.jim.common.SensorValue;

public interface SensorInterpolator {
		
	void push(SensorValue sensorValue);
	
	void addSensorInterpolatorObserver(SensorInterpolatorObserver observer);
	
	void removeSensorInterpolatorObserver(SensorInterpolatorObserver observer);
}
