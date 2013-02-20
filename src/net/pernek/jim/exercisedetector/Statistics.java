package net.pernek.jim.exercisedetector;


// create unit tests for this functions
public class Statistics {

	public static double sum(int[] input) {
		double sum = 0;
		for (int i=input.length - 1; i >= 0 ; i--){
			sum += input[i];
		}

		return sum;
	}

	public static double mean(int[] input) throws Exception {
		if (input.length == 0)
			throw new Exception("Empty input");

		double sum = sum(input);
		return sum / input.length;
	}

	public static double var(int[] input) throws Exception {
		if (input.length == 0)
			throw new Exception("Empty input");
		
		double avg = mean(input);
		double sum = 0;
		for (int i = input.length - 1; i >= 0; i--) {
			sum += (input[i] - avg) * (input[i] - avg);
		}

		return sum / (input.length - 1);
	}

	public static double stDev(int[] input) throws Exception {
		return Math.sqrt(var(input));
	}
}
