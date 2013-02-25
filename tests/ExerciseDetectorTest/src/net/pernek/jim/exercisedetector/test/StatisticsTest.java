package net.pernek.jim.exercisedetector.test;

import net.pernek.jim.exercisedetector.util.Statistics;
import junit.framework.TestCase;

public class StatisticsTest extends TestCase {
	public void testSum() {
		int[] input = { 14534, 14530, 14540, 14550, 14554, 14550, 14560, 14563,
				14560, 14570, 14580, 14581, 14580, 14584, 14580, 14590, 14593,
				14590, 14600, 14603, 14600, 14610, 14614, 14610, 14620, 14623,
				14620, 14630, 14633, 14630, 14640, 14644, 14640, 14650, 14653,
				14650, 14660, 14664, 14660, 14670, 14673, 14670, 14680, 14683,
				14680, 14690, 14693, 14690, 14700, 14704, 14700, 14710, 14713,
				14710, 14720, 14730, 14734, 14730, 14740, 14743, 14740, 14750,
				14753, 14750, 14760, 14764, 14760, 14770, 14780, 14784, 14780,
				14790, 14793, 14790, 14800, 14803, 14800, 14810, 14813, 14810 };
		
		int sum = (int)Statistics.sum(input);
		
		assertEquals(1173806, sum);
	}
	
	public void testStDev() {
		int[] input = { 14534, 14530, 14540, 14550, 14554, 14550, 14560, 14563,
				14560, 14570, 14580, 14581, 14580, 14584, 14580, 14590, 14593,
				14590, 14600, 14603, 14600, 14610, 14614, 14610, 14620, 14623,
				14620, 14630, 14633, 14630, 14640, 14644, 14640, 14650, 14653,
				14650, 14660, 14664, 14660, 14670, 14673, 14670, 14680, 14683,
				14680, 14690, 14693, 14690, 14700, 14704, 14700, 14710, 14713,
				14710, 14720, 14730, 14734, 14730, 14740, 14743, 14740, 14750,
				14753, 14750, 14760, 14764, 14760, 14770, 14780, 14784, 14780,
				14790, 14793, 14790, 14800, 14803, 14800, 14810, 14813, 14810 };
		
		int stdev = -1;
		try {
			stdev = (int)Statistics.stDev(input);
		} catch (Exception e) {
			fail();
		}
		
		assertEquals(81, stdev);
	}
	
	public void testMean(){
		int[] input = { 14534, 14530, 14540, 14550, 14554, 14550, 14560, 14563,
				14560, 14570, 14580, 14581, 14580, 14584, 14580, 14590, 14593,
				14590, 14600, 14603, 14600, 14610, 14614, 14610, 14620, 14623,
				14620, 14630, 14633, 14630, 14640, 14644, 14640, 14650, 14653,
				14650, 14660, 14664, 14660, 14670, 14673, 14670, 14680, 14683,
				14680, 14690, 14693, 14690, 14700, 14704, 14700, 14710, 14713,
				14710, 14720, 14730, 14734, 14730, 14740, 14743, 14740, 14750,
				14753, 14750, 14760, 14764, 14760, 14770, 14780, 14784, 14780,
				14790, 14793, 14790, 14800, 14803, 14800, 14810, 14813, 14810 };
		
		int mean = -1;
		try {
			mean = (int)Statistics.mean(input);
		} catch (Exception e) {
			fail();
		}
		
		assertEquals(14672, mean);
	}
}
