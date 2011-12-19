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

import tesuji.games.go.common.BasicGoMoveAdministration;
import tesuji.games.go.common.GoEngineAdapter;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.GoGameProperties;
import tesuji.games.model.BoardModel;

import static tesuji.games.general.ColorConstant.*;

/**
 * Go playing engine that generates random (but legal) moves.
 */
public class RandomTestEngine
    extends GoEngineAdapter
{
	/**
	 * This is the maximum number of tries the random generator will try to generate e legal move.
	 */
	private static final int MAX_TRIES = 100000;
	
	private int _boardSize = GoArray.DEFAULT_SIZE;
	
	private BasicGoMoveAdministration _moveAdministration;

	public RandomTestEngine()
	{
		_moveAdministration = new BasicGoMoveAdministration(getGameProperties());
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#getEngineName()
	 */
	@Override
	public String getEngineName()
	{
		return "RandomGo";
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#getEngineVersion()
	 */
	@Override
	public String getEngineVersion()
	{
		return "1.0";
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#clearBoard()
	 */
	@Override
	public void clearBoard()
	{
		_boardSize = getGameProperties().getIntProperty(GoGameProperties.BOARDSIZE);
		_moveAdministration.clear();
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#play(tesuji.games.go.common.GoMove)
	 */
	@Override
	public void playMove(GoMove move)
	{
		_moveAdministration.playMove(move);
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#takeBack()
	 */
	@Override
	public void takeBack()
	{
		_moveAdministration.takeBack();
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#requestMove(byte)
	 */
	@Override
	public GoMove requestMove(byte color)
	{
		for (int i=0; i<MAX_TRIES; i++)
		{
			// Pick a randome move-coordinate.
			int randomX = (int) Math.round(Math.random()*_boardSize)+1;
			int randomY = (int) Math.round(Math.random()*_boardSize)+1;
			
			// Use the Movefactory to create a GoMove object.
			GoMove move = getMoveFactory().createMove(randomX, randomY, color);
			
			// Check if it's actually a legal move. If it is then return it.
			if (_moveAdministration.isLegalMove(move) && !isEye(move))
				return move;
			else
				move.recycle(); // Unused moves go back to the factory.
		}
		
		return getMoveFactory().createPassMove(color);
	}

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#setup(tesuji.core.util.ArrayList)
     */
    @Override
	public void setup(Iterable<GoMove> moveList)
    {
    	_moveAdministration.clear();
    	for (GoMove move : moveList)
    		_moveAdministration.playMove(move);
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.general.GameEngineAdapter#getScore()
     */
    @Override
    public Double getScore()
    {
    	return new Double(_moveAdministration.getScore());
    }
    
    /**
	 *	A rather crudely implemented method that checks whether a move would fill an eye.
	 *	Without this check a random player can go on forever.
	 *
     * @param move
     * 
     * @return boolean indicating whether the move passed as parameter would fill its own eye.
     */
    private boolean isEye(GoMove move)
    {
    	byte oppositeColor = opposite(move.getColor());
    	int x = move.getX();
    	int y = move.getY();
    	BoardModel board = _moveAdministration.getBoardModel();
    	
       	if (board.get(x-1, y)==oppositeColor || board.get(x-1, y)==EMPTY)
    		return false;
       	if (board.get(x+1, y)==oppositeColor || board.get(x+1, y)==EMPTY)
    		return false;
       	if (board.get(x, y-1)==oppositeColor || board.get(x, y-1)==EMPTY)
    		return false;
       	if (board.get(x, y+1)==oppositeColor || board.get(x, y+1)==EMPTY)
    		return false;

       	int oppositeDiagonals = 0;
       	if (board.get(x-1, y-1)==oppositeColor)
    		oppositeDiagonals++;
       	if (board.get(x+1, y-1)==oppositeColor)
    		oppositeDiagonals++;
       	if (board.get(x-1, y+1)==oppositeColor)
    		oppositeDiagonals++;
       	if (board.get(x+1, y+1)==oppositeColor)
    		oppositeDiagonals++;
       	
       	if (oppositeDiagonals>1)
       		return false;
       	if (oppositeDiagonals==1 && (x==1 || y==1 || x==_boardSize || y==_boardSize))
       		return false;
       	
     	return true;
    }
}
