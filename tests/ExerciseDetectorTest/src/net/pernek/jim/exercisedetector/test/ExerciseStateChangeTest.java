package net.pernek.jim.exercisedetector.test;

import net.pernek.jim.exercisedetector.alg.ExerciseState;
import net.pernek.jim.exercisedetector.alg.ExerciseStateChange;
import junit.framework.TestCase;

public class ExerciseStateChangeTest extends TestCase {
	
	public void testEqualsAndHashCode(){
		ExerciseStateChange esc = ExerciseStateChange.create(ExerciseState.REST, 1231);
		ExerciseStateChange esc1 = ExerciseStateChange.create(ExerciseState.REST, 1231);
		ExerciseStateChange esc2 = ExerciseStateChange.create(ExerciseState.EXERCISE, 1231);
		ExerciseStateChange esc3 = ExerciseStateChange.create(ExerciseState.EXERCISE, 12310);
		
		assertEquals(esc, esc1);
		assertFalse(esc.equals(esc2));
		assertFalse(esc3.equals(esc2));
		
		assertEquals(esc.hashCode(), esc1.hashCode());
		assertFalse(esc.hashCode() == esc2.hashCode());
		assertFalse(esc3.hashCode() == esc2.hashCode());
	}

}
