package data;

import game.Board;

import java.util.*;

import utils.Write;

public class Exporter 
{
	private String filename;
	private List<String> predictions = new ArrayList<>();
	public Exporter(String filename)
	{
		this.filename = filename;
	}
	public void add(int id, Board board)
	{
		predictions.add(id + exportBoard(board));
	}
	private String exportBoard(Board board)
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < 20; a ++) //columns
		{
			for(int b = 0; b < 20; b++) //rows
			{
				builder.append(","+board.getData(b, a));
			}
		}
		return builder.toString();
	}
	public void export()
	{
		Write.to(filename, predictions);
	}
}
