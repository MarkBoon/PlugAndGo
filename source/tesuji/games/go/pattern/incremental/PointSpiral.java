/**
 *
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

import tesuji.core.util.List;
import tesuji.games.go.pattern.common.Pattern;
import tesuji.games.go.pattern.common.PatternGroup;
import tesuji.games.go.util.GoArray;
import tesuji.games.util.Point;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;
import static tesuji.games.go.pattern.util.PatternUtil.*;

/**
 	PatternSpiral class based on the C++ implementation by Charlie Carroll.

	This class contains a list of coordinates which are roughly ordered in
	a spiral shape around a center-point. When given a set of patterns
	it modifies the order of the points in the spiral to optimize the
	number of steps needed to determine a match or refute one. When the set
	of patterns is very large this order will probably hardly differ from the
	original spiral, but for smaller sets the order can greatly differ to make
	use of the particular characteristics of the set.
*/
public class PointSpiral
{
	// Maximum number of iterations it tries to optimize the spiral.
//	private static final int MAX_ITERATION = 2;
	
	private static final int SPIRAL_SIZE = 4;		// Maximum distance from the pattern-center.
	
	/** Total number of points on a standard size board. */
	public static final int LONGEST_SPIRAL	= (2*SPIRAL_SIZE+1)*(2*SPIRAL_SIZE+1);

	private int[] spiralMapX;	// X and Y coordinates of the order in which the points are stored in
	private int[] spiralMapY;	// the spiral tree.
	private int[] pointOrder;	// Same as spiralMap but then containing all 8 possible rotations plus
								// that the x-y coordinates are converted into a single number.

	private int lastSpiralIndex = -1;
	
	/**
	 	Construct a point spiral.
	*/
	public PointSpiral()
	{
		spiralMapX = new int[LONGEST_SPIRAL];
		spiralMapY = new int[LONGEST_SPIRAL];

		// The inside 9 (3x3) points are handled in a particular order.
		spiralMapX[0] =  0;		spiralMapY[0] =  0;
		spiralMapX[1] =  0;		spiralMapY[1] = -1;
		spiralMapX[2] =  1;		spiralMapY[2] =  0;
		spiralMapX[3] =  0;		spiralMapY[3] =  1;
		spiralMapX[4] = -1;		spiralMapY[4] =  0;
		spiralMapX[5] = -1;		spiralMapY[5] = -1;
		spiralMapX[6] =  1;		spiralMapY[6] = -1;
		spiralMapX[7] =  1;		spiralMapY[7] =  1;
		spiralMapX[8] = -1;		spiralMapY[8] =  1;

		int counter = 9;

		// The remaining points are a true spiral.  (Well, as true as a spiral can
		// be on a rectangular grid.)  We process each distance from the center
		// in turn.  For each trip around the spiral we start in the upper-left
		// corner and go clockwise.

		// This can still be optimised. It's left as is until I'm sure it all works properly. (MB)

		for (int d=2; d<=SPIRAL_SIZE; d++)
		{
			for (int i=-d; i<=d; i++)	{ spiralMapX[counter] = i;	spiralMapY[counter++] = -d; }
			for (int j=-d+1; j<d; j++)	{ spiralMapX[counter] = d;	spiralMapY[counter++] = j; }
			for (int i=d; i>=-d; i--)	{ spiralMapX[counter] = i;	spiralMapY[counter++] = d; }
			for (int j=d-1; j>-d; j--)	{ spiralMapX[counter] = -d;	spiralMapY[counter++] = j; }
		}

		pointOrder = new int[LONGEST_SPIRAL];
		computePointOrder();
	}

	/**
		Compute the best starting point from which to insert a
		pattern into the tree.  "Best" currently means that starting point
		or orientation for which the first NoCare point comes as far down
		the tree as possible.
		
		Ties are broken by prefering a black or	white stone on the earliest point. 
		Empirically, these have been found to make an efficient tree.
	
		Note: this actually modifies the startXY field n the pattern.
		
	 	@param pattern is the pattern for which to compute its best orientation
	*/
	private void computeBestStart(Pattern pattern)
	{
		int bestOrderValue = -1;

		// We will compute the distance to the first NoCare point for each
		// combination of startX, startY, and orientation.  This is usually
		// a total of width*height*8 times.  For very large patterns, we
		// restrict the center point to be within SPIRAL_SIZE of all four
		// edges.

		// For each possible starting position,
		int width = pattern.getWidth();
		for (int i=0; i<width; i++)
		{
			if (i<(width-SPIRAL_SIZE))
				continue;
			if (i>SPIRAL_SIZE)
				continue;

			int height = pattern.getHeight();
			for (int j=0; j<height; j++)
			{
				if (j<(height-SPIRAL_SIZE))
					continue;
				if (j>SPIRAL_SIZE)
					continue;

				int p;
				int orderValue = 0;
				for (p=0; p<LONGEST_SPIRAL; p++)	// look for first NoCare
				{
					byte pointValue = pattern.getPoint(spiralMapX[p]+i,spiralMapY[p]+j);
					if (pointValue == NOCARE)
						break;
					
					// Promote having stones in the first five points.
					if (p<5 && pointValue!=EMPTY)
						orderValue += 25;
				}
				
				// Definitely want the first 5 points defined.
				if (p>4)
					orderValue += 512;
				
				orderValue += p;
				if (orderValue>bestOrderValue)
				{
					bestOrderValue = orderValue;
					pattern.setStartX(i);
					pattern.setStartY(j);
				}
				
				// Things are more complicated if orderValue exactly equals bestOrderValue.
				// In this case, we want to choose a pattern that has a Black or White stone as
				// early as possible.  This tends to balance the tree and improve
				// performance.  Note that this optimization is dependent on the
				// data currently in the tree.  It should probably be looked at again
				// after the computer starts generating its own patterns.
			
				else if (orderValue == bestOrderValue)
				{
					for (int p2=0; p2<p; p2++)
					{
						byte value1 = pattern.getRelativePoint(spiralMapX[p2],spiralMapY[p2]);
						if (value1!= EMPTY)
							break;

						byte value2 = pattern.getPoint(spiralMapX[p2]+i,spiralMapY[p2]+j);
						if (value2!= EMPTY)
						{
							pattern.setStartX(i);
							pattern.setStartY(j);
							break;
						}
					}
				}
			}
		}
	}
	
	private void computePointOrder()
	{
		for (int i=0; i<LONGEST_SPIRAL; i++)
		{
			int xoffset = spiralMapX[i];
			int yoffset = spiralMapY[i];
			pointOrder[i] = GoArray.toXY(xoffset,yoffset);
		}
	}
	
	/**
		A "spiral" defines the order in which pattern points are examined when
		being put into and retrieved from a pattern tree.  The goodness of the 
		spiral has a large effect on the efficiency of the search process.  This
		routine computes an efficient spiral.

		The shape of the tree that is about to be made will have a large
		effect on the efficiency of the search routine.  There are many 
		things that can affect the shape of the tree.  For each pattern
		we can pick any of width*height starting points and any of
		eight rotations/mirrorings.  For the whole tree, we can pick many
		different sequences for examining the pattern.

		This next section picks the sequence that we will use and stores
		it in two arrays, spiralMapX and spiralMapY.  The intuitive idea is to pick
		a central point and make a spiral out from there.  This puts the most
		significant points of the pattern early in the tree and therefore
		requires fewer comparisons to make a pattern match.  The computer
		refines this intuitive idea by using a heuristic to pick an even
		better ordering.  (Many edge patterns are short and wide which
		makes a true spiral inefficient.)
	
		Here's how it works.  We make initial assumptions about the point
		ordering and pattern start points and orientations.  With these
		assumptions we compute the point ordering that puts the fewest
		NoCares in the early part of the tree.  This new point ordering
		may affect the optimum orientations and start points for each
		pattern, so we recompute them.  This recomputation may in turn
		change the optimum point ordering so we recompute it.  This iteration
		could go on indefinitely, but some practical experience has shown
		that a couple of loops will do the job.
	
	 	@param: 
	 	@return:
	*/
	public void computeSpiral(PatternGroup patternGroup)
	{
		List<Pattern> patternList = patternGroup.getPatternList();
		int size = patternList.size();

		// Prepare all the patterns for tree-building.
		for (int i=0; i<size; i++)
		{
			Pattern pattern = patternList.get(i);
			if (pattern.getUserX()==UNDEFINED_COORDINATE)
			{
				pattern.setStartX(pattern.getWidth()/2);
				pattern.setStartY(pattern.getHeight()/2);
			}
			else
			{
				pattern.setStartX(pattern.getUserX());
				pattern.setStartY(pattern.getUserY());
			}
		}

// TODO - review the whole 'recompute' procedure. Does it gain speed?	
//		for (int iteration=0; iteration<MAX_ITERATION; iteration++)
//		{
//			recompute(patternGroup);
			for (int i=0; i<size; i++)
			{
				Pattern pattern = patternList.get(i);
				if (pattern.getUserX()==UNDEFINED_COORDINATE)
					computeBestStart(pattern);	
			}
//		}
	}
	
	public byte getAdjustedPoint(int pointNr, int orientation, Pattern pattern)
	{
		int x = spiralMapX[pointNr];
		int y = spiralMapY[pointNr];
		Point point = Point.create(x,y);
		adjustOrientation(x,y,orientation,point);
		byte pointValue = pattern.getRelativePoint(point.x,point.y);
		if (orientation>7) // Inverted
		{
			if (pointValue==BLACK)
				pointValue = WHITE;
			else if (pointValue==WHITE)
				pointValue = BLACK;
		}
		point.recycle();
		return pointValue;
	}

	public final int[] getPointOrder()
	{
		return pointOrder;
	}
	/**
		Given a starting point and orientation for each pattern in the
		linear array, recompute the point ordering.  We will choose a
		point ordering based on the number of NoCares at each point.
		The goal is to minimize the number of NoCares early in the tree.

	 	@param: PatternGroup is the group of patterns used to compute an optimal spiral for.
	*/
	@SuppressWarnings("unused")
	private void recompute(PatternGroup patternGroup)
	{
		int[] sortingValue = new int[LONGEST_SPIRAL];
		int[] tmpMapX = new int[LONGEST_SPIRAL];
		int[] tmpMapY = new int[LONGEST_SPIRAL];

		for (int i=0; i<LONGEST_SPIRAL; i++)
			sortingValue[i] = 0;

		// Build the array to be sorted.  For each possible point number (0-360),
		// for each pattern in the array, increment our counter if that pattern
		// is a NoCare point.

		List<Pattern> patternList = patternGroup.getPatternList();
		for (int i=0; i<LONGEST_SPIRAL; i++)
		{
			int size = patternList.size();
			for (int j=0; j<size; j++)
			{
				Pattern pattern = patternList.get(j);
				for (int orientation=0; orientation<16; orientation++)
					if (getAdjustedPoint(i,orientation,pattern)==NOCARE)
						sortingValue[i]++;
			}
		}

		// Encode the point number in the array to be sorted.  The number of
		// NoCares is in the most significant bits so it will be what we really
		// sort on.  The pattern number (what we eventually want) goes along
		// for the ride.

		for (int i=0; i<LONGEST_SPIRAL; i++)
			sortingValue[i] = (sortingValue[i]<<9)+i;
	
		// Do a simple-minded sort of the array.  This will put the point numbers
		// that have the fewest number of NoCares in the front of the ordering.
		// This is what we want as this will lead to fewest number of NoCare
		// links high in the tree which improves the efficiency of the search
		// process.  Note that we only sort starting at point number 1; point
		// number 0 must be the starting point of the pattern.

		for (int i=1; i<LONGEST_SPIRAL-1; i++)
		{
			for (int j=i+1; j<LONGEST_SPIRAL; j++)
			{
				if (sortingValue[i]>sortingValue[j])
				{
					int tmp = sortingValue[i];
					sortingValue[i] = sortingValue[j];
					sortingValue[j] = tmp;
				}
			}
		}

		// We no longer care about how many NoCares were at each point, we
		// just want to know the ordering.  So we remove everything but the point
		// number.

		for (int i=0; i<LONGEST_SPIRAL; i++)
			sortingValue[i] &= 0x1FF;

		// Copy the new ordering into a temporary and then back into spiralMapX
		// and spiralMapX.  Then we're done.

		for (int i=0; i<LONGEST_SPIRAL; i++)
		{
			tmpMapX[i] = spiralMapX[sortingValue[i]];
			tmpMapY[i] = spiralMapY[sortingValue[i]];
		}

		for (int i=0; i<LONGEST_SPIRAL; i++)
		{
			spiralMapX[i]=tmpMapX[i];
			spiralMapY[i]=tmpMapY[i];
		}

		computePointOrder();
	}
	
	public int getLastSpiralIndex()
	{
		if (lastSpiralIndex==-1)
			return LONGEST_SPIRAL;
		return lastSpiralIndex;
	}

	/**
     * @param lastSpiralIndex the lastSpiralIndex to set
     */
    public void setLastSpiralIndex(int lastSpiralIndex)
    {
    	this.lastSpiralIndex = lastSpiralIndex;
    }
}
