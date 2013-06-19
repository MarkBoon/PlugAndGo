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

package tesuji.games.go.search;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import org.apache.log4j.Logger;

import tesuji.core.util.ArrayStack;

import tesuji.games.general.Checksum;
import tesuji.games.general.Move;
import tesuji.games.general.MoveIterator;
import tesuji.games.general.TreeNode;
import tesuji.games.general.TreeNodeFactory;
import tesuji.games.general.provider.DataProviderList;
import tesuji.games.general.search.Search;
import tesuji.games.general.search.SearchProperties;

import tesuji.games.go.monte_carlo.MCPatternsAdministration;
import tesuji.games.go.monte_carlo.MonteCarloAdministration;
import tesuji.games.go.pattern.common.HibernatePatternManager;
import tesuji.games.go.pattern.common.PatternGroup;
import tesuji.games.go.pattern.common.PatternManager;
import tesuji.games.go.pattern.incremental.IncrementalPatternMatcher;
import tesuji.games.go.pattern.incremental.PatternMatch;
import tesuji.games.go.pattern.incremental.PatternMatchList;

import tesuji.games.go.common.GoMove;
import tesuji.games.go.util.DefaultDoubleArrayProvider;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.IntStack;
import tesuji.games.go.util.Statistics;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.util.GoArray.createDoubles;
import static tesuji.games.go.util.GoArray.createBytes;

/**
 * Implementation of a simple UCT / Monte-Carlo search algorithm. 
 */
public class MonteCarloTreeSearchWithPatterns<MoveType extends Move>
	implements Search<MoveType>, PropertyChangeListener
{
	private static final int MAX_DUPLICATE_RUNS = 1000;

	public static boolean LIMIT_URGENT_SEARCH = 	false;
	
	private static Logger _logger = Logger.getLogger(MonteCarloTreeSearch.class);
	
	private SearchProperties _searchProperties;
	
	protected MonteCarloAdministration<MoveType> _monteCarloAdministration;
	protected IncrementalPatternMatcher _patternMatcher;
	protected PatternManager _patternManager;
	protected int[] _patternSuccess;
	protected int[] _patternOccurrence;
	
	protected int _secondsPerMove;
	protected TreeNode<MonteCarloTreeSearchResult<MoveType>> _rootNode;
	protected int _nrPlayouts;
	protected int _nrSets;
	protected double _lastScore = 0.0;
	protected int _nrThreads;
	protected int _minimumNrNodes;
	protected int _nodeLimit;
	protected double _explorationFactor = Math.sqrt(0.2);
	
	protected int _nrSimulationsBeforeExpansion = 1;
	protected boolean _useAMAF = false;
	protected boolean _isTestVersion = false;
	
	private int _nrSimulatedMoves;
	
	private static long _nrGeneratedMoves;
	private static long _totalNrPlayouts;
	private static int _averagePlayouts;

	private double[] _ownershipArray;
	
	private boolean _isInitialized = false;
	
	public MonteCarloTreeSearchWithPatterns()
	{	
		_ownershipArray = GoArray.createDoubles();
		initRoot();
		setSearchProperties(new SearchProperties());
		_patternManager = HibernatePatternManager.getSingleton();
		_patternSuccess = GoArray.createIntegers();
		_patternOccurrence = GoArray.createIntegers();
	}
	
	public MonteCarloTreeSearchWithPatterns(MonteCarloAdministration<MoveType> administration)
	{
		this();
		setMonteCarloAdministration(administration);
		administration.setIsTestVersion(_isTestVersion);
	}
	
	public void setMonteCarloAdministration(MonteCarloAdministration<MoveType> administration)
	{
		_monteCarloAdministration = administration;
//		_patternMatcher = ((MCPatternsAdministration)administration).getPatternMatcher();
		initRoot();
		
		GoArray.clear(_ownershipArray);
	}
	
	public void setPatternManager(PatternManager patternManager)
	{
		_patternManager = patternManager;
		cleanupPatterns();
		_patternMatcher = new IncrementalPatternMatcher(_patternManager.getDefaultPatternGroup());
//		System.out.println(group.getPatternList().get(0).toString());
	}
	
	public void cleanupPatterns()
	{
//		if (_patternMatcher!=null)
//		{
//			PatternGroup group = _patternMatcher.getPatternGroup();
//			group.purgeMutilations(_patternManager);
//			group.purgeMeaningless(_patternManager);
//			if (group.getPatternList().size()>100000)
//				group.cleanup(_patternManager);
//			System.err.println("MC uses "+ group.getPatternList() + " patterns.");
//		}		
	}
	
	@SuppressWarnings("unchecked")
	protected void initRoot()
	{
		_rootNode = (TreeNode<MonteCarloTreeSearchResult<MoveType>>) TreeNodeFactory.getSingleton().createTreeNode();
		MonteCarloTreeSearchResult<MoveType> rootResult = 
			(MonteCarloTreeSearchResult<MoveType>) SearchResultFactory.createMonteCarloTreeSearchResult();
		rootResult.setExplorationFactor(_explorationFactor);
		if (_monteCarloAdministration!=null)
			rootResult.setMove(_monteCarloAdministration.getMoveFactory().createDummyMove(opposite(_monteCarloAdministration.getColorToMove())));
		_rootNode.setContent(rootResult);
		_rootNode.getContent().setIsTestVersion(getIsTestVersion());
		if (_rootNode.hashCode()!=Checksum.UNINITIALIZED && _rootNode.hashCode()!=_monteCarloAdministration.getPositionalChecksum())
			throw new IllegalStateException();
	}
	
	public TreeNode<MonteCarloTreeSearchResult<MoveType>> getRootNode()
	{
		return _rootNode;
	}
			
	/* (non-Javadoc)
	 * @see tesuji.games.general.search.Search#doSearch()
	 */
	@SuppressWarnings("unchecked")
    public MoveType doSearch(byte startColor)
		throws Exception
	{		
		if (!_isInitialized)
		{
			_patternMatcher.initialise(_monteCarloAdministration.getBoardModel());
			_monteCarloAdministration.getBoardModel().addBoardModelListener(_patternMatcher);
			_isInitialized = true;
		}

		_logger.info("Start search with position:\n"+_monteCarloAdministration);
		long time0 = System.currentTimeMillis();

		assert _monteCarloAdministration.isConsistent() : "Inconsistent Monte-Carlo administration at the start of the search.";

		GoArray.clear(_ownershipArray);
		_monteCarloAdministration.setIsTestVersion(getIsTestVersion());
		_monteCarloAdministration.setColorToMove(startColor);
		_nrPlayouts = 0;
		_nrSets = 0;
		
		_nrGeneratedMoves++;

		long time1 = System.currentTimeMillis();
		long time2;
		long timeLimit = calculateTimeLimit();
		_nodeLimit = calculateNodeLimit();
		
		// Create a thread for each available processor and start a search in it.
		Thread[] threads = new Thread[_nrThreads];
		Runnable[] searchProcesses = new Runnable[_nrThreads];
		for (int t=0; t<_nrThreads; t++)
		{
			searchProcesses[t] = new SearchProcess(startColor);
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
		long saved = 0;
		if (_minimumNrNodes!=0)
			saved = ((_minimumNrNodes*_nrGeneratedMoves - _totalNrPlayouts) * 100) / (_minimumNrNodes*_nrGeneratedMoves);
		long time3 = System.currentTimeMillis();

		if (getIsTestVersion())
			_logger.info("TEST VERSION");
		_logger.info("Playout limit "+_nodeLimit + " - last score " + _lastScore);
		_logger.info("Nr playouts "+_nrPlayouts);
		_logger.info("Nr sets "+_nrSets);
		_logger.info("Nr root visits "+_rootNode.getContent().getNrPlayouts());
		_logger.info(""+((double)_nrPlayouts/(double)(time3-time0))+" kpos/sec");
		_logger.info("Percentage saved "+saved+"%");
		_logger.info("Average nr playouts "+_averagePlayouts);
		_logger.info("Nr pattern moves per playout "+(Statistics.nrPatternsPlayed/_totalNrPlayouts));
		_logger.info("Nr patterns matched "+Statistics.nrPatternsMatched);
		
		TreeNode<MonteCarloTreeSearchResult<MoveType>> bestNode = getBestChildNode(_rootNode);
		if (bestNode==null)
			return null;
//		TreeNode<MonteCarloTreeSearchResult<MoveType>> secondBestNode = getSecondBestChildNode(_rootNode,bestNode);
//		_logger.info("Second-best: "+secondBestNode.getContent());
		if (bestNode.getContent().getMove().isPass())
			_logger.info("PASS!");
		_lastScore = bestNode.getContent().getWinRatio();

		fillOwnershipArray();
		
		assert _monteCarloAdministration.isConsistent() : "Inconsistent Monte-Carlo administration at the end of the search.";

		_logger.info("Ended search with move: "+bestNode.getContent().getMove());
		return bestNode.getContent().getMove();
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
	protected TreeNode<MonteCarloTreeSearchResult<MoveType>> getNodeToExpand(TreeNode<MonteCarloTreeSearchResult<MoveType>> node, MonteCarloAdministration<MoveType> searchAdministration, IncrementalPatternMatcher patternMatcher)
	{
		if (node.getChildCount()==0 || node.getContent().getNrPlayouts()<_nrSimulationsBeforeExpansion)
			return node;

		TreeNode<MonteCarloTreeSearchResult<MoveType>> bestNode = getBestVirtualChildNode(node);

		searchAdministration.playExplorationMove(bestNode.getContent().getMove());
		if (bestNode.hashCode()==Checksum.UNINITIALIZED)
			bestNode.setChecksum(searchAdministration.getPositionalChecksum());
		
		if (searchAdministration.getNrPasses()<2)
			return getNodeToExpand(bestNode, searchAdministration, patternMatcher);
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
    protected void expandNode(TreeNode<MonteCarloTreeSearchResult<MoveType>> node, MonteCarloAdministration<MoveType> searchAdministration, IncrementalPatternMatcher patternMatcher)
	{
		_nrSets++;
		MoveIterator<MoveType> moveIterator = searchAdministration.getMoves();
		patternMatcher.updatePatternMatches();
		PatternMatchList matchList = patternMatcher.getMatchList();
		GoArray.clear(_patternSuccess);
		GoArray.clear(_patternOccurrence);
		for (int i=matchList.size(); --i>=0;)
		{
			int success;
			int usage;
			PatternMatch match = matchList.get(i);
			int xy = match.getXY();
			if (searchAdministration.getColorToMove()==BLACK)
			{
				success = match.getPattern().getBlackNrSuccesses();
				usage = match.getPattern().getBlackNrOccurrences();
			}
			else
			{
				success = match.getPattern().getWhiteNrSuccesses();
				usage = match.getPattern().getWhiteNrOccurrences();
			}
			if (_patternOccurrence[xy]==0 || usage==0 || success/usage > _patternSuccess[xy] / _patternOccurrence[xy])
			{
				_patternSuccess[xy] = success;
				_patternOccurrence[xy] = usage;
			}
		}
		
		while (moveIterator.hasNext())
		{
			MoveType move = moveIterator.next();
			if (move.isPass() && !searchAdministration.isGameAlmostFinished())
				continue;
			@SuppressWarnings("unchecked")
			TreeNode<MonteCarloTreeSearchResult<MoveType>> nextNode = (TreeNode<MonteCarloTreeSearchResult<MoveType>>) TreeNodeFactory.getSingleton().createTreeNode();
			@SuppressWarnings("unchecked")
			MonteCarloTreeSearchResult<MoveType> result = (MonteCarloTreeSearchResult<MoveType>)
				SearchResultFactory.createMonteCarloTreeSearchResult();
			nextNode.setContent(result);
			result.setMove(move);
			result.setPatternSuccess(_patternSuccess[((GoMove)move).getXY()]);
			result.setPatternOccurrence(_patternOccurrence[((GoMove)move).getXY()]);
			// Set pattern usage.
			result.setParentResult(node.getContent());
			node.add(nextNode);
		}
		moveIterator.recycle();
	}
    
    /**
     * The result is passed up the tree.
     * 
     * @param node
     * @param win
     */
	protected void adjustTreeValue(TreeNode<MonteCarloTreeSearchResult<MoveType>> node, boolean win)
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
	
	protected TreeNode<MonteCarloTreeSearchResult<MoveType>> getBestChildNode(TreeNode<MonteCarloTreeSearchResult<MoveType>> node)
	{
		TreeNode<MonteCarloTreeSearchResult<MoveType>> bestNode = null;
		
		for (int i=0; i<node.getChildCount(); i++)
		{
			TreeNode<MonteCarloTreeSearchResult<MoveType>> nextNode = node.getChildAt(i);
			
			if (!_monteCarloAdministration.hasRepetition(nextNode.hashCode()) &&
						(bestNode==null || nextNode.getContent().isBetterResultThan(bestNode.getContent())))
			{
				if (node!=_rootNode || !nextNode.getContent().getMove().isPass() || _monteCarloAdministration.isGameAlmostFinished())
					bestNode = nextNode;
			}
		}
		return bestNode;
	}
	
	protected TreeNode<MonteCarloTreeSearchResult<MoveType>> getSecondBestChildNode(TreeNode<MonteCarloTreeSearchResult<MoveType>> node, TreeNode<MonteCarloTreeSearchResult<MoveType>> bestNode)
	{
		TreeNode<MonteCarloTreeSearchResult<MoveType>> secondBestNode = null;
		
		for (int i=0; i<node.getChildCount(); i++)
		{
			TreeNode<MonteCarloTreeSearchResult<MoveType>> nextNode = node.getChildAt(i);
			
			if (nextNode!=bestNode && !_monteCarloAdministration.hasRepetition(nextNode.hashCode()) &&
						(secondBestNode==null || nextNode.getContent().isBetterResultThan(secondBestNode.getContent())))
			{
				if (node!=_rootNode || !nextNode.getContent().getMove().isPass() || _monteCarloAdministration.isGameAlmostFinished())
					secondBestNode = nextNode;
			}
		}
		return secondBestNode;
	}
	
	protected TreeNode<MonteCarloTreeSearchResult<MoveType>> getBestVirtualChildNode(TreeNode<MonteCarloTreeSearchResult<MoveType>> node)
	{
		TreeNode<MonteCarloTreeSearchResult<MoveType>> bestNode = null;
		
		synchronized(node)
		{
			int nrChildren = node.getChildCount();
			for (int i=0; i<nrChildren; i++)
			{
				TreeNode<MonteCarloTreeSearchResult<MoveType>> nextNode = node.getChildAt(i);
				if (i==0 && (nextNode.getContent().getNrPlayouts()&3)!=0)
					return nextNode;
				if (bestNode==null || nextNode.getContent().isBetterVirtualResultThan(bestNode.getContent()))
					bestNode = nextNode;
			}
			node.makeFirstChild(bestNode);
		}
		return bestNode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.search.Search#getBestMovePath(tesuji.core.util.ArrayStack)
	 */
	public void getBestMovePath(ArrayStack<MoveType> moveList)
	{
		getBestMovePath(_rootNode,moveList);
	}
	
	private void getBestMovePath(TreeNode<MonteCarloTreeSearchResult<MoveType>> startNode, ArrayStack<MoveType> moveList)
	{
		TreeNode<MonteCarloTreeSearchResult<MoveType>> node = startNode;

		while (node.getChildCount()!=0)
		{
			node = getBestChildNode(node);
			moveList.push(node.getContent().getMove());
		}
	}

//	private TreeNode<MonteCarloTreeSearchResult<MoveType>> getMoveNode(MoveType move)
//	{
//		for (int i=0; i<_rootNode.getChildCount(); i++)
//		{
//			TreeNode<MonteCarloTreeSearchResult<MoveType>> nextNode = _rootNode.getChildAt(i);
//			if (nextNode.getContent().getMove().equals(move))
//				return nextNode;
//		}
//		return null; // Should never come here.
//	}
	
	/* (non-Javadoc)
	 * @see tesuji.games.general.search.Search#playMove(java.lang.Object)
	 */
	public void playMove(MoveType move)
	{
		adjustPatterns((GoMove)move);
		_monteCarloAdministration.playMove(move);
		_patternMatcher.updatePatternMatches();
		
//		TreeNode<MonteCarloTreeSearchResult<MoveType>> newRoot = getMoveNode(move);
		
//		if (newRoot==null)
		{
			reset();
		}
//		else
//		{
//			newRoot.getContent().setParentResult(null);
//			newRoot.removeFromParent();
//			_rootNode.recycle();
//			_rootNode = newRoot;
//			_logger.info("Remain "+_rootNode.getContent().getNrPlayouts()+" visits");
//		}
	}
	
	/* (non-Javadoc)
	 * @see tesuji.games.general.search.Search#takeBack()
	 */
	public void takeBack()
	{
	}

	private void adjustPatterns(GoMove move)
	{
		Vector<PatternMatch> list = new Vector<PatternMatch>();
		int xy = move.getXY();
		PatternMatchList matchList = _patternMatcher.getMatchList();
		if (matchList.size()==0)
			return;

		for (int i=matchList.size(); --i>=0;)
		{
			PatternMatch match = matchList.get(i);
			if (match.getXY()==xy)
			{
				match.increasePatternRatio(move.getColor());
				list.add(match);
			}
			else
				match.increasePatternOccurrence(move.getColor());
			_patternManager.updatePattern(match.getPattern());
		}
		/*if (list.size()!=0)
		{
			int index = (int)(Math.random()*list.size());
			PatternMatch m = list.get(index);
			// if (m.getXY()==xy) implied
			{
				BoardModel boardModel = _monteCarloAdministration.getBoardModel();
				Pattern p = m.getPattern();
				Pattern newPattern = p.createMutation(boardModel,xy,m.getOrientation(),m.isInverted(),true,_monteCarloAdministration.getColorToMove());
				if (!newPattern.isEmpty())
				{
					_logger.info("Pattern before:\n"+p);
					_logger.info("Pattern after:\n"+newPattern);
					_patternMatcher.addNewPattern(newPattern);
				}
			}
		}*/
		/*
		Pattern newPattern = Pattern.createFromBoard(xy,(MonteCarloGoAdministration)_monteCarloAdministration);
		if (newPattern.getPointCount()>5)
		{
			_logger.info("Create new pattern:\n"+newPattern.toString());
			//_patternMatcher.addNewPattern(newPattern);
		}
		else
			_logger.info("Pattern too small: \n"+newPattern.toString());
		*/
	}
	
	/* (non-Javadoc)
     * @see tesuji.games.general.search.Search#clear()
     */
    public void clear()
    {
		cleanupPatterns();
		_monteCarloAdministration.clear();
		reset();
		_lastScore = 0.0;
		DataProviderList.getSingleton().addDataProvider(new DefaultDoubleArrayProvider("Ownership", _ownershipArray, _monteCarloAdministration.getBoardSize()));
    }
    
    protected void reset()
    {
		_rootNode.recycle();
		initRoot();   
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

	public boolean getIsTestVersion()
	{
		return _isTestVersion;
	}

	public void setIsTestVersion(boolean isTestVersion)
	{
		_isTestVersion = isTestVersion;
		if (_monteCarloAdministration!=null)
			_monteCarloAdministration.setIsTestVersion(_isTestVersion);
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
	
	private boolean reachedStopCondition()
	{
		if (_nrPlayouts>=_nodeLimit && _secondsPerMove==0)
			return true;
		
		if ((_nrPlayouts&15)==0 && _nodeLimit>0 && _secondsPerMove==0)
		{
			TreeNode<MonteCarloTreeSearchResult<MoveType>> bestNode;
			TreeNode<MonteCarloTreeSearchResult<MoveType>> secondBestNode;
			bestNode = getBestChildNode(_rootNode);
			secondBestNode = getSecondBestChildNode(_rootNode,bestNode);
			if (bestNode.getContent().getNrPlayouts()-secondBestNode.getContent().getNrPlayouts()>_nodeLimit-_nrPlayouts)
				return true;
/*    				if (_isTestVersion && _nrPlayouts > _nodeLimit/4)
			{
				if (bestNode.getContent().getNrPlayouts() / 3 > secondBestNode.getContent().getNrPlayouts())
				{
    				_logger.info("Urgent move found, stopping search.");
					break;
				}
			}
*/
		}
		
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return _monteCarloAdministration.toString();
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
		private MonteCarloAdministration<MoveType> _searchAdministration;
		private IncrementalPatternMatcher _searchPatternMatcher;
		private double[] _weightMap;
		private byte[] _colorMap;
    	
    	public SearchProcess(byte startColor)
    	{
    		_startColor = startColor;
    		running = false;
    		_searchAdministration = _monteCarloAdministration.createClone();
    		_searchPatternMatcher = _patternMatcher.createClone();
//    		_searchAdministration.getExplorationListenerSupport().addBoardModelListener(_searchPatternMatcher);
    		_searchAdministration.getBoardModel().addBoardModelListener(_searchPatternMatcher);
    		_weightMap = createDoubles();
    		_colorMap = createBytes();
    	}
    	
    	public void run()
    	{
    		try
    		{
    		running = true;
    		int nrDuplicateRuns = 0;
    		do
    		{
    			int nrPlayouts = _nrPlayouts;
    			_searchAdministration.copyDataFrom(_monteCarloAdministration);
    			_searchPatternMatcher.copyDataFrom(_patternMatcher);
    			
    			TreeNode<MonteCarloTreeSearchResult<MoveType>> node = getNodeToExpand(_rootNode, _searchAdministration, _searchPatternMatcher);
    			TreeNode<MonteCarloTreeSearchResult<MoveType>> playoutNode = node;
   				
	    		if (node != null)
				{
	    			if (node.getChildCount()==0)
	    			{
	    				synchronized (node)
                        {
							expandNode(node, _searchAdministration, _searchPatternMatcher);
                        }
	    			}
	    			
//	    			if (getIsTestVersion())
//	    			{
//						int start = _searchAdministration.getMoveStack().getSize();
//						boolean blackWins = _searchAdministration.playout();
//						_nrSimulatedMoves += _searchAdministration.getNrSimulatedMoves();
//						_nrPlayouts++;
//				    	adjustTreeValue(playoutNode, blackWins);
//
//				    	if (_useAMAF)
//				    	{
//							IntStack playoutMoves = _searchAdministration.getMoveStack();
//							byte color = _monteCarloAdministration.getColorToMove();
//							int end = playoutMoves.getSize();
//							
//							while (playoutNode!=null)
//							{
//						    	if (playoutNode.getContent().getNrPlayouts()<MonteCarloTreeSearchResult.OWNERSHIP_MAXIMUM)
//						    		playoutNode.getContent().addOwnership(_searchAdministration.getBlackOwnership(), _searchAdministration.getWhiteOwnership());
//
//								color = opposite(playoutNode.getContent().getMove().getColor());
//								double weight = 1.0;
//								double weightDelta = 1.0 / (end - start + 1); // Michael Williams' idea to use decreasing weights
//								GoArray.clear(_weightMap);
//								GoArray.clear(_colorMap);
//								for (int i=start; i<end; i++)
//								{
//									int moveXY = playoutMoves.get(i);
//									if (_colorMap[moveXY]==0)
//									{
//										_colorMap[moveXY] = color;
//										_weightMap[moveXY] = weight;
//									}
//									//if (_useEnhanced)
//										weight -= weightDelta;
//									color = opposite(color);
//								}
//
//								color = opposite(playoutNode.getContent().getMove().getColor());
//								boolean playerWins = (blackWins && color==BLACK) || (!blackWins && color==WHITE);
//								double score = playerWins ? MonteCarloTreeSearchResult.MAX_SCORE : MonteCarloTreeSearchResult.MIN_SCORE;
//								for (int i=0; i<playoutNode.getChildCount(); i++)
//								{
//									TreeNode<MonteCarloTreeSearchResult<MoveType>> nextNode = playoutNode.getChildAt(i);
//									MonteCarloTreeSearchResult<MoveType> result = nextNode.getContent();
//									GoMove move = (GoMove) result.getMove();
//									int xy = move.getXY();
//									double weightXY = _weightMap[xy];
//									if (_colorMap[xy]==color)
//										result.increaseVirtualPlayouts(weightXY*score,weightXY);
//								}
//								playoutNode = playoutNode.getParent();
//								start--;
//							}
//				    	}
//	    			}
//	    			else
	    			{
//	    			_searchAdministration.getBoardModel().removeBoardModelListener(_searchPatternMatcher);
					boolean blackWins = _searchAdministration.playout();
//	    			_searchAdministration.getBoardModel().addBoardModelListener(_searchPatternMatcher);
					_nrSimulatedMoves += _searchAdministration.getNrSimulatedMoves();
					_nrPlayouts++;
			    	adjustTreeValue(playoutNode, blackWins);

			    	if (_useAMAF)
			    	{
						IntStack playoutMoves = _searchAdministration.getMoveStack();
						byte color = _monteCarloAdministration.getColorToMove();
						int start = _monteCarloAdministration.getMoveStack().getSize();
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
						
						while (playoutNode!=null)
						{
					    	if (playoutNode.getContent().getNrPlayouts()<MonteCarloTreeSearchResult.OWNERSHIP_MAXIMUM)
					    		playoutNode.getContent().addOwnership(_searchAdministration.getBlackOwnership(), _searchAdministration.getWhiteOwnership());

					    	color = opposite(playoutNode.getContent().getMove().getColor());
							boolean playerWins = (blackWins && color==BLACK) || (!blackWins && color==WHITE);
							double score = playerWins ? MonteCarloTreeSearchResult.MAX_SCORE : MonteCarloTreeSearchResult.MIN_SCORE;
							for (int i=0; i<playoutNode.getChildCount(); i++)
							{
								TreeNode<MonteCarloTreeSearchResult<MoveType>> nextNode = playoutNode.getChildAt(i);
								MonteCarloTreeSearchResult<MoveType> result = nextNode.getContent();
								GoMove move = (GoMove) result.getMove();
								int xy = move.getXY();
								double weightXY = _weightMap[xy];
								if (_colorMap[xy]==color)
									result.increaseVirtualPlayouts(weightXY*score,weightXY);
							}
							playoutNode = playoutNode.getParent();
						}
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

    			if (reachedStopCondition())
    				break;
    		}
    		while (running);
    		}
    		catch (Exception exception)
    		{
    			_logger.error("Unexpected exception in MC thread: "+exception.getMessage());
    			exception.printStackTrace();
    		}
    	}
    	
    	public void stop()
    	{
    		running = false;
    	}
    }

	private void fillOwnershipArray()
	{
		for (int i=GoArray.FIRST; i<= GoArray.LAST; i++)
		{
			double black = _rootNode.getContent().getBlackOwnership()[i];
			double white = _rootNode.getContent().getWhiteOwnership()[i];
			_ownershipArray[i] = (black / (black+white))*200.0 - 100.0;
		}
	}

	@Override
    public boolean isGameFinished()
    {
	    // TODO Auto-generated method stub
	    return false;
    }
}
