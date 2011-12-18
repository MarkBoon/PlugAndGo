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

package tesuji.games.go.search.uct;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

import tesuji.core.util.ArrayStack;

import tesuji.games.general.Checksum;
import tesuji.games.general.Move;
import tesuji.games.general.TreeNode;
import tesuji.games.general.TreeNodeFactory;
import tesuji.games.general.search.Search;
import tesuji.games.general.search.SearchProperties;

import tesuji.games.go.common.GoMove;
import tesuji.games.go.monte_carlo.MonteCarloAdministration;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.IntStack;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.util.GoArray.createDoubles;

/**
 * Implementation of a simple UCT / Monte-Carlo search algorithm. 
 */
public class UctRaveSearch
	implements Search<GoMove>, PropertyChangeListener
{
	private static final int MAX_DUPLICATE_RUNS = 1000;
	
	private static Logger _logger = Logger.getLogger(UctRaveSearch.class);
	
	private SearchProperties _searchProperties;
	
	protected MonteCarloAdministration _monteCarloAdministration;
	protected int _secondsPerMove;
	protected TreeNode<UctRaveSearchResult> _rootNode;
	protected int _nrPlayouts;
	protected int _nrSets;
	protected double _lastScore = 0.0;
	protected int _nrThreads;
	protected int _minimumNrNodes;
	protected double _explorationFactor = Math.sqrt(0.2);
	
	protected int _nrSimulationsBeforeExpansion = 45;
	protected boolean _useAMAF = true;
	
	private int _nrSimulatedMoves;
	
	private boolean _isTestVersion;
	
	public UctRaveSearch()
	{	
		initRoot();
		setSearchProperties(new SearchProperties());
	}
	
	public UctRaveSearch(MonteCarloAdministration administration)
	{
		this();
		setMonteCarloAdministration(administration);
	}
	
	public void setMonteCarloAdministration(MonteCarloAdministration administration)
	{
		_monteCarloAdministration = administration;
	}
	
	@SuppressWarnings("unchecked")
	protected void initRoot()
	{
		_rootNode = TreeNodeFactory.getSingleton().createTreeNode();
		UctRaveSearchResult rootResult = 
			(UctRaveSearchResult) UCTSearchResultFactory.createUctRaveSearchResult();
		rootResult.setExplorationFactor(_explorationFactor);
		_rootNode.setContent(rootResult);
		if (_rootNode.hashCode()!=Checksum.UNINITIALIZED && _rootNode.hashCode()!=_monteCarloAdministration.getPositionalChecksum())
			throw new IllegalStateException();
	}
	
	public TreeNode<UctRaveSearchResult> getRootNode()
	{
		return _rootNode;
	}
			
	/* (non-Javadoc)
	 * @see tesuji.games.general.search.Search#doSearch()
	 */
	public UctRaveSearchResult doSearch(byte startColor)
		throws Exception
	{
		long time0 = System.currentTimeMillis();

		assert _monteCarloAdministration.isConsistent() : "Inconsistent Monte-Carlo administration at the start of the search.";

		_nrPlayouts = 0;
		_nrSets = 0;

		Thread[] threads = new Thread[_nrThreads];
		Runnable[] searchProcesses = new Runnable[_nrThreads];
		for (int t=0; t<_nrThreads; t++)
		{
			searchProcesses[t] = new SearchProcess(startColor);
			threads[t] = new Thread(searchProcesses[t]);
			threads[t].start();
		}

		long time1 = System.currentTimeMillis();
		long time2;
		long timeLimit = calculateTimeLimit();
		boolean active = true;
		
		do
		{
			Thread.sleep(10);
			active = false;
			for (int t=0; t<_nrThreads; t++)
				if (threads[t].isAlive())
					active = true;
			time2 = System.currentTimeMillis();
		}
		while (active && (_nrPlayouts<_minimumNrNodes || (time2-time1)<timeLimit));
		
		// Tell all the threads to stop
		for (int t=0; t<_nrThreads; t++)
			((SearchProcess)searchProcesses[t]).stop();

		// Then wait for them to terminate.
		for (int t=0; t<_nrThreads; t++)
				threads[t].join();
				
		long time3 = System.currentTimeMillis();

		_logger.info("Nr playouts "+_nrPlayouts);
		_logger.info("Nr sets "+_nrSets);
		_logger.info("Nr root visits "+_rootNode.getContent().getNrPlayouts());
		_logger.info(""+((double)_nrPlayouts/(double)(time3-time0))+" kpos/sec");
		
		TreeNode<UctRaveSearchResult> bestNode = getBestChildNode(_rootNode);
		if (bestNode==null)
			return null;
		_lastScore = bestNode.getContent().getWinRatio();

		assert _monteCarloAdministration.isConsistent() : "Inconsistent Monte-Carlo administration at the end of the search.";

		return bestNode.getContent();
	}
	
	protected long calculateTimeLimit()
	{
		if (_secondsPerMove==0)
			return 0;
		
		int timeLimit = _secondsPerMove*1000;
		
		int boardSize = _monteCarloAdministration.getBoardSize();
		int delta = timeLimit / (boardSize*boardSize);
		timeLimit -= _monteCarloAdministration.getNrSimulatedMoves()*delta;
		if (timeLimit<100)
			timeLimit = 100;
		
		// Gradually speed up play when further ahead
		if (_lastScore>0.95)
			timeLimit /= 4;
		else if (_lastScore>0.9)
			timeLimit /= 3;
		else if (_lastScore>0.85)
			timeLimit /= 2;
		
		if (_monteCarloAdministration.getNrPasses()!=0)
			timeLimit /= 2;
	
		return timeLimit;
	}
	
	/**
	 * Decide which node in the tree should be 'expanded' next. By definition the node
	 * should be a leaf. And it must have had the required minimum number of playouts.
	 * 
	 * If the node passed is not a leaf-node, then it continues going down the tree
	 * based on the best child-node. 'Best' depends on the UCT value.
	 *  
	 * @param node
	 * @param moveColor
	 * @param searchAdministration
	 * @return
	 */
	protected TreeNode<UctRaveSearchResult> getNodeToExpand(TreeNode<UctRaveSearchResult> node, MonteCarloAdministration searchAdministration)
	{
		if (node.getChildCount()==0 || node.getContent().getNrPlayouts()<_nrSimulationsBeforeExpansion)
			return node;

		TreeNode<UctRaveSearchResult> bestNode = getBestUCTChildNode(node);

		searchAdministration.playMove(bestNode.getContent().getMove());
		
		if (searchAdministration.getNrPasses()<2)
			return getNodeToExpand(bestNode, searchAdministration);
		else
		{
			boolean win = (searchAdministration.getWinner()==BLACK);
			assert (bestNode.getParent()==node); // node should be parent of bestNode
			adjustTreeValue(bestNode, win);
			return null;
		}
	}
	
	/**
	 * Expand the tree with all possible moves. The set of moves is retrieved from the
	 * MonteCarloAdministration object. This might exclude some legal moves that the
	 * MonteCarloAdministration deems undesirable, like eye-filling moves.
	 * 
	 * @param node - the node to expand.
	 * @param searchAdministration -  the MonteCarloAdministration used during the search.
	 */
    protected void expandNode(TreeNode<UctRaveSearchResult> node, MonteCarloAdministration searchAdministration)
	{
		_nrSets++;
		byte colorToMove = searchAdministration.getColorToMove();
		GoMove[] moves = searchAdministration.getMoveSet(colorToMove);
		
		synchronized (node)
		{
			for (int i=0; i<moves.length; i++)
			{
				GoMove move = moves[i];
				@SuppressWarnings("unchecked")
				TreeNode<UctRaveSearchResult> nextNode = TreeNodeFactory.getSingleton().createTreeNode();
				@SuppressWarnings("unchecked")
				UctRaveSearchResult result = (UctRaveSearchResult)
					UCTSearchResultFactory.createUctRaveSearchResult(node.getContent().getLogNrPlayouts(),_explorationFactor);
				nextNode.setContent(result);
				result.setMove(move);
				node.add(nextNode);
			}
		}
	}
    
    /**
     * The result is passed up the tree.
     * 
     * @param node
     * @param win
     */
	protected void adjustTreeValue(TreeNode<UctRaveSearchResult> node, boolean win)
	{
		while (node!=null)
		{
			synchronized(node)
			{
				node.getContent().increasePlayouts(win);
			}
			node = node.getParent();
		}
	}
	
	protected TreeNode<UctRaveSearchResult> getBestChildNode(TreeNode<UctRaveSearchResult> node)
	{
		TreeNode<UctRaveSearchResult> bestNode = null;
		
		for (int i=0; i<node.getChildCount(); i++)
		{
			TreeNode<UctRaveSearchResult> nextNode = node.getChildAt(i);
			if (bestNode==null || nextNode.getContent().isBetterResultThan(bestNode.getContent()))
				bestNode = nextNode;
		}
		return bestNode;
	}
	
	protected TreeNode<UctRaveSearchResult> getBestUCTChildNode(TreeNode<UctRaveSearchResult> node)
	{
		TreeNode<UctRaveSearchResult> bestNode = null;
		
		synchronized(node)
		{
			for (int i=0; i<node.getChildCount(); i++)
			{
				TreeNode<UctRaveSearchResult> nextNode = node.getChildAt(i);
				if (bestNode==null || nextNode.getContent().isBetterVirtualResultThan(bestNode.getContent()))
					bestNode = nextNode;
			}
		}
		return bestNode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.search.Search#getBestMovePath(tesuji.core.util.ArrayStack)
	 */
	public void getBestMovePath(ArrayStack<GoMove> moveList)
	{
		getBestMovePath(_rootNode,moveList);
	}
	
	private void getBestMovePath(TreeNode<UctRaveSearchResult> startNode, ArrayStack<GoMove> moveList)
	{
		TreeNode<UctRaveSearchResult> node = startNode;

		while (node.getChildCount()!=0)
		{
			node = getBestChildNode(node);
			moveList.push(node.getContent().getMove());
		}
	}

	/* (non-Javadoc)
	 * @see tesuji.games.general.search.Search#playMove(java.lang.Object)
	 */
	public void playMove(GoMove move)
	{
		_monteCarloAdministration.playMove(move);

//		TreeNode<UCTSearchResult<MoveType>> newRoot = getMoveNode(move);
		
//		if (newRoot==null)
		{
			reset();
		}
//		else
//		{
//			newRoot.removeFromParent();
//			_rootNode.recycle();
//			_rootNode = newRoot;
//		}
	}
	
	/* (non-Javadoc)
	 * @see tesuji.games.general.search.Search#takeBack()
	 */
	public void takeBack()
	{
	}

	/* (non-Javadoc)
     * @see tesuji.games.general.search.Search#clear()
     */
    public void clear()
    {
		_monteCarloAdministration.clear();
		reset();
    }
    
    protected void reset()
    {
		_rootNode.recycle();
		initRoot();   
		_lastScore = 0.0;
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.general.search.Search#getSearchProperties()
     */
	public SearchProperties getSearchProperties()
	{
		return _searchProperties;
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.search.Search#setSearchProperties(tesuji.games.general.search.SearchProperties)
	 */
	public void setSearchProperties(SearchProperties properties)
	{
		if (_searchProperties!=null)
			_searchProperties.removePropertyChangeListener(this);
		
		_searchProperties = properties;
		_searchProperties.addPropertyChangeListener(this);
		parseSearchProperties();
	}

	public double getExplorationFactor()
	{
		return _explorationFactor;
	}
	
	public void setExplorationFactor(double explorationFactor)
	{
		_explorationFactor = explorationFactor;
	}
		
	public int getNrSimulationsBeforeExpansion()
	{
		return _nrSimulationsBeforeExpansion;
	}

	public void setNrSimulationsBeforeExpansion(int nrSimulationsBeforeExpansion)
	{
		_nrSimulationsBeforeExpansion = nrSimulationsBeforeExpansion;
	}

	public boolean getUseAMAF()
	{
		return _useAMAF;
	}

	public void setUseAMAF(boolean useAMAF)
	{
		_useAMAF = useAMAF;
	}

	/*
	 * (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		parseSearchProperties();
		if (_monteCarloAdministration!=null)
			_monteCarloAdministration.set(event.getPropertyName(),event.getNewValue().toString());
	}

	/* (non-Javadoc)
	 * @see tesuji.games.general.search.Search#setSearchProperties(tesuji.games.general.search.SearchProperties)
	 */
	public void parseSearchProperties()
	{
		setSecondsPerMove(_searchProperties.getIntProperty(SearchProperties.TIME_PER_MOVE));
		setNrProcessors(_searchProperties.getIntProperty(SearchProperties.NR_PROCESSORS));
		setMinimumNrNodes(_searchProperties.getIntProperty(SearchProperties.NR_NODES));
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.search.Search#setSecondsPerMove(int)
	 */
	public void setSecondsPerMove(int seconds)
	{
		_secondsPerMove = seconds;
		_searchProperties.setIntProperty(SearchProperties.TIME_PER_MOVE,_secondsPerMove);
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.search.Search#setNrProcessors(int)
	 */
	public void setNrProcessors(int nrProcessors)
	{
		_nrThreads = nrProcessors;
		if (_nrThreads<=0)
			_nrThreads = 1;
		_searchProperties.setIntProperty(SearchProperties.NR_PROCESSORS,_nrThreads);
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.search.Search#setMinimumNrNodes(int)
	 */
	public void setMinimumNrNodes(int nrNodes)
	{
		_minimumNrNodes = nrNodes;
		if (_minimumNrNodes<0)
			_minimumNrNodes = 0;
		_searchProperties.setIntProperty(SearchProperties.NR_NODES,_minimumNrNodes);
	}
	
	/**
	 * @return the number of moves played during simulation. This is only used to gather statistics.
	 */
	public int getNrSimulatedMoves()
	{
		return _nrSimulatedMoves;
	}
	

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.monte_carlo.MonteCarloAdministration#setIsTestVersion(boolean)
     */
	public void setIsTestVersion(boolean testVersion)
	{
		_isTestVersion = testVersion;
	}
	
	public boolean isTestVersion()
	{
		return _isTestVersion;
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return _monteCarloAdministration.getBoardModel().toString();
	}

	/**
	 *	This is an inner class that implements Runnable so we can start
	 *	as many SearchProcess instances as we have processors available.
	 */
    class SearchProcess
    	implements Runnable
    {
    	byte _startColor;
    	private boolean running;
		private MonteCarloAdministration _searchAdministration;
		private double[] _winMap;
    	
    	public SearchProcess(byte startColor)
    	{
    		_startColor = startColor;
    		running = false;
    		_searchAdministration = _monteCarloAdministration.createClone();
    		_winMap = createDoubles();
    	}
    	
    	public void run()
    	{
    		running = true;
    		int nrDuplicateRuns = 0;
    		do
    		{
    			int nrPlayouts = _nrPlayouts;
    			_searchAdministration.copyDataFrom(_monteCarloAdministration);
    			
    			_rootNode.getContent().setColorToMove(_startColor);
    			TreeNode<UctRaveSearchResult> node = getNodeToExpand(_rootNode, _searchAdministration);
    			TreeNode<UctRaveSearchResult> playoutNode = node;
   				
	    		if (node != null)
				{
	    			if (node.getChildCount()==0)
						expandNode(node,_searchAdministration);
					int start = _searchAdministration.getMoveStack().getSize();
					boolean blackWins = _searchAdministration.playout();
					_nrSimulatedMoves += _searchAdministration.getNrSimulatedMoves();
					_nrPlayouts++;
			    	adjustTreeValue(playoutNode, blackWins);

			    	if (_useAMAF)
			    	{
						IntStack playoutMoves = _searchAdministration.getMoveStack();
						int end = playoutMoves.getSize();
						double weight = 1.0;
						double weightDelta = 2.0 / (end - start + 1); // Michael Williams' idea to use decreasing weights
						GoArray.clear(_winMap);
						for (int i=start; i<end; i+=2)
						{
							int moveXY = playoutMoves.get(i);
							if (_winMap[moveXY]==0)
								_winMap[moveXY] = weight;
							//if (_useEnhanced)
								weight -= weightDelta;
						}
						
						while (playoutNode!=null)
						{
							byte color = playoutNode.getContent().getColorToMove();
							boolean playerWins = (blackWins && color==BLACK) || (!blackWins && color==WHITE);
							double score = playerWins ? UctRaveSearchResult.MAX_SCORE : UctRaveSearchResult.MIN_SCORE;
							for (int i=0; i<playoutNode.getChildCount(); i++)
							{
								TreeNode<UctRaveSearchResult> nextNode = playoutNode.getChildAt(i);
								UctRaveSearchResult result = nextNode.getContent();
								GoMove move = (GoMove) result.getMove();
								int xy = move.getXY();
								result.increaseVirtualPlayouts(_winMap[xy]*score,_winMap[xy]);
							}
							playoutNode = playoutNode.getParent();
							start--;
						}
			    	}
				}
   				
    			if (nrPlayouts==_nrPlayouts)
    				nrDuplicateRuns++;
    			else
    				nrDuplicateRuns = 0;
    			if (nrDuplicateRuns>=MAX_DUPLICATE_RUNS)
    			{
    				_logger.info("Stopping search.");
    				break;
    			}
    			if (_nrPlayouts>=_minimumNrNodes && _secondsPerMove==0)
    				break;
    		}
    		while (running);
    	}
    	
    	public void stop()
    	{
    		running = false;
    	}
    }
}
