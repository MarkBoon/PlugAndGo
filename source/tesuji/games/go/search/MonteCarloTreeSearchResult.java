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

package tesuji.games.go.search;

import tesuji.core.util.ArrayStack;
import tesuji.games.general.Move;
import tesuji.games.general.search.SearchResult;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.util.GoArray;

import static tesuji.games.general.ColorConstant.*;

/**
 * A class representing a search-result. 
 */
public class MonteCarloTreeSearchResult<MoveType extends Move>
	implements SearchResult<MoveType>
{
	public static final double MAX_SCORE = 1.0;
	public static final double MIN_SCORE = 0.0;
	public static final double HOPELESS = 0.05;
	public static final double SURE_WIN = 0.99;
	
	public static final float INITIAL_WINS = 7.0f;
	public static final float INITIAL_VISITS = INITIAL_WINS*2.0f;
	
	public static final int OWNERSHIP_MAXIMUM = 63;
	
	private static final double ownershipFactor[] = 
		{
			1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00
		};
	
	/**
	 * The actual number of times a playout lead to a win.
	 */
	protected int _nrWins;
	/**
	 * The number of times a playout was performed.
	 */
	protected int _nrPlayouts;
	
	/**
	 * The number of 'virtual' wins. This is usually based on the AMAF (All Moves As First)
	 * principle and may be weighted depending how deep the move was found in the playout.
	 */
	protected float _nrVirtualWins;
	protected float _nrVirtualPlayouts;
	
	/**
	 * The number of 'virtual' wins. This is usually based on the AMAF (All Moves As First)
	 * principle and may be weighted depending how deep the move was found in the playout.
	 */
	protected int _patternSuccess;
	protected int _patternOccurrence;
	
	/**
	 * Pre-computing the log() when _nrPlayouts changes is cheaper than doing
	 * it each time a UCT value is needed.
	 */
	protected float _logNrPlayouts;	

	protected MoveType _move;
	
	/**
	 * Used to prevent loops when updating values in a tree that has transpositions.
	 */
	protected int _updateTimeStamp;
	
	private static boolean _isTestVersion = false;
	
	/**
	 * The exploration-factor determines how strongly the search will favour exploring
	 * nodes that have been searched little. The ideal value depends very much on other
	 * characteristics of your search. It's static because it's the same for all nodes.
	 */
	private static double _explorationFactor = Math.sqrt(0.2);
	/**
	 * The pattern-factor determines how strongly the search will favour exploring
	 * nodes that have been an urgency-value from a pattern.
	 */
	private static double _patternFactor = 10.0;
	
	private MonteCarloTreeSearchResult<MoveType> _parentResult;
	
	private byte[] _blackOwnership;
	private byte[] _whiteOwnership;
	
	private ArrayStack<MonteCarloTreeSearchResult<MoveType>> _owner;
	
	protected MonteCarloTreeSearchResult()
	{
	}

	/**
	 * This constructor has package scope so that only the factory can create it.
	 * 
	 * @param owner
	 */
	MonteCarloTreeSearchResult(ArrayStack<MonteCarloTreeSearchResult<MoveType>> owner)
	{
		_owner = owner;
	}

	/**
	 * This compares results. This is the method used to decide which move is the best to actually play.
	 * 
	 * @param compare - search-result to compare with
	 * 
	 * @return whether 'this' result is better than the one passed as parameter.
	 */
	public boolean isBetterResultThan(SearchResult<MoveType> compare)
	{
		if (compare==null)
			return true;
		
		MonteCarloTreeSearchResult<MoveType> compareResult = (MonteCarloTreeSearchResult<MoveType>) compare;
		
		assert _move.getColor()==compare.getMove().getColor() : "Can't compare results with a different color";

		if (getWinRatio()>SURE_WIN && compareResult.getWinRatio()>SURE_WIN)
		{
			int xy = ((GoMove)getMove()).getXY();
			int compareXY = ((GoMove)compare.getMove()).getXY();
			if (_move.getColor()==BLACK)
			{
				if (getParentResult().getBlackOwnership()[xy]!=0 && getParentResult().getBlackOwnership()[compareXY]==0)
					return true;
			}
			else
			{
				if (getParentResult().getWhiteOwnership()[xy]!=0 && getParentResult().getWhiteOwnership()[compareXY]==0)
					return true;
			}
			
			int diff = Math.abs(getParentResult().getBlackOwnership()[xy]-getParentResult().getWhiteOwnership()[xy]);
			int compareDiff = Math.abs(getParentResult().getBlackOwnership()[compareXY]-getParentResult().getWhiteOwnership()[compareXY]);
			if (diff<compareDiff)
				return true;
		}
		
		if (getNrPlayouts()>compareResult.getNrPlayouts())
			return true;

		if (getNrPlayouts()==compareResult.getNrPlayouts())
		{
			double value = getWinRatio();
			double compareValue = compareResult.getWinRatio();
			
			assert value!=Double.NaN : "Calculation error for the result-value.";
			assert compareValue!=Double.NaN : "Calculation error for the compare-value.";
			
			if (value>compareValue)
				return true;
			
			if (value==compareValue)
				return getVirtualWinRatio()>((MonteCarloTreeSearchResult<MoveType>)compare).getVirtualWinRatio();
		}
		return false;
	}

	/**
	 * This compares results, but favours the one that should be explored more, instead
	 * of favouring the best win-loss ratio. There are many viable possibilities for this
	 * method. At the moment it's based on David Silver's proposed method of:<br><br>
	 * 
	 *	beta * (virtual-win-ratio + RAVE) + (1-beta) * (win-ratio + UCT)
	 * 
	 * @param compare - search-result to compare with
	 * 
	 * @return whether 'this' result is better than the one passed as parameter.
	 */
	public boolean isBetterVirtualResultThan(MonteCarloTreeSearchResult<MoveType> compare)
	{
		if (compare==null)
			return true;
		
		double virtualResult;
		double compareResult;
		
		virtualResult = computeResult();
		compareResult = compare.computeResult();
		
		assert virtualResult!=Double.NaN : "Calculation error for the virtual result-value.";
		assert compareResult!=Double.NaN : "Calculation error for the virtual compare-value.";

		if (virtualResult>compareResult)
			return true;
		
		// Trust the most visited node more. I don't know if it's all that relevant.
		// I could probably just as easily argue it should be the other way around.
		if (virtualResult==compareResult)
			return (_nrPlayouts > compare.getNrPlayouts());	// TODO: check '<' instead of '>', see which plays better.
		
		return false;
	}
	
	public void increasePlayouts(boolean blackWins)
	{
		byte color = _move.getColor();
		boolean playerWins = (blackWins && color==BLACK) || (!blackWins && color==WHITE);
		
		if (playerWins)
		{
			_nrWins++;
			_nrVirtualWins++;
		}

		_nrPlayouts++;
		_nrVirtualPlayouts++;
		
		// Computing log() is expensive. I see no need to do it each and every time.
		if ((_nrPlayouts&0x7)==0)
			_logNrPlayouts = (float) Math.log(_nrPlayouts);
	}

	public void increaseVirtualPlayouts(double win_weight, double weight)
	{
		_nrVirtualWins += win_weight;
		_nrVirtualPlayouts += weight;
	}

	/**
	 * Increase the playout statistics with the ones from another result.
	 * This is used when encountering transpositions, if they are used.
	 * 
	 * @param source
	 */
	public void increasePlayouts(MonteCarloTreeSearchResult<MoveType> source)
	{
		_nrWins += source._nrWins;		
		_nrPlayouts += source._nrPlayouts;
		_logNrPlayouts = (float)Math.log(_nrPlayouts);
	}
	
	private double computeResult()
	{
		if (_nrPlayouts==0)
			return (getVirtualWinRatio() + getRAVEValue()) * getOwnershipValue() + getUrgencyValue() + getPatternValue();

		double beta = getBeta();
		return beta * ((getVirtualWinRatio()+getRAVEValue()) * getOwnershipValue() + getPatternValue()) + (1.0-beta) * (getWinRatio()+getUCTValue()) + getUrgencyValue();		
	}
	
	/**
	 * Some explanation of what the UCT value means can be found here:
	 * 		http://senseis.xmp.net/?UCT
	 * 
	 * @return the Upper-bound Confidence value of the Tree.
	 */
	public double getUCTValue()
	{
		return _explorationFactor * Math.sqrt( getLogNrParentPlayouts() / (_nrPlayouts+1) );
	}
	
	/**
	 */
	public double getPatternValue()
	{
		return _patternFactor * Math.sqrt( getLogNrParentPlayouts() / (_nrPlayouts+1) );
	}
	
	/**
	 * Compute the Rapid Action Value Estimate based on the virtual playout information
	 * gathered by an AMAF procedure (All Moves As First).
	 * 
	 * At the moment this formula is the same as the UCT formula, using the same exploration
	 * factor. This may need refining in te future.
	 * 
	 * @return Rapid Action Value Estimate
	 */
	public double getRAVEValue()
	{
		return _explorationFactor * Math.sqrt( getLogNrParentPlayouts() / (_nrVirtualPlayouts+1) );
	}

	public double getUrgencyValue()
	{
		if (getIsTestVersion())
			return 0.0;
		
		return (_patternFactor / ((double)((GoMove)getMove()).getUrgency())) / (_nrPlayouts+1);
	}
	
	/**
	 * Beta is a value that moves towards 1.0 based on the virtual-playouts value of the parent.
	 * This tends results to be weighted more heavily towards UCT and less towards RAVE the more
	 * a node has been explored.
	 * 
	 * @return beta
	 */
	public double getBeta()
	{
		return 1.0 - getLogNrParentPlayouts() / 20;
	}
	
	/**
	 * @return the virtual win-ratio based on AMAF information collected.
	 */
	public double getVirtualWinRatio()
	{
		if (_nrVirtualPlayouts == 0.0)
			return 0.0;

		return (double)_nrVirtualWins / (double)_nrVirtualPlayouts;
	}
	
	/**
	 */
	public double getPatternRatio()
	{
		if (_patternOccurrence == 0.0)
			return 0.0;

		return (double)_patternSuccess / (double)_patternOccurrence;
	}
	
	/**
	 * @return the number of wins (for the color of the player of the move in this node) divided by the number of visits (called playouts)
	 */
	public double getWinRatio()
	{
		if (_nrPlayouts == 0)
			return 0.0;

		return (double)_nrWins / (double)_nrPlayouts;
	}
	
	public double getOwnershipValue()
	{
//		if (!getIsTestVersion())
			return 1.0;

/*		if (_parentResult==null)
			return 1.0;

		if (_parentResult.getNrPlayouts()<OWNERSHIP_MAXIMUM)
			return 1.0;
				
		int own;
		int xy = ((GoMove)_move).getXY();
		if (_move.getColor()==BLACK)
			own = _parentResult.getBlackOwnership()[xy];
		else
			own = _parentResult.getWhiteOwnership()[xy];
		int index = own/8;
		return ownershipFactor[index];*/
		
//		if (_parentResult.getParentResult()==null)
//		return 1.0;
	
//		int current;
//		int previous;
//		int xy = ((GoMove)_move).getXY();
//		if (_move.getColor()==BLACK)
//		{
//			current = _parentResult.getBlackOwnership()[xy];
//			previous = _parentResult.getParentResult().getBlackOwnership()[xy];
//		}
//		else
//		{
//			current = _parentResult.getWhiteOwnership()[xy];
//			previous = _parentResult.getParentResult().getWhiteOwnership()[xy];
//		}
//		double diff =  (double) Math.abs(current-previous) / 256.0;
//		
//		int own;
//		int other;
//		if (_move.getColor()==BLACK)
//		{
//			own = _parentResult.getBlackOwnership()[xy];
//			other = _parentResult.getWhiteOwnership()[xy];
//		}
//		else
//		{
//			other = _parentResult.getBlackOwnership()[xy];
//			own = _parentResult.getWhiteOwnership()[xy];
//		}
//		if (own==0)
//			return ownershipFactor[0];
//
//		double ownership = ((double) own / (double)( own + other))-0.0001;
//		return ownershipFactor[(int)(ownership * ownershipFactor.length)] * diff;
	}
	
	/**
	 * @return whether the score is so bad it might just as well resign.
	 */
	public boolean isHopeless()
	{
		return getWinRatio()<HOPELESS;
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.core.util.FlyWeight#recycle()
	 */
    public void recycle()
	{
		if (_move!=null)
			_move.recycle();
		_move = null;
		
		_parentResult = null;
		_nrPlayouts = 0;
		_nrWins = 0;
		_nrVirtualPlayouts = 0.0f;
		_nrVirtualWins = 0.0f;
		_patternSuccess = 0;
		_patternOccurrence = 0;
		_updateTimeStamp = -1;
		
		_blackOwnership = null; // Maybe need to recycle at some point.
		_whiteOwnership = null;

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
    		" value = "+computeResult()+" RAVE="+getRAVEValue()+" virtual-ratio="+getVirtualWinRatio()+" uct="+getUCTValue()+" ratio="+getWinRatio();
    }
    
    // Some setter /  getters below.

    public MoveType getMove()
    {
    	return _move;
    }
    
    public void setMove(MoveType move)
    {
    	if (_move!=null)
    		_move.recycle();

    	_move = move;
    	
    	if (getIsTestVersion())
    	{
    		_nrVirtualPlayouts = INITIAL_VISITS;
    		_nrVirtualWins = INITIAL_WINS;

    		GoMove goMove = (GoMove)move;
    		double urgency = goMove.getUrgency()+2;
    		double visits = goMove.getVisits();
    		double wins = goMove.getWins();

    		if (visits>0 && urgency<1000)
    		{
        		double value = (wins * (wins/visits)) / Math.log(urgency);
    			_nrVirtualPlayouts = (float)value;
    			_nrVirtualWins = (float)value;
    		}
    	}
    }

    public int getNrWins()
    {
    	return _nrWins;
    }

    public void setNrWins(int nrWins)
    {
    	_nrWins = nrWins;
    }

    public int getNrPlayouts()
    {
    	return _nrPlayouts;
    }

    public double getLogNrPlayouts()
    {
    	return _logNrPlayouts;
    }

    public double getLogNrParentPlayouts()
    {
    	return _parentResult.getLogNrPlayouts();
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

	public MonteCarloTreeSearchResult<MoveType> getParentResult()
    {
    	return _parentResult;
    }

	public void setParentResult(MonteCarloTreeSearchResult<MoveType> result)
    {
    	_parentResult = result;
    }

	public boolean getIsTestVersion()
	{
		return _isTestVersion;
	}

	public void setIsTestVersion(boolean isTestVersion)
	{
		_isTestVersion = isTestVersion;
	}

	public byte[] getBlackOwnership()
	{
		if (_blackOwnership==null)
			_blackOwnership = GoArray.createBytes();
		return _blackOwnership;
	}

	public byte[] getWhiteOwnership()
	{
		if (_whiteOwnership==null)
			_whiteOwnership = GoArray.createBytes();
		return _whiteOwnership;
	}
	
	public void addOwnership(byte[] black, byte[] white)
	{
		if (_blackOwnership==null)
		{
			_blackOwnership = GoArray.createBytes();
			_whiteOwnership = GoArray.createBytes();
		}
		for (int i=GoArray.FIRST; i<=GoArray.LAST; i++)
		{
			_blackOwnership[i] += black[i];
			_whiteOwnership[i] += white[i];
		}
	}

	public int getPatternSuccess()
    {
    	return _patternSuccess;
    }

	public void setPatternSuccess(int patternSuccess)
    {
    	_patternSuccess = patternSuccess;
    }

	public int getPatternOccurrence()
    {
    	return _patternOccurrence;
    }

	public void setPatternOccurrence(int patternOccurrence)
    {
    	_patternOccurrence = patternOccurrence;
    }
}
