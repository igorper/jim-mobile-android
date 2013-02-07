package net.pernek.jim.exercisedetector.alg;


public interface SensorInterpolator {
		
	void push(SensorValue sensorValue);
	
	void addSensorInterpolatorListener(SensorInterpolatorListener listener);
	
	void removeSensorInterpolatorListener(SensorInterpolatorListener listener);
}
