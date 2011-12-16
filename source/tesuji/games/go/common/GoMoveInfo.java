/**
 * 
 */
package tesuji.games.go.common;

import tesuji.core.util.ArrayStack;
import tesuji.core.util.FlyWeight;
import tesuji.games.go.util.ArrayFactory;
import tesuji.games.go.util.ChainStack;
import tesuji.games.go.util.IntStack;

public class GoMoveInfo
	implements FlyWeight
{
	private ChainStack mergeList;
	private ChainStack captiveList;
	private IntStack newLiberties;
	private IntStack newNeighbours;

	private ArrayStack<GoMoveInfo> owner;
	
	
	GoMoveInfo(ArrayStack<GoMoveInfo> owner)
	{
		this.owner = owner;
		
		mergeList = ArrayFactory.createNeighbourList();
		captiveList = ArrayFactory.createNeighbourList();
		newLiberties = ArrayFactory.createSmallIntStack();
		newNeighbours = ArrayFactory.createSmallIntStack();		
	}
	
	/**
	 * @see tesuji.core.util.FlyWeight#recycle()
	 */
	public final void recycle()
	{
		for (int i=mergeList.getSize(); --i>=0;)
			mergeList.get(i).recycle();

		for (int i=captiveList.getSize(); --i>=0;)
			captiveList.get(i).recycle();

		mergeList.clear();
		captiveList.clear();
		newLiberties.clear();
		newNeighbours.clear();
		
		owner.push(this);
	}

	public ChainStack getCaptiveList()
    {
    	return captiveList;
    }

	public ChainStack getMergeList()
    {
    	return mergeList;
    }

	public IntStack getNewLiberties()
    {
    	return newLiberties;
    }

	public IntStack getNewNeighbours()
    {
    	return newNeighbours;
    }
	
}
