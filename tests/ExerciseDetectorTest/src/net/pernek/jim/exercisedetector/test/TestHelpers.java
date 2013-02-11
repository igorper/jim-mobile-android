package net.pernek.jim.exercisedetector.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.pernek.jim.exercisedetector.alg.ExerciseState;
import net.pernek.jim.exercisedetector.alg.ExerciseStateChange;
import net.pernek.jim.exercisedetector.alg.SensorValue;
import net.pernek.jim.exercisedetector.alg.SensorValue.SensorType;
import au.com.bytecode.opencsv.CSVReader;

public class TestHelpers {

	public static List<SensorValue> readAccelerationCsv(InputStreamReader input) throws NumberFormatException, IOException{
		List<SensorValue> testValues = new ArrayList<SensorValue>();
				
		CSVReader reader = new CSVReader(input);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			Float valX = nextLine[1].equals("") ? null : Float.parseFloat(nextLine[1]);
			Float valY = nextLine[2].equals("") ? null : Float.parseFloat(nextLine[2]);
			Float valZ = nextLine[3].equals("") ? null : Float.parseFloat(nextLine[3]);
			testValues.add(SensorValue.create(SensorType.ACCELEROMETER_BUILTIN, 
					new Float[] {valX, valY, valZ}, 
					Long.parseLong(nextLine[0])));
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
