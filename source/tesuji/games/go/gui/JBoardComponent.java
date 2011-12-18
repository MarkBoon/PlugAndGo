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
package tesuji.games.go.gui;

import tesuji.games.general.DefaultMoveProducer;
import tesuji.games.general.Move;
import tesuji.games.general.MoveEvent;
import tesuji.games.general.MoveListener;
import tesuji.games.general.MoveProducer;
import tesuji.games.model.BoardChange;
import tesuji.games.model.BoardModel;
import tesuji.games.model.BoardModelListener;

/**
 * Abstract class defining a graphical board component
 */
public abstract class JBoardComponent<MoveType extends Move>
	extends	javax.swing.JComponent
	implements MoveProducer<MoveType>, BoardModelListener
{
    protected BoardModel _boardModel;
    
	private DefaultMoveProducer<MoveType> _moveProducer = new DefaultMoveProducer<MoveType>();
    
    /**
     * Set the model this component uses to display its data
     * 
     * @param model
     */
    public void setModel(BoardModel model)
    {
        if (_boardModel!=null)
            _boardModel.removeBoardModelListener(this);
        
        _boardModel = model;
        
        _boardModel.addBoardModelListener(this);
    }
    
    /**
	 * This method just causes a repaint. The board component uses a
	 * BoardModel instance to draw what's on the board, it doesn't keep
	 * game-information itself. Therefore the implementation of
	 * BoardModelListener only needs to cause a repaint so that the board can be
	 * drawn based upon the BoardModel.
	 * 
	 * If you want to provide a more efficient way to update the board-component
	 * you can override this method in the subclass and just 'paint' the point
	 * that was changed in an off-screen buffer.
	 * 
	 * @see tesuji.games.model.BoardModelListener#changeBoard(tesuji.games.model.BoardChange)
	 */
    public void changeBoard(BoardChange change)
    {
        repaint();
    }
    
    // The methods below are the MoveProducer implementation using
	// DefaultMoveProducer as a delegate.
	
	/**
	 * @see tesuji.games.general.MoveProducer#addMoveListener(tesuji.games.general.MoveListener)
	 */
	public void addMoveListener( MoveListener<MoveType> listener )
	{
		_moveProducer.addMoveListener(listener);
	}

	/**
	 * @see tesuji.games.general.MoveProducer#removeMoveListener(tesuji.games.general.MoveListener)
	 */
	public void removeMoveListener( MoveListener<MoveType> listener )
	{
		_moveProducer.removeMoveListener(listener);
	}

	/**
	 * @see tesuji.games.general.MoveProducer#notifyMove(tesuji.games.general.MoveEvent)
	 */
	public void notifyMove( MoveEvent<MoveType> event )
	{
		_moveProducer.notifyMove(event);
	}

 	/**
 	 * @see tesuji.games.general.MoveProducer#notifyTakeBack(tesuji.games.general.MoveEvent)
 	 */
 	public void notifyTakeBack()
	{
		_moveProducer.notifyTakeBack();
	}
}