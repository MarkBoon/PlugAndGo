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
 *	<font color="#00000"><font size=+1>
 */
package tesuji.games.go.pattern.incremental;

import tesuji.core.util.ArrayList;
import tesuji.core.util.List;
import tesuji.games.go.pattern.common.Pattern;
import tesuji.games.go.pattern.common.PatternCondition;
import tesuji.games.go.pattern.common.PatternConditionInterface;
import tesuji.games.go.util.GoArray;
import tesuji.games.util.Point;

import static tesuji.games.go.common.GoConstant.*;
import static tesuji.games.go.pattern.util.PatternUtil.*;

/**
 * When the pattern-matcher reaches a leaf node that basically means a pattern matched.
 * So what we store here is the original pattern and in which orientation it was found.
 * It also stores a bit more information from the patterns, like the suggested moves
 * and any further pattern-conditions since they need to be translated according to the
 * orientation the pattern was found.
 * 
 * There's actually nothing incremental about this class.
 */
class IncrementalPatternTreeLeaf
{
	private Pattern pattern;		// Back-reference to the actual Pattern object.
	private int blackXY;
	private int whiteXY;
	private int orientation;					// Orientation in which the pattern is stored in the tree.
	private List<PatternConditionInterface> conditions;	// A list with the conditions put in the proper orientation
	
	/**
		Construct the object that describes a pattern as a leaf node in the search tree
		by getting the data from a pattern object.
	
	 	@param pattern for which to construct this PatternTreeLeaf
	 	@param spiral is the spiral that is used to store this pattern.
	 	@param done array with the points marked that are already in the tree. (As index in the spiral)
	 	@param orientation is the additional rotation to apply to the pattern
	*/
	public IncrementalPatternTreeLeaf(Pattern pattern, PointSpiral spiral, byte[] done, int orientation)
	{
		Point tmpPoint = Point.create();
	
		this.pattern = pattern;
		this.orientation = orientation;
		
		// The starting point, or reference point, also changes based on the orientation.
		
		int startX = pattern.getStartX();
		int startY = pattern.getStartY();
//		adjustOrientation(startX,startY,orientation,tmpPoint);
////		adjustOrientation(startX,startY,pattern.getWidth()-startX-1,pattern.getHeight()-startY-1,orientation&7,tmpPoint);
//		startX = tmpPoint.x;
//		startY = tmpPoint.y;
		
		// The data in the pattern tree may be in a different orientation than the
		// data in the pattern array.  In making this transition from pattern array
		// to pattern tree, the data are transformed to reflect a new orientation.
		// In general, this involves transforming coordinates.
	
		if (pattern.getBlackX() != UNDEFINED_COORDINATE)	// There is a recommended move for black
		{
			int x = pattern.getBlackX() - startX;	// Make relative to starting point
			int y = pattern.getBlackY() - startY;
			adjustInversedOrientation(x,y,orientation,tmpPoint);
			blackXY = GoArray.toXY(tmpPoint.x,tmpPoint.y);
		}
	 	else														// No recommended move for black
			blackXY = 0;
	
		if (pattern.getWhiteX() != UNDEFINED_COORDINATE)	// There is a recommended move for white
		{
			int x = pattern.getWhiteX() - startX;	// Make relative to starting point
			int y = pattern.getWhiteY() - startY;
			adjustInversedOrientation(x,y,orientation,tmpPoint);
			whiteXY = GoArray.toXY(tmpPoint.x,tmpPoint.y);
		}
	 	else														// No recommended move for white
			whiteXY = 0;
		
		List<PatternCondition> patConditions = pattern.getConditionList();
		if (patConditions!=null)
		{
			int size = patConditions.size();
			for (int i=0; i<size; i++)
			{
				PatternConditionInterface c = (PatternConditionInterface) (patConditions.get(i)).clone();
				int x = c.getX()-startX;
				int y = c.getY()-startY;
				adjustInversedOrientation(x,y,orientation,tmpPoint);
				c.setX(tmpPoint.x);
				c.setY(tmpPoint.y);
				if (conditions==null)
					conditions = new ArrayList<PatternConditionInterface>();
				conditions.add(c);
			}
		}
		tmpPoint.recycle();	
	}
	
	public int getBlackXY()
	{
		return blackXY;
	}
	public int getOrientation()
	{
		return orientation;
	}
	public Pattern getPattern()
	{
		return pattern;
	}
	public int getWhiteXY()
	{
		return whiteXY;
	}
	
	public boolean isInverted()
	{
		return (orientation>7);
	}

	/**
     * @return the conditions
     */
    public List<PatternConditionInterface> getConditions()
    {
    	return conditions;
    }
}
