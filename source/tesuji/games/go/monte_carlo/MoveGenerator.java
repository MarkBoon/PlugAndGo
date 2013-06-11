package tesuji.games.go.monte_carlo;

public interface MoveGenerator
{
	void clear();
	void register(MonteCarloPluginAdministration administration);
	int generate();
	MoveGenerator createClone();
	void copyDataFrom(MoveGenerator source);
	
	int getUrgency();
	void setUrgency(int urgency);
}
