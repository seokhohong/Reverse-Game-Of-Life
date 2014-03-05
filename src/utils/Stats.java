package utils;

import java.util.Collection;

public class Stats 
{
	public static double gaussian(double meanX, double meanY, double stddevX, double stddevY, double pointX, double pointY)
	{
		double xTerm = square(meanX - pointX) / (2 * square(stddevX));
		double yTerm = square(meanY - pointY) / (2 * square(stddevY));
		return Math.exp(-(xTerm + yTerm));
	}
	private static double square(double val)
	{
		return val * val;
	}
	
	public static double stddev(Collection<Integer> set)
	{
		double mean = mean(set);
		double dev = 0;
		for(Integer i : set)
		{
			dev += (i - mean) * (i - mean);
		}
		dev = Math.sqrt(dev);
		return dev;
	}
	public static double mean(Collection<Integer> set)
	{
		double mean = 0;
		for(Integer i : set)
		{
			mean += i;
		}
		return mean;
	}
}
