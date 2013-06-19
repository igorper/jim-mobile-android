package net.pernek.jim.exercisedetector.entities;

import java.util.Collections;
import java.util.List;

public class Exercise {
	
	private int id;
	
	private int order;
	
	private ExerciseType exercise_type;
	
	private List<Series> series;
	
	public int getId(){
		return id;
	}
	
	public int getOrder(){
		return order;
	}
	
	public ExerciseType getExerciseType(){
		return exercise_type;
	}
	
	public List<Series> getSeries(){
		return Collections.unmodifiableList(series);
	}
}
