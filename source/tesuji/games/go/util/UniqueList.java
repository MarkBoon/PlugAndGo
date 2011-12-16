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
package tesuji.games.go.util;

import tesuji.core.util.FlyWeight;
import tesuji.core.util.SynchronizedArrayStack;

/**
 * This class acts just like IntStack, except that it won't allow the same value
 * in the list more than once. The restriction is that the values must be within
 * the range of 0 and GoArray.MAX.<br>
 * <br>
 * Typically this class is used to store (small) lists of coordinates on a
 * go-board.
 */
public class UniqueList
	implements FlyWeight
{
	// This number is used to decide whether the list should be cleaned by
	// iterating over the members or by copying a whole array with zeros.
	private static final int SMALL_NR_OF_ITEMS = 10;
	// Maximum number of items in the list.
	private static final int MAX = 361;
	
	// Here we record what items are already in the list.
	private byte[] bitmap;
	// Most calls delegate to this list.
	private IntStack list;
	
	 // This is the stack that owns the object when it gets
	 // created and to which it gets recycled after use.
	private SynchronizedArrayStack<UniqueList> owner;

	/**
	 * UniqueList default constructor
	 */
	public UniqueList()
	{
		list = new IntStack(MAX);
		bitmap = GoArray.createBytes();
	}

	/**
	 * UniqueList constructor with it's owner for recycling
	 * 
	 * @param ownerStack
	 */
	public UniqueList(SynchronizedArrayStack<UniqueList> ownerStack)
	{
		this();
		owner = ownerStack;
	}
	
	/**
	 * Add a point to the list, but only if it's not already in the list.
	 * 
	 * @param xy coordinate of the point to add
	 * 
	 * @return whether it was added or not.
	 */
	public final boolean add(int xy)
	{
		if (bitmap[xy]!=0)
			return false;

		list.push(xy);
		bitmap[xy] = 1; //(byte)list.getSize(); //!!!
		return true;
	}
	
	/**
	 * Add all points from another list
	 * 
	 * @param otherList
	 */
	public void add(UniqueList otherList)
	{
		for (int i=otherList.getSize(); --i>=0;)
			add(otherList.get(i));
	}
	
	/**
	 * Test whether a coordinate is in the list.
	 * 
	 * @param xy coordinate to test
	 * 
	 * @return whether in the list or not.
	 */
	public final boolean hasMember(int xy)
	{
		return (bitmap[xy]!=0);
	}
	
	/**
	 * @return whether the list is empty or not.
	 */
	public final boolean isEmpty()
	{
		return list.isEmpty();
	}
	
	/**
	 * @return the number of coordinates in the list.
	 */
	public final int getSize()
	{
		return list.getSize();
	}
	
	/**
	 * Make the list empty.
	 */
	public final void clear()
	{
		list.clear();
		GoArray.clear(bitmap);
	}

	/**
	 * Random-access function in the list.
	 * @param index in the list
	 * 
	 * @return coordinate at the index
	 */
	public final int get(int index)
	{
		return list.get(index);
	}

	/**
	 * @see tesuji.core.util.FlyWeight#recycle()
	 */
	public final void recycle()
	{
		int size = list.getSize();
		if (size<SMALL_NR_OF_ITEMS)
		{
			for (int i=size; --i>=0;)
				bitmap[list.pop()]=0;
		}
		else
			clear();
		
		if (owner!=null)
			owner.push(this);
	}

	/**
	 * Remove a point from the list.
	 * This is a relatively expensive operation.
	 * 
	 * @param xy
	 */
	public final void remove(int xy)
	{
		if (bitmap[xy]!=0)
		{
			bitmap[xy] = 0;
			list.remove(xy);
		}
	}

	public final int pop()
	{
		int xy = list.pop();
		bitmap[xy] = 0;
		return xy;
	}
	
	/**
	 * Setting the size of the number of items in the list.
	 * Should only be used if the current size is greater than
	 * the new size and can be used to 'pop' more than one item.
	 * 
	 * Use with care.
	 * 
	 * @param size
	 */
	public final void setSize(int size)
	{
		while (list.getSize()>size)
		{
			int xy = list.pop();
			bitmap[xy] = 0;
		}
	}
	
	public void copyFrom(UniqueList source)
	{
		list.copyFrom(source.list);
		GoArray.copy(source.bitmap, bitmap);
	}
}