package game;

import tuning.Parameters;

public class SampleSpecs 
{
	//Part of a past attempt to work with different dimensions of samples versus collections... never saw much use, however
	private int sampleRows;				public int getSampleRows() { return sampleRows; }
	private int sampleCols;				public int getSampleCols() { return sampleCols; }
	private int collectionRows;			public int getCollectionRows() { return collectionRows; }
	private int collectionCols;			public int getCollectionCols() { return collectionCols; }
	private int delta;					public int getDelta() { return delta; }
	
	public SampleSpecs(int sampleRows, int sampleCols, int collectionRows, int collectionCols, int delta)
	{
		this.sampleRows = sampleRows;
		this.sampleCols = sampleCols;
		this.collectionRows = collectionRows;
		this.collectionCols = collectionCols;
		this.delta = delta;
	}
	
	public int bitsPerSample()
	{
		return sampleRows * sampleCols;
	}
	
	public int bitsPerCollection()
	{
		return collectionRows * collectionCols;
	}
	
	public String getMapFile(String localDir)
	{
		return localDir+"\\"+sampleRows+Integer.toString(sampleCols)+delta+"mapBinary.txt";
	}
	public String getCompactMapFile(String localDir)
	{
		return localDir+"\\"+sampleRows+Integer.toString(sampleCols)+collectionDims()+delta+"mapCompact.txt";
	}
	public String getTableFile(String localDir)
	{
		return localDir+"\\"+sampleRows+Integer.toString(sampleCols)+collectionDims()+delta+"tableCI.txt";
	}
	public String getCompactTableFile(String localDir)
	{
		return localDir+"\\"+sampleRows+Integer.toString(sampleCols)+collectionDims()+delta+"tableCompact.txt";
	}
	private String collectionDims()
	{
		if(collectionRows != sampleRows || collectionCols != sampleCols)
		{
			return collectionRows+Integer.toString(collectionCols);
		}
		return "";
	}
	
	public boolean isLarge()
	{
		return bitsPerSample() > 64;
	}
	
	public int keyDivisions()
	{
		return bitsPerSample() / 65 + 1;
	}
	public int tier()
	{
		return (int) Math.sqrt(bitsPerCollection()) - Parameters.MIN_TIER;
	}
	@Override
	public String toString()
	{
		return sampleRows + "_" + sampleCols + "_" + collectionRows + "_" + collectionCols + "_" + delta;
	}
}
