package apps;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import utils.Read;
import utils.Write;

public class CombineSubmission 
{
	private Map<Integer, String> predictions = new HashMap<>();
	public static void main(String[] args)
	{
		new CombineSubmission("submission.csv").go();
	}
	private String exportFilename;
	private CombineSubmission(String exportFilename)
	{
		this.exportFilename = exportFilename;
	}
	private void go()
	{
		addHeader();
		for(int a = 1; a < 6 ; a ++)
		{
			add("submission"+a+".csv");
		}
		export();
	}
	private void addHeader()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("id");
		for(int a = 1; a < 401; a ++)
		{
			builder.append(",start."+a);
		}
		predictions.put(0, builder.toString()); //0th index is the header
	}
	private void add(String filename)
	{
		List<String> lines = Read.from(filename);
		for(String line : lines)
		{
			int num = Integer.parseInt(line.substring(0, line.indexOf(',')));
			predictions.put(num, line);
		}
	}
	private void export()
	{
		List<String> orderedOutput = new ArrayList<>();
		for(int a = 0; a < predictions.size(); a ++)
		{
			orderedOutput.add(predictions.get(a));
		}
		Write.to(exportFilename, orderedOutput);
	}
}
