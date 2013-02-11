package net.pernek.jim.exercisedetector.alg;

import java.util.List;

public interface ExerciseDetectionAlgorithm {
	
	void push(SensorValue newValue);
	
	List<Boolean> getDetectedExercises();
	
	List<Long> getTimestamps();
	
	void addExerciseDetectionListener(ExerciseDetectionAlgorithmListener listener);
	
	void removeExerciseDetectionListener(ExerciseDetectionAlgorithmListener listener);
}
