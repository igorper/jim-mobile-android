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
    private String short_name;
    private String medium_image_url;
	
	/**
     * Gets the exercise type ID.
	 * @return
	 */
	public int getId(){
		return id;
	}

    /**
     * Gets the exercise type name.
     * @return
     */
    public String getName(){
        return name;
    }
	
	/**
     * Gets the exercise type short name.
	 * @return
	 */
	public String getShortName(){
		return short_name;
	}

    /**
     * Gets the exercise type image url on the server (relative to server root).
     * @return
     */
    public String getImageUrl() { return medium_image_url; }
}
