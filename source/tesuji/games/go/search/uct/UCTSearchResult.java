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

package tesuji.games.go.search.uct;

import tesuji.core.util.MutableDouble;
import tesuji.core.util.SynchronizedArrayStack;
import tesuji.games.general.Move;
import tesuji.games.general.search.SearchResult;

import tesuji.games.go.common.GoMove;
import tesuji.games.go.monte_carlo.MCState;

import static tesuji.games.general.ColorConstant.*;

/**
 * 
 */
public class UCTSearchResult
	implements SearchResult<GoMove>
{
	private static final double HOPELESS = 0.05;
	
	protected int _nrWins;
	protected int _nrPlayouts;
	protected final MutableDouble _logNrPlayouts = new MutableDouble();
	protected MutableDouble _logNrParentPlayouts;
	protected GoMove _move;
	protected boolean _completeNode;
	protected MCState _searchState;
	protected MCState _parentState;
	protected byte _colorToMove;
	protected int _updateTimeStamp;
	
	private double _explorationFactor = 1.0;
	
	private SynchronizedArrayStack<UCTSearchResult> _owner;
	
	protected UCTSearchResult()
	{
	}

	public UCTSearchResult(SynchronizedArrayStack<UCTSearchResult> owner)
	{
		_owner = owner;
	}

	public boolean isBetterResultThan(SearchResult compare)
	{
		if (compare==null)
			return true;
		
		UCTSearchResult compareResult = (UCTSearchResult) compare;
		
		assert _move.getColor()==compare.getMove().getColor();
		
		double value = getWinRatio(_move.getColor());
		double compareValue = compareResult.getWinRatio(_move.getColor());
		
		if (value>compareValue)
			return true;
		
		if (value==compareValue)
			return (_nrPlayouts>compareResult.getNrPlayouts());
		
		return false;
	}

	public boolean isBetterVirtualResultThan(UCTSearchResult compare)
	{
		if (compare==null)
			return true;
		
		double uct = getWinRatio(_move.getColor())+getUCTValue();
		double compareUct = compare.getWinRatio(_move.getColor())+compare.getUCTValue();
		
		if (uct>compareUct)
			return true;
		
		if (uct==compareUct)
			return (_nrPlayouts>compare.getNrPlayouts());
		
		return false;
	}
	
	public boolean isHopeless()
	{
		return getWinRatio(_move.getColor())<HOPELESS;
	}

	public double getUCTValue()
	{
		if (_nrPlayouts == 0)
			return Double.POSITIVE_INFINITY;
		
//		double urgency = (double)(_parentState.getUrgency(_move) - _nrPlayouts);
//		if (urgency<0)
//			urgency = 0;
		double urgency = 0;
		
		return urgency + _explorationFactor * Math.sqrt(2.0 * getLogNrParentPlayouts() / (10.0*_nrPlayouts));
	}
	
	public double getWinRatio(byte color)
	{
		if (_nrPlayouts == 0)
			return Double.POSITIVE_INFINITY;

		if (color==BLACK)
			return (double)_nrWins / (double)_nrPlayouts;
		else
			return (double)(_nrPlayouts -_nrWins) / (double)_nrPlayouts;
	}
	
	/**
     * @return the move
     */
    public GoMove getMove()
    {
    	return _move;
    }
	/**
     * @param move the move to set
     */
    public void setMove(GoMove move)
    {
    	if (_move!=null)
    		_move.recycle();

    	_move = move;
    }

    public void recycle()
	{
		if (_move!=null)
			_move.recycle();
		_move = null;
		
		if (_searchState!=null)
			_searchState.recycle();
		_searchState = null;
		
		_logNrParentPlayouts = null;
		_nrPlayouts = 0;
		_nrWins = 0;
		_completeNode = false;
		_updateTimeStamp = -1;

		_owner.push(this);
	}
	
	public void increasePlayouts(boolean win)
	{
		if (win)
			_nrWins++;
		_nrPlayouts++;
		_logNrPlayouts.setValue(Math.log(_nrPlayouts));
	}

	public void increasePlayouts(UCTSearchResult source)
	{
		_nrPlayouts += source._nrPlayouts;
		_nrWins += source._nrWins;
		_logNrPlayouts.setValue(Math.log(_nrPlayouts));
	}
	
	/**
     * @return the nrWins
     */
    public int getNrWins()
    {
    	return _nrWins;
    }

	/**
     * @param nrWins the nrWins to set
     */
    public void setNrWins(int nrWins)
    {
    	_nrWins = nrWins;
    }

	/**
     * @return the nrPlayouts
     */
    public int getNrPlayouts()
    {
    	return _nrPlayouts;
    }

	/**
     * @return the nrPlayouts
     */
    public MutableDouble getLogNrPlayouts()
    {
    	return _logNrPlayouts;
    }

	/**
     * @return the completeNode
     */
    public boolean isCompleteNode()
    {
    	return _completeNode;
    }

	/**
     * @param completeNode the completeNode to set
     */
    public void setCompleteNode(boolean completeNode)
    {
    	_completeNode = completeNode;
    }

	/**
     * @return the nrParentPlayouts
     */
    public double getLogNrParentPlayouts()
    {
    	return _logNrParentPlayouts.getValue();
    }
    
    public void setLogNrParentPlayouts(MutableDouble logNrParentPlayouts)
    {
    	_logNrParentPlayouts = logNrParentPlayouts;
    }

	/**
     * @return the state of the MC playout
     */
    public MCState getSearchState()
    {
    	return _searchState;
    }

	/**
     * @param the state of the MC playout
     */
    public void setSearchState(MCState searchState)
    {
    	_searchState = searchState;
    }
    
    @Override
	public String toString()
    {
    	return _move.toSGF()+" wins="+_nrWins+" runs="+_nrPlayouts+
    		" value = "+(getWinRatio(_move.getColor())+getUCTValue())+" uct="+getUCTValue()+" ratio="+getWinRatio(_move.getColor());
    }

	public byte getColorToMove()
	{
		return _colorToMove;
	}

	public void setColorToMove(byte toMove)
	{
		_colorToMove = toMove;
	}
	
	public void setExplorationFactor(double explorationValue)
	{
		_explorationFactor = explorationValue;
	}

	public MCState getParentState()
	{
		return _parentState;
	}

	public void setParentState(MCState state)
	{
		_parentState = state;
	}

	public int getUpdateTimeStamp()
	{
		return _updateTimeStamp;
	}

	public void setUpdateTimeStamp(int timeStamp)
	{
		_updateTimeStamp = timeStamp;
	}
}
