package net.pernek.jim.exercisedetector;

import net.pernek.jim.common.SensorValue;

public interface SensorInterpolatorObserver {
	
	void onNewValue(SensorValue newValue);

}
