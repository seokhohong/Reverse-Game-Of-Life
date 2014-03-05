package apps;

import game.SampleSpecs;
import data.*;

/** Checks the frequency table for abnormalities and gives an approximate idea of how much sampling has occurred. 
 *  Better metric is the number of samples added per 100k boards: BoardGenerator.c's output when building the table. 
 * 
 *  Probably should have built better distribution metrics, but this tool started to serve less use after the first bugs between Java/C file IO were fixed
 * */

public class ValidateSampling 
{
	private static final int DIM = 10;
	private static final int DELTA = 1;
	private static final SampleSpecs specs = new SampleSpecs(DIM, DIM, DIM, DIM, DELTA);
	private static final int COLLECTION_BITS = specs.bitsPerCollection();
	private static final int ZERO_SAMPLES = 0;
	private static final int NO_SAMPLES = 10;
	private static final int VERY_LOW_SAMPLES = 100;
	private static final int LOW_SAMPLES = 10000;
	private FrequencyTable frequencyTable = new FrequencyTable(specs);
	//private FrequencyTable frequencyTable = new CompactTable(specs);
	
	public static void main(String[] args)
	{
		new ValidateSampling().tableIntegrityCheck();
	}
	private void tableIntegrityCheck()
	{
		int nonFullSamples = 0;
		int veryLowSamples = 0;
		int lowSamples = 0;
		int noSamples = 0;
		int zeroSamples = 0;
		int[] table = frequencyTable.getTable();
		for(int a = 0; a < table.length / (COLLECTION_BITS + 1); a ++)
		{
			if(table[a * (COLLECTION_BITS + 1)] > 1000010)
			{
				System.out.println("Too Many Samples");
			}
			else if(table[a * (COLLECTION_BITS + 1)] < 1000000)
			{
				nonFullSamples ++;
			}
			if(table[a * (COLLECTION_BITS + 1)] < VERY_LOW_SAMPLES)
			{
				veryLowSamples ++;
			}
			if(table[a * (COLLECTION_BITS + 1)] < LOW_SAMPLES)
			{
				lowSamples ++;
			}
			if(table[a * (COLLECTION_BITS + 1)] < NO_SAMPLES)
			{
				noSamples ++;
			}
			if(table[a * (COLLECTION_BITS + 1)] == ZERO_SAMPLES)
			{
				zeroSamples ++;
			}

			for(int b = 0; b < COLLECTION_BITS; b ++)
			{
				if((double) table[a * (COLLECTION_BITS + 1) + b + 1] / table[a * (COLLECTION_BITS + 1)] > 1.1)
				{
					System.out.println("Probability Too High");
				}
			}
		}
		System.out.println("NonFullSamples: "+nonFullSamples+" of "+(table.length / (COLLECTION_BITS + 1)));
		System.out.println("Low Samples: "+lowSamples);
		System.out.println("Very Low Samples: "+veryLowSamples);
		System.out.println("Almost No Samples: "+noSamples);
		System.out.println("Zero Samples: "+zeroSamples);
	}
}
