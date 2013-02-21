package net.pernek.jim.exercisedetector.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.pernek.jim.exercisedetector.alg.DetectedEvent;

import au.com.bytecode.opencsv.CSVReader;

public class TestHelpers {
	
	public static List<int[]> readAccelerationCsv(InputStreamReader input) throws NumberFormatException, IOException{
		List<int[]> testValues = new ArrayList<int[]>();
				
		CSVReader reader = new CSVReader(input);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			int valX = Integer.parseInt(nextLine[0]);
			int valY = Integer.parseInt(nextLine[1]);
			int valZ = Integer.parseInt(nextLine[2]);
			int timestamp = Integer.parseInt(nextLine[3]);
			testValues.add(new int[] {valX, valY, valZ, timestamp});
		}
		reader.close();
		
		return testValues;
	}
	
	public static List<DetectedEvent> readExerciseStates(InputStreamReader input) throws NumberFormatException, IOException{
		List<DetectedEvent> expectedExercises = new ArrayList<DetectedEvent>();
		
		CSVReader reader = new CSVReader(input);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			Integer timestamp = Integer.parseInt(nextLine[1]);
			Boolean isExercise = Boolean.parseBoolean(nextLine[0]);
			expectedExercises.add(new DetectedEvent(isExercise, timestamp));
		}
		reader.close();
		
		return expectedExercises;
	}
}
