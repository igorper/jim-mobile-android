package net.pernek.jim.exercisedetector.entities;

public class SeriesExecution {
	
	/**
	 * Those two mark the bounding timestamps of the acceleration signal. 
	 */
	public long start_timestamp;
	public long end_timestamp;
	
	public int exercise_type_id;
	public int num_repetitions;
	public int weight;
	public int rest_time;
	private int duration;
	public int rating;
	
	public int getDuration(){
		return duration;
	}
	
	public void setDuration(int value){
		duration = value;
	}
}
