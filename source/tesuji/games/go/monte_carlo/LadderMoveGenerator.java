package tesuji.games.go.monte_carlo;

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
		_ladderReader = new LadderReader(administration.getBoardSize()); // A little space can be saved by sharing this instance.
	}

	@Override
	public void copyDataFrom(MoveGenerator source)
	{
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
