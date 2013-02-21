package net.pernek.jim.exercisedetector.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import net.pernek.jim.exercisedetector.SensorListener;
import net.pernek.jim.exercisedetector.Utils;
import net.pernek.jim.exercisedetector.alg.DetectedEvent;
import android.os.Environment;
import android.test.InstrumentationTestCase;

public class SensorListenerTest extends InstrumentationTestCase {

	public void testInterpolation() {
		File folder = new File(Environment.getExternalStorageDirectory(),
				Utils.getDataFolder());
		folder.mkdir();

		File output = new File(folder, "test2");

		SensorListener mSensorListener = SensorListener.create(output,
				getInstrumentation().getContext(), 50000, 200000, 1000000, 180,
				100, 200000, 3);

		List<int[]> allResults = new ArrayList<int[]>();

		// test for adding a single element
		Queue<int[]> results = mSensorListener.interpolate(new int[] { 10, 10,
				10 }, 10);
		for (int[] is : results) {
			allResults.add(is);
		}

		// test for adding some more elements
		assertEquals(1, results.size());
		results = mSensorListener.interpolate(new int[] { 150, 150, 150 }, 150);
		assertEquals(14, results.size());
		for (int[] is : results) {
			allResults.add(is);
		}

		// test if the values are correct and in correct order
		for (int i = 0; i < allResults.size(); i++) {
			int[] currentValue = allResults.get(i);

			// we are using a simple expected value which can be easily
			// calculated
			int expectedValue = (i + 1) * 10;

			assertEquals(expectedValue, currentValue[0]);
			assertEquals(expectedValue, currentValue[1]);
			assertEquals(expectedValue, currentValue[2]);
			assertEquals(expectedValue, currentValue[3]);
		}
	}

	public void testExerciseDetection() {
		List<int[]> testValues = null;
		List<DetectedEvent> expectedExerciseStates = null;

		try {
			testValues = TestHelpers.readAccelerationCsv(new InputStreamReader(
					getInstrumentation().getContext().getResources()
							.getAssets().open("20130221164209_i")));
			expectedExerciseStates = TestHelpers
					.readExerciseStates(new InputStreamReader(
							getInstrumentation().getContext().getResources()
									.getAssets().open("20130221164209_tstmps")));
		} catch (NumberFormatException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		File folder = new File(Environment.getExternalStorageDirectory(),
				Utils.getDataFolder());
		folder.mkdir();

		File output = new File(folder, "test2");

		SensorListener mSensorListener = SensorListener.create(output,
				getInstrumentation().getContext(), 50000, 200000, 1000000, 180,
				100, 200000, 3);

		for (int i = 0; i < testValues.size(); i++) {
			int values[] = new int[3];
			for (int j = 0; j < 3; j++) {
				values[j] = testValues.get(i)[j];
			}
			int timestamp = testValues.get(i)[3];

			try {
				mSensorListener.onSensorChanged(values, timestamp);
			} catch (Exception e) {
				fail();
			}
		}

		List<DetectedEvent> detectedEvents = mSensorListener
				.getDetectedEvents();
		
		for (DetectedEvent detectedEvent : detectedEvents) {
			DetectedEvent toDelete = null;
			for (DetectedEvent expectedEvent : expectedExerciseStates) {
				if(expectedEvent.getTimestamp() == detectedEvent.getTimestamp()
						&& expectedEvent.isExercise() == detectedEvent.isExercise()){
					toDelete = expectedEvent;
					break;
				}
				
			}
			expectedExerciseStates.remove(toDelete);
		}
		
		assertEquals(0, expectedExerciseStates.size());
	}
}
