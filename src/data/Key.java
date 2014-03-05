package data;

import java.util.Arrays;

import utils.BitHacking;

/** Exists only because long[]'s don't make good HashMap keys */
public class Key
{
	public static void main(String[] args)
	{
		long[] bitString = new long[2];
		bitString[0] = 34017655808L;
		bitString[1] = 71123639206360L;
		Key key = new Key(bitString);
		for(int a = 0; a < 100; a ++)
		{
			System.out.print(key.getBit(a, 50));
		}
		System.out.println(BitHacking.print(bitString[0]) + BitHacking.print(bitString[1]));
	}
	private long[] key;
	public Key(long[] key)
	{
		this.key = key;
	}
	public int getBit(int numBit, int bitsPerKey)
	{
		return BitHacking.nthBit(key[numBit / bitsPerKey], numBit % bitsPerKey);
	}
	public String print(int numBits, int bitsPerKey)
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < 100; a ++)
		{
			builder.append(getBit(a, bitsPerKey));
		}
		return builder.toString();
	}
	public String printBinary()
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < key.length; a++)
		{
			builder.append(BitHacking.print(key[a]));
		}
		return builder.toString();
	}
	public int numBitsSet()
	{
		int numBitsSet = 0;
		for(int a = 0; a < key.length; a ++)
		{
			numBitsSet += BitHacking.numBitsSet(key[a]);
		}
		return numBitsSet;
	}
	@Override
	public int hashCode()
	{
		return Arrays.hashCode(key);
	}
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Key)
		{
			Key other = (Key) o;
			return Arrays.equals(key, other.key);
		}
		return false;
	}
	public long[] getKey() 
	{
		return key;
	}
	public void setKey(long[] key) 
	{
		this.key = key;
	}
}