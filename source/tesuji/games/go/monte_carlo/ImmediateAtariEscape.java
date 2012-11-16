package tesuji.games.go.monte_carlo;

import static tesuji.games.go.common.GoConstant.PASS;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.games.go.tactics.LadderReader;
import tesuji.games.go.tactics.TacticsConstant;
import tesuji.games.go.util.BoardMarker;
import tesuji.games.go.util.FourCursor;

public class ImmediateAtariEscape implements MoveGenerator
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
		byte colorToMove = administration.getColorToMove();
		int previousMove = administration.getMoveStack().peek();
		int koPoint = administration._koPoint;
		
		if (previousMove==PASS)
			return UNDEFINED_COORDINATE;
		
		_boardMarker.getNewMarker();

		for (int n=0; n<4; n++)
		{
			int next = FourCursor.getNeighbour(previousMove, n);
			if (board[next]==colorToMove)
			{
				int chainNext = chain[next];
				if (_boardMarker.notSet(chainNext))
				{
					if (liberties[chainNext]==1 && administration.isPrehistoric(chainNext))
					{
						_boardMarker.set(chainNext);
						_ladderReader.setBoardArray(board);
						_ladderReader.setKoPoint(koPoint);
						if (_ladderReader.tryEscape(next)==TacticsConstant.CANNOT_CATCH)
						{
							int escapeXY = _ladderReader.getLastLadderMove();
							if (escapeXY!=PASS && escapeXY!=UNDEFINED_COORDINATE)
								return escapeXY;
						}
					}
				}
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
