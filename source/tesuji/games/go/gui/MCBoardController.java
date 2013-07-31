package tesuji.games.go.gui;

import org.cliffc.high_scale_lib.NonBlockingHashMapLong;

import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.search.MonteCarloHashMapResult;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.IntStack;
import tesuji.games.model.BoardModel;

public class MCBoardController
{
	private MonteCarloPluginAdministration _initialMCAdministration;
	private MonteCarloPluginAdministration _mcAdministration;
	private NonBlockingHashMapLong<MonteCarloHashMapResult> _hashMap;
	private IntStack moveStack = new IntStack(1000,null);
	private MonteCarloHashMapResult _result;
	
	public MCBoardController(MonteCarloPluginAdministration mcAdministration, NonBlockingHashMapLong<MonteCarloHashMapResult> hashMap)
	{
		_initialMCAdministration = (MonteCarloPluginAdministration)mcAdministration.createClone();
		_mcAdministration = (MonteCarloPluginAdministration)_initialMCAdministration.createClone();
		_hashMap = hashMap;
	}
	
	public BoardModel getBoardModel()
	{
		return _mcAdministration.getBoardModel();
	}
	
	public MonteCarloHashMapResult getResult()
	{
		_result = _hashMap.get(_mcAdministration.getPositionalChecksum());
		return _result;
	}
	
	public void play(int x, int y)
	{
		int xy = GoArray.toXY(x, y);
		_mcAdministration.playMove(xy);
		moveStack.push(xy);
	}
	
	public void takeBack()
	{
		if (moveStack.isEmpty())
			return;

		moveStack.pop();
		_mcAdministration = (MonteCarloPluginAdministration)_initialMCAdministration.createClone();
		for (int i=0; i<moveStack.getSize(); i++)
			_mcAdministration.playMove(moveStack.get(i));
	}
}
