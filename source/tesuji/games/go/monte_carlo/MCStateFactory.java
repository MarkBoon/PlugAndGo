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

package tesuji.games.go.monte_carlo;

import tesuji.core.util.Factory;
import tesuji.core.util.FactoryReport;
import tesuji.core.util.SynchronizedArrayStack;

/**
 * 
 */
public class MCStateFactory
	implements Factory
{
	private static long nrStates = 0;
	
	/**
	 * This helper-class ensures there's a unique factory for each thread.
	 */
	static class LocalAllocationHelper
		extends ThreadLocal<MCStateFactory>
	{
		@Override
		public MCStateFactory initialValue()
		{
			return new MCStateFactory();
		}
	}

	private static LocalAllocationHelper _singleton;

	private SynchronizedArrayStack<MCState> _pool = new SynchronizedArrayStack<MCState>();
	
	public static MCStateFactory getSingleton()
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
		return "MCStateFactory";
	}
	
	public String getFactoryReport()
	{
		return "Number of MCState objects:\t\t\t"+nrStates;
	}

	public static MCState createMCState()
	{
		return getSingleton()._createMCState();
	}
	
	private MCState _createMCState()
	{
    	synchronized (_pool)
        {
    		MCState newState;
	        if (_pool.isEmpty())
	        {
	        	newState = new MCState(_pool);
	        	nrStates++;
	        }
	        else
	        	newState = _pool.pop();
	        
	        return newState;
        }	
	}
}
