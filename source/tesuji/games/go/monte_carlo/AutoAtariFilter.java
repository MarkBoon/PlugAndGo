package tesuji.games.go.monte_carlo;

public class AutoAtariFilter
	implements MoveFilter
{
	MonteCarloPluginAdministration _administration;
	
//	@Override
	public void clear() {}
	
//	@Override
	public void register(MonteCarloPluginAdministration administration)
	{
		_administration = administration;
	}

//	@Override
	public boolean accept(int xy, byte color)
	{
		return _administration.isAutoAtari(xy, color);
	}

//	@Override
    public MoveFilter createClone()
    {
	    return new AutoAtariFilter();
    }

//	@Override
    public void copyDataFrom(MoveFilter source)
    {
		// NA
    }
}
