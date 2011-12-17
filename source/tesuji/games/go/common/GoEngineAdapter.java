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

import tesuji.core.util.ArrayList;
import tesuji.games.general.GameEngineAdapter;
import tesuji.games.general.MoveStack;
import tesuji.games.go.util.GoGameProperties;

/**
 * This implements all methods required by the GoEngine interface. A few of them have a default implementation
 * but most of them are empty.
 */
public abstract class GoEngineAdapter
	extends GameEngineAdapter<GoMove>
	implements GoEngine
{
	private GoGameProperties _gameProperties = new GoGameProperties();
	
	private String jar;
	
	/**
	 * @return A GoGameProperties containing game-parameters.
	 */
	public GoGameProperties getGameProperties()
	{
		return _gameProperties;
	}
	
	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#getMoveFactory()
     */
    @Override
	public GoMoveFactory getMoveFactory()
    {
	    return GoMoveFactory.getSingleton();
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#requestCandidates(int)
     */
    @Override
	public ArrayList<GoMove> requestCandidates(int n)
    {
	    return null;
    }

    @Override
	public void set(String propertyName, String propertyValue)
    {
    	_gameProperties.setProperty(propertyName, propertyValue);
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#setTimeConstraints(int, int, int)
     */
    @Override
	public void setTimeConstraints(int mainTime, int byoYomiTime,
                    int nrByoYomiStones)
    {
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#setTimeLeft(byte, int, int)
     */
    @Override
	public void setTimeLeft(byte color, int timeRemaining, int nrStonesRemaining)
    {
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#setup(tesuji.games.general.MoveStack)
     */
    public void setup(MoveStack<GoMove> moveList)
    {
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#setup(java.lang.String[])
     */
    @Override
	public void setup(String[] diagram)
    {
    	set(GoGameProperties.BOARDSIZE,""+diagram.length);
    	clearBoard();
 	    super.setup(diagram);
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#takeBack()
     */
    @Override
	public void takeBack()
    {
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.MoveGenerator#requestMove(byte, tesuji.core.util.ArrayList)
     */
    @Override
	public GoMove requestMove(byte color, Iterable<GoMove> alreadyTriedList)
    {	    // TODO Auto-generated method stub
	    return null;
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.Evaluator#evaluate()
     */
    @Override
	public void evaluate()
    {
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#getJar()
     */
	public String getJar()
	{
		return jar;
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#setJar(java.lang.String)
	 */
	public void setJar(String jarName)
	{
		jar = jarName;
	}
}
