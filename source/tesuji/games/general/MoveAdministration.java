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

import tesuji.core.util.List;
import tesuji.games.model.BoardModel;

/**
 * Interface for MoveAdministration of a game.
 */
public interface MoveAdministration<MoveType extends Move,PropertiesType extends GameProperties>
{
	/**
	 * Clear all move administration.
	 * Equivalent to starting a new game with default properties.
	 * But also used to release any FlyWeight objects held by it.
	 */
	public void clear();
	
	/**
	 * @return color of who is to move next
	 */
	public byte getColorToMove();

	/**
	 * @return number of the next move to be played.
	 */
	public int getNextMoveNr();

	/**
	 * @return the last move that was played.
	 */
	public MoveType getLastMove();

	/**
	 * @param move
	 * @return whether move is legal in the current game.
	 */
	public boolean isLegalMove(MoveType move);
    
    /**
     * Play a move
     * 
     * @param move
     */
    public void playMove(MoveType move);

    /**
     * Take back a move
     */
    public void takeBack();
    
    /**
     * Each call to playMove (and takeBack) has a different board-state as a result.
     * This board-state is represented by BoardModel.
     * 
     * @return BoardModel
     */
    public BoardModel getBoardModel();
    
    /**
     * Get the moves played so far in an array.
     * 
     * @return List<MoveType>
     */
    public List<MoveType> getMoves();
    
    /**
     * @return a Properties sub-type that has the pre-game settings. Like board-size etc.
     */
    public PropertiesType getGameProperties();
    
    public boolean isGameFinished();
}
