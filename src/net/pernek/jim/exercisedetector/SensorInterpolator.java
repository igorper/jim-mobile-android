package net.pernek.jim.exercisedetector;

public interface SensorInterpolator {
		
	void push(SensorValue sensorValue);
	
	void addSensorInterpolatorObserver(SensorInterpolatorObserver observer);
	
	void removeSensorInterpolatorObserver(SensorInterpolatorObserver observer);
}
