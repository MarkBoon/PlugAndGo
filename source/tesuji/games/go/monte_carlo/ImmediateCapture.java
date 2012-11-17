package tesuji.games.go.monte_carlo;

import static tesuji.games.go.common.GoConstant.PASS;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.games.go.tactics.LadderReader;
import tesuji.games.go.tactics.TacticsConstant;
import tesuji.games.go.util.BoardMarker;

public class ImmediateCapture extends AbstractMoveGenerator
{
	private MonteCarloPluginAdministration administration;
	private BoardMarker _boardMarker = new BoardMarker();
	private LadderReader _ladderReader;
	
	@Override
	public void register(MonteCarloPluginAdministration admin)
	{
		administration = admin;
		_ladderReader = new LadderReader(administration.getBoardSize()); // A little space can be saved by sharing this instance.
	}

	@Override
	public int generate()
	{
		if (administration.getMoveStack().getSize()==0)
			return UNDEFINED_COORDINATE;

		byte[] board = administration._board;
		int[] chain = administration._chain;
		int[] liberties = administration._liberties;
		int previousMove = administration.getMoveStack().peek();
		int koPoint = administration._koPoint;
		
		if (previousMove==PASS)
			return UNDEFINED_COORDINATE;
		
		_boardMarker.getNewMarker();

		int currentChain = chain[previousMove];
		if (liberties[currentChain]==1  && _boardMarker.notSet(currentChain) && administration.isPrehistoric(currentChain))
		{
			_boardMarker.set(currentChain);
			_ladderReader.setBoardArray(board);
			_ladderReader.setKoPoint(koPoint);
			if (_ladderReader.tryEscape(previousMove)==TacticsConstant.CANNOT_CATCH)
			{
				int captureXY = administration.getLiberty(previousMove);
				if (administration.isLegal(captureXY))
					return captureXY;
			}
		}

		return UNDEFINED_COORDINATE;
	}

	@Override
	public MoveGenerator createClone()
	{
		return new ImmediateAtariEscape();
	}

	@Override
	public void copyDataFrom(MoveGenerator source)
	{
	}
}
