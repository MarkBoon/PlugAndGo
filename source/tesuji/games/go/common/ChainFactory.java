/**
 * 
 */
package tesuji.games.go.common;

import tesuji.core.util.Factory;
import tesuji.core.util.FactoryReport;
import tesuji.core.util.SynchronizedArrayStack;

public class ChainFactory
	implements Factory
{
	private static int nrChains = 0;
	
	// Static data
	private static SynchronizedArrayStack<Chain> chainPool = new SynchronizedArrayStack<Chain>();
	
	private static ChainFactory _singleton;
	
	public static ChainFactory getSingleton()
	{
		if (_singleton==null)
		{
			_singleton = new ChainFactory();
			FactoryReport.addFactory(_singleton);
		}
		
		return _singleton;
	}
	
	private ChainFactory()
	{
	}
	
	public String getFactoryName()
	{
		return "ChainFactory";
	}
	
	public String getFactoryReport()
	{
		return "Number of Chain objects:\t\t\t"+nrChains;
	}
	
	/**
	 * Create a single-stone chain.
	 */
	public Chain createChain()
	{
		synchronized (chainPool)
		{
			if (chainPool.isEmpty())
			{
				nrChains++;
				return new Chain(chainPool);
			}
			else
				return chainPool.pop();
		}
	}
}
