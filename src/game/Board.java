package game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import utils.ArrayUtils;

import data.Key;

/** Relatively messy class with functionality from when board generation was done in Java instead of C */

public class Board 
{
	
	static final int ALIVE = 1;
	private int rows;
	private int cols;
	
	boolean isAlive = false;					public boolean isAlive() { return isAlive; }
	
	int[] data;									public int getNumCols() { return cols; }
												public int getNumRows() { return rows; }
												public int getData(int row, int col) { return data[getIndex(row, col)]; }				

	Board(int row, int col)
	{
		this.rows = row;
		this.cols = col;
		data = new int[row * col];
	}
	Board()
	{
		rows = 20;
		cols = 20;
		data = new int[20 * 20];
	}
	public Board(int[] data, int cols)
	{
		rows = data.length / cols;
		this.cols = cols;
		this.data = data;
	}
	public Board(int[][] data)
	{
		rows = data.length;
		cols = data[0].length;
		this.data = ArrayUtils.to1D(data);
	}
	
	public static Board copy(Board board)
	{
		return new Board(board.getData());
	}
	/** Returns copy of internal data*/
	public int[][] getData()
	{
		int[][] data = new int[getNumRows()][getNumCols()];
		for(int a = getNumRows(); a --> 0; )
		{
			for(int b = getNumCols(); b --> 0; )
			{
				data[a][b] = this.data[getIndex(a, b)];
			}
		}
		return data;
	}
	public boolean[] getBooleanData()
	{
		boolean[] data = new boolean[getNumRows() * getNumCols()];
		for(int a = getNumRows(); a --> 0; )
		{
			for(int b = getNumCols(); b --> 0; )
			{
				data[a * getNumCols() + b] = this.data[getIndex(a, b)] == 1;
			}
		}
		return data;
	}
	
	public int getIndex(int row, int col)
	{
		return row * cols + col;
	}
	public long getBits(int row, int col, int rowDim, int colDim)
	{
		long bits = 0L;
		for(int a = 0; a < rowDim; a ++ )
		{
			for(int b = 0; b < colDim; b++ )
			{
				if(a + row >= 0 && b + col >= 0 && a + row < getNumRows() && b + col < getNumCols())
				{
					bits += (long) this.data[getIndex(a + row, b + col)] << (a * colDim + b);
				}
			}
		}
		return bits;
	}
	/** Misnomer, refer to the C version of this */
	public long getBitsOptimized(int row, int col, int rowDim, int colDim)
	{
		long bits = 0;
		int rowLowerLimit = row;
		int colLowerLimit = col;
		int rowLimit = rowDim + row;
		int colLimit = colDim + col;
		if(colLimit > cols)
		{
			colLimit = cols;
		}
		if(rowLimit > rows)
		{
			rowLimit = rows;
		}
		if(row < 0)
		{
			rowLowerLimit = 0;
		}
		if(col < 0)
		{
			colLowerLimit = 0;
		}
		for(int a = rowLowerLimit; a < rowLimit; a ++ )
		{
			for(int b = colLowerLimit; b < colLimit; b++ )
			{
				bits += (long) (data[a * cols + b]) << ((a - row) * colDim + (b - col));
			}
		}
		return bits;
	}
	/** Low index is top-left bits, higher index is bottom-right */
	public Key getBitsOptimizedLarge(int row, int col, int rowDim, int colDim, int keyDivisions)
	{
		long[] largeBits = new long[keyDivisions];
		for(int a = 0; a < keyDivisions; a++)
		{
			largeBits[a] = getBitsOptimized(row + (rowDim / keyDivisions) * a, col, rowDim / keyDivisions, colDim);
		}
		return new Key(largeBits);
	}
	
	public int[][] getData(int x, int y, int width, int height)
	{
		int[][] data = new int[width][height];
		//int[][] normalData = new int[width][height];
		for(int a = getNumRows(); a --> 0; )
		{
			for(int b = getNumCols(); b --> 0; )
			{
				if(a + x >= 0 && b + y >= 0 && a + x < getNumRows() && b + y < getNumCols())
				{
					//data[a][b] = getNeighbors(a + x, b + y);
					data[a][b] = this.data[getIndex(a + x, b + y)];
				}
			}
		}
		return data;
	}
	
	public Board step()
	{
		int[] next = new int[getNumRows() * getNumCols()];
		boolean nextIsAlive = false;
		for(int a = getNumRows(); a --> 0; )
		{
			for(int b = getNumCols(); b --> 0; )
			{
				switch(getNeighbors(a, b))
				{
				case 2:
				{
					if(data[getIndex(a, b)] == ALIVE) 
					{
						next[getIndex(a, b)] = ALIVE;
						nextIsAlive = true;
					}
					break;
				}
				case 3: 
				{
					next[getIndex(a, b)] = ALIVE;
					nextIsAlive = true;
					break;
				}
				}
			}
		}
		Board newBoard = new Board(next, getNumCols());
		newBoard.isAlive = nextIsAlive;
		return newBoard;
	}
	//Too lazy to do it right
	public void setIndex(int row, int col, int val)
	{
		data[getIndex(row, col)] = val;
	}
	
	int getNeighbors(int row, int col)
	{
		int neighbors = 0;
		if(row > 0)
		{
			neighbors += data[(row - 1) * cols + col];
			if(col > 0)
			{
				neighbors += data[(row - 1) * cols + col - 1];
			}
			if(col < getNumCols() - 1)
			{
				neighbors += data[(row - 1) * cols + col + 1];
			}
		}
		if(col > 0)
		{
			neighbors += data[row * cols + (col - 1)];
		}
		if(col < getNumCols() - 1)
		{
			neighbors += data[row * cols + (col + 1)];
		}
		if(row < getNumRows() - 1)
		{
			neighbors += data[(row + 1) * cols + col];
			if(col > 0)
			{
				neighbors += data[(row + 1) * cols + col - 1];
			}
			if(col < getNumCols() - 1)
			{
				neighbors += data[(row + 1) * cols + col + 1];
			}
		}
		return neighbors;
	}
	
	
	
	@Override
	public String toString()
	{
		return print(ArrayUtils.to2D(data, cols));
	}
	
	/** Uses mean absolute error between two boards to return an error */
	public static double diffError(Board one, Board two)
	{
		int error = 0;
		for(int a = one.getNumRows(); a --> 0; )
		{
			for(int b = one.getNumCols(); b --> 0; )
			{
				if(one.data[one.getIndex(a, b)] != two.data[two.getIndex(a, b)])
				{
					error ++;
				}
			}
		}
		return (double) error / (one.getNumRows() * one.getNumCols());
	}
	
	public static List<Point> diff(Board one, Board two)
	{
		List<Point> errorPoints = new ArrayList<>();
		for(int a = one.getNumRows(); a --> 0; )
		{
			for(int b = one.getNumCols(); b --> 0; )
			{
				if(one.data[one.getIndex(a, b)] != two.data[two.getIndex(a, b)])
				{
					errorPoints.add(new Point(a, b));
				}
			}
		}
		return errorPoints;
	}
	
	public int getNumOnes()
	{
		return ArrayUtils.sum(data);
	}

	/** Prints Board.java-style arrays */
	public static String print(int[][] arr)
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < arr.length; a ++)
		{
			for(int b = 0; b < arr[a].length; b ++ )
			{
				builder.append(arr[a][b] != 0 ? "X" : "-");
			}
			builder.append("\n");
		}
		return builder.toString();
	}
}
