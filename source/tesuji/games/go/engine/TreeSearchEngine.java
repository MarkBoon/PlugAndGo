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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import tesuji.core.util.ArrayStack;

import tesuji.games.general.GlobalParameters;
import tesuji.games.general.MoveAdministration;
import tesuji.games.general.MoveStack;
import tesuji.games.general.provider.DataProviderList;
import tesuji.games.general.renderer.DataRendererManager;
import tesuji.games.general.renderer.DataSelector;
import tesuji.games.general.search.Search;
import tesuji.games.general.search.SearchProperties;

import tesuji.games.go.common.BasicGoMoveAdministration;
import tesuji.games.go.common.GoEngineAdapter;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.gui.GoDataPanel;
import tesuji.games.go.gui.MCBoardController;
import tesuji.games.go.gui.MCBoardDisplay;
import tesuji.games.go.search.MonteCarloHashMapSearch;
import tesuji.games.go.util.DefaultBoardProvider;
import tesuji.games.go.util.GoGameProperties;

import tesuji.games.model.BoardModel;
import tesuji.games.util.Console;

/**
 * A Go engine that uses a Search module to search for its moves.
 * As a parameter to this engine one needs to pass an object of the type
 * Search<GoMove, SearchResult<GoMove>>
 */
public class TreeSearchEngine
	extends GoEngineAdapter
{
	private Search<GoMove> _search;
	private BasicGoMoveAdministration _moveAdministration;
	private MoveStack<GoMove> _moveList = new MoveStack<GoMove>();
	private boolean _isInitialized = false;
	
	private Logger _logger;
	
	public TreeSearchEngine()
	{		
	}

	public TreeSearchEngine(Search<GoMove> search)
	{
		_moveAdministration = new BasicGoMoveAdministration(new GoGameProperties());
		_search = search;
		_logger = Logger.getLogger(this.getClass());
	}
	
	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#clearBoard()
     */
    @Override
	public void clearBoard()
    {
    	if (!_isInitialized)
    		initialize();
    	
    	_search.clear();
    	_moveAdministration.clear();
    	_moveList.recycleMoves();
    	DataProviderList.getSingleton().fireDataChange();
    }
    
    public void initialize()
    {
    	super.initialize();
    	
    	_isInitialized = true;
		
		DataRendererManager rendererManager = new DataRendererManager();
		DataProviderList.getSingleton().addDataProvider(new DefaultBoardProvider(_moveAdministration.getBoardModel()));
		final GoDataPanel goDataPanel = new GoDataPanel(rendererManager);
		DataSelector dataSelector = new DataSelector(rendererManager);
		
		Console.getSingleton().addDataPanels(goDataPanel, dataSelector);
		
		goDataPanel.addKeyListener(new KeyAdapter()
			{
				@Override
				public void keyPressed(KeyEvent event)
				{
					if (event.getKeyCode()==KeyEvent.VK_D)
					{
						JFrame window = new JFrame();					
						MCBoardController controller = new MCBoardController(((MonteCarloHashMapSearch)_search).getAdministration(), ((MonteCarloHashMapSearch)_search).getHashMap());
						MCBoardDisplay display = new MCBoardDisplay(controller);
						Point p = goDataPanel.getLocation();
						Dimension d = goDataPanel.getSize();
						window.setLocation(p.x+d.width+20, p.y);
						window.setSize(300, 300);
						window.getContentPane().add(display);
						window.setVisible(true);
					}
				}
			});
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#getEngineName()
     */
    @Override
	public String getEngineName()
    {
	    return "TreeSearchEngine";
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#getEngineVersion()
     */
    @Override
	public String getEngineVersion()
    {
	    return _search.getClass().getSimpleName() + " / " + _search.toString();
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#play(tesuji.games.go.common.GoMove)
     */
    @Override
	public void playMove(GoMove move)
    {
    	assert move.isPass() || move.isResignation() || _moveAdministration.isLegalMove(move) : "Illegal move: "+_moveList.toSGF();
    	if (!_moveAdministration.isLegalMove(move))
    	{
    		_logger.error("The engine produced an illegal move! - "+move);
    		_logger.error("Board Administration: \n"+_moveAdministration.getBoardModel().toString());
    		_logger.error("Search Administration: \n"+_search.toString());
    	}
    		
    	_moveAdministration.playMove(move);
       	_search.playMove(move);
    	_moveList.add((GoMove)move.cloneMove());
		_logger.info("Play at "+move);
		_logger.info("Search Administration ("+_search.getClass().getSimpleName()+"): \n"+_search.toString());
    	DataProviderList.getSingleton().fireDataChange();
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#requestMove(byte)
     */
    @Override
	public GoMove requestMove(byte color)
    {
    	GoMove move = null;
		try
		{
			move = (GoMove)_search.doSearch(color).cloneMove();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
    	
    	if (move==null)
    		return getMoveFactory().createPassMove(color);
//    	else if (!move.isPass() && result.isHopeless())
//    		return getMoveFactory().createResignMove(color);
    	else
    	{
    		MoveStack<GoMove> moveList = new MoveStack<GoMove>();
    		getBestMovePath(moveList);
    		_logger.info(moveList.toSGF());
    		_logger.info("Move: "+move.toString());
    		return move;
    	}
    }
    
	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#setTimeConstraints(int, int, int)
     */
    @Override
	public void setTimeConstraints(int mainTime, int byoYomiTime,
                    int nrByoYomiStones)
    {
    	int boardSize = _moveAdministration.getBoardModel().getBoardSize();
    	int nrEstimatedMoves = boardSize * boardSize;
    	nrEstimatedMoves += nrEstimatedMoves/5; // Add 20%
    	if (nrEstimatedMoves==0)
    		nrEstimatedMoves = 1;
    	int secondsPerMove = mainTime / nrEstimatedMoves + byoYomiTime / nrByoYomiStones;
		_search.getSearchProperties().setIntProperty(SearchProperties.TIME_PER_MOVE, secondsPerMove);
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#setTimeLeft(byte, int, int)
     */
    @Override
	public void setTimeLeft(byte color, int timeRemaining, int nrStonesRemaining)
    {
    	int secondsPerMove = timeRemaining / nrStonesRemaining;
		_search.getSearchProperties().setIntProperty(SearchProperties.TIME_PER_MOVE, secondsPerMove);
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.common.GoEngineAdapter#set(java.lang.String, java.lang.String)
     */
    @Override
	public void set(String propertyName, String propertyValue)
    {
    	super.set(propertyName,propertyValue);
    	_search.getSearchProperties().setProperty(propertyName, propertyValue);
    	_moveAdministration.getGameProperties().setProperty(propertyName, propertyValue);
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#takeBack()
     */
    @Override
	public void takeBack()
    {
    	_moveAdministration.takeBack();
    	_search.takeBack();
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.general.GameEngineAdapter#getScore()
     */
    @Override
    public Double getScore()
    {
    	return new Double(_moveAdministration.getScore());
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoEngine#setup(tesuji.core.util.ArrayList)
     */
    @Override
	public void setup(Iterable <GoMove> moveList)
    {
    	for (GoMove move : moveList)
    	{
    		_moveAdministration.playMove(move);
    		_search.playMove(move);
    	}
    }

	public void getBestMovePath(ArrayStack<GoMove> moveList)
	{
		_search.getBestMovePath(moveList);
	}
	
	public boolean isLegal(GoMove move)
	{
		return _moveAdministration.isLegalMove(move);
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.GameEngineAdapter#getBoardModel()
	 */
	@Override
	public BoardModel getBoardModel()
	{
		return _moveAdministration.getBoardModel();
	}
	
	public boolean getIsTestVersion()
	{
		return GlobalParameters.isTestVersion();
	}

	public void setIsTestVersion(boolean flag)
	{
		GlobalParameters.setTestVersion(flag);
	}
	
	public boolean getIsCollectingStatistics()
	{
		return GlobalParameters.isCollectingStatistics();
	}

	public void setIsCollectingStatistics(boolean flag)
	{
		GlobalParameters.setCollectingStatistics(flag);
	}
	
	public MoveAdministration<GoMove, GoGameProperties> getMoveAdministration()
	{
		return _moveAdministration;
	}
	
	public String toString()
	{
		return _moveAdministration.getBoardModel().toString();
	}
}
