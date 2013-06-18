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

import tesuji.core.util.SynchronizedArrayStack;
import tesuji.core.util.FlyWeight;

/**
 * This class acts like a (usually small) list of doubles. The only way to add
 * items is through the 'push' method. But otherwise it acts just like an array
 * of integers.
 */
public class DoubleStack
	implements FlyWeight
{
	 // This is the stack that owns the object when it gets
	 // created and to which it gets recycled after use.
	private SynchronizedArrayStack<DoubleStack> owner;
	
	private int index = 0;
	private double[] stack;
	
	/**
	 	Default constructor. A GoArray is used so that we're
	 	sure the capacity is sufficient to store enough items
	 	to fill a board.
	*/
	DoubleStack()
	{
		stack = GoArray.createDoubles();
	}
	
	/**
	 	DoubleStack constructor. The ownerStack is used so that
	 	the method 'recycle' can be used for deallocation.

	 	@param ownerStack
	*/
	public DoubleStack(SynchronizedArrayStack<DoubleStack> ownerStack)
	{
		owner = ownerStack;
		stack = GoArray.createDoubles();
	}

	/**
	 	IntStack constructor with a given capacity.
	 	This one can be used for smaller lists.
	 	The list doesn't grow automatically and no
	 	explicit bounds-checking is done.
	 	
	 	@param size is the maximum capacity of the stack
	*/
	DoubleStack(int size)
	{
		stack = new double[size];
	}
	
	/**
	 	IntStack constructor with a given capacity.
	 	This one can be used for smaller lists.
	 	The list doesn't grow automatically and no
	 	explicit bounds-checking is done.
	 	
	 	@param size is the maximum capacity of the stack
	 	
	 	@param ownerStack used to recycle this IntStack instance
	*/
	public DoubleStack(int size, SynchronizedArrayStack<DoubleStack> ownerStack)
	{
		stack = new double[size];
		owner = ownerStack;
	}

	/**
	 * Add a value to the stack
	 * 
	 * @param value
	 */
	public final void push(int value)
	{
		stack[index++] = value;
	}
	
	/**
	 * @return the topmost item from the stack and remove it.
	 */
	public final double pop()
	{
		return stack[--index];
	}
	
	/**
	 * @return the topmost item from the stack.
	 */
	public final double peek()
	{
		return stack[index-1];
	}
	
	/**
	 * @param n
	 * 
	 * @return the n-th item from the top.
	 */
	public final double peek(int n)
	{
		return stack[index-n-1];
	}
	
	/**
	 * Remove an item from the list. This may cause the order of the items in
	 * the list to change, so don't use this method if the order of the items is
	 * important.<br>
	 * <br>
	 * It's also a relatively expensive operation, so only practical for small
	 * lists.<br>
	 * <br>	
	 * @param value to remove from the list.
	 */
	public final void remove(double value)
	{
		if (index==0)
			return;

		index--;
		double last = stack[index];
	
		if (value==last)
			return;

		// If it's not the last item in the list, loop until
		// it's found and store the last item in the place
		// of the item to remove.
		for (int i=index-1; i>=0; i--)
		{
			if (stack[i]==value)
			{
				stack[i] = last;
				return;
			}
		}

		// The item was not found in the list!
		index++;
	}

	/**
	 * Clear the contents of the IntStack object. It will now have size 0 and
	 * contain 0 items.
	 */
	public final boolean isEmpty()
	{
		return (index==0);
	}
	
	/**
	 * @return the number of items on the stack
	 */
	public final int getSize()
	{
		return index;
	}

	/**
	 * Resets the stack to empty.
	 */
	public final void clear()
	{
		index = 0;
	}
	
	/**
	 * Get the item at a certain index
	 * Not really astack operation, but it's handy at times.
	 * 
	 * @param itemIndex
	 * 
	 * @return int
	 */
	public final double get(int itemIndex)
	{
		return stack[itemIndex];
	}
	
	/**
	 * @return the stacks contents as an int[]
	 */
	public final double[] getList()
	{
		return stack;
	}
	
	/**
	 * @see tesuji.core.util.FlyWeight#recycle()
	 */
	public final void recycle()
	{
		clear();

		if (owner!=null)
			owner.push(this);
	}
	
	/**
	 * Setting the size of the number of items on the stack. Should only be used
	 * if the current size is greater than the new size and can be used to 'pop'
	 * more than one item.
	 * 
	 * Use with care!
	 * 
	 * @param newSize
	 */
	public final void setSize(int newSize)
	{
		index = newSize;
	}

	/**
	 * @param source
	 */
	public final void copyFrom(DoubleStack source)
	{
		index = source.index;
		System.arraycopy(source.stack, 0, stack, 0, index);
	}	
}