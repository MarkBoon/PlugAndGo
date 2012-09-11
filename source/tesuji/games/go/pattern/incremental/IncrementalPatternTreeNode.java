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

import java.io.Serializable;

import tesuji.core.util.ArrayList;
import tesuji.core.util.List;

import tesuji.games.go.pattern.common.Pattern;
import tesuji.games.go.util.GoArray;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;

/**
	A PatternTreeNode is a single node in the pattern tree.  A node consists
	of pointers to other nodes further "down" the tree and a list of patterns
	which end at this node.
	
	There's actually nothing incremental about this class.
 */
class IncrementalPatternTreeNode
	implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4750713892478332987L;
	
	private int pointNr = 0;
	private int nextCoordinate = UNDEFINED_COORDINATE;
	private IncrementalPatternTreeNode blackChild;
	private IncrementalPatternTreeNode whiteChild;
	private IncrementalPatternTreeNode emptyChild;
	private IncrementalPatternTreeNode edgeChild;
	private IncrementalPatternTreeNode noCareChild;
	
	// Don't serialize the leafs.
	private transient ArrayList<IncrementalPatternTreeLeaf> leafList;
	
	/**
	 * PatternTreeNode default constructor.
	 */
	public IncrementalPatternTreeNode()
	{
	}
	
	/**
	 * @param patternList
	 * @param spiral
	 * @param depth
	 * @param rotation
	 */
	public void addPatterns(List<Pattern> patternList, PointSpiral spiral, int depth, int rotation)
	{
		byte[] done = GoArray.createBytes();
		GoArray.clear(done);
		addPatterns(patternList,spiral,depth,rotation,done);
	}
	
	/**
		Create a node from the list of patterns that is passed as a parameter.  As a part of creating this
		node we will likely create the descendant nodes of this one.
		
	 * @param patternList
	 * @param spiral
	 * @param depth in the tree
	 * @param orientation of the pattern
	 * @param done marks points already processed
	*/
	private void addPatterns(List<Pattern> patternList, PointSpiral spiral, int depth, int orientation, byte[] done)
	{
		// Create six new lists, one for each possible value for this position
		// (Black, White, Empty, Edge and NoCare) and one for the patterns which end
		// at this node.  Each list will be a subset of the original list.   The patterns
		// that end at this node will be stored here.  The other four lists will be
		// used to make recursive calls on this routine.

		ArrayList<Pattern> blackList = new ArrayList<Pattern>();
		ArrayList<Pattern> whiteList = new ArrayList<Pattern>();
		ArrayList<Pattern> emptyList = new ArrayList<Pattern>();
		ArrayList<Pattern> edgeList = new ArrayList<Pattern>();
		ArrayList<Pattern> noCareList = new ArrayList<Pattern>();
		ArrayList<Pattern> terminalList = new ArrayList<Pattern>();

		determineNextCoordinate(patternList, spiral, done, orientation, depth);

		int size = patternList.size();
		for (int i=0; i<size; i++)
		{
			Pattern pattern = patternList.get(i);

			if (isRestEmpty(pattern, spiral, done, orientation))
			{
				if (terminalList.size()!=0)
				{
					System.out.println(terminalList.get(0));
					System.out.println();
					System.out.println(pattern);
					System.out.println();
				}
				terminalList.add(pattern);
				continue;
			}
			
			// Find the value of the pattern bit at this pattern point and then
			// add this pattern to the appropriate list.
			// If you want to allow things like black-or-empty, add code here and in Pattern.getPoint().
			byte value = spiral.getAdjustedPoint(pointNr,orientation,pattern);
			if (value==BLACK)
				blackList.add(pattern);
			else if (value==WHITE)
				whiteList.add(pattern);
			else if (value==EMPTY)
				emptyList.add(pattern);
			else if (value==EDGE)
				edgeList.add(pattern);
			else // if (value==NOCARE)
				noCareList.add(pattern);
		}

		done[pointNr] = 1;

		// Add some patterns to the leafList.
		for (int i=0; i<terminalList.size(); i++)
		{
			Pattern pattern = terminalList.get(i);
			IncrementalPatternTreeLeaf leaf = new IncrementalPatternTreeLeaf(pattern,spiral,done,orientation);
			if (leafList==null)
				leafList = new ArrayList<IncrementalPatternTreeLeaf>();
			if (!isInList(leaf,leafList))
			{
				leafList.add(leaf);
				pattern.setAdded(true);
			}
		}
		
		if (noCareList.size()!=0)
		{
			if (noCareChild==null)
				noCareChild = new IncrementalPatternTreeNode();
			noCareChild.addPatterns(noCareList,spiral,depth+1,orientation,done);
		}

		if (blackList.size()!=0)
		{
			if (blackChild==null)
				blackChild = new IncrementalPatternTreeNode();
			blackChild.addPatterns(blackList,spiral,depth+1,orientation,done);
		}
		if (whiteList.size()!=0)
		{
			if (whiteChild==null)
				whiteChild = new IncrementalPatternTreeNode();
			whiteChild.addPatterns(whiteList,spiral,depth+1,orientation,done);
		}
		if (emptyList.size()!=0)
		{
			if (emptyChild==null)
				emptyChild = new IncrementalPatternTreeNode();
			emptyChild.addPatterns(emptyList,spiral,depth+1,orientation,done);
		}
		if (edgeList.size()!=0)
		{
			if (edgeChild==null)
				edgeChild = new IncrementalPatternTreeNode();
			edgeChild.addPatterns(edgeList,spiral,depth+1,orientation,done);
		}
			
		done[pointNr] = 0;
	}
	
	public final IncrementalPatternTreeNode getBlackChild() 
	{
		return blackChild;
	}
	public final IncrementalPatternTreeNode getEmptyChild()
	{
		return emptyChild;
	}
	public final IncrementalPatternTreeNode getEdgeChild()
	{
		return edgeChild;
	}
	public final IncrementalPatternTreeNode getNoCareChild()
	{
		return noCareChild;
	}
	public final IncrementalPatternTreeNode getWhiteChild()
	{
		return whiteChild;
	}
	public final List<IncrementalPatternTreeLeaf> getLeafList()
	{
		return leafList;
	}
	public final int getPointNr()
	{
		return pointNr;
	}
	
	private final boolean isInList(IncrementalPatternTreeLeaf newLeaf, List<IncrementalPatternTreeLeaf> leafList)
	{
		for (int i=0; i<leafList.size(); i++)
		{
			IncrementalPatternTreeLeaf leaf = leafList.get(i);
			if (leaf.getPattern().getConditions().equals(newLeaf.getPattern().getConditions()))
				return true;
//			if ((leaf.getOrientation())==(newLeaf.getOrientation()) && leaf.getPattern().isSamePattern(newLeaf.getPattern()))
//				return true;
		}
		return false;
	}
   
	/**
	 * Get the next node down the tree. The value on the actual board determines
	 * which branch down is taken.
	 * 
	 * @param startXY point on the board where the matching started.
	 * @param board
	 * 
	 * @return PatternTreeNode
	 */
//	public final FastPatternTreeNode getNextNode(FastMatchContext context)
//	{
//		int startXY = context.startXY;
//		int xy=startXY+nextCoordinate;
//		
//		if (xy<0 || xy>=GoArray.MAX)
//			return null;
//		else
//		{
//			switch (context.boardModel.get(xy))
//			{
//				case EMPTY:
//					return emptyChild;
//				case BLACK:
//					return blackChild;
//				case WHITE:
//					return whiteChild;
//				case EDGE:
//					return edgeChild;
//				default:
//					return null;
//			}
//		}
//	}
//	
//	/**
//	 * Get the next node down the tree. The value on the actual board determines
//	 * which branch down is taken.
//	 * 
//	 * @param startXY point on the board where the matching started.
//	 * @param board
//	 * 
//	 * @return PatternTreeNode
//	 */
//	public final FastPatternTreeNode getNextNodeAndStoreState(FastMatchContext context)
//	{
//		FastPatternTreeNode nextNode = getNextNode(context);
//		if (nextNode!=null)
//		{
//			int nextCoordinate = context.startXY + nextNode.nextCoordinate;
//			if (nextCoordinate>=0 && nextCoordinate<MAX)
//			{
//				MatchingState state = context.matchingState[nextCoordinate];
//				if (state!=null)
//					state.add(nextNode);
//			}
//		}
//		return nextNode;
//	}
//	
//	/**
//	 * Get the next node down the tree. The value on the actual board determines
//	 * which branch down is taken.
//	 * 
//	 * @param startXY point on the board where the matching started.
//	 * @param board
//	 * 
//	 * @return PatternTreeNode
//	 */
//	public final FastPatternTreeNode getNextNodeAndRemoveState(FastMatchContext context)
//	{
//		FastPatternTreeNode nextNode = getNextNode(context);
//		if (nextNode!=null)
//		{
//			int nextCoordinate = context.startXY + nextNode.nextCoordinate;
//			if (nextCoordinate>=0 && nextCoordinate<MAX)
//			{
//				MatchingState state = context.matchingState[nextCoordinate];
//				if (state!=null)
//					state.remove(nextNode);
//			}
//		}
//		return nextNode;
//	}
	
	/**
		Remove any terminal data blocks which no longer point to valid patterns. This routine is
		generally called after some major operation on the pattern group which deletes patterns.
	*/
	void removeDeadPatterns()
	{
		for (int i=leafList.size(); --i>=0;)
		{
			IncrementalPatternTreeLeaf leaf = leafList.get(i);
			if (leaf.getPattern().isRemoved())
				leafList.remove(i);
		}

		if (blackChild!=null)
			blackChild.removeDeadPatterns();
		if (whiteChild!=null)
			whiteChild.removeDeadPatterns();
		if (emptyChild!=null)
			emptyChild.removeDeadPatterns();
		if (edgeChild!=null)
			edgeChild.removeDeadPatterns();
		if (noCareChild!=null)
			noCareChild.removeDeadPatterns();
	}

	/**
     * @return the nextCoordinate
     */
    public int getNextCoordinate()
    {
    	return nextCoordinate;
    }

    private boolean isRestEmpty(Pattern pattern, PointSpiral spiral, byte[] done, int orientation)
    {
    	int last = spiral.getLastSpiralIndex();
		for (int j=0; j<last; j++)
		{
			if (done[j]==0 && spiral.getAdjustedPoint(j,orientation,pattern)!=NOCARE)
			{
				return false;
			}
		}
		return true;
    }

    private void determineNextCoordinate(List<Pattern> patternList, PointSpiral spiral, byte[] done, int orientation, int depth)
    {
		if (nextCoordinate==UNDEFINED_COORDINATE)
		{
			int bestPoint = 0;
			int minNoCare = Integer.MAX_VALUE;
			int minEmpty = Integer.MAX_VALUE;
			int minEdge = Integer.MAX_VALUE;
			int minSpread = Integer.MAX_VALUE;

			int size = patternList.size();
			
			if (depth!=0)
			{
				int last = spiral.getLastSpiralIndex();
				for (int i=0; i<last; i++)
				{
					if (done[i]!=0)
						continue;
	
					if (depth<5)
					{
						bestPoint = i;
						break;
					}
					
					int countBlack = 0;
					int countWhite = 0;
					int countEmpty = 0;
					int countEdge = 0;
					int countNoCare = 0;
	
					for (int j=0; j<size && j<1000; j++) // Limit the sampling size to 1000, for now...
					{
						Pattern pattern = patternList.get(j);
						byte value = spiral.getAdjustedPoint(i,orientation, pattern);
						
						// If you want to allow things like black-or-empty, add code here and in Pattern.getPoint().
						if (value==BLACK)
							countBlack++;
						else if (value==WHITE)
							countWhite++;
						else if (value==EMPTY)
							countEmpty++;
						else if (value==EDGE)
							countEdge++;
						else if (value==NOCARE)
							countNoCare++;
					}
					
					int max = countEmpty > countWhite ? (countEmpty > countBlack ? countEmpty : countBlack) : (countWhite > countBlack ? countWhite : countBlack);
					int min = countEmpty < countWhite ? (countEmpty < countBlack ? countEmpty : countBlack) : (countWhite < countBlack ? countWhite : countBlack);
					int spread = max - min;
					if (countNoCare < minNoCare || (countNoCare == minNoCare && countEdge < minEdge)
												|| (countNoCare == minNoCare && countEdge==minEdge && countEmpty < minEmpty)
												|| (countNoCare == minNoCare && countEdge==minEdge && countEmpty == minEmpty && spread < minSpread))
	//					if (countNoCare < minNoCare || (countNoCare == minNoCare && spread < minSpread))
					{
						bestPoint = i;
						minNoCare = countNoCare;
						minEmpty = countEmpty;
						minEdge = countEdge;
						minSpread = spread;
					}
					// Note: removing the condition below makes the tree a little more efficient but slower to build.
					else if (bestPoint>0 && i-bestPoint>25)
						break;
				}
				// Don't know exactly why, but probably because it can't start at the first point. (MB)
				if (bestPoint == 0)
					bestPoint = 1;
	
			}
			pointNr = bestPoint;
			nextCoordinate = spiral.getPointOrder()[pointNr];
		}    	
    }
}
