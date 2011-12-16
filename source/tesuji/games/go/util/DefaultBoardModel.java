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

import tesuji.games.model.BoardChange;
import tesuji.games.model.BoardChangeSupport;
import tesuji.games.model.BoardChangeFactory;
import tesuji.games.model.BoardModel;
import tesuji.games.model.BoardModelListener;

import static tesuji.games.go.util.GoArray.*;

/**
 * BoardModel implementation based on a ByteArray implementation using 
 * BoardChangeSupport to delegate the propagation of changes.
 */
public class DefaultBoardModel
	extends ByteArrayImpl
	implements BoardModel
{
	private BoardChangeSupport _boardchangeSupport = new BoardChangeSupport();

    /**
     * @param size
     */
	public DefaultBoardModel(int size)
    {
		super(size,GoArray.createBoardArray(size));
    }

	/**
	 * @param size
	 * @param array
	 */
	public DefaultBoardModel(int size, byte[] array)
	{
		super(size,array);
	}

	/**
	 * @param boardSize
	 */
	@Override
	public void setBoardSize(int boardSize)
	{
		super.setBoardSize(boardSize);
        _array = GoArray.createBoardArray(boardSize);
	}
	
	@Override
	public void set(int xy, byte value)
	{
		if (_boardchangeSupport.hasListeners())
		{
			byte oldValue = _array[xy];
			_array[xy] = value;
			
			BoardChange boardChange = BoardChangeFactory.createBoardChange(xy, value, oldValue);
			_boardchangeSupport.sendBoardChange(boardChange);
			boardChange.recycle();
		}
		else
			_array[xy] = value;		
	}
	
	@Override
	public void set(int x, int y, byte value)
	{
		if (_boardchangeSupport.hasListeners())
		{
			byte oldValue = _array[toXY(x,y)];
			_array[toXY(x,y)] = value;
			
			BoardChange boardChange = BoardChangeFactory.createBoardChange(x, y, value, oldValue);
			_boardchangeSupport.sendBoardChange(boardChange);
			boardChange.recycle();
		}
		else
			_array[toXY(x,y)] = value;		
	}
	
	/* (non-Javadoc)
     * @see tesuji.games.model.BoardModel#addBoardModelListener(tesuji.games.model.BoardModelListener)
     */
    public void addBoardModelListener(BoardModelListener listener)
    {
    	_boardchangeSupport.addBoardModelListener(listener);
    }

	/* (non-Javadoc)
     * @see tesuji.games.model.BoardModel#removeBoardModelListener(tesuji.games.model.BoardModelListener)
     */
    public void removeBoardModelListener(BoardModelListener listener)
    {
    	_boardchangeSupport.removeBoardModelListener(listener);
    }

	/* (non-Javadoc)
     * @see tesuji.games.model.BoardModel#setMultipleChanges(boolean)
     */
    public void setMultipleChanges(boolean flag)
    {
    	_boardchangeSupport.setMultipleChanges(flag);
    }
    
    @Override
	public String toString()
    {
    	return GoArray.printBoardToString(getSingleArray());
    }
    
    @Override
	public boolean equals(Object o)
    {
    	if (!(o instanceof DefaultBoardModel))
    		throw new IllegalStateException();
    	
    	DefaultBoardModel compare = (DefaultBoardModel)o;
    	byte[] compareArray = compare.getSingleArray();
    	for (int i=0; i<compareArray.length; i++)
    		if (compareArray[i]!=_array[i])
    			return false;
    	
    	return true;
    }
    
    public DefaultBoardModel createClone()
    {
    	DefaultBoardModel clone = new DefaultBoardModel(getBoardSize());
    	System.arraycopy(getSingleArray(), 0, clone.getSingleArray() , 0, getSingleArray().length);

    	return clone;
    }
}
