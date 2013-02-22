package net.pernek.jim.exercisedetector.alg;

import java.util.ArrayList;
import java.util.List;

public class Exercise {
	
	private String mName;
	
	private List<Series> mSeries;
	
	private Exercise(){}
	
	public static Exercise create(String name){
		Exercise retVal = new Exercise();
		retVal.mName = name;
		retVal.mSeries = new ArrayList<Series>();
		
		return retVal;
	}
	
	public void addSeries(Series newSeries){
		mSeries.add(newSeries);
	}
	
	public String getName(){
		return mName;
	}
	
	public List<Series> getSeries(){
		return mSeries;
	}

}
