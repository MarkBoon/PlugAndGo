/**
 *	Product: Tesuji Software Go Library.<br><br>
 *
 *	<font color="#CC6600"><font size=-1>
 *	Copyright (c) 2001-2004 Tesuji Software B.V.<br>
 *	All rights reserved.<br><br>
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a
 *	copy of this software and associated documentation files (the
 *	"Software"), to deal in the Software without restriction, including
 *	without limitation the rights to use, copy, modify, merge, publish,
 *	distribute, and/or sell copies of the Software, and to permit persons
 *	to whom the Software is furnished to do so, provided that the above
 *	copyright notice(s) and this permission notice appear in all copies of
 *	the Software and that both the above copyright notice(s) and this
 *	permission notice appear in supporting documentation.<br><br>
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 *	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.<br><br>
 *
 *	Except as contained in this notice, the name of a Tesuji Software
 *	shall not be used in advertising or otherwise to promote the sale, use
 *	or other dealings in this Software without prior written authorization
 *	of Tesuji Software.<br><br>
 *	<font color="#00000"><font size=+1>
 */
package tesuji.games.go.util;

import tesuji.core.util.FlyWeight;
import tesuji.core.util.SynchronizedArrayStack;
import tesuji.games.go.common.Chain;

/**
 * A short list of chains. The size is always 4 for the 4 neighbours.
 */
public class ChainStack
	implements FlyWeight
{
	 // This is the stack that owns the object when it gets
	 // created and to which it gets recycled after use.
	private SynchronizedArrayStack<ChainStack> owner;
	
	private int index = 0;
	private Chain[] stack;
	
	/**
	 	Default constructor.
	*/
	public ChainStack()
	{
		this(4);
	}
	
	/**
	 * Default constructor.
	 * 
	 * @param size
	 */
	public ChainStack(int size)
	{
		stack = new Chain[size];
	}

	/**
	 	ObjectStack constructor. The ownerStack is used so that
	 	the method 'recycle' can be used for deallocation.
	 	
	 	@param ownerStack
	*/
	public ChainStack(SynchronizedArrayStack<ChainStack> ownerStack)
	{
		this(4);
		owner = ownerStack;
	}

	/**
	 	ObjectStack constructor. The ownerStack is used so that
	 	the method 'recycle' can be used for deallocation.
	
	 	@param size
	 	@param ownerStack
	*/
	public ChainStack(int size, SynchronizedArrayStack<ChainStack> ownerStack)
	{
		this(size);
		owner = ownerStack;
	}

	/**
	 * Add a chain to the stack
	 * 
	 * @param chain
	 */
	public final void push(Chain chain)
	{
		stack[index++] = chain;
	}
	
	/**
	 * @return the top-most item from the stack and remove it.
	 */
	public final Chain pop()
	{
		return stack[--index];
	}
	
	/**
	 * @return the top-most item from the stack.
	 */
	public final Chain peek()
	{
		return stack[index-1];
	}
	
	/**
	 * @param n
	 * 
	 * @return the n-th item from the top.
	 */
	public final Chain peek(int n)
	{
		return stack[index-n-1];
	}
	
	/**
	 	Remove an item from the list. This may cause the
	 	order of the items in the list to change, so don't
	 	use this method if the order of the items is important.
	
		It's also a relatively expensive operation, so only
		practical for small lists.
	
	 	@param value to remove from the list.
	*/
	public final void remove(Chain value)
	{
		index--;
		Chain last = stack[index];
		
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
	 * @return whether the stack is empty
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
	 	Clear the contents of the IntStack object.
	 	It will now have size 0 and contain 0 items.
	*/
	public final void clear()
	{
		index = 0;
	}
	
	/**
	 * Get the item at a certain index
	 * Not really a stack operation, but it's handy at times.
	 * 
	 * @param itemIndex
	 * 
	 * @return int
	 */
	public final Chain get(int itemIndex)
	{
		return stack[itemIndex];
	}
	
	/**
	 * @return the stacks contents as a Chain[]
	 */
	public final Chain[] getList()
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
}