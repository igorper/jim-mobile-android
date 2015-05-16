package com.trainerjim.android.entities;

import java.util.Date;

/** Contains information about exercise type.
 * @author Igor
 *
 */
public class ExerciseType {

    /**
     * Local file name extension of exercise type image.
     */
    public static String IMAGE_EXTENSION = ".image";

    public static String IMAGE_DATE_DELIMITER = "_";
	
	/***********************
	 * Fields deserialized from server data;
	 ***********************/
	private int id;
	private String name;
    private String short_name;
    private String medium_image_url;
    private Date image_updated_at;
	
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
    public String getServerImageFileName() { return medium_image_url; }

    /**
     * Gets the local file name of the exercise type image. The file name has an extension appended
     * to allow filtering a directory for exercise images only.
     * @return
     */
    public String getLocalImageFileName() {
        return String.format("%d%s%d%s", getId(), IMAGE_DATE_DELIMITER, getImageUpdatedDate().getTime(), IMAGE_EXTENSION);
    }

    public Date getImageUpdatedDate() { return  image_updated_at; }

    private static String[] splitFileName(String fileName){
        return fileName.replaceFirst("[.][^.]+$", "").split(ExerciseType.IMAGE_DATE_DELIMITER);
    }

    public static int extractIdFromFileName(String fileName){
        return Integer.parseInt(splitFileName(fileName)[0]);
    }

    public static long extractTimestampFromFileName(String fileName){
        return Long.parseLong(splitFileName(fileName)[1]);
    }
}
