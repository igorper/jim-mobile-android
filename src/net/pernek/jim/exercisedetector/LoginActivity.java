package net.pernek.jim.exercisedetector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	private DetectorSettings mSettings;

	private EditText mPasswordEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mSettings = DetectorSettings.create(PreferenceManager
				.getDefaultSharedPreferences(this));
		
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mPasswordEditText = (EditText) findViewById(R.id.password_edittext);
		mPasswordEditText.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					onLoginButtonClick(v);
					return true;
				} else {
					return false;
				}
			}
		});

	}

	public void onLoginButtonClick(View source) {

//		Intent intent = new Intent(this, DataUploaderService.class);
//		intent.putExtra(DataUploaderService.INTENT_KEY_ACTION,
//				DataUploaderService.ACTION_GET_TRAINING_LIST);
//		startService(intent);
		mSettings.saveUsername("test");
		mSettings.savePassword("");
		
		startActivity(new Intent(LoginActivity.this, TrainingActivity.class));
		

		Toast.makeText(getApplicationContext(), "Login", Toast.LENGTH_SHORT)
				.show();
	}

}
