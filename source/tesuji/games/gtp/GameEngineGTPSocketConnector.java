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

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import tesuji.games.general.GameEngine;
import tesuji.games.general.Move;
import tesuji.games.util.Console;

/**
 * This class connects a GameEngine to a socket and communicates according to 
 * the GTP protocol. The advantage of using a socket instead of the standard
 * input- and output-streams is that it's easier to start the engine in a debugger.
 */
public class GameEngineGTPSocketConnector<MoveType extends Move>
{
	private static final Logger _logger = Logger.getLogger(GameEngineGTPSocketConnector.class);
	
	private String _host;
	private int _port;
	private String _engineName;
	private boolean _keepTrying = false;
	
	public GameEngineGTPSocketConnector()
	{
		Logger.getRootLogger().addAppender(Console.getSingleton().getLogAppender());
	}
	
	public void init(String[] args)
	{
		if (args.length<2)
		{
			_host = "localhost";
			_port = 3434;
		}
		else
		{
			_host = args[0];
			_port = Integer.valueOf(args[1]);
		}
		Console.getSingleton();
	}

	public void connectEngine(GameEngine<MoveType> engine)
	{
		_engineName = engine.getEngineName()+" v"+engine.getEngineVersion();
		
        GTPToGameEngine<MoveType> bridge = new GTPToGameEngine<MoveType>(engine);
		GTPCommandLoop<MoveType> commandLoop = new GTPCommandLoop<MoveType>(System.err,bridge);
		
		Console.getSingleton().setTitle(_engineName);

		do
		{
			try
			{
				Socket socket = new Socket(_host,_port);
				Console.getSingleton().setSocket(socket);
				commandLoop.start(socket.getInputStream(),socket.getOutputStream());
				_keepTrying = false;
			}
			catch (UnknownHostException ex)
			{
				_logger.error("Unknown host: "+_host);
				if (_keepTrying)
				{
					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e) {}
				}
				else
					quit();
			}
			catch (IOException ex)
			{
				_logger.error("IOException: "+ex.getMessage());
				if (_keepTrying)
				{
					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e) {}
				}
				else
					quit();
			}
		}
		while (_keepTrying);
	}

	private void quit()
	{
		try
		{
			Thread.sleep(3000);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(-1);
	}
	
	public String getEngineName()
	{
		return _engineName;
	}
	
	public void setKeepTrying(boolean flag)
	{
		_keepTrying = flag;
	}
}
