package data;

import game.GameOfLife;
import game.SampleSpecs;

import java.io.File;

public class CompactBitsMap extends BitsMap 
{
	public CompactBitsMap(SampleSpecs specs)
	{
		super(specs);
	}
	public CompactBitsMap(SampleSpecs specs, long[] arr)
	{
		super(specs, arr);
	}
	public static boolean exists(SampleSpecs specs)
	{
		return new File(specs.getCompactMapFile(GameOfLife.LOCAL_DIR)).exists();
	}
	@Override
	public String filename()
	{
		return getSpecs().getCompactMapFile(GameOfLife.LOCAL_DIR);
	}
}
