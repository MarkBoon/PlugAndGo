package tesuji.games.go.util;

import java.util.HashMap;
import java.util.Set;

public class Statistics
{
	private static HashMap<String,Integer> statistics = new HashMap<String, Integer>();

	public static int nrPatternsPlayed = 0;
	public static int nrPatternsMatched = 0;
	
	public static void reset()
	{
		nrPatternsPlayed = 0;
		nrPatternsMatched = 0;
	}
	
	public static void increment(String key)
	{
		Integer v = statistics.get(key);
		if (v==null)
			statistics.put(key, 1);
		else
			statistics.put(key, new Integer(v+1));
	}
	
	public static int get(String key)
	{
		Integer v = statistics.get(key);
		if (v==null)
			return 0;
		return v;
	}
	
	public static Set<String> getKeys()
	{
		return statistics.keySet();
	}
}
