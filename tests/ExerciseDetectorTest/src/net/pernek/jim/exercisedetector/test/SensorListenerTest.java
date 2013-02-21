package net.pernek.jim.exercisedetector.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import net.pernek.jim.exercisedetector.SensorListener;
import net.pernek.jim.exercisedetector.Utils;
import android.os.Environment;
import android.test.InstrumentationTestCase;

public class SensorListenerTest extends InstrumentationTestCase {

	public void testInterpolation() {
		File folder = new File(Environment.getExternalStorageDirectory(),
				Utils.getDataFolder());
		folder.mkdir();

		File output = new File(folder, "test2");

		SensorListener mSensorListener = SensorListener.create(output,
				getInstrumentation().getContext());

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
		for(int i=0; i < allResults.size(); i++){
			int[] currentValue = allResults.get(i);
			
			// we are using a simple expected value which can be easily calculated
			int expectedValue = (i + 1)*10;
			
			assertEquals(expectedValue, currentValue[0]);
			assertEquals(expectedValue, currentValue[1]);
			assertEquals(expectedValue, currentValue[2]);
			assertEquals(expectedValue, currentValue[3]);
		}
	}

	private void testExerciseDetection() {
		List<int[]> testValues = null;

		try {
			testValues = TestHelpers.readAccelerationCsv(new InputStreamReader(
					getInstrumentation().getContext().getResources()
							.getAssets().open("")));
			// expectedExerciseStates = TestHelpers.readExerciseStates(new
			// InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("results.alg.igor.csv")));
		} catch (NumberFormatException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		File folder = new File(Environment.getExternalStorageDirectory(),
				Utils.getDataFolder());
		folder.mkdir();

		SensorListener mSensorListener = SensorListener.create(new File(folder,
				"test1"), getInstrumentation().getContext());
		try {
			boolean status = mSensorListener.start(50000, 200000, 1000000, 180,
					100, 200000, 3);

			if (!status) {
				// do this more gracefully -> the app simply does not need to
				// offer automated functionalities if the sensors are not
				// present
				// (user can still swipe through the user interface)
				fail();
			}
			for (int i = 0; i < testValues.size(); i++) {
				int values[] = new int[3];
				for (int j = 0; j < 3; j++) {
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
