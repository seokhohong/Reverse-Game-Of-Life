package game;

import java.util.ArrayList;
import java.util.List;
import utils.Read;

public class TrainingSet
{
	private Board endBoard;					public Board getEndBoard() { return endBoard; }
	private Board startBoard;				public Board getStartBoard() { return startBoard; }
	
	private int delta;						public int getDelta() { return delta; }
	private int id;							public int getId() { return id; }
	
	public TrainingSet(String line)
	{
		String[] values = line.split(",");
		int[][] startBoard = new int[GameOfLife.HEIGHT][GameOfLife.WIDTH];
		for(int a = GameOfLife.HEIGHT; a --> 0; )
		{
			for(int b = GameOfLife.WIDTH; b --> 0; )
			{
				startBoard[a][b] = Integer.parseInt(values[b * GameOfLife.HEIGHT + a + 2].trim());
			}
		}
		int readOffset = 2 + GameOfLife.HEIGHT * GameOfLife.WIDTH; 
		int[][] endBoard = new int[GameOfLife.HEIGHT][GameOfLife.WIDTH];
		for(int a = GameOfLife.HEIGHT; a --> 0; )
		{
			for(int b = GameOfLife.WIDTH; b --> 0; )
			{
				endBoard[a][b] = Integer.parseInt(values[b * GameOfLife.HEIGHT + a + readOffset].trim());
			}
		}
		this.startBoard = new Board(startBoard);
		this.endBoard = new Board(endBoard);
		this.id = Integer.parseInt(values[0]);
		this.delta = Integer.parseInt(values[1]);
	}
	
	public TrainingSet(Board startBoard, Board endBoard, int delta)
	{
		this.startBoard = startBoard;
		this.endBoard = endBoard;
		this.delta = delta;
		this.id = -1;
	}
	
	public static List<TrainingSet> load(String filename)
	{
		List<String> lines = Read.from(filename);
		List<TrainingSet> trainingSet = new ArrayList<>();
		for(int a = lines.size(); a --> 1; )
		{
			trainingSet.add(new TrainingSet(lines.get(a)));
		}
		return trainingSet;
	}
	
	public static List<TrainingSet> load(String filename, int delta)
	{
		List<String> lines = Read.from(filename);
		List<TrainingSet> trainingSet = new ArrayList<>();
		for(int a = lines.size(); a --> 1; )
		{
			TrainingSet newTrainingSet = new TrainingSet(lines.get(a));
			if(newTrainingSet.getDelta() == delta)
			{
				trainingSet.add(newTrainingSet);
			}
		}
		return trainingSet;
	}
	public static List<TrainingSet> load(String filename, int delta, int maxCount)
	{
		List<String> lines = Read.from(filename);
		List<TrainingSet> trainingSet = new ArrayList<>();
		for(int a = lines.size(); a --> 1; )
		{
			TrainingSet newTrainingSet = new TrainingSet(lines.get(a));
			if(newTrainingSet.getDelta() == delta)
			{
				trainingSet.add(newTrainingSet);
				if(trainingSet.size() > maxCount)
				{
					break;
				}
			}
		}
		return trainingSet;
	}
}
