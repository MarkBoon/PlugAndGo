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

import tesuji.core.util.ArrayList;
import tesuji.core.util.ArrayStack;

import tesuji.games.general.Checksum;
import tesuji.games.general.Move;
import tesuji.games.general.TreeNode;
import tesuji.games.general.TreeNodeFactory;
import tesuji.games.general.search.Search;
import tesuji.games.general.search.SearchProperties;

import tesuji.games.go.common.GoMove;
import tesuji.games.go.monte_carlo.MCState;
import tesuji.games.go.monte_carlo.MonteCarloAdministration;
import tesuji.games.go.util.GoGameProperties;
import tesuji.games.go.util.Statistics;

import static tesuji.games.general.ColorConstant.*;

/**
 * Implementation of a simple UCT / Monte-Carlo search algorithm. 
 */
public class UCTSearch
	implements Search<GoMove>, PropertyChangeListener
{
	private static final int MAX_DUPLICATE_RUNS = 1000;
	
	private static Logger _logger = Logger.getLogger(UCTSearch.class);
	
	private SearchProperties _searchProperties;
	
	protected MonteCarloAdministration _monteCarloAdministration;
	protected int _secondsPerMove;
	protected TreeNode<UCTSearchResult> _rootNode;
	protected int _nrPlayouts;
	protected int _nrRuns;
	protected int _nrSets;
	protected double _lastScore = 0.0;
	protected int _nrThreads;
	protected int _minimumNrNodes;
	protected double _explorationFactor = 1.0;
	
	private int nrSimulatedMoves;
	
	private boolean _isTestVersion;
	
	public UCTSearch()
	{	
		initRoot();
		setSearchProperties(new SearchProperties());
	}
	
	public UCTSearch(MonteCarloAdministration administration)
	{
		this();
		setMonteCarloAdministration(administration);
	}
	
	public void setMonteCarloAdministration(MonteCarloAdministration administration)
	{
		_monteCarloAdministration = administration;
	}
	
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
		_logger.info("Use UCT exploration-factor of "+_explorationFactor);
	}
	
	@SuppressWarnings("unchecked")
	protected void initRoot()
	{
		_rootNode = TreeNodeFactory.getSingleton().createTreeNode();
		UCTSearchResult rootResult = 
			(UCTSearchResult) UCTSearchResultFactory.createUCTSearchResult();
		rootResult.setExplorationFactor(_explorationFactor);
		_rootNode.setContent(rootResult);
		if (_rootNode.hashCode()!=Checksum.UNINITIALIZED && _rootNode.hashCode()!=_monteCarloAdministration.getPositionalChecksum())
			throw new IllegalStateException();
	}
	
	public TreeNode<UCTSearchResult> getRootNode()
	{
		return _rootNode;
	}
			
	/* (non-Javadoc)
	 * @see tesuji.games.general.search.Search#doSearch()
	 */
	public UCTSearchResult doSearch(byte startColor)
	{
		Statistics.nrPatternsPlayed = 0;
		
		long t0 = System.currentTimeMillis();

		assert _monteCarloAdministration.isConsistent();

		_nrPlayouts = 0;
		_nrRuns = 0;
		_nrSets = 0;

		Thread[] threads = new Thread[_nrThreads];
		Runnable[] runnables = new Runnable[_nrThreads]; // Beats me why I can't instantiate a UCTThread[]
		for (int t=0; t<_nrThreads; t++)
		{
			runnables[t] = new UCTThread(startColor);
			threads[t] = new Thread(runnables[t]);
			threads[t].start();
		}
		try
		{
			long t1 = System.currentTimeMillis();
			long t2;
			long timeLimit = calculateTimeLimit();
			boolean active = true;
			
			do
			{
				Thread.sleep(10);
				active = false;
				for (int t=0; t<_nrThreads; t++)
					if (threads[t].isAlive())
						active = true;
				t2 = System.currentTimeMillis();
			}
			while (active && (_nrPlayouts<_minimumNrNodes || (t2-t1)<timeLimit));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		// Tell all the threads to stop
		for (int t=0; t<_nrThreads; t++)
			((UCTThread)runnables[t]).stop();

		// Then wait for them to terminate.
		for (int t=0; t<_nrThreads; t++)
		{
			try
			{
				threads[t].join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
				
//		printNodeStatistics(_rootNode);

		_logger.info("Nr playouts "+_nrPlayouts);
		_logger.info("Nr runs "+_nrRuns);
		_logger.info("Nr sets "+_nrSets);
		_logger.info("Nr root visits "+_rootNode.getContent().getNrPlayouts());
		_logger.info("Nr pattern moves "+Statistics.nrPatternsPlayed);
		long t3 = System.currentTimeMillis();
		_logger.info(""+((double)_nrPlayouts/(double)(t3-t0))+" kpos/sec");
		
		TreeNode<UCTSearchResult> bestNode = getBestChildNode(_rootNode);
		if (bestNode==null)
			return null;
		_lastScore = bestNode.getContent().getWinRatio(startColor);
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
		
	protected TreeNode<UCTSearchResult> getNodeToExpand(TreeNode<UCTSearchResult> node, byte moveColor, MonteCarloAdministration searchAdministration)
	{
		if (!node.getContent().isCompleteNode())
			return node;

		TreeNode<UCTSearchResult> bestNode = getBestUCTChildNode(node);

		if (bestNode==null)
		{
			boolean win = (searchAdministration.getWinner()==BLACK);
			adjustTreeValue(node, win);
			return null;
		}
		
		searchAdministration.playMove(bestNode.getContent().getMove());
		
		if (searchAdministration.getNrPasses()<2)
			return getNodeToExpand(bestNode, opposite(moveColor), searchAdministration);
		else
		{
			boolean win = (searchAdministration.getWinner()==BLACK);
			assert (bestNode.getParent()==node); // node should be parent of bestNode
			adjustTreeValue(bestNode, win);
			return null;
		}
	}
	
	protected void expandNode(TreeNode<UCTSearchResult> node, byte moveColor, MonteCarloAdministration searchAdministration)
	{
		MCState searchState;
		GoMove nextMove;
		
		synchronized(node)
		{
			searchState = node.getContent().getSearchState();
			if (searchState==null)
			{
				searchState = searchAdministration.createState();
				node.getContent().setSearchState(searchState);
				_nrSets++;
			}

			nextMove = searchAdministration.selectExplorationMove(moveColor,searchState);
		
			if (nextMove.isInitialised())
				if (nextMove.isPass())
					node.getContent().setCompleteNode(true);
		}
		
		@SuppressWarnings("unchecked")
		TreeNode<UCTSearchResult> nextNode = TreeNodeFactory.getSingleton().createTreeNode();
		@SuppressWarnings("unchecked")
		UCTSearchResult result = 
			(UCTSearchResult) UCTSearchResultFactory.createUCTSearchResult(node.getContent().getLogNrPlayouts(),_explorationFactor);

		nextNode.setContent(result);
		nextNode.getContent().setColorToMove(opposite(nextMove.getColor()));
		result.setMove(nextMove);
		result.setParentState(searchState);
		
		searchAdministration.playMove(nextMove);
		// If a move repeats a position, we don't do anything.
		// It just has been removed as a candidate.
		if (searchAdministration.hasRepetition())
			return;
		nextNode.setChecksum(searchAdministration.getPositionalChecksum());

		if (_rootNode.hashCode()!=Checksum.UNINITIALIZED && _rootNode.hashCode()!=_monteCarloAdministration.getPositionalChecksum())
			throw new IllegalStateException();
		
		boolean win;
		if (searchAdministration.getNrPasses()==1)
		{
			// Only allow pass when winning and when game is almost finished.
			if (!searchAdministration.isGameAlmostFinished())
			{
				nextNode.recycle();
				return;
			}
			byte winner = searchAdministration.getWinner();
			if (winner!=nextMove.getColor())
			{
				nextNode.recycle();
				return;
			}
		}

		win = searchAdministration.playout();
		nrSimulatedMoves += searchAdministration.getNrSimulatedMoves();
		_nrPlayouts++;
		_nrRuns++;

		synchronized(node)
		{
			node.add(nextNode);
		}
		adjustTreeValue(nextNode,win);
	}
	
	protected void adjustTreeValue(TreeNode<UCTSearchResult> node, boolean win)
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
	
	protected TreeNode<UCTSearchResult> getBestChildNode(TreeNode<UCTSearchResult> node)
	{
		TreeNode<UCTSearchResult> bestNode = null;
		
		for (int i=0; i<node.getChildCount(); i++)
		{
			TreeNode<UCTSearchResult> nextNode = node.getChildAt(i);
			if (bestNode==null || nextNode.getContent().isBetterResultThan(bestNode.getContent()))
				bestNode = nextNode;
		}
		return bestNode;
	}
	
	protected TreeNode<UCTSearchResult> getBestUCTChildNode(TreeNode<UCTSearchResult> node)
	{
		TreeNode<UCTSearchResult> bestNode = null;
		
		synchronized(node)
		{
			for (int i=0; i<node.getChildCount(); i++)
			{
				TreeNode<UCTSearchResult> nextNode = node.getChildAt(i);
				if (bestNode==null || nextNode.getContent().isBetterVirtualResultThan(bestNode.getContent()))
					bestNode = nextNode;
			}
		}
		return bestNode;
	}
	
	public void getBestMovePath(ArrayStack<GoMove> moveList)
	{
		getBestMovePath(_rootNode,moveList);
	}
	
	private void getBestMovePath(TreeNode<UCTSearchResult> startNode, ArrayStack<GoMove> moveList)
	{
		TreeNode<UCTSearchResult> node = startNode;

		while (node.getChildCount()!=0)
		{
			node = getBestChildNode(node);
			moveList.push(node.getContent().getMove());
		}
	}

	/* (non-Javadoc)
	 * @see tesuji.games.general.search.Search#doSearch(tesuji.core.util.ArrayList)
	 */
	public UCTSearchResult doSearch(byte startColor, ArrayList<GoMove> alreadyTriedList)
	{
		// TODO Auto-generated method stub
		return null;
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
	
	private TreeNode<UCTSearchResult> getMoveNode(GoMove move)
	{
		for (int i=0; i<_rootNode.getChildCount(); i++)
		{
			TreeNode<UCTSearchResult> nextNode = _rootNode.getChildAt(i);
			if (nextNode.getContent().getMove().equals(move))
				return nextNode;
		}
		return null; // Should never come here.
	}
	
	/* (non-Javadoc)
	 * @see tesuji.games.general.search.Search#takeBack()
	 */
	public void takeBack()
	{
		// TODO _mcCurrentAdministration.takeBack();
	}

//    protected void printNodeStatistics(TreeNode<UCTSearchResult<MoveType>> rootNode)
//	{
//    	@SuppressWarnings("unchecked")
//		UCTSearchResult<MoveType>[] resultArray = new UCTSearchResult[GoArray.MAX];
//		for (TreeNode<UCTSearchResult<MoveType>> childNode : rootNode.getChildren())
//		{
//			int xy = ((GoMove)childNode.getContent().getMove()).getXY();
//			resultArray[xy] = childNode.getContent();
//		}
//		int boardSize = _currentAdministration.getBoardSize();
//		_logger.info("---");
//		_logger.info("\n"+_currentAdministration.toString());
//		
//		for (int row=1; row<=boardSize; row++)
//		{
//			StringBuilder line = new StringBuilder();
//			for (int col=1; col<=boardSize; col++)
//			{
//				int xy = GoArray.toXY(col,row);
//				UCTSearchResult<MoveType> result = resultArray[xy];
//				if (result==null)
//				{
//					if (_currentAdministration.getBoardModel().get(xy)==BLACK)
//						line.append("XXX ");
//					else if (_currentAdministration.getBoardModel().get(xy)==WHITE)
//						line.append("OOO ");
//					else
//						line.append("    ");
//				}
//				else
//					line.append(String.format("%3d ", result._nrWins));
//			}
//			_logger.info(line);
//			line.setLength(0);
//			for (int col=1; col<=boardSize; col++)
//			{
//				int xy = GoArray.toXY(col,row);
//				UCTSearchResult<MoveType> result = resultArray[xy];
//				if (result==null)
//				{
//					if (_currentAdministration.getBoardModel().get(xy)==BLACK)
//						line.append("XXX ");
//					else if (_currentAdministration.getBoardModel().get(xy)==WHITE)
//						line.append("OOO ");
//					else
//						line.append("    ");
//				}
//				else
//					line.append(String.format("%3d ", result._nrPlayouts));
//			}
//			_logger.info(line);
//			_logger.info(" ");
//		}
//		_logger.info("---");
//	}
//	
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
    

	public SearchProperties getSearchProperties()
	{
		return _searchProperties;
	}

	public void propertyChange(PropertyChangeEvent event)
	{
		parseSearchProperties();
		if (event.getPropertyName().equals(GoGameProperties.BOARDSIZE))
			_monteCarloAdministration.setBoardSize(_searchProperties.getIntProperty(GoGameProperties.BOARDSIZE));
		if (event.getPropertyName().equals(GoGameProperties.KOMI))
			_monteCarloAdministration.setKomi(_searchProperties.getDoubleProperty(GoGameProperties.KOMI));
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
	
	public void setSecondsPerMove(int seconds)
	{
		_secondsPerMove = seconds;
		_searchProperties.setIntProperty(SearchProperties.TIME_PER_MOVE,_secondsPerMove);
	}
	
	public void setNrProcessors(int nrProcessors)
	{
		_nrThreads = nrProcessors;
		if (_nrThreads<=0)
			_nrThreads = 1;
		_searchProperties.setIntProperty(SearchProperties.NR_PROCESSORS,_nrThreads);
	}
	
	public int getNrProcessors()
	{
		return _nrThreads;
	}
	
	public void setMinimumNrNodes(int nrNodes)
	{
		_minimumNrNodes = nrNodes;
		if (_minimumNrNodes<0)
			_minimumNrNodes = 0;
		_searchProperties.setIntProperty(SearchProperties.NR_NODES,_minimumNrNodes);
	}
	
	public int getNrSimulatedMoves()
	{
		return nrSimulatedMoves;
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

	@Override
	public String toString()
	{
		return _monteCarloAdministration.getBoardModel().toString();
	}

    class UCTThread
    	implements Runnable
    {
    	byte _startColor;
    	private boolean running;
		private MonteCarloAdministration _searchAdministration;
    	
    	public UCTThread(byte startColor)
    	{
    		_startColor = startColor;
    		running = false;
    		_searchAdministration = _monteCarloAdministration.createClone();
    	}
    	
    	public void run()
    	{
    		running = true;
    		int nrDuplicateRuns = 0;
    		do
    		{
    			int nrRuns = _nrRuns;
    			_searchAdministration.copyDataFrom(_monteCarloAdministration);
    			
    			_rootNode.getContent().setColorToMove(_startColor);
    			TreeNode<UCTSearchResult> node;
   				node = getNodeToExpand(_rootNode, _startColor, _searchAdministration);
   				
    			if (node!=null)
    				expandNode(node, node.getContent().getColorToMove(), _searchAdministration);
    			
    			if (nrRuns==_nrRuns)
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
