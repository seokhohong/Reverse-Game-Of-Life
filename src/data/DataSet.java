package data;

import game.Board;
import game.GameOfLife;
import game.SampleSpecs;
import game.SampleTypes;

import java.awt.Point;

/** Encapsulates all resources required to make a prediction about a given cell from one data file */
public class DataSet implements Comparable<DataSet>
{
	//Compact is for testing
	private static final boolean LOAD_COMPACT = false;
	private SampleSpecs specs;												public SampleSpecs getSpecs() { return specs; }
	private FrequencyTable table;
	private BitsMap bitsMap;
	private LargeBitsMap largeBitsMap;
	
	private boolean loaded = false;
	
	public DataSet(SampleSpecs specs)
	{
		this.specs = specs;
	}
	/** Does not load the data until used, so that we can control when this data gets loaded into memory */
	public void load()
	{
		if(loaded)
		{
			return;
		}
		loaded = !loaded;
		System.out.println("Loaded "+specs.getTableFile(GameOfLife.LOCAL_DIR));
		if(specs.isLarge())
		{
			//largeBitsMap = new CompactLargeBitsMap(specs, specs.keyDivisions());
			largeBitsMap = LOAD_COMPACT ? new CompactLargeBitsMap(specs, specs.keyDivisions()) : new LargeBitsMap(specs, specs.keyDivisions()); //for test boards
		}
		else
		{
			//bitsMap = new CompactBitsMap(specs);
			bitsMap = LOAD_COMPACT ? new CompactBitsMap(specs) : new BitsMap(specs);
		}
		table = LOAD_COMPACT ? new CompactTable(specs) : new FrequencyTable(specs);
		System.out.println("Loaded "+specs.getMapFile(GameOfLife.LOCAL_DIR));
	}
	/** Manual load */
	public void load(BitsMap bitsMap, CompactTable table)
	{
		this.bitsMap = bitsMap;
		this.table = table;
		loaded = true;
	}
	/** Manual load */
	public void load(LargeBitsMap largeBitsMap, CompactTable table)
	{
		this.largeBitsMap = largeBitsMap;
		this.table = table;
		loaded = true;
	}
	//Uses a hashmap to retrieve the corresponding index where the frequencies are stored given a particular bit sequence
	private int getFreqsTableIndex(Board endBoard, int a, int b, int subRow, int subCol)
	{
		load();
		int subRowOffset = (specs.getSampleRows() - specs.getCollectionRows()) / 2;
		int subColOffset = (specs.getSampleCols() - specs.getCollectionCols()) / 2;
		
		if(specs.isLarge())
		{
			//Unfortunately some slightly-confusing coordinate math has to happen here to extract the correct bits from the board
			Key key = endBoard.getBitsOptimizedLarge(a - subRow - subRowOffset, b - subCol - subColOffset, specs.getSampleRows(), specs.getSampleCols(), specs.keyDivisions());
			return largeBitsMap.get(key);
		}
		else
		{
			long bits = endBoard.getBitsOptimized(a - subRow - subRowOffset, b - subCol - subColOffset, specs.getSampleRows(), specs.getSampleCols());
			return bitsMap.get(bits);
		}
	}
	public PredictionData predict(Board endBoard, int a, int b, double[][] weights)
	{
		load();
		double meanProbability = 0;
		double totalWeight = 0;
		double confidence = 0; //arbitrarily scaled metric to measure the confidence in a given prediction
		int numSamples = 0;
		for(int subRow = 0; subRow < specs.getCollectionRows(); subRow ++) //add across all data points in the Collection
		{
			for(int subCol = 0; subCol < specs.getCollectionCols(); subCol++)
			{
				//subindex within the frequency table for the index of the given bit sequence
				int localIndex = subRow * specs.getCollectionCols() + subCol;
				int freqsIndex = getFreqsTableIndex(endBoard, a, b, subRow, subCol);
				//Index of -1 means it was not found
				if(table.numSamples(freqsIndex) > 0)
				{
					numSamples ++;
					double frequency = table.get(freqsIndex, localIndex);
					int sampleTypeIndex = SampleTypes.getInstance().indexOf(specs.getCollectionRows(), new Point(subRow, subCol));
					double weight = weights[specs.tier()][sampleTypeIndex] * weights[specs.tier()][weights[specs.tier()].length - 1];
					totalWeight += weight;
					confidence += Math.log(table.numSamples(freqsIndex)) * weight;
					meanProbability += frequency * weight;
				}
			}
		}
		double averageWeight = totalWeight / numSamples;
		meanProbability /= numSamples;
		meanProbability /= averageWeight;
		confidence /= averageWeight;

		if(Double.isNaN(confidence))
		{
			confidence = 0;
		}
		return new PredictionData(specs.getDelta(), specs.bitsPerSample(), meanProbability, confidence);
	}
	@Override
	public int compareTo(DataSet arg0) 
	{
		return arg0.specs.bitsPerSample() -  specs.bitsPerSample();
	}
}