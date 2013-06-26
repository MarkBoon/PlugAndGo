package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.general.ColorConstant.opposite;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import static tesuji.games.go.util.GoArray.above;
import static tesuji.games.go.util.GoArray.below;
import static tesuji.games.go.util.GoArray.left;
import static tesuji.games.go.util.GoArray.right;
import tesuji.games.general.ColorConstant;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.util.FourCursor;

public class KeimaBlock extends LadderMoveGenerator
{
//	@Override
	public int generate()
	{
		int xy = administration.getLastMove();
		if (xy!=GoConstant.PASS)
		{
			for (int n=0; n<4; n++)
			{
				int neighbour = FourCursor.getNeighbour(xy, n);
				if ((isKeimaBlock(neighbour,ColorConstant.BLACK) || isKeimaBlock(neighbour,ColorConstant.WHITE)) && isSafeToMove(neighbour))
					administration.getProbabilityMap().add(neighbour, getWeight());
			}
		}
		
		return UNDEFINED_COORDINATE;
	}

	private boolean isKeimaBlock(int xy, byte color)
	{
		byte[] neighbours = administration.getNeighbourArray();
		byte[] board = administration.getBoardArray();
		
		if (board[xy]!=ColorConstant.EMPTY || neighbours[xy]>3 || (neighbours[xy]==3 && administration.isFirstRow(xy)))
			return false;
		
		int[] liberties = administration.getLibertyArray();
		int[] chain = administration.getChainArray();

		byte otherColor = opposite(color);
		if (board[left(xy)]==color && board[above(xy)]==otherColor && board[left(below(xy))]==otherColor
			&& liberties[chain[left(xy)]]!=1)
			return true;
		if (board[left(xy)]==color && board[below(xy)]==otherColor && board[left(above(xy))]==otherColor
						&& liberties[chain[left(xy)]]!=1)
			return true;
		if (board[right(xy)]==color && board[above(xy)]==otherColor && board[right(below(xy))]==otherColor
						&& liberties[chain[right(xy)]]!=1)
			return true;
		if (board[right(xy)]==color && board[below(xy)]==otherColor && board[right(above(xy))]==otherColor
						&& liberties[chain[right(xy)]]!=1)
			return true;
		if (board[above(xy)]==color && board[left(xy)]==otherColor && board[above(right(xy))]==otherColor
						&& liberties[chain[above(xy)]]!=1)
			return true;
		if (board[above(xy)]==color && board[right(xy)]==otherColor && board[above(left(xy))]==otherColor
						&& liberties[chain[above(xy)]]!=1)
			return true;
		if (board[below(xy)]==color && board[left(xy)]==otherColor && board[below(right(xy))]==otherColor
						&& liberties[chain[below(xy)]]!=1)
			return true;
		if (board[below(xy)]==color && board[right(xy)]==otherColor && board[below(left(xy))]==otherColor
						&& liberties[chain[below(xy)]]!=1)
			return true;

		return false;
	}
	
//	@Override
	public MoveGenerator createClone()
	{
		return new KeimaBlock();
	}

//	@Override
//	public void copyDataFrom(MoveGenerator source)
//	{
//		// TODO Auto-generated method stub
//
//	}
}
