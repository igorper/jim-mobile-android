package net.pernek.jim.exercisedetector.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.pernek.jim.exercisedetector.alg.ExerciseState;
import net.pernek.jim.exercisedetector.alg.ExerciseStateChange;
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
	
	public static HashMap<Long, Boolean> readExpectedExercises(InputStreamReader input) throws NumberFormatException, IOException{
		HashMap<Long, Boolean> expectedExercises = new HashMap<Long, Boolean>();
		
		CSVReader reader = new CSVReader(input);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			Long timestamp = Long.parseLong(nextLine[0]);
			Boolean isExercise = Integer.parseInt(nextLine[1]) == 1;
			expectedExercises.put(timestamp, isExercise);
		}
		reader.close();
		
		return expectedExercises;
	}
	
	public static List<ExerciseStateChange> readExerciseStates(InputStreamReader input) throws NumberFormatException, IOException{
		List<ExerciseStateChange> exerciseStateChanges = new ArrayList<ExerciseStateChange>();
		
		CSVReader reader = new CSVReader(input);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			exerciseStateChanges.add(ExerciseStateChange.create(ExerciseState.EXERCISE, Long.parseLong(nextLine[0])));
			exerciseStateChanges.add(ExerciseStateChange.create(ExerciseState.REST, Long.parseLong(nextLine[1])));
		}
		reader.close();
		
		return exerciseStateChanges;
	}

}
