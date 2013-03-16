package tesuji.games.go.monte_carlo.move_generator;

import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;

public interface MoveGenerator
{
	void register(MonteCarloPluginAdministration admin);
	int generate();
	void update();
	MoveGenerator createClone();
	void copyDataFrom(MoveGenerator source);
	
	int getUrgency();
	void setUrgency(int urgency);
	double getWeight();
	void setWeight(double weight);
}
