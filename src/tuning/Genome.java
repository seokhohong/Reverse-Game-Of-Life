package tuning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import utils.ArrayUtils;
import data.DataSet;
import data.PredictionData;
import game.Board;
import game.SampleTypes;
import game.TrainingSet;

public class Genome implements Comparable<Genome>
{
	private static final Random rnd = new Random();
	
	private static final double MUTATION_RATE = 0.1d;
	private static final double CROSSOVER_RATE = 0.1d;
	private static final double GRAB_NEIGHBOR = 0.05d;
	private static final double COPY_RATE = 0.1d;
	
	private double[][] weights;
	private int[] confThresholds;
	private int[] confPassing;
	
	private int generation = 0;
	private double error;							public double getError() { return error; }
	
	public Genome()
	{
		confThresholds = new int[TuneParameters.NUM_DIMS];
		confPassing = new int[TuneParameters.NUM_DIMS];
		initRandomWeights();
		initRandomConfThresholds();
		initRandomConfPassing();
	}
	private Genome(double[][] weights, int[] confThresholds, int[] confPassing, int generation)
	{
		this.weights = weights;
		this.confThresholds = confThresholds;
		this.confPassing = confPassing;
		this.generation = generation;
	}
	public void initUnmutatedPresets(int delta)
	{
		weights = new double[TuneParameters.NUM_DIMS][];
		for(int a = 0; a < TuneParameters.NUM_DIMS; a ++)
		{
			weights[a] = Arrays.copyOf(Parameters.WEIGHTS[delta][a], Parameters.WEIGHTS[delta][a].length);
		}
		confThresholds = Arrays.copyOf(Parameters.CONFIDENCE_THRESHOLD[delta], Parameters.CONFIDENCE_THRESHOLD[delta].length);
		confPassing = Arrays.copyOf(Parameters.CONTRIBUTION_CONFIDENCE[delta], Parameters.CONTRIBUTION_CONFIDENCE[delta].length);
		
	}
	public void initPresets(int delta)
	{
		weights = new double[TuneParameters.NUM_DIMS][];
		for(int a = 0; a < TuneParameters.NUM_DIMS; a ++)
		{
			weights[a] = Arrays.copyOf(Parameters.WEIGHTS[delta][a], Parameters.WEIGHTS[delta][a].length);
			for(int b = 0; b < weights[a].length; b++)
			{
				weights[a][b] *= 1d + (rnd.nextGaussian() * 0.4d);
			}
		}
		confThresholds = Arrays.copyOf(Parameters.CONFIDENCE_THRESHOLD[delta], Parameters.CONFIDENCE_THRESHOLD[delta].length);
		for(int b = 0; b < confThresholds.length; b++)
		{
			confThresholds[b] *= 1d + (rnd.nextGaussian() * 0.4d);
		}
		confPassing = Arrays.copyOf(Parameters.CONTRIBUTION_CONFIDENCE[delta], Parameters.CONTRIBUTION_CONFIDENCE[delta].length);
		for(int b = 0; b < confPassing.length; b++)
		{
			confPassing[b] *= 1d + (rnd.nextGaussian() * 0.4d);
		}
	}
	private void initRandomWeights()
	{
		weights = new double[TuneParameters.NUM_DIMS][];
		for(int a = 0; a < TuneParameters.NUM_DIMS; a ++)
		{
			weights[a] = new double[SampleTypes.getInstance().numTypes(a + TuneParameters.MIN_DIM) + 1];
			for(int b = 0; b < weights[a].length; b++)
			{
				weights[a][b] = rnd.nextDouble() * 5d; 
			}
		}
	}
	private void initRandomConfThresholds()
	{
		for(int a = 0; a < TuneParameters.NUM_DIMS; a ++)
		{
			confThresholds[a] = rnd.nextInt(300) + 20;
		}
	}
	private void initRandomConfPassing()
	{
		for(int a = 0; a < TuneParameters.NUM_DIMS; a ++)
		{
			confPassing[a] = rnd.nextInt(100) + 5;
		}
	}
	private static int[][] extractConfThresholds(List<Genome> genomes)
	{
		int[][] allConfThresholds = new int[genomes.size()][];
		for(int a = 0; a < genomes.size(); a ++)
		{
			allConfThresholds[a] = genomes.get(a).confThresholds;
		}
		return allConfThresholds;
	}
	private static int[][] extractConfPassing(List<Genome> genomes)
	{
		int[][] allConfPassing = new int[genomes.size()][];
		for(int a = 0; a < genomes.size(); a ++)
		{
			allConfPassing[a] = genomes.get(a).confPassing;
		}
		return allConfPassing;
	}
	public Genome mutate(List<Genome> others)
	{
		double[][] weights = mutateWeights(others);
		int[][] allConfThresholds = extractConfThresholds(others);
		int[][] extractConfPassings = extractConfPassing(others);
		int[] confThresholds = mutateIntArray(this.confThresholds, allConfThresholds);
		int[] confPassing = mutateIntArray(this.confPassing, extractConfPassings);
		return new Genome(weights, confThresholds, confPassing, generation + 1);
	}
	private double deviation()
	{
		return (1d / (Math.log(generation) + 20));
	}
	private double mutate(double val)
	{
		double mult = 1;
		double add = 0;
		if(rnd.nextDouble() < MUTATION_RATE)	
		{
			mult = 1 + ((rnd.nextGaussian() * 2) * deviation());
		}
		if(rnd.nextDouble() < MUTATION_RATE)
		{
			add = ((rnd.nextGaussian() * 2) * deviation());
			if(Math.abs(val) < 0.000001d)
			{
				val++;
			}
		}
		return val * mult + (val * add);
	}
	//Limit is the largest number that can be returned
	private int getSwapIndex(int originalIndex, int limit)
	{
		int otherIndex = originalIndex;
		if(rnd.nextDouble() < CROSSOVER_RATE)
		{
			if(rnd.nextDouble() < GRAB_NEIGHBOR && otherIndex > 0)
			{
				otherIndex --;
			}
			if(rnd.nextDouble() < GRAB_NEIGHBOR && otherIndex < limit - 1)
			{
				otherIndex ++;
			}
		}
		return otherIndex;
	}
	private int[] mutateIntArray(int[] myWeights, int[][] others)
	{
		int[] newWeights = new int[myWeights.length];
		for(int a = 0; a < myWeights.length; a ++)
		{
			newWeights[a] = (int) mutate(myWeights[a]);
			int otherIndex = getSwapIndex(a, newWeights.length);
			newWeights[a] = others[rnd.nextInt(others.length)][otherIndex];
		}
		return newWeights;
	}
	private double[][] mutateWeights(List<Genome> others)
	{
		double[][] newWeights = new double[weights.length][];
		if(rnd.nextDouble() < COPY_RATE)
		{
			for(int a = 0; a < weights.length; a ++)
			{
				int whichOther = rnd.nextInt(others.size());
				newWeights[a] = Arrays.copyOf(others.get(whichOther).weights[a], weights[a].length);
			}
			return newWeights;
		}
		for(int a = 0; a < weights.length; a ++)
		{
			double[] weightSet = weights[a];
			double[] newWeightSet = new double[weightSet.length];
			for(int b = 0; b < weightSet.length; b ++)
			{
				newWeightSet[b] = mutate(weightSet[b]);
				int otherIndex = getSwapIndex(b, weightSet.length);
				int whichOther = rnd.nextInt(others.size());
				newWeightSet[b] = others.get(whichOther).weights[a][otherIndex];
			}
			newWeights[a] = newWeightSet;
		}
		return newWeights;
	}
	public void calculateError(List<TrainingSet> trainingSets, Map<Integer, DataSet> dataSets)
	{
		int numGuesses = 0;
		int numIncorrect = 0;
		for(TrainingSet tSet : trainingSets)
		{
			Board endBoard = tSet.getEndBoard();
			int[][] startData = tSet.getStartBoard().getData();
			for(int a = 0; a < endBoard.getNumRows(); a++)
			{
				for(int b = 0; b < endBoard.getNumCols(); b++)
				{
					double prediction = predict(endBoard, a, b, dataSets);
					int guess = (int) Math.round(prediction);
					if(guess != startData[a][b])
					{
						numIncorrect ++;
					}
					numGuesses ++;
				}
			}
		}
		error = (double) numIncorrect / numGuesses;
	}
	//Very similar code to what is in Parameters.java, but different enough that it just was copied
	private double predict(Board board, int a, int b, Map<Integer, DataSet> dataSets)
	{
		double weightedProbability = 0;
		double totalConfidence = 0;
		int lowestBitsPerCollection = smallest(dataSets.keySet());
		List<Integer> ordered = new ArrayList<>();
		ordered.addAll(dataSets.keySet());
		Collections.sort(ordered);
		Collections.reverse(ordered);
		for(Integer dim : ordered)
		{
			PredictionData prediction = dataSets.get(dim).predict(board, a, b, weights);
			//if the prediction's confidence is high enough to make a clear decision
			if(prediction.getConfidence() > confThresholds[prediction.tier()] || dim == lowestBitsPerCollection)
			{
				weightedProbability += prediction.getProbability() * prediction.getConfidence(); 
				totalConfidence += prediction.getConfidence();
				break;
			}
			//else if the prediction's confidence is high enough to make a contribution, but not enough to decide
			else if(prediction.getConfidence() > confPassing[prediction.tier()])
			{
				double confidence = prediction.getConfidence();
				weightedProbability += prediction.getProbability() * confidence;
				totalConfidence += confidence;
			}
		}
		weightedProbability /= totalConfidence;
		return weightedProbability;
	}
	private int smallest(Set<Integer> set)
	{
		int smallest = Integer.MAX_VALUE;
		for(int i : set)
		{
			smallest = Math.min(i, smallest);
		}
		return smallest;
	}
	
	public int compareTo(Genome genome)
	{
		return Double.compare(error, genome.error);
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < weights.length; a ++)
		{
			builder.append(ArrayUtils.print(weights[a])+"\n");
		}
		builder.append(ArrayUtils.print(confThresholds)+"\n");
		builder.append(ArrayUtils.print(confPassing)+"\n");
		return builder.toString();
	}
}
