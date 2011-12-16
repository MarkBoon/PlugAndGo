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

/**
 * This is an important class that is used to mark points that have already been
 * processed. Each time the method getNewMarker is called, the information is
 * reset and all points are not marked.
 */
public class BoardMarker
{
	private int labelNr;
	private int[] markers;

	/** Default constructor */
	public BoardMarker()
	{
		markers = GoArray.createIntegers();
		labelNr = 1;
	}
	
	/**
	 	This causes all points previously marked as 'set'
	 	to be unmarked.
	*/
	public final void getNewMarker()
	{
		labelNr++;
		if (labelNr==0)
		{
			labelNr = 1;
			GoArray.clear(markers);
		}
	}

	/**
	 	Mark a point as set.
	
	 	@param xy is the coordinate of the point to mark as set.
	*/
	public final void set( int xy )
	{
		markers[xy] = labelNr;
	}

	/**
	 	Test whether a point was already marked or not.
	
	 	@param xy coordinate of the point to test
	 	@return whether the point was already marked or not.
	*/
	public final boolean notSet( int xy )
	{
		return markers[xy]!=labelNr;
	}

	/**
	 	Test whether a point was already marked or not.
	
	 	@param xy coordinate of the point to test
	 	@return whether the point was already marked or not.
	*/
	public final boolean isSet( int xy )
	{
		return markers[xy]==labelNr;
	}
}