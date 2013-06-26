package tesuji.games.go.monte_carlo;

import tesuji.games.general.ColorConstant;
import static tesuji.games.go.util.GoArray.*;

public class EyeMoveFilter
	implements MoveFilter
{
	private byte[] _blackNeighbours;
	private byte[] _whiteNeighbours;
	private byte[] _blackDiagonalNeighbours;
	private byte[] _whiteDiagonalNeighbours;
	private byte[] _maxDiagonalsOccupied;
	private int[] _liberties;
	private int[] _chain;
	
	MonteCarloPluginAdministration _administration;
	
//	@Override
	public void clear() {}
	
//	@Override
	public void register(MonteCarloPluginAdministration administration)
	{
		_administration = administration;
		
		_blackNeighbours = administration.getBlackNeighbourArray();
		_whiteNeighbours = administration.getWhiteNeighbourArray();
		
		_blackDiagonalNeighbours = administration.getBlackDiagonalNeighbourArray();
		_whiteDiagonalNeighbours = administration.getWhiteDiagonalNeighbourArray();
		
		_maxDiagonalsOccupied = administration.getMaxDiagonalArray();
		
		_liberties = administration.getLibertyArray();
		_chain = administration.getChainArray();
	}

//	@Override
	public boolean accept(int xy, byte color)
	{
		if (_liberties[_chain[left(xy)]]==1 || _liberties[_chain[right(xy)]]==1 || _liberties[_chain[above(xy)]]==1 || _liberties[_chain[below(xy)]]==1)
			return false;

		if (color==ColorConstant.BLACK)
			return (_blackNeighbours[xy]==4 && _whiteDiagonalNeighbours[xy]<_maxDiagonalsOccupied[xy]);
		else
			return (_whiteNeighbours[xy]==4 && _blackDiagonalNeighbours[xy]<_maxDiagonalsOccupied[xy]);
	}

//	@Override
    public MoveFilter createClone()
    {
	    return new EyeMoveFilter();
    }

//	@Override
    public void copyDataFrom(MoveFilter source)
    {
		// NA
    }

}
