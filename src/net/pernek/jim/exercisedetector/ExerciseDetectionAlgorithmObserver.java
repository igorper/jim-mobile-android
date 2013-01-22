package net.pernek.jim.exercisedetector;

public interface ExerciseDetectionAlgorithmObserver {
	
	void exerciseStateChanged(ExerciseState newState);
}
