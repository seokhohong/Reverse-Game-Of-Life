package apps;

import game.TrainingSet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import utils.ArrayUtils;

public class MeasureInformation 
{
	public static void main(String[] args)
	{
		new MeasureInformation().go();
	}
	private void go()
	{
		try
		{
			int delta = 1;
			List<TrainingSet> trainingSets = TrainingSet.load("2000train.csv", delta);
			FileOutputStream outputStartingBoards = new FileOutputStream("info\\startingBoards"+delta+".txt");
			FileOutputStream outputEndingBoards = new FileOutputStream("info\\endingBoards"+delta+".txt");
			for(TrainingSet tSet : trainingSets)
			{
				writeBooleans(outputStartingBoards, tSet.getStartBoard().getBooleanData());
				writeBooleans(outputEndingBoards, tSet.getEndBoard().getBooleanData());
			}
			outputStartingBoards.close();
			outputEndingBoards.close();
		}
		catch(IOException e)
		{}
	}
	//Credit to http://stackoverflow.com/questions/9349519/how-to-convert-boolean-array-to-binary-and-vice-versa-in-java
	private static void writeBooleans(OutputStream out, boolean[] ar) throws IOException 
	{
	    for (int i = 0; i < ar.length; i += 8) {
	        int b = 0;
	        for (int j = Math.min(i + 7, ar.length-1); j >= i; j--) {
	            b = (b << 1) | (ar[j] ? 1 : 0);
	        }
	        out.write(b);
	    }
	}
}
