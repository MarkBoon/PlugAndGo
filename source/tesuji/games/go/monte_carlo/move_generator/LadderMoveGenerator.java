package tesuji.games.go.monte_carlo.move_generator;

import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.tactics.LadderReader;

public abstract class LadderMoveGenerator extends AbstractMoveGenerator
{
	protected MonteCarloPluginAdministration administration;
	protected LadderReader _ladderReader;
	private boolean checkHistory = false;
	
	@Override
	public void register(MonteCarloPluginAdministration admin)
	{
		administration = admin;
		_ladderReader = new LadderReader(administration.getBoardSize()); // A little space (and time) can be saved by sharing this instance.
	}

	@Override
	public void copyDataFrom(MoveGenerator source)
	{
		super.copyDataFrom(source);
		checkHistory = ((LadderMoveGenerator)source).checkHistory;
	}

	public boolean isCheckHistory()
    {
	    return checkHistory;
    }

	public void setCheckHistory(boolean checkHistory)
    {
	    this.checkHistory = checkHistory;
    }
}
