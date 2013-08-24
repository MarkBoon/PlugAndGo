package tesuji.games.go.monte_carlo;

import tesuji.games.go.tactics.LadderReader;
import tesuji.games.go.tactics.TacticsConstant;

public class SafetyFilter
	implements MoveFilter
{
	LadderReader _ladderReader;
	MonteCarloPluginAdministration _administration;
	
//	@Override
	public void clear() {}
	
//	@Override
	public void register(MonteCarloPluginAdministration administration)
	{
		_administration = administration;
		_ladderReader = new LadderReader(administration.getBoardSize());
	}

//	@Override
	public boolean accept(int xy, byte color)
	{
		return (_administration.getProbabilityMap().getWeight(xy,color)<100 || isSafeToMove(xy, color));
	}

//	@Override
    public MoveFilter createClone()
    {
	    return new SafetyFilter();
    }

//	@Override
    public void copyDataFrom(MoveFilter source)
    {
		// NA
    }

   protected boolean isSafeToMove(int moveXY, byte color)
   {
	   if (_administration.getNeighbourArray()[moveXY]<2)
		   return true;
	   
		_ladderReader.setBoardArray(_administration.getBoardArray());
		_ladderReader.setKoPoint(_administration.getKoPoint());
		return (_ladderReader.wouldBeLadder(moveXY,color)==TacticsConstant.CANNOT_CATCH);
   }
}
