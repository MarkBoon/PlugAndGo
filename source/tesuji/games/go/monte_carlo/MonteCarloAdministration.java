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

package tesuji.games.go.monte_carlo;

import tesuji.games.general.Move;
import tesuji.games.general.MoveFactory;
import tesuji.games.general.MoveIterator;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.util.IntStack;
import tesuji.games.model.BoardModel;

/**
 * This interface describes a board administration that can perform a Monte-Carlo simulation.
 */
public interface MonteCarloAdministration
{
	/**
	 * Clear the administrative information. This is equivalent to starting
	 * a new game with an empty board.
	 */
	public void clear();
	
	/**
	 * Copy all data from a starting point. The difference with createClone()
	 * is that it might not allocate the data-structure and it may not need to
	 * copy some fixed data.
	 * 
	 * @param source
	 */
	public void copyDataFrom(MonteCarloAdministration source);

	/**
	 * @return a perfect, newly created, copy.
	 */
	public MonteCarloAdministration createClone();
			
	/**
	 * Decide whether a move is legal or not.
	 */
	public boolean isLegalMove(GoMove move);

	/**
	 * Decide whether a move will not be used during the playout sequence.
	 * This is typically the case for moves that would fill the player's own eye(s).
	 */
	public boolean isVerboten(GoMove move);

	/**
	 * Play a move of a certain color and update the administration, whatever information it is keeping.
	 */
	public void playMove(GoMove move);

	/**
	 * Play a move of a certain color and update the administration, whatever information it is keeping.
	 */
	public void playExplorationMove(GoMove move);

	/**
	 * Generate a playout sequence until the end.
	 * 
	 * @return whether Black is the winner or not.
	 */
	public boolean playout();

	/**
	 * Select a move but don't play it.
	 * This method assumes the color to move is known.
	 * 
	 * @return coordinate of the move
	 */
	public GoMove selectSimulationMove();
	
	public MoveIterator<GoMove> getMoves();
	
	/**
	 * Get the score. Although it will depend on the actual implementation, generally
	 * what all of them will have in common is that all stones still on the board
	 * are counted towards their color. Empty points are counted for the color that
	 * surrounds it. If a point or empty area is neighbour to both colors, it's not
	 * counted for either side. 
	 * 
	 * @return
	 */
	public double getScore();
	
	/**
	 * @return the color for the winning side
	 */
	public byte getWinner();

	/**
	 * @return whether the game has run too long (probably some repetition)
	 */
	public boolean isGameTooLong();
	
	/**
	 *  This is defined as all empty points having 4 occupied neighbours.
	 *  The idea is to only allow a pass in a search-tree when this condition is met.
	 * @return whether the game is almost finished.
	 */
	public boolean isGameAlmostFinished();
	
	/**
	 * @return wether the mercy-rule applies.
	 */
	public boolean exceedsMercyThreshold();
	
	/**
	 * @return the BoardModel of the MonteCarloAdministration
	 */
	public BoardModel getBoardModel();
	
	/**
	 * @return the checksum used as a hash-code to find equal positions. This
	 * number takes into account any possible ko-captures.
	 */
	public int getPositionalChecksum();
	
	/**
	 * @return whether the current position is a repetition of a previous position.
	 */
	public boolean hasRepetition(int checksum);

	/**
	 * @return the number of moves played until the end of the game.
	 */
	public int getNrSimulatedMoves();
	
	public IntStack getMoveStack();
	
	public byte getColorToMove();
	public void setColorToMove(byte color);
	
	/**
	 * Set a key-value property.
	 * 
	 * @param propertyName
	 * @param propertyValue
	 */
	public void set(String propertyName, String propertyValue);
	
	/**
	 * Get a key-value property.
	 * 
	 * @param propertyName
	 */
	public String get(String propertyName);
	
	/**
	 * Whenever a non-pass is played this value is reset to 0.
	 * So it does not necessarily reflect how many passes are played during a game.

	 * @return the number of consecutive passes played.
	 */
	public int getNrPasses();
	
	/**
	 * This is only used for checking purposes.
	 * 
	 * @return
	 */
	public boolean isConsistent();

	/**
	 * This is a property used in many classes under construction. It is used when a relatively small
	 * change needs to be tested. In that case two identical engines are started, one of which has
	 * this property set to true. It can then test this property and change its behaviour to use some
	 * new code that needs to be tested to see if it's an improvement or not.
	 * 
	 * @param testVersion
	 */
	public void setIsTestVersion(boolean testVersion);
	
	public MoveFactory<GoMove> getMoveFactory();
	
	public byte[] getBlackOwnership();
	public byte[] getWhiteOwnership();
}
