package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.go.common.GoConstant.PASS;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.games.go.tactics.TacticsConstant;
import tesuji.games.go.util.Statistics;

public class ImmediateCapture extends LadderMoveGenerator
{	
//	@Override
	public int generate()
	{
		Statistics.increment("-ImmediateCapture");
		if (administration.getMoveStack().getSize()==0)
			return UNDEFINED_COORDINATE;

		byte[] board = administration.getBoardArray();
		int[] chain = administration.getChainArray();
		int[] liberties = administration.getLibertyArray();
		int previousMove = administration.getMoveStack().peek();
		int koPoint = administration.getKoPoint();
		
		if (previousMove==PASS)
			return UNDEFINED_COORDINATE;
		
		int currentChain = chain[previousMove];
		if (liberties[currentChain]==1  && (!isCheckHistory() || administration.isPrehistoricChain(currentChain)))
		{
			_ladderReader.setBoardArray(board);
			_ladderReader.setKoPoint(koPoint);
			Statistics.increment("ImmediateCapture");
			if (_ladderReader.tryEscape(previousMove)==TacticsConstant.CANNOT_CATCH)
			{
				int captureXY = administration.getLiberty(previousMove);
				if (administration.isLegal(captureXY))
				{
					administration.getProbabilityMap().add(captureXY, 0.4);
//					return captureXY;
				}
			}
		}

		return UNDEFINED_COORDINATE;
	}

//	@Override
	public MoveGenerator createClone()
	{
		return new ImmediateCapture();
	}
}
