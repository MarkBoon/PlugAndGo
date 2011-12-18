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

import static tesuji.games.general.ColorConstant.*;

/**
 * A class representing a search-result. 
 */
public class UctRaveSearchResult
	implements SearchResult<GoMove>
{
	public static final double MAX_SCORE = 1.0;
	public static final double MIN_SCORE = 0.0;
	public static final double HOPELESS = 0.05;
	
	protected int _nrWins;
	protected int _nrPlayouts;
	
	protected double _nrVirtualWins;
	protected double _nrVirtualPlayouts;
	
	/**
	 * Pre-computing the log() when _nrPlayouts changes is cheaper than doing
	 * it each time a UCT value is needed. Note that it's constructed such
	 * that with this calculation it automatically updates the LogNrParentPlayouts
	 * of all the children.
	 */
	protected final MutableDouble _logNrPlayouts = new MutableDouble();
	protected MutableDouble _logNrParentPlayouts;
	
	protected GoMove _move;
	protected byte _colorToMove;
	protected int _updateTimeStamp;
	
	private double _explorationFactor = Math.sqrt(0.2);
	
	private SynchronizedArrayStack<UctRaveSearchResult> _owner;
	
	protected UctRaveSearchResult()
	{
	}

	/**
	 * This constructor has package scope so that only the factory can create it.
	 * 
	 * @param owner
	 */
	UctRaveSearchResult(SynchronizedArrayStack<UctRaveSearchResult> owner)
	{
		_owner = owner;
	}

	/**
	 * This compares results.
	 * 
	 * @param compare - search-result to compare with
	 * 
	 * @return whether 'this' result is better than the one passed as parameter.
	 */
	public boolean isBetterResultThan(SearchResult compare)
	{
		if (compare==null)
			return true;
		
		UctRaveSearchResult compareResult = (UctRaveSearchResult) compare;
		
		assert _move.getColor()==compare.getMove().getColor() : "Can't compare results with a different color";

		if (getNrPlayouts()>compareResult.getNrPlayouts())
			return true;

		if (getNrPlayouts()==compareResult.getNrPlayouts())
		{
			double value = getWinRatio();
			double compareValue = compareResult.getWinRatio();
			
			if (value>compareValue)
				return true;
			
			if (value==compareValue)
				return getVirtualWinRatio()>((UctRaveSearchResult)compare).getVirtualWinRatio();
		}
		return false;
	}

	/**
	 * This compares results, but favours the one that should be explored more, instead
	 * of favouring the best win-loss ratio.
	 * 
	 * @param compare - search-result to compare with
	 * 
	 * @return whether 'this' result is better than the one passed as parameter.
	 */
	public boolean isBetterVirtualResultThan(UctRaveSearchResult compare)
	{
		if (compare==null)
			return true;
		
		double virtualResult;
		double compareResult;

		virtualResult = getVirtualWinRatio()+getUCTValue();
		compareResult = compare.getVirtualWinRatio()+compare.getUCTValue();
		
		if (virtualResult>compareResult)
			return true;
		
		// Trust the most visited node more.
		if (virtualResult==compareResult)
			return (_nrPlayouts > compare.getNrPlayouts());
		
		return false;
	}
	
	public void increasePlayouts(boolean blackWins)
	{
		byte color = (_move==null? opposite(getColorToMove()) : _move.getColor()); // The root may not have a move.
		boolean playerWins = (blackWins && color==BLACK) || (!blackWins && color==WHITE);
		
		if (playerWins)
			_nrWins++;

		_nrPlayouts++;
		_logNrPlayouts.setValue(Math.log(_nrPlayouts));
	}

	public void increaseVirtualPlayouts(double win_weight, double weight)
	{
		_nrVirtualWins += win_weight;
		_nrVirtualPlayouts += weight;
	}

	public void increasePlayouts(UctRaveSearchResult source)
	{
		_nrPlayouts += source._nrPlayouts;
		_nrWins += source._nrWins;		
		_logNrPlayouts.setValue(Math.log(_nrPlayouts));
	}
	
	public double getUCTValue()
	{
		return _explorationFactor * Math.sqrt( getLogNrParentPlayouts() / (_nrPlayouts+1) );
	}
	
	public double getVirtualWinRatio()
	{
		if (_nrVirtualPlayouts == 0)
			return 0.0;
		return (_nrVirtualWins / _nrVirtualPlayouts);

//		if (_nrPlayouts == 0 && _nrVirtualPlayouts == 0)
//			return 0.0;
//		if (_nrPlayouts == 0)
//			return (_nrVirtualWins / _nrVirtualPlayouts);
//		
//		return ( (_nrWins*2 + _nrVirtualWins) / (_nrPlayouts*2 + _nrVirtualPlayouts));
	}
	
	public double getWinRatio()
	{
		if (_nrPlayouts == 0)
			return 0.0;

		return (double)_nrWins / (double)_nrPlayouts;
	}
	
	/**
	 * @return whether the score is so bad it might just as well resign.
	 */
	public boolean isHopeless()
	{
		return getWinRatio()<HOPELESS;
	}

    public void recycle()
	{
		if (_move!=null)
			_move.recycle();
		_move = null;
		
		_logNrParentPlayouts = null;
		_nrPlayouts = 0;
		_nrWins = 0;
		_nrVirtualPlayouts = 0.0;
		_nrVirtualWins = 0.0;
		_updateTimeStamp = -1;

		_owner.push(this);
	}
	
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	return _move.toSGF()+" wins="+_nrWins+" runs="+_nrPlayouts+
    		" value = "+(getWinRatio()+getUCTValue()+getVirtualWinRatio())+" virtual-ratio="+getVirtualWinRatio()+" uct="+getUCTValue()+" ratio="+getWinRatio();
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
     * @return the log of the nrPlayouts
     */
    public MutableDouble getLogNrPlayouts()
    {
    	return _logNrPlayouts;
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

	public int getUpdateTimeStamp()
	{
		return _updateTimeStamp;
	}

	public void setUpdateTimeStamp(int timeStamp)
	{
		_updateTimeStamp = timeStamp;
	}
}
