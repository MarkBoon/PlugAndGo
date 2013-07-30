package tesuji.games.go.search;

import static tesuji.games.general.ColorConstant.BLACK;
import static tesuji.games.general.ColorConstant.WHITE;
import tesuji.core.util.MersenneTwisterFast;
import tesuji.core.util.SynchronizedArrayStack;
import tesuji.games.general.ColorConstant;
import tesuji.games.general.search.SearchResult;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.monte_carlo.move_generator.MoveGenerator;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.PointSet;
import tesuji.games.go.util.PointSetFactory;

public class MonteCarloHashMapResult
//	implements SearchResult<GoMove>
{
	public static final int FORBIDDEN = 1000000;
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

//	private GoMove		_move;
	private int			_xy;
	private byte		_color;
//	private int			_koPoint;
	private int			_totalPlayouts;
	private int[]		_wins;
	private int[]		_playouts;
	private PointSet	_emptyPoints;
	private float[]		_virtualWins;
	private float[]		_virtualPlayouts;
	private double		_logNrPlayouts;
	private double		_beta;
	private int			_age;
	private long		_checksum;
	private int			_bestMove;
	private double		_bestResult;
	public boolean		usedLastBest;
	
	private int _boardSize;
	
//	private byte[] _board;
//	private IntStack _moves;
	
	private SynchronizedArrayStack<MonteCarloHashMapResult> _owner;

	MonteCarloHashMapResult(SynchronizedArrayStack<MonteCarloHashMapResult> owner)
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
//		_board = GoArray.createBytes();
//		_moves = new ArrayFactory().createIntStack();
	}
	
	public void setPointSet(MonteCarloPluginAdministration administration)
	{
		MersenneTwisterFast random = administration.RANDOM;
		_boardSize = administration.getBoardSize();

//		_koPoint = administration.getKoPoint();
//
//		if (administration.getBoardArray()!=null)
//		{
//			GoArray.copy(administration.getBoardArray(), _board);
//			_moves.copyFrom(administration.getMoveStack());
//		}

		assert(_emptyPoints.getSize()==0);
		PointSet copy = PointSetFactory.createPointSet();
		copy.copyFrom(administration.getEmptyPoints());
		for (int size = copy.getSize(); size>0; size--)
		{
			int xy = copy.get(random.nextInt(size));
			copy.remove(xy);
			if (administration.isLegal(xy) && !administration.isVerboten(xy))
			{
				_emptyPoints.add(xy);
				_virtualPlayouts[xy] = 0;
				_virtualWins[xy] = 0;
			}
		}
		copy.recycle();
		if (administration.isGameAlmostFinished())
		{
			_virtualPlayouts[GoConstant.PASS] = 0;
			_virtualWins[GoConstant.PASS] = 0;
			_emptyPoints.add(GoConstant.PASS);
		}
		
		for (MoveGenerator generator : administration.getExplorationMoveGeneratorList())
		{
			int xy = generator.generate();
			if (xy!=GoConstant.UNDEFINED_COORDINATE)
				increaseVirtualPlayouts(xy, generator.getUrgency(), generator.getUrgency());
		}
		assert(_emptyPoints.freeze());
	}
	
	protected void init()
	{
		_xy = GoConstant.UNDEFINED_COORDINATE;
		_logNrPlayouts = 0.0;
		_beta = 0.0;
		_totalPlayouts = 0;
		_bestMove = GoConstant.PASS;
		_bestResult = -1.0;
		usedLastBest = false;
		
		_emptyPoints.clear();
		GoArray.clear(_playouts);
		GoArray.clear(_wins);
		GoArray.clear(_virtualWins);
		GoArray.clear(_virtualPlayouts);
//		GoArray.clear(_results);
	}

//	@Override
    public void recycle()
    {
//		init();
//		if (_move!=null)
//			_move.recycle();
//		_move = null;

    	assert(_emptyPoints.unfreeze());
		_owner.push(this);
    }

//	@Override
//    public GoMove getMove()
//    {
//	    return _move;
//    }
	
//	@Override
//    public void setMove(GoMove move)
//    {
//	    _move = move;
//    }
	
	public PointSet getEmptyPoints()
	{
		return _emptyPoints;
	}
	
    public boolean isBetterResultThan(int xy1, int xy2)
    {
		if (xy2==GoConstant.PASS)
			return true;
		
		if (_playouts[xy1]>_playouts[xy2])
			return true;

		if (_playouts[xy1]==_playouts[xy2])
		{
			double value = getWinRatio(xy1);
			double compareValue = getWinRatio(xy2);
			
			assert value!=Double.NaN : "Calculation error for the result-value.";
			assert compareValue!=Double.NaN : "Calculation error for the compare-value.";
			
			if (value>compareValue)
				return true;
			
			if (value==compareValue)
				return getVirtualWinRatio(xy1)>getVirtualWinRatio(xy2);
		}
		return false;
    }

//	@Override
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

	public int getWins(int xy)
    {
	    return _wins[xy];
    }

	public int getPlayouts(int xy)
    {
	    return _playouts[xy];
    }
	
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
		double bestResult = computeResult(_bestMove);
		if (_bestMove!=GoConstant.PASS && bestResult>_bestResult)
		{
			assert(bestResult>0.0);
			usedLastBest = true;
			// What was previously the best move, most likely still is as it only got better.
			_bestResult = bestResult;
			return _bestMove;
		}
		usedLastBest = false;

		for (int i=_emptyPoints.getSize(); --i>=0;)
		{
			int next = _emptyPoints.get(i);
			double result = computeResult(next);
			boolean better;
			if (result>bestResult)
				better = true;
			else if (result==bestResult)
				better = (bestMove==GoConstant.PASS || _virtualPlayouts[next] > _virtualPlayouts[bestMove]);
			else
				better = false;
			
			if (better)
			{
				bestMove = next;
				bestResult = result;
			}
		}
		_bestMove = bestMove;
		_bestResult = bestResult;
		assert(bestResult>=0.0);
		return bestMove;
	}
	
	public int getBestMove()
	{
		int bestMove = GoConstant.PASS;
		for (int i=_emptyPoints.getSize(); --i>=0;)
		{
			int next = _emptyPoints.get(i);
			if (isBetterResultThan(next, bestMove))
				bestMove = next;
		}
		return bestMove;
	}
	
	public double computeResult(int xy)
	{
		double virtualResult = getVirtualWinRatio(xy) + getRAVEValue(xy);
		if (_playouts[xy]==0)
			return virtualResult;

		double result = getWinRatio(xy)+getUCTValue(xy);
		return _beta * virtualResult + (1.0-_beta) * result;
		//return _beta * (getVirtualWinRatio(xy)+getRAVEValue(xy)) + (1.0-_beta) * (getWinRatio(xy)+getUCTValue(xy));	// Java barfed on this expression, hence the 'spelling out' above.	
	}

//	private boolean isBetterVirtualMove(int xy1, int xy2)
//	{
//		double virtualResult;
//		double compareResult;
//		
////		virtualResult = _results[xy1];
////		compareResult = _results[xy2];
////		if (virtualResult<0)
//			virtualResult = computeResult(xy1);
////		if (compareResult<0)
//			compareResult = computeResult(xy2);
//		
//		assert virtualResult!=Double.NaN : "Calculation error for the virtual result-value.";
//		assert compareResult!=Double.NaN : "Calculation error for the virtual compare-value.";
//
//		if (virtualResult>compareResult)
//			return true;
//		
//		// Trust the most visited node more. I don't know if it's all that relevant.
//		// I could probably just as easily argue it should be the other way around.
//		if (virtualResult==compareResult)
//			return (xy2==GoConstant.PASS || _playouts[xy1] > _playouts[xy2]);	// TODO: check '<' instead of '>', see which plays better.
//		
//		return false;
//		
//	}

	public void increasePlayouts()
	{
		_totalPlayouts++;
		if ((_totalPlayouts&0x7)==0)
			_logNrPlayouts = Math.log(_totalPlayouts);
		_beta = getBeta();
	}

//	public void increasePlayouts(int xy)
//	{
//		_totalPlayouts++;
//		_playouts[xy]++;
//		_virtualPlayouts[xy]++;
//		
//		// Computing log() is expensive. I see no need to do it each and every time.
//		if ((_totalPlayouts&0x7)==0)
//			_logNrPlayouts = Math.log(_totalPlayouts);
//		_beta = getBeta();
//	}

	public void increaseWins(int xy, boolean blackWins)
	{		
		byte color = ColorConstant.opposite(_color);
		boolean playerWins = (blackWins && color==BLACK) || (!blackWins && color==WHITE);
		
		if (playerWins)
		{
			_wins[xy]++;
			_virtualWins[xy]++;
		}
		_playouts[xy]++;
		_virtualPlayouts[xy]++;
//		_results[xy] = Double.MIN_VALUE;
	}

	public void increasePlayouts(int xy, int wins, int played)
	{
		_wins[xy] += wins;
		_playouts[xy] += played;
//		_results[xy] = Double.MIN_VALUE;
	}
	
	public void increaseVirtualPlayouts(int xy, double win_weight, double weight)
	{
		_virtualWins[xy] += win_weight;
		_virtualPlayouts[xy] += weight;
//		_results[xy] = Double.MIN_VALUE;
	}
	
	public void forbid(int xy)
	{
		_wins[xy] = 0;
		_virtualWins[xy] = 0;
		_playouts[xy] =FORBIDDEN;
		_virtualPlayouts[xy] = FORBIDDEN;
//		_results[xy] = Double.MIN_VALUE;
	}

	public String toString()
	{
		StringBuilder out = new StringBuilder();
		
		out.append("\nHashMapSearch\n");
//		out.append(GoArray.toString(_board));
		GoMove move = GoMoveFactory.getSingleton().createMove(getBestMove(), ColorConstant.opposite(_color));
		out.append(move.toString()+"\n");
		for (int row=1; row<=_boardSize; row++)
		{
			for (int col=1; col<=_boardSize; col++)
			{
				int xy = GoArray.toXY(col,row);
				if (_playouts[xy]!=0)
					out.append(Double.toString(_wins[xy]/_playouts[xy]));
				else
				{
					out.append(Integer.toString(_wins[xy]));
					out.append("/");
					out.append(Integer.toString(_playouts[xy]));
				}
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
				if (_virtualPlayouts[xy]!=0.0)
					out.append(Double.toString((double)_virtualWins[xy]/(double)_virtualPlayouts[xy]));
				else
				{
					out.append(Double.toString(_virtualWins[xy]));
					out.append("/");
					out.append(Double.toString(_virtualPlayouts[xy]));
				}
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
				out.append(Double.toString(getVirtualWinRatio(xy)));
				out.append("/");
				out.append(Double.toString(getWinRatio(xy)));
				out.append("\t");
			}
			out.append("\n");
		}
		out.append("\n");
		return out.toString();
	}

//	@Override
    public boolean isBetterResultThan(SearchResult<GoMove> compare)
    {
	    // TODO Auto-generated method stub
	    return false;
    }

	public int getAge()
	{
		return _age;
	}

	public void setAge(int age)
	{
		_age = age;
	}

	public long getChecksum()
	{
		return _checksum;
	}

	public void setChecksum(long checksum)
	{
		_checksum = checksum;
	}

	public int getXY()
    {
    	return _xy;
    }

	public void setXY(int xy)
    {
    	_xy = xy;
    }

	public byte getColor()
    {
    	return _color;
    }

	public void setColor(byte color)
    {
    	_color = color;
    }
}
