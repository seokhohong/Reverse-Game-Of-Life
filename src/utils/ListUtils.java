package utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils 
{
	/** Returns a List copy with ArrayList implementation */
	public static <T> List<T> copy(List<T> toCopy)
	{
		List<T> copy = new ArrayList<T>();
		copy.addAll(toCopy);
		return copy;
	}
	public static double sum(List<Double> list)
	{
		double sum = 0;
		for(double val : list)
		{
			sum += val;
		}
		return sum;
	}
	public static double mean(List<Double> list)
	{
		return sum(list) / list.size();
	}
	/** Divides the original list into a number of components */
	public static <T> List<List<T>> divide(List<T> list, int numDivisions)
	{
		List<List<T>> containerList = new ArrayList<>();
		for(int a = 0; a < numDivisions; a++)
		{
			containerList.add(new ArrayList<T>());
		}
		for(int a = 0; a < list.size(); a ++)
		{
			containerList.get(a % numDivisions).add(list.get(a));
		}
		return containerList;
	}
}
