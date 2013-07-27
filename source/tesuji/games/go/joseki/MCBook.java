package tesuji.games.go.joseki;

import java.util.HashMap;
import java.util.List;

public class MCBook
{
	private HashMap<Long,MCJosekiEntry> _map;
	
	
	public MCBook()
	{
		_map = new HashMap<Long, MCJosekiEntry>();
	}
	
	public void read()
	{
		List<MCJosekiEntry> list = JosekiManager.getSingleton().getJosekiEntries();
		for (MCJosekiEntry entry : list)
			_map.put(entry.getChecksum(), entry);
	}
	
	public MCJosekiEntry get(long checksum)
	{
		return _map.get(checksum);
	}
	
	public MCJosekiEntry load(long checksum)
	{
		MCJosekiEntry entry =  _map.get(checksum);
		if (entry!=null)
		{
			entry = JosekiManager.getSingleton().getJosekiEntry(checksum);
			if (entry==null)
			{
				System.err.println("No entry found with checksum "+checksum);
			}
			else if (entry.getChecksum()!=checksum)
			{
				System.err.println("Inconsistent entry found with checksum "+checksum+" vs. entry.checksum "+entry.getChecksum());
			}
		}
		return entry;
	}
	
	public void put(MCJosekiEntry entry)
	{
		_map.put(entry.getChecksum(),entry);
	}
}
