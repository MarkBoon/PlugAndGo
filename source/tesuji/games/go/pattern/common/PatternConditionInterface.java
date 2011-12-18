/**
 *	Product: Tesuji Software Go Library.<br><br>
 *
 *	<font color="#CC6600"><font size=-1>
 *	Copyright (c) 2001-2004 Tesuji Software B.V.<br>
 *	All rights reserved.<br><br>
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a
 *	copy of this software and associated documentation files (the
 *	"Software"), to deal in the Software without restriction, including
 *	without limitation the rights to use, copy, modify, merge, publish,
 *	distribute, and/or sell copies of the Software, and to permit persons
 *	to whom the Software is furnished to do so, provided that the above
 *	copyright notice(s) and this permission notice appear in all copies of
 *	the Software and that both the above copyright notice(s) and this
 *	permission notice appear in supporting documentation.<br><br>
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 *	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.<br><br>
 *
 *	Except as contained in this notice, the name of a Tesuji Software
 *	shall not be used in advertising or otherwise to promote the sale, use
 *	or other dealings in this Software without prior written authorization
 *	of Tesuji Software.<br><br>
 *	<font color="#00000"><font size=+1>
 */
package tesuji.games.go.pattern.common;

import tesuji.games.general.provider.DataProviderList;

/**
 * Interface for matching extra conditions attached to a pattern.
 */
public interface PatternConditionInterface
{
	/**
	 * @param startX coordinate on the board of the top-left of the pattern.
	 * @param startY coordinate on the board of the top-left of the pattern.
	 * @param orientation in which the pattern matched
	 * @param inverted whether the colors matched in reverse.
	 * @param providerList abstract pointer to the Go data structures against which to test the condition.
	 * 
	 * @return whether the condition matched with the given data from the DataProvider
	 */
	boolean match(int startX, int startY, int orientation, boolean inverted, DataProviderList providerList);
	
	/**
	 * @return x-coordinate of the location of the condition relative to the top-left.
	 */
	int getX();
	/**
	 * @return y-coordinate of the location of the condition relative to the top-left.
	 */
	int getY();
	
	/** 
	 * @param x coordinate of the location of the condition relative to the top-left.
	 */
	void setX(int x);
	/** 
	 * @param y coordinate of the location of the condition relative to the top-left.
	 */
	void setY(int y);
	
	/**
	 * @return the name of the data-provider to use for retrieving the data.
	 */
	public String getDataProviderName();
	
	public Object clone();
}
