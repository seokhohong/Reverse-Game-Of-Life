package data;

import java.io.File;

import game.GameOfLife;
import game.SampleSpecs;

/** Reduces cache-misses */
public class CompactTable extends FrequencyTable
{
	public CompactTable(SampleSpecs specs)
	{
		super(specs);
	}
	public CompactTable(int[] table, SampleSpecs specs)
	{
		super(table, specs);
	}
	public static boolean exists(SampleSpecs specs)
	{
		return new File(specs.getCompactTableFile(GameOfLife.LOCAL_DIR)).exists();
	}
	public static boolean olderThanFrequencyTable(SampleSpecs specs)
	{
		return new File(specs.getCompactTableFile(GameOfLife.LOCAL_DIR)).lastModified() > new File(specs.getTableFile(GameOfLife.LOCAL_DIR)).lastModified();
	}
	@Override
	public String filename()
	{
		return getSpecs().getCompactTableFile(GameOfLife.LOCAL_DIR);
	}
}
