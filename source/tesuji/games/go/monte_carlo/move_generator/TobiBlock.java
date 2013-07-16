package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.general.ColorConstant.opposite;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import static tesuji.games.go.util.GoArray.*;
import static tesuji.games.general.ColorConstant.*;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.util.FourCursor;
import tesuji.games.go.util.EightCursor;

public class TobiBlock extends LadderMoveGenerator
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
				if (isBlock(neighbour,color) || isBlock(neighbour,opposite(color)))
				{
					if (isSafeToMove(neighbour))
						return neighbour;
				}
			}
			for (int n=0; n<8; n++)
			{
				int neighbour = EightCursor.getNeighbour(xy, n);
				if (isNet(neighbour,color) || isNet(neighbour,opposite(color)))
				{
					if (isSafeToMove(neighbour))
						return neighbour;
				}
			}
		}
		
		return UNDEFINED_COORDINATE;
	}

	private boolean isBlock(int xy, byte color)
	{
		byte[] neighbours = administration.getNeighbourArray();
		byte[] blackNeighbours = administration.getBlackNeighbourArray();
		byte[] whiteNeighbours = administration.getWhiteNeighbourArray();
		byte[] board = administration.getBoardArray();
		
		if (board[xy]!=EMPTY || blackNeighbours[xy]==0 || whiteNeighbours[xy]==0)
			return false;
		
		byte otherColor = opposite(color);
		
		if (board[left(xy)]==otherColor && board[right(xy)]==EMPTY && board[above(xy)]==color && board[below(xy)]==color && (neighbours[right(xy)]==0 || (neighbours[right(xy)]==1 && administration.isFirstRow(right(xy)))))
			return true;
		if (board[right(xy)]==otherColor && board[left(xy)]==EMPTY && board[above(xy)]==color && board[below(xy)]==color && (neighbours[left(xy)]==0 || (neighbours[left(xy)]==1 && administration.isFirstRow(left(xy)))))
			return true;
		if (board[above(xy)]==otherColor && board[below(xy)]==EMPTY && board[left(xy)]==color && board[right(xy)]==color && (neighbours[below(xy)]==0 || (neighbours[below(xy)]==1 && administration.isFirstRow(below(xy)))))
			return true;
		if (board[below(xy)]==otherColor && board[above(xy)]==EMPTY && board[left(xy)]==color && board[right(xy)]==color && (neighbours[above(xy)]==0 || (neighbours[above(xy)]==1 && administration.isFirstRow(above(xy)))))
			return true;

		return false;
	}


	private boolean isNet(int xy, byte color)
	{
		byte[] neighbours = administration.getNeighbourArray();
		byte[] board = administration.getBoardArray();
		
		if (board[xy]!=EMPTY || (neighbours[xy]!=1 && (neighbours[xy]!=2 || !administration.isFirstRow(xy))))
			return false;
		
		byte otherColor = opposite(color);
		
		if (board[left(xy)]==otherColor && board[left_above(xy)]==color && board[left_below(xy)]==color)
			return true;
		if (board[right(xy)]==otherColor && board[right_above(xy)]==color && board[right_below(xy)]==color)
			return true;
		if (board[above(xy)]==otherColor && board[left_above(xy)]==color && board[right_above(xy)]==color)
			return true;
		if (board[below(xy)]==otherColor && board[left_below(xy)]==color && board[right_below(xy)]==color)
			return true;

		return false;
	}

	//	@Override
	public MoveGenerator createClone()
	{
		return new TobiBlock();
	}

//	@Override
//	public void copyDataFrom(MoveGenerator source)
//	{
//		// TODO Auto-generated method stub
//
//	}
}
