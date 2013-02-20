package net.pernek.jim.exercisedetector;

import java.util.List;

// create unit tests for this functions
public class Statistics {

	public static float sum(float[] input) {
		float sum = 0f;
		for (int i=input.length - 1; i >= 0 ; i--){
			sum += input[i];
		}

		return sum;
	}
	
	public static long sum(long[] input) {
		long sum = 0l;
		for (int i=input.length - 1; i >= 0 ; i--){
			sum += input[i];
		}

		return sum;
	}

	public static float mean(float[] input) throws Exception {
		if (input.length == 0)
			throw new Exception("Empty input");

		float sum = sum(input);
		return sum / input.length;
	}
	
	public static long mean(long[] input) throws Exception {
		if (input.length == 0)
			throw new Exception("Empty input");

		long sum = sum(input);
		return sum / input.length;
	}

	public static float var(float[] input) throws Exception {
		if (input.length == 0)
			throw new Exception("Empty input");
		
		float avg = mean(input);
		float sum = 0f;
		for (int i = input.length - 1; i >= 0; i--) {
			sum += (input[i] - avg) * (input[i] - avg);
		}

		return sum / (input.length - 1);
	}

	public static float stDev(float[] input) throws Exception {
		return (float)Math.sqrt(var(input));
	}
}
