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
package tesuji.games.model;

import tesuji.core.util.FlyWeight;
import tesuji.core.util.SynchronizedArrayStack;
import tesuji.games.go.util.GoArray;

/**
 * A class used to store and pass board-changes.
 */
public class BoardChange
	implements FlyWeight
{
	private int _xy;
	private byte _newValue;
	private byte _oldValue;
	
	private SynchronizedArrayStack<BoardChange> _owner;
	
	BoardChange(SynchronizedArrayStack<BoardChange> owner)
	{
		_owner = owner;
	}
	
	public byte getOldValue()
	{
		return _oldValue;
	}
	
	public byte getNewValue()
	{
		return _newValue;
	}
	
	public int getXY()
	{
		return _xy;
	}
	
	public int getX()
	{
		return GoArray.getX(_xy);
	}
	
	public int getY()
	{
		return GoArray.getY(_xy);
	}
	
	public void recycle()
	{
		_owner.push(this);
	}

	// These are package scope so they can only be set by the factory.
	void setXY(int xy)
    {
    	this._xy = xy;
    }

	void setNewValue(byte newValue)
    {
    	this._newValue = newValue;
    }

	void setOldValue(byte oldValue)
    {
    	this._oldValue = oldValue;
    }
}