/**
 * Project: Tesuji Go Framework.<br>
 * <br>
 * <font color="#CC6600"><font size=-1> Copyright (c) 1985-2006 Mark Boon<br>
 * All rights reserved.<br>
 * <br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * provided that the above copyright notice(s) and this permission notice appear
 * in all copies of the Software and that both the above copyright notice(s) and
 * this permission notice appear in supporting documentation.<br>
 * <br>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.<br>
 * <br>
 * <font color="#00000"><font size=+1>
 * 
 */
package tesuji.games.go.common;

import tesuji.core.util.FlyWeight;
import tesuji.core.util.SynchronizedArrayStack;
import tesuji.games.go.util.ArrayFactory;
import tesuji.games.go.util.IntStack;
import tesuji.games.go.util.UniqueList;

// TODO - move this to its own package, together with ChainFactory.

/**
 * Class representing a 'chain'. A chain is a collection
 * of connected stones that share their liberties.
 */
public class Chain
	implements FlyWeight
{
	/** Flag whether a chain keeps it's neighbours or not. */
	public static final boolean KEEP_NEIGHBOURS = true;
	
	// Location of the chain
	protected int			_xy;
	
	// Color of the chain
	protected byte			_color;
	
	// List of coordinates of the stones in the chain
	protected IntStack		_stoneList;
	
	// List of coordinates of the liberties of the chain.
	protected UniqueList	_libertyList;
	
	protected UniqueList	_neighbourList;
	
	// Owner to which to return the object when it needs recycling.
	protected SynchronizedArrayStack<Chain> _owner;
	
	/**
	 * Default constructor
	 */
	Chain(SynchronizedArrayStack<Chain> owner)
	{
		_owner = owner;
		_stoneList = ArrayFactory.createIntStack();
		_libertyList = ArrayFactory.createUniqueList();
		if (KEEP_NEIGHBOURS)
			_neighbourList = ArrayFactory.createUniqueList();
	}
	
	public void init(int xy, byte color)
	{
		_xy = xy;
		_color = color;
		_stoneList.push(xy);
	}

	/**
	 * Expand the chain with a stone at the given coordinate.
	 * 
	 * @param stoneXY to be added to the chain's stone-list
	 */
	public final void addStone(int stoneXY)
	{
		_stoneList.push(stoneXY);
	}
	
	/**
	 * Remove a stone from a chain's stone-list.
	 * @param stoneXY
	 */
	public final void removeStone(int stoneXY)
	{
		_stoneList.remove(stoneXY);
	}
	
	/**
	 * Add a liberty to the chain's liberty-list, provided it didn'talready have it.
	 * 
	 * @param libertyXY coordinate of the liberty to add
	 * 
	 * @return whether aliberty was indeed added or not
	 */
	public final boolean addLiberty(int libertyXY)
	{
		return (_libertyList.add(libertyXY));
	}
	
	/**
	 * Remove a liberty from a chain's liberty-list.
	 * If the liberty wasn't in the list to begin with,
	 * this method basically does nothing.
	 * 
	 * @param libertyXY coordinate of the liberty to remove
	 * 
	 * @return the number of liberties of the chain after removal.
	 */
	public final int removeLiberty(int libertyXY)
	{
		_libertyList.remove(libertyXY);
		return _libertyList.getSize();
	}
	
	/**
	 * Add a chain to the list of neighbouring chains. This is done
	 * by adding the chain's reference-point to a UniqueList.
	 * 
	 * @param chainXY reference coordinate of a chain
	 * 
	 * @return whether the chain was added, or whether itwas already in the list.
	 */
	public final boolean addNeighbourChain(int chainXY)
	{
		return _neighbourList.add(chainXY);
	}
	
	/**
	 * Remove a chain from thelist of neighbouring chains.
	 * 
	 * @param chainXY reference coordinate of a chain
	 */
	public final void removeNeighbourChain(int chainXY)
	{
		_neighbourList.remove(chainXY);
	}
	
	/**
	 * @see tesuji.core.util.FlyWeight#recycle()
	 */
	public void recycle()
	{
		_stoneList.clear();
		_libertyList.clear();
		
		if (KEEP_NEIGHBOURS)
			_neighbourList.clear();
		
		_owner.push(this);
	}
	
	public Chain createClone()
	{
		Chain newChain = ChainFactory.getSingleton().createChain();
		newChain._color = _color;
		newChain._xy = _xy;
		newChain._stoneList.copyFrom(_stoneList);
		newChain._libertyList.copyFrom(_libertyList);
		
		return newChain;
	}
	
	/**
	 * @return Returns the libertyList.
	 */
	public final UniqueList getLibertyList()
	{
		return _libertyList;
	}
	
	/**
	 * @return Returns the stoneList.
	 */
	public final IntStack getStoneList()
	{
		return _stoneList;
	}
	/**
	 * @return Returns the neighbourList.
	 */
	public final UniqueList getNeighbourList()
	{
		return _neighbourList;
	}

	/**
	 * @return Returns the nrLiberties.
	 */
	public final int getNrLiberties()
	{
		return _libertyList.getSize();
	}
	
	/**
	 * @return Returns the xy.
	 */
	public final int getXY()
	{
		return _xy;
	}
	
	/**
	 * @return Returns the color.
	 */
	public final byte getColor()
	{
		return _color;
	}
}
