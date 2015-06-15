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
}
