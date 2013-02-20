package net.pernek.jim.exercisedetector.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import net.pernek.jim.exercisedetector.SensorListener;
import net.pernek.jim.exercisedetector.Utils;
import android.os.Environment;
import android.test.InstrumentationTestCase;

public class SensorListenerTest extends InstrumentationTestCase {
/*
	private void testInterpolation() {
		List<int[]> testValues = null;
		
		try {
			testValues = TestHelpers.readAccelerationCsv(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("20130220201258")));
			//expectedExerciseStates = TestHelpers.readExerciseStates(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("results.alg.igor.csv")));
		} catch (NumberFormatException e) {
			fail(e.getMessage());
		} catch(IOException e) {
			fail(e.getMessage());
		}
		
		
		File folder = new File(Environment.getExternalStorageDirectory(),
				Utils.getDataFolder());
		folder.mkdir();
		
		SensorListener mSensorListener = SensorListener.create(
				new File(folder, "test2"), getInstrumentation().getContext());
		try {
			if (!mSensorListener.start()) {
				// do this more gracefully -> the app simply does not need to
				// offer automated functionalities if the sensors are not
				// present
				// (user can still swipe through the user interface)
				fail();
			}
			for(int i=0; i < testValues.size(); i++){
				int values[] = new int[3];
				for(int j=0; j < 3; j++){
					values[j] = testValues.get(i)[j];
				}
				int timestamp = testValues.get(i)[3];
				
				mSensorListener.interpolate(values, timestamp);
			}
			
		} catch (Exception ex) {
			fail();
		}
	}*/
	
	public void testExerciseDetection() {
		List<int[]> testValues = null;
		
		try {
			testValues = TestHelpers.readAccelerationCsv(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("input.alg.igor1.csv")));
			//expectedExerciseStates = TestHelpers.readExerciseStates(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("results.alg.igor.csv")));
		} catch (NumberFormatException e) {
			fail(e.getMessage());
		} catch(IOException e) {
			fail(e.getMessage());
		}
		
		
		File folder = new File(Environment.getExternalStorageDirectory(),
				Utils.getDataFolder());
		folder.mkdir();
		
		SensorListener mSensorListener = SensorListener.create(
				new File(folder, "test1"), getInstrumentation().getContext());
		try {
			if (!mSensorListener.start()) {
				// do this more gracefully -> the app simply does not need to
				// offer automated functionalities if the sensors are not
				// present
				// (user can still swipe through the user interface)
				fail();
			}
			for(int i=0; i < testValues.size(); i++){
				int values[] = new int[3];
				for(int j=0; j < 3; j++){
					values[j] = testValues.get(i)[j];
				}
				int timestamp = testValues.get(i)[3];
				
				mSensorListener.onSensorChanged(values, timestamp);
			}
			
		} catch (Exception ex) {
			fail();
		}
	}
}
