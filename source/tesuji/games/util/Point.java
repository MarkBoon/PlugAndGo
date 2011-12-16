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
package tesuji.games.util;

import tesuji.core.util.FlyWeight;
import tesuji.core.util.SynchronizedArrayStack;

/**
 * Actually the same as java.awt.Point except that it implements the FlyWeight
 * interface for fast object allocation. The factory methods are also here.
 */
public class Point
	implements FlyWeight
{
	private static SynchronizedArrayStack<Point> pointPool = new SynchronizedArrayStack<Point>();

	/**
	 	Create a Point object. Instead of allocating
	 	new objects each time, the objects created through this method
	 	can be 'recycled' and then reused. This is much more efficient
	 	than relying on the VM to allocate an object and then later
	 	garbage-collect it again.
	 	
	 	@return Point uninitialized
	*/
	public static Point create()
	{
		synchronized (pointPool)
		{
			if (pointPool.isEmpty())
				return new Point(pointPool);
		
			return pointPool.pop();
		}
	}
	
	/**
	 	Create a Point object initialized with x and y coordinates
	 	
	 	@param x
	 	@param y
	 	
	 	@return Point initialized with x and y
	*/
	public static Point create(int x, int y)
	{
		Point point = create();
		point.x = x;
		point.y = y;
		return point;
	}	
	
	/** X-coordinate. Public for easy access. */
	public int x;
	/** Y-coordinate. Public for easy access. */
	public int y;

	private SynchronizedArrayStack<Point> _owner;

	/**
	 * Point default constructor.
	 */
	public Point()
	{
	}
	
	/**
	 * Point constructor with initialisation.
	 * 
	 * @param x
	 * @param y
	 */
	public Point(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Point constructor with _owner.
	 * 
	 * @param stack owning this instance
	 */
	private Point(SynchronizedArrayStack<Point> stack)
	{
		_owner = stack;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (11-May-01 11:55:38 AM)
	 * @return int
	 */
	public int getX() 
	{
		return x;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (11-May-01 11:55:50 AM)
	 * @return int
	 */
	public int getY() 
	{
		return y;
	}
	
	/**
	 * @see tesuji.core.util.FlyWeight#recycle()
	 */
	public void recycle()
	{
		x = 0;
		y = 0;
		_owner.push(this);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (11-May-01 11:55:38 AM)
	 * @param newX int
	 */
	public void setX(int newX) 
	{
		x = newX;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (11-May-01 11:55:50 AM)
	 * @param newY int
	 */
	public void setY(int newY) 
	{
		y = newY;
	}
}
