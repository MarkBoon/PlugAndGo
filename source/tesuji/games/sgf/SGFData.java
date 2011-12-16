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
package tesuji.games.sgf;

import java.util.ArrayList;

import tesuji.core.util.FlyWeight;
import tesuji.core.util.SynchronizedArrayStack;

import tesuji.games.general.Move;

/**
 * Node data in a game-tree as defined in SGF
 * Each node contains a collection of properties.
 *
 */
public class SGFData<MoveType extends Move>
	implements FlyWeight
{
	private static final long serialVersionUID = -6112070946683551432L;
		
	private ArrayList<SGFProperty> _propertyList;
	private MoveType _move;
	
	// This is kept for reference in the root for checking purposes.
	private String _sgfString;
	
	private SynchronizedArrayStack<SGFData<MoveType>> _owner;
	
	/**
	 * 
	 */
	SGFData(SynchronizedArrayStack<SGFData<MoveType>> owner)
	{
		_owner = owner;
		_propertyList = new ArrayList<SGFProperty>();
	}
	
	/**
	 * @return list of properties of the node
	 */
	public ArrayList<SGFProperty> getPropertyList()
	{
		return _propertyList;
	}
	
	/**
	 * Add a property to this node
	 * 
	 * @param property
	 */
	public void addProperty(SGFProperty property)
	{
		_propertyList.add(property);
	}
	
	public MoveType getMove()
	{
		return _move;
	}
	
	public void setMove(MoveType move)
	{
		if (_move!=null)
			_move.recycle();

		_move = move;
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.core.util.FlyWeight#recycle()
	 */
	public void recycle()
    {
        for (int i=_propertyList.size(); --i>=0;)
            _propertyList.get(i).recycle();
        _propertyList.clear();

        if (_move!=null)
        	_move.recycle();
        _move = null;

        _owner.push(this);
    }

	/**
     * @return the sgfString
     */
    public String getSGF()
    {
    	return _sgfString;
    }

	/**
     * @param sgfString the sgfString to set
     */
    public void setSGF(String sgfString)
    {
    	_sgfString = sgfString;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	if (_sgfString!=null)
    		return _sgfString;

    	return super.toString();
    }
}
