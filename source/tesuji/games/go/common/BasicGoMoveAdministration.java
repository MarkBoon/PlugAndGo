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
package tesuji.games.go.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import tesuji.games.general.MoveAdministration;
import tesuji.games.general.MoveStack;
import tesuji.games.go.util.BoardMarker;
import tesuji.games.go.util.DefaultBoardModel;
import tesuji.games.go.util.FourCursor;
import tesuji.games.go.util.GoGameProperties;
import tesuji.games.model.BoardModel;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;
import static tesuji.games.go.util.GoArray.*;

/**
 * Very basic implementation of a move administration.
 */
public class BasicGoMoveAdministration
	implements MoveAdministration<GoMove,GoGameProperties>, PropertyChangeListener
{
	private static final byte BLACK_POINT = 	1;
	private static final byte WHITE_POINT = 	-1;
	private static final byte NEUTRAL_POINT = 	2;
	private static final byte UNDEFINED_POINT =	0;
	
	private byte _startingColor;
	private byte _colorToMove;
	
	private GoGameProperties	_gameProperties;
	private MoveStack<GoMove>	_moveStack;
	private DefaultBoardModel	_board;
	private DefaultBoardModel	_scoreBoard;

	private BoardMarker _boardMarker = new BoardMarker();
	
	public BasicGoMoveAdministration(String[] diagram)
	{
		setup(diagram);
	}
	
	public BasicGoMoveAdministration(GoGameProperties gameProperties)
	{
		_gameProperties = gameProperties;
		_gameProperties.addPropertyChangeListener(this);
		init(_gameProperties.getIntProperty(GoGameProperties.BOARDSIZE),BLACK);
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.MoveAdministration#getGameProperties()
	 */
	public GoGameProperties getGameProperties()
	{
		return _gameProperties;
	}
	
	private void init(int boardSize, byte startingColor)
	{
		_startingColor = startingColor;
		_moveStack = new MoveStack<GoMove>(MAXMOVE);
        _board = new DefaultBoardModel(boardSize);
        if (_gameProperties==null)
        	_gameProperties = new GoGameProperties();
        _gameProperties.setIntProperty(GoGameProperties.BOARDSIZE, boardSize);
		clear();
		
	}
	
	/* (non-Javadoc)
     * @see tesuji.games.general.MoveAdministration#clear()
     */
   public void clear()
    {
    	_colorToMove = _startingColor;
	    int boardSize = _gameProperties.getIntProperty(GoGameProperties.BOARDSIZE);
	    _board.setBoardSize(boardSize);
    	
		while (!_moveStack.isEmpty())
		{
			GoMove goMove = _moveStack.pop();
			goMove.recycle();
		}
		
		for (int i=0; i<MAX; i++)
		{
			if (_board.get(i)!=EDGE)
				_board.set(i, EMPTY);
		}
    }
    

    /* (non-Javadoc)
     * @see tesuji.games.general.MoveAdministration#getColorToMove()
     */
    public byte getColorToMove()
    {
	    return _colorToMove;
    }
    
	/* (non-Javadoc)
     * @see tesuji.games.general.MoveAdministration#getNextMoveNr()
     */
    public int getNextMoveNr()
    {
	    return _moveStack.size()+1;
    }
    
	/* (non-Javadoc)
     * @see tesuji.games.general.MoveAdministration#getLastMove()
     */
    public GoMove getLastMove()
    {
    	if (_moveStack.isEmpty())
    		return null;

	    return _moveStack.peek();
    }

	/**
	 * It's probably not a good idea to expose this kind of detail
	 * at this level. Instead it can be obtained from the BoardModel
	 * if desired. Since there are still some unit-tests using it
	 * it's just flagged as deprecated.
	 * 
     * @deprecated
     */
    public byte[] getBoardArray()
    {
	    return _board.getSingleArray();
    }
    
	/* (non-Javadoc)
     * @see tesuji.games.general.MoveAdministration#getBoardModel()
     */
    public BoardModel getBoardModel()
    {
	    return _board;
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.general.MoveAdministration#getMoves()
     */
    public MoveStack<GoMove> getMoves()
    {
    	return _moveStack;
    }
    
	/**
	 * This is a very simple test whether a move is legal. It does prohibit
	 * straightforward ko, but it does not prohibit longer repetition cycles.
     */
    public boolean isLegalMove(GoMove move)
    {
    	int moveXY = move.getXY();
    	if (moveXY==PASS || moveXY==RESIGN)
    		return true;
    	
    	// Check if the move is within range and on an empty point.
		if (moveXY<0 || moveXY>=MAX || _board.get(moveXY)!=EMPTY)
			return false;					// Occupied.
		
		byte[] board = _board.getSingleArray(); // Can't use the board-model because it will trigger events.
		board[moveXY] = move.getColor();
		
		// If the move has at least one liberty it's legal.
		if (hasLiberty(moveXY))
		{
			board[moveXY] = EMPTY;
			return true;
		}

		byte otherColor = opposite(move.getColor());
		int left = left(moveXY);
		int right = right(moveXY);
		int above = above(moveXY);
		int below = below(moveXY);

		// A check is made if the move captures something, in which case it's not suicide.
		if ((_board.get(left)!=otherColor || hasLiberty(left))
		&& (_board.get(right)!=otherColor || hasLiberty(right))
		&& (_board.get(above)!=otherColor || hasLiberty(above)) 
		&& (_board.get(below)!=otherColor || hasLiberty(below)))
		{
			board[moveXY] = EMPTY;
			return false; // No liberties, no capture. So it's suicide.
		}
		
		// Check for KO.
		// If the previous move captured a single stone and this move tries
		// to play at the point where that stone was captured, then it's ko,
		// given the previous tests that the move makes no other liberties.
		GoMove lastMove = getLastMove();
		if (lastMove!=null && !lastMove.isPass() && !lastMove.isResignation())
		{
			int lastMoveXY = lastMove.getXY();
			if (_board.get(left(lastMoveXY)) == otherColor
			 || _board.get(right(lastMoveXY)) == otherColor
			 || _board.get(above(lastMoveXY)) == otherColor
			 || _board.get(below(lastMoveXY)) == otherColor)
			{
				board[moveXY] = EMPTY;
				return true;
			}
			if (lastMove.hasCaptives())
			{
				if (lastMove.getCaptives().peek()==moveXY)
				{
					board[moveXY] = EMPTY;
					return false; // Ko
				}
			}
		}
		
		board[moveXY] = EMPTY;
		return true;
    }
    
	/* (non-Javadoc)
     * @see tesuji.games.general.MoveListener#playMove(tesuji.games.general.Move)
     */
    public void playMove(GoMove move)
    {
    	assert isLegalMove(move) : "Illegal move";
    	
    	GoMove goMove = (GoMove)move.cloneMove();
    	_moveStack.push(goMove);

		byte oppositeColor;
		if (goMove.getColor()==COLOR_UNDEFINED)
			oppositeColor = opposite(_colorToMove);
		else
			oppositeColor = opposite(goMove.getColor());
		
		int xy = goMove.getXY();
		if (xy!=PASS && xy!=RESIGN)
		{
			_board.set(xy, goMove.getColor());
			
			// See if there's a chain next to this move that has no liberties and 
			// remove it from the board.
			if (_board.get(left(xy)) == oppositeColor
			                && !hasLiberty(left(xy)))
				removeCapturedChain(left(xy), oppositeColor, goMove);
			if (_board.get(right(xy)) == oppositeColor
			                && !hasLiberty(right(xy)))
				removeCapturedChain(right(xy), oppositeColor, goMove);
			if (_board.get(above(xy)) == oppositeColor
			                && !hasLiberty(above(xy)))
				removeCapturedChain(above(xy), oppositeColor, goMove);
			if (_board.get(below(xy)) == oppositeColor
			                && !hasLiberty(below(xy)))
				removeCapturedChain(below(xy), oppositeColor, goMove);
			
			if (!hasLiberty(xy))
				removeCapturedChain(xy, _colorToMove, goMove);
		}
		
		_colorToMove = oppositeColor;
	}
    
	/* (non-Javadoc)
     * @see tesuji.games.general.MoveListener#takeBack(tesuji.games.general.MoveEvent)
     */
    public void takeBack()
    {
		GoMove lastMove = _moveStack.pop();
		_colorToMove = lastMove.getColor();
		byte oppositeColor = opposite(_colorToMove);
		_board.set(lastMove.getXY(), EMPTY);
		if (lastMove.hasCaptives())
		{
			for (int i=lastMove.getCaptives().getSize(); --i>=0;)
				_board.set(lastMove.getCaptives().get(i), oppositeColor);
		}
		lastMove.recycle();
    }

    public void setup(String[] diagram)
    {
    	init(diagram.length,BLACK);
    	MoveStack<GoMove> moveList = GoMoveFactory.getSingleton().parseDiagram(diagram);
		for (GoMove move : moveList)
			playMove(move);
    	
    }

    /**
     * Note how it uses boardMarker to track which stones have been visited.
     * It's necessary to prevent infinite recursion.
     * 
     * @param xy
     * @return whether there's a liberty.
     */
    private boolean hasLiberty(int xy)
    {
        _boardMarker.getNewMarker();
        return _hasLiberty(xy);
    }    

    private boolean _hasLiberty(int xy)
    {
    	_boardMarker.set(xy);
    	
    	if (_board.get(left(xy))==EMPTY)
    		return true;
    	if (_board.get(right(xy))==EMPTY)
    		return true;
    	if (_board.get(above(xy))==EMPTY)
    		return true;
    	if (_board.get(below(xy))==EMPTY)
    		return true;
    	
        byte boardPoint = _board.get(xy);
    	if (_board.get(left(xy)) == boardPoint
		                && _boardMarker.notSet(left(xy))
		                && _hasLiberty(left(xy)))
			return true;
		if (_board.get(right(xy)) == boardPoint
		                && _boardMarker.notSet(right(xy))
		                && _hasLiberty(right(xy)))
			return true;
		if (_board.get(above(xy)) == boardPoint
		                && _boardMarker.notSet(above(xy))
		                && _hasLiberty(above(xy)))
			return true;
		if (_board.get(below(xy)) == boardPoint
		                && _boardMarker.notSet(below(xy))
		                && _hasLiberty(below(xy)))
			return true;
    					
    	return false;
    }
    
    private void removeCapturedChain(int xy, byte color, GoMove move)
    {
        move.addCaptive(xy);
    	_board.set(xy, EMPTY);

    	if (_board.get(left(xy)) == color)
    		removeCapturedChain(left(xy), color, move);
    	if (_board.get(right(xy)) == color)
    		removeCapturedChain(right(xy), color, move);
    	if (_board.get(above(xy)) == color)
    		removeCapturedChain(above(xy), color, move);
    	if (_board.get(below(xy)) == color)
    		removeCapturedChain(below(xy), color, move);
    }

    public boolean isGameFinished()
    {
    	if (_moveStack.peek().isPass() && _moveStack.peek(1).isPass() && _moveStack.peek(2).isPass())
    		return true;
    	return false;
    }
    /*
     * (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getPropertyName().equals(GoGameProperties.BOARDSIZE))
			clear();
	}
	
	public double getScore()
	{
		double score = 0.0;
		score -= _gameProperties.getDoubleProperty(GoGameProperties.KOMI);

		_scoreBoard = new DefaultBoardModel(_board.getBoardSize());
		for (int i=0; i<MAX; i++)
		{
			if (_board.get(i)==BLACK)
			{
				_scoreBoard.set(i, BLACK_POINT);
				for (int n=0; n<4; n++)
				{
					int neighbour = FourCursor.getNeighbour(i, n);
					if (_scoreBoard.get(neighbour)==UNDEFINED_POINT)
						floodFill(neighbour,BLACK_POINT);
				}
			}
			if (_board.get(i)==WHITE)
			{
				_scoreBoard.set(i, WHITE_POINT);
				for (int n=0; n<4; n++)
				{
					int neighbour = FourCursor.getNeighbour(i, n);
					if (_scoreBoard.get(neighbour)==UNDEFINED_POINT)
						floodFill(neighbour,WHITE_POINT);
				}
			}
		}
		
		for (int i=0; i<MAX; i++)
		{
			if (_board.get(i)!=EDGE)
			{
				if (_scoreBoard.get(i)==BLACK_POINT)
					score += 1.0;
				if (_scoreBoard.get(i)==WHITE_POINT)
					score -= 1.0;
			}
		}
		
		return score;
	}
	
	private void floodFill(int xy, byte value)
	{
		if (_scoreBoard.get(xy)==value || _board.get(xy)!=EMPTY)
			return;
		
		_scoreBoard.set(xy, value);
		
		boolean hasBlackNeighbour = false;
		boolean hasWhiteNeighbour = false;
		for (int n=0; n<4; n++)
		{
			int neighbour = FourCursor.getNeighbour(xy, n);
			if (_scoreBoard.get(neighbour)==BLACK_POINT)
				hasBlackNeighbour = true;
			if (_scoreBoard.get(neighbour)==WHITE_POINT)
				hasWhiteNeighbour = true;
		}
		if (hasBlackNeighbour && hasWhiteNeighbour)
			neutralize(xy);
		else
		{
			for (int n=0; n<4; n++)
			{
				int neighbour = FourCursor.getNeighbour(xy, n);
				if (_scoreBoard.get(neighbour)==UNDEFINED_POINT)
					floodFill(neighbour, value);
			}
		}
	}
	
	private void neutralize(int xy)
	{
		if (_scoreBoard.get(xy)==NEUTRAL_POINT  || _board.get(xy)!=EMPTY)
			return;
		
		_scoreBoard.set(xy, NEUTRAL_POINT);
		
		for (int n=0; n<4; n++)
		{
			int neighbour = FourCursor.getNeighbour(xy, n);
			neutralize(neighbour);
		}		
	}
}
