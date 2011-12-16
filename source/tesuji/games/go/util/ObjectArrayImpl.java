package tesuji.games.go.util;

/**
 * 1-dimensional implementation of ObjectArray.
 * 
 * Project: Tesuji Go Framework.<br>
 * <br>
 * 
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
public class ObjectArrayImpl<Type>
	implements ObjectArray<Type>
{
	private int boardSize;
	protected Type[] array;

	/**
	 * @param size
	 */
	@SuppressWarnings("unchecked")
	public ObjectArrayImpl(int size)
	{
	    boardSize = size;
		array = (Type[])GoArray.createObjects();
	}

	/**
	 * @param size
	 * @param array
	 */
	public ObjectArrayImpl(int size, Type[] array)
	{
	    boardSize = size;
		this.array = array;
	}
	
	public void clear()
	{
		GoArray.clear(array);
	}

	/**
	 	Get an element from the array using a 1-dimensional coordinate.
	*/
	public final Type get(int xy)
	{
		return array[xy];
	}
	
	public final void set(int xy, Type value)
	{
		array[xy] = value;
	}

	/**
	 	Get an element from the array using a 2-dimensional coordinate.
	*/
	public Type get(int x, int y)
	{
		return array[GoArray.toXY(x,y)];
	}

	/**
	 	Get the board-size for this array.
	*/
	public int getBoardSize()
	{
		return boardSize;
	}

	/**
	 	Get the array as a 2-dimensional array.
	*/
	@SuppressWarnings("unchecked")
	public Type[][] getDoubleArray()
	{
		Type[][] newArray = (Type[][])new Object[boardSize+2][boardSize+2];
		for (int i=1; i<=boardSize; i++)
		{
			for (int j=1; j<=boardSize; j++)
			{
				newArray[i][j] = get(i,j);
			}
		}
		return newArray;
	}

	/**
	 	Get the array as a 1-dimensional array.
	*/
	public Type[] getSingleArray()
	{
		return array;
	}

	/**
	 	Get the width that is used to do the coordinate
	 	conversion between 1-dimensional and 2-dimensional.
	*/
	public int getWidth()
	{
		return GoArray.WIDTH;
	}
}