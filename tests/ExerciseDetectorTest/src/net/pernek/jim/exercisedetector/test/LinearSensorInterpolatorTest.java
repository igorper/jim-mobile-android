package net.pernek.jim.exercisedetector.test;

import java.util.List;

import net.pernek.jim.exercisedetector.alg.SensorValue;
import android.test.InstrumentationTestCase;

public class LinearSensorInterpolatorTest extends InstrumentationTestCase{

	private List<SensorValue> expectedValues = null;
	public void testInterpolation(){
	/*	List<SensorValue> testValues = null;
		
		try {
			testValues = TestHelpers.readAccelerationCsv(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("acc1")));
			expectedValues = TestHelpers.readAccelerationCsv(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("acc1Int")));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		SensorInterpolator sensorInterpolator = LinearSensorInterpolator.create(10);
		sensorInterpolator.addSensorInterpolatorListener(new SensorInterpolatorListener() {
			int expectedIndex = 0;
			@Override
			public void onNewInterpolatedValue(SensorValue newValue) {
				SensorValue expected = expectedValues.get(expectedIndex++);
				assertEquals(expected.getTimestamp(), newValue.getTimestamp());
				assertEquals(expected.getValues()[0], newValue.getValues()[0], 0.01);
				assertEquals(expected.getValues()[1], newValue.getValues()[1], 0.01);
				assertEquals(expected.getValues()[2], newValue.getValues()[2], 0.01);
				assertEquals(SensorType.ACCELEROMETER_BUILTIN, newValue.getSensorType());
			}
		});
		
		for(SensorValue sv : testValues) {
			sensorInterpolator.push(sv);
		}*/
	}
}
