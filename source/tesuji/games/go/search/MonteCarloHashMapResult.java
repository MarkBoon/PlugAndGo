package tesuji.games.go.search;

import static tesuji.games.general.ColorConstant.BLACK;
import static tesuji.games.general.ColorConstant.WHITE;
import tesuji.games.general.ColorConstant;
import tesuji.games.general.search.SearchResult;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.PointSet;
import tesuji.games.go.util.PointSetFactory;

public class MonteCarloHashMapResult
	implements SearchResult<GoMove>
{
	/**
	 * The exploration-factor determines how strongly the search will favour exploring
	 * nodes that have been searched little. The ideal value depends very much on other
	 * characteristics of your search. It's static because it's the same for all nodes.
	 */
	private static final double _explorationFactor = Math.sqrt(0.2);

	private GoMove		_move;
	private int			_playouts;
//	private long		_checksum;
	private PointSet	_emptyPoints;
//	private int[]		_checksums;
	private int[]		_wins;
	private int[]		_nrPlayouts;
	private float[]		_virtualWins;
	private float[]		_virtualPlayouts;
	private double		_logNrPlayouts;
	private double		_beta;
	
	MonteCarloHashMapResult()
	{
		_emptyPoints = PointSetFactory.createPointSet();
//		_checksums = GoArray.createIntegers();
		_wins = GoArray.createIntegers();
		_nrPlayouts = GoArray.createIntegers();
		_virtualWins = GoArray.createFloats();
		_virtualPlayouts = GoArray.createFloats();
	}
	
	public void setPointSet(PointSet set)
	{
		_emptyPoints.copyFrom(set);
	}
	
	void init()
	{
		_emptyPoints.clear();
//		GoArray.clear(_checksums);
		GoArray.clear(_wins);
		GoArray.clear(_nrPlayouts);
		GoArray.clear(_virtualWins);
		GoArray.clear(_virtualPlayouts);
	}

	@Override
    public void recycle()
    {
	    // TODO Auto-generated method stub
	    
    }
	@Override
    public GoMove getMove()
    {
	    // TODO Auto-generated method stub
	    return _move;
    }
	
	@Override
    public void setMove(GoMove move)
    {
	    _move = move;
    }
	
	public PointSet getEmptyPoints()
	{
		return _emptyPoints;
	}
	
	@Override
    public boolean isBetterResultThan(SearchResult<GoMove> compare)
    {
	    // TODO Auto-generated method stub
	    return false;
    }
	@Override
    public boolean isHopeless()
    {
	    // TODO Auto-generated method stub
	    return false;
    }

//	public void setChecksum(long checksum)
//    {
//	    _checksum = checksum;
//    }
//
//	public long getChecksum()
//    {
//	    return _checksum;
//    }

	public void setPlayouts(int playouts)
    {
	    _playouts = playouts;
    }

	public int getPlayouts()
    {
	    return _playouts;
    }
	
	public int getPlayouts(int xy)
	{
		return _nrPlayouts[xy];
	}
	
	/**
	 * @return the number of wins (for the color of the player of the move in this node) divided by the number of visits (called playouts)
	 */
	private double getWinRatio(int xy)
	{
		if (_nrPlayouts[xy] == 0)
			return 0.0;

		return (double)_wins[xy] / (double)_nrPlayouts[xy];
	}
	
	/**
	 * Some explanation of what the UCT value means can be found here:
	 * 		http://senseis.xmp.net/?UCT
	 * 
	 * @return the Upper-bound Confidence value of the Tree.
	 */
	public double getUCTValue(int xy)
	{
		return _explorationFactor * Math.sqrt( _logNrPlayouts / (_nrPlayouts[xy]+1) );
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
	public double getRAVEValue(int xy)
	{
		return _explorationFactor * Math.sqrt( _logNrPlayouts / (_virtualPlayouts[xy]+1) );
	}

	/**
	 * Beta is a value that moves towards 1.0 based on the virtual-playouts value of the parent.
	 * This tends results to be weighted more heavily towards UCT and less towards RAVE the more
	 * a node has been explored.
	 * 
	 * @return beta
	 */
	private double getBeta()
	{
		return 1.0 - _logNrPlayouts / 20;
	}
	
	/**
	 * @return the virtual win-ratio based on AMAF information collected.
	 */
	private  double getVirtualWinRatio(int xy)
	{
		if (_virtualPlayouts[xy] == 0.0)
			return 0.0;

		return (double)_virtualWins[xy] / (double)_virtualPlayouts[xy];
	}
	
	private double computeResult(int xy)
	{
		if (_nrPlayouts[xy]==0)
			return (getVirtualWinRatio(xy) + getRAVEValue(xy));

		return _beta * ((getVirtualWinRatio(xy)+getRAVEValue(xy))) + (1.0-_beta) * (getWinRatio(xy)+getUCTValue(xy));		
	}

	public int getBestVirtualMove()
	{
		int bestMove = GoConstant.PASS;
		for (int i=_emptyPoints.getSize(); --i>0;)
		{
			int next = _emptyPoints.get(i);
			if (isBetterVirtualMove(next,bestMove))
				bestMove = next;
		}
		return bestMove;
	}
	
	private boolean isBetterVirtualMove(int xy1, int xy2)
	{
		double virtualResult;
		double compareResult;
		
		virtualResult = computeResult(xy1);
		compareResult = computeResult(xy2);
		
		assert virtualResult!=Double.NaN : "Calculation error for the virtual result-value.";
		assert compareResult!=Double.NaN : "Calculation error for the virtual compare-value.";

		if (virtualResult>compareResult)
			return true;
		
		// Trust the most visited node more. I don't know if it's all that relevant.
		// I could probably just as easily argue it should be the other way around.
		if (virtualResult==compareResult)
			return (_nrPlayouts[xy1] > _nrPlayouts[xy1]);	// TODO: check '<' instead of '>', see which plays better.
		
		return false;
		
	}

	public void increasePlayouts(int xy, boolean blackWins)
	{
		
		byte color = ColorConstant.opposite(_move.getColor());
		boolean playerWins = (blackWins && color==BLACK) || (!blackWins && color==WHITE);
		
		_playouts++;
		if (playerWins)
		{
			_wins[xy]++;
			_virtualWins[xy]++;
		}

		_nrPlayouts[xy]++;
		_virtualPlayouts[xy]++;
		
		// Computing log() is expensive. I see no need to do it each and every time.
// TODO - Needs testing		if ((_playouts&0x7)==0)
			_logNrPlayouts = Math.log(_playouts);
			_beta = getBeta();
	}

	public void increaseVirtualPlayouts(int xy, double win_weight, double weight)
	{
		_virtualWins[xy] += win_weight;
		_virtualPlayouts[xy] += weight;
	}
}
