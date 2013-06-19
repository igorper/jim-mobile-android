package net.pernek.jim.exercisedetector.entities;

public class Series {
	
	private int id;
	
	private int repeat_count;
	
	private int rest_time;
	
	private int weight;
	
	public int getId(){
		return id;
	}
	
	public int getNumberRepetitions(){
		return repeat_count;
	}
	
	public int getRestTime(){
		return rest_time;
	}

	public int getWeight(){
		return weight;
	}
}
