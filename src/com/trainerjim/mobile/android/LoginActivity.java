package com.trainerjim.mobile.android;

import com.trainerjim.mobile.android.events.LoginEvent;
import com.trainerjim.mobile.android.network.ServerCommunicationService;
import com.trainerjim.mobile.android.storage.PermanentSettings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import de.greenrobot.event.EventBus;

public class LoginActivity extends Activity {

	private PermanentSettings mSettings;

	private EditText mPasswordEditText;
	private EditText mUsernameEditText;
	private ProgressDialog mLoginProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mSettings = PermanentSettings.create(PreferenceManager
				.getDefaultSharedPreferences(this));

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mUsernameEditText = (EditText) findViewById(R.id.username_edittext);
		mPasswordEditText = (EditText) findViewById(R.id.password_edittext);
		mPasswordEditText.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// event.getAction() == 0 is checked because sometimes this event is
				// fired multiple times
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
					onLoginButtonClick(v);
					return true;
				} else {
					return false;
				}
			}
		});

        EventBus.getDefault().register(this);

        IntentFilter filter = new IntentFilter(ServerCommunicationService.ACTION_LOGIN_COMPLETED);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
	}

	@Override
	protected void onDestroy() {
        EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public void onLoginButtonClick(View source) {
		Intent intent = new Intent(this, ServerCommunicationService.class);
		intent.putExtra(ServerCommunicationService.INTENT_KEY_ACTION,
				ServerCommunicationService.ACTION_CHECK_CREDENTIALS);
		intent.putExtra(ServerCommunicationService.INTENT_KEY_USERNAME,
				mUsernameEditText.getText().toString());
		intent.putExtra(ServerCommunicationService.INTENT_KEY_PASSWORD,
				mPasswordEditText.getText().toString());
		startService(intent);

		mLoginProgress = new ProgressDialog(this);
		mLoginProgress.setIndeterminate(true);
		mLoginProgress.setMessage("Checking credentials ...");
		mLoginProgress.show();
	}

    public void onEvent(LoginEvent loginEvent){
        int userId = loginEvent.getUserId();

        mLoginProgress.dismiss();

        if (userId >= 0) {
            mSettings.saveUserId(userId);

            // TODO: remove this with implementation of new API (userId will be used for all
            // subsequent queries)
            mSettings.saveUsername(mUsernameEditText.getText().toString());
            mSettings.savePassword(mPasswordEditText.getText().toString());

            startActivity(new Intent(LoginActivity.this,
                    TrainingActivity.class));
            finish();
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    loginEvent.getStatusMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
