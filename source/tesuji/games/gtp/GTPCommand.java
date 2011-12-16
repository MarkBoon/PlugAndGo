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

import java.util.ArrayList;

/**
 * This class represents a command send through the GTP protocol.
 * It consists of a command-name, the arguments to the command and optionally
 * it contains a command-id. Having GoGui as an example saved a lot of time,
 * which maybe shows in the code.
  */
public class GTPCommand
{
	// Required commands.
	public static final String LIST_COMMANDS =		"list_commands";
	public static final String KNOWN_COMMAND =		"known_command";
	public static final String NAME =				"name";
	public static final String VERSION =			"version";
	public static final String PROTOCOL_VERSION =	"protocol_version";
	public static final String GENMOVE =			"genmove";
	public static final String BOARDSIZE =			"boardsize";
	public static final String QUIT =				"quit";
	public static final String CLEAR_BOARD =		"clear_board";
	public static final String KOMI =				"komi";
	public static final String PLAY =				"play";

	// Optional commands
	public static final String FINAL_SCORE =		"final_score";
	public static final String TIME_SETTINGS =		"time_settings";
	public static final String TIME_LEFT =			"time_left";
	public static final String REQUEST_MOVE =		"req_genmove";
	
	// Non-standard commands
	public static final String TAKEBACK =			"undo";
	public static final String LOAD_SGF =			"load_sgf";
	public static final String HEART_BEAT =			"heart_beat";
	public static final String SCORE =				"score";
	// TODO - define more commands
	
	// Special commands
	public static final String UNKNOWN_COMMAND =	"unknown command";

	// Characters with special meaning.
	public static final char COMMENT_START =		'#';
	public static final char STATUS_OK =			'=';
	public static final char STATUS_ERROR =			'?';

	// Predefined strings used in GTP commands
	public static final String BLACK_COLOR =		"b";
	public static final String WHITE_COLOR =		"w";
	public static final String PASS_MOVE =			"pass";
	public static final String RESIGN_MOVE =		"resign";

	// Private members.
	private String commandLine;
	private int id;
	private String command;
	private String[] arguments;

	/**
	 * Parse a command and construct a GTPCommand
	 * 
	 * @param line to parse
	 */
	public GTPCommand(StringBuffer line)
	{
		String[] array = tokenize(line);
		assert (array.length > 0);
		int commandIndex = 0;
		
		if (Character.isDigit(array[0].charAt(0)))
		{
			try
			{
				id = Integer.parseInt(array[0]);
				commandLine = line.substring(array[0].length()).trim();
				commandIndex = 1;
			}
			catch (NumberFormatException e)
			{
				id = Integer.MIN_VALUE;
				commandLine = line.toString();
			}
		}
		else
		{
			id = Integer.MIN_VALUE;
			commandLine = line.toString();
		}

		if (commandIndex >= array.length)
		{
			command = "";
			arguments = null;
			return;
		}
		
		command = array[commandIndex];
		int nrArguments = array.length - commandIndex - 1;
		arguments = new String[nrArguments];
		for (int i = 0; i < nrArguments; ++i)
			arguments[i] = array[commandIndex + i + 1];
	}

	public boolean hasID()
	{
		return (id!=Integer.MIN_VALUE);
	}

	/**
	 * @return the argument part of the command-line.
	 */
	public String getArgumentLine()
	{
		int pos = commandLine.indexOf(command) + command.length();
		return commandLine.substring(pos).trim();
	}

	/**
	 * @return the command part of the command-line.
	 */
	public String getCommand()
	{
		return command;
	}

	/**
	 * @return an array with arguments.
	 */
	public String[] getArguments()
	{
		return arguments;
	}

	/**
	 * @return the command-line with its arguments but without its ID.
	 */
	public String getCommandLine()
	{
		return commandLine;
	}

	public int getID()
	{
		return id;
	}
	
	@Override
	public String toString()
	{
		return commandLine;
	}

	/**
	 * Tokenize the command-line in space-separated pieces and put them in an
	 * array.
	 * 
	 * @param string containing the command-line
	 * @return String[] array with tokens.
	 */
	private String[] tokenize(StringBuffer line)
	{
		assert (line != null);
		ArrayList<String> vector = new ArrayList<String>();
		boolean escape = false;
		boolean inString = false;
		StringBuffer token = new StringBuffer();
		
		for (int i = 0; i < line.length(); ++i)
		{
			char c = line.charAt(i);
			if (c == '"' && !escape)
			{
				if (inString)
				{
					vector.add(token.toString());
					token.setLength(0);
				}
				inString = !inString;
			}
			else if (Character.isWhitespace(c) && !inString)
			{
				if (token.length() > 0)
				{
					vector.add(token.toString());
					token.setLength(0);
				}
			}
			else
				token.append(c);
			escape = (c == '\\' && !escape);
		}
		
		if (token.length() > 0)
			vector.add(token.toString());

		int size = vector.size();
		String result[] = new String[size];
		for (int i = 0; i < size; ++i)
			result[i] = vector.get(i);
		return result;
	}
}
