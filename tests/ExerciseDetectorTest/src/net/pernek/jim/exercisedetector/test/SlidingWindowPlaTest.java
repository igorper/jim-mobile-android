package net.pernek.jim.exercisedetector.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import net.pernek.jim.exercisedetector.alg.Pla;
import net.pernek.jim.exercisedetector.alg.SensorValue;
import net.pernek.jim.exercisedetector.alg.SlidingWindowPla;
import android.os.Environment;
import android.test.InstrumentationTestCase;

public class SlidingWindowPlaTest extends InstrumentationTestCase{

	public void testProcess() throws IOException {		
		List<SensorValue> testValues = null;
		List<SensorValue> expectedValues = null;
		try {
			testValues = TestHelpers.readAccelerationCsv(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("acc.csv")));
			expectedValues = TestHelpers.readAccelerationCsv(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("accPLA.csv")));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		Pla plaProc = SlidingWindowPla.create(0.1);
		for(int i=0; i < testValues.size(); i++) {
			SensorValue currentValue = testValues.get(i);		
			plaProc.process(currentValue);
		}
				
		// test if produced values are the same as expected values
		List<SensorValue> producedValues = plaProc.getKeyValues();
		for(int i=0; i < expectedValues.size(); i++){
			SensorValue produced = producedValues.get(i);
			SensorValue expected = expectedValues.get(i);
						
			assertEquals(produced, expected);
		}
	}
	
	public void testWriteToFile(){
		String outputPath = "/sdcard/testOutput.csv";
		
		List<SensorValue> testValues = null;
		List<SensorValue> expectedValues = null;
		try {
			testValues = TestHelpers.readAccelerationCsv(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("acc.csv")));
			expectedValues = TestHelpers.readAccelerationCsv(new InputStreamReader(getInstrumentation().getContext().getResources().getAssets().open("accPLA.csv")));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		Pla plaProc = SlidingWindowPla.create(0.1);
		try {
			plaProc.setOutputFile(outputPath);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		for(int i=0; i < testValues.size(); i++) {
			SensorValue currentValue = testValues.get(i);		
			plaProc.process(currentValue);
		}
		plaProc.closeOutputFile();
		
		List<SensorValue> producedValues = null;
		File f = new File(outputPath);
		if(f.exists()){
			try {
				producedValues = TestHelpers.readAccelerationCsv(new InputStreamReader(new FileInputStream(outputPath)));
			} catch (NumberFormatException e) {
				fail(e.getMessage());
			} catch (IOException e){
				fail(e.getMessage());
			}
			f.delete();
		}
				
		// test if produced values are the same as expected values
		for(int i=0; i < expectedValues.size(); i++){
			SensorValue produced = producedValues.get(i);
			SensorValue expected = expectedValues.get(i);
			
			if(expected.getValues()[0] == null){
				assertEquals(expected.getValues()[0], produced.getValues()[0]);
			} else {
				assertEquals(produced.getValues()[0], expected.getValues()[0], 0.01);
			}
			
			if(expected.getValues()[1] == null){
				assertEquals(produced.getValues()[1], expected.getValues()[1]);
			} else {
				assertEquals(produced.getValues()[1], expected.getValues()[1], 0.01);
			}
			
			if(expected.getValues()[2] == null){
				assertEquals(produced.getValues()[2], expected.getValues()[2]);
			} else {
				assertEquals(produced.getValues()[2], expected.getValues()[2], 0.01);
			}
			
			assertEquals(produced.getTimestamp(), expected.getTimestamp());
			assertEquals(produced.getSensorType(), expected.getSensorType());
		}
	}
}
