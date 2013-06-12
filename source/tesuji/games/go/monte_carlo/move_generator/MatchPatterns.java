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
import tesuji.games.go.pattern.incremental.PatternMatchList;
import tesuji.games.go.util.DefaultBoardModel;
import tesuji.games.model.BoardModelListener;

public class MatchPatterns extends AbstractMoveGenerator
{
	protected IncrementalPatternMatcher patternMatcher;
	private String patternGroupName;
	private IncrementalPatternMatcher _patternMatcher;
	
	public MatchPatterns(String groupName)
	{
		patternGroupName = groupName;
	}
	
	@Override
    public void register(MonteCarloPluginAdministration admin)
    {
		administration = admin;
		PatternManager patternManager = HibernatePatternManager.getSingleton();
		patternMatcher = new IncrementalPatternMatcher();
		PatternGroup group = patternManager.getPatternGroup(patternGroupName);
	    patternManager.getPatterns(group);
	    _patternMatcher = new IncrementalPatternMatcher();
	    _patternMatcher.setPatternGroup(group);
	    _patternMatcher.setPatternManager(patternManager);
	    _patternMatcher.initialise(new DefaultBoardModel(administration.getBoardSize()));
		administration._explorationMoveSupport.addBoardModelListener(_patternMatcher);
		administration.getBoardModel().addBoardModelListener(_patternMatcher);
    }

	@Override
    public int generate()
    {
		_patternMatcher.updatePatternMatches();
		ArrayList<PatternMatch> matchList = _patternMatcher.getNewMatchList();
		for (PatternMatch pm : matchList)
		{
			if (administration.getColorToMove()==ColorConstant.BLACK)
			{
				administration.addPriorityMove(pm.getXY(), 1 /*pm.getUrgencyValue(ColorConstant.BLACK)*/, pm.getNrOccurences(ColorConstant.BLACK), pm.getNrPlayed(ColorConstant.BLACK));
			}
			else
			{
				administration.addPriorityMove(pm.getXY(), 1 /*pm.getUrgencyValue(ColorConstant.WHITE)*/, pm.getNrOccurences(ColorConstant.WHITE), pm.getNrPlayed(ColorConstant.WHITE));
			}
		}
		return UNDEFINED_COORDINATE;
    }

	@Override
    public MoveGenerator createClone()
    {
	    return new MatchPatterns(patternGroupName);
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
