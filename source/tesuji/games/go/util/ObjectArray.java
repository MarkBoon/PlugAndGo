package tesuji.games.go.util;

/**
 * Interface for defining an array for a Go program that can be used both as a
 * 1-dimensional array and as a 2-dimensional array.<br>
 * <br>
 * 
 * The rule is that the index in the 1-dimensional can be computed from the
 * 2-dimensional array as follows:<br>
 * <code>xy = x+y*getWidth()</code><br>
 * <br>
 * 
 * Notes:<lu>
 * <li>The first point is the 1,1 point and not the 0,0 point.</li>
 * <li>The size of the 1-dimensional array is always at least
 * getWidth()*(getWidth()+1)</li>
 * <li>For the moment these arrays are always considerad as read-only.
 * Therefore the behaviour of getSingleArray()[0] = 1 is undefined.</li>
 * </lu>
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
public interface ObjectArray<Type>
{
	public void set(int xy,Type value);
	
	/**
	 	Get an element from the array using a 1-dimensional coordinate.
	*/
	public Type get(int xy);
	/**
	 	Get an element from the array using a 2-dimensional coordinate.
	*/
	public Type get(int x, int y);
	/**
	 	Get the board-size for this array.
	*/
	public int getBoardSize();
	/**
	 	Get the array as a 2-dimensional array.
	*/
	public Type[][] getDoubleArray();
	/**
	 	Get the array as a 1-dimensional array.
	*/
	public Type[] getSingleArray();
	/**
	 	Get the width that is used to do the coordinate
	 	conversion between 1-dimensional and 2-dimensional.
	*/
	public int getWidth();
}