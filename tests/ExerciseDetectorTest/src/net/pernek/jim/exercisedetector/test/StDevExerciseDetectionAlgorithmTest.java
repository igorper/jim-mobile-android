package net.pernek.jim.exercisedetector.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import net.pernek.jim.exercisedetector.alg.ExerciseDetectionAlgorithm;
import net.pernek.jim.exercisedetector.alg.SensorValue;
import net.pernek.jim.exercisedetector.alg.StDevExerciseDetectionAlgorithm;
import android.test.InstrumentationTestCase;

public class StDevExerciseDetectionAlgorithmTest extends InstrumentationTestCase {

	public void testExerciseDetection(){
		List<SensorValue> testValues = null;
		try {
			testValues = TestHelpers.readAccelerationCsv(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("igor.android.10.csv")));
		} catch (NumberFormatException e) {
			fail(e.getMessage());
		} catch(IOException e) {
			fail(e.getMessage());
		}
		
		HashMap<Long, Boolean> expectedExercises = null;
		try {
			expectedExercises = TestHelpers.readExpectedExercises(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("igor.android.10.expected.csv")));
		} catch (NumberFormatException e) {
			fail(e.getMessage());
		} catch(IOException e){
			fail(e.getMessage());
		}
		
		ExerciseDetectionAlgorithm alg = StDevExerciseDetectionAlgorithm
				.create(0.8F, 0.1F, 0.3F, 10.11F, 15000, 120, 20, 5, 10);
		
		for (SensorValue val : testValues) {
			alg.push(val);
		}
		
		List<Boolean> results = alg.getDetectedExercises();
		List<Long> tstmps = alg.getTimestamps();
		
		for (int k=0; k < results.size(); k++){
			Long currentTimestamp = tstmps.get(k);
			assertEquals("Wrong element at index " + k ,expectedExercises.get(currentTimestamp), results.get(k));
		}
	}
}
