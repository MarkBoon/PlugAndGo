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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import tesuji.games.general.GameEngineAdapter;
import tesuji.games.general.GameProperties;
import tesuji.games.general.Move;
import tesuji.games.general.MoveFactory;
import tesuji.games.general.MoveStack;

import static tesuji.games.general.ColorConstant.*;

/**
 * This class acts as a GameEngine and translates the calls into GTP requests.
 * Usually this is used by a user-interface to translate user-actions in calls
 * to a GameEngine using the GTP protocol to communicate.
 * 
 * This of course assumes there's a GameEngine actually responding on the other end.
 * The actual GameEngine implementation will use the counterpart of this class
 * called GTPToGameEngine
 */
public class GameEngineToGTP<MoveType extends Move>
	extends GameEngineAdapter<MoveType>
{
	private BufferedReader _reader;
	private PrintStream _outputStream;
	private MoveFactory<MoveType> _moveFactory;
	
	private boolean _alive = true;
	
	private static final Logger _logger = 	Logger.getLogger(GameEngineToGTP.class);
	
	public GameEngineToGTP(InputStream in, OutputStream out, MoveFactory<MoveType> moveFactory)
	{
		_reader = new BufferedReader(new InputStreamReader(in));
		_outputStream = new PrintStream(out);
		_moveFactory = moveFactory;
	}
	
	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#getMoveFactory()
	 */
	@Override
	public MoveFactory<MoveType> getMoveFactory()
	{
		return _moveFactory;
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#clearBoard()
	 */
	@Override
	public void clearBoard()
	{
		sendGTPCommand(GTPCommand.CLEAR_BOARD);
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#generateMove(byte)
	 */
	@Override
	public MoveType generateMove(byte color)
	{
		String colorString = (color==BLACK ? GTPCommand.BLACK_COLOR : GTPCommand.WHITE_COLOR);
		String response =  sendGTPCommand(GTPCommand.GENMOVE+" "+colorString);
		String moveString = colorString+" "+response.toLowerCase();
		return getMoveFactory().parseMove(moveString);
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#getEngineName()
	 */
	@Override
	public String getEngineName()
	{
		return sendGTPCommand(GTPCommand.NAME);
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#getEngineVersion()
	 */
	@Override
	public String getEngineVersion()
	{
		String versionString = sendGTPCommand(GTPCommand.VERSION);
		if (versionString.length()>15)
			return versionString.substring(0,15);
		return versionString;
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#getScore()
	 */
	@Override
	public Double getScore()
	{
		return Double.parseDouble(sendGTPCommand(GTPCommand.SCORE));
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#getFinalScore()
	 */
	@Override
	public String getFinalScore()
	{
		return sendGTPCommand(GTPCommand.FINAL_SCORE);
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#play(tesuji.games.go.common.GoMove)
	 */
	@Override
	public void playMove(MoveType move)
	{
		sendGTPCommand(GTPCommand.PLAY+" "+move.toGTPWithColor());
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#quit()
	 */
	@Override
	public void quit()
	{
		sendGTPCommand(GTPCommand.QUIT);
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#requestMove(byte)
	 */
	@Override
	public MoveType requestMove(byte color)
	{
		String colorString = (color==BLACK ? GTPCommand.BLACK_COLOR : GTPCommand.WHITE_COLOR);
		String response =  sendGTPCommand(GTPCommand.REQUEST_MOVE+" "+colorString);
		String moveString = colorString+" "+response.toLowerCase();
		return getMoveFactory().parseMove(moveString);
	}
	
	@Override
	public void set(String propertyName, String propertyValue)
	{
		sendGTPCommand(propertyName+" "+propertyValue);
	}

    @Override
	public void setProperties(GameProperties properties)
    {
    	for (Enumeration<Object> e=properties.keys(); e.hasMoreElements();)
    	{
    		Object key = e.nextElement();
    		set(key.toString(),properties.get(key).toString());
    	}
    }
    
	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#setTimeConstraints(int, int, int)
	 */
	@Override
	public void setTimeConstraints(int mainTime, int byoYomiTime,
	                int nrByoYomiStones)
	{
		sendGTPCommand(GTPCommand.TIME_SETTINGS+" "+mainTime+" "+byoYomiTime+" "+nrByoYomiStones);
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#setTimeLeft(byte, int, int)
	 */
	@Override
	public void setTimeLeft(byte color, int timeRemaining, int nrStonesRemaining)
	{
		String colorString = (color==BLACK ? GTPCommand.BLACK_COLOR : GTPCommand.WHITE_COLOR);
		sendGTPCommand(GTPCommand.TIME_LEFT+" "+colorString+" "+timeRemaining+" "+nrStonesRemaining);
	}

	/* (non-Javadoc)
	 * @see tesuji.games.go.common.GoEngine#takeBack()
	 */
	@Override
	public void takeBack()
	{
		sendGTPCommand(GTPCommand.TAKEBACK);
	}
	
	public void sendHeartBeat()
	{
		sendGTPCommand(GTPCommand.HEART_BEAT);
	}
	
	private String sendGTPCommand(String gtpCommand)
	{
		_logger.debug("Send GTP command '"+gtpCommand+"'");
		_outputStream.println(gtpCommand);
		_outputStream.flush();
		String response = getResponse();
		if (response.startsWith("="))
			return response.substring(1).trim();
		return response.trim();
	}
	
	public boolean isAlive()
	{
		return _alive;
	}
	
	private String getResponse()
	{
		try
		{
			StringBuffer result = new StringBuffer();
			while (true)
			{
				if (_reader.ready())
				{
					int c = _reader.read();
					if (c!='\n' && Character.isISOControl(c)) // This is necessary to strip it of any CR that may have been inserted after LF by Windows.
						continue;
					result.append((char)c);
					if (result.lastIndexOf("\n\n")!=-1)
						break;
				}
				else
				{
					try
                    {
	                    Thread.sleep(50);
                    }
                    catch (InterruptedException exception)
                    {
	                    // TODO Auto-generated catch block
	                    exception.printStackTrace();
                    }
				}
			}

			_logger.debug("getResponse: '"+result.toString()+"'");
			return result.toString();
		}
		catch (IOException ex)
		{
			_logger.error("Unexpected exception#getResponse: "+ex.getMessage());
			_alive = false;
			return "";
		}
	}

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#setup(tesuji.core.util.GoMove)
     */
    public void setup(MoveStack<MoveType> moveList)
    {
    	if (moveList.size()!=0)
    		sendGTPCommand(GTPCommand.LOAD_SGF+" "+moveList.toSGF());
    }
}
