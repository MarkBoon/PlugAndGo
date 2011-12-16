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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

import tesuji.games.general.GameEngine;
import tesuji.games.general.Move;
import tesuji.games.go.util.GoGameProperties;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.gtp.GTPCommand.*;

/**
 * This class acts as a bridge between a GTPCommand and a GoEngine. The method
 * handleCommand does all the work and it makes all the corresponding calls to
 * the GoEngine delegate.
 */
public class GTPToGameEngine<MoveType extends Move>
	implements GTPCommandHandler
{
	private static final Logger _logger = 	Logger.getLogger(GTPToGameEngine.class);

	private GameEngine<MoveType> _engine;
	
	public GTPToGameEngine(GameEngine<MoveType> engine) 
	{
		_engine = engine;
	}
	
	public void start(InputStream in, OutputStream out, PrintStream err)
	{
		GTPCommandLoop<MoveType> commandLoop = new GTPCommandLoop<MoveType>(err,this);
		commandLoop.start(in,out);
	}
	
	/**
	 * Handle a GTPCommand.
	 * 
	 * It gets a command and makes the corresponding call to the GoEngine. It
	 * then returns the result of that in the form of a string.
	 * 
	 * @param gtpCommand to be executed by the GoEngine
	 */
    public String handleCommand(GTPCommand gtpCommand)
		throws GTPError 
	{
		_logger.info("handleCommand#start "+gtpCommand);
		String command = gtpCommand.getCommand();
		String response = "";
		
		try
		{
			if (command.equals(NAME))
			{
				response = _engine.getEngineName();
			}
			else if (command.equals(VERSION))
			{
				response = _engine.getEngineVersion();
			}
			else if (command.equals(PROTOCOL_VERSION))
			{
				response = "2.0";
			}
			else if (command.equals(LIST_COMMANDS))
			{
				response = listCommands();
			}
			else if (command.equals(KNOWN_COMMAND))
			{
				String arg = gtpCommand.getArgumentLine() + "\n";
				if (listCommands().indexOf(arg)>=0)
					response = "true";
				else
					response = "false";
			}
			else if (command.equals(BOARDSIZE))
			{
				_engine.set(GoGameProperties.BOARDSIZE,gtpCommand.getArgumentLine());
			}
			else if (command.equals(KOMI))
			{
				_engine.set(GoGameProperties.KOMI,gtpCommand.getArgumentLine());
			}
			else if (command.equals(CLEAR_BOARD))
			{
				_engine.clearBoard();
			}
			else if (command.equals(GENMOVE))
			{
				String arg = gtpCommand.getArgumentLine().toLowerCase();
				byte color = EMPTY;
				if (arg.startsWith(WHITE_COLOR))
					color = WHITE;
				if (arg.startsWith(BLACK_COLOR))
					color = BLACK;
				_logger.info("handleCommand#genmove");
				@SuppressWarnings("unchecked")
				MoveType move = (MoveType)_engine.requestMove(color).cloneMove();
				_logger.info("handleCommand#play");
				if (!move.isResignation())
					_engine.playMove(move);
				response = move.toGTP();
				move.recycle();
			}
			else if (command.equals(REQUEST_MOVE))
			{
				String arg = gtpCommand.getArgumentLine().toLowerCase();
				byte color = EMPTY;
				if (arg.startsWith(WHITE_COLOR))
					color = WHITE;
				if (arg.startsWith(BLACK_COLOR))
					color = BLACK;
				@SuppressWarnings("unchecked")
				MoveType move = (MoveType)_engine.requestMove(color).cloneMove();
				response = move.toGTP();
				move.recycle();
			}
			else if (command.equals(PLAY))
			{
				MoveType move = _engine.getMoveFactory().parseMove(gtpCommand.getArgumentLine().toLowerCase());				
				_engine.playMove(move);
				_engine.evaluate();
				move.recycle();
			}
			else if (command.equals(TAKEBACK))
			{
				_engine.takeBack();
			}
			else if (command.equals(LOAD_SGF))
			{
				String sgf = gtpCommand.getArgumentLine();
				_logger.debug("Load SGF: "+sgf);
				_engine.setup(_engine.getMoveFactory().parseSGF(sgf));
			}
			else if (command.equals(FINAL_SCORE))
			{
				response = _engine.getFinalScore();
			}
			else if (command.equals(SCORE))
				response = ""+_engine.getScore();
			else if (command.equals(TIME_SETTINGS))
			{
				String[] args = gtpCommand.getArguments();
				_engine.setTimeConstraints(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			}
			else if (command.equals(TIME_LEFT))
			{
				String[] args = gtpCommand.getArguments();
				byte color = EMPTY;
				if (args[0].startsWith("w"))
					color = WHITE;
				if (args[0].startsWith("b"))
					color = BLACK;
				_engine.setTimeLeft(color, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			}
			else if (command.equals(QUIT))
				_engine.quit();
			else if (command.equals(HEART_BEAT))
				; // No need to do anything, just proceed to show it's alive.
			else
				// Simply pass on any unknown commands as a property-value pair
				_engine.set(command,gtpCommand.getArgumentLine());
		}
		catch (Exception exception)
		{
			_logger.error("GTPError",exception);
			exception.printStackTrace();
			throw new GTPError(exception.getMessage());		
		}
		return response;
	}

	public String listCommands()
	{
		String commands = "";
		
		commands += LIST_COMMANDS+"\n";
		commands += KNOWN_COMMAND+"\n";
		commands += PROTOCOL_VERSION+"\n";
		commands += NAME+"\n";
		commands += VERSION+"\n";
		commands += GENMOVE+"\n";
		commands += BOARDSIZE+"\n";
		commands += QUIT+"\n";
		commands += CLEAR_BOARD+"\n";
		commands += KOMI+"\n";
		commands += PLAY+"\n";
		commands += TAKEBACK+"\n";
		commands += HEART_BEAT+"\n";
		commands += LOAD_SGF+"\n";
		commands += FINAL_SCORE+"\n";
//		commands += SCORE+"\n";
//		commands += TIME_SETTINGS+"\n";
//		commands += TIME_LEFT+"\n";
		
		return commands;
	}
}
