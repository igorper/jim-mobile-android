package com.trainerjim.mobile.android.storage;

import android.content.SharedPreferences;

/**
 * Wrapper for {@link SharedPreferences} offering persistent storage for
 * application files.
 * 
 * @author Igor
 * 
 */
public class PermanentSettings {

	/**
	 * Key for accessing the current training plan encoded as a JSON string.
	 */
	private static final String KEY_CURRENT_TRAINING_PLAN = "current_training_plan";

    private static final String KEY_AUTH_USER_ID = "auth_user_id";

    private static final String KEY_AUTH_SESSION_COOKIE = "author_session_cookie";

    private static final String KEY_SELECTED_TRAINING_ID = "selected_training_id";

    private static final String KEY_MAIN_PAGE_TUTORIAL_COUNT = "main_page_tutorial_count";

    private static final String KEY_REST_TUTORIAL_COUNT = "rest_tutorial_count";

    private static final String KEY_SAVE_SERIES_TUTORIAL_COUNT = "save_series_tutorial_count";

    private static final String KEY_EXERCISES_LIST_TUTORIAL_COUNT = "exercises_list_tutorial_count";


    /**
	 * Handle to the {@link SharedPreferences}.
	 */
	private SharedPreferences mPreferences;

	private PermanentSettings() {
	}

	/**
	 * Factory method for creating the wrapper file based on input
	 * {@link SharedPreferences}.
	 * 
	 * @param preferences
	 * @return
	 */
	public static PermanentSettings create(SharedPreferences preferences) {
		PermanentSettings ds = new PermanentSettings();
		ds.mPreferences = preferences;

		return ds;
	}

	/**
	 * Saves the json encoded training plan.
	 * 
	 * @param jsonString
	 */
	public void saveCurrentTrainingPlan(String jsonString) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(KEY_CURRENT_TRAINING_PLAN, jsonString);
		editor.commit();
	}

	/**
	 * Returns the training plan encoded as json string.
	 * 
	 * @return
	 */
	public String getCurrentTrainingPlan() {
		return mPreferences.getString(KEY_CURRENT_TRAINING_PLAN, "");
	}

    public void saveUserId(int userId){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(KEY_AUTH_USER_ID, userId);
        editor.commit();
    }

    public int getUserId() {
        return mPreferences.getInt(KEY_AUTH_USER_ID, -1);
    }

    public void saveSessionCookie(String sessionCookie){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(KEY_AUTH_SESSION_COOKIE, sessionCookie);
        editor.commit();
    }

    public String getSessionCookie() {
        return mPreferences.getString(KEY_AUTH_SESSION_COOKIE, "");
    }

    public void saveSelectedTrainingId(int selectedTrainingId){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(KEY_SELECTED_TRAINING_ID, selectedTrainingId);
        editor.commit();
    }

    public int getSelectedTrainingId() {
        return mPreferences.getInt(KEY_SELECTED_TRAINING_ID, -1);
    }

    public void saveMainPageTutorialCount(int count){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(KEY_MAIN_PAGE_TUTORIAL_COUNT, count);
        editor.commit();
    }

    public int getMainPageTutorialCount() {
        return mPreferences.getInt(KEY_MAIN_PAGE_TUTORIAL_COUNT, 0);
    }

    public void saveRestTutorialCount(int count){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(KEY_REST_TUTORIAL_COUNT, count);
        editor.commit();
    }

    public int getRestTutorialCount() {
        return mPreferences.getInt(KEY_REST_TUTORIAL_COUNT, 0);
    }

    public void saveSeriesTutorialCount(int count){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(KEY_SAVE_SERIES_TUTORIAL_COUNT, count);
        editor.commit();
    }

    public int getSaveSeriesTutorialCount() {
        return mPreferences.getInt(KEY_SAVE_SERIES_TUTORIAL_COUNT, 0);
    }

    public void saveExercisesListTutorialCount(int count){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(KEY_EXERCISES_LIST_TUTORIAL_COUNT, count);
        editor.commit();
    }

    public int getExercisesListTutorialCount() {
        return mPreferences.getInt(KEY_EXERCISES_LIST_TUTORIAL_COUNT, 0);
    }
}
