package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.general.ColorConstant.BLACK;
import static tesuji.games.general.ColorConstant.WHITE;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.tactics.LadderReader;
import tesuji.games.go.tactics.TacticsConstant;

public abstract class LadderMoveGenerator extends AbstractMoveGenerator
{
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

	   protected boolean isSafeToMove(int moveXY)
	   {
			_ladderReader.setBoardArray(administration.getBoardArray());
			_ladderReader.setKoPoint(administration.getKoPoint());
			return (_ladderReader.wouldBeLadder(moveXY,BLACK)==TacticsConstant.CANNOT_CATCH
				&& _ladderReader.wouldBeLadder(moveXY,WHITE)==TacticsConstant.CANNOT_CATCH);
	   }

	   protected boolean isSafeToMove(int moveXY, byte color)
	   {
			_ladderReader.setBoardArray(administration.getBoardArray());
			_ladderReader.setKoPoint(administration.getKoPoint());
			return (_ladderReader.wouldBeLadder(moveXY,color)==TacticsConstant.CANNOT_CATCH);
	   }

}
