package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.general.ColorConstant.opposite;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import static tesuji.games.go.util.GoArray.above;
import static tesuji.games.go.util.GoArray.below;
import static tesuji.games.go.util.GoArray.left;
import static tesuji.games.go.util.GoArray.right;
import tesuji.games.general.ColorConstant;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.util.EightCursor;

public class Connect extends LadderMoveGenerator
{
//	@Override
	public int generate()
	{
		int xy = administration.getLastMove();
		if (xy!=GoConstant.PASS)
		{
			byte oppositeColor = ColorConstant.opposite(administration.getColorToMove());
			for (int n=0; n<4; n++)
			{
				int neighbour = EightCursor.getNeighbour(xy, n);
				if (isConnection(neighbour,oppositeColor) && isSafeToMove(neighbour))
					administration.getProbabilityMap().add(neighbour, getWeight());
			}
		}
		
		return UNDEFINED_COORDINATE;
	}

	private boolean isConnection(int xy, byte color)
	{
		byte[] blackNeighbours = administration.getBlackNeighbourArray();
		byte[] whiteNeighbours = administration.getWhiteNeighbourArray();
		byte[] board = administration.getBoardArray();
		int[] liberties = administration.getLibertyArray();
		int[] chain = administration.getChainArray();
		
		if (board[xy]!=ColorConstant.EMPTY || blackNeighbours[xy]==0 || whiteNeighbours[xy]==0)
			return false;
		
		byte otherColor = opposite(color);
		if (board[left(xy)]==otherColor && board[above(xy)]==otherColor && board[left(above(xy))]==color 
				&& liberties[chain[left(above(xy))]]!=1 && (board[right(xy)]==color || board[below(xy)]==color))
			return true;
		if (board[left(xy)]==otherColor && board[below(xy)]==otherColor && board[left(below(xy))]==color
			&& liberties[chain[left(below(xy))]]!=1 && (board[right(xy)]==color || board[above(xy)]==color))
			return true;
		if (board[right(xy)]==otherColor && board[above(xy)]==otherColor && board[right(above(xy))]==color
			&& liberties[chain[right(above(xy))]]!=1 && (board[left(xy)]==color || board[below(xy)]==color))
			return true;
		if (board[right(xy)]==otherColor && board[below(xy)]==otherColor && board[right(below(xy))]==color
			&& liberties[chain[right(below(xy))]]!=1 && (board[left(xy)]==color || board[above(xy)]==color))
			return true;
		
		return false;
	}

//	@Override
	public MoveGenerator createClone()
	{
		return new Connect();
	}

//	@Override
//	public void copyDataFrom(MoveGenerator source)
//	{
//		// TODO Auto-generated method stub
//
//	}
}
