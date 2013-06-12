package tesuji.games.go.test;

import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.monte_carlo.move_generator.MatchPatterns;
import static tesuji.games.go.util.GoArray.*;
import static tesuji.games.general.ColorConstant.*;

public class PatternTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		MatchPatterns matchPatterns = new MatchPatterns("GenTest");
		MonteCarloPluginAdministration admin = new MonteCarloPluginAdministration(9);
		matchPatterns.register(admin);
		admin.playMove(GoMoveFactory.getSingleton().createMove(toXY(3,3),BLACK));
		matchPatterns.generate();
	}

}
