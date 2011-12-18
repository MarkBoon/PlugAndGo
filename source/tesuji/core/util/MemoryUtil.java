package tesuji.core.util;

public class MemoryUtil
{
	public static long getFreeMemory()
	{
		long last = Runtime.getRuntime().freeMemory();
		System.gc();
		long current = Runtime.getRuntime().freeMemory();
		while (current/(last-current)>20)
		{
			last = current;
			System.gc();
			current = Runtime.getRuntime().freeMemory();
		}
		return current;
	}
}
