package net.pernek.jim.exercisedetector.test;

import java.util.List;

import net.pernek.jim.exercisedetector.alg.ExerciseStateChange;
import android.test.InstrumentationTestCase;

public class StDevExerciseDetectionAlgorithmTest extends InstrumentationTestCase {

	List<ExerciseStateChange> expectedExerciseStates = null;
	public void testExerciseDetection(){
		/*List<SensorValue> testValues = null;
				
		try {
			testValues = TestHelpers.readAccelerationCsv(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("input.alg.igor.csv")));
			expectedExerciseStates = TestHelpers.readExerciseStates(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("results.alg.igor.csv")));
		} catch (NumberFormatException e) {
			fail(e.getMessage());
		} catch(IOException e) {
			fail(e.getMessage());
		}
		
		ExerciseDetectionAlgorithm alg = StDevExerciseDetectionAlgorithm
				.create(0.8F, 0.1F, 0.3F, 10.11F, 120, 20, 5, 10);
		alg.addExerciseDetectionListener(new ExerciseDetectionAlgorithmListener() {
			
			@Override
			public void exerciseStateChanged(ExerciseStateChange newState) {
				expectedExerciseStates.remove(newState);
			}
		});
		
		for (SensorValue val : testValues) {
			try {
				alg.push(val);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail();
			}
		}
		
		assertTrue(expectedExerciseStates.isEmpty());*/
	}
}
