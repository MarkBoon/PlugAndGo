package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.core.util.ArrayList;
import tesuji.games.general.ColorConstant;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.pattern.common.HibernatePatternManager;
import tesuji.games.go.pattern.common.PatternGroup;
import tesuji.games.go.pattern.common.PatternManager;
import tesuji.games.go.pattern.incremental.IncrementalPatternMatcher;
import tesuji.games.go.pattern.incremental.PatternMatch;
import tesuji.games.go.util.DefaultBoardModel;

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
    }

//	@Override
    public int generate()
    {
		_patternMatcher.updatePatternMatches();
		ArrayList<PatternMatch> matchList = _patternMatcher.getNewMatchList();
		for (PatternMatch pm : matchList)
		{
			if (administration.getColorToMove()==ColorConstant.BLACK)
			{
//				administration.addPriorityMove(pm.getXY(), 1 /*pm.getUrgencyValue(ColorConstant.BLACK)*/, pm.getNrOccurences(ColorConstant.BLACK), pm.getNrPlayed(ColorConstant.BLACK));
			}
			else
			{
//				administration.addPriorityMove(pm.getXY(), 1 /*pm.getUrgencyValue(ColorConstant.WHITE)*/, pm.getNrOccurences(ColorConstant.WHITE), pm.getNrPlayed(ColorConstant.WHITE));
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
