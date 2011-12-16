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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

import tesuji.games.general.Move;

import static tesuji.games.gtp.GTPCommand.*;

/**
 * This class implements a loop that reads a GTPCommand from an input stream,
 * calls handleCommand of the GTPGoEngineBridge and writes the results of the command
 * back into the output stream.
 */
public class GTPCommandLoop<MoveType extends Move>
{
	private static final Logger _logger = Logger.getLogger(GTPCommandLoop.class);

	private final PrintStream _log;
	private final GTPToGameEngine<MoveType> _gtpBridge;

	private PrintStream _outputStream;
	
	GTPCommandLoop(PrintStream log, GTPToGameEngine<MoveType> bridge)
	{
		_log = log;
		_gtpBridge = bridge;
	}

	public void start(InputStream in, OutputStream out)
	{
		_outputStream = new PrintStream(out);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		while (true)
		{
			try
			{
				if (reader.ready())
				{
					String line = reader.readLine();
					
					if (line == null)
						return;
					
					StringBuffer commandLine = preprocessInput(line);
					GTPCommand gtpCommand = new GTPCommand(commandLine);
					processCommand(gtpCommand);
					
					if (gtpCommand.getCommand().equals(QUIT))
						return;
				}
				else
					Thread.sleep(50);
			}
			catch (Exception exception)
			{
				_logger.error("Unexpected exception "+exception.getClass().getName()+": "+exception.getMessage(),exception);				
				_logger.error("Stacktrace:"+exception.getStackTrace());
				exception.printStackTrace();
				if (_log!=null)
				{
					_log.println("Unexpected exception: "+exception.getMessage());
					exception.printStackTrace(_log);
				}
				return;
			}
		}
	}

	private void processCommand(GTPCommand command)
	{
		boolean statusOK = true;
		String engineResponse;
		try
		{
			engineResponse = _gtpBridge.handleCommand(command);
			
			if (engineResponse.equals(UNKNOWN_COMMAND))
				statusOK = false;
		}
		catch (GTPError e)
		{
			engineResponse = e.getMessage();
			statusOK = false;
		}
		
		StringBuffer processedResponse = preprocessOutput(engineResponse);
		StringBuffer gtpResponse = new StringBuffer();
		
		// Set message status
		if (statusOK)
			gtpResponse.append(STATUS_OK);
		else
			gtpResponse.append(STATUS_ERROR);
		// Insert ID if it has one
		if (command.hasID())
			gtpResponse.append(command.getID());
		if (processedResponse.toString().trim().length()!=0)
			gtpResponse.append(' ');
		// And append the rest of the message.
		gtpResponse.append(processedResponse);
		
		// Make sure the message ends with a line-feed.
		if (!gtpResponse.toString().endsWith("\n"))
			gtpResponse.append('\n');
		
		_logger.debug("GTPResponse: '"+gtpResponse+"'");
		_outputStream.println(gtpResponse);
		_outputStream.flush();
	}

	/**
	 * According to the GTP, the following preprocessing is necessary on input:<br>
	 * <br>
	 * 1. Remove all occurences of CR and other control characters except for HT
	 * and LF.<br> 
	 * 2. For each line with a hash sign (#), remove all text following
	 * and including this character.<br>
	 * 3. Convert all occurences of HT to SPACE.<br>
	 * 4. Discard any empty or white-space only lines.<br>
	 * 
	 * @param line
	 * @return preprocessed command-line
	 */
	public static StringBuffer preprocessInput(String line)
	{
		int len = line.length();
		StringBuffer result = new StringBuffer(len);
		for (int i = 0; i < len; ++i)
		{
			char c = line.charAt(i);
			// Remove everything after the comment character.
			if (c == COMMENT_START)
				break;
			// Replace HT by SPACE
			if (c == '\t')
				result.append(' ');
			// Keep line-feeds.
			else if (c == '\n')
				result.append('\n');
			// Remove all other control-characters.
			else if (Character.isISOControl(c))
				continue;
			else
				result.append(c);
		}
		return result;
	}

	/**
	 * Accrding to the GTP, the following preprocessing is necessary on output:<br>
	 * <br>
	 * 1. Remove all occurences of CR and other control characters except for HT
	 * and LF.<br> 
	 * 2. Convert all occurences of HT to SPACE.<br>
	 * 3. Make sure there are no two consecutive line-feeds as it means end of
	 * message.<br>
	 * 
	 * @param line
	 * @return preprocessed command-line
	 */
	public static StringBuffer preprocessOutput(String line)
	{
		boolean lastWasLineFeed = false;
		int len = line.length();
		StringBuffer result = new StringBuffer(len);
		for (int i = 0; i < len; ++i)
		{
			char c = line.charAt(i);
			// Replace HT by SPACE
			if (c == '\t')
				c = ' ';
			// Keep line-feeds.
			if (c == '\n')
			{
				// But insert a space when there are two in a row.
				if (lastWasLineFeed)
					result.append(' ');
				result.append('\n');
				lastWasLineFeed = true;
				continue;
			}
			else
				lastWasLineFeed = false;
			// Remove all other control-characters.
			if (Character.isISOControl(c))
				continue;
			else
				result.append(c);
		}
		return result;
	}
}
