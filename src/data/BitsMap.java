package data;

import utils.FastIO;
import game.GameOfLife;
import game.SampleSpecs;

import java.io.File;
import java.util.*;

public class BitsMap 
{
	private Map<Long, Integer> map = new HashMap<Long, Integer>();		public Map<Long, Integer> getMap() { return map; }
																		public Set<Long> getKeys() { return map.keySet(); }																	
	private SampleSpecs specs;											public SampleSpecs getSpecs() { return specs; }
	
	public BitsMap(SampleSpecs specs)
	{
		this.specs = specs;
		load();
	}
	public BitsMap(SampleSpecs specs, long[] arr)
	{
		this.specs = specs;
		addToMap(arr);
	}
	public static boolean exists(SampleSpecs specs)
	{
		return new File(specs.getTableFile(GameOfLife.LOCAL_DIR)).exists();
	}
	private void load()
	{
		long[] arr = FastIO.readLongArray(filename());
		addToMap(arr);
	}
	public void save()
	{
		long[] arr = mapToArr();
		FastIO.writeLongArray(filename(), arr);
	}
	public String filename()
	{
		return specs.getMapFile(GameOfLife.LOCAL_DIR);
	}
	private void addToMap(long[] arr)
	{
		for(int a = 0; a < arr.length; a ++)
		{
			map.put(arr[a], a);
		}
	}
	private long[] mapToArr()
	{
		long[] arr = new long[map.size()];
		for(Long key : map.keySet())
		{
			arr[map.get(key)] = key;
		}
		return arr;
	}
	public boolean containsKey(long key)
	{
		return map.containsKey(key);
	}
	public int get(long key)
	{
		return map.get(key);
	}
	public int size()
	{
		return map.size();
	}
}
