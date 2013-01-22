package net.pernek.jim.exercisedetector.test;

import android.test.suitebuilder.TestSuiteBuilder;
import android.util.Log;

import net.pernek.jim.exercisedetector.LinearSensorInterpolator;
import net.pernek.jim.exercisedetector.SensorInterpolator;
import net.pernek.jim.exercisedetector.SensorInterpolatorObserver;
import net.pernek.jim.exercisedetector.SensorValue;
import net.pernek.jim.exercisedetector.SensorValue.SensorType;
import junit.framework.Test;
import junit.framework.TestCase;

public class LinearSensorInterpolatorTest extends TestCase {
	
	public void testInterpolateFiveValuesTo10ms(){
		SensorInterpolator sensorInterpolator = LinearSensorInterpolator.create(10);
		sensorInterpolator.addSensorInterpolatorObserver(new SensorInterpolatorObserver() {
			
			int valueCounter = 1; 
			
			@Override
			public void onNewValue(SensorValue newValue) {
				if(valueCounter == 1){
					assertEquals(10L, newValue.getTimestamp());
					assertEquals(4.83F, newValue.getValues()[0], 0.01);
					assertEquals(4.27F, newValue.getValues()[1], 0.01);
					assertEquals(15.03F, newValue.getValues()[2], 0.01);
					assertEquals(SensorType.ACCELEROMETER_BUILTIN, newValue.getSensorType());
					valueCounter++;
					return;
				}
				else if(valueCounter == 2){
					assertEquals(20L, newValue.getTimestamp());
					assertEquals(7.13F, newValue.getValues()[0], 0.01);
					assertEquals(8.37F, newValue.getValues()[1], 0.01);
					assertEquals(22.57F, newValue.getValues()[2], 0.01);
					assertEquals(SensorType.ACCELEROMETER_BUILTIN, newValue.getSensorType());
					valueCounter++;
					return;
				}
				else if(valueCounter == 3){
					assertEquals(30L, newValue.getTimestamp());
					assertEquals(9.42F, newValue.getValues()[0], 0.01);
					assertEquals(12.47F, newValue.getValues()[1], 0.01);
					assertEquals(30.11F, newValue.getValues()[2], 0.01);
					assertEquals(SensorType.ACCELEROMETER_BUILTIN, newValue.getSensorType());
					valueCounter++;
					return;
				}
				else if(valueCounter == 4){
					assertEquals(40L, newValue.getTimestamp());
					assertEquals(11.72F, newValue.getValues()[0], 0.01);
					assertEquals(16.57F, newValue.getValues()[1], 0.01);
					assertEquals(37.65F, newValue.getValues()[2], 0.01);
					assertEquals(SensorType.ACCELEROMETER_BUILTIN, newValue.getSensorType());
					valueCounter++;
					return;
				}
				else if(valueCounter == 5){
					assertEquals(50L, newValue.getTimestamp());
					assertEquals(14.01F, newValue.getValues()[0], 0.01);
					assertEquals(20.67F, newValue.getValues()[1], 0.01);
					assertEquals(45.19F, newValue.getValues()[2], 0.01);
					assertEquals(SensorType.ACCELEROMETER_BUILTIN, newValue.getSensorType());
					valueCounter++;
					return;
				}
				else if(valueCounter == 6){
					assertEquals(60L, newValue.getTimestamp());
					assertEquals(16.31F, newValue.getValues()[0], 0.01);
					assertEquals(24.77F, newValue.getValues()[1], 0.01);
					assertEquals(52.73F, newValue.getValues()[2], 0.01);
					assertEquals(SensorType.ACCELEROMETER_BUILTIN, newValue.getSensorType());
					valueCounter++;
					return;
				}
				else if(valueCounter == 7){
					assertEquals(70L, newValue.getTimestamp());
					assertEquals(20F, newValue.getValues()[0], 0.01);
					assertEquals(30F, newValue.getValues()[1], 0.01);
					assertEquals(40F, newValue.getValues()[2], 0.01);
					assertEquals(SensorType.ACCELEROMETER_BUILTIN, newValue.getSensorType());
					valueCounter++;
					return;
				}
				else if(valueCounter == 8){
					assertEquals(80L, newValue.getTimestamp());
					assertEquals(44F, newValue.getValues()[0], 0.01);
					assertEquals(59F, newValue.getValues()[1], 0.01);
					assertEquals(43.5F, newValue.getValues()[2], 0.01);
					assertEquals(SensorType.ACCELEROMETER_BUILTIN, newValue.getSensorType());
					valueCounter++;
					return;
				}
				else if(valueCounter == 9){
					assertEquals(90L, newValue.getTimestamp());
					assertEquals(68F, newValue.getValues()[0], 0.01);
					assertEquals(88F, newValue.getValues()[1], 0.01);
					assertEquals(47F, newValue.getValues()[2], 0.01);
					assertEquals(SensorType.ACCELEROMETER_BUILTIN, newValue.getSensorType());
					valueCounter++;
					return;
				}
				
				assertEquals("Unexpected interpolation value.", false, true );
			}
		});
		
		SensorValue first = SensorValue.create(SensorType.ACCELEROMETER_BUILTIN, new float[]{3, 1, 9}, 2);
		SensorValue second = SensorValue.create(SensorType.ACCELEROMETER_BUILTIN, new float[]{17, 26, 55}, 63);
		SensorValue third = SensorValue.create(SensorType.ACCELEROMETER_BUILTIN, new float[]{20, 30, 40}, 70);
		SensorValue fourth = SensorValue.create(SensorType.ACCELEROMETER_BUILTIN, new float[]{68, 88, 47}, 90);
		SensorValue fifth = SensorValue.create(SensorType.ACCELEROMETER_BUILTIN, new float[]{57, 45, 15}, 92);

		sensorInterpolator.push(first);
		sensorInterpolator.push(second);
		sensorInterpolator.push(third);
		sensorInterpolator.push(fourth);
		sensorInterpolator.push(fifth);
	}
	
	/*public static Test suite() {
        return new TestSuiteBuilder(LinearSensorInterpolatorTest.class)
                .includeAllPackagesUnderHere()
                .build();
    }*/

}
