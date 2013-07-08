package com.trainerjim.android.entities;

/** Contains information about exercise type.
 * @author Igor
 *
 */
public class ExerciseType {
	
	/***********************
	 * Fields deserialized from server data;
	 ***********************/
	private int id;
	private String name;
	
	/** Gets the exercise type ID.
	 * @return
	 */
	public int getId(){
		return id;
	}
	
	/** Gets the exercise type name.
	 * @return
	 */
	public String getName(){
		return name;
	}
}
