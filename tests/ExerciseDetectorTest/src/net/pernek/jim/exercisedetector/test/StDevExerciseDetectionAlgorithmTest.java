package net.pernek.jim.exercisedetector.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.TestSuiteBuilder;
import au.com.bytecode.opencsv.CSVReader;

import net.pernek.jim.exercisedetector.ExerciseDetectionAlgorithm;
import net.pernek.jim.exercisedetector.ExerciseState;
import net.pernek.jim.common.SensorValue;
import net.pernek.jim.exercisedetector.StDevExerciseDetectionAlgorithm;
import net.pernek.jim.common.SensorValue.SensorType;
import junit.framework.Test;

public class StDevExerciseDetectionAlgorithmTest extends
		InstrumentationTestCase {

	public void testExerciseDetection(){
		List<SensorValue> testValues = new ArrayList<SensorValue>();
		InputStream androidCsvStream = null;
		InputStream androidResultsCsvStream = null;
		try {
			androidCsvStream = getInstrumentation().getContext().getResources()
					.getAssets().open("igor.android.10.csv");
			androidResultsCsvStream = getInstrumentation().getContext().getResources()
					.getAssets().open("igor.android.10.expected.csv");
			
		} catch (IOException e) {
			assertEquals("Could not load CSV test data.", false, true);
		}

		CSVReader reader = new CSVReader(new InputStreamReader(androidCsvStream));
		String[] nextLine;
		try {
			while ((nextLine = reader.readNext()) != null) {
				testValues.add(SensorValue.create(SensorType.ACCELEROMETER_BUILTIN, 
						new Float[] {Float.parseFloat(nextLine[1]), Float.parseFloat(nextLine[2]), Float.parseFloat(nextLine[3])}, 
						Long.parseLong(nextLine[0])));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Boolean> expectedExercises = new ArrayList<Boolean>();
		List<Long> expectedTimestamps = new ArrayList<Long>();
		reader = new CSVReader(new InputStreamReader(androidResultsCsvStream));
		try {
			while ((nextLine = reader.readNext()) != null) {
				expectedTimestamps.add(Long.parseLong(nextLine[0]));
				expectedExercises.add(Integer.parseInt(nextLine[1]) == 1);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		ExerciseDetectionAlgorithm alg = StDevExerciseDetectionAlgorithm
				.create(0.8F, 0.1F, 0.3F, 10.11F, 15000, 120, 20, 5, 10);
		
		for (SensorValue val : testValues) {
			alg.push(val);
		}
		
		List<Boolean> results = alg.getDetectedExercises();
		List<Long> tstmps = alg.getTimestamps();
		
		for (int k=0; k < results.size(); k++){
			assertEquals("Wrong element at index " + k ,expectedExercises.get(k), results.get(k));
			assertEquals("Wrong element at index " + k, expectedTimestamps.get(k), tstmps.get(k));
		}
	}

	public static Test suite() {
		return new TestSuiteBuilder(StDevExerciseDetectionAlgorithmTest.class)
				.includeAllPackagesUnderHere().build();
	}
}
