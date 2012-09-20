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
package tesuji.games.go.engine;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import tesuji.core.util.LoggerConfigurator;
import tesuji.games.general.ColorConstant;
import tesuji.games.general.search.Search;
import tesuji.games.general.search.SearchProperties;
import tesuji.games.general.search.SearchResult;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.util.GoGameProperties;

public class EngineTester
{
	private static Logger _logger = Logger.getLogger("EngineTesterMain");

	private int _boardSize;
	private double _komi;
	private int _nrPlayouts;
	private int _nrProcessors;
	private Search<GoMove> _blackPlayer;
	private Search<GoMove> _whitePlayer;

	public static void main(String[] args)
	{
    	LoggerConfigurator.configure();
    	_logger.setLevel(Level.INFO);

    	try
		{
			Resource resource = new FileSystemResource("source/tesuji/games/go/engine/EngineConfig.xml");
			XmlBeanFactory factory = new XmlBeanFactory(resource);
			EngineTester tester = (EngineTester) factory.getBean("EngineTester");
			tester.playGame();
		}
		catch (Exception exception)
		{
			System.err.println(exception.getMessage());
			exception.printStackTrace();
		}
	}
	
	private void playGame() throws Exception
	{
		_blackPlayer.getSearchProperties().setIntProperty(GoGameProperties.BOARDSIZE, _boardSize);
		_blackPlayer.getSearchProperties().setDoubleProperty(GoGameProperties.KOMI, _komi);
		_blackPlayer.getSearchProperties().setIntProperty(SearchProperties.NR_NODES, _nrPlayouts);
		_blackPlayer.getSearchProperties().setIntProperty(SearchProperties.NR_PROCESSORS, _nrProcessors);
		_whitePlayer.getSearchProperties().setIntProperty(GoGameProperties.BOARDSIZE, _boardSize);
		_whitePlayer.getSearchProperties().setDoubleProperty(GoGameProperties.KOMI, _komi);
		_whitePlayer.getSearchProperties().setIntProperty(SearchProperties.NR_NODES, _nrPlayouts);
		_whitePlayer.getSearchProperties().setIntProperty(SearchProperties.NR_PROCESSORS, _nrProcessors);

		while (!_blackPlayer.isGameFinished())
		{
			SearchResult<GoMove> blackResult = _blackPlayer.doSearch(ColorConstant.BLACK);
			GoMove blackMove = (GoMove)blackResult.getMove().cloneMove();
			_blackPlayer.playMove(blackMove);
			_whitePlayer.playMove(blackMove);
			
			if (_whitePlayer.isGameFinished())
				break;
			
			SearchResult<GoMove> whiteResult = _whitePlayer.doSearch(ColorConstant.WHITE);
			GoMove whiteMove = (GoMove)whiteResult.getMove().cloneMove();
			_blackPlayer.playMove(whiteMove);
			_whitePlayer.playMove(whiteMove);
		}
	}
	
	public int getBoardSize() 
	{
		return _boardSize;
	}

	public void setBoardSize(int size) 
	{
		_boardSize = size;
	}

	public double getKomi() 
	{
		return _komi;
	}

	public void setKomi(double _komi) 
	{
		this._komi = _komi;
	}

	public int getNrPlayouts() 
	{
		return _nrPlayouts;
	}

	public void setNrPlayouts(int playouts) 
	{
		_nrPlayouts = playouts;
	}

	public int getNrProcessors() 
	{
		return _nrProcessors;
	}

	public void setNrProcessors(int nrProcessors) 
	{
		_nrProcessors = nrProcessors;
	}

	public void setBlackPlayer(Search blackPlayer)
    {
	    _blackPlayer = blackPlayer;
    }

	public Search getBlackPlayer()
    {
	    return _blackPlayer;
    }

	public void setWhitePlayer(Search whitePlayer)
    {
	    _whitePlayer = whitePlayer;
    }

	public Search getWhitePlayer()
    {
	    return _whitePlayer;
    }
}
