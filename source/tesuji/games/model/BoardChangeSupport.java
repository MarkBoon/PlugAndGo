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

import java.util.ArrayList;

/**
 * This class implements the basic functionality required for sending board
 * changes as defined by BoardModel.
 */
public class BoardChangeSupport
{
    private boolean _multipleChanges;
    private ArrayList<BoardModelListener> _listeners = new ArrayList<BoardModelListener>();
    private ArrayList<BoardChange> _eventQueue = new ArrayList<BoardChange>();
    private boolean _hasListeners = false;
    
    public final void addBoardModelListener(BoardModelListener listener)
    {
    	assert(listener!=null);
    	_hasListeners = true;
        if (!_listeners.contains(listener))
            _listeners.add(listener);
    }
    
    public final void removeBoardModelListener(BoardModelListener listener)
    {
        _listeners.remove(listener);
        if (_listeners.size()==0)
			_hasListeners = false;
    }
    
    public final boolean hasListeners()
    {
    	return _hasListeners;
    }

    public void sendBoardChange(BoardChange change)
    {
    	if (_multipleChanges)
    		_eventQueue.add(change);
    	else
    		sendChange(change);
    }
    
    public final void setMultipleChanges(boolean flag)
    {
        if (flag==_multipleChanges)
            return;
        
        _multipleChanges = flag;
        
        if (_multipleChanges==false)
        {
        	for (int i=0; i<_eventQueue.size(); i++)
        		sendChange(_eventQueue.get(i));
        	_eventQueue.clear();
        }
    }

    private void sendChange(BoardChange changeEvent)
    {
        for (BoardModelListener listener : _listeners)
            listener.changeBoard(changeEvent);
    }
    
    public void sendChange(int xy, byte value, byte oldValue)
    {
		BoardChange boardChange = BoardChangeFactory.createBoardChange(xy, value, oldValue);
		sendBoardChange(boardChange);
		boardChange.recycle();
    }
}
