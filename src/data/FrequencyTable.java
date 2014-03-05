package data;

import java.io.File;

import game.GameOfLife;
import game.SampleSpecs;
import utils.FastIO;

public class FrequencyTable 
{
	int[] table;												public int[] getTable() { return table; }
	private SampleSpecs specs;									public SampleSpecs getSpecs() { return specs; }
	private int bitsPerCollection;
	
	public FrequencyTable(SampleSpecs specs)
	{
		this.specs = specs;
		initVars();
		readTable();
	}
	public FrequencyTable(int[] table, SampleSpecs specs)
	{
		this.table = table;
		this.specs = specs;
		initVars();
	}
	/** Clears out frequency data for keys that are not in the partial map. Returns a regular FrequencyTable */
	public static FrequencyTable reduce(SampleSpecs specs, BitsMap partialMap, FrequencyTable fullTable, BitsMap fullMap)
	{
		int[] tableData = new int[partialMap.getKeys().size() * (specs.bitsPerSample() + 1)];
		for(long key : partialMap.getKeys())
		{
			int originalIndex = fullMap.get(key);
			int newIndex = partialMap.get(key);
			for(int a = 0; a < specs.bitsPerSample() + 1; a ++)
			{
				tableData[newIndex * (specs.bitsPerSample() + 1) + a] = fullTable.getRaw(originalIndex, a);
			}
		}
		return new FrequencyTable(tableData, specs);
	}
	
	private void initVars()
	{
		bitsPerCollection = specs.bitsPerCollection();
	}

	public void save()
	{
		FastIO.writeIntArray(filename(), table);
	}
	private void readTable()
	{
		table = FastIO.readIntArray(filename());
		System.gc();
	}
	public static boolean exists(SampleSpecs specs)
	{
		return new File(specs.getTableFile(GameOfLife.LOCAL_DIR)).exists();
	}
	public double get(int index, int subIndex)
	{
		return ((double) table[index * (bitsPerCollection + 1) + subIndex + 1] / (int) table[index * (bitsPerCollection + 1)]);
	}
	/** subIndex 0 is the number of samples */
	public int getRaw(int index, int subIndex)
	{
		return table[index * (bitsPerCollection + 1) + subIndex];
	}
	public int numSamples(int index)
	{
		return (int) table[index * (bitsPerCollection + 1)];
	}
	public int length()
	{
		return table.length / (bitsPerCollection + 1);
	}
	public String filename()
	{
		return specs.getTableFile(GameOfLife.LOCAL_DIR);
	}
}
