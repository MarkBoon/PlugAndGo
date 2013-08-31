package tesuji.games.go.search;

import static tesuji.games.general.ColorConstant.BLACK;
import static tesuji.games.general.ColorConstant.WHITE;
import static tesuji.games.general.ColorConstant.opposite;
import static tesuji.games.go.util.GoArray.createBytes;
import static tesuji.games.go.util.GoArray.createDoubles;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;

import tesuji.core.util.ArrayStack;
import tesuji.games.general.GlobalParameters;
import tesuji.games.general.TreeNode;
import tesuji.games.general.search.Search;
import tesuji.games.general.search.SearchProperties;
import tesuji.games.general.search.SearchResult;
import tesuji.games.go.common.GoConstant;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.gui.MCBoardController;
import tesuji.games.go.gui.MCBoardDisplay;
import tesuji.games.go.joseki.JosekiManager;
import tesuji.games.go.joseki.MCBook;
import tesuji.games.go.joseki.MCJosekiEntry;
import tesuji.games.go.monte_carlo.MonteCarloAdministration;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.pattern.util.PatternUtil;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.IntStack;
import tesuji.games.go.util.PointSet;
import tesuji.games.util.Console;
import tesuji.games.util.Point;

public class MonteCarloHashMapSearch
	implements Search<GoMove>, PropertyChangeListener
{
	private static Logger _logger = Logger.getLogger(MonteCarloHashMapSearch.class);

	protected MonteCarloAdministration<GoMove> _monteCarloAdministration;
//	private HashMap<Integer, MonteCarloHashMapResult> _hashMap = new HashMap<Integer, MonteCarloHashMapResult>();
//	private ConcurrentHashMap<Integer, MonteCarloHashMapResult> _hashMap = new ConcurrentHashMap<Integer, MonteCarloHashMapResult>();
	private NonBlockingHashMapLong<MonteCarloHashMapResult> _hashMap = new NonBlockingHashMapLong<MonteCarloHashMapResult>(false);
	private MonteCarloHashMapResult _rootResult;
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
	
	@SuppressWarnings("unused")
    private static int _averagePlayouts;

	private SearchProperties _searchProperties;

	Point p = new Point();
	
	private MCBook _book;

	MCBoardController controller;
	MCBoardDisplay display;
	JFrame dataWindow;
	
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
		controller = new MCBoardController((MonteCarloPluginAdministration)administration, getHashMap());
		display = new MCBoardDisplay(controller);
		dataWindow = new JFrame();					
		java.awt.Point p = Console.getSingleton().getDataPanel().getLocation();
		Dimension d = Console.getSingleton().getDataPanel().getSize();
		dataWindow.setLocation(p.x+d.width+20, p.y);
		dataWindow.setSize(300, 300);
		dataWindow.getContentPane().add(display);

		_monteCarloAdministration = administration;
		initRoot();
	}
	
//	@Override
    public void propertyChange(PropertyChangeEvent event)
    {
		parseSearchProperties();
		if (_monteCarloAdministration!=null)
			_monteCarloAdministration.set(event.getPropertyName(),event.getNewValue().toString());
    }
    
    public MonteCarloPluginAdministration getAdministration()
    {
    	return (MonteCarloPluginAdministration) _monteCarloAdministration;
    }
    
    public NonBlockingHashMapLong<MonteCarloHashMapResult> getHashMap()
    {
    	return _hashMap;
    }
    
    public MCBook getBook()
    {
    	return _book;
    }
    public void setBook(MCBook book)
    {
    	_book = book;
    	_book.read();
    }
	
	private void initRoot()
	{
		if (_monteCarloAdministration!=null)
		{
			_nrSets = 0;
			int age = _monteCarloAdministration.getMoveStack().getSize();
			for (MonteCarloHashMapResult r : _hashMap.values())
			{
				if (r.getAge()<age)
				{
					_hashMap.remove(r.getChecksum());
					r.recycle();
				}
			}
			//_hashMap.clear();
			
			long checksum = _monteCarloAdministration.getPositionalChecksum();
			_rootResult = _hashMap.get(checksum);
			if (_rootResult==null)
			{
				_rootResult = SearchResultFactory.createMonteCarloHashMapResult();
				_rootResult.setPointSet((MonteCarloPluginAdministration)_monteCarloAdministration);
				_rootResult.setXY(GoConstant.PASS);
				_rootResult.setColor(opposite(_monteCarloAdministration.getColorToMove()));
				_rootResult.setAge(_monteCarloAdministration.getMoveStack().getSize());
				_rootResult.setChecksum(checksum);
	
				_hashMap.put(checksum,_rootResult);
			}
			
			if (_book!=null)
			{
				int mid = (_monteCarloAdministration.getBoardSize()+1)/2;
				Point p = new Point();
				for (int orientation=0; orientation<8; orientation++)
				{
					MCJosekiEntry entry = _book.get(_monteCarloAdministration.getPositionalChecksum(orientation));
					if (entry!=null)
					{
						for (int i=0; i<entry.xy.length; i++)
						{
							int x = GoArray.getX(entry.xy[i]) - mid;
							int y = GoArray.getY(entry.xy[i]) - mid;
							PatternUtil.adjustInversedOrientation(x, y, orientation, p);
							int entryXY = GoArray.toXY(p.x+mid, p.y+mid);
							//_rootResult.increasePlayouts(entryXY, entry.wins[i], entry.played[i]);
							_rootResult.increaseVirtualPlayouts(entryXY, entry.wins[i], entry.played[i]);
						}
						break;
					}
				}
			}
		}
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

//	@Override
    public void setSearchProperties(SearchProperties properties)
    {
		if (_searchProperties!=null)
			_searchProperties.removePropertyChangeListener(this);
		
		_searchProperties = properties;
		_searchProperties.addPropertyChangeListener(this);
		parseSearchProperties();
    }

//	@Override
    public SearchProperties getSearchProperties()
    {
	    return _searchProperties;
    }

//	@Override
    public void setSecondsPerMove(int seconds)
    {
	   _secondsPerMove = seconds;
		_searchProperties.setIntProperty(SearchProperties.TIME_PER_MOVE,_secondsPerMove);
    }

//	@Override
    public void setMinimumNrNodes(int minimum)
    {
		if (minimum>0 && minimum!=_minimumNrNodes)
		{
			_minimumNrNodes = minimum;
//			SearchResultFactory.ensureMonteCarloHashMapResultCapacity(_minimumNrNodes*2);
//			_hashMap = new ConcurrentHashMap<Integer, MonteCarloHashMapResult>(_minimumNrNodes*2);
			_hashMap = new NonBlockingHashMapLong<MonteCarloHashMapResult>(_minimumNrNodes*2, false);
//			_hashMap = new HashMap<Integer,MonteCarloHashMapResult>(_minimumNrNodes*2);
			_searchProperties.setIntProperty(SearchProperties.NR_NODES,minimum);
		}
    }

//	@Override
    public void setNrProcessors(int nrProcessors)
    {
		_nrThreads = nrProcessors;
		if (_nrThreads<=0)
			_nrThreads = 1;
		_searchProperties.setIntProperty(SearchProperties.NR_PROCESSORS,_nrThreads);
    }

    public void updateStats()
    {
		if (GlobalParameters.isTestVersion())
		{
			controller.init((MonteCarloPluginAdministration)_monteCarloAdministration, _hashMap);
			display.update();
			dataWindow.setVisible(true);
		}
    }

//	@Override
    public GoMove doSearch(byte startColor) throws Exception
    {
		long time0 = System.currentTimeMillis();

		assert _monteCarloAdministration.isConsistent() : "Inconsistent Monte-Carlo administration at the start of the search.";

//?		MCTacticsAdministration.reset();
		
		_monteCarloAdministration.setColorToMove(startColor);
		_nrPlayouts = 0;
		_nrSets = 0;
		
		_nrGeneratedMoves++;

		initRoot();

		updateStats();

		long time1 = System.currentTimeMillis();
		long time2;
		long timeLimit = calculateTimeLimit();
		if (isOptimizeNodeLimit())
			_nodeLimit = calculateNodeLimit();
		else
			_nodeLimit = _minimumNrNodes;
		
		// Create a thread for each available processor and start a search in it.
		Thread[] threads = new Thread[_nrThreads];
		Runnable[] searchProcesses = new Runnable[_nrThreads];
		for (int t=0; t<_nrThreads; t++)
		{
			searchProcesses[t] = new SearchProcess(startColor,(MonteCarloPluginAdministration)_monteCarloAdministration);
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
		_logger.info("Nr root visits "+_rootResult.getPlayouts());
		_logger.info(""+((double)_nrPlayouts/(double)(time3-time0))+" kpos/sec");
				
		updateStats();

/*		JFrame window = new JFrame();					
		MCBoardController controller = new MCBoardController(getAdministration(), getHashMap());
		MCBoardDisplay display = new MCBoardDisplay(controller);
		java.awt.Point p = Console.getSingleton().getDataPanel().getLocation();
		Dimension d = Console.getSingleton().getDataPanel().getSize();
		window.setLocation(p.x+d.width+20, p.y);
		window.setSize(300, 300);
		window.getContentPane().add(display);
		window.setVisible(true);
		while (window.isVisible())
			Thread.sleep(100);*/
		
//		System.out.println(startNode.toString());
		
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
		
		int xy = _rootResult.getBestMove();
		
		if (_book!=null)
		{
			recordJoseki();
		}
//		TreeNode<MonteCarloTreeSearchResult<MoveType>> secondBestNode = getSecondBestChildNode(_rootNode,bestNode);
//		_logger.info("Second-best: "+secondBestNode.getContent());
		if (xy==GoConstant.PASS)
			_logger.info("PASS!");
		else
		{
			_lastScore = _rootResult.getWinRatio(xy);
			if (_lastScore<0.05)
				return GoMoveFactory.getSingleton().createResignMove(_monteCarloAdministration.getColorToMove());
		}
		
		assert _monteCarloAdministration.isConsistent() : "Inconsistent Monte-Carlo administration at the end of the search.";

		return GoMoveFactory.getSingleton().createMove(xy, _monteCarloAdministration.getColorToMove());
    }

    private void recordJoseki()
    {
		int mid = (_monteCarloAdministration.getBoardSize()+1)/2;
		MCJosekiEntry entry = null;
		
		for (int orientation=0; orientation<8; orientation++)
		{
			entry = _book.load(_monteCarloAdministration.getPositionalChecksum(orientation));
			if (entry!=null)
			{
			    for (int i=0; i<entry.xy.length; i++)
			    {
			    	int entryXY = entry.xy[i];
			    	int x = GoArray.getX(entry.xy[i]) - mid;
			    	int y = GoArray.getY(entry.xy[i]) - mid;
			    	PatternUtil.adjustOrientation(x, y, orientation, p);
			    	entryXY = GoArray.toXY(p.x+mid, p.y+mid);
			    	entry.wins[i] +=_rootResult.getWins(entryXY);
			    	entry.played[i] +=_rootResult.getPlayouts(entryXY);
			    }
			    entry.setOccurrences(entry.getOccurrences()+1);
			    entry.setTimestamp(System.currentTimeMillis());
			    JosekiManager.getSingleton().updateJosekiEntry(entry);
			    _book.put(entry);
				break;
			}
		}

		if (entry==null && _monteCarloAdministration.getMoveStack().getSize()<MCBook.MAX_MOVE)
		{
			entry = _book.load(_rootResult.getChecksum());
			if (entry==null)
			{
				entry = new MCJosekiEntry();
				entry.setChecksum(_rootResult.getChecksum());
				entry.setTimestamp(System.currentTimeMillis());
				int nrPoints = _rootResult.getEmptyPoints().getSize();
				entry.xy = new int[nrPoints];
				entry.wins = new int[nrPoints];
				entry.played = new int[nrPoints];
				for (int i=0; i<nrPoints; i++)
				{
					int entryXY = _rootResult.getEmptyPoints().get(i);
					entry.xy[i] = entryXY;
				}
				for (int i=0; i<entry.xy.length; i++)
				{
					int entryXY = entry.xy[i];
					entry.wins[i] =_rootResult.getWins(entryXY);
					entry.played[i] =_rootResult.getPlayouts(entryXY);
				}
				entry.setOccurrences(1);
				_book.put(entry);
				JosekiManager.getSingleton().createJosekiEntry(entry);
			}
			else
			{
			    for (int i=0; i<entry.xy.length; i++)
			    {
			    	entry.wins[i] +=_rootResult.getWins(entry.xy[i]);
			    	entry.played[i] +=_rootResult.getPlayouts(entry.xy[i]);
			    }
			    entry.setOccurrences(entry.getOccurrences()+1);
			    entry.setTimestamp(System.currentTimeMillis());
			    JosekiManager.getSingleton().updateJosekiEntry(entry);
			    _book.put(entry);
			}
		}    	
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

//	@Override
    public void playMove(GoMove move)
    {
		_monteCarloAdministration.playMove(move);
		initRoot();   
    }

//	@Override
    public void takeBack()
    {
	    // TODO Auto-generated method stub
	    
    }

//	@Override
    public void clear()
    {
		_monteCarloAdministration.clear();    
		for (MonteCarloHashMapResult r : _hashMap.values())
				r.recycle();
		_hashMap.clear();
	}
    
//	@Override
    public void getBestMovePath(ArrayStack<GoMove> moveList)
    {
	    // TODO Auto-generated method stub
	    
    }

//	@Override
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
		private MonteCarloPluginAdministration _newAdministration;
		private MonteCarloPluginAdministration _initAdministration;
		private MonteCarloPluginAdministration _searchAdministration;
		private double[] _weightMap;
		private byte[] _colorMap;
		private IntStack _moveStack;
		private ArrayStack<MonteCarloHashMapResult> _resultStack;
    	
    	public SearchProcess(byte startColor, MonteCarloPluginAdministration admin)
    	{
    		_startColor = startColor;
    		running = false;
    		_initAdministration = (MonteCarloPluginAdministration)admin.createClone();
    		_searchAdministration = (MonteCarloPluginAdministration) admin.createClone();
    		_weightMap = createDoubles();
    		_colorMap = createBytes();
    		_moveStack = new IntStack(GoArray.LAST, null);
    		_resultStack = new ArrayStack<MonteCarloHashMapResult>();
    	}
    	
    	public void updateAdministration(MonteCarloPluginAdministration admin)
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
    		MonteCarloHashMapResult rootNode = _hashMap.get(_searchAdministration.getPositionalChecksum());
    		_resultStack.push(rootNode);
    	}
    	
    	public void run()
    	{
    		running = true;
    		do
    		{
    			reset();
    			
    			MonteCarloHashMapResult node = getNodeToExpand();
   				
				setNrPlayouts(getNrPlayouts() + 1);

				if (node != null)
				{
					int bestXY = node.getBestVirtualMove();
					_searchAdministration.playMove(bestXY);
					boolean blackWins = _searchAdministration.playout();
					node.increaseWins(bestXY, blackWins);
					setNrSimulatedMoves(getNrSimulatedMoves() + _searchAdministration.getNrSimulatedMoves());
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
							weight -= weightDelta;
							color = opposite(color);
						}
						
						for (int i=0; i<_resultStack.size(); i++)
						{
							MonteCarloHashMapResult playoutNode = _resultStack.peek(i);
					    	color = opposite(playoutNode.getColor());
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
						}
			    	}
				}
    		}
    		while (running && !reachedStopCondition());
    	}
    	
    	public void stop()
    	{
    		running = false;
    	}
    	
    	private boolean reachedStopCondition()
    	{
    		if (_nrPlayouts>=_nodeLimit && _secondsPerMove==0)
    			return true;
    		    		
    		return false;
    	}
    	
    	private MonteCarloHashMapResult getNodeToExpand()
    	{
    		MonteCarloHashMapResult node = _hashMap.get(_searchAdministration.getPositionalChecksum());
    		if (node==null)
    			throw new IllegalStateException();
    		while (_searchAdministration.getNrPasses()<2)
    		{
    			node.increasePlayouts();
	    		if (node.getPlayouts()<_nrSimulationsBeforeExpansion)
	    			return node;
	
	    		int xy = node.getBestVirtualMove();
	    		byte color = _searchAdministration.getColorToMove();
//	   			GoMove move = GoMoveFactory.getSingleton().createMove(xy, _searchAdministration.getColorToMove());
	    		_moveStack.push(xy);
	    		assert(xy==GoConstant.PASS || _searchAdministration.isLegal(xy));
	   			_searchAdministration.playExplorationMove(xy);
	
	   			long checksum = _searchAdministration.getPositionalChecksum();
	   			if (_searchAdministration.hasRepetition(checksum))
	   			{
	   				node.forbid(xy);
	   				_moveStack.pop();
	   				return null;
	   			}

	    		MonteCarloHashMapResult bestNode = _hashMap.get(checksum);
	   			if (bestNode==null)
	   			{
	   				bestNode = SearchResultFactory.createMonteCarloHashMapResult();
	   				bestNode.setXY(xy);
	   				bestNode.setColor(color);
	   				bestNode.setAge(_searchAdministration.getMoveStack().getSize());
	   				bestNode.setPointSet((MonteCarloPluginAdministration)_searchAdministration);
	   				bestNode.setChecksum(checksum);
	   				
	   				if (_book!=null)
	   				{
	   					int mid = (_searchAdministration.getBoardSize()+1) / 2;
	   					for (int orientation=0; orientation<8; orientation++)
	   					{
	   						MCJosekiEntry entry = _book.get(_searchAdministration.getPositionalChecksum(orientation));
	   						if (entry!=null)
	   						{
	   							for (int i=0; i<entry.xy.length; i++)
	   							{
	   								int x = GoArray.getX(entry.xy[i]) - mid;
	   								int y = GoArray.getY(entry.xy[i]) - mid;
	   								PatternUtil.adjustInversedOrientation(x, y, orientation, p);
	   								int entryXY = GoArray.toXY(p.x+mid, p.y+mid);
	   								//_rootResult.increasePlayouts(entryXY, entry.wins[i], entry.played[i]);
	   								_rootResult.increaseVirtualPlayouts(entryXY, entry.wins[i], entry.played[i]);
	   							}
	   							break;
	   						}
	   					}
	   				}
	   				
		    		_resultStack.push(bestNode);
		    		_hashMap.put(checksum, bestNode);
	   				return bestNode;
	   			}
	    		_resultStack.push(bestNode);
	   			node = bestNode;
    		}

   			boolean win = (_searchAdministration.getWinner()==BLACK);
   			adjustTreeValue(win);
   			return null;
   		}
    	
    	public void adjustTreeValue(boolean blackWins)
    	{
    		for (int i=0; i<_moveStack.getSize(); i++)
    		{
    			int xy = _moveStack.peek(i);
    			MonteCarloHashMapResult node = _resultStack.peek(i+1);
   				node.increaseWins(xy,blackWins);
    		}
    	}
    }
    

//	@Override
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


	public boolean isOptimizeNodeLimit()
	{
		return _optimizeNodeLimit;
	}

	public void setOptimizeNodeLimit(boolean optimizeNodeLimit)
	{
		_optimizeNodeLimit = optimizeNodeLimit;
	}
}
