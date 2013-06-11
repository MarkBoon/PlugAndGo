package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.go.common.GoConstant.PASS;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.games.go.tactics.TacticsConstant;
import tesuji.games.go.util.Statistics;

public class ImmediateLadder extends LadderMoveGenerator
{	
	@Override
	public int generate()
	{
		Statistics.increment("-ImmediateLadder");
		if (administration.getMoveStack().getSize()==0)
			return UNDEFINED_COORDINATE;

		byte[] board = administration.getBoardArray();
		int[] chain = administration.getChainArray();
		int[] liberties = administration.getLibertyArray();
		byte[] ownDiagonalNeighbours = administration.getOwnDiagonalAray();
		byte[] otherDiagonalNeighbours = administration.getOtherDiagonalAray();
		byte[] maxDiagonalsOccupied = administration.getMaxDiagonalArray();
		int previousMove = administration.getMoveStack().peek();
		int koPoint = administration.getKoPoint();
		
		if (previousMove==PASS)
			return UNDEFINED_COORDINATE;
		
		int currentChain = chain[previousMove];
		if (liberties[currentChain]==2 && (!isCheckHistory() || administration.isPrehistoricChain(currentChain))
			&& (maxDiagonalsOccupied[previousMove]>1 || ownDiagonalNeighbours[previousMove]>0 || otherDiagonalNeighbours[previousMove]>0))
		{
			_ladderReader.setBoardArray(board);
			_ladderReader.setKoPoint(koPoint);
			Statistics.increment("ImmediateLadder");
			if (_ladderReader.tryLadder(previousMove)==TacticsConstant.CAN_CATCH)
			{
				int ladderXY = _ladderReader.getLastLadderMove();
				if (administration.isLegal(ladderXY))
				{
					administration.getProbabilityMap().add(ladderXY, 0.3);
					//return ladderXY;
				}
			}
		}

		return UNDEFINED_COORDINATE;
	}

	@Override
	public MoveGenerator createClone()
	{
		return new ImmediateLadder();
	}
}
