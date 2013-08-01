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

package tesuji.games.go.monte_carlo;

import java.util.ArrayList;
import java.util.List;

import tesuji.core.util.ArrayStack;
import tesuji.core.util.InconsistencyException;
import tesuji.core.util.MersenneTwisterFast;

import tesuji.games.general.Checksum;
import tesuji.games.general.MoveIterator;

import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.common.Util;

import tesuji.games.go.monte_carlo.move_generator.MoveGenerator;
import tesuji.games.go.pattern.util.PatternUtil;
import tesuji.games.go.util.ArrayFactory;
import tesuji.games.go.util.BoardMarker;
import tesuji.games.go.util.DefaultBoardModel;
import tesuji.games.go.util.DiagonalCursor;
import tesuji.games.go.util.FourCursor;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.IntStack;
import tesuji.games.go.util.LongStack;
import tesuji.games.go.util.PointSet;
import tesuji.games.go.util.PointSetFactory;
import tesuji.games.go.util.ProbabilityMap;
import tesuji.games.go.util.SGFUtil;
import tesuji.games.gtp.GTPCommand;
import tesuji.games.model.BoardChangeSupport;
import tesuji.games.model.BoardModel;
import tesuji.games.model.BoardModelListener;
import tesuji.games.util.Point;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.util.GoArray.*;
import static tesuji.games.go.common.GoConstant.*;

/**
 * This is an abstract class that implements the MonteCarloAdministration for as far
 * as it's the same across the different playout strategies implemented.
 */
public class MonteCarloPluginAdministration
	implements MonteCarloAdministration<GoMove>
{
	public static final boolean USE_MERCY_RULE = true;
	
	public final MersenneTwisterFast RANDOM = new MersenneTwisterFast();
	
	protected int		_boardSize = 9;
	protected double	_komi = 7.5;
	protected int		_intKomi = 7;
	protected byte		_colorToPlay;
	protected byte		_oppositeColor;
	protected int		_previousMove;
	protected int		_nrPasses;
	private int			_maxGameLength;
	private int			_mercyThreshold;
	
	private int			_nrBlackStones;
	private int			_nrWhiteStones;
	
	private byte[]		_black;
	private byte[]		_white;
	
	/**
	 * An array containing chain-numbers.
	 * All the stones belonging to the same chain have the same unique chain-number.
	 */
	protected int _chain[];
	/**
	 * An array with the coordinate of the next stone in a chain, in effect implementing
	 * an efficient circular list of stones in the chain.
	 */
	protected int _chainNext[];
	/**
	 * The liberties of each chain. The liberties of a chain at coordinate 'xy' is
	 * obtained by _liberties[_chain[xy]]. How the liberties are computed is
	 * up to the sub-class extending this abstract class. The only requirements is
	 * that when all liberties of a chain are filled that _liberties[_chain[xy]]
	 * contains the value zero.
	 */
	protected int _liberties[];
	
	/**
	 * An array containing the number of occupied neighbouring points of each point.
	 * Points at the edge always have at least a value 1, corner points at least 2.
	 */
	protected byte[] _neighbours;
	/**
	 * An array containing the number of neighbouring point occupied by black of each point.
	 * Points at the edge always have at least a value 1, corner points at least 2.
	 */
	protected byte[] _blackNeighbours;
	/**
	 * An array containing the number of neighbouring point occupied by white of each point.
	 * Points at the edge always have at least a value 1, corner points at least 2.
	 */
	protected byte[] _whiteNeighbours;
	/**
	 * An array containing the number of diagonally neighbouring point occupied by black of each point.
	 * Points at the edge are not counted.
	 */
	protected byte[] _blackDiagonalNeighbours;
	/**
	 * An array containing the number of diagonally neighbouring point occupied by white of each point.
	 * Points at the edge are not counted.
	 */
	protected byte[] _whiteDiagonalNeighbours;
	
	/**
	 * An array containing how many diagonals can be occupied at most by the opponent for a point
	 * to qualify as an eye.
	 */
	protected byte[] _maxDiagonalsOccupied;

	/**
	 * An alias to the array containing the neighbours of the color to play.
	 */
	protected byte[] _ownNeighbours;
	/**
	 * An alias to the array containing the neighbours of the opposite color to play.
	 */
	protected byte[] _otherNeighbours;
	/**
	 * An alias to the array containing the diagonal neighbours of the color to play.
	 */
	protected byte[] _ownDiagonalNeighbours;
	/**
	 * An alias to the array containing the diagonal neighbours of the opposite color to play.
	 */
	protected byte[] _otherDiagonalNeighbours;

	/**
	 * The set of unoccupied points.
	 */
	protected PointSet _emptyPoints;
	
	protected BoardChangeSupport _simulationMoveSupport;
	public BoardChangeSupport _explorationMoveSupport;

	/**
	 * BoardModel representing the board-state of the current position.
	 * This model is affected by the playout() method and will contain
	 * the end position when done.
	 */
	protected DefaultBoardModel _boardModel;
	
	/**
	 * Point where a ko-stone might have been captured by the previous move.
	 * This is set when the previous move captured a single stone and that move
	 * had four opposing neighbours. Otherwise it's set to UNDEFINED_COORDINATE.
	 */
	protected int _koPoint;
	protected Point _koPt = new Point();
	
	/**
	 * A stack with the move-coordinates.
	 */
	protected IntStack _moveStack;
	
	/**
	 * A stack with the move-coordinates that have priority.
	 */
	protected IntStack _priorityMoveStack;
	
	/**
	 * A stack with the urgency values of the priority moves. 'Urgency' is a combination of three values:
	 * - urgency as the number of moves before the 'pattern' was played.
	 * - visits as the number of times it occurred.
	 * - wins as the number of times the pattern was successful
	 */
	protected IntStack _urgencyStack;
//	protected IntStack _visitStack;
//	protected IntStack _winStack;
	protected IntStack _illegalStack;
	
	/**
	 * A list of checksums computed after each move. It's used to check for super-ko.
	 */
	protected LongStack _checksumStack;
	
	/**
	 * The checksum of the current position. Note that this is the checksum of the board-position
	 * and doesn't take into account possible ko-capture. Use getPositionalChecksum() to obtain a checksum
	 * that takes into account ko.
	 */
	private Checksum _checksum;
	
	protected int[] _stoneAge;
	protected int _playoutStart;

	/**
	 * This is a variable that gets set to true after the first move has been played in the playout() method.
	 * It gets set back to false when the playout finishes. This allows to forbid moves only during playout
	 * for example.
	 */
	protected boolean _inPlayout;
		
	protected BoardMarker _boardMarker;

	private ProbabilityMap _probabilityMap;

	private boolean _isTestVersion;
	
	protected int _lastRandomNumber;
	
	private ArrayStack<GoMoveIterator> _iteratorPool =	new ArrayStack<GoMoveIterator>();

	private boolean _spreadTest = false;
	
	private List<MoveFilter> _simulationMoveFilterList = new ArrayList<MoveFilter>();
	private List<MoveFilter> _explorationMoveFilterList = new ArrayList<MoveFilter>();

	private List<MoveGenerator> _simulationMoveGeneratorList = new ArrayList<MoveGenerator>();
	private List<MoveGenerator> _explorationMoveGeneratorList = new ArrayList<MoveGenerator>();
	
	public MonteCarloPluginAdministration()
	{
		_simulationMoveSupport = new BoardChangeSupport();
		_explorationMoveSupport = new BoardChangeSupport();
		
		_boardModel = new DefaultBoardModel();
		
		_emptyPoints = PointSetFactory.createPointSet();
		
		_liberties = createIntegers();
		_chain = createIntegers();
		_chainNext = createIntegers();
		
		_neighbours = createBytes();
		_blackNeighbours = createBytes();
		_whiteNeighbours = createBytes();
		_blackDiagonalNeighbours = createBytes();
		_whiteDiagonalNeighbours = createBytes();
		_maxDiagonalsOccupied = createBytes();
			
		_black = createBytes();
		_white = createBytes();

		_checksum = new Checksum();

		_illegalStack = ArrayFactory.createIntStack();
		_moveStack = ArrayFactory.createLargeIntStack();
		_checksumStack = ArrayFactory.createLargeLongStack();
		_priorityMoveStack = ArrayFactory.createIntStack();
		_urgencyStack = ArrayFactory.createIntStack();
//		_visitStack = ArrayFactory.createIntStack();
//		_winStack = ArrayFactory.createIntStack();
		
		_stoneAge = GoArray.createIntegers();
		
		_boardMarker = new BoardMarker();
		
		_probabilityMap = new ProbabilityMap(RANDOM);
	}

	public MonteCarloPluginAdministration(int boardSize)
	{
		this();
		setBoardSize(boardSize);
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#clear()
	 */
	public void clear()
	{
		_nrBlackStones = 0;
		_nrWhiteStones = 0;
		_previousMove = 0;
		_nrPasses = 0;
		_colorToPlay = BLACK;
		_oppositeColor = WHITE;
		
		_emptyPoints.clear();
		
		_checksum.clear();
		
		GoArray.clear(_liberties);
		GoArray.clear(_chain);
		GoArray.clear(_chainNext);
		
		GoArray.clear(_neighbours);
		GoArray.clear(_blackNeighbours);
		GoArray.clear(_whiteNeighbours);
		GoArray.clear(_blackDiagonalNeighbours);
		GoArray.clear(_whiteDiagonalNeighbours);
		
		GoArray.clear(_black);
		GoArray.clear(_white);

		GoArray.clear(_stoneAge);

		_ownNeighbours = _blackNeighbours;
		_otherNeighbours = _whiteNeighbours;
		_ownDiagonalNeighbours = _blackDiagonalNeighbours;
		_otherDiagonalNeighbours = _whiteDiagonalNeighbours;
		
		_koPoint = UNDEFINED_COORDINATE;
		
		_probabilityMap.reset();

		for (int i=FIRST; i<=LAST; i++)
		{
			if (_boardModel.get(i)!=EDGE)
			{
				//_board[i] = EMPTY;
				_boardModel.set(i, EMPTY);
				int x = getX(i);
				int y = getY(i);

				if (x==1 || y==1 || x==_boardSize || y==_boardSize)
					_maxDiagonalsOccupied[i] = 1;
				else
					_maxDiagonalsOccupied[i] = 2;
				
				_emptyPoints.add(i);			
				_probabilityMap.add(i);
			}
			
			for (int n=0; n<4; n++)
			{
				int next = FourCursor.getNeighbour(i, n);
				//if (_board[next]==EDGE)
				if (_boardModel.get(next)==EDGE)
				{
					_neighbours[i]++;
					_blackNeighbours[i]++;
					_whiteNeighbours[i]++;
				}
			}
		}
		
		// Randomize the order of the empty points.
		PointSet tmpPoints = PointSetFactory.createPointSet();
		while (_emptyPoints.getSize()>0)
		{
			int index = RANDOM.nextInt(_emptyPoints.getSize());
			int xy = _emptyPoints.get(index);
			tmpPoints.add(xy);
			_emptyPoints.remove(xy);
		}
		_emptyPoints.recycle();
		_emptyPoints = tmpPoints;
		
		_maxGameLength = _emptyPoints.getSize() * 3;
		_mercyThreshold = _boardSize*3;
		
		_moveStack.clear();
		_checksumStack.clear();
		_liberties[0] = 1000;
	}
	
	public boolean isFirstRow(int xy)
	{
		return _maxDiagonalsOccupied[xy]==1;
	}
	
	public boolean isSecondRow(int xy)
	{
		return _maxDiagonalsOccupied[xy]==2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#copyDataFrom(tesuji.games.go.monte_carlo.MonteCarloAdministration)
	 */
	public void copyDataFrom(MonteCarloAdministration<GoMove> sourceAdministration)
	{
		MonteCarloPluginAdministration source = (MonteCarloPluginAdministration) sourceAdministration;
		_spreadTest = source._spreadTest;
		_copyDataFrom(source);
	}

	private void _copyDataFrom(MonteCarloPluginAdministration source)
	{
		_boardSize = source._boardSize;
		_nrBlackStones = source._nrBlackStones;
		_nrWhiteStones = source._nrWhiteStones;
		_komi = source._komi;
		_intKomi = source._intKomi;
		_nrPasses = source._nrPasses;
		_colorToPlay = source._colorToPlay;
		_oppositeColor = source._oppositeColor;
		_previousMove = source._previousMove;
		
		_emptyPoints.copyFrom(source._emptyPoints);
		
		_checksum.copyFrom(source._checksum);
		
		_boardModel.setBoardSize(source.getBoardModel().getBoardSize());
		//copy(source._board,_board);
		copy(source._boardModel.getSingleArray(),_boardModel.getSingleArray());
		
		copy(source._liberties,_liberties);
		copy(source._chain,_chain);
		copy(source._chainNext,_chainNext);
		
		copy(source._neighbours,_neighbours);
		copy(source._blackNeighbours,_blackNeighbours);
		copy(source._whiteNeighbours,_whiteNeighbours);
		copy(source._blackDiagonalNeighbours,_blackDiagonalNeighbours);
		copy(source._whiteDiagonalNeighbours,_whiteDiagonalNeighbours);
		copy(source._maxDiagonalsOccupied,_maxDiagonalsOccupied);
		
		copy(source._black,_black);
		copy(source._white,_white);
		
		copy(source._stoneAge, _stoneAge);
		
		_moveStack.copyFrom(source._moveStack);
		_checksumStack.copyFrom(source._checksumStack);
		_probabilityMap.copyFrom(source._probabilityMap);
		
		_ownNeighbours = source._ownNeighbours;
		_otherNeighbours = source._otherNeighbours;
		_ownDiagonalNeighbours = source._ownDiagonalNeighbours;
		_otherDiagonalNeighbours = source._otherDiagonalNeighbours;
		
		_koPoint = source._koPoint;
		
		_maxGameLength = source._maxGameLength;
		_mercyThreshold = source._mercyThreshold;
		
		_isTestVersion = source._isTestVersion;
		
		for (int i=_simulationMoveFilterList.size(); --i>=0;)
		{
			_simulationMoveFilterList.get(i).copyDataFrom(source._simulationMoveFilterList.get(i));
		}
		for (int i=_explorationMoveFilterList.size(); --i>=0;)
		{
			_explorationMoveFilterList.get(i).copyDataFrom(source._explorationMoveFilterList.get(i));
		}
		
		for (int i=_simulationMoveGeneratorList.size(); --i>=0;)
		{
			_simulationMoveGeneratorList.get(i).copyDataFrom(source._simulationMoveGeneratorList.get(i));
		}
		for (int i=_explorationMoveGeneratorList.size(); --i>=0;)
		{
			_explorationMoveGeneratorList.get(i).copyDataFrom(source._explorationMoveGeneratorList.get(i));
		}
	}
	
	/**
	 * Select an empty point and play it (updating liberties and such).
	 * Whether the point is selected randomly or otherwise is up to the implementation.
	 * 
	 * @return the selected coordinate
	 */
	protected int selectAndPlay()
	{
		int xy = selectSimulationMove(_emptyPoints);
		playMove(xy);
		return xy;
	}
	
	public void playMove(int xy)
	{
		_moveStack.push(xy);		
		play(xy);
		
		if (xy==PASS)
			_nrPasses++;
		else
			_nrPasses = 0;

		_previousMove = xy;
		assert _previousMove==_moveStack.peek() : "Inconsistent previous move";
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#playMove(tesuji.games.general.Move)
	 */
	public void playMove(GoMove move)
	{
		setColorToMove(move.getColor());
		_checksumStack.push(getPositionalChecksum());
		playMove(move.getXY());
		update();
	}
	
	private void update()
	{
		for (int i=_explorationMoveGeneratorList.size(); --i>=0;)
		{
			_explorationMoveGeneratorList.get(i).update();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#playExplorationMove(tesuji.games.general.Move)
	 */
	public void playExplorationMove(GoMove move)
	{
		setColorToMove(move.getColor());
		_checksumStack.push(getPositionalChecksum());
		playMove(move.getXY());
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#playExplorationMove(tesuji.games.general.Move)
	 */
	public void playExplorationMove(int xy)
	{
		_checksumStack.push(getPositionalChecksum());
		playMove(xy);
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#isLegal(tesuji.games.general.Move)
	 */
	public boolean isLegalMove(GoMove move)
	{
		return isLegal(move.getXY());
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#isVerboten(tesuji.games.general.Move)
	 */
//	public boolean isVerboten(GoMove move)
//	{
//		return isVerboten(move.getXY());
//	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#hasRepetition()
	 */
	public boolean hasRepetition(long checksum)
	{
		int count = 0;
		for (int i=_checksumStack.getSize(); --i>=0 && count++<8;)
		{
			if (_checksumStack.get(i)==checksum)
				return true;
		}
		return false;
	}
	
	public boolean isLegal(int xy)
	{
		byte[] board = _boardModel.getSingleArray();
		assert (board[xy]==EMPTY);

		if (_neighbours[xy]!=4)
			return true;
		if (xy==_koPoint && _otherNeighbours[xy]==4)
			return false;

		for (int n=0; n<4; n++)
		{
			int next = FourCursor.getNeighbour(xy, n);
			int liberties = _liberties[_chain[next]];
			byte nextBoardValue = board[next];
			if (nextBoardValue==_oppositeColor)
			{
				if (liberties==1)
					return true;
			}
			else if (nextBoardValue==_colorToPlay)
			{
				if (liberties>1)
					return true;
			}
		}
		return false;
	}
	
	public void play(int xy)
	{
		_koPoint = UNDEFINED_COORDINATE;
		if (xy!=PASS)
		{
			boolean extended = false;
			boolean merged = false;
			byte[] board = _boardModel.getSingleArray();
			
//			assert _board[xy] == EMPTY : SGFUtil.createSGF(getMoveStack());
			assert _boardModel.get(xy) == EMPTY : SGFUtil.createSGF(getMoveStack());
			
			_chain[xy] = xy;
			_chainNext[xy] = xy;
			assert(isChainConsistent(xy));
			_stoneAge[xy] = _moveStack.getSize();
			
			addStone(xy);

			for (int n=0; n<4; n++)
			{
				int next = FourCursor.getNeighbour(xy, n);
				if (board[next]==_colorToPlay)
				{
					if (!extended)
					{
						extended = true;
						_chain[xy] = _chain[next];
						_chainNext[xy] = _chainNext[next];
						_chainNext[next] = xy;
						assert(isChainConsistent(xy));
					}
					else if (_chain[next]!=_chain[xy])
					{
						merged = true;
						int mergeLocation = next;
						int runner = next;
						int chain = _chain[xy];
						do
						{
							_chain[mergeLocation] = chain;
							mergeLocation = _chainNext[mergeLocation];
							assert( mergeLocation==next || mergeLocation!= (runner = _chainNext[_chainNext[runner]]));
						}
						while (mergeLocation!=next);
						int temp = _chainNext[xy];
						_chainNext[xy] = _chainNext[next];
						_chainNext[next] = temp;
						assert(isChainConsistent(xy));
					}
				}
			}

			if (merged) // No way but the expensive way.
				_liberties[_chain[xy]] = getLiberties(xy);
			else if (extended)
			{
				// When adding a stone to an existing chain, just take care of the shared liberties.
				int shared = getSharedLiberties(xy, _chain[xy]);
				_liberties[_chain[xy]] += (3 - _neighbours[xy]) - shared;
			}
			else // For a single stone we get the liberties cheapo.
				_liberties[_chain[xy]] = 4 - _neighbours[xy];
			
			for (int n=0; n<4; n++)
			{
				int next = FourCursor.getNeighbour(xy, n);
				if (board[next]==_oppositeColor)
				{
					int libs = _liberties[_chain[next]];
					if (libs==0)
					{
						if (_ownNeighbours[next]==4 && _otherNeighbours[xy]==4)
							_koPoint = next;
						removeCapturedChain(next);
					}
//					if (libs==1)
//					{
//						int lib = getLiberty(next);
//						addAtari(next,lib);
//					}
				}
//				if (board[next]==EMPTY)
//				{
//					if (_blackNeighbours[next]==0 || _whiteNeighbours[next]==0)
//						_probabilityMap.subtract(next,ProbabilityMap.DEFAULT/8.0);
//					else
//						resetNeighbourValue(next);
//				}
			}
			
//			if (board[left(above(xy))]==EMPTY)
//				subtractNeighbourValue(left(above(xy)),ProbabilityMap.DEFAULT/8.0);
//			if (board[left(below(xy))]==EMPTY)
//				subtractNeighbourValue(left(below(xy)),ProbabilityMap.DEFAULT/8.0);
//			if (board[right(above(xy))]==EMPTY)
//				subtractNeighbourValue(right(above(xy)),ProbabilityMap.DEFAULT/8.0);
//			if (board[right(below(xy))]==EMPTY)
//				subtractNeighbourValue(right(below(xy)),ProbabilityMap.DEFAULT/8.0);

//			if (_liberties[_chain[xy]]==1)
//			{
//				int lib = getLiberty(xy);
//				addAtari(xy,lib);
//			}
			
			assert _liberties[_chain[xy]]==getLiberties(xy) :
				"Computed liberties incorrect at "+GoArray.getX(xy)+","+GoArray.getY(xy)+"\nGot "+_liberties[_chain[xy]]+" but should be "+getLiberties(xy)+"\n\n"+toString();
		}
		
		_oppositeColor = _colorToPlay;
		_colorToPlay = opposite(_colorToPlay);
		
		setNeighbourArrays();
		
		assert _probabilityMap.isConsistent();
		assert isLibertiesConsistent() : toString();
		assert isProbabilityMapConsistent() : toString();
	}
	
	private void subtractNeighbourValue(int xy, double value)
	{
		if (_blackNeighbours[xy]==0 || _whiteNeighbours[xy]==0)
			_probabilityMap.subtract(xy, value);
	}
	
	private void resetNeighbourValue(int xy)
	{
		_probabilityMap.add(xy, ProbabilityMap.DEFAULT*(double)(_ownNeighbours[xy]-1)+ProbabilityMap.DEFAULT*(double)_ownDiagonalNeighbours[xy]);
	}
	
	private void addAtari(int xy, int lib)
	{
		int stone = xy;
		int runner = xy;
		do
		{
			_probabilityMap.add(lib);
			stone = _chainNext[stone];
			assert(stone==xy || (stone != (runner=_chainNext[_chainNext[runner]])));
		}
		while (stone!=xy);		
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#createClone()
	 */
//	@Override
	public MonteCarloAdministration<GoMove> createClone()
	{
		MonteCarloPluginAdministration clone = new MonteCarloPluginAdministration(getBoardSize());
		
		for (MoveFilter filter : _simulationMoveFilterList)
		{
			MoveFilter clonedFilter = filter.createClone();
			clone._simulationMoveFilterList.add(clonedFilter);
			clonedFilter.register(clone);
			if (clonedFilter instanceof BoardModelListener)
				clone._simulationMoveSupport.addBoardModelListener((BoardModelListener)clonedFilter);
		}
		for (MoveFilter filter : _explorationMoveFilterList)
		{
			MoveFilter clonedFilter = filter.createClone();
			clone._explorationMoveFilterList.add(clonedFilter);
			clonedFilter.register(clone);
			if (clonedFilter instanceof BoardModelListener)
				clone._explorationMoveSupport.addBoardModelListener((BoardModelListener)clonedFilter);
		}
		
		for (MoveGenerator generator : _simulationMoveGeneratorList)
		{
			MoveGenerator clonedGenerator = generator.createClone();
			clone._simulationMoveGeneratorList.add(clonedGenerator);
			clonedGenerator.register(clone);
			if (clonedGenerator instanceof BoardModelListener)
				clone._simulationMoveSupport.addBoardModelListener((BoardModelListener)clonedGenerator);
		}
		for (MoveGenerator generator : _explorationMoveGeneratorList)
		{
			MoveGenerator clonedGenerator = generator.createClone();
			clone._explorationMoveGeneratorList.add(clonedGenerator);
			clonedGenerator.register(clone);
			if (clonedGenerator instanceof BoardModelListener)
				clone._explorationMoveSupport.addBoardModelListener((BoardModelListener)clonedGenerator);
		}
		
		clone.copyDataFrom(this);
		
		return clone;
	}
	
		
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#playout()
	 */
    public boolean playout()
	{
		_inPlayout = true;
		_playoutStart = _moveStack.getSize();
		int nrMovesSofar = _playoutStart;
		while (true)
		{
			assert(_probabilityMap.isConsistent());
			selectAndPlay();

//    		System.out.println( "\n" + GoArray.toString(_boardModel.getSingleArray()) + "\n");
			assert _emptyPoints.isConsistent(_boardModel.getSingleArray());

    		// Check some of the criteria that end a game.
			if (getNrPasses()>1)
			{
				_inPlayout = false;
				return (getScore()>0);
			}
			else if (++nrMovesSofar>_maxGameLength)
			{
				_inPlayout = false;
				return (getScoreEstimate()>0);
			}
			else if (USE_MERCY_RULE)
			{
				int score = getScoreEstimate();
		    	if (score>_mercyThreshold || score<-_mercyThreshold)
		    	{
		    		_inPlayout = false;
		    		return (score>0);
		    	}
			}
		}
	}
	
	private int getLiberties(int xy)
	{
		assert xy!=0 : "Cannot get liberties for pass";

		int nrLiberties = 0;
		_boardMarker.getNewMarker();
		byte[] board = _boardModel.getSingleArray();
		
		int stoneXY = xy;
		int runnerXY = xy;
		do
		{
			assert stoneXY!=0 : "Coordinate 0 cannot be part of a chain";

			if (_neighbours[stoneXY]!=4)
			{
				int left = left(stoneXY);
				int right = right(stoneXY);
				int above = above(stoneXY);
				int below = below(stoneXY);
				if (board[left]==EMPTY && _boardMarker.notSet(left))
				{
					_boardMarker.set(left);
					nrLiberties++;
				}
				if (board[right]==EMPTY && _boardMarker.notSet(right))
				{
					_boardMarker.set(right);
					nrLiberties++;
				}
				if (board[above]==EMPTY && _boardMarker.notSet(above))
				{
					_boardMarker.set(above);
					nrLiberties++;
				}
				if (board[below]==EMPTY && _boardMarker.notSet(below))
				{
					_boardMarker.set(below);
					nrLiberties++;
				}
			}
			
			stoneXY = _chainNext[stoneXY];
			assert(stoneXY==xy || stoneXY != (runnerXY = _chainNext[_chainNext[runnerXY]])); // Detect loop.
		}
		while (stoneXY!=xy);

		return nrLiberties;
	}
	
	private boolean isChainConsistent(int chainXY)
	{
		int stoneXY = chainXY;
		int runnerXY = chainXY;
		do
		{
			stoneXY = _chainNext[stoneXY];
			if (!(stoneXY==chainXY || stoneXY != (runnerXY = _chainNext[_chainNext[runnerXY]])))
				return false; // Detect loop.
		}
		while (stoneXY!=chainXY);
		return true;
	}

	private void removeCapturedChain(int xy)
	{
		assert !hasLiberty(xy) : SGFUtil.createSGF(getMoveStack());

		int captive = xy;
		int runner = xy;
		do
		{
//			assert _board[captive]==_oppositeColor : SGFUtil.createSGF(getMoveStack());
			assert _boardModel.get(captive)==_oppositeColor : SGFUtil.createSGF(getMoveStack());
			
			_chain[captive] = 0;
			
			removeStone(captive);
			
			captive = _chainNext[captive];
			assert( captive==xy || captive != (runner = _chainNext[_chainNext[runner]]));
		}
		while (captive!=xy);		
	}
	
	private int getSharedLiberties(int xy, int chain)
	{
		int shared = 0;
		int left = left(xy);
		int right = right(xy);
		int above = above(xy);
		int below = below(xy);
		byte[] board = _boardModel.getSingleArray();
		
		if (board[left]==EMPTY &&
						(_chain[left(left)]==chain || _chain[left(above)]==chain || _chain[left(below)]==chain))
			shared++;
		if (board[right]==EMPTY &&
						(_chain[right(right)]==chain || _chain[right(above)]==chain || _chain[right(below)]==chain))
			shared++;
		if (board[above]==EMPTY &&
						(_chain[above(above)]==chain || _chain[above(left)]==chain || _chain[above(right)]==chain))
			shared++;
		if (board[below]==EMPTY &&
						(_chain[below(below)]==chain || _chain[below(left)]==chain || _chain[below(right)]==chain))
			shared++;
		return shared;
	}
	
	public int getLiberty(int xy)
	{
		byte[] board = _boardModel.getSingleArray();
		int stone = xy;
		int runner = xy;
		do
		{
			if (board[left(stone)]==EMPTY)
				return left(stone);
			if (board[right(stone)]==EMPTY)
				return right(stone);
			if (board[above(stone)]==EMPTY)
				return above(stone);
			if (board[below(stone)]==EMPTY)
				return below(stone);
			
			stone = _chainNext[stone];
			assert(stone==xy || stone!= (runner = _chainNext[_chainNext[runner]]));
		}
		while (stone!=xy);
 	
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#selectSimulationMove()
	 */
	public GoMove selectSimulationMove()
	{
		return GoMoveFactory.getSingleton().createMove(selectSimulationMove(_emptyPoints),_colorToPlay);
	}

	/**
	 * Generally the administration keeps track itself of who is to move.
	 * But if for whatever reason you want to force a certain side to move
	 * in a certain position, the color of the player to move next can be set here.
	 * 
	 * @param color
	 */
	public void setColorToMove(byte color)
	{
		_colorToPlay = color;
		_oppositeColor = opposite(color);
		setNeighbourArrays();
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getColorToMove()
	 */
	public byte getColorToMove()
	{
		return _colorToPlay;
	}
	
//	@Override
    public boolean isGameFinished()
    {
		if (!_moveStack.isEmpty() && _moveStack.peek() == PASS && _moveStack.peek(1) == PASS && _moveStack.peek(2) == PASS)
			return true;

	    return false;
    }

	/**
	 * Select a move during playout as the next move.
	 * 
	 * Note: this may look silly as a direct delegation to selectRandomMoveCoordinate()
	 * but sub-classes may choose to first favour another move-selection before
	 * making a call to selectRandomMoveCoordinate()
	 * 
	 * @param emptyPoints - set of points to choose from.
	 * 
	 * @return the selected move
	 */
	protected int selectSimulationMove(PointSet emptyPoints)
	{
		int priorityMove = selectPriorityMove(_simulationMoveGeneratorList);
		//if (priorityMove!=UNDEFINED_COORDINATE && priorityMove!=PASS && isLegal(priorityMove))
		//	return priorityMove;
		
//		return selectRandomMoveCoordinate(emptyPoints, _simulationMoveFilterList);
//		for (int i=_priorityMoveStack.getSize(); --i>=0;)
//		{
//			int xy = _priorityMoveStack.get(i);
//			double weight = 0.0;
//			if (_boardModel.get(xy)==EMPTY && _winStack.get(i)!=0)
//				weight = (double)_visitStack.get(i)/(double)_winStack.get(i);
//			if (weight!=0.0)
//				_probabilityMap.add(xy,weight);
//		}
		return selectWeightedMoveCoordinate(emptyPoints, _simulationMoveFilterList);
	}
	
	/**
	 * Select a move during exploration as the next move.
	 * 
	 * @param emptyPoints - set of points to choose from.
	 * 
	 * @return the selected move
	 */
	protected int selectExplorationMove(PointSet emptyPoints)
	{
//		int priorityMove = selectPriorityMove(_explorationMoveGeneratorList);
//		if (priorityMove!=PASS && priorityMove!=UNDEFINED_COORDINATE)
//			return priorityMove;
		
		return selectRandomMoveCoordinate(emptyPoints, _explorationMoveFilterList);
	}

	/**
	 * Randomly select an empty point from a set of empty points.
	 * 
	 * @param emptyPoints
	 * 
	 * @return coordinate of a move that's legal and not 'verboten',
	 * i.e. does something terrible like filling an own eye.
	 */
	protected int selectWeightedMoveCoordinate(PointSet emptyPoints, List<MoveFilter> filterList)
	{
		if (!_illegalStack.isEmpty())
			throw new IllegalStateException();
		assert(_illegalStack.isEmpty());
		assert(_probabilityMap.isConsistent());
		while (emptyPoints.getSize()!=0)
		{
			assert(_probabilityMap.hasPoints());
			int xy = _probabilityMap.getCoordinate();
			assert(xy!=PASS);
			assert(_boardModel.get(xy)==EMPTY);
			if (isLegal(xy) && !isVerboten(xy,filterList))
			{
				while (!_illegalStack.isEmpty())
				{
					int illegalXY = _illegalStack.pop();
					emptyPoints.add(illegalXY);
					_probabilityMap.add(illegalXY);
				}
				assert(_probabilityMap.isConsistent());
				return xy;
			}
			emptyPoints.remove(xy);
			_illegalStack.push(xy);
			assert(_probabilityMap.getWeight(xy)==ProbabilityMap.DEFAULT);
			_probabilityMap.reset(xy);
//			Statistics.increment("IllegalTries");
		}
		assert(!_probabilityMap.hasPoints());
//		if ((emptyPoints.getSize()==0 && _probabilityMap.hasPoints()) || (emptyPoints.getSize()!=0 && !_probabilityMap.hasPoints()))
//			System.err.println("Inconsistent!");
		assert(_probabilityMap.isConsistent());
//		assert((emptyPoints.getSize()!=0)==_probabilityMap.hasPoints());
		while (!_illegalStack.isEmpty())
		{
			int illegalXY = _illegalStack.pop();
			emptyPoints.add(illegalXY);
			_probabilityMap.add(illegalXY);
		}

		assert(_probabilityMap.isConsistent());
		return PASS;
	}

	protected int selectRandomMoveCoordinate(PointSet emptyPoints, List<MoveFilter> filterList)
	{
		if (!_illegalStack.isEmpty())
			throw new IllegalStateException();
		assert(_illegalStack.isEmpty());
		while (emptyPoints.getSize()!=0)
		{
			int xy = emptyPoints.get(RANDOM.nextInt(emptyPoints.getSize()));
			if (!isVerboten(xy,filterList) && isLegal(xy))
			{
				while (!_illegalStack.isEmpty())
					emptyPoints.add(_illegalStack.pop());
				return xy;
			}
			emptyPoints.remove(xy);
			_illegalStack.push(xy);
		}
		while (!_illegalStack.isEmpty())
			emptyPoints.add(_illegalStack.pop());

		return PASS;
	}

	protected int selectPriorityMove(List<MoveGenerator> moveGeneratorList)
	{
		while (!_priorityMoveStack.isEmpty())
		{
			int xy = _priorityMoveStack.pop();
			int urgency = _urgencyStack.pop();
			_probabilityMap.subtract(xy, urgency);
			_probabilityMap.add(xy, urgency/4);
		}
//		_priorityMoveStack.clear();
//    	_urgencyStack.clear();
//    	_visitStack.clear();
//    	_winStack.clear();
		if (_previousMove!=PASS)
		{
			int size = moveGeneratorList.size();
			for (int i=0; i<size; i++)
			{
				MoveGenerator generator = moveGeneratorList.get(i);
				int xy = generator.generate();
				if (xy!=UNDEFINED_COORDINATE)
				{
					_probabilityMap.add(xy,generator.getWeight());
//					return xy;
				}
			}
		}
		
		return UNDEFINED_COORDINATE;
	}
	
	/**
	 * Check if a move is not allowed, not because it's illegal but because it's undesirable.
	 * This typically will not allow a side to fill its own eyes.
	 * 
	 * @param xy - coordinate of the move
	 * @return whether allowed or not
	 */
	public boolean isVerboten(GoMove move)
	{
		int size = _simulationMoveFilterList.size();
		for (int i=0; i<size; i++)
		{
			MoveFilter filter = _simulationMoveFilterList.get(i);
			if (filter.accept(move.getXY(), getColorToMove()))
				return true;
		}
		
		return false;
		
		// Check for standard 'eye' definition.
//		return (_ownNeighbours[xy]==4 && _otherDiagonalNeighbours[xy]<_maxDiagonalsOccupied[xy]);
	}

	/**
	 * Check if a move is not allowed, not because it's illegal but because it's undesirable.
	 * This typically will not allow a side to fill its own eyes.
	 * 
	 * @param xy - coordinate of the move
	 * @return whether allowed or not
	 */
	public boolean isVerboten(int xy)
	{
		int size = _simulationMoveFilterList.size();
		for (int i=0; i<size; i++)
		{
			MoveFilter filter = _simulationMoveFilterList.get(i);
			if (filter.accept(xy, getColorToMove()))
				return true;
		}
		
		return false;
		
		// Check for standard 'eye' definition.
//		return (_ownNeighbours[xy]==4 && _otherDiagonalNeighbours[xy]<_maxDiagonalsOccupied[xy]);
	}

	/**
	 * Check if a move is not allowed, not because it's illegal but because it's undesirable.
	 * This typically will not allow a side to fill its own eyes.
	 * 
	 * @param xy - coordinate of the move
	 * @return whether allowed or not
	 */
	public boolean isVerbotenExploration(int xy)
	{
		int size = _explorationMoveFilterList.size();
		for (int i=0; i<size; i++)
		{
			MoveFilter filter = _explorationMoveFilterList.get(i);
			if (filter.accept(xy, getColorToMove()))
				return true;
		}
		
		return false;
		
		// Check for standard 'eye' definition.
//		return (_ownNeighbours[xy]==4 && _otherDiagonalNeighbours[xy]<_maxDiagonalsOccupied[xy]);
	}

	/**
	 * Check if a move is not allowed, not because it's illegal but because it's undesirable.
	 * This typically will not allow a side to fill its own eyes.
	 * 
	 * @param xy - coordinate of the move
	 * @return whether allowed or not
	 */
	public boolean isVerboten(int xy, List<MoveFilter> filterList)
	{
		int size = filterList.size();
		for (int i=0; i<size; i++)
		{
			MoveFilter filter = filterList.get(i);
			if (filter.accept(xy, getColorToMove()))
				return true;
		}
		
		return false;
		
		// Check for standard 'eye' definition.
//		return (_ownNeighbours[xy]==4 && _otherDiagonalNeighbours[xy]<_maxDiagonalsOccupied[xy]);
	}

	/**
	 * This sets the aliases of the neighbouring arrays so that 'ownNeighbours' has
	 * the neighbours of the color to play and 'otherNeighbours' has the neighbours
	 * of the opposite color.
	 */
	protected void setNeighbourArrays()
	{
		if (_colorToPlay==BLACK)
		{
			_ownNeighbours = _blackNeighbours;
			_otherNeighbours = _whiteNeighbours;
			_ownDiagonalNeighbours = _blackDiagonalNeighbours;
			_otherDiagonalNeighbours = _whiteDiagonalNeighbours;
		}
		else
		{
			_ownNeighbours = _whiteNeighbours;
			_otherNeighbours = _blackNeighbours;
			_ownDiagonalNeighbours = _whiteDiagonalNeighbours;
			_otherDiagonalNeighbours = _blackDiagonalNeighbours;			
		}		
	}
	
	/**
	 * Add a stone to the administration.
	 * 
	 * @param xy - coordinate of the stone
	 * @param color - color of the stone
	 */
	protected void addStone(int xy)
	{
		byte[] board = _boardModel.getSingleArray();
		_boardModel.set(xy, _colorToPlay);
		_emptyPoints.remove(xy);
		if (!_inPlayout)
			_checksum.add(xy, _colorToPlay);
		
		int left = left(xy);
		int right = right(xy);
		int above = above(xy);
		int below = below(xy);
		
		int leftChain = _chain[left];
		int rightChain = _chain[right];
		int aboveChain = _chain[above];
		int belowChain = _chain[below];
		
		if (board[left]==_oppositeColor)
			_liberties[leftChain]--;
		if (board[right]==_oppositeColor && leftChain!=rightChain)
			_liberties[rightChain]--;
		if (board[above]==_oppositeColor && leftChain!=aboveChain && rightChain!=aboveChain)
			_liberties[aboveChain]--;
		if (board[below]==_oppositeColor && leftChain!=belowChain && rightChain!=belowChain && aboveChain!=belowChain)
			_liberties[belowChain]--;
		
		_neighbours[left]++;
		_neighbours[right]++;
		_neighbours[above]++;
		_neighbours[below]++;
		
		if (_colorToPlay==BLACK) // For some strange reason, using _ownNeighbours without 'if' here is MUCH slower.
		{
			_nrBlackStones++;
			_black[xy] = 1;
			++_blackNeighbours[left];
			++_blackNeighbours[right];
			++_blackNeighbours[above];
			++_blackNeighbours[below];
//			if (_blackNeighbours[left]==4)
//				_probabilityMap.reset(left);
//			if (_blackNeighbours[right]==4)
//				_probabilityMap.reset(right);
//			if (_blackNeighbours[above]==4)
//				_probabilityMap.reset(above);
//			if (_blackNeighbours[below]==4)
//				_probabilityMap.reset(below);
			_blackDiagonalNeighbours[left(above)]++;
			_blackDiagonalNeighbours[left(below)]++;
			_blackDiagonalNeighbours[right(above)]++;
			_blackDiagonalNeighbours[right(below)]++;
		}
		else
		{
			_nrWhiteStones++;
			_white[xy] = 1;
			++_whiteNeighbours[left];
			++_whiteNeighbours[right];
			++_whiteNeighbours[above];
			++_whiteNeighbours[below];
//			if (_whiteNeighbours[left]==4)
//				_probabilityMap.reset(left);
//			if (_whiteNeighbours[right]==4)
//				_probabilityMap.reset(right);
//			if (_whiteNeighbours[above]==4)
//				_probabilityMap.reset(above);
//			if (_whiteNeighbours[below]==4)
//				_probabilityMap.reset(below);
			_whiteDiagonalNeighbours[left(above)]++;
			_whiteDiagonalNeighbours[left(below)]++;
			_whiteDiagonalNeighbours[right(above)]++;
			_whiteDiagonalNeighbours[right(below)]++;			
		}
		_probabilityMap.reset(xy);
	}

	/**
	 * Remove a stone from the administration.
	 * 
	 * @param xy - coordinate of the stone
	 * @param color - color of the stone
	 */
	protected void removeStone(int xy)
	{
		byte[] board = _boardModel.getSingleArray();
//		_board[xy] = EMPTY;
		_boardModel.set(xy, EMPTY);
		_emptyPoints.add(xy);
		if (!_inPlayout)
			_checksum.remove(xy, _oppositeColor);

		int left = left(xy);
		int right = right(xy);
		int above = above(xy);
		int below = below(xy);
		
		int leftChain = _chain[left];
		int rightChain = _chain[right];
		int aboveChain = _chain[above];
		int belowChain = _chain[below];
		
		if (board[left]==_colorToPlay)
			_liberties[leftChain]++;
		if (board[right]==_colorToPlay && leftChain!=rightChain)
			_liberties[rightChain]++;
		if (board[above]==_colorToPlay && leftChain!=aboveChain && rightChain!=aboveChain)
			_liberties[aboveChain]++;
		if (board[below]==_colorToPlay && leftChain!=belowChain && rightChain!=belowChain && aboveChain!=belowChain)
			_liberties[belowChain]++;

		_neighbours[left]--;
		_neighbours[right]--;
		_neighbours[above]--;
		_neighbours[below]--;
		if (_oppositeColor==BLACK) // For some strange reason, using _otherNeighbours without 'if' here is MUCH slower.
		{
			_nrBlackStones--;
			_black[xy] = 0;
			--_blackNeighbours[left];
			--_blackNeighbours[right];
			--_blackNeighbours[above];
			--_blackNeighbours[below];
//			if (_blackNeighbours[left]==3 && _boardModel.get(left)==EMPTY)
//				_probabilityMap.add(left);
//			if (_blackNeighbours[right]==3 && _boardModel.get(right)==EMPTY)
//				_probabilityMap.add(right);
//			if (_blackNeighbours[above]==3 && _boardModel.get(above)==EMPTY)
//				_probabilityMap.add(above);
//			if (_blackNeighbours[below]==3 && _boardModel.get(below)==EMPTY)
//				_probabilityMap.add(below);
			_blackDiagonalNeighbours[left(above)]--;
			_blackDiagonalNeighbours[left(below)]--;
			_blackDiagonalNeighbours[right(above)]--;
			_blackDiagonalNeighbours[right(below)]--;
		}
		else
		{
			_nrWhiteStones--;
			_white[xy] = 0;
			--_whiteNeighbours[left];
			--_whiteNeighbours[right];
			--_whiteNeighbours[above];
			--_whiteNeighbours[below];
//			if (_whiteNeighbours[left]==3 && _boardModel.get(left)==EMPTY)
//				_probabilityMap.add(left);
//			if (_whiteNeighbours[right]==3 && _boardModel.get(right)==EMPTY)
//				_probabilityMap.add(right);
//			if (_whiteNeighbours[above]==3 && _boardModel.get(above)==EMPTY)
//				_probabilityMap.add(above);
//			if (_whiteNeighbours[below]==3 && _boardModel.get(below)==EMPTY)
//				_probabilityMap.add(below);
			_whiteDiagonalNeighbours[left(above)]--;
			_whiteDiagonalNeighbours[left(below)]--;
			_whiteDiagonalNeighbours[right(above)]--;
			_whiteDiagonalNeighbours[right(below)]--;		
		}
//		if (_whiteNeighbours[xy]==4 || _blackNeighbours[xy]==4)
//			_probabilityMap.reset(xy);
//		else
			_probabilityMap.add(xy);
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getPositionalChecksum()
	 */
	public long getPositionalChecksum()
	{
		return _checksum.getValue() + 13*_colorToPlay + (_koPoint==UNDEFINED_COORDINATE? 0:_koPoint*1313);
//		if (_koPoint==UNDEFINED_COORDINATE)
//			return _checksum.getValue() + getNrPasses()*13;
//		return  _checksum.getValue() + _koPoint*17;
	}
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getPositionalChecksum()
	 */
	public long getPositionalChecksum(int orientation)
	{
		int mid = (_boardSize+1)/2;
		int x = GoArray.getX(_koPoint) - mid;
		int y = GoArray.getY(_koPoint) - mid;
		PatternUtil.adjustOrientation(x, y, orientation, _koPt);
		int koPoint = GoArray.toXY(_koPt.x+mid, _koPt.y+mid);
		return _checksum.getValue(orientation) + 13*_colorToPlay + (koPoint==UNDEFINED_COORDINATE? 0:koPoint*1313);
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getNrPasses()
	 */
    public int getNrPasses()
    {
    	return _nrPasses;
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getScore()
     */
    public int getScore()
    {
    	int nrEmptyPoints = _emptyPoints.getSize();
    	int score = getScoreEstimate();
    	if (score>0)
    	{
    		if (score-nrEmptyPoints>0)
    			return score;
    	}
    	else
    	{
    		if (score+nrEmptyPoints<0)
    			return score;
    	}
    	
    	for (int i=nrEmptyPoints; --i>=0;)
    	{
    		int xy = _emptyPoints.get(i);
    		
//    		assert _board[xy]==EMPTY : "\n" + GoArray.toString(_board) + "\n" + SGFUtil.createSGF(getMoveStack());
    		assert _boardModel.get(xy)==EMPTY : SGFUtil.createSGF(getMoveStack());
    		
    		if (_blackNeighbours[xy]==_neighbours[xy])
    		{
    			score++;
    			_black[xy] = 1;
    		}
    		else if (_whiteNeighbours[xy]==_neighbours[xy])
    		{
    			score--;
    			_white[xy] = 1;
    		}
    		else
    		{
//    			score = score;
    		}
    	}
    	
    	return score;
    }

    /**
     * @return a very rough estimate of the score, counting just the stones and komi.
     */
    private int getScoreEstimate()
    {
    	return _nrBlackStones - _nrWhiteStones - _intKomi;
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#isGameTooLong()
     */
    public boolean isGameTooLong()
    {
    	return (_moveStack.getSize() > _maxGameLength);
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#isGameAlmostFinished()
     */
    public boolean isGameAlmostFinished()
    {
       	for (int i=_emptyPoints.getSize(); --i>=0;)
    	{
    		int xy = _emptyPoints.get(i);
    		
    		if (_neighbours[xy]<3)
    			return false;
    	}
       	return true;
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#exceedsMercyThreshold()
     */
    public boolean exceedsMercyThreshold()
    {
    	int score = getScoreEstimate();
    	return (score>_mercyThreshold || score<-_mercyThreshold);
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#set(java.lang.String, java.lang.String)
     */
    public void set(String propertyName, String propertyValue)
    {
    	if (propertyName.equals(GTPCommand.BOARDSIZE))
    		setBoardSize(Integer.parseInt(propertyValue));
    	if (propertyName.equals(GTPCommand.KOMI))
    		setKomi(Double.parseDouble(propertyValue));
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#set(java.lang.String, java.lang.String)
     */
    public String get(String propertyName)
    {
    	if (propertyName.equals(GTPCommand.BOARDSIZE))
    		return Integer.toString(getBoardSize());
    	if (propertyName.equals(GTPCommand.KOMI))
    		return Double.toString(getKomi());
    	return "NA";
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getKomi()
     */
    public double getKomi()
    {
    	return _komi;
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#setKomi(double)
     */
    public void setKomi(double komi)
    {
    	_komi = komi;
    	_intKomi = (int) komi;
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getNrSimulatedMoves()
     */
    public int getNrSimulatedMoves()
    {
    	return _moveStack.getSize();
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getWinner()
     */
    public byte getWinner()
    {
    	return (getScore() <= 0) ? WHITE : BLACK;
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#isConsistent()
     */
    public boolean isConsistent()
    {
    	int[] markArray = GoArray.createIntegers();
    	for (int i=0; i<_emptyPoints.getSize(); i++)
    		markArray[_emptyPoints.get(i)] = 1;
    	
    	for (int i=0; i<MAX; i++)
    	{
       		//if (_board[i]!=EDGE)
       	   	if (_boardModel.get(i)!=EDGE)
       	   	{
    			assert !(_boardModel.get(i)==EMPTY && markArray[i]==0);
    			assert !(_boardModel.get(i)!=EMPTY && markArray[i]!=0);
//    			assert !(_board[i]==EMPTY && markArray[i]==0);
//    			assert !(_board[i]!=EMPTY && markArray[i]!=0);
    			
    			int blackNeighbours = 0;
    			int whiteNeighbours = 0;
    			for (int n=0; n<4; n++)
    			{
    				int next = FourCursor.getNeighbour(i, n);
    				//if (_board[next]==BLACK || _board[next]==EDGE)
       				if (_boardModel.get(next)==BLACK || _boardModel.get(next)==EDGE)
    					blackNeighbours++;
    				//if (_board[next]==WHITE || _board[next]==EDGE)
       				if (_boardModel.get(next)==WHITE || _boardModel.get(next)==EDGE)
    					whiteNeighbours++;
    			}
    			if (blackNeighbours!=_blackNeighbours[i])
    				throw new InconsistencyException("Black neighbour count inconsistent. "+blackNeighbours+" counted "+_blackNeighbours[i]+" stored at "+Util.printCoordinate(i));
    			if (whiteNeighbours!=_whiteNeighbours[i])
    				throw new InconsistencyException("White neighbour count inconsistent. "+whiteNeighbours+" counted "+_whiteNeighbours[i]+" stored at"+Util.printCoordinate(i));
    			
    			int blackDiagonalNeighbours = 0;
    			int whiteDiagonalNeighbours = 0;
    			for (int n=0; n<4; n++)
    			{
    				int next = DiagonalCursor.getNeighbour(i,n);
    				//if (_board[next]==BLACK)
       				if (_boardModel.get(next)==BLACK)
    					blackDiagonalNeighbours++;
    				//if (_board[next]==WHITE)
       				if (_boardModel.get(next)==WHITE)
    					whiteDiagonalNeighbours++;
    			}
    			if (blackDiagonalNeighbours!=_blackDiagonalNeighbours[i])
    				throw new InconsistencyException("Black diagonal neighbour count inconsistent. "+blackDiagonalNeighbours+" counted "+_blackDiagonalNeighbours[i]+" stored at"+Util.printCoordinate(i));
    			if (whiteDiagonalNeighbours!=_whiteDiagonalNeighbours[i])
    				throw new InconsistencyException("White diagonal neighbour count inconsistent. "+whiteDiagonalNeighbours+" counted "+_whiteDiagonalNeighbours[i]+" stored at"+Util.printCoordinate(i));
    		}
    	}
    	
    	return isLibertiesConsistent();
    }

    /**
     * This is for verification purposes.
     * 
     * @return
     */
    private boolean isLibertiesConsistent()
    {
    	for (int i=FIRST; i<=LAST; i++)
    	{
    		//if (_board[i]==BLACK || _board[i]==WHITE)
        	if (_boardModel.get(i)==BLACK || _boardModel.get(i)==WHITE)
    		{
    			assert _liberties[_chain[i]]==getLiberties(i) : "Inconsistent liberties at "+getX(i)+","+getY(i)+"\n"+
    				"Countedliberties="+getLiberties(i)+" Recorded liberties="+_liberties[_chain[i]]+"\n"+
    				//GoArray.printBoardToString(_board);
    				getBoardModel().toString();
    		}
    	}
    	
    	return true;
    }
    
    /**
     * This is for verification purposes.
     * 
     * @return
     */
    private boolean isProbabilityMapConsistent()
    {
    	for (int i=FIRST; i<=LAST; i++)
    	{
    		assert (_boardModel.get(i)==EMPTY || _probabilityMap.getWeight(i)<ProbabilityMap.ERROR_MARGIN);
//        	if (_boardModel.get(i)==EMPTY && (_blackNeighbours[i]==4 || _whiteNeighbours[i]==4))
//    		{
//        		boolean isCapture = false;
//        		for (int n=0; n<4; n++)
//        		{
//        			int next = FourCursor.getNeighbour(i, n);
//        			if (_liberties[_chain[next]]==1)
//        				isCapture = true;
//        		}
//        		assert(isCapture==true || _probabilityMap.getWeight(i)<ProbabilityMap.ERROR_MARGIN);
//    		}
    	}
    	
    	return true;
    }
    
	/**
     * @return the emptyPoints
     */
    public PointSet getEmptyPoints()
    {
    	return _emptyPoints;
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getMoves()
     */
    public MoveIterator<GoMove> getMoves()
    {
    	GoMoveIterator moveIterator;
    	if (_iteratorPool.isEmpty())
    		moveIterator = new GoMoveIterator();
    	else
    		moveIterator = _iteratorPool.pop();
    	moveIterator.init();
    	return moveIterator;
    }
    
    /**
     * Fill _priorityMoveStack with priority moves.
     * Subclasses must override this method to give certain moves priority.
     */
//    protected void getPriorityMoves()
//    {
//		for (MoveGenerator generator : _explorationMoveGeneratorList)
//		{
//			int xy = generator.generate();
//			if (xy!=UNDEFINED_COORDINATE)
//			{
//				addPriorityMove(xy,1,generator.getUrgency(),generator.getUrgency());
//			}
//		}
//    }
    
    public void addPriorityMove(int xy, int urgency/*, int visits, int wins*/)
    {
    	_priorityMoveStack.push(xy);
    	_urgencyStack.push(urgency);
    	_probabilityMap.add(xy,urgency);
//    	_visitStack.push(visits);
//    	_winStack.push(wins);
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getBoardModel()
     */
    public BoardModel getBoardModel()
    {
    	return _boardModel;
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getBoardSize()
     */
    public int getBoardSize()
    {
    	return _boardSize;
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#setIsTestVersion(boolean)
     */
	public void setIsTestVersion(boolean testVersion)
	{
		_isTestVersion = testVersion;
	}
	
	public boolean isTestVersion()
	{
		return _isTestVersion;
	}
	
    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#setBoardSize(int)
     */
    public void setBoardSize(int size)
    {
    	_boardSize = size;
    	//_board = GoArray.createBoardArray(size);
    	_boardModel.setBoardSize(size);
    	clear();
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getMoveFactory()
     */
    public GoMoveFactory getMoveFactory()
    {
    	return GoMoveFactory.getSingleton();
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getMoveStack()
     */
    public IntStack getMoveStack()
    {
    	return _moveStack;
    }
    
    public byte[] getBlackOwnership()
    {
    	return _black;
    }
    
    public byte[] getWhiteOwnership()
    {
    	return _white;
    }
    
    public ProbabilityMap getProbabilityMap()
    {
    	return _probabilityMap;
    }
    
    /**
     * This is for verification purposes.
     * 
     * @param xy
     * @return
     */
    protected boolean hasLiberty(int xy)
    {
		int stone = xy;
		int runner = xy;
		do
		{
			// Note the checks are redundant, if everything is correct...
			if (_neighbours[stone]!=4)
				return true;

			//if (_board[left(stone)]==EMPTY)
			if (_boardModel.get(left(stone))==EMPTY)
				return true;
			//if (_board[right(stone)]==EMPTY)
			if (_boardModel.get(right(stone))==EMPTY)
				return true;
			//if (_board[above(stone)]==EMPTY)
			if (_boardModel.get(above(stone))==EMPTY)
				return true;
			//if (_board[below(stone)]==EMPTY)
			if (_boardModel.get(below(stone))==EMPTY)
				return true;
			
			stone = _chainNext[stone];
			assert(stone==xy || stone != (runner = _chainNext[_chainNext[runner]]));
		}
		while (stone!=xy);
    	
		return false;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
//    	return SGFUtil.createSGF(getMoveStack()) +"\n\n"+GoArray.printBoardToString(_board);
    	return SGFUtil.createSGF(getMoveStack()) +"\n\n"+_boardModel.toString();
    }
    
    public void setIsSpreadTest(boolean spreadTest)
    {
	    _spreadTest = spreadTest;
    }

	public boolean getIsSpreadTest()
    {
	    return _spreadTest;
    }

	/**
     * When the getMoves() method is called it returns an instance of this inner-class.
     */
    private class GoMoveIterator
		implements MoveIterator<GoMove>
	{
		private PointSet _emptyPoints;
		private boolean _hasNext;
		private byte _color;
		private byte priorityIndex;
		
		GoMoveIterator()
		{
			_emptyPoints = PointSetFactory.createPointSet();
		}
		
		public void init()
		{
			_priorityMoveStack.clear();
			_urgencyStack.clear();
//			_visitStack.clear();
//			_winStack.clear();
//			getPriorityMoves();
			priorityIndex = 0;
			
			_emptyPoints.copyFrom(getEmptyPoints());
			_hasNext = true;
			_color = getColorToMove();
			_boardMarker.getNewMarker();
			update();
		}
		
		public boolean hasNext()
	    {
		    return _hasNext;
	    }
	
		public GoMove next()
	    {
			if (priorityIndex<_priorityMoveStack.getSize())
			{
				int priorityMoveXY = _priorityMoveStack.get(priorityIndex++);
				if (priorityMoveXY!=PASS && _boardMarker.notSet(priorityMoveXY) && isLegal(priorityMoveXY))
				{
					GoMove move = GoMoveFactory.getSingleton().createLightMove(priorityMoveXY,_color);
					move.setUrgency(_urgencyStack.get(priorityIndex-1));
//					move.setVisits(_visitStack.get(priorityIndex-1));
//					move.setWins(_winStack.get(priorityIndex-1));
					_emptyPoints.remove(priorityMoveXY);
					_boardMarker.set(priorityMoveXY);
					return move;
				}
			}
			
			int moveXY = selectExplorationMove(_emptyPoints);
			if (moveXY==PASS)
			{
				_hasNext = false;
				return GoMoveFactory.getSingleton().createLightPassMove(_color);
			}
			_emptyPoints.remove(moveXY);
		    return GoMoveFactory.getSingleton().createLightMove(moveXY,_color);
	    }
	
		public void remove() {}
		
		public void recycle()
		{
//			_emptyPoints.recycle();
			_iteratorPool.push(this);
		}
	}

    public boolean isPrehistoricChain(int chain)
    {
    	return (_stoneAge[chain]<=_playoutStart);
    }
    
    public boolean isPrehistoricStone(int xy)
    {
    	return (_stoneAge[_chain[xy]]<=_playoutStart);
    }
    
    public void setSimulationMoveFilterList(List<MoveFilter> list)
    {
    	_simulationMoveFilterList = list;
    	for (MoveFilter filter : list)
    	{
			if (filter instanceof BoardModelListener)
				_simulationMoveSupport.addBoardModelListener((BoardModelListener)filter);
    		filter.register(this);
    	}
    }
    
    public void setExplorationMoveFilterList(List<MoveFilter> list)
    {
    	_explorationMoveFilterList = list;
    	for (MoveFilter filter : list)
    	{
			if (filter instanceof BoardModelListener)
				_explorationMoveSupport.addBoardModelListener((BoardModelListener)filter);
    		filter.register(this);
    	}
    }
    
    public void setSimulationMoveGeneratorList(List<MoveGenerator> list)
    {
    	_simulationMoveGeneratorList = list;
    	for (MoveGenerator generator : list)
    	{
			if (generator instanceof BoardModelListener)
				_simulationMoveSupport.addBoardModelListener((BoardModelListener)generator);
			generator.register(this);
    	}
    }
    
    public List<MoveGenerator> getExplorationMoveGeneratorList()
    {
    	return _explorationMoveGeneratorList;
    }
    
    public void setExplorationMoveGeneratorList(List<MoveGenerator> list)
    {
    	_explorationMoveGeneratorList = list;
    	for (MoveGenerator generator : list)
    	{
			if (generator instanceof BoardModelListener)
				_explorationMoveSupport.addBoardModelListener((BoardModelListener)generator);
			generator.register(this);
    	}
    }
    
	public int getDiagonalHalfEye(int xy)
	{
		if (xy==0 || _otherNeighbours[xy]!=4)
			return 0;
		
		int edge=0;
		int own=0;
		int empty=0;
		int emptyXY = 0;
		for (int n=0; n<4; n++)
		{
			int next = DiagonalCursor.getNeighbour(xy, n);
			byte board = _boardModel.get(next);
			if (board==EDGE)
				edge++;
			else if (board==EMPTY)
			{
				if (_neighbours[next]>2 && _neighbours[next]==_otherNeighbours[next])
					own++;
				else
				{
					empty++;
					emptyXY = next;
				}
			}
			else if (board==_oppositeColor)
				own++;
			else if (_liberties[_chain[next]]==1)
				own++;
		}
		if (empty!=1)
			return 0;
		if (edge==3)
			return emptyXY;
		if (edge==2 && own==1)
			return emptyXY;
		if (own==2)
			return emptyXY;
		
		return 0;
	}
	
	public int getHalfEye(int xy)
	{
		if (xy==0 || _otherNeighbours[xy]!=3)
			return 0;
		
		int edge=0;
		int own=0;
		int other=0;
		int empty=0;
		for (int n=0; n<4; n++)
		{
			int next = DiagonalCursor.getNeighbour(xy, n);
			byte board = _boardModel.get(next);
			if (board==EDGE)
				edge++;
			else if (board==EMPTY)
			{
				if (_neighbours[next]>2 && _neighbours[next]==_otherNeighbours[next])
					own++;
				else
					empty++;
			}
			else if (board==_oppositeColor)
				own++;
			else if (_liberties[_chain[next]]==1)
				own++;
			else
				other++;
		}
		
		if (other+empty>1)
			return 0;
		if (edge==3 && own!=1)
			return 0;
		if (edge==2 && own!=2)
			return 0;
		for (int n=0; n<4; n++)
		{
			int next = FourCursor.getNeighbour(xy, n);
			if (_boardModel.get(next)==EMPTY)
				if (_otherNeighbours[next]==2 || (_otherNeighbours[next]==3 && _ownDiagonalNeighbours[next]>_maxDiagonalsOccupied[next]))
					return next;
		}
		
		return 0;
	}

	protected boolean isFalsePoint(int xy, byte color)
   {
	    if (_boardModel.get(left(xy))==color
						&& (_boardModel.get(left_above(xy))==opposite(color) || _boardModel.get(left_above(xy))==EDGE)
						&& (_boardModel.get(left_below(xy))==opposite(color) || _boardModel.get(left_below(xy))==EDGE))
			return true;
		if (_boardModel.get(right(xy))==color
						&& (_boardModel.get(right_above(xy))==opposite(color) || _boardModel.get(right_above(xy))==EDGE)
						&& (_boardModel.get(right_below(xy))==opposite(color) || _boardModel.get(right_below(xy))==EDGE))
			return true;
		if (_boardModel.get(above(xy))==color
						&& (_boardModel.get(left_above(xy))==opposite(color) || _boardModel.get(left_above(xy))==EDGE)
						&& (_boardModel.get(right_above(xy))==opposite(color) || _boardModel.get(right_above(xy))==EDGE))
			return true;
		if (_boardModel.get(below(xy))==color
						&& (_boardModel.get(left_below(xy))==opposite(color) || _boardModel.get(left_below(xy))==EDGE)
						&& (_boardModel.get(right_below(xy))==opposite(color) || _boardModel.get(right_below(xy))==EDGE))
			return true;
		return false;
   }

	protected int getNrStones(int xy)
	{
		int nrStones = 0;
		int stone = xy;
		int runner = xy;
		do
		{
			nrStones++;
			stone = _chainNext[stone];
			assert(stone==xy || stone!= (runner = _chainNext[_chainNext[runner]]));
		}
		while (stone!=xy);
 	
		return nrStones;
 	}    

	public boolean isAutoAtari(int xy, byte color)
	{
//		if (!isPrehistoric(_chain[left(xy)]) && !isPrehistoric(_chain[right(xy)])
//				&& !isPrehistoric(_chain[above(xy)]) && !isPrehistoric(_chain[below(xy)]))
//			return false;
		
		int nLib = 4-_neighbours[xy];
		if (nLib>1)
			return false;
		int nStones = 0;
		boolean hasAtari = false;
		boolean hasCapture = false;
		int libertyLocation = UNDEFINED_COORDINATE;
		byte otherColor = opposite(color);
		
		boolean hasFalsePoint = isFalsePoint(xy,otherColor);

		for (int n=4; --n>=0;)
		{
			int next = FourCursor.getNeighbour(xy, n);
			byte boardValue = _boardModel.get(next);
			if (boardValue==color)
			{
				int stone = next;
				int runner = next;
				do
				{
					nStones++;
					int left = left(stone);
					if (xy!=left && libertyLocation!=left && _boardModel.get(left)==EMPTY)
					{
						if (++nLib>1)
							return false;
						libertyLocation = left;
					}
					int right = right(stone);
					if (xy!=right && libertyLocation!=right && _boardModel.get(right)==EMPTY)
					{
						if (++nLib>1)
							return false;
						libertyLocation = right;
					}
					int above = above(stone);
					if (xy!=above && libertyLocation!=above && _boardModel.get(above)==EMPTY)
					{
						if (++nLib>1)
							return false;
						libertyLocation = above;
					}
					int below = below(stone);
					if (xy!=below && libertyLocation!=below && _boardModel.get(below)==EMPTY)
					{
						if (++nLib>1)
							return false;
						libertyLocation = below;
					}
					
					if (!hasFalsePoint && isFalsePoint(stone,otherColor))
						hasFalsePoint = true;					

					stone = _chainNext[stone];
					assert(stone==next || stone != (runner = _chainNext[_chainNext[runner]]));
				}
				while (stone!=next);
			}
			else if (boardValue==otherColor)
			{
				if (!hasAtari && _liberties[_chain[next]]==2 && getNrStones(next)>1)
					hasAtari = true;
				if (_liberties[_chain[next]]==1)
				{
					if (getNrStones(next)>1)
						return false;
					nLib++;
					hasCapture = true;
				}
			}
			else if (boardValue==EMPTY)
				libertyLocation = next;
		}
		
		if (nLib<2)
		{
			if (hasFalsePoint)
				nStones-=2;
			if (nStones>5)
				return true;
			if (color==BLACK)
			{
				if (nStones==1 && libertyLocation!=UNDEFINED_COORDINATE
						&& _whiteNeighbours[xy]==3 && _blackNeighbours[libertyLocation]>=3)
					return true;
				if (_neighbours[xy]==4 && !hasCapture && !hasAtari && _whiteNeighbours[xy]==3)
					return true;
			}
			else
			{
				if (nStones==1 && libertyLocation!=UNDEFINED_COORDINATE
								&& _blackNeighbours[xy]==3 && _whiteNeighbours[libertyLocation]>=3)
					return true;
				if (_neighbours[xy]==4 && !hasCapture && !hasAtari && _blackNeighbours[xy]==3)
					return true;
			}
		}
		
		return false;
	}
    
    public int getLastMove()
    {
    	return _previousMove;
    }
    
    public int getBeforeLastMove()
    {
    	if (_moveStack.getSize()<2)
    		return PASS;
    	return _moveStack.peek(1);
    }
    
    public int getKoPoint()
    {
    	return _koPoint;
    }
    
    public byte[] getBoardArray()
    {
    	return _boardModel.getSingleArray();
    }
    
    public int[] getChainArray()
    {
    	return _chain;
    }
    
    public int[] getLibertyArray()
    {
    	return _liberties;
    }
    
    public byte[] getNeighbourArray()
    {
    	return _neighbours;
    }
    
    public byte[] getBlackNeighbourArray()
    {
    	return _blackNeighbours;
    }
    
    public byte[] getWhiteNeighbourArray()
    {
    	return _whiteNeighbours;
    }
    
    public byte[] getBlackDiagonalNeighbourArray()
    {
    	return _blackDiagonalNeighbours;
    }
    
    public byte[] getWhiteDiagonalNeighbourArray()
    {
    	return _whiteDiagonalNeighbours;
    }
    
    public byte[] getMaxDiagonalArray()
    {
    	return _maxDiagonalsOccupied;
    }
    
    public byte[] getOwnNeighbourAray()
    {
    	return _ownNeighbours;
    }
    
    public byte[] getOtherNeighbourAray()
    {
    	return _otherNeighbours;
    }    
    
    public byte[] getOwnDiagonalAray()
    {
    	return _ownDiagonalNeighbours;
    }
    
    public byte[] getOtherDiagonalAray()
    {
    	return _ownDiagonalNeighbours;
    }    
}
