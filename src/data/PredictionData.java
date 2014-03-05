package data;

import tuning.Parameters;

/** Struct-like class to carry data about one prediction */
public class PredictionData implements Comparable<PredictionData>
{
	private int delta;																					public int getDelta() { return delta; }
	private double probability;
	private int bitsPerCollection;
	private double confidence; //how important this sampling is
	public PredictionData(int delta, int bitsPerCollection, double probability, double confidence)
	{
		this.delta = delta;
		setBitsPerCollection(bitsPerCollection);
		setProbability(probability);
		setConfidence(confidence);
	}
	@Override
	public int compareTo(PredictionData o) 
	{
		return o.getBitsPerCollection() - getBitsPerCollection();
	}
	public double getProbability() 
	{
		return probability;
	}
	public void setProbability(double probability) 
	{
		this.probability = probability;
	}
	public double getConfidence() 
	{
		return confidence;
	}
	public void setConfidence(double confidence) 
	{
		this.confidence = confidence;
	}
	public int getBitsPerCollection() 
	{
		return bitsPerCollection;
	}
	public void setBitsPerCollection(int bitsPerCollection) 
	{
		this.bitsPerCollection = bitsPerCollection;
	}
	public int tier()
	{
		return (int) Math.sqrt(bitsPerCollection) - Parameters.MIN_TIER;
	}
}