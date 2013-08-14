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
package tesuji.games.model;

import tesuji.core.util.Factory;
import tesuji.core.util.SynchronizedArrayStack;
import tesuji.games.go.util.GoArray;

/**
 * Factory for pooling BoardChange objects.<br>
 */
public class BoardChangeFactory
	implements Factory
{
	private static int nrBoardChanges = 0;

	private static final int INITIAL_POOL_SIZE = 1000;

	/**
	 * This helper-class ensures there's a unique factory for each thread.
	 */
//	static class LocalAllocationHelper
//		extends ThreadLocal<BoardChangeFactory>
//	{
//		@Override
//		public BoardChangeFactory initialValue()
//		{
//			return new BoardChangeFactory();
//		}
//	}
//
//	private static LocalAllocationHelper _singleton;
	private static BoardChangeFactory _singleton;
	
	private SynchronizedArrayStack<BoardChange> pool = new SynchronizedArrayStack<BoardChange>();

	public static BoardChangeFactory getSingleton()
	{
		if (_singleton==null)
			_singleton = new BoardChangeFactory();
		return _singleton;
		/*
		if (_singleton==null)
		{
			_singleton = new LocalAllocationHelper();
			FactoryReport.addFactory(_singleton.get());
		}
		
		return _singleton.get();
		*/
	}
	
	private BoardChangeFactory()
	{
		for (int i=0; i<INITIAL_POOL_SIZE; i++)
			pool.push(new BoardChange(pool));
	}

	public String getFactoryName()
	{
		return "BoardChangeFactory";
	}
	
	public String getFactoryReport()
	{
		return "Number of BoardChange objects:\t\t\t"+nrBoardChanges;
	}
	
	private static BoardChange createBoardChange()
	{
		return getSingleton()._createBoardChange();
	}
	private BoardChange _createBoardChange()
	{
		synchronized (pool)
		{
			if (pool.isEmpty())
			{
				nrBoardChanges++;
				return new BoardChange(pool);
			}
			else
				return pool.pop();
		}
	}

	public static BoardChange createBoardChange(int xy, byte newValue, byte oldValue)
	{
		BoardChange boardChange = createBoardChange();
		boardChange.setXY(xy);
		boardChange.setNewValue(newValue);
		boardChange.setOldValue(oldValue);
		
		return boardChange;
	}

	public static BoardChange createBoardChange(int xy, byte newValue)
	{
		BoardChange boardChange = createBoardChange();
		boardChange.setXY(xy);
		boardChange.setNewValue(newValue);
		boardChange.setOldValue(Byte.MIN_VALUE);
		
		return boardChange;
	}

	public static BoardChange createBoardChange(int x, int y, byte newValue, byte oldValue)
	{
		BoardChange boardChange = createBoardChange();
		boardChange.setXY(GoArray.toXY(x,y));
		boardChange.setNewValue(newValue);
		boardChange.setOldValue(oldValue);
		
		return boardChange;
	}

	public static BoardChange createBoardChange(int x, int y, byte newValue)
	{
		BoardChange boardChange = createBoardChange();
		boardChange.setXY(GoArray.toXY(x,y));
		boardChange.setNewValue(newValue);
		boardChange.setOldValue(Byte.MIN_VALUE);
		
		return boardChange;
	}

	public static BoardChange createClone(BoardChange source)
	{
		BoardChange boardChange = createBoardChange();
		boardChange.setXY(source.getXY());
		boardChange.setNewValue(source.getNewValue());
		boardChange.setOldValue(source.getOldValue());
		
		return boardChange;
	}
}
