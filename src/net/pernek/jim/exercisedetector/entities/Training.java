package net.pernek.jim.exercisedetector.entities;

import java.util.Collections;
import java.util.List;

public class Training {

	private int id;
	
	private String name;
	
	private List<Exercise> exercises;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public List<Exercise> getExercises(){
		return Collections.unmodifiableList(exercises);
	}
}
