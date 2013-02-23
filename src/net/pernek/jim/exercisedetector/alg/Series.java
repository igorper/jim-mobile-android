package net.pernek.jim.exercisedetector.alg;

public class Series {
	private int mNumRepetitions;
	private int mWeight;
	
	private Series() {}
	
	public static Series create(int numRepetitions, int weight){
		Series retVal = new Series();
		retVal.mNumRepetitions = numRepetitions;
		retVal.mWeight = weight;
		
		return retVal;
	}
	
	public void setNumRepetitions(int numRepetitions){
		mNumRepetitions = numRepetitions;
	}
	
	public void setWeight(int weight){
		mWeight = weight;
	}
	
	public int getNumRepetitions() {
		return mNumRepetitions;
	}
	
	public int getWeight() {
		return mWeight;
	}
}
