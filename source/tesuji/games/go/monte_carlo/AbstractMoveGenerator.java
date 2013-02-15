package tesuji.games.go.monte_carlo;

public abstract class AbstractMoveGenerator implements MoveGenerator
{
	private int _urgency;

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

	public void update()
	{
	}
}
