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

import tesuji.core.util.Factory;
import tesuji.core.util.FactoryReport;
import tesuji.core.util.SynchronizedArrayStack;
import tesuji.games.go.common.GoConstant;
import tesuji.games.model.BoardArray;

/**
 * This is a factory class for constructing array data-structures of different 
 * kinds.
 */
public class ArrayFactory
	implements Factory
{
	/**
	 * This helper-class ensures there's a unique factory for each thread.
	 */
	static class LocalAllocationHelper
		extends ThreadLocal<ArrayFactory>
	{
		@Override
		public ArrayFactory initialValue()
		{
			return new ArrayFactory();
		}
	}

	private static LocalAllocationHelper _singleton;
	
	private static final int SMALL_LIST_SIZE = 10;
	private static final int LARGE_LIST_SIZE = 1100;
	private static final int INITIAL_POOL_SIZE = 1000;

	private static int nrIntStacks = 		INITIAL_POOL_SIZE;
	private static int nrSmallIntStacks =	INITIAL_POOL_SIZE;
	private static int nrlargeIntStacks =	INITIAL_POOL_SIZE;
	private static int nrUniqueLists =		INITIAL_POOL_SIZE;
	private static int nrNeighbourLists =	INITIAL_POOL_SIZE;
	private static int nrChainLists =		INITIAL_POOL_SIZE;
	
	private SynchronizedArrayStack<IntStack> intStackPool =			new SynchronizedArrayStack<IntStack>();
	private SynchronizedArrayStack<IntStack> smallIntStackPool =	new SynchronizedArrayStack<IntStack>();
	private SynchronizedArrayStack<IntStack> largeIntStackPool =	new SynchronizedArrayStack<IntStack>();
	private SynchronizedArrayStack<UniqueList> uniqueListPool =		new SynchronizedArrayStack<UniqueList>();

	public static ArrayFactory getSingleton()
	{
		if (_singleton==null)
		{
			_singleton = new LocalAllocationHelper();
			FactoryReport.addFactory(_singleton.get());
		}
		
		return _singleton.get();
	}
	
//	private ArrayFactory()
//	{
//		// Time will tell what are good initial pool-sizes.
//		for (int i=0; i<INITIAL_POOL_SIZE; i++)
//			intStackPool.push(new IntStack(intStackPool));
//		for (int i=0; i<INITIAL_POOL_SIZE*3; i++)
//			smallIntStackPool.push(new IntStack(SMALL_LIST_SIZE,smallIntStackPool));
//		for (int i=0; i<INITIAL_POOL_SIZE; i++)
//			uniqueListPool.push(new UniqueList(uniqueListPool));
//
//		for (int i=0; i<INITIAL_POOL_SIZE*3; i++)
//			neighbourListPool.push(new ChainStack(neighbourListPool));
//		for (int i=0; i<INITIAL_POOL_SIZE/10; i++)
//			chainListPool.push(new ChainStack(GoConstant.MAXCHAINS,chainListPool));		
//	}
	
	public String getFactoryName()
	{
		return "ArrayFactory";
	}
	
	public String getFactoryReport()
	{
		return "Number of IntStack objects:\t\t\t"+nrIntStacks+"\n"+
		"Number of small IntStack objects\t\t"+nrSmallIntStacks+"\n"+
		"Number of large IntStack objects\t\t"+nrlargeIntStacks+"\n"+
		"Number of UniqueList objects\t\t\t"+nrUniqueLists+"\n"+
		"Number of neighbour list objects\t\t"+nrNeighbourLists+"\n"+
		"Number of ChainStack objects\t\t\t"+nrChainLists+"\n";
	}

	/**
	 * This method creates a BoardArray of bytes with the borders marked as EDGE.
	 * 
	 * @param size
	 * @return BoardArray
	 */
	public static BoardArray getBoardArray(int size)
	{
		return getSingleton()._getBoardArray(size);
	}
	private BoardArray _getBoardArray(int size)
	{
		return new ByteArrayImpl(size,GoArray.createBoardArray(size));
	}

	/**
	 * This method creates a BoardArray of bytes with everything zero.
	 * 
	 * @param size
	 * @return BoardArray
	 */
	public static ByteArray getByteArray(int size)
	{
		return getSingleton()._getByteArray(size);
	}
	private ByteArray _getByteArray(int size)
	{
		return new ByteArrayImpl(size, GoArray.createBytes());
	}

	public static ObjectArray<?> getObjectArray(int size)
	{
		return getSingleton()._getObjectArray(size);
	}

	private ObjectArray<?> _getObjectArray(int size)
	{
		return new ObjectArrayImpl<Object>(size);
	}

	/**
	 * Allocate an IntStack of default size. Instead of allocating new objects
	 * each time, the objects created through this method can be 'recycled' and
	 * then reused. This is much more efficient than relying on the VM to
	 * allocate an object and then later garbage-collect it again.
	 * 
	 * @return second-hand IntStack
	 */
	public static IntStack createIntStack()
	{
		return getSingleton()._createIntStack();
	}
	private IntStack _createIntStack()
	{
		synchronized(intStackPool)
		{
			if (intStackPool.isEmpty())
			{
				nrIntStacks++;
				return new IntStack(intStackPool);
			}
	
			IntStack intStack = intStackPool.pop();
			return intStack;
		}
	}

	/**
	 * Allocate an IntStack for a small number of items as defined by
	 * SMALL_LIST_SIZE. Instead of allocating new objects each time, the objects
	 * created through this method can be 'recycled' and then reused. This is
	 * much more efficient than relying on the VM to allocate an object and then
	 * later garbage-collect it again.
	 * 
	 * @return second-hand IntStack
	 */
	public static IntStack createSmallIntStack()
	{
		return getSingleton()._createSmallIntStack();
	}
	private IntStack _createSmallIntStack()
	{
		synchronized(smallIntStackPool)
		{
			if (smallIntStackPool.isEmpty())
			{
				nrSmallIntStacks++;
				return new IntStack(SMALL_LIST_SIZE, smallIntStackPool);
			}
	
			IntStack intStack = smallIntStackPool.pop();
			return intStack;
		}
	}

	/**
	 * Allocate an IntStack for a large number of items as defined by
	 * LARGE_LIST_SIZE. Instead of allocating new objects each time, the objects
	 * created through this method can be 'recycled' and then reused. This is
	 * much more efficient than relying on the VM to allocate an object and then
	 * later garbage-collect it again.
	 * 
	 * @return second-hand IntStack
	 */
	public static IntStack createLargeIntStack()
	{
		return getSingleton()._createLargeIntStack();
	}
	private IntStack _createLargeIntStack()
	{
		synchronized(largeIntStackPool)
		{
			if (largeIntStackPool.isEmpty())
			{
				nrlargeIntStacks++;
				return new IntStack(LARGE_LIST_SIZE, largeIntStackPool);
			}
	
			IntStack intStack = largeIntStackPool.pop();
			return intStack;
		}
	}

	/**
	 * Allocate an UniqueList. Instead of allocating new objects each time, the
	 * objects created through this method can be 'recycled' and then reused.
	 * This is much more efficient than relying on the VM to allocate an object
	 * and then later garbage-collect it again.
	 * 
	 * This one is a bit more expensive than usual anyway, because clearing
	 * implies an array copy.
	 * 
	 * @return second-hand UniqueList
	 */
	public static UniqueList createUniqueList()
	{
		return getSingleton()._createUniqueList();
	}
	private UniqueList _createUniqueList()
	{
		synchronized (uniqueListPool)
        {
			if (uniqueListPool.isEmpty())
			{
				nrUniqueLists++;
				return new UniqueList(uniqueListPool);
			}
	
			return uniqueListPool.pop();
        }
	}
}
