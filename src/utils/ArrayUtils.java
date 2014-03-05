package utils;

import java.lang.reflect.Array;

public class ArrayUtils 
{
	public static void main(String[] args)
	{
		Integer[][] doubleDim = { { 1, 2, 3},
								{4, 5, 6} };
		
		Integer[] singleDim = to1D(Integer.class, doubleDim);
		Integer[][] restored = to2D(Integer.class, singleDim, 2);
		for(int a = 0; a < restored.length; a ++)
		{
			for(int b = 0; b < restored[a].length; b++)
			{
				System.out.print(restored[a][b]);
			}
			System.out.println();
		}
	}
	
	public static <T> T[] to1D(Class<T> type, T[][] arr)
	{
		@SuppressWarnings("unchecked")
		T[] singleDim = (T[]) Array.newInstance(type, arr.length * arr[0].length);
		for(int a = arr.length; a --> 0; )
		{
			for(int b = arr[0].length; b --> 0; )
			{
				singleDim[a * arr[0].length + b] = arr[a][b];
			}
		}
		return singleDim;
	}
	@SuppressWarnings("unchecked")
	public static <T> T[][] to2D(Class<T> type, T[] arr, int dim1Len)
	{
		int dim2Len = arr.length / dim1Len;
		T[][] doubleDim = (T[][]) Array.newInstance(Array.newInstance(type, 1).getClass(), dim1Len);
		for(int a = dim1Len; a --> 0; )
		{
			doubleDim[a] = (T[]) Array.newInstance(type, dim2Len);
		}
		for(int a = arr.length; a --> 0; )
		{
			doubleDim[a / dim2Len][a % dim2Len] = (T) arr[a]; 
		}
		return doubleDim;
	}
	
	public static int[] to1D(int[][] arr)
	{
		int[] singleDim = new int[arr.length * arr[0].length];
		for(int a = 0; a < arr.length; a++)
		{
			for(int b = 0; b < arr[0].length; b++)
			{
				singleDim[a * arr[0].length + b] = arr[a][b];
			}
		}
		return singleDim;
	}
	public static int[][] to2D(int[] arr, int dim1Len)
	{
		int dim2Len = arr.length / dim1Len;
		int[][] doubleDim = new int[dim1Len][dim2Len];
		for(int a = arr.length; a --> 0; )
		{
			doubleDim[a / dim2Len][a % dim2Len] = arr[a]; 
		}
		return doubleDim;
	}
	
	public static int sum(int[] arr)
	{
		int sum = 0;
		for(int val : arr)
		{
			sum += val;
		}
		return sum;
	}
	public static double sum(double[] arr)
	{
		double sum = 0;
		for(double val : arr)
		{
			sum += val;
		}
		return sum;
	}
	public static double mean(double[] arr)
	{
		return sum(arr) / arr.length;
	}
	
	public static double[] normalize(double[] arr)
	{
		double[] normalized = new double[arr.length];
		double sqrtSumOfSquares = Math.sqrt(sumOfSquares(arr));
		for(int a = 0; a < arr.length; a++)
		{
			normalized[a] = arr[a] / sqrtSumOfSquares;
		}
		return normalized;
	}
	
	public static double sumOfSquares(double[] arr)
	{
		double sum = 0;
		for(double d : arr)
		{
			sum += d * d;
		}
		return sum;
	}
	
	/** Cuts out an element at a given index. Inefficient... */
	public static long[] cutout(long[] arr, int index)
	{
		long[] newArr = new long[arr.length - 1];
		int newArrPos = 0;
		for(int a = 0; a < arr.length; a ++)
		{
			if(a != index)
			{
				newArr[newArrPos] = arr[a];
				newArrPos++;
			}
		}
		return newArr;
	}
	
	public static int[] cutout(int[] arr, int index)
	{
		int[] newArr = new int[arr.length - 1];
		int newArrPos = 0;
		for(int a = 0; a < arr.length; a ++)
		{
			if(a != index)
			{
				newArr[newArrPos] = arr[a];
				newArrPos++;
			}
		}
		return newArr;
	}
	
	public static int[] longToInt(long[] arr)
	{
		int[] newArr = new int[arr.length];
		for(int a = arr.length; a --> 0; )
		{
			newArr[a] = (int) arr[a];
		}
		return newArr;
	}
	
	public static <T> String print(T[] arr)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for(int a = 0; a < arr.length - 1; a ++)
		{
			builder.append(arr[a] + ", ");
		}
		builder.append(arr[arr.length - 1]);
		builder.append("]");
		return builder.toString();
	}
	public static String print(int[] arr)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for(int a = 0; a < arr.length - 1; a ++)
		{
			builder.append(arr[a] + ", ");
		}
		builder.append(arr[arr.length - 1]);
		builder.append("]");
		return builder.toString();
	}
	public static String print(double[] arr)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for(int a = 0; a < arr.length - 1; a ++)
		{
			builder.append(arr[a] + ", ");
		}
		builder.append(arr[arr.length - 1]);
		builder.append("]");
		return builder.toString();
	}
	public static String print(long[] arr)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for(int a = 0; a < arr.length - 1; a ++)
		{
			builder.append(arr[a] + ", ");
		}
		builder.append(arr[arr.length - 1]);
		builder.append("]");
		return builder.toString();
	}
}
