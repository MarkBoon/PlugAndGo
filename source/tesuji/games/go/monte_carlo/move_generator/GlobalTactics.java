package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.general.ColorConstant.opposite;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.games.go.tactics.TacticsConstant;
import tesuji.games.go.util.BoardMarker;
import tesuji.games.go.util.Statistics;

import static tesuji.games.go.util.GoArray.*;

public class GlobalTactics extends LadderMoveGenerator
{	
	private BoardMarker _boardMarker = new BoardMarker();

	@Override
	public int generate()
	{
		Statistics.increment("-ImmediateLadder");
		if (administration.getMoveStack().getSize()==0)
			return UNDEFINED_COORDINATE;

		byte[] board = administration.getBoardArray();
		int[] chain = administration.getChainArray();
		int[] liberties = administration.getLibertyArray();
		int previousMove = administration.getLastMove();
		byte colorToMove = administration.getColorToMove();
		byte oppositeColor = opposite(colorToMove);
		int koPoint = administration.getKoPoint();

		_boardMarker.getNewMarker();
		for (int i=FIRST; i<=LAST; i++)
		{
			int c = chain[i];
			if (i!=previousMove && !isNeighbour(i,previousMove) && _boardMarker.notSet(c))
			{
				_boardMarker.set(c);
				byte boardValue = board[i];
				if (boardValue==colorToMove && liberties[c]==1)
				{
					_ladderReader.setBoardArray(board);
					_ladderReader.setKoPoint(koPoint);
					if (_ladderReader.tryEscape(i)==TacticsConstant.CANNOT_CATCH)
					{
						int escapeXY = _ladderReader.getLastLadderMove();
						if (administration.isLegal(escapeXY))
						{
							administration.addPriorityMove(escapeXY, getUrgency(), getUrgency(), getUrgency());
						}
					}
				}
				if (boardValue==oppositeColor && liberties[c]==1)
				{
					_ladderReader.setBoardArray(board);
					_ladderReader.setKoPoint(koPoint);
					if (_ladderReader.tryEscape(i)==TacticsConstant.CANNOT_CATCH)
					{
						int captureXY = administration.getLiberty(i);
						if (administration.isLegal(captureXY))
						{
							administration.addPriorityMove(captureXY, getUrgency(), getUrgency(), getUrgency());
						}
					}
				}
				if (boardValue==oppositeColor && liberties[c]==2)
				{
					_ladderReader.setBoardArray(board);
					_ladderReader.setKoPoint(koPoint);
					if (_ladderReader.tryLadder(i)==TacticsConstant.CAN_CATCH)
					{
						int ladderXY = _ladderReader.getLastLadderMove();
						administration.addPriorityMove(ladderXY, getUrgency(), getUrgency(), getUrgency());
					}
				}
			}
		}

		return UNDEFINED_COORDINATE;
	}

	@Override
	public MoveGenerator createClone()
	{
		return new GlobalTactics();
	}
}
