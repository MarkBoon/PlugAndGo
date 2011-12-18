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

import tesuji.core.util.ArrayList;
import tesuji.games.go.common.GoMove;

import tesuji.games.go.pattern.common.HibernatePatternManager;
import tesuji.games.go.pattern.common.Pattern;
import tesuji.games.go.pattern.common.PatternGroup;
import tesuji.games.go.pattern.common.PatternManager;
import tesuji.games.go.pattern.incremental.IncrementalPatternMatcher;
import tesuji.games.go.pattern.incremental.PatternMatch;
import tesuji.games.go.pattern.incremental.PatternMatchList;
import tesuji.games.go.pattern.util.PatternUtil;
import tesuji.games.go.util.PointSet;
import tesuji.games.go.util.Statistics;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;

/**
 * 
 */
public class MCPatternsAdministration
	extends MCTacticsAdministration
{
	private static final int URGENCY_THRESHOLD = 400;
	
	private static final boolean USE_PLAYOUT_PATTERNS = false;
	private static final boolean USE_EXPLORATION_PATTERNS = false;
	private static final boolean LEARN_PATTERNS = false;
	
	private int nrPatterns = -1;
	
	private IncrementalPatternMatcher _patternSet;
	private PatternManager _patternManager;
	
	private MCPatternsAdministration(int boardSize, IncrementalPatternMatcher patternSet)
	{
		super(boardSize);
		
		_patternSet = patternSet;
	}

	public MCPatternsAdministration(String patternGroupName)
	{
		super();
		
		_patternSet = loadPatternSet(patternGroupName);
		_patternManager = HibernatePatternManager.getSingleton();
	}
	
	PatternManager getPatternManager()
	{
		return _patternManager;
	}

	private IncrementalPatternMatcher loadPatternSet(String patternGroupName)
	{
		if (_patternSet!=null && _patternSet.getPatternGroup()!=null && _patternSet.getPatternGroup().getPatternList().size()==nrPatterns)
			return _patternSet;
		
		PatternGroup group = _patternManager.getPatternGroup(patternGroupName);
	    _patternManager.getPatterns(group);
		IncrementalPatternMatcher patternSet = new IncrementalPatternMatcher();
		patternSet.setPatternGroup(group);
		patternSet.setPatternManager(_patternManager);
		nrPatterns = patternSet.getPatternGroup().getPatternList().size();
		
		return patternSet;
	}
	
    public void setBoardSize(int size)
    {
    	int oldSize = getBoardSize();
    	super.setBoardSize(size);
    	if (oldSize!=size)
    	{
    		clear();
    	}
    }
    
    @Override
    public void clear()
    {
    	super.clear();
    	if (_patternSet!=null)
    		getBoardModel().removeBoardModelListener(_patternSet); // This deallocates the former pattern-tree.
		_patternSet = loadPatternSet(_patternSet.getPatternGroup().getGroupName());
		_patternSet.initialise(getBoardModel());
    }

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#playMove(tesuji.games.general.Move)
	 */
	public void playMove(GoMove move)
	{
		if (LEARN_PATTERNS)
		{
			learnMove(move);
			rewardPlayedPattern(move);
			punishUnplayedPatterns(move.getColor());
		}
		
		super.playMove(move);
		_patternSet.updatePatternMatches();
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#playExplorationMove(tesuji.games.general.Move)
	 */
	public void playExplorationMove(GoMove move)
	{
		super.playExplorationMove(move);
		_patternSet.updatePatternMatches();
	}

	public void play(int xy)
	{
		super.play(xy);
		if (USE_PLAYOUT_PATTERNS)
			_patternSet.updatePatternMatches();
	}
	
	@Override
	public int selectSimulationMove(PointSet emptyPoints)
	{
		if (USE_PLAYOUT_PATTERNS)
		{
			if (_previousMove!=PASS)
			{
				int tacticalXY = selectSimulationPriorityMove();
				if (tacticalXY!=PASS && tacticalXY!=UNDEFINED_COORDINATE)
					return tacticalXY;
				
				int patternMove = getRandomPatternMove(_patternSet.getMatchList());
	//			int patternMove = getWeightedRandomPatternMove();
				if (patternMove!=UNDEFINED_COORDINATE && patternMove!=PASS)
				{
					Statistics.nrPatternsPlayed++;
					return patternMove;
				}
			}
			return selectRandomMoveCoordinate(emptyPoints);
		}
		else
			return super.selectSimulationMove(emptyPoints);
	}
	
	private int getRandomPatternMove(ArrayList<PatternMatch> matchList)
	{
		int size = matchList.size();
		if (size>0)
		{
			int startIndex = RANDOM.nextInt(size);
			for (int j=startIndex; j<size; j++)
			{
				PatternMatch match = matchList.get(j);
				int moveXY = match.getMoveXY(_colorToPlay);
				if (moveXY>0 && _boardModel.get(moveXY)==EMPTY && isLegal(moveXY)
								/*&& match.isSafeToMove(moveXY, _ladderReader)*/)
					return moveXY;
			}
			for (int j=0; j<startIndex; j++)
			{
				PatternMatch match = matchList.get(j);
				int moveXY = match.getMoveXY(_colorToPlay);
				if (moveXY>0 && _boardModel.get(moveXY)==EMPTY && isLegal(moveXY)
								/*&& match.isSafeToMove(moveXY, _ladderReader)*/)
					return moveXY;
			}
		}
		return UNDEFINED_COORDINATE;
	}
	
	@SuppressWarnings("unused")
    private int getWeightedRandomPatternMove()
	{
		PatternMatchList matchList = _patternSet.getMatchList();
		double mostUrgentValue = URGENCY_THRESHOLD*2;
		double opponentsUrgentValue = URGENCY_THRESHOLD*2;
		for (int i=0; i<matchList.size(); i++)
		{
			PatternMatch match = matchList.get(i);
			double urgency = match.getUrgencyValue(_colorToPlay);
			int moveXY = match.getMoveXY(_colorToPlay);
			if (urgency<mostUrgentValue
					&& moveXY>0 && _boardModel.get(moveXY)==EMPTY && isLegal(moveXY)
					&& match.isSafeToMove(moveXY, _ladderReader))
			{
				mostUrgentValue = urgency;
				opponentsUrgentValue = match.getUrgencyValue(opposite(_colorToPlay));
			}
		}
		if (opponentsUrgentValue>URGENCY_THRESHOLD)
			opponentsUrgentValue = mostUrgentValue;
		
		// There's a certain chance not to play a pattern at all.
		int treshHold = RANDOM.nextInt(URGENCY_THRESHOLD);
		if (treshHold<=mostUrgentValue)
			return UNDEFINED_COORDINATE;
		
		double variance = RANDOM.nextInt((int)mostUrgentValue+1);
		double requiredUrgency = mostUrgentValue+variance+opponentsUrgentValue+25;
		
		int size = matchList.size();
		if (size>0)
		{
			int startIndex = RANDOM.nextInt(size);
			for (int j=startIndex; j<size; j++)
			{
				PatternMatch match = matchList.get(j);
				int moveXY = match.getMoveXY(_colorToPlay);
				if (match.getUrgencyValue(_colorToPlay)<=requiredUrgency
						&& moveXY>0 && _boardModel.get(moveXY)==EMPTY && isLegal(moveXY)
						&& match.isSafeToMove(moveXY, _ladderReader))
					return moveXY;
			}
			for (int j=0; j<startIndex; j++)
			{
				PatternMatch match = matchList.get(j);
				int moveXY = match.getMoveXY(_colorToPlay);
				if (match.getUrgencyValue(_colorToPlay)<=requiredUrgency
						&& moveXY>0 && _boardModel.get(moveXY)==EMPTY && isLegal(moveXY)
						&& match.isSafeToMove(moveXY, _ladderReader))
					return moveXY;
			}
		}
		return UNDEFINED_COORDINATE;
	}
	
	@Override
	protected void getPriorityMoves()
	{
		super.getPriorityMoves();
		
		if (USE_EXPLORATION_PATTERNS)
		{
			PatternMatchList matchList = _patternSet.getMatchList();
			Statistics.nrPatternsPlayed += matchList.size(); // XXX
			for (int i=matchList.size(); --i>=0;)
			{
				PatternMatch match = matchList.get(i);
				int moveXY = match.getMoveXY(_colorToPlay);
				if (moveXY>0 && match.getPattern().isUseful() && isLegal(moveXY) && isSafeToMove(moveXY))
				{
//					addPriorityMove(moveXY, 3);
					addPriorityMove(moveXY, (int)match.getUrgencyValue(_colorToPlay),match.getNrOccurences(_colorToPlay),match.getNrPlayed(_colorToPlay));
				}
			}
		}
	}
	
	public void copyDataFrom(MonteCarloAdministration<GoMove> sourceAdmin)
	{
		super.copyDataFrom(sourceAdmin);

		MCPatternsAdministration source = (MCPatternsAdministration)sourceAdmin;
		getBoardModel().removeBoardModelListener(_patternSet);
		_patternSet.copyDataFrom(source._patternSet);
//		_patternSet.getMatchList().clear();
//		_patternSet.getDeletedMatchList().clear();
//		_patternSet = ((MCFastPatternAdministration)sourceAdmin)._patternSet.createClone();
		getBoardModel().addBoardModelListener(_patternSet);
		assert(_patternSet.getBoardModel().equals(sourceAdmin.getBoardModel()));
	}
	
	public MonteCarloAdministration<GoMove> createClone()
	{
		MCPatternsAdministration clone = new MCPatternsAdministration(getBoardSize(), _patternSet.createClone());
		clone.setIsTestVersion(isTestVersion());
		clone.initBoardModel(getBoardSize());		
		clone.copyDataFrom(this);
		
		return clone;
	}
	
	public void learnMove(GoMove move)
	{
		for (int n=3; n<=5; n+=2)
		{
			Pattern newPattern = PatternUtil.createPattern(n, move.getXY(), move.getColor(), _boardModel, null, null);
			newPattern.setGroupId(_patternSet.getPatternGroup().getGroupId());
			if ((newPattern.getBlackCount()!=0 && newPattern.getWhiteCount()!=0)
					|| newPattern.getBlackCount()+newPattern.getWhiteCount()>3)
			{
				if (newPattern.getWidth()+newPattern.getHeight()>4)
				{
					if (!_patternSet.getPatternGroup().containsPattern(newPattern))
					{
						_patternSet.getPatternManager().createPattern(newPattern);
						_patternSet.getPatternGroup().getPatternList().add(newPattern);
					}
				}
			}
		}
	}
	
	public void rewardPlayedPattern(GoMove move)
	{
		PatternMatchList matchList = _patternSet.getMatchList();
		for (int j=0; j<matchList.size(); j++)
		{
			PatternMatch match = matchList.get(j);
			Pattern pattern = match.getPattern();
			if (match.getMoveXY()==move.getXY())
			{
    			match.increasePatternRatio(move.getColor());
    			int nrMovesBeforePlayed = _patternSet.getMoveNr()-match.getMoveNr();
    			if (nrMovesBeforePlayed<0)
    				throw new IllegalStateException("Negative nr moves before pattern played ("+nrMovesBeforePlayed+")");
    			match.updatePatternUrgency(move.getColor(),nrMovesBeforePlayed);
                _patternSet.getPatternManager().updatePattern(pattern);
			}
			
		}
	}
	
    public void punishUnplayedPatterns(byte color)
    {
		PatternMatchList matchList = _patternSet.getDeletedMatchList();
		for (int j=0; j<matchList.size(); j++)
		{
			PatternMatch match = matchList.get(j);
			Pattern pattern = match.getPattern();
			match.increasePatternOccurrence(color);
   			int nrMovesBeforePlayed = _patternSet.getMoveNr()-match.getMoveNr();
			if (nrMovesBeforePlayed<0)
				throw new IllegalStateException("Negative nr moves before pattern not played ("+nrMovesBeforePlayed+")");
			match.updatePatternUrgency(color,nrMovesBeforePlayed);
            _patternSet.getPatternManager().updatePattern(pattern);
		}
    }
}
