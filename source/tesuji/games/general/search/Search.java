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

package tesuji.games.general.search;

import tesuji.core.util.ArrayStack;
import tesuji.games.general.Move;
import tesuji.games.general.TreeNode;

/**
 * This is a general interface to define a search module for a game-playing engine.
 */
public interface Search<MoveType extends Move>
{
	public void setSearchProperties(SearchProperties properties);
	public SearchProperties getSearchProperties();
	
	/**
	 * Set the amount of time the search has to run.
	 * 
	 * @param seconds
	 */
	public void setSecondsPerMove(int seconds);
	/**
	 * Set the number of nodes to search. This overrides the time it can run in the method above.
	 * 
	 * @param minimum
	 */
	public void setMinimumNrNodes(int minimum);
	/**
	 * Set the number of processors that need to be used to search simultaneously, if supported.
	 * 
	 * @param nrProcessors
	 */
	public void setNrProcessors(int nrProcessors);

	/**
	 * This is a property used in many classes under construction. It is used when a relatively small
	 * change needs to be tested. In that case two identical engines are started, one of which has
	 * this property set to true. It can then test this property and change its behaviour to use some
	 * new code that needs to be tested to see if it's an improvement or not.
	 * 
	 * @param testVersion
	 */
	public void setIsTestVersion(boolean testVersion);
	
	/**
	 * Do a search for a color as the next to move and return the best move in a SearchResult object
	 * 
	 * @param startColor - the color to move first in the current position
	 * 
	 * @return SearchResult object containing (among other things) the best move resulted from the search.
	 * 
	 * @throws Exception Any exception thrown should be treated as an unrecoverable error.
	 */
	public SearchResult<MoveType> doSearch(byte startColor) throws Exception;
	
	// These methods are used to keep the state of the Search object synchronized with the actual game.
	public void playMove(MoveType move);
	public void takeBack();
	public void clear();

	// These methods are used to query the resulting search-tree.
	public void getBestMovePath(ArrayStack<MoveType> moveList);
	public TreeNode<? extends SearchResult<MoveType>> getRootNode();

}
