package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.go.common.GoConstant.PASS;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.games.go.tactics.TacticsConstant;
import tesuji.games.go.util.BoardMarker;
import tesuji.games.go.util.FourCursor;
import tesuji.games.go.util.Statistics;

public class ImmediateAtariEscape extends LadderMoveGenerator
{
	private BoardMarker _boardMarker = new BoardMarker();
	
	@Override
	public int generate()
	{
		Statistics.increment("-ImmediateEscape");
		if (administration.getMoveStack().getSize()==0)
			return UNDEFINED_COORDINATE;

		byte[] board = administration.getBoardArray();
		int[] chain = administration.getChainArray();
		int[] liberties = administration.getLibertyArray();
		byte colorToMove = administration.getColorToMove();
		int previousMove = administration.getMoveStack().peek();
		int koPoint = administration.getKoPoint();
		
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
					if (liberties[chainNext]==1 && (!isCheckHistory() || administration.isPrehistoricChain(chainNext)))
					{
						_boardMarker.set(chainNext);
						_ladderReader.setBoardArray(board);
						_ladderReader.setKoPoint(koPoint);
						Statistics.increment("ImmediateEscape");
						if (_ladderReader.tryEscape(next)==TacticsConstant.CANNOT_CATCH)
						{
							int escapeXY = _ladderReader.getLastLadderMove();
							if (escapeXY!=PASS && escapeXY!=UNDEFINED_COORDINATE)
							{
								administration.getProbabilityMap().add(escapeXY, 0.5);
								//return escapeXY;
							}
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
}
