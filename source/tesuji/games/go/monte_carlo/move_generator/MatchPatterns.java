package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.core.util.ArrayList;
import tesuji.games.general.ColorConstant;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.pattern.common.PatternManager;
import tesuji.games.go.pattern.incremental.IncrementalPatternMatcher;
import tesuji.games.go.pattern.incremental.PatternMatch;
import tesuji.games.go.pattern.incremental.PatternMatchList;
import tesuji.games.go.util.ProbabilityMap;

public class MatchPatterns extends AbstractMoveGenerator
{
	protected IncrementalPatternMatcher patternMatcher;
	private PatternManager _patternManager;
	private IncrementalPatternMatcher _patternMatcher;
	
	public MatchPatterns(PatternManager patternManager)
	{
		_patternManager = patternManager;
	    _patternMatcher = new IncrementalPatternMatcher();
	    _patternMatcher.setPatternGroup(_patternManager.getDefaultPatternGroup());
	}
	
	@Override
    public void register(MonteCarloPluginAdministration admin)
    {
		administration = admin;
		administration._explorationMoveSupport.addBoardModelListener(_patternMatcher);
		administration.getBoardModel().addBoardModelListener(_patternMatcher);
//		clear();		
    }
	
	public void clear()
	{
	    _patternMatcher.initialise();
		
    	ProbabilityMap map = administration.getProbabilityMap();
		ArrayList<PatternMatch> matchList = _patternMatcher.getMatchList();
		for (PatternMatch pm : matchList)
		{
			map.add(pm.getMoveXY(ColorConstant.BLACK), pm.getUrgencyValue(ColorConstant.BLACK), ColorConstant.BLACK);
			map.add(pm.getMoveXY(ColorConstant.WHITE), pm.getUrgencyValue(ColorConstant.WHITE), ColorConstant.WHITE);
		}
	}

//	@Override
    public int generate()
    {
//    	byte[] board = administration.getBoardArray();
    	ProbabilityMap map = administration.getProbabilityMap();
		update();
//		ArrayList<PatternMatch> matchList = _patternMatcher.getDeletedMatchList();
//		for (PatternMatch pm : matchList)
//		{
//			if (board[pm.getMoveXY(ColorConstant.BLACK)]==ColorConstant.EMPTY)
//				map.subtract(pm.getMoveXY(ColorConstant.BLACK), pm.getUrgencyValue(ColorConstant.BLACK), ColorConstant.BLACK);
//			if (board[pm.getMoveXY(ColorConstant.WHITE)]==ColorConstant.EMPTY)
//				map.subtract(pm.getMoveXY(ColorConstant.WHITE), pm.getUrgencyValue(ColorConstant.WHITE), ColorConstant.WHITE);
//		}
//		matchList = _patternMatcher.getNewMatchList();
//		for (PatternMatch pm : matchList)
//		{
//			if (board[pm.getMoveXY(ColorConstant.BLACK)]==ColorConstant.EMPTY)
//				map.add(pm.getMoveXY(ColorConstant.BLACK), pm.getUrgencyValue(ColorConstant.BLACK), ColorConstant.BLACK);
////				administration.addPriorityMove(pm.getXY(), pm.getUrgencyValue(ColorConstant.BLACK));
//			if (board[pm.getMoveXY(ColorConstant.WHITE)]==ColorConstant.EMPTY)
//				map.add(pm.getMoveXY(ColorConstant.WHITE), pm.getUrgencyValue(ColorConstant.WHITE), ColorConstant.WHITE);
////				administration.addPriorityMove(pm.getXY(), pm.getUrgencyValue(ColorConstant.WHITE));
//		}
		return UNDEFINED_COORDINATE;
    }
    
    public void setBoardSize(int size)
    {
    	_patternMatcher.setBoardSize(size);
    }

//	@Override
    public MoveGenerator createClone()
    {
    	MoveGenerator clone = new MatchPatterns(_patternManager);
    	clone.setBoardSize(_patternMatcher.getBoardModel().getBoardSize());
    	return clone;
    }

	@Override
    public void copyDataFrom(MoveGenerator source)
    {
		MatchPatterns sourceGenerator = (MatchPatterns)source;
		_patternMatcher.copyDataFrom(sourceGenerator._patternMatcher);
    }
	
	public void update()
	{
    	byte[] board = administration.getBoardArray();
    	ProbabilityMap map = administration.getProbabilityMap();
		_patternMatcher.updatePatternMatches();
		ArrayList<PatternMatch> deletedMatchList = _patternMatcher.getDeletedMatchList();
		ArrayList<PatternMatch> newMatchList = _patternMatcher.getNewMatchList();
		for (PatternMatch pm : newMatchList)
		{
			if (board[pm.getMoveXY(ColorConstant.BLACK)]==ColorConstant.EMPTY)
				map.add(pm.getMoveXY(ColorConstant.BLACK), pm.getUrgencyValue(ColorConstant.BLACK), ColorConstant.BLACK);
			if (board[pm.getMoveXY(ColorConstant.WHITE)]==ColorConstant.EMPTY)
				map.add(pm.getMoveXY(ColorConstant.WHITE), pm.getUrgencyValue(ColorConstant.WHITE), ColorConstant.WHITE);
		}
		for (PatternMatch pm : deletedMatchList)
		{
			if (board[pm.getMoveXY(ColorConstant.BLACK)]==ColorConstant.EMPTY)
				map.subtract(pm.getMoveXY(ColorConstant.BLACK), pm.getUrgencyValue(ColorConstant.BLACK), ColorConstant.BLACK);
			if (board[pm.getMoveXY(ColorConstant.WHITE)]==ColorConstant.EMPTY)
				map.subtract(pm.getMoveXY(ColorConstant.WHITE), pm.getUrgencyValue(ColorConstant.WHITE), ColorConstant.WHITE);
		}
	}

	public String toString()
	{
		return administration.toString();
	}
	
	public PatternMatchList getMatches()
	{
		return _patternMatcher.getMatchList();
	}
}
