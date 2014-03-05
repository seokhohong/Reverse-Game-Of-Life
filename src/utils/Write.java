package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Write 
{
	public static void to(String filename, List<String> lines)
	{
		to(new File(filename), lines);
	}
	public static void to(String filename, String line)
	{
		to(new File(filename), line);
	}
	public static void to(File file, String line)
	{
		try
		{
			BufferedWriter buff = new BufferedWriter(new FileWriter(file));
			buff.write(line);
			buff.close();
		} 
		catch(IOException e)
		{
			
		}
	}
	public static void to(File file, List<String> lines)
	{
		try
		{
			BufferedWriter buff = new BufferedWriter(new FileWriter(file));
			for(String line : lines)
			{
				buff.write(line);
				buff.newLine();
			}
			buff.close();
		} 
		catch(IOException e)
		{
			
		}
	}
}
