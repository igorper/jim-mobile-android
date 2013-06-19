package net.pernek.jim.exercisedetector.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Exercise {
	
	private int id;
	
	private int order;
	
	private ExerciseType exercise_type;
	
	private List<Series> series;
	
	private List<Integer> mSeriesToDo;
	
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
	
	public void initializeExercise(){
		mSeriesToDo = new ArrayList<Integer>();
		
		for(int i=0; i < series.size(); i++){
			mSeriesToDo.add(i);
		}
	}
	
	public int getCurrentSeriesId(){
		return mSeriesToDo.size() == 0 ? -1 : mSeriesToDo.get(0);
	}
}
