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
package tesuji.games.gtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class GTPUtil
{
	private static String getGTPResponse(Reader reader)
		throws Exception
	{
		StringBuffer result = new StringBuffer();
		while (true)
		{
			int c = reader.read();
			if (c!='\n' && Character.isISOControl(c)) // This is necessary to strip it of any CR that may have been inserted after LF by Windows.
				continue;
			result.append((char)c);
			if (result.lastIndexOf("\n\n")!=-1)
				break;
		}
	
		return result.toString();
	}

	public static String readGTPResponse(BufferedReader reader)
		throws Exception
	{
		String response = getGTPResponse(reader);
		if (response.charAt(0)!='=')
			throw new IOException("The engine returned an error: "+response.substring(1).trim());
		return response.substring(1).trim();
	}

	public static String readGTPResponse(final Reader reader, int timeout)
		throws Exception
	{
		final StringBuilder response  = new StringBuilder();
		final StringBuilder error  = new StringBuilder();
		Thread readingThread = new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						response.append(getGTPResponse(reader));
					}
					catch (Exception exception)
					{
						error.append("Unexpected exception "+exception.getClass().getName()+": "+exception.getMessage());
					}
				}
			});
		
		readingThread.start();
		
		int counter=0;
		int max = timeout / 10;
		while (response.length()==0 && counter<max)
		{
			Thread.sleep(10);
			counter++;
		}
		if (counter>=max)
			throw new IOException("The engine didn't respond. Possible reason: "+error.toString());
		if (response.charAt(0)!='=')
			throw new IOException("The engine returned an error: "+response.substring(1).trim());
		return response.substring(1).trim();
	}
}
