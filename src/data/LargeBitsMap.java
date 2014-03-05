package data;

import game.GameOfLife;
import game.SampleSpecs;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import utils.ArrayUtils;
import utils.FastIO;

/** BitsMap using Key's instead of long's (which makes it much slower) for samples of large dimensions */
public class LargeBitsMap 
{
	public static void main(String[] args)
	{
		LargeBitsMap lbitsMap = new LargeBitsMap(new SampleSpecs(10, 10, 10, 10, 2), 2);
		long[] arr = FastIO.readLongArray(lbitsMap.filename());
		long[] back = lbitsMap.mapToArr();
		System.out.println(ArrayUtils.print(Arrays.copyOf(arr, 100)));
		System.out.println(ArrayUtils.print(Arrays.copyOf(back, 100)));
	}
	
	
	private Map<Key, Integer> map = new HashMap<>();					public Map<Key, Integer> getMap() { return map; }
																		public Set<Key> getKeys() { return map.keySet(); }
	private SampleSpecs specs;											public SampleSpecs getSpecs() { return specs; }				
	private int divisions;
	public LargeBitsMap(SampleSpecs specs, int divisions)
	{
		this.specs = specs;
		this.divisions = divisions;
		load();
	}
	public LargeBitsMap(SampleSpecs specs, Key[] keys)
	{
		this.specs = specs;
		this.divisions = keys[0].getKey().length;
		for(int a = 0; a < keys.length; a++)
		{
			map.put(keys[a], a);
		}
	}
	private void load()
	{
		long[] arr = FastIO.readLongArray(filename());
		arrToMap(arr);
	}
	public void save()
	{
		FastIO.writeLongArray(filename(), mapToArr());
	}
	private long[] mapToArr()
	{
		long[] arr = new long[map.size() * divisions];
		for(Key key : map.keySet())
		{
			for(int a = 0 ; a < divisions; a++)
			{
				arr[map.get(key) * divisions + a] = key.getKey()[a];
			}
		}
		return arr;
	}
	private void arrToMap(long[] arr)
	{
		for(int a = 0; a < arr.length / divisions; a ++)
		{
			long[] key = new long[divisions];
			for(int b = 0; b < divisions; b ++)
			{
				key[b] = arr[a * divisions + b];
			}
			map.put(new Key(key), a); //key is just a convenient index to map to
		}
	}
	public String filename()
	{
		return specs.getMapFile(GameOfLife.LOCAL_DIR);
	}
	public static boolean exists(SampleSpecs specs)
	{
		return new File(specs.getTableFile(GameOfLife.LOCAL_DIR)).exists();
	}
	public boolean containsKey(Key key)
	{
		return map.containsKey(key);
	}
	public int get(Key key)
	{
		return map.get(key);
	}
	public int size()
	{
		return map.size();
	}
}
