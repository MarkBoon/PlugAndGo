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
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import tesuji.games.general.GameEngine;
import tesuji.games.general.Move;
import tesuji.games.general.MoveFactory;
import tesuji.games.model.BoardModel;

/**
 * This is a GameEngine place-holder that waits for a real GameEngine to connect
 * to a socket. It then uses this connected GameEngine to delegate all GTP
 * calls to. This allows programs to start the place-holder while the real engine
 * can be started from a development environment. This has the advantage of
 * being able to use the IDE's debugger with all its features.
 * 
 * Until the real engine connects to the socket this class will respond to the
 * standard GTP calls 'name', 'version' and 'list_commands' using default responses.
 * All other commands will result in an error when no engine is connected yet.
 */
public abstract class GameEngineSocketAdapter<MoveType extends Move>
	implements GameEngine<MoveType>
{
	private static final int WAITING_TIME = 5;
	
	private static final Logger _logger = Logger.getLogger(GameEngineSocketAdapter.class);

	private GameEngineToGTP<MoveType> _engineDelegate;

	public GameEngineSocketAdapter()
	{
	}
	
	public void openSocket()
	{
		_logger.info("Open socket.");
		startServerSocket();
		_logger.info("Socket opened, waiting "+WAITING_TIME+" min. for engine to connect...");
		for (int i=0; i<WAITING_TIME*1000; i++)
		{
			if (_engineDelegate==null)
			{
	            try
                {
                	Thread.sleep(60);
                }
                catch (InterruptedException e)
                {
	                e.printStackTrace();
                }
			}
            else
			{
				_logger.info("Engine "+_engineDelegate.getEngineName()+" connected.");
				return;
			}
		}
		_logger.warn("No engine connected yet. Limited commands available until a real engine connects.");		
	}
	
	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#clearBoard()
     */
    public void clearBoard()
    {
    	if (!isConnected())
    		openSocket();

       	if (_engineDelegate!=null)
    		_engineDelegate.clearBoard();
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#generateMove(byte)
     */
    public MoveType generateMove(byte color)
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		return _engineDelegate.generateMove(color);

    	return null; // Unfortunately I don't think we have anything more elegant to do.
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#getEngineName()
     */
    public String getEngineName()
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		return _engineDelegate.getEngineVersion();

	    return "GameEngine socket unconnected";
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#getEngineVersion()
     */
    public String getEngineVersion()
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		return _engineDelegate.getEngineVersion();

	    return "GameEngine socket unconnected";
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#getFinalScore()
     */
    public String getFinalScore()
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		return _engineDelegate.getFinalScore();
    	
	    return "GameEngine socket unconnected";
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#getMoveFactory()
     */
    public abstract MoveFactory<MoveType> getMoveFactory();

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#getScore()
     */
    public Double getScore()
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		return _engineDelegate.getScore();

	    return null;
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#play(tesuji.games.general.Move)
     */
    public void playMove(MoveType move)
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		_engineDelegate.playMove(move);
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#quit()
     */
    public void quit()
    {
		System.exit(0);
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#requestCandidates(int)
     */
    public Iterable<MoveType> requestCandidates(int n)
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		return _engineDelegate.requestCandidates(n);

	    return null;
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#requestMove(byte)
     */
    public MoveType requestMove(byte color)
    {
    	if (_engineDelegate!=null)
    		return _engineDelegate.requestMove(color);

	    return null;
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#set(java.lang.String, java.lang.String)
     */
    public void set(String propertyName, String propertyValue)
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		_engineDelegate.set(propertyName,propertyValue);
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#setTimeConstraints(int, int, int)
     */
    public void setTimeConstraints(int mainTime, int byoYomiTime,
                    int nrByoYomiStones)
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		_engineDelegate.setTimeConstraints(mainTime,byoYomiTime,nrByoYomiStones);
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#setTimeLeft(byte, int, int)
     */
    public void setTimeLeft(byte color, int timeRemaining, int nrStonesRemaining)
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		_engineDelegate.setTimeLeft(color,timeRemaining,nrStonesRemaining);
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#setup(tesuji.games.general.MoveStack)
     */
    public void setup(Iterable<MoveType> moveList)
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		_engineDelegate.setup(moveList);
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#setup(java.lang.String[])
     */
    public void setup(String[] diagram)
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		_engineDelegate.setup(diagram);
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#takeBack()
     */
    public void takeBack()
    {
    	if (!isConnected())
    		openSocket();

	    if (_engineDelegate!=null)
	    	_engineDelegate.takeBack();
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.MoveGenerator#requestMove(byte, tesuji.core.util.ArrayList)
     */
    public MoveType requestMove(byte color, Iterable<MoveType> alreadyTriedList)
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		return _engineDelegate.requestMove(color, alreadyTriedList);

	    return null;
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.Evaluator#evaluate()
     */
    public void evaluate()
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		_engineDelegate.evaluate();
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.GameEngine#getboardModel()
     */
    public BoardModel getBoardModel()
    {
    	if (!isConnected())
    		openSocket();

    	if (_engineDelegate!=null)
    		return _engineDelegate.getBoardModel();
    	
    	return null;
    }
    
	public boolean isConnected()
	{
		return (_engineDelegate!=null);
	}

	private void startServerSocket()
	{
		try
		{
			final ServerSocket serverSocket = new ServerSocket(3434);
			final Thread thread = new Thread(new Runnable()
					{
						public void run()
						{
							try
                            {
								while (true)
								{
		                            Socket connection = serverSocket.accept();
		                            synchronized (GameEngineSocketAdapter.this)
                                    {
			                            if (_engineDelegate==null)
			                            {
			                            	_engineDelegate = new GameEngineToGTP<MoveType>(connection.getInputStream(),connection.getOutputStream(),getMoveFactory());
			                            	_logger.info("Engine connected.");
			                            	_logger.debug("Engine "+_engineDelegate.getEngineName()+_engineDelegate.getEngineVersion()+" connected to socket");
			                            	
			                            	//copyPentUpProperties();
			                            	//_engineDelegate.clearBoard();
			                            }
			                            else
			                            {
			                            	connection.close();
			                            	_logger.debug("An engine already connected to the socket, new connection closed.");
			                            }
									}
                            	}
                            }
                            catch (IOException ex)
                            {
                            	_logger.error("IOException: "+ex.getMessage());
                            }
						}
					}
				);
			thread.start();
		}
		catch (IOException ex)
		{
			_logger.error("IOException: "+ex.getMessage());
		}
	}
}
