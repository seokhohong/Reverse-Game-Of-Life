package tuning;

import game.SampleSpecs;
import game.TrainingSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apps.CompactData;
import utils.Choose;
import utils.ListUtils;
import utils.Multitasker;
import data.DataSet;
import data.FrequencyTable;

/** Optimizes parameters using genetic algorithms. Evaluation function cache misses like crazy...*/
public class TuneParameters 
{
	private static final int DELTA = 3;
	
	public static final int MIN_DIM = 5;
	private static final int MAX_DIM = 10;
	public static final int NUM_DIMS = MAX_DIM - MIN_DIM + 1;
	
	private static final int NUM_THREADS = 6;
	private static final boolean USE_PRESET = true;
	private static final boolean MUTATE_PRESET = false;
	
	private List<TrainingSet> trainingSets = new ArrayList<>();
	//for each size
	private Map<Integer, DataSet> datasets = new HashMap<>();
	
	public static void main(String[] args)
	{
		new TuneParameters().go();
	}
	private void go()
	{
		loadTrainingSets();
		loadDatasets();
		GenePool genePool = new GenePool(50, 25);
		for(int a = 0; a < 2000; a ++)
		{
			genePool.evaluate();
		}
	}
	private void loadTrainingSets()
	{
		trainingSets = TrainingSet.load("2000train.csv", DELTA);
	}
	private void loadDatasets()
	{
		for(int a = MIN_DIM; a <= MAX_DIM; a++)
		{
			SampleSpecs specs = new SampleSpecs(a, a, a, a, DELTA);
			if(FrequencyTable.exists(specs))
			{
				CompactData compactedData = new CompactData(specs, trainingSets);
				DataSet dataset = new DataSet(specs);
				if(specs.isLarge())
				{
					dataset.load(compactedData.getLargeBitsMap(), compactedData.getCompactTable());
				}
				else
				{
					dataset.load(compactedData.getBitsMap(), compactedData.getCompactTable());
				}
				datasets.put(specs.getCollectionRows(), dataset);
			}
		}
		System.out.println("Completed Loading");
	}
	class GenePool
	{
		private List<Genome> genes = new ArrayList<>();
		private int size;
		private int numKept;
		GenePool(int size, int numKept)
		{
			this.size = size;
			this.numKept = numKept;
			for(int a = 0; a < size; a++)
			{
				Genome genome = new Genome();
				if(USE_PRESET)
				{
					if(MUTATE_PRESET)
					{
						genome.initPresets(DELTA);
					}
					else
					{
						genome.initUnmutatedPresets(DELTA);
					}
				}
				genes.add(genome);
			}
		}
		void evaluate()
		{
			Multitasker multitasker = new Multitasker(NUM_THREADS, 10000);
			List<List<Genome>> divisions = ListUtils.divide(genes, NUM_THREADS);
			for(int a = 0; a < NUM_THREADS; a ++)
			{
				multitasker.load(new Evaluator(divisions.get(a)));
			}
			multitasker.done();
			Collections.sort(genes);
			System.out.println("Highest is "+genes.get(0).getError()+"\n" + genes.get(0).toString());
			List<Genome> nextGen = new ArrayList<>();
			List<Genome> best = genes.subList(0, numKept);
			while(nextGen.size() < size)
			{
				nextGen.add(Choose.from(best).mutate(best));
			}
			genes = nextGen;
		}
	}
	class Evaluator extends Thread
	{
		private List<Genome> genes;
		Evaluator(List<Genome> genes)
		{
			this.genes = genes;
		}
		@Override
		public void run()
		{
			for(Genome genome : genes)
			{
				genome.calculateError(trainingSets, datasets);
			}
		}
	}
}
