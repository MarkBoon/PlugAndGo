package tesuji.games.general;

public class GlobalParameters
{
	private static boolean _testVersion;
	private static boolean _collectingStatistics;
	
	public static boolean isTestVersion()
	{
		return _testVersion;
	}
	
	public static void setTestVersion(boolean flag)
	{
		_testVersion = flag;
	}
	
	public static boolean isCollectingStatistics()
	{
		return _collectingStatistics;
	}
	
	public static void setCollectingStatistics(boolean flag)
	{
		_collectingStatistics = flag;
	}
}
