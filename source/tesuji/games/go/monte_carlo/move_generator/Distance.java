package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import static tesuji.games.go.util.GoArray.above;
import static tesuji.games.go.util.GoArray.below;
import static tesuji.games.go.util.GoArray.left;
import static tesuji.games.go.util.GoArray.right;
import tesuji.games.general.ColorConstant;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.util.ProbabilityMap;

public class Distance extends AbstractMoveGenerator
{
	@Override
	public int generate()
	{
		ProbabilityMap map = administration.getProbabilityMap();
		byte[] board = administration.getBoardArray();
			
		int previousXY = administration.getBeforeLastMove();
		if (previousXY!=GoConstant.PASS)
		{
			if (board[left(previousXY)]==ColorConstant.EMPTY)
				map.decay(left(previousXY));
			if (board[right(previousXY)]==ColorConstant.EMPTY)
				map.decay(right(previousXY));
			if (board[above(previousXY)]==ColorConstant.EMPTY)
				map.decay(above(previousXY));
			if (board[below(previousXY)]==ColorConstant.EMPTY)
				map.decay(below(previousXY));
			if (board[left(above(previousXY))]==ColorConstant.EMPTY)
				map.decay(left(above(previousXY)));
			if (board[left(below(previousXY))]==ColorConstant.EMPTY)
				map.decay(left(below(previousXY)));
			if (board[right(above(previousXY))]==ColorConstant.EMPTY)
				map.decay(right(above(previousXY)));
			if (board[right(below(previousXY))]==ColorConstant.EMPTY)
				map.decay(right(below(previousXY)));
			if (board[left(previousXY)]==ColorConstant.EMPTY && board[left(left(previousXY))]==ColorConstant.EMPTY)
				map.decay(left(left(previousXY)));
			if (board[right(previousXY)]==ColorConstant.EMPTY && board[right(right(previousXY))]==ColorConstant.EMPTY)
				map.decay(right(right(previousXY)));
			if (board[above(previousXY)]==ColorConstant.EMPTY && board[above(above(previousXY))]==ColorConstant.EMPTY)
				map.decay(above(above(previousXY)));
			if (board[below(previousXY)]==ColorConstant.EMPTY && board[below(below(previousXY))]==ColorConstant.EMPTY)
				map.decay(below(below(previousXY)));
		}
		int xy = administration.getLastMove();
		if (xy!=GoConstant.PASS)
		{
			double weight = getWeight();
			if (board[left(xy)]==ColorConstant.EMPTY)
				map.add(left(xy),weight*2);
			if (board[right(xy)]==ColorConstant.EMPTY)
				map.add(right(xy),weight*2);
			if (board[above(xy)]==ColorConstant.EMPTY)
				map.add(above(xy),weight*2);
			if (board[below(xy)]==ColorConstant.EMPTY)
				map.add(below(xy),weight*2);
			if (board[left(above(xy))]==ColorConstant.EMPTY)
				map.add(left(above(xy)),weight);
			if (board[left(below(xy))]==ColorConstant.EMPTY)
				map.add(left(below(xy)),weight);
			if (board[right(above(xy))]==ColorConstant.EMPTY)
				map.add(right(above(xy)),weight);
			if (board[right(below(xy))]==ColorConstant.EMPTY)
				map.add(right(below(xy)),weight);
			if (board[left(xy)]==ColorConstant.EMPTY && board[left(left(xy))]==ColorConstant.EMPTY)
				map.add(left(left(xy)),weight);
			if (board[right(xy)]==ColorConstant.EMPTY && board[right(right(xy))]==ColorConstant.EMPTY)
				map.add(right(right(xy)),weight);
			if (board[above(xy)]==ColorConstant.EMPTY && board[above(above(xy))]==ColorConstant.EMPTY)
				map.add(above(above(xy)),weight);
			if (board[below(xy)]==ColorConstant.EMPTY && board[below(below(xy))]==ColorConstant.EMPTY)
				map.add(below(below(xy)),weight);
		}
		
		return UNDEFINED_COORDINATE;
	}

	@Override
	public MoveGenerator createClone()
	{
		return new Distance();
	}

//	@Override
//	public void copyDataFrom(MoveGenerator source)
//	{
//		// TODO Auto-generated method stub
//
//	}
}
