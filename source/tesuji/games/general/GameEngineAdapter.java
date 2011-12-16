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

import tesuji.games.model.BoardModel;

/**
 * This an adapter class of the GameEngine interface that has empty implementations
 * of most (non-critical) methods. It also provides a default implementation
 * of a few methods.
 */
public abstract class GameEngineAdapter<MoveType extends Move>
	implements GameEngine<MoveType>
{
    public abstract String getEngineName();
    public abstract String getEngineVersion();
    public abstract void clearBoard();
    public abstract void playMove(MoveType move);
    public abstract MoveType requestMove(byte color);

    /*
     * (non-Javadoc)
     * @see tesuji.games.general.GameEngine#initialize()
     */
    public void initialize()
    {
    	
    }
    
	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#generateMove(byte)
     */
    public MoveType generateMove(byte color)
    {
		MoveType move = requestMove(color);
    	if (!move.isResignation())
    		playMove(move);
		return move;
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#getFinalScore()
     */
    public String getFinalScore()
    {
		double score = getScore();
		if (score==0.0)
			return "Draw";
		else if (score>0.0)
			return "B+"+score;
		else
			return "W+"+(-score);
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#getMoveFactory()
     */
    public MoveFactory<MoveType> getMoveFactory()
    {
	    // Auto-generated method stub
	    return null;
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#getScore()
     */
    public Double getScore()
    {
	    // Auto-generated method stub
	    return Double.NaN;
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#quit()
     */
    public void quit()
    {
		System.exit(0);
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#requestCandidates(int)
     */
    public Iterable<MoveType> requestCandidates(int n)
    {
	    // Auto-generated method stub
	    return null;
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#set(java.lang.String, java.lang.String)
     */
    public void set(String propertyName, String propertyValue)
    {
	    // Auto-generated method stub 
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#setTimeConstraints(int, int, int)
     */
    public void setTimeConstraints(int mainTime, int byoYomiTime,
                    int nrByoYomiStones)
    {
	    // Auto-generated method stub
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#setTimeLeft(byte, int, int)
     */
    public void setTimeLeft(byte color, int timeRemaining, int nrStonesRemaining)
    {
	    // Auto-generated method stub
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#setup(tesuji.games.general.MoveStack)
     */
    public void setup(Iterable<MoveType> moveList)
    {
	    // Auto-generated method stub
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#setup(java.lang.String[])
     */
    public void setup(String[] diagram)
    {
	    MoveStack<MoveType> moveList = getMoveFactory().parseDiagram(diagram);
	    setup(moveList);
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#takeBack()
     */
    public void takeBack()
    {
	    // Auto-generated method stub
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.MoveGenerator#requestMove(byte, tesuji.core.util.ArrayList)
     */
    public MoveType requestMove(byte color, Iterable<MoveType> alreadyTriedList)
    {
	    // Auto-generated method stub
	    return null;
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.Evaluator#evaluate()
     */
    public void evaluate()
    {
	    // Auto-generated method stub	    
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#getboardModel()
     */
    public BoardModel getBoardModel()
    {
	    // Auto-generated method stub
	    return null;
    }
}
