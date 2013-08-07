package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.core.util.ArrayList;
import tesuji.games.general.ColorConstant;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.pattern.common.PatternManager;
import tesuji.games.go.pattern.incremental.IncrementalPatternMatcher;
import tesuji.games.go.pattern.incremental.PatternMatch;
import tesuji.games.go.util.DefaultBoardModel;
import tesuji.games.go.util.ProbabilityMap;

public class MatchPatterns extends AbstractMoveGenerator
{
	protected IncrementalPatternMatcher patternMatcher;
	private PatternManager _patternManager;
	private IncrementalPatternMatcher _patternMatcher;
	
	public MatchPatterns(PatternManager patternManager)
	{
		_patternManager = patternManager;
	}
	
	@Override
    public void register(MonteCarloPluginAdministration admin)
    {
		administration = admin;
	    _patternMatcher = new IncrementalPatternMatcher();
	    _patternMatcher.setPatternGroup(_patternManager.getDefaultPatternGroup());
	    _patternMatcher.initialise(new DefaultBoardModel(administration.getBoardSize()));
		administration._explorationMoveSupport.addBoardModelListener(_patternMatcher);
		administration.getBoardModel().addBoardModelListener(_patternMatcher);
		generate();
    }

//	@Override
    public int generate()
    {
    	ProbabilityMap map = administration.getProbabilityMap();
		_patternMatcher.updatePatternMatches();
		ArrayList<PatternMatch> matchList = _patternMatcher.getNewMatchList();
		for (PatternMatch pm : matchList)
		{
			if (administration.getColorToMove()==ColorConstant.BLACK)
			{
				map.add(pm.getXY(), pm.getUrgencyValue(ColorConstant.BLACK));
//				administration.addPriorityMove(pm.getXY(), pm.getUrgencyValue(ColorConstant.BLACK));
			}
			else
			{
				map.add(pm.getXY(), pm.getUrgencyValue(ColorConstant.WHITE));
//				administration.addPriorityMove(pm.getXY(), pm.getUrgencyValue(ColorConstant.WHITE));
			}
		}
		return UNDEFINED_COORDINATE;
    }

//	@Override
    public MoveGenerator createClone()
    {
	    return new MatchPatterns(_patternManager);
    }

	@Override
    public void copyDataFrom(MoveGenerator source)
    {
		MatchPatterns sourceGenerator = (MatchPatterns)source;
		_patternMatcher.copyDataFrom(sourceGenerator._patternMatcher);
    }
	
	public void update()
	{
		_patternMatcher.updatePatternMatches();
	}

}
