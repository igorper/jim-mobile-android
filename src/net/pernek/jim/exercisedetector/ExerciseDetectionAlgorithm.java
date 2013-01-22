package net.pernek.jim.exercisedetector;

import java.util.List;

public interface ExerciseDetectionAlgorithm {
	
	void push(SensorValue newValue);
	
	List<Boolean> getDetectedExercises();
	
	List<Long> getTimestamps();
	
	void addExerciseDetectionObserver(ExerciseDetectionAlgorithmObserver observer);
	
	void removeExerciseDetectionObserver(ExerciseDetectionAlgorithmObserver observer);
}
