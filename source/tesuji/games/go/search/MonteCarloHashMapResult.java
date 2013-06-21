package tesuji.games.go.search;

import static tesuji.games.general.ColorConstant.BLACK;
import static tesuji.games.general.ColorConstant.WHITE;
import tesuji.core.util.ArrayStack;
import tesuji.games.general.ColorConstant;
import tesuji.games.general.search.SearchResult;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.PointSet;
import tesuji.games.go.util.PointSetFactory;

public class MonteCarloHashMapResult
	implements SearchResult<GoMove>
{
	public static final double MAX_SCORE = 1.0;
	public static final double MIN_SCORE = 0.0;
	public static final double HOPELESS = 0.05;
	public static final double SURE_WIN = 0.99;

	/**
	 * The exploration-factor determines how strongly the search will favour exploring
	 * nodes that have been searched little. The ideal value depends very much on other
	 * characteristics of your search. It's static because it's the same for all nodes.
	 */
	private static final double _explorationFactor = Math.sqrt(0.2);

	private GoMove		_move;
	private int			_totalPlayouts;
	private int[]		_wins;
	private int	[]		_playouts;
	private PointSet	_emptyPoints;
	private float[]		_virtualWins;
	private float[]		_virtualPlayouts;
	private double		_logNrPlayouts;
	private double		_beta;
	
	private int _boardSize;
	
	private ArrayStack<MonteCarloHashMapResult> _owner;

	MonteCarloHashMapResult(ArrayStack<MonteCarloHashMapResult> owner)
	{
		this();
		_owner = owner;
	}
	
	MonteCarloHashMapResult()
	{
		_emptyPoints = PointSetFactory.createPointSet();
		_wins = GoArray.createIntegers();
		_playouts = GoArray.createIntegers();
		_virtualWins = GoArray.createFloats();
		_virtualPlayouts = GoArray.createFloats();
	}
	
	public void setPointSet(PointSet set, MonteCarloPluginAdministration administration)
	{
		_boardSize = administration.getBoardSize();
		for (int i=0; i<set.getSize(); i++)
		{
			int xy = set.get(i);
			if (administration.isLegal(xy) && !administration.isVerboten(xy))
			{
				_emptyPoints.add(xy);
				_virtualPlayouts[xy] = 1;
				_virtualWins[xy] = 1;
			}
		}
		if (administration.isGameAlmostFinished())
		{
			_virtualPlayouts[GoConstant.PASS] = 1;
			_virtualWins[GoConstant.PASS] = 1;
			_emptyPoints.add(GoConstant.PASS);
		}
	}
	
	void init()
	{
		_logNrPlayouts = 0.0;
		_beta = 0.0;
		
		_emptyPoints.clear();
		GoArray.clear(_playouts);
		GoArray.clear(_wins);
		GoArray.clear(_virtualWins);
		GoArray.clear(_virtualPlayouts);
	}

	@Override
    public void recycle()
    {
		init();
		if (_move!=null)
			_move.recycle();
		_move = null;

		_owner.push(this);
    }

	@Override
    public GoMove getMove()
    {
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
		return false;
		/*
		if (compare==null)
			return true;
		
		MonteCarloHashMapResult compareResult = (MonteCarloHashMapResult) compare;
		
		assert _move.getColor()==compare.getMove().getColor() : "Can't compare results with a different color";

//		if (getWinRatio()>SURE_WIN && compareResult.getWinRatio()>SURE_WIN)
//		{
//			int xy = ((GoMove)getMove()).getXY();
//			int compareXY = ((GoMove)compare.getMove()).getXY();
//			if (_move.getColor()==BLACK)
//			{
//				if (getParentResult().getBlackOwnership()[xy]!=0 && getParentResult().getBlackOwnership()[compareXY]==0)
//					return true;
//			}
//			else
//			{
//				if (getParentResult().getWhiteOwnership()[xy]!=0 && getParentResult().getWhiteOwnership()[compareXY]==0)
//					return true;
//			}
//			
//			int diff = Math.abs(getParentResult().getBlackOwnership()[xy]-getParentResult().getWhiteOwnership()[xy]);
//			int compareDiff = Math.abs(getParentResult().getBlackOwnership()[compareXY]-getParentResult().getWhiteOwnership()[compareXY]);
//			if (diff<compareDiff)
//				return true;
//		}
		
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
				return getVirtualWinRatio()>compareResult.getVirtualWinRatio();
		}
		return false;
		*/
    }

	@Override
    public boolean isHopeless()
    {
		return false;
//		return getWinRatio(xy)<HOPELESS;
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

//	public void setPlayouts(int playouts)
//    {
//	    _playouts = playouts;
//    }

	public int getPlayouts()
    {
	    return _totalPlayouts;
    }
	
	/**
	 * @return the number of wins (for the color of the player of the move in this node) divided by the number of visits (called playouts)
	 */
	public double getWinRatio(int xy)
	{
		if (_playouts[xy] == 0)
			return 0.0;

		return (double)_wins[xy] / (double)_playouts[xy];
	}
	
	/**
	 * Some explanation of what the UCT value means can be found here:
	 * 		http://senseis.xmp.net/?UCT
	 * 
	 * @return the Upper-bound Confidence value of the Tree.
	 */
	public double getUCTValue(int xy)
	{
		return _explorationFactor * Math.sqrt( _logNrPlayouts / (_playouts[xy]+1) );
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
	
	public int getBestMove()
	{
		int bestMove = GoConstant.PASS;
		int bestResult = -1;
		for (int i=_emptyPoints.getSize(); --i>0;)
		{
			int next = _emptyPoints.get(i);
			if (_playouts[next]>bestResult)
			{
				bestMove = next;
				bestResult = _playouts[next];
			}
			else if (_playouts[next]==bestResult && _wins[next]>_wins[bestMove])
				bestMove = next;
		}
		return bestMove;
	}
	
	private double computeResult(int xy)
	{
		if (_playouts[xy]==0)
			return (getVirtualWinRatio(xy) + getRAVEValue(xy));

		return _beta * (getVirtualWinRatio(xy)+getRAVEValue(xy)) + (1.0-_beta) * (getWinRatio(xy)+getUCTValue(xy));		
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
			return (_playouts[xy1] > _playouts[xy2]);	// TODO: check '<' instead of '>', see which plays better.
		
		return false;
		
	}

	public void increasePlayouts(int xy)
	{
		_totalPlayouts++;
		_playouts[xy]++;
		_virtualPlayouts[xy]++;
		
		// Computing log() is expensive. I see no need to do it each and every time.
		if ((_totalPlayouts&0x7)==0)
			_logNrPlayouts = Math.log(_totalPlayouts);
		_beta = getBeta();
	}

	public void increaseWins(int xy, boolean blackWins)
	{		
		byte color = ColorConstant.opposite(_move.getColor());
		boolean playerWins = (blackWins && color==BLACK) || (!blackWins && color==WHITE);
		
		if (playerWins)
		{
			_wins[xy]++;
			_virtualWins[xy]++;
		}
	}

	public void increaseVirtualPlayouts(int xy, double win_weight, double weight)
	{
		_virtualWins[xy] += win_weight;
		_virtualPlayouts[xy] += weight;
	}

	public String toString()
	{
		StringBuilder out = new StringBuilder();
		
		out.append("\n");
		for (int row=1; row<=_boardSize; row++)
		{
			for (int col=1; col<=_boardSize; col++)
			{
				int xy = GoArray.toXY(col,row);
				out.append(Integer.toString(_wins[xy]));
				out.append("/");
				out.append(Integer.toString(_playouts[xy]));
				out.append("\t");
			}
			out.append("\n");
		}
		out.append("\n");
		for (int row=1; row<=_boardSize; row++)
		{
			for (int col=1; col<=_boardSize; col++)
			{
				int xy = GoArray.toXY(col,row);
				out.append(Integer.toString((int)_virtualWins[xy]));
				out.append("/");
				out.append(Integer.toString((int)_virtualPlayouts[xy]));
				out.append("\t");
			}
			out.append("\n");
		}
		out.append("\n");
		return out.toString();
	}
}
