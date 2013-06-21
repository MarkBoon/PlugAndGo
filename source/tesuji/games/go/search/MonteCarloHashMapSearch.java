package tesuji.games.go.search;

import static tesuji.games.general.ColorConstant.BLACK;
import static tesuji.games.general.ColorConstant.WHITE;
import static tesuji.games.general.ColorConstant.opposite;
import static tesuji.games.go.util.GoArray.createBytes;
import static tesuji.games.go.util.GoArray.createDoubles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;

import tesuji.core.util.ArrayStack;
import tesuji.games.general.TreeNode;
import tesuji.games.general.search.Search;
import tesuji.games.general.search.SearchProperties;
import tesuji.games.general.search.SearchResult;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;
//import tesuji.games.go.monte_carlo.MCTacticsAdministration;
import tesuji.games.go.monte_carlo.MonteCarloAdministration;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.IntStack;
import tesuji.games.go.util.PointSet;

public class MonteCarloHashMapSearch
	implements Search<GoMove>, PropertyChangeListener
{
	private static Logger _logger = Logger.getLogger(MonteCarloHashMapSearch.class);

	protected MonteCarloAdministration<GoMove> _monteCarloAdministration;
	private NonBlockingHashMapLong<MonteCarloHashMapResult> _hashMap = new NonBlockingHashMapLong<MonteCarloHashMapResult>();
	private int _nrPlayouts;
	private int _nrSimulationsBeforeExpansion = 1;
	private int _nrSimulatedMoves = 0;
	private boolean _useAMAF = true;

	protected int _secondsPerMove;
	protected int _nrSets;
	protected double _lastScore = 0.0;
	protected int _nrThreads = 1;
	protected int _minimumNrNodes;
	protected int _nodeLimit;
	protected boolean _optimizeNodeLimit = false;
	protected double _explorationFactor = Math.sqrt(0.2);
	
	private static long _nrGeneratedMoves;
	private static long _totalNrPlayouts;
	private static int _averagePlayouts;

	private SearchProperties _searchProperties;

	public MonteCarloHashMapSearch()
	{	
		initRoot();
		setSearchProperties(new SearchProperties());
	}
	
	public MonteCarloHashMapSearch(MonteCarloAdministration<GoMove> administration)
	{
		this();
		setMonteCarloAdministration(administration);
	}
	
	public void setMonteCarloAdministration(MonteCarloAdministration<GoMove> administration)
	{
		_monteCarloAdministration = administration;
		initRoot();
	}

	@Override
    public void propertyChange(PropertyChangeEvent event)
    {
		parseSearchProperties();
		if (_monteCarloAdministration!=null)
			_monteCarloAdministration.set(event.getPropertyName(),event.getNewValue().toString());
    }
	
	private void initRoot()
	{
		_nrSets = 0;
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

	@Override
    public void setSearchProperties(SearchProperties properties)
    {
		if (_searchProperties!=null)
			_searchProperties.removePropertyChangeListener(this);
		
		_searchProperties = properties;
		_searchProperties.addPropertyChangeListener(this);
		parseSearchProperties();
    }

	@Override
    public SearchProperties getSearchProperties()
    {
	    return _searchProperties;
    }

	@Override
    public void setSecondsPerMove(int seconds)
    {
	   _secondsPerMove = seconds;
		_searchProperties.setIntProperty(SearchProperties.TIME_PER_MOVE,_secondsPerMove);
    }

	@Override
    public void setMinimumNrNodes(int minimum)
    {
		_minimumNrNodes = minimum;
		_searchProperties.setIntProperty(SearchProperties.NR_NODES,minimum);
    }

	@Override
    public void setNrProcessors(int nrProcessors)
    {
		_nrThreads = nrProcessors;
		if (_nrThreads<=0)
			_nrThreads = 1;
		_searchProperties.setIntProperty(SearchProperties.NR_PROCESSORS,_nrThreads);
    }

	@Override
    public void setIsTestVersion(boolean testVersion)
    {
	    // TODO Auto-generated method stub	    
    }

	@Override
    public GoMove doSearch(byte startColor) throws Exception
    {
		long time0 = System.currentTimeMillis();

		assert _monteCarloAdministration.isConsistent() : "Inconsistent Monte-Carlo administration at the start of the search.";

//?		MCTacticsAdministration.reset();
		
		_monteCarloAdministration.setColorToMove(startColor);
		_nrPlayouts = 0;
		_nrSets = 0;
		
		_nrGeneratedMoves++;

		MonteCarloHashMapResult startNode = _hashMap.get(_monteCarloAdministration.getPositionalChecksum());
		if (startNode==null)
		{
			startNode = new MonteCarloHashMapResult();
			startNode.setPointSet(_monteCarloAdministration.getEmptyPoints(),(MonteCarloPluginAdministration)_monteCarloAdministration);
			startNode.setMove(GoMoveFactory.getSingleton().createPassMove(opposite(_monteCarloAdministration.getColorToMove())));
			_hashMap.put(_monteCarloAdministration.getPositionalChecksum(),startNode);
		}

		long time1 = System.currentTimeMillis();
		long time2;
		long timeLimit = calculateTimeLimit();
//		if (isOptimizeNodeLimit())
//			_nodeLimit = calculateNodeLimit();
//		else
			_nodeLimit = _minimumNrNodes;
		
		// Create a thread for each available processor and start a search in it.
		Thread[] threads = new Thread[_nrThreads];
		Runnable[] searchProcesses = new Runnable[_nrThreads];
		for (int t=0; t<_nrThreads; t++)
		{
			searchProcesses[t] = new SearchProcess(startColor,_monteCarloAdministration);
			threads[t] = new Thread(searchProcesses[t]);
			threads[t].start();
		}

		boolean active = true;

		// Now loop for the required amount of time or until
		// the required number of playouts have been performed.
		// It also stops when no processor remains active, although
		// a search-thread stopping by itself is most likely a bug.
		do
		{
			Thread.sleep(10);
			active = false;
			for (int t=0; t<_nrThreads; t++)
				if (threads[t].isAlive())
					active = true;
			time2 = System.currentTimeMillis();
		}
		while (active && (_nrPlayouts<_nodeLimit || (time2-time1)<timeLimit));
		
		// Tell all the threads to stop
		for (int t=0; t<_nrThreads; t++)
			((SearchProcess)searchProcesses[t]).stop();

		// Then wait for them to terminate.
		for (int t=0; t<_nrThreads; t++)
				threads[t].join();
				
		_totalNrPlayouts += _nrPlayouts;
		_averagePlayouts = (int) (_totalNrPlayouts/_nrGeneratedMoves);
//		long saved = 0;
//		if (_minimumNrNodes!=0)
//			saved = ((_minimumNrNodes*_nrGeneratedMoves - _totalNrPlayouts) * 100) / (_minimumNrNodes*_nrGeneratedMoves);
		long time3 = System.currentTimeMillis();

		_logger.info("Playout limit "+_nodeLimit + " - last score " + _lastScore);
		_logger.info("Nr playouts "+_nrPlayouts);
		_logger.info("Nr sets "+_nrSets);
		_logger.info("Nr root visits "+startNode.getPlayouts());
		_logger.info(""+((double)_nrPlayouts/(double)(time3-time0))+" kpos/sec");
		
		System.out.println(startNode.toString());
		
//		if (isOptimizeNodeLimit())
//		{
//			_logger.info("Percentage saved "+saved+"%");
//			_logger.info("Average nr playouts "+_averagePlayouts);
//		}
//		_logger.info("Nr pattern moves per playout "+(Statistics.nrPatternsPlayed/_totalNrPlayouts));
//		_logger.info("Nr hard-coded patterns generated: "+MCTacticsAdministration.getNrPatternsGenerated());
//		_logger.info("Nr hard-coded patterns played: "+MCTacticsAdministration.getNrPatternsUsed());
		
		assert _monteCarloAdministration.isConsistent() : "Inconsistent Monte-Carlo administration at the end of the search.";
//		fillOwnershipArray();
		
		int xy = startNode.getBestMove();
//		TreeNode<MonteCarloTreeSearchResult<MoveType>> secondBestNode = getSecondBestChildNode(_rootNode,bestNode);
//		_logger.info("Second-best: "+secondBestNode.getContent());
		if (xy==GoConstant.PASS)
			_logger.info("PASS!");
		_lastScore = startNode.getWinRatio(xy);
//		if (_lastScore<0.05)
//			return GoMoveFactory.getSingleton().createResignMove(_monteCarloAdministration.getColorToMove());
		
		assert _monteCarloAdministration.isConsistent() : "Inconsistent Monte-Carlo administration at the end of the search.";

		return GoMoveFactory.getSingleton().createMove(xy, _monteCarloAdministration.getColorToMove());
    }

	protected long calculateTimeLimit()
	{
		if (_secondsPerMove==0)
			return 0;
		
		int timeLimit = _secondsPerMove*1000;
		
		int boardSize = _monteCarloAdministration.getBoardSize();
		int delta = timeLimit / (boardSize*boardSize);
		timeLimit -= _monteCarloAdministration.getMoveStack().getSize()*delta;
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
	
	protected int calculateNodeLimit()
	{
		if (_minimumNrNodes==0)
			return 0;
		
		int nodeLimit = _minimumNrNodes;
		
		if (_minimumNrNodes>=2048)
			nodeLimit = _minimumNrNodes * 4 / 3;
		if (_minimumNrNodes>=16384)
			nodeLimit = _minimumNrNodes * 3 / 2;
//		if (_minimumNrNodes>=65536)
//			_nodeLimit = _minimumNrNodes * 2;

		// Gradually speed up play when further ahead
		if (_lastScore>0.95)
			nodeLimit /= 8;
		else if (_lastScore>0.9)
			nodeLimit /= 4;
		else if (_lastScore>0.85)
			nodeLimit /= 2;
		
		if (_monteCarloAdministration.getNrPasses()!=0)
			nodeLimit /= 2;
		
		return nodeLimit;
	}

	@Override
    public void playMove(GoMove move)
    {
		_monteCarloAdministration.playMove(move);
    }

	@Override
    public void takeBack()
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void clear()
    {
		_monteCarloAdministration.clear();    
	}

	@Override
    public void getBestMovePath(ArrayStack<GoMove> moveList)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public TreeNode<? extends SearchResult<GoMove>> getRootNode()
    {
	    // TODO Auto-generated method stub
	    return null;
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
		private MonteCarloAdministration<GoMove> _newAdministration;
		private MonteCarloAdministration<GoMove> _initAdministration;
		private MonteCarloAdministration<GoMove> _searchAdministration;
		private double[] _weightMap;
		private byte[] _colorMap;
		private IntStack _moveStack;
		private ArrayStack<MonteCarloHashMapResult> _resultStack;
    	
    	public SearchProcess(byte startColor, MonteCarloAdministration<GoMove> admin)
    	{
    		_startColor = startColor;
    		running = false;
    		_initAdministration = (MonteCarloAdministration<GoMove>) admin.createClone();
    		_searchAdministration = (MonteCarloAdministration<GoMove>) admin.createClone();
    		_weightMap = createDoubles();
    		_colorMap = createBytes();
    		_moveStack = new IntStack(GoArray.LAST, null);
    		_resultStack = new ArrayStack<MonteCarloHashMapResult>();
    	}
    	
    	public void updateAdministration(MonteCarloAdministration<GoMove> admin)
    	{
    		_newAdministration = admin;
    	}
    	
    	private void reset()
    	{
    		if (_newAdministration!=null)
    		{
    			_initAdministration.copyDataFrom(_newAdministration);
    			_newAdministration = null;
    		}
    		_searchAdministration.copyDataFrom(_initAdministration);
    		_moveStack.clear();
    		_resultStack.clear();
    	}
    	
    	public void run()
    	{
    		running = true;
    		do
    		{
    			reset();
    			
    			MonteCarloHashMapResult node = getNodeToExpand();
    			MonteCarloHashMapResult playoutNode = node;
   				
	    		if (node != null)
				{
					boolean blackWins = _searchAdministration.playout();
					setNrSimulatedMoves(getNrSimulatedMoves() + _searchAdministration.getNrSimulatedMoves());
					setNrPlayouts(getNrPlayouts() + 1);
			    	adjustTreeValue(blackWins);

			    	if (_useAMAF)
			    	{
						IntStack playoutMoves = _searchAdministration.getMoveStack();
						byte color = _initAdministration.getColorToMove();
						int start = _initAdministration.getMoveStack().getSize();
						int end = playoutMoves.getSize();
						double weight = 1.0;
						double weightDelta = 1.0 / (end - start + 1); // Michael Williams' idea to use decreasing weights
						GoArray.clear(_weightMap);
						GoArray.clear(_colorMap);
						for (int i=start; i<end; i++)
						{
							int moveXY = playoutMoves.get(i);
							if (_colorMap[moveXY]==0)
							{
								_colorMap[moveXY] = color;
								_weightMap[moveXY] = weight;
							}
							//if (_useEnhanced)
								weight -= weightDelta;
							color = opposite(color);
						}
						
						for (int i=0; i<_resultStack.size(); i++)
						{
					    	color = opposite(playoutNode.getMove().getColor());
							boolean playerWins = (blackWins && color==BLACK) || (!blackWins && color==WHITE);
							double score = playerWins ? MonteCarloTreeSearchResult.MAX_SCORE : MonteCarloTreeSearchResult.MIN_SCORE;
							PointSet points = playoutNode.getEmptyPoints();
							for (int p=0; p<points.getSize(); p++)
							{
								int xy = points.get(p);
								double weightXY = _weightMap[xy];
								if (_colorMap[xy]==color)
									playoutNode.increaseVirtualPlayouts(xy,weightXY*score,weightXY);
							}
							playoutNode = _resultStack.peek(i);
						}
			    	}
				}
    		}
    		while (running);
    	}
    	
    	public void stop()
    	{
    		running = false;
    	}
    	
    	private MonteCarloHashMapResult getNodeToExpand()
    	{
    		MonteCarloHashMapResult node = _hashMap.get(_searchAdministration.getPositionalChecksum());
    		while (_searchAdministration.getNrPasses()<2)
    		{
	    		int xy = node.getBestVirtualMove();
	   			GoMove move = GoMoveFactory.getSingleton().createMove(xy, _searchAdministration.getColorToMove());
	   			node.increasePlayouts(xy);
	    		_resultStack.push(node);
	    		_moveStack.push(xy);
	   			_searchAdministration.playExplorationMove(move);
	    		MonteCarloHashMapResult nextNode = _hashMap.get(_searchAdministration.getPositionalChecksum());
	    		if (nextNode==null)
	    		{
	    			if (node.getPlayouts()<_nrSimulationsBeforeExpansion)
	    			{
	        			return node;
	    			}
	    			else
	    			{
	    				nextNode = SearchResultFactory.createMonteCarloHashMapResult();
	    				nextNode.setMove(move);
	    				nextNode.setPointSet(_searchAdministration.getEmptyPoints(),(MonteCarloPluginAdministration)_searchAdministration);
	    				// Fill virtual playouts of all children with pattern-matcher.
	    				GoMove nextMove = _searchAdministration.selectSimulationMove(); // temporary
	    				_resultStack.push(nextNode);
	    				_moveStack.push(nextMove.getXY());
	    	   			_searchAdministration.playExplorationMove(nextMove);
	    				return nextNode;
	    			}
	    		}
	    		node = nextNode;
    		}
    		return node;
    	}
    	
    	public void adjustTreeValue(boolean blackWins)
    	{
    		for (int i=0; i<_resultStack.size(); i++)
    		{
    			int xy = _moveStack.peek(i);
    			MonteCarloHashMapResult node = _resultStack.peek(i);    			
    			assert(node!=null);
    			node.increaseWins(xy,blackWins);
    		}
    	}
    }

	@Override
    public boolean isGameFinished()
    {
		return false;
    }

	public int getNrSimulatedMoves()
    {
	    return _nrSimulatedMoves;
    }

	public void setNrSimulatedMoves(int nrSimulatedMoves)
    {
	    _nrSimulatedMoves = nrSimulatedMoves;
    }

	public int getNrPlayouts()
    {
	    return _nrPlayouts;
    }

	public void setNrPlayouts(int nrPlayouts)
    {
	    _nrPlayouts = nrPlayouts;
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

}
