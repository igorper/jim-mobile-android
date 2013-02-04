package net.pernek.jim.exercisedetector.test;

import java.util.ArrayList;

import com.jayway.android.robotium.solo.Solo;

import net.pernek.jim.exercisedetector.ExerciseDetectorActivity;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.preference.CheckBoxPreference;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

public class ExerciseDetectorActivityTest extends
		ActivityInstrumentationTestCase2<ExerciseDetectorActivity> {
	
	private Solo solo;

	public ExerciseDetectorActivityTest() {
		super("net.pernek.jim.exercisedetector", ExerciseDetectorActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		solo = new Solo(getInstrumentation(), getActivity());
		
		// perform service cleanup if still running
		if(solo.isCheckBoxChecked(0)){
			solo.clickOnCheckBox(0);
			
			// run and stop service
			solo.clickOnCheckBox(0);
			solo.clickOnCheckBox(0);
		}
	}
	
	private boolean isServicerunning(String serviceName) {
		ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			Log.d("Debug", "All services running : " + service.service.getClassName());
			if (serviceName.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;

	}
	
	// CATION! The screen should be on during tests, otherwise they will fail
	public void testDetectorService(){
		solo.assertCurrentActivity("Run activity for the 1st time.", ExerciseDetectorActivity.class);
		assertFalse("Service should not be started", isServicerunning("net.pernek.jim.exercisedetector.DetectorService"));
		assertFalse("Checkbox unchecked: 1st run.", solo.isCheckBoxChecked(0));
		solo.setActivityOrientation(Solo.LANDSCAPE);
		assertFalse("Service should not be started", isServicerunning("net.pernek.jim.exercisedetector.DetectorService"));
		assertFalse("Checkbox unchecked: 1st run, change orientation.", solo.isCheckBoxChecked("ToggleService"));
		solo.clickOnCheckBox(0);
		assertTrue("Service should be started", isServicerunning("net.pernek.jim.exercisedetector.DetectorService"));
		assertTrue("Checkbox checked: 1st run, 1st click.", solo.isCheckBoxChecked("ToggleService"));
		solo.setActivityOrientation(Solo.PORTRAIT);
		assertTrue("Service should be started", isServicerunning("net.pernek.jim.exercisedetector.DetectorService"));
		assertTrue("Checkbox checked: 1st run, change orientation.", solo.isCheckBoxChecked("ToggleService"));
		solo.clickOnCheckBox(0);
		assertFalse("Service should not be started", isServicerunning("net.pernek.jim.exercisedetector.DetectorService"));
		assertFalse("Checkbox unchecked: 1st run, 2nd click.", solo.isCheckBoxChecked("ToggleService"));
		
		// TODO: add the case when the activity is closed and restarted
	}

}
