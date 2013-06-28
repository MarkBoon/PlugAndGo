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

import static tesuji.games.general.ColorConstant.EMPTY;
import static tesuji.games.go.util.GoArray.copy;
import static tesuji.games.go.util.GoArray.createShorts;
import tesuji.core.util.FlyWeight;
import tesuji.core.util.SynchronizedArrayStack;

/**
 * 
 */
public class PointSet
	implements FlyWeight
{
	private short[] _pointList;
	private short[] _pointIndex;
	private short _nrPoints;
	
	private boolean _frozen = false;
	
	private SynchronizedArrayStack<PointSet> _owner;
	
	PointSet(SynchronizedArrayStack<PointSet> owner)
	{
		_owner = owner;
		_pointList = createShorts();
		_pointIndex = createShorts();
		reset();
	}
	
	public final void add(int xy)
	{
		assert(!_frozen);
		_pointList[_nrPoints] = (short)xy;
		_pointIndex[xy] = _nrPoints;
		_nrPoints++;	
	}
	
	public final void remove(int xy)
	{
		assert(!_frozen);
		short pointIndex = _pointIndex[xy];
//		if (pointIndex<0)
//			return;
		_nrPoints--;
		_pointIndex[_pointList[_nrPoints]] = pointIndex;
		_pointList[pointIndex] = _pointList[_nrPoints];
//		_pointIndex[xy] = -1;
	}
	
	public final int getSize()
	{
		return _nrPoints;
	}
	
	public final int get(int index)
	{
		return _pointList[index];
	}
	
	public final void copyFrom(PointSet source)
	{
		assert(!_frozen);
		copy(source._pointList, _pointList);
		copy(source._pointIndex,_pointIndex);
		_nrPoints = (short)source.getSize();
	}
	
	public final void clear()
	{
		GoArray.clear(_pointList);
		GoArray.clear(_pointIndex);
		reset();
	}
	
	public final void reset()
	{
		assert(!_frozen);
		_nrPoints = 0;
	}
	
	public final void recycle()
	{
		assert(!(_frozen = false));
		reset();
		_owner.push(this);
	}
	
	public boolean isConsistent(byte[] board)
	{
    	for (int i=getSize(); --i>=0;)
    		assert board[get(i)]==EMPTY : "\n" + GoArray.toString(board) + "\n";    		
    	
    	return true;
	}
	
	public boolean freeze()
	{
		return (_frozen = true);
	}
	
	public boolean unfreeze()
	{
		return !(_frozen = false);
	}
	
	public boolean hasPoint(int xy)
	{
		return (_pointIndex[xy]<_nrPoints && _pointList[_pointIndex[xy]]==xy);
	}
	
	public boolean isSameSet(PointSet compare)
	{
    	for (int i=getSize(); --i>=0;)
    	{
    		if (!compare.hasPoint(get(i)))
    			return false;
    	}
    	for (int i=compare.getSize(); --i>=0;)
    	{
    		if (!hasPoint(compare.get(i)))
    			return false;
    	}
    	return true;
	}
}
