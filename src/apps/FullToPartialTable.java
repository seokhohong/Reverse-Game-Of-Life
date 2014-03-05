package apps;

import utils.FastIO;
import game.SampleSpecs;
import data.BitsMap;
import data.FrequencyTable;

/** Reduce 50k training boards to 2k while keeping 50k test boards intact: cuts frequency table files by ~40% */
public class FullToPartialTable
{
	private static final int DIM = 6;
	private static final int DELTA = 3;
	private static final SampleSpecs specs = new SampleSpecs(DIM, DIM, DIM, DIM, DELTA);
	
	public static void main(String[] args)
	{
		new FullToPartialTable().go();
	}
	private void go()
	{
		FrequencyTable table = new FrequencyTable(specs);
		BitsMap fullMap = new BitsMap(specs);
		long[] partialData = FastIO.readLongArray(specs.getMapFile("C:\\Life\\Partials\\"));
		BitsMap partialMap = new BitsMap(specs, partialData);
		FrequencyTable partialTable = FrequencyTable.reduce(specs, partialMap, table, fullMap);
		partialTable.save();
		System.out.println("Done");
	}
}
