package tesuji.games.go.monte_carlo.move_generator;

import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;

public interface MoveGenerator
{
	void clear();
	void register(MonteCarloPluginAdministration administration);
	int generate();
	void update();
	void setBoardSize(int size);
	MoveGenerator createClone();
	void copyDataFrom(MoveGenerator source);
	
	int getUrgency();
	void setUrgency(int urgency);
	double getWeight();
	void setWeight(double weight);
}
