package data;

import game.Board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tuning.Parameters;

public class Solution
{	
	private Board board;
	private int delta;
	private int id;								public int getId() { return id; }
	private List<PredictionData>[][] data;
	
	@SuppressWarnings("unchecked")
	public Solution(Board board, int id)
	{
		this.id = id;
		this.board = board;
		data = new List[board.getNumRows()][board.getNumCols()];
		for(int a = 0; a < board.getNumRows(); a ++ )
		{
			for(int b = 0; b < board.getNumRows(); b ++ )
			{
				data[a][b] = new ArrayList<PredictionData>();
			}
		}
	}
	public void addData(DataSet dataSet)
	{
		delta = dataSet.getSpecs().getDelta();
		for(int a = 0; a < board.getNumRows(); a ++ )
		{
			for(int b = 0; b < board.getNumRows(); b ++ )
			{
				data[a][b].add(dataSet.predict(board, a, b, Parameters.WEIGHTS[delta])); //Null makes it not use weights
			}
		}
	}
	/** Returns detailed statistics about the given solution */
	public Map<Integer, PredictionStats> getStats(Board actualBoard)
	{
		Map<Integer, PredictionStats> predictionsMap = new HashMap<>();
		int[][] results = new int[board.getNumRows()][board.getNumCols()];
		int[][] actualData = actualBoard.getData();
		for(int a = 0; a < board.getNumRows(); a ++)
		{
			for(int b = 0; b < board.getNumRows(); b ++ )
			{
				//Combine the list of predictions into one final prediction
				Prediction prediction = combine(data[a][b]);
				
				//add prediction for this set of bitsPerCollection if it doesn't exist
				if(!predictionsMap.containsKey(prediction.bitsPerCollection))
				{
					predictionsMap.put(prediction.bitsPerCollection, new PredictionStats(prediction.bitsPerCollection));
				}
				
				PredictionStats stats = predictionsMap.get(prediction.bitsPerCollection);
				if(prediction.probability > 0.5d)
				{
					results[a][b] = 1;
				}
				else
				{
					results[a][b] = 0;
				}
				if(results[a][b] == actualData[a][b])
				{
					stats.addCorrect(prediction.confidence);
				}
				else
				{
					stats.addIncorrect(prediction.confidence);
				}
			}
		}
		return predictionsMap;
	}
	/** Simply returns the predicted board bypassing the memory-intensive data collection method */
	public Board getBoard(List<DataSet> datasets)
	{
		int[][] results = new int[board.getNumRows()][board.getNumCols()];
		List<PredictionData> predictions = new ArrayList<>();
		for(int a = 0; a < board.getNumRows(); a ++ )
		{
			for(int b = 0; b < board.getNumRows(); b ++ )
			{
				predictions.clear();
				for(DataSet dataset : datasets)
				{
					PredictionData predictionData = dataset.predict(board, a, b, Parameters.WEIGHTS[dataset.getSpecs().getDelta()]);
					predictions.add(predictionData);
				}
				//Combine the list of predictions into one final prediction
				Prediction prediction = combine(predictions);
				if(prediction.probability > 0.5d) //because its not a real probability
				{
					results[a][b] = 1;
				}
				else
				{
					results[a][b] = 0;
				}
			}
		}
		return new Board(results);
	}
	private Prediction combine(List<PredictionData> predictionSets)
	{
		Collections.sort(predictionSets);
		double weightedProbability = 0;
		double totalConfidence = 0;
		int lowestBitsPerCollection = 0;
		for(int dataIndex = 0; dataIndex < predictionSets.size(); dataIndex++)
		{
			PredictionData dataSet = predictionSets.get(dataIndex);
			//if the prediction's confidence is high enough to make a clear decision
			if(dataSet.getConfidence() > Parameters.CONFIDENCE_THRESHOLD[dataSet.getDelta()][dataSet.tier()] || dataIndex == predictionSets.size() - 1)
			{
				weightedProbability += dataSet.getProbability() * dataSet.getConfidence(); 
				totalConfidence += dataSet.getConfidence();
				lowestBitsPerCollection = dataSet.getBitsPerCollection();
				break;
			}
			//else if the prediction's confidence is high enough to make a contribution, but not enough to decide
			else if(dataSet.getConfidence() > Parameters.CONTRIBUTION_CONFIDENCE[dataSet.getDelta()][dataSet.tier()])
			{
				double confidence = dataSet.getConfidence();
				weightedProbability += dataSet.getProbability() * confidence;
				totalConfidence += confidence;
			}
		}
		weightedProbability /= totalConfidence;
		//Probability domain IS NOT 0 to 1... thank you weird weights
		return new Prediction(lowestBitsPerCollection, weightedProbability, totalConfidence);
	}
	private class Prediction
	{
		int bitsPerCollection;
		double probability;
		double confidence;
		private Prediction(int bitsPerCollection, double probability, double confidence)
		{
			this.bitsPerCollection = bitsPerCollection;
			this.probability = probability;
			this.confidence = confidence;
		}
	}
}
