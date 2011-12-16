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

import tesuji.core.util.Factory;
import tesuji.core.util.FactoryReport;
import tesuji.core.util.SynchronizedArrayStack;

public class BoardMarkFactory
	implements Factory
{
	private static int nrBoardMarks;
	
	/**
	 * This helper-class ensures there's a unique factory for each thread.
	 */
	static class LocalAllocationHelper
		extends ThreadLocal<BoardMarkFactory>
	{
		@Override
		public BoardMarkFactory initialValue()
		{
			return new BoardMarkFactory();
		}
	}

	private static LocalAllocationHelper _singleton;

	private SynchronizedArrayStack<BoardMark> boardMarkPool = new SynchronizedArrayStack<BoardMark>();

	public static BoardMarkFactory getSingleton()
	{
		if (_singleton==null)
		{
			_singleton = new LocalAllocationHelper();
			FactoryReport.addFactory(_singleton.get());
		}
		
		return _singleton.get();
	}
	
	public String getFactoryName()
	{
		return "BoardMarkFactory";
	}
	
	public String getFactoryReport()
	{
		return "Number of BoardMark objects:\t\t\t"+nrBoardMarks;
	}

	/**
	 * Factory method for an uninitialised move.
	 * 
	 * @return BoardMark
	 */
	public static BoardMark createBoardMark()
	{
		return getSingleton()._createBoardMark();
	}
	
	private BoardMark _createBoardMark()
	{
		synchronized (boardMarkPool)
		{
			BoardMark newBoardMark;
			if (boardMarkPool.isEmpty())
			{
				newBoardMark = new BoardMark(boardMarkPool);
				nrBoardMarks++;
			}
			else
				newBoardMark = boardMarkPool.pop();
			
			return newBoardMark;
		}
	}
	
	/**
	 * Factory method for an initialised move.
	 * 
	 * @param x
	 * @param y
	 * @param color
	 * 
	 * @return BoardMark
	 */
	public static BoardMark createBoardMark(int x, int y, byte color)
	{
		BoardMark newBoardMark = createBoardMark();
		
		newBoardMark.setX(x);
		newBoardMark.setY(y);
		newBoardMark.setColor(color);
		
		return newBoardMark;
	}
	
	/**
	 * Factory method for an initialised move.
	 * 
	 * @param x
	 * @param y
	 * @param color
	 * @param type
	 * 
	 * @return BoardMark
	 */
	public static BoardMark createBoardMark(int x, int y, byte color, byte type)
	{
		BoardMark newBoardMark = createBoardMark();
		
		newBoardMark.setX(x);
		newBoardMark.setY(y);
		newBoardMark.setColor(color);
		newBoardMark.setMarkType(type);
		
		return newBoardMark;
	}
}
