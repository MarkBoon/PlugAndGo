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
package tesuji.games.general;

import java.io.Serializable;

import tesuji.core.util.ArrayList;
import tesuji.core.util.FlyWeight;

/**
 * Interface class defining a move.
 * Basically a move has a color, indicating which player played it, and a
 * move-number. All game-specific data has been left out.
 */
public interface Move
	extends Serializable, FlyWeight
{
	/**
	 * @return this move's GTP representation
	 */
	public String toGTP();
	
	/**
	 * @return this move's GTP representation including the color of the move
	 */
	public String toGTPWithColor();
	
	/**
	 * @return this move's SGF representation
	 */
	public String toSGF();
	
	/**
	 * @return the move-number
	 */
	public int getMoveNr();
	
	/**
	 * Set the move-number.
	 * 
	 * @param nr
	 */
	public void setMoveNr(int nr);

	/**
	 * @return the color of the player who played the move.
	 */
	public byte getColor();
	
	/**
	 * Set the color of the player who played the move.
	 */
	public void setColor(byte color);
	
	/**
	 * @return whether this move was a pass.
	 */
	public boolean isPass();
	
	/**
	 * @return this move resigned the game.
	 */
	public boolean isResignation();
	
	/**
	 * @return Text associated with this move, like an annotation or comment.
	 */
	public String getText();
	
	/**
	 * @param text String associated with this move, like an annotation or comment.
	 */
	public void setText(String text);
	
	/**
	 * @return a cloned copy of the move
	 */
	public Move cloneMove();
	
	/**
	 * @return whether this move is initialised with proper move data.
	 */
	public boolean isInitialised();
	
	/**
	 * @return whether this move has board-marks attached to it.
	 */
	public boolean hasBoardMarks();
	
	public ArrayList<BoardMark> getBoardMarks();

	public void addBoardMark(BoardMark boardMark);

}