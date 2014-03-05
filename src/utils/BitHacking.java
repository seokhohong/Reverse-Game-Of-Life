package utils;

public class BitHacking 
{
	public static String print(long val)
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < 64; a++)
		{
			if((val & (1L << a)) != 0)
			{
				builder.append("1");
			}
			else
			{
				builder.append("0");
			}
		}
		return builder.toString();
	}
	public static String print(long val, boolean tagLsb)
	{
		StringBuilder builder = new StringBuilder();
		if(tagLsb)
		{
			builder.append("LSB ");
		}
		builder.append(print(val));
		return builder.toString();
	}
	public static int nthBit(long bitString, int numBit)
	{
		return (int) ((bitString >> numBit) & 1);
	}
	public static int numBitsSet(long bitString)
	{
		int num = 0;
		for(int a = 0; a < 64; a++)
		{
			num += nthBit(bitString, a);
		}
		return num;
	}
}
