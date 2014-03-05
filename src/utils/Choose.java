package utils;

import java.util.List;
import java.util.Random;

public class Choose 
{
	public static <T> T from(List<T> list)
	{
		return list.get(new Random().nextInt(list.size()));
	}
}
