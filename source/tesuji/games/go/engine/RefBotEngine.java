package tesuji.games.go.engine;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

import tesuji.core.util.InconsistencyException;

import tesuji.games.go.common.BasicGoMoveAdministration;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.common.GoEngineAdapter;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;

import tesuji.games.go.monte_carlo.MonteCarloAdministration;

import tesuji.games.go.util.BoardMarker;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.GoGameProperties;
import tesuji.games.go.util.IntStack;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.util.GoArray.*;

/**
 * Go playing engine that conforms to Don Dailey's 'reference-bot' implementation.
 */
public class RefBotEngine
    extends GoEngineAdapter
{
	private Logger _logger;

	private int _boardSize = GoArray.DEFAULT_SIZE;
	private int _nrSimulations;
	private double[] _winMap;
	private double[] _hitMap;
	
	private boolean _verbose;
	private boolean _useEnhanced = true;
	private boolean _resignEnabled = false;

	private static final double MAX_SCORE = 1.0;
	private static final double MIN_SCORE = -1.0;
	
	private BoardMarker _boardMarker = new BoardMarker();
	
	private MonteCarloAdministration<GoMove> _monteCarloAdministration;
	
	// The BasicGoMoveAdministration is redundant but used to verify MonteCarloAdministration
	private BasicGoMoveAdministration _moveAdministration;

	public RefBotEngine()
	{
		_logger = Logger.getLogger(this.getClass());

		_moveAdministration = new BasicGoMoveAdministration(new GoGameProperties());
		_winMap = createDoubles();
		_hitMap = createDoubles();
		
		getGameProperties().addPropertyChangeListener( new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent event)
					{
						_moveAdministration.getGameProperties().setProperty(event.getPropertyName(), event.getNewValue().toString());
						_monteCarloAdministration.set(event.getPropertyName(), event.getNewValue().toString());
					}
				}
			);
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#getEngineName()
	 */
	@Override
	public String getEngineName()
	{
		return "RefBot";
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#getEngineVersion()
	 */
	@Override
	public String getEngineVersion()
	{
		return "1.0";
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#clearBoard()
	 */
	@Override
	public void clearBoard()
	{
		_boardSize = getGameProperties().getIntProperty(GoGameProperties.BOARDSIZE);
		_monteCarloAdministration.set(GoGameProperties.BOARDSIZE,""+_boardSize);
		_monteCarloAdministration.clear();
		_moveAdministration.clear();
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#play(tesuji.games.go.common.GoMove)
	 */
	@Override
	public void playMove(GoMove move)
	{
		_monteCarloAdministration.playMove(move);
		_moveAdministration.playMove(move);
		if (isVerbose())
			_logger.info("Position:\n"+_monteCarloAdministration.toString());
		
		assert _monteCarloAdministration.isConsistent() : "MC administration inconsistent.";
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#takeBack()
	 */
	@Override
	public void takeBack()
	{
		// N/A
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#requestMove(byte)
	 */
	@Override
	public GoMove requestMove(byte color)
	{
		MonteCarloAdministration<GoMove> playoutAdministration =  _monteCarloAdministration.createClone();
		clear(_winMap);
		clear(_hitMap);
		
		assert _monteCarloAdministration.getColorToMove()==color : "Color mismatch!";
		
		for (int n=0; n<_nrSimulations; n++)
		{
			playoutAdministration.copyDataFrom(_monteCarloAdministration);
			boolean blackWins = playoutAdministration.playout();
			double score = (blackWins && color==BLACK) || (!blackWins && color==WHITE) ? MAX_SCORE : MIN_SCORE;
			
			IntStack playoutMoves = playoutAdministration.getMoveStack();
			int start = _monteCarloAdministration.getMoveStack().getSize();
			int end = playoutMoves.getSize();
			double weight = 1.0;
			double weightDelta = 2.0 / (end - start + 1); // Michael Williams' idea to use decreasing weights
			_boardMarker.getNewMarker();
			for (int i=start; i<end; i+=2)
			{
				int moveXY = playoutMoves.get(i);
				if (_boardMarker.notSet(moveXY))
				{
					_winMap[moveXY] += weight * score;
					_hitMap[moveXY] += weight;
					_boardMarker.set(moveXY);
				}
				if (_useEnhanced)
					weight -= weightDelta;
			}
		}
		
		double bestResult = MIN_SCORE;
		int bestMove = GoConstant.PASS;
		boolean hasLegalMove = false;
		for (int i=FIRST; i<=LAST; i++)
		{
			GoMove move = getMoveFactory().createMove(i, color);
			if (_monteCarloAdministration.isLegalMove(move) && !_monteCarloAdministration.isVerboten(move))
			{
				hasLegalMove = true;
				if (_hitMap[i]>0)
				{
					double result = _winMap[i]/_hitMap[i];
					if (result>bestResult)
					{
							bestResult = result;
							bestMove = i;
					}
				}
			}
			move.recycle();
		}
		if (bestResult>MIN_SCORE)
		{
			GoMove move = getMoveFactory().createMove(bestMove, color);
			if (!_moveAdministration.isLegalMove(move))
			{
				throw new InconsistencyException("Illegal move");
			}
			assert _monteCarloAdministration.isConsistent() : "MC administration inconsistent.";
			assert _monteCarloAdministration.isLegalMove(move) : "Move "+move+"illegal according to MC-administration.\n"+_monteCarloAdministration.toString();
			assert _moveAdministration.isLegalMove(move) : "Move "+move+" illegal according to move-administration.\n"+_moveAdministration.getBoardModel()+"\n\n"+_monteCarloAdministration.toString();
			if (isVerbose())
				_logger.info("Play at "+move);
			return move;
		}
		
		if (hasLegalMove)
		{
			// None of the playouts resulted in a win but there are legal moves left.
			// This would be a good place to resign, although that depends on the number of playouts used.
			// For now it selects a move at random.
			if (_resignEnabled)
			{
				GoMove resignMove = GoMoveFactory.getSingleton().createResignMove(color);
				return resignMove;
			}
			
			GoMove move = _monteCarloAdministration.selectSimulationMove();
			if (!_moveAdministration.isLegalMove(move))
			{
				throw new InconsistencyException("Illegal move");
			}
			return move;
		}
		
		return getMoveFactory().createPassMove(color);
	}

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#setup(tesuji.core.util.ArrayList)
     */
    @Override
	public void setup(Iterable<GoMove> moveList)
    {
    	_monteCarloAdministration.clear();
    	_moveAdministration.clear();
    	for (GoMove move : moveList)
    	{
    		_monteCarloAdministration.playMove(move);
    		_moveAdministration.playMove(move);
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.general.GameEngineAdapter#getScore()
     */
    @Override
    public Double getScore()
    {
    	return new Double(_monteCarloAdministration.getScore());
    }

	public int getNrSimulations()
	{
		return _nrSimulations;
	}

	public void setNrSimulations(int simulations)
	{
		_nrSimulations = simulations;
	}
	
	public MonteCarloAdministration<GoMove> getMonteCarloAdministration()
	{
		return _monteCarloAdministration;
	}
	
	public void setMonteCarloAdministration(MonteCarloAdministration<GoMove> administration)
	{
		_monteCarloAdministration = administration;
	}
	
	public boolean isVerbose()
	{
		return _verbose;
	}
	
	public void setIsVerbose(boolean verbose)
	{
		_verbose = verbose;
	}
	
	public boolean isEnhanced()
	{
		return _useEnhanced;
	}
	
	public void setIsEnhanced(boolean enhanced)
	{
		_useEnhanced = enhanced;
	}
	
	public boolean getResignEnabled()
	{
		return _resignEnabled;
	}
	
	public void setResignEnabled(boolean enabled)
	{
		_resignEnabled = enabled;
	}
}
