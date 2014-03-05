package apps;

import game.Board;
import game.SampleExtractor;
import game.TestingSet;
import game.TrainingSet;
import data.Key;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import utils.FastIO;

/** 
 *
 * Counts the number of unique samples that the BitsMap should be accounting for
 * 
 **/
public class CountUniqueBoards 
{
	int dim = 10;
	int delta = 1;
	int keyDivisions = dim * dim / 65 + 1;
	
	String filePrefix = dim+Integer.toString(dim)+delta;
	List<TrainingSet> trainingSets = TrainingSet.load("2000train.csv", delta); //which data sets it reads from
	List<TestingSet> testingSets = TestingSet.load("test.csv", delta);
	public CountUniqueBoards()
	{
		System.out.println("Done Loading");
	}
	public static void main(String[] args)
	{
		new CountUniqueBoards().write();
	}
	private void write()
	{
		if(dim <= 8)
		{
			writeSmall();
		}
		else
		{
			writeLarge();
		}
	}
	private void writeLarge()
	{
		Set<Key> uniqueSamples = new HashSet<>();
		uniqueSamples.addAll(SampleExtractor.getLargeTrainingSamples(trainingSets, keyDivisions));
		uniqueSamples.addAll(SampleExtractor.getLargeTestingSamples(testingSets, keyDivisions));
		writeBinary(uniqueSamples, keyDivisions);
		System.out.println("Unique Samples "+uniqueSamples.size());
	}
	private void writeSmall()
	{
		Set<Long> uniqueSamples = new HashSet<>();
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
		for(TestingSet testingSet : testingSets)
		{
			Board board = testingSet.getEndBoard();
			for(int a = -dim + 1; a < board.getNumRows(); a ++)
			{
				for(int b = -dim + 1; b < board.getNumCols(); b++)
				{
					uniqueSamples.add(board.getBitsOptimized(a, b, dim, dim));
				}
			}
		}
		writeBinary(uniqueSamples);
		System.out.println("Unique Samples "+uniqueSamples.size());
	}
	private void writeBinary(Set<Long> uniqueSamples)
	{
		long[] longArray = new long[uniqueSamples.size()];
		Iterator<Long> sampleIter = uniqueSamples.iterator();
		for(int a = 0; a < longArray.length; a++)
		{
			longArray[a] = sampleIter.next();
		}
		FastIO.writeLongArray(filePrefix+"mapBinary.txt", longArray);
	}
	private void writeBinary(Set<Key> uniqueSamples, int listLength)
	{
		long[] longArray = new long[uniqueSamples.size() * listLength];
		Iterator<Key> sampleIter = uniqueSamples.iterator();
		int index = 0;
		while(sampleIter.hasNext())
		{
			Key currElem = sampleIter.next();
			for(Long val : currElem.getKey())
			{
				longArray[index] = val;
				index ++ ;
			}
		}
		FastIO.writeLongArray(filePrefix+"mapBinary.txt", longArray);
	}
}
