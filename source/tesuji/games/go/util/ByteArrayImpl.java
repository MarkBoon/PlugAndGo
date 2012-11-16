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

import static tesuji.games.go.util.GoArray.*;

/**
 * 1-dimensional implementation of ByteArray.
 */
public class ByteArrayImpl
	implements ByteArray
{
	private int _boardSize;
	protected byte[] _array;

    protected ByteArrayImpl()
    {
    }

    /**
     * @param size
     */
    protected ByteArrayImpl(int size)
    {
    	setBoardSize(size);
    }

	/**
	 * @param size
	 * @param array
	 */
	ByteArrayImpl(int size, byte[] array)
	{
	    _boardSize = size;
		this._array = array;
	}
	
	public void clear()
	{
		GoArray.clear(_array);
	}
	
	public void setBoardSize(int boardSize)
	{
        _boardSize = boardSize;
        _array = GoArray.createBytes();
	}
	
	/**
	 	Get an element from the array using a 1-dimensional coordinate.
	*/
	public byte get(int xy)
	{
		return _array[xy];
	}

	public void set(int xy, byte value)
	{
		_array[xy] = value;
	}
	
	/**
	 	Get an element from the array using a 2-dimensional coordinate.
	*/
	public byte get(int x, int y)
	{
		return _array[toXY(x,y)];
	}

	public void set(int x, int y, byte value)
	{
		_array[toXY(x, y)] = value;
	}
	
	/**
	 * @see tesuji.games.model.BoardModel#get(int, int)
	 */
	public byte getBoardPoint(int x, int y)
	{
		return get(x,y);
	}
	
	public void setBoardPoint(int x, int y, byte value)
	{
		_array[toXY(x,y)] = value;
	}
	
	/**
	 	Get the board-size for this array.
	*/
	public int getBoardSize()
	{
		return _boardSize;
	}

	/**
	 	Get the array as a 2-dimensional array.
	*/
	public byte[][] getDoubleArray()
	{
		byte[][] newArray = new byte[_boardSize+2][_boardSize+2];
		for (int i=1; i<=_boardSize; i++)
		{
			for (int j=1; j<=_boardSize; j++)
			{
				newArray[i][j] = get(i,j);
			}
		}
		return newArray;
	}

	/**
	 	Get the array as a 1-dimensional array.
	*/
	public byte[] getSingleArray()
	{
		return _array;
	}

	/**
	 	Get the width that is used to do the coordinate
	 	conversion between 1-dimensional and 2-dimensional.
	*/
	public int getWidth()
	{
		return WIDTH;
	}
}