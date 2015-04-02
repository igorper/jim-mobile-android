package com.trainerjim.android.entities;

/** Contains different sizes images paths for exercise type..
 * @author Igor
 *
 */
public class ExerciseTypeImagesItem {

    /***********************
     * Fields deserialized from server data;
     ***********************/
    private int id;
    private String name;
    private String short_name;
    private String image_file_name;
    private String image_url;
    private String thumb_image_url;
    private String medium_image_url;
    private String large_image_url;

    public ExerciseTypeImagesItem() {}

    /**
     * Gets the exercise short name.
     * @return
     */
    public String getShortName() { return short_name; }

    /**
     * Gets the exercise's medium size image url.
     * @return
     */
    public String getMediumImageUrl(){ return medium_image_url; }

    /**
     * Gets the exercise type id.
     * @return
     */
    public int getId() { return id; }
}
