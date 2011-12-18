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

package tesuji.games.go.monte_carlo;

import java.util.Arrays;

import tesuji.core.util.SynchronizedArrayStack;

import tesuji.games.general.Move;

import tesuji.games.go.common.GoMove;
import tesuji.games.go.util.PointSet;
import tesuji.games.go.util.PointSetFactory;

import static tesuji.games.go.util.GoArray.*;

/**
 * 
 */
public class MCState
{
	public static final int MAX_URGENCY = 100;
	private static final int MAX_PRIORITY_LIST_SIZE = 20;
	
	private PointSet _emptyPoints;
	private int[] _priorityList;
	private int _priorityListSize;
	private int _priorityIndex;
	private boolean _initialised;
	
	private int[] _urgency;

	private SynchronizedArrayStack<MCState> _owner;
	
	MCState(SynchronizedArrayStack<MCState> owner)
	{
		_owner = owner;
		_emptyPoints = PointSetFactory.createPointSet();
		_priorityList = new int[MAX_PRIORITY_LIST_SIZE];
		_urgency = createIntegers();
		Arrays.fill(_urgency, MAX_URGENCY);
	}
	
	public boolean isEmpty()
	{
		return _emptyPoints.getSize()==0;
	}
	
	public boolean hasPriorityMove()
	{
		return _priorityIndex<_priorityListSize;
	}
	
	public PointSet getEmptyPoints()
	{
		return _emptyPoints;
	}
	
	public int getNrEmptyPoints()
	{
		return _emptyPoints.getSize();
	}
	
	public void addPriorityMove(int xy)
	{
		// TODO at some point we may want to check if the move is in the empty list?
		if (_priorityListSize<MAX_PRIORITY_LIST_SIZE)
		{
			_priorityList[_priorityListSize++] = xy;
		}
	}
	
	public int getNextPriorityMove()
	{
		return _priorityList[_priorityIndex++];
	}
	
	public void recycle()
	{
		_emptyPoints.reset();
		_priorityListSize = 0;
		_priorityIndex = 0;
		_initialised = false;
		Arrays.fill(_urgency, MAX_URGENCY);		
		
		_owner.push(this);
	}

	/**
     * @return the initialised
     */
    public boolean isInitialised()
    {
    	return _initialised;
    }

	/**
     * @param initialised the initialised to set
     */
    public void setInitialised(boolean initialised)
    {
    	_initialised = initialised;
    }
    
    public int[] getUrgencyArray()
    {
    	return _urgency;
    }
    
    public int getUrgency(Move move)
    {
    	return _urgency[((GoMove)move).getXY()]; // Ugly cast, need to fix.
    }
}
