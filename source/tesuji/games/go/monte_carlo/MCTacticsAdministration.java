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

import java.util.Enumeration;

import org.apache.log4j.Logger;

import tesuji.core.util.MultiTypeProperties;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.monte_carlo.MonteCarloAdministration;
import tesuji.games.go.tactics.LadderReader;
import tesuji.games.go.tactics.TacticsConstant;
import tesuji.games.go.util.DiagonalCursor;
import tesuji.games.go.util.EightCursor;
import tesuji.games.go.util.FourCursor;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.PointSet;
import tesuji.games.go.util.TwelveCursor;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;
import static tesuji.games.go.util.GoArray.*;

/**
 * 
 */
public class MCTacticsAdministration
	extends MonteCarloGoAdministration
{
	private static Logger _logger = Logger.getLogger(MCTacticsAdministration.class);
	
	protected LadderReader _ladderReader;
		
	private byte[] _row;
	private byte[] _fogOfWar;
	
	private static int _nrPatternsUsed = 0;
	private static int _nrPatternsGenerated = 0;
	
	public static int getNrPatternsUsed() { return _nrPatternsUsed; }
	public static int getNrPatternsGenerated() { return _nrPatternsGenerated; }
	public static void reset() { _nrPatternsUsed = _nrPatternsGenerated = 0; }
	
//	public boolean USE_STONE_AGE = 					true;
//	public boolean FOG_OF_WAR = 					true;		// -RefBot2K=54.2%, MCTS10K=51.8%
//	public boolean NO_FIRST_LINE = 					true;		// -RefBot2K=58.1%, MCTS10K=50.7%?
//	public boolean NO_EMPTY_TRIANGLE = 				false;		// -RefBot2K=51.4%, MCTS10K=?
//	public boolean NO_AUTO_ATARI = 					true;		// -RefBot2K=56.1%, MCTS10K=?
//	public boolean USE_TACTICS_IN_SIMULATION = 		true;		// MCTS50K=88.6%
//	public boolean USE_TACTICS_IN_EXPLORATION = 	true;
//	public boolean IMMEDIATE_ESCAPE_ATARI = 		true;		// RefBot2K=86.6%, MCTS10K=82.2%, MCTS50K=83.8%
//																// RefBot2K=53.3%, MCTS10K=?
//																// 10*1-RefBot2K=59.5%, MCTS10K=?
//	public boolean CAPTURE_LAST_MOVE_IN_ATARI = 	false;		// RefBot2K=62.7%, MCTS10K=?
//	
//	public boolean CAPTURE_LAST_MOVE_IN_LADDER =	true;		// RefBot2K=74.7%, MCTS10K=78.4%, MCTS50K=81.9%
//
//	
//	public boolean ESCAPE_ATARI = 					true;
//	public boolean CAPTURE_STONES_IN_ATARI = 		true;
//	public boolean CAPTURE_STONES_IN_LADDER = 		true;
//	
//	public boolean CAPTURE_STONES = 				true;
//	
	public final int IMMEDIATE_ESCAPE_PRIORITY =	1;
	public final int IMMEDIATE_CAPTURE_PRIORITY =	3;
	public final int IMMEDIATE_LADDER_PRIORITY =	2;
	public final int ESCAPE_PRIORITY =				4;
	public final int CAPTURE_PRIORITY =				5;
	public final int LADDER_PRIORITY =				6;
	public final int IMMEDIATE_VISITS =				5;
	public final int IMMEDIATE_WINS =				5;
//	
//	public boolean USE_HARD_PATTERNS = 				false;
//	public boolean SEPARATE_PATTERN = 				true;
//	public boolean CUT_PATTERN = 					true;
//	public boolean TOBI_CUT_PATTERN = 				false;
//	public boolean KEIMA_CUT_PATTERN = 				true; // MCTS-2K=55%
//	public boolean TOBI_CONNECT_PATTERN = 			false;
//	public boolean FIRST_LINE_ATARI_PATTERN = 		false;
//	public boolean SECOND_LINE_ATARI_PATTERN = 		false;
//	public boolean FIRST_LINE_BLOCK_PATTERN = 		false;
//	public boolean SECOND_LINE_BLOCK_PATTERN = 		false;
//	public boolean EYE_PATTERN = 					false;
	
	private MultiTypeProperties _properties;
	
	public MCTacticsAdministration()
	{
		super();
		initProperties();
	}

	protected MCTacticsAdministration(int boardSize)
	{
		super(boardSize);
		
		initProperties();
		_ladderReader = new LadderReader(boardSize);
		createFogOfWar();
	}
	
	private void initProperties()
	{
		_properties = new MultiTypeProperties();
		_properties.setBooleanProperty("USE_STONE_AGE", true);
		_properties.setBooleanProperty("FOG_OF_WAR", true);
		_properties.setBooleanProperty("NO_FIRST_LINE", true);
		_properties.setBooleanProperty("NO_AUTO_ATARI", true);
		_properties.setBooleanProperty("USE_TACTICS_IN_SIMULATION", true);
		_properties.setBooleanProperty("USE_TACTICS_IN_EXPLORATION", true);
		_properties.setBooleanProperty("IMMEDIATE_ESCAPE_ATARI", true);
		_properties.setBooleanProperty("CAPTURE_LAST_MOVE_IN_LADDER", true);
		_properties.setBooleanProperty("ESCAPE_ATARI", true);
		_properties.setBooleanProperty("CAPTURE_STONES_IN_ATARI", true);
		_properties.setBooleanProperty("CAPTURE_STONES_IN_LADDER", true);
		_properties.setBooleanProperty("CAPTURE_STONES", true);
		_properties.setBooleanProperty("SEPARATE_PATTERN", true);
	}

	@Override
	public void clear()
	{
		super.clear();

		_logger.info("MC_TEST_VERSION = "+isTestVersion());
//		_logger.info("FOG_OF_WAR = "+FOG_OF_WAR);
//		_logger.info("NO_FIRST_LINE = "+NO_FIRST_LINE);
//		_logger.info("NO_EMPTY_TRIANGLE = "+NO_EMPTY_TRIANGLE);
//		_logger.info("NO_AUTO_ATARI = "+NO_AUTO_ATARI);
//		_logger.info("USE_TACTICS_IN_SIMULATION = "+USE_TACTICS_IN_SIMULATION);
//		_logger.info("USE_TACTICS_IN_EXPLORATION = "+USE_TACTICS_IN_EXPLORATION);
		_logger.info("USE_HARD_PATTERNS = "+useHardPatterns());

		createFogOfWar();
	}
	
	private void createFogOfWar()
	{
		if (useFogOfWar())
		{
			_fogOfWar = createBytes();
			_row = createRowArray(getBoardSize());
			for (int i=FIRST; i<=LAST; i++)
			{
				if (_row[i]<3)
					_fogOfWar[i] = Byte.MAX_VALUE;
			}
		}
	}
	
	private boolean useFogOfWar()
	{
		return isFOG_OF_WAR();
	}

	private boolean useHardPatterns()
	{
		return isUSE_HARD_PATTERNS();
	}

	@Override
	public void copyDataFrom(MonteCarloAdministration<GoMove> sourceAdmin)
	{
		super.copyDataFrom(sourceAdmin);
		
		MCTacticsAdministration source = (MCTacticsAdministration) sourceAdmin;
		for (Enumeration e = source._properties.propertyNames(); e.hasMoreElements();)
		{
			String key = (String)e.nextElement();
			Object value = source._properties.get(key);
			_properties.put(key, value);
		}
		if (useFogOfWar())
		{
			copy(source._fogOfWar,_fogOfWar);
		}
	}

	@Override
	public void setBoardSize(int size)
	{
		_row = createRowArray(size);
		super.setBoardSize(size);
		_ladderReader = new LadderReader(size);
	}
	
	@Override
	public void play(int xy)
	{
		super.play(xy);
		
		if (useFogOfWar())
			removeFogOfWar(xy);
	}
  
    @Override
	public boolean isVerboten(int xy)
	{
		// Eye check
		if (_ownNeighbours[xy]==4 && _otherDiagonalNeighbours[xy]<_maxDiagonalsOccupied[xy])
			return true;
		
		if (isNO_EMPTY_TRIANGLE())
		{
			if (_inPlayout && _neighbours[xy]==2 && _boardModel.get(left(xy))!=_boardModel.get(right(xy)))
			{
				if (_ownNeighbours[xy]==2 && _otherDiagonalNeighbours[xy]==0)
					return true;
				if (_otherNeighbours[xy]==2 && _ownDiagonalNeighbours[xy]==0)
					return true;
			}
		}
		
		if (useFogOfWar())
			if (_fogOfWar[xy]!=0)
				return true;

		if (isNO_FIRST_LINE())
		{
			if (_inPlayout && _row[xy]==1 && _neighbours[xy]==1 && _blackDiagonalNeighbours[xy]==0 && _whiteDiagonalNeighbours[xy]==0)
				return true;
		}
		
		if (isNO_AUTO_ATARI())
		{
			if (_inPlayout)
			{
				// Auto-atari check.
				if (_neighbours[xy]==3)
				{
					boolean ownAtari = false;
					boolean otherAtari = false;
					boolean otherPreAtari = false;
					for (int n=0; n<4; n++)
					{
						int next = FourCursor.getNeighbour(xy, n);
						byte stone = _boardModel.get(next);
						if (stone!=EMPTY && stone!=EDGE)
						{
							int liberties = _liberties[_chain[next]];
							if (liberties<3)
							{
								if (stone==_colorToPlay)
									ownAtari = true;
								else
									otherAtari = true;
							}
							else if (liberties==3 && stone==_oppositeColor)
								otherPreAtari = true;
						}
					}
					if (!otherAtari && !otherPreAtari && _otherNeighbours[xy]==3 && _ownDiagonalNeighbours[xy]<_maxDiagonalsOccupied[xy])
						return true;
					if (!ownAtari && _ownNeighbours[xy]==3 && _otherDiagonalNeighbours[xy]<_maxDiagonalsOccupied[xy])
						return true;
		
					if (isAutoAtari(xy))
						return true;
				}
				else if (_neighbours[xy]==4 && isAutoAtari(xy))
					return true;
			}
		}
		
		return false;
	}

	private boolean isAutoAtari(int xy)
	{
		if (_koPoint!=UNDEFINED_COORDINATE && _liberties[_chain[_previousMove]]==1)
			return false;
		
//		if (!isPrehistoric(_chain[left(xy)]) && !isPrehistoric(_chain[right(xy)])
//				&& !isPrehistoric(_chain[above(xy)]) && !isPrehistoric(_chain[below(xy)]))
//			return false;
		
		int nLib = 4-_neighbours[xy];
		int nStones = 0;
		boolean hasAtari = false;
		boolean hasCapture = false;
		int libertyLocation = UNDEFINED_COORDINATE;
		
		boolean hasFalsePoint = isFalsePoint(xy,_oppositeColor);

		for (int n=4; --n>=0;)
		{
			int next = FourCursor.getNeighbour(xy, n);
			byte boardValue = _boardModel.get(next);
			if (boardValue==_colorToPlay)
			{
				int stone = next;
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
					
					if (!hasFalsePoint && isFalsePoint(stone,_oppositeColor))
						hasFalsePoint = true;					

					stone = _chainNext[stone];
				}
				while (stone!=next);
			}
			else if (boardValue==_oppositeColor)
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
			if (nStones==1 && libertyLocation!=UNDEFINED_COORDINATE
					&& _otherNeighbours[xy]==3 && _ownNeighbours[libertyLocation]>=3)
				return true;
			if (_neighbours[xy]==4 && !hasCapture && !hasAtari && _otherNeighbours[xy]==3)
				return true;
		}
		
		return false;
	}

	@Override
	protected int selectSimulationMove(PointSet emptyPoints)
	{
		int priorityMove = selectSimulationPriorityMove();
		if (priorityMove!=PASS && priorityMove!=UNDEFINED_COORDINATE)
			return priorityMove;
		
		return super.selectSimulationMove(emptyPoints);
	}
	
	protected int selectSimulationPriorityMove()
	{
		if (_previousMove!=PASS)
		{
			if (isUSE_TACTICS_IN_SIMULATION())
			{
//				if (!isTestVersion() || _lastRandomNumber<(_boardSize*_boardSize)/2+_boardSize)
				{
					_boardMarker.getNewMarker();
					int tacticalXY = getTacticalMove(_previousMove);
					if (tacticalXY!=UNDEFINED_COORDINATE && isLegal(tacticalXY))
						return tacticalXY;
				}
			}
			if (useHardPatterns())
			{
				int patternXY = getPatternMove(_previousMove);
//				int patternXY = getPatternMove();
				if (patternXY!=UNDEFINED_COORDINATE && isLegal(patternXY))
				{
					_nrPatternsGenerated++;
					if (RANDOM.nextInt(2+GoArray.getDistance(_previousMove, patternXY)/2)==0)
					{
						_nrPatternsUsed++;
						return patternXY;
					}
				}
			}
			if (isCAPTURE_STONES())
			{
				int random = RANDOM.nextInt(64);
				if ((random&7)==0)
				{
					int nrStones = random/8+1;
					for (int i=FIRST; i<=LAST; i++)
					{
						if (_boardModel.get(i)==_oppositeColor && _liberties[_chain[i]]==1 && getNrStones(i)>=nrStones)
						{
							int captureXY = getLiberty(i);
							if (isLegal(captureXY))
								return captureXY;
						}
					}
				}
			}
		}
		return UNDEFINED_COORDINATE;
	}
	
	@Override
	protected void getPriorityMoves()
	{
		if (isUSE_TACTICS_IN_EXPLORATION())
			getTacticalPriorityMoves();
	}
	
	public int getTacticalMove(int previousMove)
	{
		if (isIMMEDIATE_ESCAPE_ATARI())
		{
			for (int n=0; n<4; n++)
			{
				int next = FourCursor.getNeighbour(previousMove, n);
				if (_boardModel.get(next)==_colorToPlay)
				{
					int chainNext = _chain[next];
					if (_boardMarker.notSet(chainNext))
					{
						if (_liberties[chainNext]==1 && isPrehistoric(chainNext))
						{
							_boardMarker.set(chainNext);
							_ladderReader.setBoardArray(_boardModel.getSingleArray());
							_ladderReader.setKoPoint(_koPoint);
							if (_ladderReader.tryEscape(next)==TacticsConstant.CANNOT_CATCH)
							{
								int escapeXY = _ladderReader.getLastLadderMove();
								if (escapeXY!=PASS && escapeXY!=UNDEFINED_COORDINATE)
									return escapeXY;
							}
						}
					}
				}
			}
		}
		if (isCAPTURE_LAST_MOVE_IN_ATARI())
		{
			int chain = _chain[previousMove];
			if (_liberties[chain]==1  && _boardMarker.notSet(chain) && isPrehistoric(chain))
			{
				_boardMarker.set(chain);
				_ladderReader.setBoardArray(_boardModel.getSingleArray());
				_ladderReader.setKoPoint(_koPoint);
				if (_ladderReader.tryEscape(previousMove)==TacticsConstant.CANNOT_CATCH)
				{
					int captureXY = getLiberty(previousMove);
					if (isLegal(captureXY))
						return captureXY;
				}
			}
		}
		if (isCAPTURE_LAST_MOVE_IN_LADDER())
		{
			int chain = _chain[previousMove];
			if (_liberties[chain]==2 && _boardMarker.notSet(chain) && isPrehistoric(chain)
				&& (_row[previousMove]>1 || _ownDiagonalNeighbours[previousMove]>0 || _otherDiagonalNeighbours[previousMove]>0))
			{
				_boardMarker.set(chain);
				_ladderReader.setBoardArray(_boardModel.getSingleArray());
				_ladderReader.setKoPoint(_koPoint);
				if (_ladderReader.tryLadder(previousMove)==TacticsConstant.CAN_CATCH)
				{
					int ladderXY = _ladderReader.getLastLadderMove();
					if (isLegal(ladderXY))
						return ladderXY;
				}
			}
		}

		return UNDEFINED_COORDINATE;
	}
	
	public void getTacticalPriorityMoves()
	{
		if (_previousMove!=PASS)
		{
			if (isIMMEDIATE_ESCAPE_ATARI())
			{
				for (int n=0; n<4; n++)
				{
					int next = FourCursor.getNeighbour(_previousMove, n);
					if (_boardModel.get(next)==_colorToPlay && _liberties[_chain[next]]==1)
					{
						_ladderReader.setBoardArray(_boardModel.getSingleArray());
						_ladderReader.setKoPoint(_koPoint);
						if (_ladderReader.tryEscape(next)==TacticsConstant.CANNOT_CATCH)
						{
							int escapeXY = _ladderReader.getLastLadderMove();
							if (escapeXY!=PASS && escapeXY!=UNDEFINED_COORDINATE && isLegal(escapeXY))
							{
								addPriorityMove(escapeXY, IMMEDIATE_ESCAPE_PRIORITY, IMMEDIATE_VISITS, IMMEDIATE_WINS);
							}
						}
					}
				}
			}
			if (isCAPTURE_LAST_MOVE_IN_ATARI())
			{
				if (_liberties[_chain[_previousMove]]==1)
				{
					_ladderReader.setBoardArray(_boardModel.getSingleArray());
					_ladderReader.setKoPoint(_koPoint);
					if (_ladderReader.tryEscape(_previousMove)==TacticsConstant.CANNOT_CATCH)
					{
						int captureXY = getLiberty(_previousMove);
						if (isLegal(captureXY))
						{
							addPriorityMove(captureXY, IMMEDIATE_CAPTURE_PRIORITY, IMMEDIATE_VISITS, IMMEDIATE_WINS);
						}
					}
				}
			}
			if (isCAPTURE_LAST_MOVE_IN_LADDER())
			{
				if (_liberties[_chain[_previousMove]]==2)
				{
					_ladderReader.setBoardArray(_boardModel.getSingleArray());
					_ladderReader.setKoPoint(_koPoint);
					if (_ladderReader.tryLadder(_previousMove)==TacticsConstant.CAN_CATCH)
					{
						int ladderXY = _ladderReader.getLastLadderMove();
						addPriorityMove(ladderXY, IMMEDIATE_LADDER_PRIORITY, IMMEDIATE_VISITS, IMMEDIATE_WINS);
					}
				}
			}
		}
		// TODO - instead of looping over the whole board it's probably faster to keep lists with chains with 1 and 2 liberties.
		_boardMarker.getNewMarker();
		for (int i=FIRST; i<=LAST; i++)
		{
			int chain = _chain[i];
			if (i!=_previousMove && !isNeighbour(i,_previousMove) && _boardMarker.notSet(chain))
			{
				_boardMarker.set(chain);
				byte boardValue = _boardModel.get(i);
				if (isESCAPE_ATARI())
				{
					if (boardValue==_colorToPlay && _liberties[chain]==1)
					{
						_ladderReader.setBoardArray(_boardModel.getSingleArray());
						_ladderReader.setKoPoint(_koPoint);
						if (_ladderReader.tryEscape(i)==TacticsConstant.CANNOT_CATCH)
						{
							int escapeXY = _ladderReader.getLastLadderMove();
							if (isLegal(escapeXY))
							{
								addPriorityMove(escapeXY, ESCAPE_PRIORITY, IMMEDIATE_VISITS, IMMEDIATE_WINS);
							}
						}
					}
				}
				if (isCAPTURE_STONES_IN_ATARI())
				{
					if (boardValue==_oppositeColor && _liberties[chain]==1)
					{
						_ladderReader.setBoardArray(_boardModel.getSingleArray());
						_ladderReader.setKoPoint(_koPoint);
						if (_ladderReader.tryEscape(i)==TacticsConstant.CANNOT_CATCH)
						{
							int captureXY = getLiberty(i);
							if (isLegal(captureXY))
							{
								addPriorityMove(captureXY, CAPTURE_PRIORITY, IMMEDIATE_VISITS, IMMEDIATE_WINS);
							}
						}
					}
				}
				if (isCAPTURE_STONES_IN_LADDER())
				{
					if (boardValue==_oppositeColor && _liberties[chain]==2)
					{
						_ladderReader.setBoardArray(_boardModel.getSingleArray());
						_ladderReader.setKoPoint(_koPoint);
						if (_ladderReader.tryLadder(i)==TacticsConstant.CAN_CATCH)
						{
							int ladderXY = _ladderReader.getLastLadderMove();
							addPriorityMove(ladderXY, LADDER_PRIORITY, IMMEDIATE_VISITS, IMMEDIATE_WINS);
						}
					}
				}
			}
		}
	}
	
	/*
	private int captureNeighbour(int xy)
	{
		int stone = xy;
		do
		{
			for (int n=0; n<4; n++)
			{
				int next = FourCursor.getNeighbour(stone, n);
				if (_boardModel.get(next)==_oppositeColor && _liberties[_chain[next]]==2 && !_marker.isSet(_chain[next]))
				{
					_marker.set(_chain[next]);
					_ladderReader.setKoPoint(_koPoint);
					if (_ladderReader.tryLadder(next)==TacticsConstant.CAN_CATCH)
						return _ladderReader.getLastLadderMove();
				}
			}
			stone = _chainNext[stone];
		}
		while (stone!=xy);
		
		return UNDEFINED_COORDINATE;
	}*/
	
	protected int getLiberty(int xy)
	{
		int stone = xy;
		do
		{
			if (_boardModel.get(left(stone))==EMPTY)
				return left(stone);
			if (_boardModel.get(right(stone))==EMPTY)
				return right(stone);
			if (_boardModel.get(above(stone))==EMPTY)
				return above(stone);
			if (_boardModel.get(below(stone))==EMPTY)
				return below(stone);
			
			stone = _chainNext[stone];
		}
		while (stone!=xy);
  	
		return 0;
	}   

	protected int getNrStones(int xy)
	{
		int nrStones = 0;
		int stone = xy;
		do
		{
			nrStones++;
			stone = _chainNext[stone];
		}
		while (stone!=xy);
  	
		return nrStones;
  	}    

	protected boolean isFalsePoint(int xy, byte color)
    {
	    if (_boardModel.get(left(xy))==color
						&& (_boardModel.get(left(above(xy)))==opposite(color) || _boardModel.get(left(above(xy)))==EDGE)
						&& (_boardModel.get(left(below(xy)))==opposite(color) || _boardModel.get(left(below(xy)))==EDGE))
			return true;
		if (_boardModel.get(right(xy))==color
						&& (_boardModel.get(right(above(xy)))==opposite(color) || _boardModel.get(right(above(xy)))==EDGE)
						&& (_boardModel.get(right(below(xy)))==opposite(color) || _boardModel.get(right(below(xy)))==EDGE))
			return true;
		if (_boardModel.get(above(xy))==color
						&& (_boardModel.get(above(left(xy)))==opposite(color) || _boardModel.get(above(left(xy)))==EDGE)
						&& (_boardModel.get(above(right(xy)))==opposite(color) || _boardModel.get(above(right(xy)))==EDGE))
			return true;
		if (_boardModel.get(below(xy))==color
						&& (_boardModel.get(below(left(xy)))==opposite(color) || _boardModel.get(below(left(xy)))==EDGE)
						&& (_boardModel.get(below(right(xy)))==opposite(color) || _boardModel.get(below(right(xy)))==EDGE))
			return true;
		return false;
    }

	private void removeFogOfWar(int xy)
	{
		_fogOfWar[xy] = 0;
		for (int n=12; --n>=0;)
		{
			int next = TwelveCursor.getNeighbour(xy, n);
			if (next>=FIRST && next<=LAST)
				_fogOfWar[next] = 0;
		}
	}
	
	@Override
	public MonteCarloAdministration<GoMove> createClone()
	{
		MCTacticsAdministration clone = new MCTacticsAdministration(getBoardSize());
		clone.setIsTestVersion(isTestVersion());
		clone.copyDataFrom(this);
		
		return clone;
	}	
	
	public int getPatternMove(int previousMove)
	{
		if (previousMove<=0)
			return UNDEFINED_COORDINATE;
		
		int[] _next = new int[8];
		for (int n=0; n<8; n++)
		{
			_next[n] = EightCursor.getNeighbour(previousMove, n);
			if (_boardModel.get(_next[n])!=EMPTY)
				_next[n] = 0;
		}

		if (isEYE_PATTERN())
		{
			for (int n=0; n<8; n++)
			{
				int xy = getDiagonalHalfEye(_next[n]);
				if (xy!=0 && isLegal(xy) && !isAutoAtari(xy))
					return xy;
				xy = getHalfEye(_next[n]);
				if (xy!=0 && isLegal(xy))
					return xy;
			}
		}
//		if (_lastRandomNumber<(_boardSize*_boardSize)/2+_boardSize)
		{
			if (isSEPARATE_PATTERN())
				for (int n=0; n<4; n++)
					if (isCross(_next[n]))
						return _next[n];
			if (isCUT_PATTERN())
			{
				for (int n=0; n<4; n++)
					if (isCut(_next[n],_oppositeColor))
						return _next[n];
				for (int n=0; n<4; n++)
					if (isCut(_next[n],_colorToPlay))
						return _next[n];
			}
		}
			
//		if (_lastRandomNumber<(_boardSize*_boardSize)/4)
		{
			if (isTOBI_CUT_PATTERN())
			{
				for (int n=0; n<4; n++)
					if (isTobiCut(_next[n],_oppositeColor))
						return _next[n];
				for (int n=4; n<8; n++)
					if (isTobiCut(_next[n],_colorToPlay))
						return _next[n];
			}
	
			if (isKEIMA_CUT_PATTERN())
			{
				for (int n=0; n<8; n++)
					if (isKeimaPush(_next[n],_oppositeColor))
						return _next[n];
				for (int n=0; n<4; n++)
					if (isKeimaPush(_next[n],_colorToPlay))
						return _next[n];
			}
			
			if (isTOBI_CONNECT_PATTERN())
			{
				for (int n=0; n<4; n++)
					if (isTobiConnect(_next[n],_oppositeColor))
						return _next[n];
				for (int n=0; n<4; n++)
					if (isTobiConnect(_next[n],_colorToPlay))
						return _next[n];
			}
			
			if (isFIRST_LINE_ATARI_PATTERN())
				for (int n=0; n<4; n++)
					if (isFirstLineAtari(_next[n]))
						return _next[n];
			
			if (isSECOND_LINE_ATARI_PATTERN())
				for (int n=0; n<4; n++)
					if (isSecondLineAtari(_next[n]))
						return _next[n];
		
			if (isFIRST_LINE_BLOCK_PATTERN())
				for (int n=0; n<4; n++)
					if (isFirstLineBlock(_next[n]))
						return _next[n];
			if (isSECOND_LINE_BLOCK_PATTERN())
				for (int n=0; n<4; n++)
					if (isSecondLineBlock(_next[n]))
						return _next[n];
//
//		for (int n=4; n<8; n++)
//			if (isDiagonalConnect(_next[n]))
//				return _next[n];
		}
		
		return UNDEFINED_COORDINATE;
	}
	
	public int getPatternMove()
	{
		int size = _emptyPoints.getSize();
		for (int i=0; i<size; i++)
		{
			int xy = _emptyPoints.get(i);
			
			if (isSEPARATE_PATTERN())
				if (isCross(xy))
					if (isOK(xy))
						return xy;
			if (isCUT_PATTERN())
			{
				if (isCut(xy,_oppositeColor))
					if (isOK(xy))
						return xy;
				if (isCut(xy,_colorToPlay))
					if (isOK(xy))
						return xy;
			}
			if (isKEIMA_CUT_PATTERN())
			{
				if (isKeimaPush(xy,_oppositeColor))
					if (isOK(xy))
						return xy;
				if (isKeimaPush(xy,_colorToPlay))
					if (isOK(xy))
						return xy;
			}
			
			if (isTOBI_CUT_PATTERN())
			{
				if (isTobiCut(xy,_oppositeColor))
					if (isOK(xy))
						return xy;
				if (isTobiCut(xy,_colorToPlay))
					if (isOK(xy))
						return xy;
			}
			
			if (isTOBI_CONNECT_PATTERN())
			{
				if (isTobiConnect(xy,_oppositeColor))
					if (isOK(xy))
						return xy;
				if (isTobiConnect(xy,_colorToPlay))
					if (isOK(xy))
						return xy;
			}
			
			if (isFIRST_LINE_ATARI_PATTERN())
			{
				if (isFirstLineAtari(xy))
					if (isOK(xy))
						return xy;
			}
			
			if (isSECOND_LINE_ATARI_PATTERN())
			{
				if (isSecondLineAtari(xy))
					if (isOK(xy))
						return xy;
			}
		
			if (isFIRST_LINE_BLOCK_PATTERN())
			{
				if (isFirstLineBlock(xy))
					if (isOK(xy))
						return xy;
			}
			if (isSECOND_LINE_BLOCK_PATTERN())
			{
				if (isSecondLineBlock(xy))
					if (isOK(xy))
						return xy;
			}
		}
		return UNDEFINED_COORDINATE;
	}
	
	private boolean isOK(int xy)
	{
		return true;
//		return (RANDOM.nextInt(2+_emptyPoints.getSize()/5)==0 && isSafeToMove(xy));
	}

	private boolean isCross(int xy)
	{
		if (xy==0)
			return false;
		
		return (_ownNeighbours[xy]>=2 && _otherNeighbours[xy]>=2 
				&& (_boardModel.get(left(xy))==_boardModel.get(right(xy)) || _boardModel.get(above(xy))==_boardModel.get(below(xy))));
	}
	
	private boolean isCut(int xy, byte color)
	{
		if (xy==0 || _blackNeighbours[xy]==0 || _whiteNeighbours[xy]==0)
			return false;
		
		byte otherColor = opposite(color);
		if (_boardModel.get(left(xy))==otherColor && _boardModel.get(above(xy))==otherColor && _boardModel.get(left(above(xy)))==color 
				&& _liberties[_chain[left(above(xy))]]!=1 && (_boardModel.get(right(xy))==color || _boardModel.get(below(xy))==color))
			return true;
		if (_boardModel.get(left(xy))==otherColor && _boardModel.get(below(xy))==otherColor && _boardModel.get(left(below(xy)))==color
			&& _liberties[_chain[left(below(xy))]]!=1 && (_boardModel.get(right(xy))==color || _boardModel.get(above(xy))==color))
			return true;
		if (_boardModel.get(right(xy))==otherColor && _boardModel.get(above(xy))==otherColor && _boardModel.get(right(above(xy)))==color
			&& _liberties[_chain[right(above(xy))]]!=1 && (_boardModel.get(left(xy))==color || _boardModel.get(below(xy))==color))
			return true;
		if (_boardModel.get(right(xy))==otherColor && _boardModel.get(below(xy))==otherColor && _boardModel.get(right(below(xy)))==color
			&& _liberties[_chain[right(below(xy))]]!=1 && (_boardModel.get(left(xy))==color || _boardModel.get(above(xy))==color))
			return true;
		
		return false;
	}

	private boolean isTobiCut(int xy, byte color)
	{
		if (xy==0 ||  _neighbours[xy]>1 || (_neighbours[xy]==2 && _row[xy]!=1))
			return false;
		
		byte otherColor = opposite(color);
		if (_boardModel.get(left(xy))==color && _boardModel.get(left(above(xy)))==otherColor && _boardModel.get(left(below(xy)))==otherColor
						&& _liberties[_chain[left(xy)]]!=1)
			return true;
		if (_boardModel.get(right(xy))==color && _boardModel.get(right(above(xy)))==otherColor && _boardModel.get(right(below(xy)))==otherColor
						&& _liberties[_chain[right(xy)]]!=1)
			return true;
		if (_boardModel.get(above(xy))==color && _boardModel.get(above(left(xy)))==otherColor && _boardModel.get(above(right(xy)))==otherColor
						&& _liberties[_chain[above(xy)]]!=1)
			return true;
		if (_boardModel.get(below(xy))==color && _boardModel.get(below(left(xy)))==otherColor && _boardModel.get(below(right(xy)))==otherColor
						&& _liberties[_chain[below(xy)]]!=1)
			return true;
		
		return false;
	}
	
	private boolean isKeimaPush(int xy, byte color)
	{
		if (xy==0 ||  _neighbours[xy]>3 || (_neighbours[xy]==3 && _row[xy]!=1))
			return false;
		
		byte otherColor = opposite(color);
		if (_boardModel.get(left(xy))==color && _boardModel.get(above(xy))==otherColor && _boardModel.get(left(below(xy)))==otherColor
			&& _liberties[_chain[left(xy)]]!=1)
			return true;
		if (_boardModel.get(left(xy))==color && _boardModel.get(below(xy))==otherColor && _boardModel.get(left(above(xy)))==otherColor
						&& _liberties[_chain[left(xy)]]!=1)
			return true;
		if (_boardModel.get(right(xy))==color && _boardModel.get(above(xy))==otherColor && _boardModel.get(right(below(xy)))==otherColor
						&& _liberties[_chain[right(xy)]]!=1)
			return true;
		if (_boardModel.get(right(xy))==color && _boardModel.get(below(xy))==otherColor && _boardModel.get(right(above(xy)))==otherColor
						&& _liberties[_chain[right(xy)]]!=1)
			return true;
		if (_boardModel.get(above(xy))==color && _boardModel.get(left(xy))==otherColor && _boardModel.get(above(right(xy)))==otherColor
						&& _liberties[_chain[above(xy)]]!=1)
			return true;
		if (_boardModel.get(above(xy))==color && _boardModel.get(right(xy))==otherColor && _boardModel.get(above(left(xy)))==otherColor
						&& _liberties[_chain[above(xy)]]!=1)
			return true;
		if (_boardModel.get(below(xy))==color && _boardModel.get(left(xy))==otherColor && _boardModel.get(below(right(xy)))==otherColor
						&& _liberties[_chain[below(xy)]]!=1)
			return true;
		if (_boardModel.get(below(xy))==color && _boardModel.get(right(xy))==otherColor && _boardModel.get(below(left(xy)))==otherColor
						&& _liberties[_chain[below(xy)]]!=1)
			return true;

		return false;
	}
	
	
	private boolean isTobiConnect(int xy, byte color)
	{
		if (xy==0)
			return false;
		
		if (color==BLACK && _whiteNeighbours[xy]!=2)
			return false;
		if (color==WHITE && _blackNeighbours[xy]!=2)
			return false;

		if (_boardModel.get(left(xy))==color && _liberties[_chain[left(xy)]]>2 && _boardModel.get(right(xy))==EMPTY
				&& _boardModel.get(right(above(xy)))==EMPTY && _boardModel.get(right(below(xy)))==EMPTY)
			return true;
		if (_boardModel.get(right(xy))==color && _liberties[_chain[right(xy)]]>2 && _boardModel.get(left(xy))==EMPTY
				&& _boardModel.get(left(above(xy)))==EMPTY && _boardModel.get(left(below(xy)))==EMPTY)
			return true;
		if (_boardModel.get(above(xy))==color && _liberties[_chain[above(xy)]]>2 && _boardModel.get(below(xy))==EMPTY
				&& _boardModel.get(below(left(xy)))==EMPTY && _boardModel.get(below(right(xy)))==EMPTY)
			return true;
		if (_boardModel.get(below(xy))==color && _liberties[_chain[below(xy)]]>2 && _boardModel.get(above(xy))==EMPTY
				&& _boardModel.get(above(left(xy)))==EMPTY && _boardModel.get(above(right(xy)))==EMPTY)
			return true;
		
		return false;
	}
	
	private boolean isFirstLineAtari(int xy)
	{
		if (xy==0 || _row[xy]!=1 || _otherNeighbours[xy]!=2 || _neighbours[xy]>3 || _ownDiagonalNeighbours[xy]==0)
			return false;
		
		if (_row[left(xy)]==1 && _boardModel.get(left(xy))==_oppositeColor 
						&& _liberties[_chain[left(xy)]]==2 && _boardModel.get(right(xy))==EMPTY )
			return true;
		if (_row[right(xy)]==1 && _boardModel.get(right(xy))==_oppositeColor
						&& _liberties[_chain[right(xy)]]==2 && _boardModel.get(left(xy))==EMPTY )
			return true;
		if (_row[above(xy)]==1 && _boardModel.get(above(xy))==_oppositeColor 
						&& _liberties[_chain[above(xy)]]==2 && _boardModel.get(below(xy))==EMPTY )
			return true;
		if (_row[below(xy)]==1 && _boardModel.get(below(xy))==_oppositeColor 
						&& _liberties[_chain[below(xy)]]==2 && _boardModel.get(above(xy))==EMPTY )
			return true;

		return false;
	}

	private boolean isSecondLineAtari(int xy)
	{
		if (xy==0 || _row[xy]!=2 || _neighbours[xy]!=1)
			return false;
		
		if (_row[left(xy)]==2 && _boardModel.get(left(xy))==_oppositeColor && _liberties[_chain[left(xy)]]==2)
			return true;
		if (_row[right(xy)]==2 && _boardModel.get(right(xy))==_oppositeColor && _liberties[_chain[right(xy)]]==2)
			return true;
		if (_row[above(xy)]==2 && _boardModel.get(above(xy))==_oppositeColor && _liberties[_chain[above(xy)]]==2)
			return true;
		if (_row[below(xy)]==2 && _boardModel.get(below(xy))==_oppositeColor && _liberties[_chain[below(xy)]]==2)
			return true;

		return false;
	}
	
	private boolean isFirstLineBlock(int xy)
	{
		if (xy==0 || _row[xy]!=1 || _otherNeighbours[xy]!=2 || _neighbours[xy]>3)
			return false;
		
		if (_row[left(xy)]==1 && _boardModel.get(left(xy))==_oppositeColor 
				&& _boardModel.get(right(above(xy)))!=_colorToPlay && _boardModel.get(right(below(xy)))!=_colorToPlay)
			return true;
		if (_row[right(xy)]==1 && _boardModel.get(right(xy))==_oppositeColor 
				&& _boardModel.get(left(above(xy)))!=_colorToPlay && _boardModel.get(left(below(xy)))!=_colorToPlay)
			return true;
		if (_row[above(xy)]==1 && _boardModel.get(above(xy))==_oppositeColor 
				&& _boardModel.get(below(left(xy)))!=_colorToPlay && _boardModel.get(below(right(xy)))!=_colorToPlay)
			return true;
		if (_row[below(xy)]==1 && _boardModel.get(below(xy))==_oppositeColor 
				&& _boardModel.get(above(left(xy)))!=_colorToPlay && _boardModel.get(above(left(xy)))!=_colorToPlay)
			return true;

		return false;
	}
	
	private boolean isSecondLineBlock(int xy)
	{
		if (xy==0 || _row[xy]!=2 || _otherNeighbours[xy]!=1 || _ownNeighbours[xy]!=1)
			return false;
		
		if (_row[left(xy)]==2 && _boardModel.get(left(xy))==_oppositeColor 
				&& _boardModel.get(right(above(xy)))!=_colorToPlay && _boardModel.get(right(below(xy)))!=_colorToPlay)
			return true;
		if (_row[right(xy)]==2 && _boardModel.get(right(xy))==_oppositeColor 
				&& _boardModel.get(left(above(xy)))!=_colorToPlay && _boardModel.get(left(below(xy)))!=_colorToPlay)
			return true;
		if (_row[above(xy)]==2 && _boardModel.get(above(xy))==_oppositeColor 
				&& _boardModel.get(below(left(xy)))!=_colorToPlay && _boardModel.get(below(right(xy)))!=_colorToPlay)
			return true;
		if (_row[below(xy)]==2 && _boardModel.get(below(xy))==_oppositeColor 
				&& _boardModel.get(above(left(xy)))!=_colorToPlay && _boardModel.get(above(left(xy)))!=_colorToPlay)
			return true;

		return false;
	}

	private int getDiagonalHalfEye(int xy)
	{
		if (xy==0 || _otherNeighbours[xy]!=4)
			return 0;
		
		int edge=0;
		int own=0;
		int other=0;
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
			else
				other++;
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
	
	private int getHalfEye(int xy)
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
	
    protected boolean isSafeToMove(int moveXY)
    {
		return (_ladderReader.wouldBeLadder(moveXY,BLACK)==TacticsConstant.CANNOT_CATCH
			&& _ladderReader.wouldBeLadder(moveXY,WHITE)==TacticsConstant.CANNOT_CATCH);
    }
    
    private boolean isPrehistoric(int chain)
    {
    	return (!isUSE_STONE_AGE() || _stoneAge[chain]<=_playoutStart);
    }

	public boolean isUSE_STONE_AGE()
    {
		return _properties.getBooleanProperty("USE_STONE_AGE");
    	//return USE_STONE_AGE;
    }

	public void setUSE_STONE_AGE(boolean uSE_STONE_AGE)
    {
		_properties.setBooleanProperty("USE_STONE_AGE",uSE_STONE_AGE);
    	//USE_STONE_AGE = uSE_STONE_AGE;
    }

	public boolean isFOG_OF_WAR()
    {
		return _properties.getBooleanProperty("FOG_OF_WAR");
    	//return FOG_OF_WAR;
    }

	public void setFOG_OF_WAR(boolean fOG_OF_WAR)
    {
		_properties.setBooleanProperty("FOG_OF_WAR",fOG_OF_WAR);
    	//FOG_OF_WAR = fOG_OF_WAR;
    }

	public boolean isNO_FIRST_LINE()
    {
		return _properties.getBooleanProperty("NO_FIRST_LINE");
    	//return NO_FIRST_LINE;
    }

	public void setNO_FIRST_LINE(boolean nO_FIRST_LINE)
    {
		_properties.setBooleanProperty("NO_FIRST_LINE",nO_FIRST_LINE);
    	//NO_FIRST_LINE = nO_FIRST_LINE;
    }

	public boolean isNO_EMPTY_TRIANGLE()
    {
		return _properties.getBooleanProperty("NO_EMPTY_TRIANGLE");
    	//return NO_EMPTY_TRIANGLE;
    }

	public void setNO_EMPTY_TRIANGLE(boolean nO_EMPTY_TRIANGLE)
    {
		_properties.setBooleanProperty("NO_EMPTY_TRIANGLE",nO_EMPTY_TRIANGLE);
    	//NO_EMPTY_TRIANGLE = nO_EMPTY_TRIANGLE;
    }

	public boolean isNO_AUTO_ATARI()
    {
		return _properties.getBooleanProperty("NO_AUTO_ATARI");
    	//return NO_AUTO_ATARI;
    }

	public void setNO_AUTO_ATARI(boolean nO_AUTO_ATARI)
    {
		_properties.setBooleanProperty("NO_AUTO_ATARI",nO_AUTO_ATARI);
    	//NO_AUTO_ATARI = nO_AUTO_ATARI;
    }

	public boolean isUSE_TACTICS_IN_SIMULATION()
    {
		return _properties.getBooleanProperty("USE_TACTICS_IN_SIMULATION");
    	//return USE_TACTICS_IN_SIMULATION;
    }

	public void setUSE_TACTICS_IN_SIMULATION(boolean uSE_TACTICS_IN_SIMULATION)
    {
		_properties.setBooleanProperty("USE_TACTICS_IN_SIMULATION",uSE_TACTICS_IN_SIMULATION);
    	//USE_TACTICS_IN_SIMULATION = uSE_TACTICS_IN_SIMULATION;
    }

	public boolean isUSE_TACTICS_IN_EXPLORATION()
    {
		return _properties.getBooleanProperty("USE_TACTICS_IN_EXPLORATION");
    	//return USE_TACTICS_IN_EXPLORATION;
    }

	public void setUSE_TACTICS_IN_EXPLORATION(boolean uSE_TACTICS_IN_EXPLORATION)
    {
		_properties.setBooleanProperty("USE_TACTICS_IN_EXPLORATION",uSE_TACTICS_IN_EXPLORATION);
    	//USE_TACTICS_IN_EXPLORATION = uSE_TACTICS_IN_EXPLORATION;
    }

	public boolean isIMMEDIATE_ESCAPE_ATARI()
    {
		return _properties.getBooleanProperty("IMMEDIATE_ESCAPE_ATARI");
    	//return IMMEDIATE_ESCAPE_ATARI;
    }

	public void setIMMEDIATE_ESCAPE_ATARI(boolean iMMEDIATE_ESCAPE_ATARI)
    {
		_properties.setBooleanProperty("IMMEDIATE_ESCAPE_ATARI",iMMEDIATE_ESCAPE_ATARI);
    	//IMMEDIATE_ESCAPE_ATARI = iMMEDIATE_ESCAPE_ATARI;
    }

	public boolean isCAPTURE_LAST_MOVE_IN_ATARI()
    {
		return _properties.getBooleanProperty("CAPTURE_LAST_MOVE_IN_ATARI");
    	//return CAPTURE_LAST_MOVE_IN_ATARI;
    }

	public void setCAPTURE_LAST_MOVE_IN_ATARI(boolean cAPTURE_LAST_MOVE_IN_ATARI)
    {
		_properties.setBooleanProperty("CAPTURE_LAST_MOVE_IN_ATARI",cAPTURE_LAST_MOVE_IN_ATARI);
    	//CAPTURE_LAST_MOVE_IN_ATARI = cAPTURE_LAST_MOVE_IN_ATARI;
    }

	public boolean isCAPTURE_LAST_MOVE_IN_LADDER()
    {
		return _properties.getBooleanProperty("CAPTURE_LAST_MOVE_IN_LADDER");
    	//return CAPTURE_LAST_MOVE_IN_LADDER;
    }

	public void setCAPTURE_LAST_MOVE_IN_LADDER(boolean cAPTURE_LAST_MOVE_IN_LADDER)
    {
		_properties.setBooleanProperty("CAPTURE_LAST_MOVE_IN_LADDER",cAPTURE_LAST_MOVE_IN_LADDER);
    	//CAPTURE_LAST_MOVE_IN_LADDER = cAPTURE_LAST_MOVE_IN_LADDER;
    }

	public boolean isESCAPE_ATARI()
    {
		return _properties.getBooleanProperty("ESCAPE_ATARI");
    	//return ESCAPE_ATARI;
    }

	public void setESCAPE_ATARI(boolean eSCAPE_ATARI)
    {
		_properties.setBooleanProperty("ESCAPE_ATARI",eSCAPE_ATARI);
    	//ESCAPE_ATARI = eSCAPE_ATARI;
    }

	public boolean isCAPTURE_STONES_IN_ATARI()
    {
		return _properties.getBooleanProperty("CAPTURE_STONES_IN_ATARI");
    	//return CAPTURE_STONES_IN_ATARI;
    }

	public void setCAPTURE_STONES_IN_ATARI(boolean cAPTURE_STONES_IN_ATARI)
    {
		_properties.setBooleanProperty("CAPTURE_STONES_IN_ATARI",cAPTURE_STONES_IN_ATARI);
    	//CAPTURE_STONES_IN_ATARI = cAPTURE_STONES_IN_ATARI;
    }

	public boolean isCAPTURE_STONES_IN_LADDER()
    {
		return _properties.getBooleanProperty("CAPTURE_STONES_IN_LADDER");
    	//return CAPTURE_STONES_IN_LADDER;
    }

	public void setCAPTURE_STONES_IN_LADDER(boolean cAPTURE_STONES_IN_LADDER)
    {
		_properties.setBooleanProperty("CAPTURE_STONES_IN_LADDER",cAPTURE_STONES_IN_LADDER);
    	//CAPTURE_STONES_IN_LADDER = cAPTURE_STONES_IN_LADDER;
    }

	public boolean isCAPTURE_STONES()
    {
		return _properties.getBooleanProperty("CAPTURE_STONES");
    	//return CAPTURE_STONES;
    }

	public void setCAPTURE_STONES(boolean cAPTURE_STONES)
    {
		_properties.setBooleanProperty("CAPTURE_STONES",cAPTURE_STONES);
    	//CAPTURE_STONES = cAPTURE_STONES;
    }

	public boolean isUSE_HARD_PATTERNS()
    {
		return _properties.getBooleanProperty("USE_HARD_PATTERNS");
    	//return USE_HARD_PATTERNS;
    }

	public void setUSE_HARD_PATTERNS(boolean uSE_HARD_PATTERNS)
    {
		_properties.setBooleanProperty("USE_HARD_PATTERNS",uSE_HARD_PATTERNS);
    	//USE_HARD_PATTERNS = uSE_HARD_PATTERNS;
    }

	public boolean isSEPARATE_PATTERN()
    {
		return _properties.getBooleanProperty("SEPARATE_PATTERN");
    	//return SEPARATE_PATTERN;
    }

	public void setSEPARATE_PATTERN(boolean sEPARATE_PATTERN)
    {
		_properties.setBooleanProperty("SEPARATE_PATTERN",sEPARATE_PATTERN);
    	//SEPARATE_PATTERN = sEPARATE_PATTERN;
    }

	public boolean isCUT_PATTERN()
    {
		return _properties.getBooleanProperty("CUT_PATTERN");
    	//return CUT_PATTERN;
    }

	public void setCUT_PATTERN(boolean CUT_PATTERN)
    {
		_properties.setBooleanProperty("CUT_PATTERN",CUT_PATTERN);
    	//CUT_PATTERN = cUT_PATTERN;
    }

	public boolean isTOBI_CUT_PATTERN()
    {
		return _properties.getBooleanProperty("TOBI_CUT_PATTERN");
    	//return TOBI_CUT_PATTERN;
    }

	public void setTOBI_CUT_PATTERN(boolean tOBI_CUT_PATTERN)
    {
		_properties.setBooleanProperty("TOBI_CUT_PATTERN",tOBI_CUT_PATTERN);
    	//TOBI_CUT_PATTERN = tOBI_CUT_PATTERN;
    }

	public boolean isKEIMA_CUT_PATTERN()
    {
		return _properties.getBooleanProperty("KEIMA_CUT_PATTERN");
    	//return KEIMA_CUT_PATTERN;
    }

	public void setKEIMA_CUT_PATTERN(boolean KEIMA_CUT_PATTERN)
    {
		_properties.setBooleanProperty("KEIMA_CUT_PATTERN",KEIMA_CUT_PATTERN);
    	//KEIMA_CUT_PATTERN = kEIMA_CUT_PATTERN;
    }

	public boolean isTOBI_CONNECT_PATTERN()
    {
		return _properties.getBooleanProperty("TOBI_CONNECT_PATTERN");
    	//return TOBI_CONNECT_PATTERN;
    }

	public void setTOBI_CONNECT_PATTERN(boolean tOBI_CONNECT_PATTERN)
    {
		_properties.setBooleanProperty("TOBI_CONNECT_PATTERN",tOBI_CONNECT_PATTERN);
    	//TOBI_CONNECT_PATTERN = tOBI_CONNECT_PATTERN;
    }

	public boolean isFIRST_LINE_ATARI_PATTERN()
    {
		return _properties.getBooleanProperty("FIRST_LINE_ATARI_PATTERN");
    	//return FIRST_LINE_ATARI_PATTERN;
    }

	public void setFIRST_LINE_ATARI_PATTERN(boolean FIRST_LINE_ATARI_PATTERN)
    {
		_properties.setBooleanProperty("FIRST_LINE_ATARI_PATTERN",FIRST_LINE_ATARI_PATTERN);
    	//FIRST_LINE_ATARI_PATTERN = fIRST_LINE_ATARI_PATTERN;
    }

	public boolean isSECOND_LINE_ATARI_PATTERN()
    {
		return _properties.getBooleanProperty("SECOND_LINE_ATARI_PATTERN");
    	//return SECOND_LINE_ATARI_PATTERN;
    }

	public void setSECOND_LINE_ATARI_PATTERN(boolean sECOND_LINE_ATARI_PATTERN)
    {
		_properties.setBooleanProperty("SECOND_LINE_ATARI_PATTERN",sECOND_LINE_ATARI_PATTERN);
    	//SECOND_LINE_ATARI_PATTERN = sECOND_LINE_ATARI_PATTERN;
    }

	public boolean isFIRST_LINE_BLOCK_PATTERN()
    {
		return _properties.getBooleanProperty("FIRST_LINE_BLOCK_PATTERN");
    	//return FIRST_LINE_BLOCK_PATTERN;
    }

	public void setFIRST_LINE_BLOCK_PATTERN(boolean FIRST_LINE_BLOCK_PATTERN)
    {
		_properties.setBooleanProperty("FIRST_LINE_BLOCK_PATTERN",FIRST_LINE_BLOCK_PATTERN);
    	//FIRST_LINE_BLOCK_PATTERN = fIRST_LINE_BLOCK_PATTERN;
    }

	public boolean isSECOND_LINE_BLOCK_PATTERN()
    {
		return _properties.getBooleanProperty("SECOND_LINE_BLOCK_PATTERN");
    	//return SECOND_LINE_BLOCK_PATTERN;
    }

	public void setSECOND_LINE_BLOCK_PATTERN(boolean sECOND_LINE_BLOCK_PATTERN)
    {
		_properties.setBooleanProperty("SECOND_LINE_BLOCK_PATTERN",sECOND_LINE_BLOCK_PATTERN);
    	//SECOND_LINE_BLOCK_PATTERN = sECOND_LINE_BLOCK_PATTERN;
    }

	public boolean isEYE_PATTERN()
    {
		return _properties.getBooleanProperty("EYE_PATTERN");
    	//return EYE_PATTERN;
    }

	public void setEYE_PATTERN(boolean eYE_PATTERN)
    {
		_properties.setBooleanProperty("EYE_PATTERN",eYE_PATTERN);
    	//EYE_PATTERN = eYE_PATTERN;
    }
}
