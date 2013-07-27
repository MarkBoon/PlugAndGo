package tesuji.games.go.joseki;

import java.text.ParseException;

import tesuji.games.go.util.GoArray;

public class MCJosekiEntry
{
	private long checksum;
	public int[] xy;
	public long[] wins;
	public long[] played;
	private long timestamp;
	private int occurrences;
	private String stats;
	
	public long getChecksum()
	{
		return checksum;
	}
	public void setChecksum(long checksum)
	{
		this.checksum = checksum;
	}
	
	public long getTimestamp()
	{
		return timestamp;
	}
	
	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public int getOccurrences()
	{
		return occurrences;
	}
	
	public void setOccurrences(int occurrences)
	{
		this.occurrences = occurrences;
	}

	public String toString()
	{
		StringBuilder output = new StringBuilder();
		output.append(Long.toString(checksum));
		output.append(",");
		output.append(Long.toString(timestamp));
		output.append(",");
		output.append(Integer.toString(occurrences));
		output.append(",");
		for (int i=0; i<xy.length; i++)
		{
			output.append(GoArray.getX(xy[i]));
			output.append(",");
			output.append(GoArray.getY(xy[i]));
			output.append(",");
			output.append(wins[i]);
			output.append(",");
			output.append(played[i]);
			if (i<xy.length-1)
				output.append(",");
		}
		return output.toString();
	}
	
	public static MCJosekiEntry parse(String input)
		throws ParseException
	{
		MCJosekiEntry entry = new MCJosekiEntry();
		String[] parts = input.split(",");
		entry.setChecksum(Long.parseLong(parts[0]));
		entry.setTimestamp(Long.parseLong(parts[1]));
		entry.setOccurrences(Integer.parseInt(parts[2]));
		if ((parts.length-3)%4!=0)
			throw new ParseException("Error while parsing input",0);
		int nrPoints = (parts.length-3)/4;
		int[] xyArray = new int[nrPoints];
		long[] wins = new long[nrPoints];
		long[] played = new long[nrPoints];
		int index = 0;
		for (int i=3; i<parts.length; i+=4)
		{
			int x = Integer.parseInt(parts[i]);
			int y = Integer.parseInt(parts[i+1]);
			long nrWins = Long.parseLong(parts[i+2]);
			long nrPlayed = Long.parseLong(parts[i+3]);
			xyArray[index] = GoArray.toXY(x, y);
			wins[index] = nrWins;
			played[index] = nrPlayed;
			index++;
		}
		entry.xy = xyArray;
		entry.wins = wins;
		entry.played = played;
		return entry;
	}
	
	public String getStats()
	{
		StringBuilder output = new StringBuilder();
		for (int i=0; i<xy.length; i++)
		{
			output.append(GoArray.getX(xy[i]));
			output.append(",");
			output.append(GoArray.getY(xy[i]));
			output.append(",");
			output.append(wins[i]);
			output.append(",");
			output.append(played[i]);
			if (i<xy.length-1)
				output.append(",");
		}
		stats = output.toString();

		return stats;
	}
	
	public void setStats(String values)
	{
		this.stats = values;
		
		String[] parts = values.split(",");
//		if (parts.length%4!=0)
//			throw new ParseException("Error while parsing input",0);
		int nrPoints = parts.length/4;
		int[] xy = new int[nrPoints];
		long[] wins = new long[nrPoints];
		long[] played = new long[nrPoints];
		int index = 0;
		for (int i=0; i<parts.length; i+=4)
		{
			int x = Integer.parseInt(parts[i]);
			int y = Integer.parseInt(parts[i+1]);
			long nrWins = Long.parseLong(parts[i+2]);
			long nrPlayed = Long.parseLong(parts[i+3]);
			xy[index] = GoArray.toXY(x, y);
			wins[index] = nrWins;
			played[index] = nrPlayed;
			index++;
		}
		this.xy = xy;
		this.wins = wins;
		this.played = played;
	}
}
