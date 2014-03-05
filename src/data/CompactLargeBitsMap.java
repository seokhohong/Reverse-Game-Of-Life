package data;

import game.GameOfLife;
import game.SampleSpecs;

import java.io.File;

public class CompactLargeBitsMap extends LargeBitsMap 
{
	public CompactLargeBitsMap(SampleSpecs specs, int divisions)
	{
		super(specs, divisions);
	}
	public CompactLargeBitsMap(SampleSpecs specs, Key[] keys)
	{
		super(specs, keys);
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
