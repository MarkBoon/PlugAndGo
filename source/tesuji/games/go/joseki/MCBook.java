package tesuji.games.go.joseki;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class MCBook
	implements Runnable
{
	private String _fileName;
	private HashMap<Long,MCJosekiEntry> _map;
	
	
	public MCBook(String fileName)
	{
		_fileName = fileName;
		_map = new HashMap<Long, MCJosekiEntry>();
	}
	
	public void read()
	{
		try
        {
	        BufferedReader reader = new BufferedReader(new FileReader(_fileName));
	        
	        String line;
	        while ((line=reader.readLine())!=null)
	        {
	        	MCJosekiEntry entry = MCJosekiEntry.parse(line);
	        	_map.put(entry.getChecksum(), entry);
	        }
	        reader.close();
        }
        catch (Exception exception)
        {
	        exception.printStackTrace();
        }
	}
	
	public void save()
	{
		Thread t = new Thread(this);
		t.start();
	}
	
	public void run()
	{
		try
        {
	        BufferedWriter writer = new BufferedWriter(new FileWriter(_fileName));
	        
	        for (MCJosekiEntry entry : _map.values())
	        {
	        	writer.write(entry.toString());
	        	writer.write("\n");
	        }
	        writer.flush();
	        writer.close();
        }
        catch (Exception exception)
        {
	        exception.printStackTrace();
        }
	}
	
	public MCJosekiEntry get(long checksum)
	{
		return _map.get(checksum);
	}
	
	public void put(MCJosekiEntry entry)
	{
		_map.put(entry.getChecksum(),entry);
	}
}
