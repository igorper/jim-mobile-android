package net.pernek.jim.exercisedetector;

import java.util.List;

import net.pernek.jim.common.SensorValue;

public interface ExerciseDetectionAlgorithm {
	
	void push(SensorValue newValue);
	
	List<Boolean> getDetectedExercises();
	
	List<Long> getTimestamps();
	
	void addExerciseDetectionObserver(ExerciseDetectionAlgorithmObserver observer);
	
	void removeExerciseDetectionObserver(ExerciseDetectionAlgorithmObserver observer);
}
