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

import tesuji.core.util.ArrayStack;
import tesuji.core.util.InconsistencyException;
import tesuji.core.util.MersenneTwisterFast;

import tesuji.games.general.Checksum;
import tesuji.games.general.MoveIterator;

import tesuji.games.go.common.GoConstant;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.common.Util;

import tesuji.games.go.util.ArrayFactory;
import tesuji.games.go.util.BoardMarker;
import tesuji.games.go.util.DefaultBoardModel;
import tesuji.games.go.util.DiagonalCursor;
import tesuji.games.go.util.FourCursor;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.IntStack;
import tesuji.games.go.util.PointSet;
import tesuji.games.go.util.PointSetFactory;
import tesuji.games.go.util.SGFUtil;
import tesuji.games.gtp.GTPCommand;
import tesuji.games.model.BoardModel;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.util.GoArray.*;
import static tesuji.games.go.common.GoConstant.*;

/**
 * This is an abstract class that implements the MonteCarloAdministration for as far
 * as it's the same across the different playout strategies implemented.
 */
public class MonteCarloGoAdministration
	implements MonteCarloAdministration<GoMove>
{
	public static final boolean USE_MERCY_RULE = false;
	
	protected final MersenneTwisterFast RANDOM = new MersenneTwisterFast();
	
	protected int		_boardSize;
	protected double	_komi = 7.5;
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
	protected IntStack _visitStack;
	protected IntStack _winStack;
	
	/**
	 * A list of checksums computed after each move. It's used to check for super-ko.
	 */
	protected IntStack _checksumStack;
	
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

	private boolean _isTestVersion;
	
	protected int _lastRandomNumber;
	
	private ArrayStack<GoMoveIterator> _iteratorPool =	new ArrayStack<GoMoveIterator>();
	
	
	public MonteCarloGoAdministration()
	{
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

		_moveStack = ArrayFactory.createLargeIntStack();
		_checksumStack = ArrayFactory.createLargeIntStack();
		_priorityMoveStack = ArrayFactory.createIntStack();
		_urgencyStack = ArrayFactory.createIntStack();
		_visitStack = ArrayFactory.createIntStack();
		_winStack = ArrayFactory.createIntStack();
		
		_stoneAge = GoArray.createIntegers();
		
		_boardMarker = new BoardMarker();
	}

	protected MonteCarloGoAdministration(int boardSize)
	{
		this();
		_boardSize = boardSize;
		initBoardModel(getBoardSize());		
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
		
		for (int i=FIRST; i<=LAST; i++)
		{
			if (_boardModel.get(i)!=EDGE)
			{
				_boardModel.set(i, EMPTY);
				int x = getX(i);
				int y = getY(i);

				if (x==1 || y==1 || x==_boardSize || y==_boardSize)
					_maxDiagonalsOccupied[i] = 1;
				else
					_maxDiagonalsOccupied[i] = 2;
				
				_emptyPoints.add(i);
				
			}
			for (int n=0; n<4; n++)
			{
				int next = FourCursor.getNeighbour(i, n);
				if (_boardModel.get(next)==EDGE)
				{
					_neighbours[i]++;
					_blackNeighbours[i]++;
					_whiteNeighbours[i]++;
				}
			}
		}

		_maxGameLength = _emptyPoints.getSize() * 3;
		_mercyThreshold = _boardSize*3;
		
		_moveStack.clear();
		_checksumStack.clear();
		_liberties[0] = 1000;
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#copyDataFrom(tesuji.games.go.monte_carlo.MonteCarloAdministration)
	 */
	public void copyDataFrom(MonteCarloAdministration<GoMove> sourceAdministration)
	{
		MonteCarloGoAdministration source = (MonteCarloGoAdministration) sourceAdministration;
		_copyDataFrom(source);
	}

	private void _copyDataFrom(MonteCarloGoAdministration source)
	{
		_boardSize = source._boardSize;
		_nrBlackStones = source._nrBlackStones;
		_nrWhiteStones = source._nrWhiteStones;
		_komi = source._komi;
		_nrPasses = source._nrPasses;
		_colorToPlay = source._colorToPlay;
		_oppositeColor = source._oppositeColor;
		_previousMove = source._previousMove;
		
		_emptyPoints.copyFrom(source._emptyPoints);
		
		_checksum.setValue(source._checksum.getValue());
		
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
		
		_ownNeighbours = source._ownNeighbours;
		_otherNeighbours = source._otherNeighbours;
		_ownDiagonalNeighbours = source._ownDiagonalNeighbours;
		_otherDiagonalNeighbours = source._otherDiagonalNeighbours;
		
		_koPoint = source._koPoint;
		
		_maxGameLength = source._maxGameLength;
		_mercyThreshold = source._mercyThreshold;
		
		_isTestVersion = source._isTestVersion;
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
		_checksumStack.push(getPositionalChecksum());
		return xy;
	}
	
	private void playMove(int xy)
	{
		_koPoint = UNDEFINED_COORDINATE;

		_moveStack.push(xy);		
		play(xy);
		
		if (xy==PASS)
			_nrPasses++;
		else
			_nrPasses = 0;

		_previousMove = xy;
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#playMove(tesuji.games.general.Move)
	 */
	public void playMove(GoMove move)
	{
		setColorToMove(move.getColor());
		playMove(move.getXY());
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#playExplorationMove(tesuji.games.general.Move)
	 */
	public void playExplorationMove(GoMove move)
	{
		setColorToMove(move.getColor());
		playMove(move.getXY());
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
	public boolean isVerboten(GoMove move)
	{
		return isVerboten(move.getXY());
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#hasRepetition()
	 */
	public boolean hasRepetition(int checksum)
	{
		for (int i=_checksumStack.getSize(); --i>=0;)
		{
			if (_checksumStack.get(i)==checksum)
				return true;
		}
		return false;
	}
	
	public boolean isLegal(int xy)
	{
		if (xy<0) // Temporary hack
			return false;

		byte[] board = _boardModel.getSingleArray();
		if (board[xy]!=EMPTY)
			return false; // Occupied.

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
		if (xy!=PASS)
		{
			boolean extended = false;
			boolean merged = false;
			byte[] board = _boardModel.getSingleArray();
			
			assert _boardModel.get(xy) == EMPTY : SGFUtil.createSGF(getMoveStack());
			
			_chain[xy] = xy;
			_chainNext[xy] = xy;
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
					}
					else if (_chain[next]!=_chain[xy])
					{
						merged = true;
						int mergeLocation = next;
						int chain = _chain[xy];
						do
						{
							_chain[mergeLocation] = chain;
							mergeLocation = _chainNext[mergeLocation];
						}
						while (mergeLocation!=next);
						int temp = _chainNext[xy];
						_chainNext[xy] = _chainNext[next];
						_chainNext[next] = temp;
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
					if (_liberties[_chain[next]]==0)
					{
						if (_ownNeighbours[next]==4 && _otherNeighbours[xy]==4)
							_koPoint = next;
						removeCapturedChain(next);
					}
				}
			}
			
			assert _liberties[_chain[xy]]==getLiberties(xy) :
				"Computed liberties incorrect at "+GoArray.getX(xy)+","+GoArray.getY(xy)+"\nGot "+_liberties[_chain[xy]]+" but should be "+getLiberties(xy)+"\n\n"+toString();
		}
		
		_oppositeColor = _colorToPlay;
		_colorToPlay = opposite(_colorToPlay);
		
		setNeighbourArrays();
		
		assert isLibertiesConsistent() : toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#createClone()
	 */
	@Override
	public MonteCarloAdministration<GoMove> createClone()
	{
		MonteCarloGoAdministration clone = new MonteCarloGoAdministration(getBoardSize());
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
		while (true)
		{
			selectAndPlay();
			
			// Check some of the criteria that end a game.
			if (getNrPasses()>1)
			{
				_inPlayout = false;
				return (getScore()>0.0);
			}
			else if (USE_MERCY_RULE && exceedsMercyThreshold())
			{
				_inPlayout = false;
				return (getScoreEstimate()>0.0);
			}
			else if (isGameTooLong())
			{
				_inPlayout = false;
				return (getScoreEstimate()>0.0); // Should never happen.
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
		}
		while (stoneXY!=xy);

		return nrLiberties;
	}

	private void removeCapturedChain(int xy)
	{
		assert !hasLiberty(xy) : SGFUtil.createSGF(getMoveStack());

		byte[] board = _boardModel.getSingleArray();
		int nrStones = 0;
		int captive = xy;
		do
		{
			assert _boardModel.get(captive)==_oppositeColor : SGFUtil.createSGF(getMoveStack());
			
			_chain[captive] = 0;
			nrStones++;
			
			removeStone(captive);
			
			captive = _chainNext[captive];
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
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#selectSimulationMove()
	 */
	public GoMove selectSimulationMove()
	{
		return GoMoveFactory.getSingleton().createMove(selectSimulationMove(_emptyPoints), _colorToPlay);
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
		return selectRandomMoveCoordinate(emptyPoints);
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
		return selectRandomMoveCoordinate(emptyPoints);
	}
	
	/**
	 * Randomly select an empty point from a set of empty points.
	 * 
	 * @param emptyPoints
	 * 
	 * @return coordinate of a move that's legal and not 'verboten',
	 * i.e. does something terrible like filling an own eye.
	 */
	protected int selectRandomMoveCoordinate(PointSet emptyPoints)
	{
		int start;
		int index;
		int nrEmptyPoints = emptyPoints.getSize();
		if (nrEmptyPoints!=0)
		{
			_lastRandomNumber = index = start = RANDOM.nextInt(nrEmptyPoints);
			do
			{
				int xy = emptyPoints.get(index);
				if (!isVerboten(xy) && isLegal(xy))
				{
					return xy;
				}
				if (index++ == nrEmptyPoints)
					index = 0;
			}
			while (index!=start);
		}

		return PASS;
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
		// Check for standard 'eye' definition.
		return (_ownNeighbours[xy]==4 && _otherDiagonalNeighbours[xy]<_maxDiagonalsOccupied[xy]);
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
		board[xy] = _colorToPlay;
		_emptyPoints.remove(xy);
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
			_blackNeighbours[left]++;
			_blackNeighbours[right]++;
			_blackNeighbours[above]++;
			_blackNeighbours[below]++;
			_blackDiagonalNeighbours[left(above)]++;
			_blackDiagonalNeighbours[left(below)]++;
			_blackDiagonalNeighbours[right(above)]++;
			_blackDiagonalNeighbours[right(below)]++;
		}
		else
		{
			_nrWhiteStones++;
			_white[xy] = 1;
			_whiteNeighbours[left]++;
			_whiteNeighbours[right]++;
			_whiteNeighbours[above]++;
			_whiteNeighbours[below]++;
			_whiteDiagonalNeighbours[left(above)]++;
			_whiteDiagonalNeighbours[left(below)]++;
			_whiteDiagonalNeighbours[right(above)]++;
			_whiteDiagonalNeighbours[right(below)]++;			
		}
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
		board[xy] = EMPTY;
		_emptyPoints.add(xy);
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
			_blackNeighbours[left]--;
			_blackNeighbours[right]--;
			_blackNeighbours[above]--;
			_blackNeighbours[below]--;
			_blackDiagonalNeighbours[left(above)]--;
			_blackDiagonalNeighbours[left(below)]--;
			_blackDiagonalNeighbours[right(above)]--;
			_blackDiagonalNeighbours[right(below)]--;
		}
		else
		{
			_nrWhiteStones--;
			_white[xy] = 0;
			_whiteNeighbours[left]--;
			_whiteNeighbours[right]--;
			_whiteNeighbours[above]--;
			_whiteNeighbours[below]--;
			_whiteDiagonalNeighbours[left(above)]--;
			_whiteDiagonalNeighbours[left(below)]--;
			_whiteDiagonalNeighbours[right(above)]--;
			_whiteDiagonalNeighbours[right(below)]--;		
		}
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#getPositionalChecksum()
	 */
	public int getPositionalChecksum()
	{
		return _checksum.getValue();
//		if (_koPoint==UNDEFINED_COORDINATE)
//			return _checksum.getValue();
//		return  _checksum.getValue() + _koPoint;
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
    public double getScore()
    {
    	double score = getScoreEstimate();
    	
    	for (int i=_emptyPoints.getSize(); --i>=0;)
    	{
    		int xy = _emptyPoints.get(i);
    		
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
    	}
    	
    	return score;
    }

    /**
     * @return a very rough estimate of the score, counting just the stones and komi.
     */
    private double getScoreEstimate()
    {
    	return (double)_nrBlackStones - (double)_nrWhiteStones - _komi;
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
    	double score = getScoreEstimate();
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
    		if (_boardModel.get(i)!=EDGE)
    		{
    			assert !(_boardModel.get(i)==EMPTY && markArray[i]==0);
    			assert !(_boardModel.get(i)!=EMPTY && markArray[i]!=0);
    			
    			int blackNeighbours = 0;
    			int whiteNeighbours = 0;
    			for (int n=0; n<4; n++)
    			{
    				int next = FourCursor.getNeighbour(i, n);
    				if (_boardModel.get(next)==BLACK || _boardModel.get(next)==EDGE)
    					blackNeighbours++;
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
    				if (_boardModel.get(next)==BLACK)
    					blackDiagonalNeighbours++;
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
    		if (_boardModel.get(i)==BLACK || _boardModel.get(i)==WHITE)
    		{
    			assert _liberties[_chain[i]]==getLiberties(i) : "Inconsistent liberties at "+getX(i)+","+getY(i)+"\n"+
    				"Countedliberties="+getLiberties(i)+" Recorded liberties="+_liberties[_chain[i]]+"\n"+
    				getBoardModel().toString();
    		}
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
    protected void getPriorityMoves()
    {
    }
    
    protected void addPriorityMove(int xy, int urgency, int visits, int wins)
    {
    	_priorityMoveStack.push(xy);
    	_urgencyStack.push(urgency);
    	_visitStack.push(visits);
    	_winStack.push(wins);
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
    	initBoardModel(size);
    	clear();
    }
    
    public void initBoardModel(int size)
    {
		_boardModel = new DefaultBoardModel(size);
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
    
    /**
     * This is for verification purposes.
     * 
     * @param xy
     * @return
     */
    protected boolean hasLiberty(int xy)
    {
		int stone = xy;
		do
		{
			// Note the checks are redundant, if everything is correct...
			if (_neighbours[stone]!=4)
				return true;

			if (_boardModel.get(left(stone))==EMPTY)
				return true;
			if (_boardModel.get(right(stone))==EMPTY)
				return true;
			if (_boardModel.get(above(stone))==EMPTY)
				return true;
			if (_boardModel.get(below(stone))==EMPTY)
				return true;
			
			stone = _chainNext[stone];
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
    	return SGFUtil.createSGF(getMoveStack()) +"\n\n"+_boardModel.toString();
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
			_visitStack.clear();
			_winStack.clear();
			getPriorityMoves();
			priorityIndex = 0;
			
			_emptyPoints.copyFrom(getEmptyPoints());
			_hasNext = true;
			_color = getColorToMove();
			_boardMarker.getNewMarker();
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
					move.setVisits(_visitStack.get(priorityIndex-1));
					move.setWins(_winStack.get(priorityIndex-1));
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
			_emptyPoints.recycle();
			_iteratorPool.push(this);
		}
	}
}
