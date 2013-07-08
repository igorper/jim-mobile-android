package com.trainerjim.android.storage;

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

	/**
	 * Key for accessing username of the currently logged in user. NOTE: If no
	 * user is logged in the username will be an empty string.
	 */
	private static final String KEY_AUTH_USERNAME = "auth_username";

	/**
	 * Key for accessing a password of the currently logged in user.
	 */
	private static final String KEY_AUTH_PASS = "auth_pass";

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

	/**
	 * Saves the username of the currently logged in user. NOTE: Username is
	 * null when no user is logged in.
	 * 
	 * @param username
	 */
	public void saveUsername(String username) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(KEY_AUTH_USERNAME, username);
		editor.commit();
	}

	/**
	 * This method returns the username used to authenticate with the remote
	 * server. If the user is not yet authenticated an empty string will be
	 * returned.
	 * 
	 * @return an empty string if the user is not yet authenticated otherwise
	 *         the login username.
	 */
	public String getUsername() {
		return mPreferences.getString(KEY_AUTH_USERNAME, "");
	}

	/**
	 * Saves the password of the currently logged in user.
	 * 
	 * @param password
	 */
	public void savePassword(String password) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(KEY_AUTH_PASS, password);
		editor.commit();
	}

	/**
	 * This method returns the password of the currently logged in user.
	 * 
	 * @return
	 */
	public String getPassword() {
		return mPreferences.getString(KEY_AUTH_PASS, "");
	}
}
