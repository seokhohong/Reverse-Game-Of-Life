package game;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import data.Key;

/** Extracts unique samples from a set of boards */
public class SampleExtractor 
{
	private static final int TOO_DENSE = 10; //for really large boards, we're almost guaranteed to not see highly dense samples, so why track them?
	
	public static Set<Key> getLargeTestingSamples(List<TestingSet> testingSets, int keyDivisions)
	{
		Set<Key> uniqueSamples = new HashSet<>();
		int dim = testingSets.get(0).getEndBoard().getNumRows();
		for(TestingSet testingSet : testingSets)
		{
			Board board = testingSet.getEndBoard();
			addLargeSamples(uniqueSamples, board, dim, keyDivisions);
		}
		removeDense(uniqueSamples, dim);
		return uniqueSamples;
	}
	public static Set<Key> getLargeTrainingSamples(List<TrainingSet> trainingSets, int keyDivisions)
	{
		Set<Key> uniqueSamples = new HashSet<>();
		int dim = trainingSets.get(0).getStartBoard().getNumRows();
		for(TrainingSet trainingSet : trainingSets)
		{
			Board board = trainingSet.getEndBoard();
			addLargeSamples(uniqueSamples, board, dim, keyDivisions);
		}
		removeDense(uniqueSamples, dim);
		return uniqueSamples;
	}
	private static void addLargeSamples(Set<Key> uniqueSamples, Board board, int dim, int keyDivisions)
	{
		for(int a = -dim + 1; a < board.getNumRows(); a ++)
		{
			for(int b = -dim + 1; b < board.getNumCols(); b++)
			{
				uniqueSamples.add(board.getBitsOptimizedLarge(a, b, dim, dim, keyDivisions));
			}
		}
	}
	private static void removeDense(Set<Key> uniqueSamples, int dim)
	{
		if(dim >= 12) //number of keys kind of gets out of control
		{
			Iterator<Key> sampleIter = uniqueSamples.iterator();
			while(sampleIter.hasNext())
			{
				Key currElem = sampleIter.next();
				if(currElem.numBitsSet() > TOO_DENSE)
				{
					sampleIter.remove();
				}
			}
		}
	}
}
