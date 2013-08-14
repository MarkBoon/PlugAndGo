/**
 * Project: Tesuji Go Framework.<br>
 * <br>
 * <font color="#CC6600"><font size=-1> Copyright (c) 1985-2006 Mark Boon<br>
 * All rights reserved.<br>
 * <br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * provided that the above copyright notice(s) and this permission notice appear
 * in all copies of the Software and that both the above copyright notice(s) and
 * this permission notice appear in supporting documentation.<br>
 * <br>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.<br>
 * <br>
 * <font color="#00000"><font size=+1>
 * 
 */
package tesuji.games.go.util;

import static tesuji.games.general.ColorConstant.*;

import java.util.Arrays;

import tesuji.games.model.BoardModel;

/**
 *	This class defines arrays used for a Go-board.
 *	All coordinates are coded such that x,y translates to x+y*WIDTH.
 *	There are methods to create and clear arrays of the appropriate size
 *	and a lot of utility methods to manipulate coordinates.
 */
public class GoArray
{
	/** Width of a 'row'. */
	public static final int MAX_BOARD_SIZE = 19;
	/** Width of a 'row'. */
	public static final int WIDTH = MAX_BOARD_SIZE+1;
	/** Width of a 'row'. */
	public static final int DEFAULT_SIZE = WIDTH - 1;
	/** Index of the first real point.*/
	public static final int FIRST = WIDTH+1;
	/** Last point in the array. */
	public static final int MAX = WIDTH*(WIDTH+1)+1;
	/** Index of the last real point.*/
	public static final int LAST = WIDTH*WIDTH-1;
	/** Easy to spot number used for illegal or unitialized data. */
	public static final int ILLEGAL_VALUE = Integer.MIN_VALUE;
	
	private static byte[][] rowArrays = new byte[WIDTH][0];

//	private static IntStack toDoList = new IntStack();
//	private static BoardMarker marker = new BoardMarker();
	
	/**
	 * Clear an array of byte
	 * 
	 * @param array
	 */
	public static final void clear(byte[] array)
	{
		Arrays.fill(array, (byte)0);
	}
    
	/**
	 * Clear an array of short
	 * 
	 * @param array
	 */
	public static final void clear(short[] array)
	{
		Arrays.fill(array, (short)0);
	}
    
	/**
	 * Clear an array of int
	 * 
	 * @param array
	 */
	public static final void clear(int[] array)
	{
		Arrays.fill(array, 0);
	}
    
	/**
	 * Clear an array of long
	 * 
	 * @param array
	 */
	public static final void clear(long[] array)
	{
		Arrays.fill(array, 0);
	}
    
	/**
	 * Clear an array of double
	 * 
	 * @param array
	 */
	public static final void clear(double[] array)
	{
		Arrays.fill(array,0.0);
	}
    
	/**
	 * Clear an array of float
	 * 
	 * @param array
	 */
	public static final void clear(float[] array)
	{
		Arrays.fill(array,0.0f);
	}
    
	/**
	 * Clear an array of Object
	 * 
	 * @param array
	 */
	public static final void clear(Object[] array)
	{
		Arrays.fill(array,null);
	}
    
	/**
	 * Copy an array of byte
	 * 
	 * @param source array
	 * @param destination array
	 */
	public static final void copy(byte[] source, byte[] destination)
	{
		System.arraycopy(source,0,destination,0,MAX);
	}
    
	/**
	 * Copy an array of short
	 * 
	 * @param source array
	 * @param destination array
	 */
	public static final void copy(short[] source, short[] destination)
	{
		System.arraycopy(source,0,destination,0,MAX);
	}
    
	/**
	 * Copy an array of int
	 * 
	 * @param source array
	 * @param destination array
	 */
	public static final void copy(int[] source, int[] destination)
	{
		System.arraycopy(source,0,destination,0,MAX);
	}
    
	/**
	 * Copy an array of long
	 * 
	 * @param source array
	 * @param destination array
	 */
	public static final void copy(long[] source, long[] destination)
	{
		System.arraycopy(source,0,destination,0,MAX);
	} 
	
	/**
	 * Copy an array of long
	 * 
	 * @param source array
	 * @param destination array
	 */
	public static final void copy(double[] source, double[] destination)
	{
		System.arraycopy(source,0,destination,0,MAX);
	} 
	
	/**
	 * Copy an array of Object
	 * 
	 * @param source array
	 * @param destination array
	 */
	public static final void copy(Object[] source, Object[] destination)
	{
		System.arraycopy(source,0,destination,0,MAX);
	} 
	
	/**
	 * Compare two arrays to see if they contain the same information.
	 * 
	 * @param array1
	 * @param array2
	 */
	public static final boolean isEqual(byte[] array1, byte[] array2)
	{
		for (int i=MAX; --i>=0;)
			if (array1[i]!=array2[i])
				return false;
		return true;
	} 
    
	/**
	 * Compare two arrays to see if they contain the same information.
	 * 
	 * @param array1
	 * @param array2
	 */
	public static final boolean isEqual(short[] array1, short[] array2)
	{
		for (int i=MAX; --i>=0;)
			if (array1[i]!=array2[i])
				return false;
		return true;
	} 
    
	/**
	 * Compare two arrays to see if they contain the same information.
	 * 
	 * @param array1
	 * @param array2
	 */
	public static final boolean isEqual(int[] array1, int[] array2)
	{
		for (int i=MAX; --i>=0;)
			if (array1[i]!=array2[i])
				return false;
		return true;
	} 
    
	/**
	 * Compare two arrays to see if they contain the same information.
	 * 
	 * @param array1
	 * @param array2
	 */
	public static final boolean isEqual(long[] array1, long[] array2)
	{
		for (int i=MAX; --i>=0;)
			if (array1[i]!=array2[i])
				return false;
		return true;
	} 
	
	/**
	 * Compare two arrays to see if they contain the same information.
	 * 
	 * @param array1
	 * @param array2
	 */
	public static final boolean isEqual(Object[] array1, Object[] array2)
	{
		for (int i=0; i<MAX; i++)
			if (!array1[i].equals(array2[i]))
				return false;
		return true;
	} 
	
	/**
	 * @return allocated space for a fixed-size array of byte
	 */
	public static final byte[] createBytes()
	{
		return new byte[MAX];
	}

	/**
	 * @return allocated space for a fixed-size array of short
	 */
	public static final short[]	createShorts()
	{
		return new short[MAX];
	}

	/**
	 * @return allocated space for a fixed-size array of int
	 */
	public static final int[] createIntegers()
	{
		return new int[MAX];
	}

	/**
	 * @return allocated space for a fixed-size array of long
	 */
	public static final long[] createLongs()
	{
		return new long[MAX];
	}
	
	/**
	 * @return allocated space for a fixed-size array of double
	 */
	public static final double[] createDoubles()
	{
		return new double[MAX];
	}

	/**
	 * @return allocated space for a fixed-size array of double
	 */
	public static final float[] createFloats()
	{
		return new float[MAX];
	}

	/**
	 * @return allocated space for a fixed-size array of Object
	 */
	public static final Object[] createObjects()
	{
		return new Object[MAX];
	}
	
	/**
	 * This method does a 4-way flood-fill.
	 * 
	 * All points in the board array that are connected with the
	 * same value (most likely stone-color) get the same number set
	 * in the numbers array.
	 * 
	 * @param value to fill the numbers array with 
	 * @param startXY coordinate of the point where to start
	 * @param board
	 * @param numbers
	 */
	public static final void link(int value, int startXY, byte[] board, int[] numbers)
	{
		byte color = board[startXY];
		IntStack toDoList = ArrayFactory.createIntStack();
		toDoList.push(startXY);
		do
		{
			int xy = toDoList.pop();
			numbers[xy] = value;
			int left = left(xy);
			if (numbers[left]!=value && board[left]==color)
				toDoList.push(left);
			int right = right(xy);
			if (numbers[right]!=value && board[right]==color)
				toDoList.push(right);
			int above = above(xy);
			if (numbers[above]!=value && board[above]==color)
				toDoList.push(above);
			int below = below(xy);
			if (numbers[below]!=value && board[below]==color)
				toDoList.push(below);
		}
		while (!toDoList.isEmpty());
		
		toDoList.recycle();
	}
	
	/**
	 * This method does a 4-way flood-fill.
	 * 
	 * All points in the board array that are connected with the
	 * same value (most likely stone-color) get the same number set
	 * in the numbers array.
	 * 
	 * @param value to fill the numbers array with 
	 * @param startXY coordinate of the point where to start
	 * @param board
	 * @param numbers
	 */
	/* TODO - these are temporarily commented out to make the project compile
	public static final void link(byte value, int startXY, byte[] board, byte[] numbers)
	{
		byte color = board[startXY];
		IntStack toDoList = FlyWeightFactory.getIntStack();
		toDoList.push(startXY);
		do
		{
			int xy = toDoList.pop();
			numbers[xy] = value;
			int left = left(xy);
			if (numbers[left]!=value && board[left]==color)
				toDoList.push(left);
			int right = right(xy);
			if (numbers[right]!=value && board[right]==color)
				toDoList.push(right);
			int above = above(xy);
			if (numbers[above]!=value && board[above]==color)
				toDoList.push(above);
			int below = below(xy);
			if (numbers[below]!=value && board[below]==color)
				toDoList.push(below);
		}
		while (!toDoList.isEmpty());

		toDoList.recycle();
	}
	*/
	
	/**
	 * This method does a 4-way flood-fill.
	 * 
	 * All points in the board array that are connected with the
	 * same value (most likely stone-color) get the same objects set
	 * in the objects array.
	 * 
	 * @param value to fill the numbers array with 
	 * @param startXY coordinate of the point where to start
	 * @param board
	 * @param objects
	 */
	
	/* TODO - these are temporarily commented out to make the project compile
	public static final void link(Object value, int startXY, byte[] board, Object[] objects)
	{
		byte color = board[startXY];
		IntStack toDoList = FlyWeightFactory.getIntStack();
		toDoList.push(startXY);
		do
		{
			int xy = toDoList.pop();
			objects[xy] = value;
			int left = left(xy);
			if (objects[left]!=value && board[left]==color)
				toDoList.push(left);
			int right = right(xy);
			if (objects[right]!=value && board[right]==color)
				toDoList.push(right);
			int above = above(xy);
			if (objects[above]!=value && board[above]==color)
				toDoList.push(above);
			int below = below(xy);
			if (objects[below]!=value && board[below]==color)
				toDoList.push(below);
		}
		while (!toDoList.isEmpty());

		toDoList.recycle();
	}
	*/
	
	/**
	 * This method creates an array for a Go board of a given size with an edge
	 * around the board-points. The size of the array is always the same, but
	 * the points not used are marked with the value EDGE.
	 * 
	 * @param size is the board-size used. Maximum value is 19
	 * @return byte[]
	 */
	public static final byte[] createBoardArray(int size)
	{
		byte[] board = createBytes();

		for (int i=0; i<MAX; i++)
		{
			int x = getX(i);
			int y = getY(i);

			if (x==0 || y==0 || x>size || y>size)
				board[i] = EDGE;
			else
				board[i] = EMPTY;
		}
		return board;
	}

	/**
	 * This method creates an array for a Go board of a given size with at each
	 * point the distance to the edge of the board. Any point that is not on the
	 * board gets a value 0.<br>
	 * <br>
	 * Very sneakily, the board-size is stored in the last element of the array,
	 * so be careful...<br>
	 * <br>
	 * This is basically an array with static data that is very handy. To
	 * minimize memory use, the array is reused for each board-size so make sure
	 * the array never gets used to store other data.<br>
	 * 
	 * @param size is the board-size used. Maximum value is 19
	 * @return byte[]
	 */
	public static final byte[] createRowArray(int size)
	{
		if (rowArrays[size].length!=0)
			return rowArrays[size];

		byte[] rows = createBytes();

		for (int i=0; i<MAX; i++)
		{
			int x = getX(i);
			int y = getY(i);

			int horizontal = Math.min(x,size+1-x);
			int vertical = Math.min(y,size+1-y);
			int row = Math.min(horizontal,vertical);
		
			if (row<=0)
				rows[i] = 0;
			else
				rows[i] = (byte) row;
		}
		
		rows[rows.length-1] = (byte) size;

		rowArrays[size] = rows;
		return rows;
	}

	/**
	 * Compute the x component of the one-dimensional xy-coordinate.
	 * 
	 * @param xy
	 * 
	 * @return x
	 */
	public static final int getX(int xy)
	{
		return xy%WIDTH;
	}

	/**
	 * Compute the y component of the one-dimensional xy-coordinate.
	 * 
	 * @param xy
	 * 
	 * @return y
	 */
	public static final int getY(int xy)
	{
		return xy/WIDTH;
	}
	
	public static final int getDistance(int xy1, int xy2)
	{
		int difference = Math.abs(xy1-xy2);
		return getX(difference) + getY(difference);
	}

	/**
	 * @param xy1
	 * @param xy2
	 * @return boolean value whether xy1 and xy2 are next to each other..
	 */
	public static final boolean isNeighbour(int xy1, int xy2)
	{
		int difference = xy1-xy2;
		if (difference==1 || difference==-1 || difference==WIDTH || difference==-WIDTH)
			return true;
		return false;
	}

	/**
	 * Return the coordinate one point to the left of the coordinate passed as a
	 * parameter.
	 * 
	 * @param xy coordinate
	 * 
	 * @return left of xy
	 */
	public static final int left(int xy)
	{
		return xy-1;
	}

	/**
	 * Return the coordinate one point to the rigth of the coordinate passed as
	 * a parameter.
	 * 
	 * @param xy coordinate
	 * 
	 * @return right of xy
	 */
	public static final int right(int xy)
	{
		return xy+1;
	}

	/**
	 * Return the coordinate one point above the coordinate passed as a
	 * parameter.
	 * 
	 * @param xy coordinate
	 * 
	 * @return above xy
	 */
	public static final int above(int xy)
	{
		return xy-WIDTH;
	}
	
	/**
	 * Return the coordinate one point below the coordinate passed as a
	 * parameter.
	 * 
	 * @param xy coordinate
	 * 
	 * @return below xy
	 */
	public static final int below(int xy)
	{
		return xy+WIDTH;
	}

	public static final int left_above(int xy)
	{
		return xy-WIDTH-1;
	}

	public static final int left_below(int xy)
	{
		return xy+WIDTH-1;
	}

	public static final int right_above(int xy)
	{
		return xy-WIDTH+1;
	}

	public static final int right_below(int xy)
	{
		return xy+WIDTH+1;
	}

	/**
	 * Convert a 2-dimensional coordinate to a 1 dimensional coordinate.
	 * 
	 * @param x coordinate
	 * @param y coordinate
	 * 
	 * @return one-dimensional xy coordinate
	 */
	public static final int toXY(int x, int y)
	{
		return x+y*WIDTH;
	}
	
	public static void printBoard(byte[] array)
	{
		System.out.println();
		boolean lastWasNewline = false;
		for (int i=FIRST; i<=LAST; i++)
		{
			char c;
			switch(array[i])
			{
				case BLACK: c = '#'; break;
				case WHITE: c = 'O'; break;
				case EMPTY: c = '.'; break;
				case EDGE: c = '\n'; break;
				default: c = '?';
			}
			if (c!='\n' || !lastWasNewline)
				System.out.print(c);
			if (c=='\n')
				lastWasNewline = true;
			else
				lastWasNewline = false;
		}
		System.out.println();	
	}
	
	public static String toString(byte[] array)
	{
		StringBuilder out = new StringBuilder();
		out.append('\n');
		boolean lastWasNewline = false;
		for (int i=FIRST; i<=LAST; i++)
		{
			char c;
			switch(array[i])
			{
				case BLACK: c = '#'; break;
				case WHITE: c = 'O'; break;
				case EMPTY: c = '.'; break;
				case EDGE: c = '\n'; break;
				default: c = '?';
			}
			if (c!='\n' || !lastWasNewline)
				out.append(c);
			if (c=='\n')
				lastWasNewline = true;
			else
				lastWasNewline = false;
		}
		out.append('\n');
		return out.toString();
	}
	
	public static void printBoard(BoardModel model)
	{
		System.out.println();
		for (int row=1; row<=model.getBoardSize(); row++)
		{
			for (int column=1; column<=model.getBoardSize(); column++)
			{
				char c;
				switch(model.get(column,row))
				{
					case BLACK: c = '#'; break;
					case WHITE: c = 'O'; break;
					case EMPTY: c = '.'; break;
					case EDGE: c = '\n'; break;
					default: c = '?';
				}
				System.out.print(c);
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static String printBoardToString(byte[] array)
	{
		StringBuilder stringBuilder = new StringBuilder();
		boolean lastWasNewline = false;
		for (int i=FIRST; i<=LAST; i++)
		{
			char c;
			switch(array[i])
			{
				case BLACK: c = '#'; break;
				case WHITE: c = 'O'; break;
				case EMPTY: c = '.'; break;
				case EDGE: c = '\n'; break;
				default: c = '?';
			}
			if (c!='\n' || !lastWasNewline)
				stringBuilder.append(c);
			if (c=='\n')
				lastWasNewline = true;
			else
				lastWasNewline = false;
		}
		return stringBuilder.toString();
	}
	
	public static String printBoardToString(byte[] array, int boardSize)
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (int row=1; row<=boardSize; row++)
		{
			for (int col=1; col<=boardSize; col++)
			{
				int xy = toXY(col,row);
				char c;
				switch(array[xy])
				{
					case BLACK: c = 'X'; break;
					case WHITE: c = 'O'; break;
					case EMPTY: c = '.'; break;
					case EDGE: c = '#'; break;
					default: c = '?';
				}
				stringBuilder.append(c);
			}
			stringBuilder.append('\n');
		}
		stringBuilder.append('\n');
		return stringBuilder.toString();
	}
	
	public static void printNumbers(byte[] array)
	{
		System.out.println();
		for (int i=FIRST; i<=LAST; i++)
		{
			char c;
			int x = getX(i);
			if (x==0)
				c = '\n';
			else
				c = (char)('0'+array[i]);
			System.out.print(c);
		}
		System.out.println();
		
	}
	
	public static void printNumbers(int[] array)
	{
		System.out.println();
		for (int i=FIRST; i<=LAST; i++)
		{
			int x = getX(i);
			if (x==0)
				System.out.println("\n");
			else
			{
				String number = Integer.toHexString(array[i]);
				if (number.length()==1)
					number = "0"+number;
				else
					number = number.substring(number.length()-2);
				System.out.print(number+" ");
			}
		}
		System.out.println();
		
	}
}