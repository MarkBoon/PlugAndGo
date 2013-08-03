package tesuji.games.go.monte_carlo.move_generator;

import static tesuji.games.go.common.GoConstant.UNDEFINED_COORDINATE;
import tesuji.games.go.util.FourCursor;
import tesuji.games.go.util.IntStack;

public class NakadeMove extends AbstractMoveGenerator
{
//	@Override
	public int generate()
	{
		IntStack captiveStack = administration.getCaptiveStack();
		int nrCaptives = captiveStack.getSize();
		if (nrCaptives>=1 && nrCaptives<=6 && nrCaptives!=4)
		{
			byte[] neighbours = administration.getNeighbourArray();
			byte[] diagonalNeighbours = administration.getOtherDiagonalAray();
			byte[] maxDiagonalNeighbours = administration.getMaxDiagonalArray();
			
			for (int i=0; i<nrCaptives; i++)
			{
				int c = captiveStack.get(i);
				if (neighbours[c]<3)
				{
					for (int n=0; n<4; n++)
					{
						int neighbour = FourCursor.getNeighbour(c, n);
						if (neighbours[neighbour]==3 && diagonalNeighbours[neighbour]<maxDiagonalNeighbours[neighbour])
							return c;
					}						
				}
			}
		}	
		
		return UNDEFINED_COORDINATE;
	}

//	@Override
	public MoveGenerator createClone()
	{
		return new NakadeMove();
	}
}
