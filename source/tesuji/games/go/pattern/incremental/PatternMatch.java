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

import tesuji.core.util.List;
import tesuji.core.util.SynchronizedArrayStack;
import tesuji.core.util.FlyWeight;
import tesuji.games.go.pattern.common.Pattern;
import tesuji.games.go.pattern.common.PatternCondition;
import tesuji.games.go.pattern.common.PatternConditionInterface;
import tesuji.games.go.tactics.LadderReader;
import tesuji.games.go.tactics.TacticsConstant;
import tesuji.games.go.util.GoArray;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;

/**
 	Simple data object to store pattern-matching information. This is basically a pointer to
 	a leaf in the pattern-tree combined with a location on the board. The tree-leaf contains
 	all the required information like rotation, conditions and move-suggestions.
 */
public class PatternMatch
	implements FlyWeight
{
	IncrementalPatternTreeLeaf leaf;
	int xy;

	int matchListIndex;
	int moveNr;
	
	private SynchronizedArrayStack<PatternMatch> _owner;
	
	/**
	 * Match constructor.
	 */
	PatternMatch(SynchronizedArrayStack<PatternMatch> owner)
	{
		_owner = owner;
	}

	public int getBlackXY() 
	{
		if (leaf.getBlackXY()==UNDEFINED_COORDINATE || leaf.getBlackXY()==Integer.MIN_VALUE)
			return UNDEFINED_COORDINATE;
		return leaf.getBlackXY()+xy;
	}
	public int getOrientation() 
	{
		return leaf.getOrientation();
	}
	public IncrementalPatternTreeLeaf getLeaf()
	{
		return leaf;
	}
	public Pattern getPattern() 
	{
		return leaf.getPattern();
	}
	public int getWhiteXY() 
	{
		if (leaf.getWhiteXY()==UNDEFINED_COORDINATE || leaf.getWhiteXY()==Integer.MIN_VALUE)
			return UNDEFINED_COORDINATE;
		return leaf.getWhiteXY()+xy;
	}
	public int getXY() 
	{
		return xy;
	}
	public boolean isInverted() 
	{
		return leaf.isInverted();
	}
	public void setXY(int newXY) 
	{
		xy = newXY;
	}

	public int getMoveXY()
	{
		if (isInverted())
			return getWhiteXY();
		else
			return getBlackXY();
	}
	
	public int getMoveXY(byte color)
	{
		if (isInverted())
		{
			if (color==BLACK)
				return getWhiteXY();
			else
				return getBlackXY();
		}
		else
		{
			if (color==BLACK)
				return getBlackXY();
			else
				return getWhiteXY();
		}
	}
	
	public double getUrgencyValue(byte color)
	{
		if (isInverted())
		{
			if (color==BLACK)
				return leaf.getPattern().getUrgencyValueWhite();
			else
				return leaf.getPattern().getUrgencyValueBlack();
		}
		else
		{
			if (color==BLACK)
				return leaf.getPattern().getUrgencyValueBlack();
			else
				return leaf.getPattern().getUrgencyValueWhite();
		}
	}
	
	public int getNrOccurences(byte color)
	{
		if (isInverted())
		{
			if (color==BLACK)
				return leaf.getPattern().getWhiteNrOccurrences();
			else
				return leaf.getPattern().getBlackNrOccurrences();
		}
		else
		{
			if (color==BLACK)
				return leaf.getPattern().getBlackNrOccurrences();
			else
				return leaf.getPattern().getWhiteNrOccurrences();
		}
	}
	
	public int getNrPlayed(byte color)
	{
		if (isInverted())
		{
			if (color==BLACK)
				return leaf.getPattern().getWhiteNrSuccesses();
			else
				return leaf.getPattern().getBlackNrSuccesses();
		}
		else
		{
			if (color==BLACK)
				return leaf.getPattern().getBlackNrSuccesses();
			else
				return leaf.getPattern().getWhiteNrSuccesses();
		}
	}
	
	public double getSuccessRatio()
	{
		if (isInverted())
			return (double)leaf.getPattern().getWhiteNrSuccesses() / (double)leaf.getPattern().getWhiteNrOccurrences();
		else
			return (double)leaf.getPattern().getBlackNrSuccesses() / (double)leaf.getPattern().getBlackNrOccurrences();
	}
	
	public void increasePatternRatio(byte color)
	{
		if (isInverted())
		{
			if (color==BLACK)
				leaf.getPattern().increaseWhiteRatio();
			else
				leaf.getPattern().increaseBlackRatio();
		}
		else
		{
			if (color==BLACK)
				leaf.getPattern().increaseBlackRatio();
			else
				leaf.getPattern().increaseWhiteRatio();
		}
	}

	
	public void increasePatternOccurrence(byte color)
	{
		if (isInverted())
		{
			if (color==BLACK)
				leaf.getPattern().increaseWhiteOccurrence();
			else
				leaf.getPattern().increaseBlackOccurrence();
		}
		else
		{
			if (color==BLACK)
				leaf.getPattern().increaseBlackOccurrence();
			else
				leaf.getPattern().increaseWhiteOccurrence();
		}
	}
	
	public void updatePatternUrgency(byte color, int nrMoves)
	{
		assert(nrMoves>=0);
		if (isInverted())
		{
			if (color==BLACK)
				leaf.getPattern().updateWhiteUrgency(nrMoves);
			else
				leaf.getPattern().updateBlackUrgency(nrMoves);
		}
		else
		{
			if (color==BLACK)
				leaf.getPattern().updateBlackUrgency(nrMoves);
			else
				leaf.getPattern().updateWhiteUrgency(nrMoves);
		}
	}

	/**
	 * @see tesuji.games.general.FlyWeight#recycle()
	 */
	public void recycle()
	{
		_owner.push(this);
	}
	
	public boolean equals(Object o)
	{
		if (!(o instanceof PatternMatch))
			return false;
		
		PatternMatch compare = (PatternMatch)o;
		return (xy==compare.xy && leaf==compare.leaf);
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder(getPattern().toString());
		builder.append("location="+GoArray.getX(getXY())+","+GoArray.getY(getXY())+" orientation="+getOrientation()+" inverted="+isInverted());
		builder.append('\n');
		return builder.toString();
	}
	
	/**
     * @return the conditions
     */
    public List<PatternConditionInterface> getConditions()
    {
    	return leaf.getConditions();
    }

	/**
     * @return the moveNr
     */
    public int getMoveNr()
    {
    	return moveNr;
    }

	/**
     * @param moveNr the moveNr to set
     */
    public void setMoveNr(int moveNr)
    {
    	this.moveNr = moveNr;
    }
    
    public PatternMatch createClone()
    {
    	PatternMatch clone = PatternMatchFactory.createMatch();
    	
    	clone.leaf = leaf;
    	clone.xy = xy;
    	clone.moveNr = moveNr;
    	clone.matchListIndex = matchListIndex;
 
    	return clone;
    }
    
    public boolean isSafeToMove(int moveXY,LadderReader reader)
    {
    	if (leaf.getPattern().isGenerated() || (leaf.getConditions()!=null
    		&& leaf.getConditions().get(0).getDataProviderName().equals(PatternCondition.SAFETY)))
    	{
    		return (reader.wouldBeLadder(moveXY,BLACK)==TacticsConstant.CANNOT_CATCH
    			&& reader.wouldBeLadder(moveXY,WHITE)==TacticsConstant.CANNOT_CATCH);
    	}
    	return true;
    }
}
