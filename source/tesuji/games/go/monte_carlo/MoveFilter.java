package tesuji.games.go.monte_carlo;

public interface MoveFilter
{
	void clear();
	void register(MonteCarloPluginAdministration administration);
	boolean accept(int xy, byte color);
	MoveFilter createClone();
	void copyDataFrom(MoveFilter source);
}
