package net.pernek.jim.exercisedetector;

import net.pernek.jim.exercisedetector.UploadSessionActivity.ResponseReceiver;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private DetectorSettings mSettings;

	private EditText mPasswordEditText;
	private EditText mUsernameEditText;
	private ProgressDialog mLoginProgress;

	private ResponseReceiver mBroadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mSettings = DetectorSettings.create(PreferenceManager
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

		IntentFilter filter = new IntentFilter(DataUploaderService.ACTION_DONE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		mBroadcastReceiver = new ResponseReceiver();
		registerReceiver(mBroadcastReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	public void onLoginButtonClick(View source) {
		Intent intent = new Intent(this, DataUploaderService.class);
		intent.putExtra(DataUploaderService.INTENT_KEY_ACTION,
				DataUploaderService.ACTION_LOGIN);
		intent.putExtra(DataUploaderService.INTENT_KEY_USERNAME,
				mUsernameEditText.getText().toString());
		intent.putExtra(DataUploaderService.INTENT_KEY_PASSWORD,
				mPasswordEditText.getText().toString());
		startService(intent);

		mLoginProgress = new ProgressDialog(this);
		mLoginProgress.setIndeterminate(true);
		mLoginProgress.setMessage("Checking credentials ...");
		mLoginProgress.show();
	}

	private class ResponseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean loginSuccessful = intent.getExtras().getBoolean(
					DataUploaderService.PARAM_LOGIN_SUCCESSFUL);

			mLoginProgress.dismiss();

			if (loginSuccessful) {
				mSettings.saveUsername(mUsernameEditText.getText().toString());
				mSettings.savePassword(mPasswordEditText.getText().toString());

				startActivity(new Intent(LoginActivity.this,
						TrainingActivity.class));
				finish();
			} else {
				Toast.makeText(
						getApplicationContext(),
						"Ups. Unable to login. Also, this should be a part of the UI.",
						Toast.LENGTH_SHORT).show();
			}

		}
	}
}
