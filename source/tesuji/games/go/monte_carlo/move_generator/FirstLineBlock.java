package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.general.ColorConstant.EMPTY;
import static tesuji.games.general.ColorConstant.opposite;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import static tesuji.games.go.util.GoArray.*;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.util.FourCursor;

public class FirstLineBlock extends LadderMoveGenerator
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
		
		if (board[xy]!=EMPTY || otherNeighbours[xy]!=2 || neighbours[xy]>3 || ownDiagonalNeighbours[xy]==0)
			return false;
		
		byte oppositeColor = opposite(color);

		if (administration.isFirstRow(left(xy)) && board[left(xy)]==oppositeColor && board[right(above(xy))]!=color && board[right(below(xy))]!=color)
			return true;
		if (administration.isFirstRow(right(xy)) && board[right(xy)]==oppositeColor && board[left(above(xy))]!=color && board[left(below(xy))]!=color)
			return true;
		if (administration.isFirstRow(above(xy)) && board[above(xy)]==oppositeColor && board[below(left(xy))]!=color && board[below(right(xy))]!=color)
			return true;
		if (administration.isFirstRow(below(xy)) && board[below(xy)]==oppositeColor && board[above(left(xy))]!=color && board[above(right(xy))]!=color)
			return true;

		return false;
	}

	//	@Override
	public MoveGenerator createClone()
	{
		return new FirstLineBlock();
	}

//	@Override
//	public void copyDataFrom(MoveGenerator source)
//	{
//		// TODO Auto-generated method stub
//
//	}
}
