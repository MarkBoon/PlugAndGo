package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.util.EightCursor;
import tesuji.games.go.util.Statistics;

public class EyeMove extends AbstractMoveGenerator
{
//	@Override
	public int generate()
	{
		int xy = administration.getLastMove();
		if (xy!=GoConstant.PASS)
		{
			byte[] board = administration.getBoardModel().getSingleArray();
			for (int n=0; n<8; n++)
			{
				int next = EightCursor.getNeighbour(xy, n);
				if (board[next]==EMPTY)
				{
					int eyeXY = administration.getDiagonalHalfEye(next);
					if (eyeXY!=0 && !administration.isAutoAtari(eyeXY,administration.getColorToMove()))
					{
						Statistics.increment("EyeMove");
						administration.addPriorityMove(eyeXY, getUrgency());
//						administration.getProbabilityMap().add(eyeXY,getUrgency());
					}
					eyeXY = administration.getHalfEye(next);
					if (eyeXY!=0)
					{
						Statistics.increment("EyeMove");
						administration.addPriorityMove(eyeXY, getUrgency());
//						administration.getProbabilityMap().add(eyeXY,getUrgency());
					}
				}
			}
		}
		
		return UNDEFINED_COORDINATE;
	}

//	@Override
	public MoveGenerator createClone()
	{
		return new EyeMove();
	}
}
