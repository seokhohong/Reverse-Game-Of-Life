package apps;

import game.Board;
import game.SampleExtractor;
import game.SampleSpecs;
import game.TrainingSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import data.BitsMap;
import data.CompactBitsMap;
import data.CompactLargeBitsMap;
import data.CompactTable;
import data.FrequencyTable;
import data.Key;
import data.LargeBitsMap;

/** Reduces the data to just what is necessary for a particular set of TrainingSets
 * 
 * 	Using the whole dataset incurs incredibly cache misses when evaluating large numbers of boards (particularly for tuning)
 *  thus Compact versions of the table/bitsmap are necessary.
 * 
 * */
public class CompactData 
{
	private static final boolean VERBOSE = true;
	
	private SampleSpecs specs;
	private List<TrainingSet> trainingSets;
	private BitsMap bitsMap;						public BitsMap getBitsMap() { return bitsMap; }
	private BitsMap originalBitsMap;
	private LargeBitsMap largeBitsMap;				public LargeBitsMap getLargeBitsMap() { return largeBitsMap; }
	private LargeBitsMap originalLargeBitsMap;		public LargeBitsMap getOriginalLargeBitsMap() { return originalLargeBitsMap; }
	private CompactTable table;						public CompactTable getCompactTable() { return table; }
	private FrequencyTable originalTable;
	
	public static void main(String[] args)
	{
		for(int a = 1; a <= 5; a ++)
		{
			for(int dim : Arrays.asList(5, 6, 7, 8, 10))
			{
				List<TrainingSet> trainingSets = TrainingSet.load("2000train.csv", a);
				new CompactData(new SampleSpecs(dim, dim, dim, dim, a), trainingSets);
			}
		}
	}
	
	public CompactData(SampleSpecs specs, List<TrainingSet> trainingSets)
	{
		this.specs = specs;
		this.trainingSets = trainingSets;
		generate();
	}
	private void generate()
	{
		if(!specs.isLarge())
		{
			if(CompactBitsMap.exists(specs) && BitsMap.exists(specs))
			{
				if(VERBOSE) System.out.println("BitsMap exists for "+specs);
				bitsMap = new CompactBitsMap(specs);
			}
			else
			{
				if(VERBOSE) System.out.println("Compacting BitsMap for "+specs);
				originalBitsMap = new BitsMap(specs);
				buildBitsMap();
			}
		}
		else
		{
			if(LargeBitsMap.exists(specs) && CompactLargeBitsMap.exists(specs))
			{
				if(VERBOSE) System.out.println("BitsMap exists for "+specs);
				largeBitsMap = new CompactLargeBitsMap(specs, specs.keyDivisions());
			}
			else
			{
				if(VERBOSE) System.out.println("Compacting BitsMap for "+specs);
				originalLargeBitsMap = new LargeBitsMap(specs, 2);
				buildLargeBitsMap();
			}
		}
		
		if(CompactTable.exists(specs) && FrequencyTable.exists(specs) && CompactTable.olderThanFrequencyTable(specs))
		{
			if(VERBOSE) System.out.println("Table exists for "+specs);
			table = new CompactTable(specs);
		}
		else
		{
			originalTable = new FrequencyTable(specs);
			if(VERBOSE) System.out.println("Compacting Table for "+specs);
			if(!specs.isLarge())
			{
				if(originalBitsMap == null)
				{
					originalBitsMap = new BitsMap(specs);
				}
				buildSmallCompactTable();
			}
			else
			{
				if(originalLargeBitsMap == null)
				{
					originalLargeBitsMap = new LargeBitsMap(specs, specs.keyDivisions());
				}
				buildLargeCompactTable();
			}
		}
		originalBitsMap = null;
		originalLargeBitsMap = null;
	}
	
	private void buildBitsMap()
	{
		Set<Long> uniqueSamples = new HashSet<>();
		int dim = specs.getCollectionRows();
		for(TrainingSet trainingSet : trainingSets)
		{
			Board board = trainingSet.getEndBoard();
			for(int a = -dim + 1; a < board.getNumRows(); a ++)
			{
				for(int b = -dim + 1; b < board.getNumCols(); b++)
				{
					uniqueSamples.add(board.getBitsOptimized(a, b, dim, dim));
				}
			}
		}
		long[] samples = new long[uniqueSamples.size()];
		Iterator<Long> sampleIter = uniqueSamples.iterator();
		for(int a = 0; a < samples.length; a++)
		{
			samples[a] = sampleIter.next();
		}
		bitsMap = new CompactBitsMap(specs, samples);
		bitsMap.save();
	}
	private void buildLargeBitsMap()
	{
		Set<Key> uniqueSamples = SampleExtractor.getLargeTrainingSamples(trainingSets, specs.keyDivisions());
		Key[] samples = new Key[uniqueSamples.size()];
		Iterator<Key> sampleIter = uniqueSamples.iterator();
		for(int a = 0; a < samples.length; a++)
		{
			samples[a] = sampleIter.next();
		}
		largeBitsMap = new CompactLargeBitsMap(specs, samples);
		largeBitsMap.save();
	}
	private void buildSmallCompactTable()
	{
		int[] table = new int[bitsMap.getKeys().size() * (specs.bitsPerSample() + 1)];
		for(long key : bitsMap.getKeys())
		{
			int originalIndex = originalBitsMap.get(key);
			int newIndex = bitsMap.get(key);
			for(int a = 0; a < specs.bitsPerSample() + 1; a ++)
			{
				table[newIndex * (specs.bitsPerSample() + 1) + a] = originalTable.getRaw(originalIndex, a);
			}
		}
		this.table = new CompactTable(table, specs);
		this.table.save();
	}
	private void buildLargeCompactTable()
	{
		int[] table = new int[largeBitsMap.getKeys().size() * (specs.bitsPerSample() + 1)];
		for(Key key : largeBitsMap.getKeys())
		{
			int originalIndex = originalLargeBitsMap.get(key);
			int newIndex = largeBitsMap.get(key);
			for(int a = 0; a < specs.bitsPerSample() + 1; a ++)
			{
				table[newIndex * (specs.bitsPerSample() + 1) + a] = originalTable.getRaw(originalIndex, a);
			}
		}
		this.table = new CompactTable(table, specs);
		/*
		for(Key key : largeBitsMap.getKeys())
		{
			int originalIndex = originalLargeBitsMap.get(key);
			int newIndex = largeBitsMap.get(key);
			for(int a = 0; a < specs.bitsPerSample() + 1; a ++)
			{
				if(this.table.getRaw(newIndex, a) != originalTable.getRaw(originalIndex, a))
				{
					System.out.println("Error");
				}
			}
		}
		*/
		this.table.save();
	}
}
