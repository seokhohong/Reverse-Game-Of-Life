package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

//Static class
public class Read 
{
	private Read() { throw new AssertionError("Do Not Instantiate Me"); }
	/** Reads from a file and returns the contents in an ArrayList<String>. Returns an empty ArrayList<String> if there is no file */
	public static ArrayList<String> from(String filename, boolean readBlanks)
	{
		return from(new File(filename), readBlanks);
	}
	public static ArrayList<String> from(File f, boolean readBlanks)
	{
		ArrayList<String> strings = new ArrayList<String>();
		if(!f.exists())
		{
			System.err.println("File "+f.getPath()+" not found!");
			return strings;
		}
		while(true)
		{
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line;
				while((line = reader.readLine())!=null)
				{
					//ignore comments and newlines
					if(!line.startsWith("#") && (!line.isEmpty() || readBlanks))
					{
						strings.add(line);
					}
				}
				reader.close();
				break;
			}
			catch(IOException e)
			{
				System.err.println("Error reading file "+f.getName()+".... attempting Again");
			}
		}
		return strings;
	}
	public static ArrayList<String> from(File f)
	{
		return from(f, false);
	}
	public static ArrayList<String> from(String filename)
	{
		return from(filename, false);
	}
}
