package tesuji.games.go.pattern.incremental;

import tesuji.core.util.SynchronizedArrayStack;

/**
 *	Factory class for PatternMatch objects.
 */
public class PatternMatchFactory
{
	/**
	 * This helper-class ensures there's a unique factory for each thread.
	 */
	static class LocalAllocationHelper
		extends ThreadLocal<PatternMatchFactory>
	{
		@Override
		public PatternMatchFactory initialValue()
		{
			return new PatternMatchFactory();
		}
	}

	private static LocalAllocationHelper _singleton;

	private static final int INITIAL_POOL_SIZE = 2000;

	private SynchronizedArrayStack<PatternMatch> pool = new SynchronizedArrayStack<PatternMatch>();
	
	public static PatternMatchFactory getSingleton()
	{
		if (_singleton==null)
			_singleton = new LocalAllocationHelper();
		
		return _singleton.get();
	}
	
	private PatternMatchFactory()
	{
		for (int i=0; i<INITIAL_POOL_SIZE; i++)
			pool.push(new PatternMatch(pool));
	}

	public static PatternMatch createMatch()
	{
		return getSingleton()._createMatch();
	}
	private PatternMatch _createMatch()
	{
		synchronized(pool)
		{
			if (pool.isEmpty())
			{
				return new PatternMatch(pool);
			}
			else
				return pool.pop();
		}
	}

	/**
	 * Factory method to create a Match object.
	 * 
	 * @param pattern
	 * @param xy
	 * @param orientation
	 * @param blackXY
	 * @param whiteXY
	 * @param inverted
	 * 
	 * @return created Object
	 */
	public static PatternMatch createMatch(IncrementalPatternTreeLeaf leaf, int xy)
	{
		PatternMatch match = createMatch();
		
		match.leaf = leaf;
		match.xy = xy;
		
		return match;
	}

}
