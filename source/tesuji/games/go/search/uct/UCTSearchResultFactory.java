package tesuji.games.go.search.uct;

import tesuji.core.util.Factory;
import tesuji.core.util.FactoryReport;
import tesuji.core.util.MutableDouble;
import tesuji.core.util.SynchronizedArrayStack;
import tesuji.games.general.Move;

public class UCTSearchResultFactory
	implements Factory
{
	private static int nrResults = 0;
	private static int nrRaveResults = 0;
	
	/**
	 * This helper-class ensures there's a unique factory for each thread.
	 */
	static class LocalAllocationHelper
		extends ThreadLocal<UCTSearchResultFactory>
	{
		@Override
		public UCTSearchResultFactory initialValue()
		{
			return new UCTSearchResultFactory();
		}
	}

	private static LocalAllocationHelper _singleton;

	private SynchronizedArrayStack<UCTSearchResult> uctResultPool =
		new SynchronizedArrayStack<UCTSearchResult>();
	
	private SynchronizedArrayStack<UctRaveSearchResult> uctRaveResultPool =
		new SynchronizedArrayStack<UctRaveSearchResult>();
	
	public static UCTSearchResultFactory getSingleton()
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
		return "UCTSearchResultFactory";
	}
	
	public String getFactoryReport()
	{
		return "Number of UCTSearchResult objects:\t\t"+nrResults+"\n"+
				"Number of UctRaveSearchResult objects:\t\t"+nrRaveResults;
	}

	public static UCTSearchResult createUCTSearchResult()
	{
		return getSingleton()._createUCTSearchResult();
	}
	
	private UCTSearchResult _createUCTSearchResult()
	{
    	synchronized (uctResultPool)
        {
    		UCTSearchResult newResult;
	        if (uctResultPool.isEmpty())
	        {
	        	newResult = new UCTSearchResult(uctResultPool);
	        	nrResults++;
	        }
	        else
	        	newResult = uctResultPool.pop();
	        
	        return newResult;
        }	
	}

	public static UCTSearchResult createUCTSearchResult(MutableDouble logNrParentPlayouts)
	{
		UCTSearchResult newResult = createUCTSearchResult();

		newResult.setLogNrParentPlayouts(logNrParentPlayouts);
	        
        return newResult;
	}
	
	public static UCTSearchResult createUCTSearchResult(MutableDouble logNrParentPlayouts, double explorationFactor)
	{
		UCTSearchResult newResult = createUCTSearchResult();

		newResult.setLogNrParentPlayouts(logNrParentPlayouts);
	    newResult.setExplorationFactor(explorationFactor);
	    
        return newResult;
	}

	public static UctRaveSearchResult createUctRaveSearchResult()
	{
		return getSingleton()._createUctRaveSearchResult();
	}
	
	private UctRaveSearchResult _createUctRaveSearchResult()
	{
    	synchronized (uctResultPool)
        {
    		UctRaveSearchResult newResult;
	        if (uctResultPool.isEmpty())
	        {
	        	newResult = new UctRaveSearchResult(uctRaveResultPool);
	        	nrResults++;
	        }
	        else
	        	newResult = uctRaveResultPool.pop();
	        
	        return newResult;
        }	
	}

	public static UctRaveSearchResult createUctRaveSearchResult(MutableDouble logNrParentPlayouts)
	{
		UctRaveSearchResult newResult = createUctRaveSearchResult();

		newResult.setLogNrParentPlayouts(logNrParentPlayouts);
	        
        return newResult;
	}
	
	public static UctRaveSearchResult createUctRaveSearchResult(MutableDouble logNrParentPlayouts, double explorationFactor)
	{
		UctRaveSearchResult newResult = createUctRaveSearchResult();

		newResult.setLogNrParentPlayouts(logNrParentPlayouts);
	    newResult.setExplorationFactor(explorationFactor);
	    
        return newResult;
	}
}
