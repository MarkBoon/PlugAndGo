package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.core.util.ArrayList;
import tesuji.games.general.ColorConstant;
import tesuji.games.general.GlobalParameters;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.pattern.common.PatternManager;
import tesuji.games.go.pattern.incremental.IncrementalPatternMatcher;
import tesuji.games.go.pattern.incremental.PatternMatch;
import tesuji.games.go.pattern.incremental.PatternMatchList;
import tesuji.games.go.util.ProbabilityMap;

public class MatchPatterns extends LadderMoveGenerator
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
		super.register(admin);
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
		for (int i=newMatchList.size(); --i>=0;)
		{
			PatternMatch match = newMatchList.get(i);
			if (deletedMatchList.contains(match))
			{
				newMatchList.remove(i);
				deletedMatchList.remove(match);
			}
		}
		double factor = 1.0;
		if (GlobalParameters.isTestVersion())
			factor = 0.1;
		for (int i=newMatchList.size(); --i>=0;)
		{
			PatternMatch pm = newMatchList.get(i);
			boolean valid = ((board[pm.getMoveXY(ColorConstant.BLACK)]!=ColorConstant.EMPTY || isSafeToMove(pm.getMoveXY(ColorConstant.BLACK), ColorConstant.BLACK)) 
							&& (board[pm.getMoveXY(ColorConstant.WHITE)]!=ColorConstant.EMPTY || isSafeToMove(pm.getMoveXY(ColorConstant.WHITE), ColorConstant.WHITE)));
			
			if (board[pm.getMoveXY(ColorConstant.BLACK)]==ColorConstant.EMPTY)
			{
				if (valid)
					map.add(pm.getMoveXY(ColorConstant.BLACK), pm.getUrgencyValue(ColorConstant.BLACK)*factor, ColorConstant.BLACK);
				else
					i=i;
			}
			if (board[pm.getMoveXY(ColorConstant.WHITE)]==ColorConstant.EMPTY)
			{
				if (valid)
					map.add(pm.getMoveXY(ColorConstant.WHITE), pm.getUrgencyValue(ColorConstant.WHITE)*factor, ColorConstant.WHITE);
				else
					i=i;
			}
		}
		for (int i=deletedMatchList.size(); --i>=0;)
		{
			PatternMatch pm = deletedMatchList.get(i);
			if (board[pm.getMoveXY(ColorConstant.BLACK)]==ColorConstant.EMPTY)
				map.subtract(pm.getMoveXY(ColorConstant.BLACK), pm.getUrgencyValue(ColorConstant.BLACK)*factor, ColorConstant.BLACK);
			if (board[pm.getMoveXY(ColorConstant.WHITE)]==ColorConstant.EMPTY)
				map.subtract(pm.getMoveXY(ColorConstant.WHITE), pm.getUrgencyValue(ColorConstant.WHITE)*factor, ColorConstant.WHITE);
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
