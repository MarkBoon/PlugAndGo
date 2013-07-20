package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.general.ColorConstant.EMPTY;
import static tesuji.games.general.ColorConstant.opposite;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import static tesuji.games.go.util.GoArray.*;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.util.FourCursor;

public class FirstLineAtari extends LadderMoveGenerator
{
//	@Override
	public int generate()
	{
		int xy = administration.getLastMove();
		if (xy!=GoConstant.PASS)
		{
			byte color = administration.getColorToMove();
			for (int n=0; n<4; n++)
			{
				int neighbour = FourCursor.getNeighbour(xy, n);
				if (isBlock(neighbour,color))
				{
//					if (isSafeToMove(neighbour))
						return neighbour;
				}
			}
		}
		
		return UNDEFINED_COORDINATE;
	}

	private boolean isBlock(int xy, byte color)
	{
		if (!administration.isFirstRow(xy))
			return false;
		
		byte[] neighbours = administration.getNeighbourArray();
		byte[] ownDiagonalNeighbours = administration.getOwnDiagonalAray();
		byte[] otherNeighbours = administration.getOtherDiagonalAray();
		byte[] board = administration.getBoardArray();
		int[] liberties = administration.getLibertyArray();
		int[] chain = administration.getChainArray();
		
		if (board[xy]!=EMPTY || otherNeighbours[xy]!=2 || neighbours[xy]>3 || ownDiagonalNeighbours[xy]==0)
			return false;
		
		byte oppositeColor = opposite(color);

		if (administration.isFirstRow(left(xy)) && board[left(xy)]==oppositeColor && liberties[chain[left(xy)]]==2 && board[right(xy)]==EMPTY )
			return true;
		if (administration.isFirstRow(right(xy)) && board[right(xy)]==oppositeColor && liberties[chain[right(xy)]]==2 && board[left(xy)]==EMPTY )
			return true;
		if (administration.isFirstRow(above(xy)) && board[above(xy)]==oppositeColor && liberties[chain[above(xy)]]==2 && board[below(xy)]==EMPTY )
			return true;
		if (administration.isFirstRow(below(xy)) && board[below(xy)]==oppositeColor && liberties[chain[below(xy)]]==2 && board[above(xy)]==EMPTY )
			return true;

		return false;
	}

	//	@Override
	public MoveGenerator createClone()
	{
		return new FirstLineAtari();
	}

//	@Override
//	public void copyDataFrom(MoveGenerator source)
//	{
//		// TODO Auto-generated method stub
//
//	}
}
