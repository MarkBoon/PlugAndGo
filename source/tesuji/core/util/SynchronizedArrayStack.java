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
package tesuji.core.util;

/**
 * Straightforward array implementation of a generic stack.<br>
 * <br>
 * This is the same as ArrayStack but with synchronized methods.
 * 
 * @see ArrayList
 */
public class SynchronizedArrayStack<Type>
	extends ArrayList<Type>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5196545381394676034L;

	public SynchronizedArrayStack()
	{
		super();
	}

	public SynchronizedArrayStack(int capacity)
	{
		super(capacity);
	}
	
	public synchronized final void push(Type o)
	{
		add(o);
	}
	
	public synchronized final Type pop()
	{
		return _array[--_index];
	}
	
	public synchronized final Type testAndPop()
	{
		if (isEmpty())
			return null;

		return _array[--_index];
	}
	
	/**
	 * Take a peek at what's at the top of the stack without removing the item.
	 * 
	 * @return Type
	 */
	public synchronized final Type peek()
	{
		return _array[_index-1];
	}
}
