package gov.nci.nih.meltzerlab.utils;

import java.util.Collection;


public class YevMathUtils 
{

	/**
	 * Calculate mean
	 * @param values the values to use
	 * @return mean
	 */
	public static double getAverage(double[] values)
	{
		return sum(values) / (double) values.length;
	}

	public static double getAverage(Collection<Double> values) {
		return sum(values) / (double) values.size();
	}

	public static double sum(double[] values)
	{
		double sum = 0.0;
		for (int i = 0; i < values.length; i++)
		{
			sum += values[i];
		}
		return sum;
	}

	/**
	 * Calculate sum
	 * @param values
	 * @return
	 */
	public static double sum(Collection<Double> values) {

		double sum = 0.0;

		for (Double value : values) {
			sum += value;
		}

		return sum;
	}


	/**
	 * Calculate standard deviation
	 * @param values
	 * @param complete true for population; false for sample
	 * @return
	 */
	public static double getStandardDeviation(double[] values, boolean complete)
	{
		double mean = getAverage(values);

		if (! complete)
		{
			return Math.sqrt(sum(raiseToPower(subtract(values, mean), 2.0)) / (values.length - 1));
		}
		else
		{
			return Math.sqrt(sum(raiseToPower(subtract(values, mean), 2.0)) / values.length);
		}
	}

	/**
	 * Subtracts a value from every value in the array
	 * @param values collection of values
	 * @param value a value to be subtracted
	 * @return
	 */
	public static double[] subtract(double[] values, double value)
	{
		double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++)
		{
			result[i] = values[i] - value;
		}
		return result;
	}

	/**
	 * Raises the vector to a power
	 * @param values a vector
	 * @param power
	 * @return
	 */
	public static double[] raiseToPower(double[] values, double power)
	{
		double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++)
		{
			result[i] = Math.pow(values[i], power);
		}
		return result;

	}

	/**
	 * Subtract vectors
	 * @param v1 will be subtracted from
	 * @param v2 will be subtracted
	 * @return
	 */
	public static double[] getDifference(double[] v1, double[] v2)
	{
		if (v1.length != v2.length)
		{
			throw new IllegalArgumentException("Vectors of unequal size");
		}
		double[] difference = new double[v1.length];

		for (int i = 0; i < v1.length; i++)
		{
			difference[i] = v1[i] - v2[i];
		}

		return difference;
	}

	/**
	 * Return maximum value for an array
	 * @param values
	 * @return
	 */
	public static double getMaximum(double[] values)
	{
		double max = Double.MIN_VALUE;
		for (int i = 0; i < values.length; i++)
		{
			max = Math.max(max, values[i]);
		}
		return max;
	}

	/**
	 * Return the minimum value
	 * @param values
	 * @return
	 */

	public static double getMinimum(double[] values)
	{
		double min = Double.MAX_VALUE;
		for (int i = 0; i < values.length; i++)
		{
			min = Math.min(min, values[i]);
		}
		return min;
	}

	/**
	 * Scales array to values between 0 and 1
	 * @param values
	 */
	public static void scale(double[] values)
	{	
		double min = getMinimum(values);
		double max = getMaximum(values);
		double scaleFactor = max - min;
		for (int i = 0; i < values.length; i++)
		{
			values[i] = (values[i] - min) / scaleFactor;
		}
	}


	/**
	 * 
	 * Taken from http://stackoverflow.com/questions/202302/rounding-to-an-arbitrary-number-of-significant-digits
	 * @param num
	 * @param n
	 * @return
	 */
	public static double roundToSignificantFigures(double num, int n) 
	{
		if(num == 0) {
			return 0;
		}

		final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
		final int power = n - (int) d;

		final double magnitude = Math.pow(10, power);
		final long shifted = Math.round(num*magnitude);
		return shifted/magnitude;
	}

	/**
	 * Rounds the number to the nearest. 900 rounded to nearest 500 is 1000
	 * @param num the number i.e. 900
	 * @param nearest the nearest i.e. 500
	 * @return
	 */
	public static int roundToNearest(int num, int nearest)
	{
		return (int) (nearest * Math.round( (double)num / (double) nearest));
	}

	/**
	 * Generate sequence of integers
	 * generateSequence(1000, 10, -100); -> [1000, 900, 800, 700, 600, 500, 400, 300, 200, 100]
	 * generateSequence(1000, 10, 100); -> [1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900]
	 * @param from the starting number (invlusive)
	 * @param n the length of the sequence
	 * @param by the interval
	 * @return generated sequence
	 */
	public static int[] generateSequence(int from, int n, int by)
	{
		int[] sequence = new int[n];
		for (int i = 0; i < sequence.length; i++)
		{
			sequence[i] = from + i * by;
		}
		return sequence;
	}

	public static double[] generateSequence(double from, int n, double by)
	{
		double[] sequence = new double[n];
		for (int i = 0; i < sequence.length; i++)
		{
			sequence[i] = from + i * by;
		}
		return sequence;
	}
	
	/**
	 * Returns an array representation
	 * @param values
	 * @return
	 */
	public static double[] toArray(Collection<Double> values) {
		double[] result = new double[values.size()];
		int i = 0;
		for (Double value : values) {
			result[i] = value;
			i++;
		}
		
		
		return result;
	}


	public static void main (String[] args)
	{
		double[] x = {2,4,4,4,5,5,7,9};
		System.out.println(getStandardDeviation(x, false));
		//		//System.out.println(roundToSignificantFigures(44637, 3));
		//		System.out.println(roundToNearest(900, 500));
		//		System.out.println(roundToSignificantFigures(0.341533010760132, 2));
		//		int[] seq = generateSequence(1000000, 100, -1000000/100);
		//		System.out.println(Arrays.toString(seq));
		//		
		//		double[] aSeq = generateSequence((double)1000000, 100, -1000000/100);
		//		System.out.println(Arrays.toString(aSeq));
		//		
		//		double[][] profiles = TimingAgreement.getProfiles(new File("/Users/yev/CloudStation/M/replication_timing/useful/chr21_wgEncodeUwDnaseGm06990HotspotsRep1.CGHnTiming.csv.timing.gz"), 
		//				new File( "/Users/yev/CloudStation/M/replication_timing/hansen/hg19/chr21/GM06990.txt"));
		//		
		//		System.out.println(getMaximum(profiles[0]) + " " + getMaximum(profiles[1]));
		//		System.out.println(getMinimum(profiles[0]) + " " + getMinimum(profiles[1]));
		//
		//		
		//	scale(profiles[0]);
		//		scale(profiles[1]);
		//		
		//		System.out.println(getMaximum(profiles[0]) + " " + getMaximum(profiles[1]));
		//		System.out.println(getMinimum(profiles[0]) + " " + getMinimum(profiles[1]));

	}
}
