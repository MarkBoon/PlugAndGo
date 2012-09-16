package tesuji.games.go.search;

import static tesuji.games.general.ColorConstant.BLACK;
import static tesuji.games.general.ColorConstant.WHITE;
import static tesuji.games.general.ColorConstant.opposite;
import static tesuji.games.go.util.GoArray.createBytes;
import static tesuji.games.go.util.GoArray.createDoubles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.cliffc.high_scale_lib.NonBlockingHashMapLong;

import tesuji.core.util.ArrayStack;
import tesuji.games.general.TreeNode;
import tesuji.games.general.search.Search;
import tesuji.games.general.search.SearchProperties;
import tesuji.games.general.search.SearchResult;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.monte_carlo.MonteCarloGoAdministration;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.IntStack;
import tesuji.games.go.util.PointSet;

public class MonteCarloHashMapSearch
	implements Search<GoMove>, PropertyChangeListener
{
	private NonBlockingHashMapLong<MonteCarloHashMapResult> _hashMap = new NonBlockingHashMapLong<MonteCarloHashMapResult>();
	private int _nrPlayouts;
	private int _nrSimulationsBeforeExpansion = 0;
	private int _nrSimulatedMoves = 0;
	private boolean _useAMAF = true;
	
	@Override
    public void propertyChange(PropertyChangeEvent arg0)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void setSearchProperties(SearchProperties properties)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public SearchProperties getSearchProperties()
    {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public void setSecondsPerMove(int seconds)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void setMinimumNrNodes(int minimum)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void setNrProcessors(int nrProcessors)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void setIsTestVersion(boolean testVersion)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public SearchResult<GoMove> doSearch(byte startColor) throws Exception
    {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public void playMove(GoMove move)
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void takeBack()
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void clear()
    {
	    // TODO Auto-generated method stub
	    
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
		private MonteCarloGoAdministration _newAdministration;
		private MonteCarloGoAdministration _initAdministration;
		private MonteCarloGoAdministration _searchAdministration;
		private double[] _weightMap;
		private byte[] _colorMap;
		private IntStack _moveStack;
		private IntStack _checksumStack;
    	
    	public SearchProcess(byte startColor, MonteCarloGoAdministration admin)
    	{
    		_startColor = startColor;
    		running = false;
    		_initAdministration = (MonteCarloGoAdministration) admin.createClone();
    		_searchAdministration = (MonteCarloGoAdministration) admin.createClone();
    		_weightMap = createDoubles();
    		_colorMap = createBytes();
    		_moveStack = new IntStack(GoArray.LAST, null);
    	}
    	
    	public void updateAdministration(MonteCarloGoAdministration admin)
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
					_nrSimulatedMoves += _searchAdministration.getNrSimulatedMoves();
					_nrPlayouts++;
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
						
						while (!_moveStack.isEmpty())
						{							
					    	color = opposite(playoutNode.getMove().getColor());
							boolean playerWins = (blackWins && color==BLACK) || (!blackWins && color==WHITE);
							double score = playerWins ? MonteCarloTreeSearchResult.MAX_SCORE : MonteCarloTreeSearchResult.MIN_SCORE;
							PointSet points = playoutNode.getEmptyPoints();
							for (int i=0; i<points.getSize(); i++)
							{
								int xy = points.get(i);
								double weightXY = _weightMap[xy];
								if (_colorMap[xy]==color)
									playoutNode.increaseVirtualPlayouts(xy,weightXY*score,weightXY);
							}
							playoutNode = _hashMap.get(_moveStack.pop());
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
	    		_checksumStack.push(_searchAdministration.getPositionalChecksum());
	    		_moveStack.push(xy);
	   			_searchAdministration.playExplorationMove(xy);
	    		MonteCarloHashMapResult nextNode = _hashMap.get(_searchAdministration.getPositionalChecksum());
	    		if (nextNode==null)
	    		{
	    			if (node.getPlayouts(xy)<_nrSimulationsBeforeExpansion)
	    			{
	        			return node;
	    			}
	    			else
	    			{
	    				nextNode = SearchResultFactory.createMonteCarloHashMapResult();
	    	   			GoMove move = GoMoveFactory.getSingleton().createMove(xy, _searchAdministration.getColorToMove());
	    				nextNode.setMove(move);
	    				nextNode.setPointSet(_searchAdministration.getEmptyPoints());
	    				return nextNode;
	    			}
	    		}
	    		node = nextNode;
    		}
    		return node;
    	}
    	
    	public void adjustTreeValue(boolean blackWins)
    	{
    		while (!_moveStack.isEmpty())
    		{
    			int xy = _moveStack.pop();
    			long checksum = _checksumStack.pop();
    			MonteCarloHashMapResult node = _hashMap.get(checksum);
    			assert(node!=null);
    			assert(node.getMove().getXY()==xy);
    			node.increasePlayouts(xy,blackWins);
    		}
    	}
    }

	@Override
    public boolean isGameFinished()
    {
		return false;
    }
}
