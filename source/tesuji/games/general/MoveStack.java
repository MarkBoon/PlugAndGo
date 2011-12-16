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
package tesuji.games.general;

import tesuji.core.util.ArrayStack;
import tesuji.core.util.StringBufferFactory;

public class MoveStack<MoveType extends Move>
	extends ArrayStack<MoveType>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 932701888638258553L;

	public MoveStack()
	{
		super();
	}
	
	public MoveStack(int size)
	{
		super(size);
	}
	
	/**
	 * @return The list of moves contained by this stack in SGF format.
	 */
	public String toSGF()
	{
		StringBuffer output = StringBufferFactory.createStringBuffer();
		output.append("(");
		for (MoveType move : this)
		{
			output.append(";");
			output.append(move.toSGF());
		}
		output.append(")");
		
		String outputString = output.toString();
		StringBufferFactory.recycleStringBuffer(output);
		return outputString;
	}
	
	/**
	 * Empty the stack and recycle all the moves on it.
	 */
	public void recycleMoves()
	{
		while (!isEmpty())
			pop().recycle();
	}
	
	@Override
	public String toString()
	{
		return toSGF();
	}
}
