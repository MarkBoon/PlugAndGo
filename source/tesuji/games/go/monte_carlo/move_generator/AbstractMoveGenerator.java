package tesuji.games.go.monte_carlo.move_generator;

public abstract class AbstractMoveGenerator implements MoveGenerator
{
	private int _urgency;
	private double _weight;

	@Override
	public int getUrgency()
	{
		return _urgency;
	}

	@Override
	public void setUrgency(int urgency)
	{
		_urgency = urgency;
	}

	@Override
	public double getWeight()
	{
		return _weight;
	}

	@Override
	public void setWeight(double weight)
	{
		_weight = weight;
	}

	public void update()
	{
	}
	
	@Override
	public void copyDataFrom(MoveGenerator source)
	{
		_urgency = source.getUrgency();
		_weight = source.getWeight();
	}

}
