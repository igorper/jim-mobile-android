package net.pernek.jim.exercisedetector;

import java.util.List;

// create unit tests for this functions
public class Statistics {

	public static double sum(List<? extends Number> input) {
		double sum = 0.0;
		for (Number val : input) {
			sum += val.doubleValue();
		}

		return sum;
	}

	public static double mean(List<? extends Number> input) {
		if (input.size() == 0)
			return Double.NaN;

		double sum = sum(input);
		return sum / input.size();
	}

	public static double var(List<? extends Number> input) {
		if (input.size() == 0)
			return Double.NaN;
		double avg = mean(input);
		double sum = 0.0;
		for (Number val : input) {
			sum += (val.doubleValue() - avg) * (val.doubleValue() - avg);
		}

		return sum / (input.size() - 1);
	}

	public static double stDev(List<? extends Number> input) {
		return Math.sqrt(var(input));
	}
}
