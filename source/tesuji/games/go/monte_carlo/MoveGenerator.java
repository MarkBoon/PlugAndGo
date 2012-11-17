package tesuji.games.go.monte_carlo;

public interface MoveGenerator
{
	void register(MonteCarloPluginAdministration admin);
	int generate();
	MoveGenerator createClone();
	void copyDataFrom(MoveGenerator source);
	
	int getUrgency();
	void setUrgency(int urgency);
}
