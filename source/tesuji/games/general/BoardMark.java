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

import tesuji.core.util.FlyWeight;
import tesuji.core.util.SynchronizedArrayStack;

import static tesuji.games.general.ColorConstant.*;

/**
 * A class defining a board-mark. This is either used to set up a position or to mark
 * certain points on the board to display in a diagram.
 */
public class BoardMark
	implements FlyWeight
{
	private int _x;
	private int _y;
	private byte _color;
	private byte _type;
	private SynchronizedArrayStack<BoardMark> _owner;
	
	BoardMark(SynchronizedArrayStack<BoardMark> owner)
	{
		_owner = owner;
	}

	/**
	 * @return SGF representation of the BoardMark
	 */
	public String toSGF()
	{
		StringBuffer output = new StringBuffer();
		switch(_color)
		{
			case BLACK:
			{
				output.append("AB[");
				output.append('a'-1+getX());
				output.append('a'-1+getY());
				output.append(']');
				break;
			}
			case WHITE:
			{
				output.append("AW[");
				output.append('a'-1+getX());
				output.append('a'-1+getY());
				output.append(']');
				break;
			}
			case EMPTY:
			{
				output.append("AE[");
				output.append('a'-1+getX());
				output.append('a'-1+getY());
				output.append(']');
				break;
			}
			default:
				// Nothing
		}
		return output.toString();
	}
	
	public int getX()
	{
		return _x;
	}
	
	public void setX(int x)
	{
		_x = x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public void setY(int y)
	{
		_y = y;
	}
	
	public byte getColor()
	{
		return _color;
	}
	
	public void setColor(byte color)
	{
		_color = color;
	}
	
	public byte getMarkType()
	{
		return _type;
	}
	
	public void setMarkType(byte type)
	{
		_type = type;
	}

	public boolean hasSameLocation(BoardMark mark)
	{
		return (mark._x==_x && mark._y==_y);
	}
	
	public void recycle()
	{
		_owner.push(this);
	}
}