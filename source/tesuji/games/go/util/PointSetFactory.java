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

/**
 * 
 */
public class PointSetFactory
	implements Factory
{
	private static int nrPointSets = 0;
	
	/**
	 * This helper-class ensures there's a unique factory for each thread.
	 */
	static class LocalAllocationHelper
		extends ThreadLocal<PointSetFactory>
	{
		@Override
		public PointSetFactory initialValue()
		{
			return new PointSetFactory();
		}
	}

	private static LocalAllocationHelper _singleton;

	public static PointSetFactory getSingleton()
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
		return "PointSetFactory";
	}
	
	public String getFactoryReport()
	{
		return "Number of PointSet objects:\t\t\t"+nrPointSets;
	}
	
	private SynchronizedArrayStack<PointSet> _pool =	new SynchronizedArrayStack<PointSet>();


	public static PointSet createPointSet()
	{
	
		return getSingleton()._createPointSet();
	}
	private PointSet _createPointSet()
	{
    	synchronized (_pool)
        {
    		PointSet newPointSet;
	        if (_pool.isEmpty())
	        {
	        	newPointSet = new PointSet(_pool);
	        	nrPointSets++;
	        }
	        else
	        	newPointSet = _pool.pop();
	        
	        return newPointSet;
        }	
	}
}
