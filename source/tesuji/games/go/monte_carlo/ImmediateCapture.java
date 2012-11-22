package tesuji.games.go.monte_carlo;

import static tesuji.games.go.common.GoConstant.PASS;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.games.go.tactics.TacticsConstant;
import tesuji.games.go.util.Statistics;

public class ImmediateCapture extends LadderMoveGenerator
{	
	@Override
	public int generate()
	{
		Statistics.increment("-ImmediateCapture");
		if (administration.getMoveStack().getSize()==0)
			return UNDEFINED_COORDINATE;

		byte[] board = administration._board;
		int[] chain = administration._chain;
		int[] liberties = administration._liberties;
		int previousMove = administration.getMoveStack().peek();
		int koPoint = administration._koPoint;
		
		if (previousMove==PASS)
			return UNDEFINED_COORDINATE;
		
		int currentChain = chain[previousMove];
		if (liberties[currentChain]==1  && (!isCheckHistory() || administration.isPrehistoric(currentChain)))
		{
			_ladderReader.setBoardArray(board);
			_ladderReader.setKoPoint(koPoint);
			Statistics.increment("ImmediateCapture");
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
		return new ImmediateCapture();
	}
}
