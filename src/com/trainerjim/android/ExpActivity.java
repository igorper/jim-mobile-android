package com.trainerjim.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class ExpActivity extends Activity {
	
	public int counter = 0;
	public int imageCounter = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exp);
	}
	
	public void onExpViewClick(View v){
		Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
		counter++;
	}
	
	public void onExpImageViewClick(View v){
		Toast.makeText(getApplicationContext(), "Image clicked", Toast.LENGTH_SHORT).show();
		imageCounter++;
	}

}
