package utils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/** Much credit to http://stackoverflow.com/questions/12375774/fastest-way-to-read-write-an-array-from-to-a-file*/
public class FastIO 
{
	public static void main(String[] args) throws IOException
	{
		int[] arr = new int[10000000];
		for(int a = 0; a < arr.length; a ++)
		{
			//arr[a] = a;
		}
		double time = System.currentTimeMillis();
		System.out.println(System.currentTimeMillis() - time);
	}
	public static void writeIntArray(String filename, int[] arr)
	{
		try
		{
			if(!new File(filename).exists())
			{
				Write.to(filename, "");
			}
			final Path path = Paths.get(filename);
			final ByteBuffer buf = ByteBuffer.allocateDirect(arr.length << 2);
	
			buf.asIntBuffer().put(arr).flip();
			try (final WritableByteChannel out = Files.newByteChannel(path,
			    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			  do {
			    out.write(buf);
			  } while (buf.hasRemaining());
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void writeShortArray(String filename, short[] arr)
	{
		try
		{
			if(!new File(filename).exists())
			{
				Write.to(filename, "");
			}
			final Path path = Paths.get(filename);
			final ByteBuffer buf = ByteBuffer.allocateDirect(arr.length << 1);
	
			buf.asShortBuffer().put(arr).flip();
			try (final WritableByteChannel out = Files.newByteChannel(path,
			    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			  do {
			    out.write(buf);
			  } while (buf.hasRemaining());
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void writeLongArray(String filename, long[] arr)
	{
		try
		{
			if(!new File(filename).exists())
			{
				Write.to(filename, "");
			}
			final Path path = Paths.get(filename);
			final ByteBuffer buf = ByteBuffer.allocateDirect(arr.length << 3);
	
			buf.asLongBuffer().put(arr).flip();
			try (final WritableByteChannel out = Files.newByteChannel(path,
			    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			  do {
			    out.write(buf);
			  } while (buf.hasRemaining());
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void readIntArray(String filename, int[] arr)
	{
		try
		{
			final Path path = Paths.get(filename);
			final ByteBuffer buf = ByteBuffer.allocateDirect(arr.length << 2);
			try (final ReadableByteChannel in = Files.newByteChannel(path,
				    StandardOpenOption.READ)) {
				  do {
				    in.read(buf);
				  } while (buf.hasRemaining());
				}
			buf.clear();
			buf.asIntBuffer().get(arr);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static int[] readIntArray(String filename)
	{
		System.gc();
		int[] arr = new int[(int) (new File(filename).length() / 4)];
		if(((long) arr.length << 2) > Integer.MAX_VALUE) //computer doesn't have enough memory
		{
			return readBufferedIntArray(filename, arr);
		}
		return mapIntArray(filename, arr);
	}
	public static int[] mapIntArray(String filename, int[] arr)
	{
		try
		{
			final Path path = Paths.get(filename);
			final ByteBuffer buf = ByteBuffer.allocateDirect(arr.length << 2);
			try (final ReadableByteChannel in = Files.newByteChannel(path,
				    StandardOpenOption.READ)) {
				  do {
				    in.read(buf);
				  } while (buf.hasRemaining());
				}
			buf.clear();
			buf.asIntBuffer().get(arr);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return arr;
	}
	public static long[] readLongArray(String filename)
	{
		System.gc();
		long[] arr = new long[(int) (new File(filename).length() / 8)];
		if(((long) arr.length << 3) > Integer.MAX_VALUE / 2)
		{
			return readBufferedLongArray(filename, arr);
		}
		return mapLongArray(filename, arr);
	}
	public static int[] readLongArrayInInts(String filename)
	{
		int[] arr = new int[(int) (new File(filename).length() / 8)];
		try
		{
		    FileInputStream fin = new FileInputStream(filename);
		    DataInputStream din = new DataInputStream(new BufferedInputStream(fin));
		    for(int a = 0; a < arr.length; a ++)
		    {
		    	if(a % 1000000 == 0)
		    	{
		    		System.out.println("Read "+ a + " longs of "+arr.length);
		    	}
		    	arr[a] = (int) din.readLong();
		    }
		    din.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return arr;
	}
	private static long[] readBufferedLongArray(String filename, long[] arr)
	{
		try
		{
		    FileInputStream fin = new FileInputStream(filename);
		    DataInputStream din = new DataInputStream(new BufferedInputStream(fin));
		    for(int a = 0; a < arr.length; a ++)
		    {
		    	if(a % 1000000 == 0)
		    	{
		    		System.out.println("Read "+ a + " longs of "+arr.length);
		    	}
		    	arr[a] = din.readLong();
		    }
		    din.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return arr;
	}
	private static int[] readBufferedIntArray(String filename, int[] arr)
	{
		try
		{
		    FileInputStream fin = new FileInputStream(filename);
		    DataInputStream din = new DataInputStream(new BufferedInputStream(fin));
		    for(int a = 0; a < arr.length; a ++)
		    {
		    	if(a % 1000000 == 0)
		    	{
		    		System.out.println("Read "+ a + " ints of "+arr.length);
		    	}
		    	arr[a] = din.readInt();
		    }
		    din.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return arr;
	}
	private static long[] mapLongArray(String filename, long[] arr)
	{
		try
		{
			final Path path = Paths.get(filename);
			final ByteBuffer buf = ByteBuffer.allocateDirect(arr.length << 3);
			try (final ReadableByteChannel in = Files.newByteChannel(path,
				    StandardOpenOption.READ)) {
				  do {
				    in.read(buf);
				  } while (buf.hasRemaining());
				}
			buf.clear();
			buf.asLongBuffer().get(arr);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return arr;
	}
}
