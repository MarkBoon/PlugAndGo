package tesuji.games.go.monte_carlo;

public interface MoveFilter
{
	void register(MonteCarloPluginAdministration admin);
	boolean accept(int xy, byte color);
	MoveFilter createClone();
	void copyDataFrom(MoveFilter source);
}
