package tesuji.games.go.test;

import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.monte_carlo.move_generator.MatchPatterns;
import tesuji.games.go.pattern.common.HibernatePatternManager;
import static tesuji.games.go.util.GoArray.*;
import static tesuji.games.general.ColorConstant.*;

public class PatternTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		HibernatePatternManager patternManager = new HibernatePatternManager("GenTest");
		MatchPatterns matchPatterns = new MatchPatterns(patternManager);
		MonteCarloPluginAdministration admin = new MonteCarloPluginAdministration(9);
		matchPatterns.register(admin);
		admin.playMove(GoMoveFactory.getSingleton().createMove(toXY(3,3),BLACK));
		matchPatterns.generate();
	}

}
