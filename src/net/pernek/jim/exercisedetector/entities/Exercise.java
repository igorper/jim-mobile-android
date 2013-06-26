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
	
	public int getAllSeriesCount(){
		return series.size();
	}
	
	public int getSeriesLeftCount(){
		return mSeriesToDo.size();
	}
	
//	
//	public List<Series> getSeries(){
//		return Collections.unmodifiableList(series);
//	}
	
	public void initializeExercise(){
		mSeriesToDo = new ArrayList<Integer>();
		
		for(int i=0; i < series.size(); i++){
			mSeriesToDo.add(i);
		}
	}
	
	public Series getCurrentSeries() {
		return mSeriesToDo.size() == 0 ? null : series.get(mSeriesToDo.get(0));
	}
	
	/** Returns {@value true } if this exercise contains some more series.
	 * @return
	 */
	public boolean moveToNextSeries(){
		if(mSeriesToDo.size() == 0){
			return false;
		}
		
		mSeriesToDo.remove(0);
		
		return mSeriesToDo.size() > 0;
	}
	
	public int getCurrentSeriesNumber(){
		return mSeriesToDo.get(0) + 1;
	}
}
