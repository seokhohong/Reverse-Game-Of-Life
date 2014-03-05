package data;

import java.util.*;

/** Stores basic error data. Can easily be modified to store more data */
public class PredictionStats 
{
	private int bitsPerCollection;
	//Confidence distribution for correct and incorrect results
	private List<Double> correctConfidence = new ArrayList<>();
	private List<Double> incorrectConfidence = new ArrayList<>();
	private int correct = 0;
	private int incorrect = 0;
	
	public PredictionStats(int bitsPerCollection)
	{
		this.bitsPerCollection = bitsPerCollection;
	}
	public void addCorrect(double confidence)
	{
		correctConfidence.add(confidence);
		correct ++;
	}
	public void addIncorrect(double confidence)
	{
		incorrectConfidence.add(confidence);
		incorrect ++;
	}
	public static PredictionStats merge(List<PredictionStats> statsList, int bitsPerCollection)
	{
		PredictionStats combinedStats = new PredictionStats(bitsPerCollection);
		for(PredictionStats stats : statsList)
		{
			combinedStats.correct += stats.correct;
			combinedStats.incorrect += stats.incorrect;
			combinedStats.correctConfidence.addAll(stats.correctConfidence);
			combinedStats.incorrectConfidence.addAll(stats.incorrectConfidence);
		}
		return combinedStats;
	}
	public List<Double> confidenceDistribution(List<Double> wholeList, int numMarkers)
	{
		List<Double> distribution = new ArrayList<>();
		Collections.sort(wholeList);
		for(int a = 0; a < wholeList.size(); a += Math.max(1, wholeList.size() / numMarkers))
		{
			distribution.add(wholeList.get(a));
		}
		return distribution;
	}
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("For "+bitsPerCollection+" bit collections:\n");
		Collections.sort(correctConfidence);
		builder.append("Correct: " + correct + " " + confidenceDistribution(correctConfidence, 10)+"\n");
		builder.append("Incorrect: " + incorrect + " " + confidenceDistribution(incorrectConfidence, 10)+"\n");
		return builder.toString();
	}
	public double error()
	{
		return (double) incorrect / (correct + incorrect);
	}
}
