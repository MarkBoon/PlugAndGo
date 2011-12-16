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

import java.text.ParseException;

import tesuji.core.util.Factory;
import tesuji.games.sgf.SGFData;

public interface MoveFactory<MoveType extends Move>
	extends Factory
{
	/**
	 * Factory method for creating an uninitialized move.
	 * 
	 * @return uninitialized move
	 */
	public MoveType createMove();
	
	/**
	 * Factory method for cloning a move.
	 * 
	 * @param move
	 * @return exact copy of move
	 */
	public MoveType cloneMove(MoveType move);
	
	/**
	 * Parse data a string containing a move in standard notation.
	 * 
	 * @param moveString
	 * @return move obtained by 'parsing' a standard move notation
	 */
	public MoveType parseMove(String moveString);
	
	/**
	 * Parse data obtained from a SGF file and create a move from it.
	 * 
	 * @param sgfData
	 * @return move obtained by 'parsing' a SGFData
	 */
	public MoveType parseMove(SGFData<MoveType> sgfData);
	
	/**
	 * Parse a string containing sgf-data, possibly a whole game and create
	 * a MoveStack containing the moves represented in the SGF.
	 * 
	 * @param sgfData
	 * @return move obtained by 'parsing' a SGFData
	 */
	public MoveStack<MoveType> parseSGF(String sgfData) throws ParseException;
	
	/**
	 * Parse a text-based diagram of a game position and return a list of moves
	 * that results in that position.
	 * 
	 * @param diagram
	 * 
	 * @return MoveStack<MoveType>
	 */
    public MoveStack<MoveType> parseDiagram(String[] diagram);
    
	/**
	 * Create a move from a BoardMark. Since not all BoardMarks translate into a move this can return null.
	 * 
	 * @param boardMark
	 * @return move
	 */
	public MoveType createMove(BoardMark boardMark);
	
	/**
	 * Create a pass-move. Also called a null-move.
	 * 
	 * @return move
	 */
	public MoveType createPassMove(byte color);
	
	/**
	 * Create a move that resigns the game.
	 * 
	 * @return move
	 */
	public MoveType createResignMove(byte color);

	/**
	 * Create a dummy-move. A move that has the color defined, but no coordinate.
	 * 
	 * @return move
	 */
	public MoveType createDummyMove(byte color);	
}
