package game;

import java.util.ArrayList;
import java.util.List;

import utils.Read;

public class TestingSet
{
	private Board endBoard;					public Board getEndBoard() { return endBoard; }
	
	private int id;						public int getId() { return id; }
	private int delta;					public int getDelta() { return delta; }
	
	public TestingSet(String line)
	{
		String[] values = line.split(",");
		int[][] endBoard = new int[GameOfLife.HEIGHT][GameOfLife.WIDTH];
		for(int a = GameOfLife.HEIGHT; a --> 0; )
		{
			for(int b = GameOfLife.WIDTH; b --> 0; )
			{
				endBoard[a][b] = Integer.parseInt(values[b * GameOfLife.HEIGHT + a + 2].trim());
			}
		}
		this.endBoard = new Board(endBoard);
		this.id = Integer.parseInt(values[0]);
		this.delta = Integer.parseInt(values[1]);
	}
	
	
	public static List<TestingSet> load(String filename)
	{
		List<String> lines = Read.from(filename);
		List<TestingSet> trainingSet = new ArrayList<>();
		for(int a = lines.size(); a --> 1; )
		{
			trainingSet.add(new TestingSet(lines.get(a)));
		}
		return trainingSet;
	}
	
	public static List<TestingSet> load(String filename, int delta)
	{
		List<String> lines = Read.from(filename);
		List<TestingSet> trainingSet = new ArrayList<>();
		for(int a = lines.size(); a --> 1; )
		{
			TestingSet newTrainingSet = new TestingSet(lines.get(a));
			if(newTrainingSet.getDelta() == delta)
			{
				trainingSet.add(newTrainingSet);
			}
		}
		return trainingSet;
	}
	public static List<TestingSet> load(String filename, int delta, int maxCount)
	{
		List<String> lines = Read.from(filename);
		List<TestingSet> trainingSet = new ArrayList<>();
		for(int a = lines.size(); a --> 1; )
		{
			TestingSet newTrainingSet = new TestingSet(lines.get(a));
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
