package game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleTypes 
{
	private static final int MIN_DIM = 5;
	private static final int MAX_DIM = 13;
	
	private static SampleTypes types = null;
	
	public static void main(String[] args)
	{
		getInstance();
	}
	
	public static SampleTypes getInstance() 
	{ 
		if(types == null)
		{
			types = new SampleTypes();
		}
		return types; 
	}
	
	
	//Contains data mapped per dimension
		//Each submap maps a point on a sample grid to its own index (1-n) to provide a mapping for a contiguous array
	private Map<Integer, Map<Point, Integer>> sampleTypes = new HashMap<>();
	private Map<Integer, Integer> numTypes = new HashMap<>(); //The number of different distances from the center
	
	public int indexOf(int dim, Point gridPoint)
	{
		return sampleTypes.get(dim).get(gridPoint);
	}
	public int numTypes(int dim)
	{
		return numTypes.get(dim);
	}

	private SampleTypes()
	{
		for(int a = MIN_DIM; a < MAX_DIM; a++)
		{
			findSampleTypes(a);
		}
	}
	
	private void findSampleTypes(int delta)
	{
		Map<Double, List<Point>> distancesMap = new HashMap<Double, List<Point>>();
		Point.Double sampleCenter = new Point.Double((double) delta / 2, (double) delta / 2); //center of the delta-sized sample
		for(int a = 0; a < delta; a++)
		{
			for(int b = 0; b < delta; b++)
			{
				Point.Double squareCenter = new Point.Double(a + 0.5d, b + 0.5d); //center of this square
				//hash by distance
				double distance = sampleCenter.distance(squareCenter);
				if(!distancesMap.containsKey(distance))
				{
					distancesMap.put(distance, new ArrayList<Point>());
				}
				distancesMap.get(distance).add(new Point(a, b));
			}
		}
		List<Double> distances = new ArrayList<>();
		for(Double key : distancesMap.keySet())
		{
			distances.add(key);
		}
		numTypes.put(delta, distances.size());
		Collections.sort(distances);
		sampleTypes.put(delta, new HashMap<Point, Integer>());
		for(Double key : distancesMap.keySet())
		{
			int indexOfDistance = distances.indexOf(key);
			for(Point point : distancesMap.get(key))
			{
				sampleTypes.get(delta).put(point, indexOfDistance);
			}
		}
	}
}
