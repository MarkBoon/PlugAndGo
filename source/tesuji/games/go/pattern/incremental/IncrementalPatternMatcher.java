/**
 *	Product: Tesuji Software Go Library.<br><br>
 *
 *	<font color="#CC6600"><font size=-1>
 *	Copyright (c) 2001-2004 Tesuji Software B.V.<br>
 *	All rights reserved.<br><br>
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a
 *	copy of this software and associated documentation files (the
 *	"Software"), to deal in the Software without restriction, including
 *	without limitation the rights to use, copy, modify, merge, publish,
 *	distribute, and/or sell copies of the Software, and to permit persons
 *	to whom the Software is furnished to do so, provided that the above
 *	copyright notice(s) and this permission notice appear in all copies of
 *	the Software and that both the above copyright notice(s) and this
 *	permission notice appear in supporting documentation.<br><br>
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 *	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.<br><br>
 *	<font color="#00000"><font size=+1>
 */
package tesuji.games.go.pattern.incremental;

import org.apache.log4j.Logger;

import tesuji.core.util.ArrayList;
import tesuji.core.util.List;
import tesuji.games.go.pattern.common.Pattern;
import tesuji.games.go.pattern.common.PatternGroup;
import tesuji.games.go.pattern.common.PatternManager;
import tesuji.games.go.util.GoArray;
import tesuji.games.model.BoardChange;
import tesuji.games.model.BoardChangeFactory;
import tesuji.games.model.BoardModel;
import tesuji.games.model.BoardModelListener;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.util.GoArray.MAX;

/**
 * This is the main class of the package containing an incremental pattern-matcher.
 * 
 * Although the basis is that of a pattern-matcher implemented by Charlie Carroll in C++,
 * its nature has changed considerably over time. A pattern-matcher more closely
 * resembling the original implementation is in the package tesuji.games.go.pattern.traditional.
 * 
 */
public class IncrementalPatternMatcher
	implements BoardModelListener
{
	private static Logger _logger = Logger.getLogger(IncrementalPatternMatcher.class);

	private PointSpiral spiral;
	private PatternGroup group;
	private FullPatternTree tree;
	private PatternManager patternManager;
	private BoardModel _boardModel;
	private PatternMatchList _matchList = new PatternMatchList();
	private ArrayList<BoardChange> _boardChangeList = new ArrayList<BoardChange>(32);
	private int _moveNr;
	
	private MatchingState[] matchingState;
	private ArrayList<PatternMatch> _newMatchList = new ArrayList<PatternMatch>();
	private PatternMatchList _deletedMatchList = new PatternMatchList();
	private ArrayList<Pattern> _newPatternList = new ArrayList<Pattern>();
	
	public static int nrMatches = 0;
		
	/**
	 * PatternSet constructor comment.
	 */
	public IncrementalPatternMatcher()
	{
		group = new PatternGroup();
		
		recomputeTree();
	}
	
	/**
	 * PatternSet constructor comment.
	 */
	public IncrementalPatternMatcher(PatternGroup patternGroup)
	{
		this();
		setPatternGroup(patternGroup);
	}
	
	public void initialise(BoardModel boardModel)
	{
		// Due to the incremental nature of this pattern-matcher, new patterns can only be added
		// before a new game starts, not during a game.
		tree.addPatterns(_newPatternList);
		for (Pattern p : _newPatternList)
		{
			if (p.isAdded())
			{
				patternManager.createPattern(p);
				group.getPatternList().add(p);
				_logger.info("New pattern:\n"+p.toString());
			}
		}
		_newPatternList.clear();
		recomputeTree();

		_moveNr = 1;
		_matchList.clear();
		_boardChangeList.clear();
		_boardModel = boardModel;
//		boardModel.addBoardModelListener(this);
		matchingState = new MatchingState[GoArray.MAX];
		for (int i=0; i<GoArray.MAX; i++)
		{
			_boardModel.set(i,boardModel.get(i));
			matchingState[i] = new MatchingState();
			matchingState[i].add(tree.getRoot());
		}
		for (int i=0; i<GoArray.MAX; i++)
		{
			if (_boardModel.get(i)!=EDGE)
			{
				// Note that below it does not call matchBoardToTreeAndStoreState(_context).
				// That may appear to be the same but it's not because a point may have gotten extra nodes added
				// apart from the root due to matching an earlier point. That can cause superfluous matches.
				recursiveMatchAndStoreState(tree.getRoot(), i);
			}
		}
		if (_boardModel.hasListeners())
			return;
	}
	
	public PatternGroup getPatternGroup()
	{
		return group;
	}
	
	public void setPatternGroup(PatternGroup patternGroup)
	{
		group = patternGroup;
		if (group.getPatternList()!=null && group.getPatternList().size()!=0)
			recomputeTree();		
	}
	
	public void addPattern(Pattern pattern)
	{
		ArrayList<Pattern> list = new ArrayList<Pattern>(1);
		list.add(pattern);
		tree.addPatterns(list);
	}
	
	public void deletePattern(int patternNr)
	{
		group.getPatternList().get(patternNr).setRemoved(true);
		group.getPatternList().remove(patternNr);
		tree.getRoot().removeDeadPatterns();
	}
	
	/**
	 * This is where the major work is done. Based on changes of the board,
	 * recorded in a list of BoardChange objects, the pattern-matching states
	 * are updated.
	 * 
	 * Upon completion of this method, _matchList will contain the list of patterns
	 * that are found on the board. _newMatchList will contain patterns that were
	 * newly found since the previous call to this method while _deletedMatchList
	 * contains the matches that are no longer present on the board because of the
	 * recent board-changes.
	 */
	public void updatePatternMatches()
	{
		_moveNr++;
		_newMatchList.clear();
		_deletedMatchList.clear();
		if (_boardModel.hasListeners())
			return;
    	for (int i=0; i<_boardChangeList.size(); i++)
    	{
    		BoardChange boardChange = _boardChangeList.get(i);
    		int xy = boardChange.getXY();
    		MatchingState state = matchingState[xy];
    		ArrayList<IncrementalPatternTreeNode> nodeList = state.getNodeList();
    		
    		for (int n=0; n<nodeList.size(); n++)
    		{
    			IncrementalPatternTreeNode node = nodeList.get(n);
    			recursiveMatchAndRemoveState(node,xy-node.getNextCoordinate());
    		}
    		
    		_boardModel.set(boardChange.getXY(), boardChange.getNewValue());
    		
    		for (int n=0; n<nodeList.size(); n++)
    		{
    			IncrementalPatternTreeNode node = nodeList.get(n);
    			recursiveMatchAndStoreState(node,xy-node.getNextCoordinate());
    		}
    		
    		boardChange.recycle();
    	}
    	_boardChangeList.clear();
//    	System.out.println("Match Board:");
//    	System.out.println(_boardModel.toString());
//    	System.out.println("to patterns:\n"+_newMatchList);
	}

	private void recursiveMatchAndStoreState(IncrementalPatternTreeNode node, int startXY)
	{
		// Some of the recursion has been unwound.  This loop deals with the
		// main line.  NoCare branches handled by a recursive call on this routine.
		while (node!=null)
		{
			List<IncrementalPatternTreeLeaf> leafList = node.getLeafList();
			if (leafList!=null)
				for (int i=leafList.size(); --i>=0;)
					storeMatch(leafList.get(i),startXY);

			IncrementalPatternTreeNode noCareChild = node.getNoCareChild();
			if (noCareChild!=null)
			{
				int nextXY = noCareChild.getNextCoordinate()+startXY;
				if (nextXY>0 && nextXY<GoArray.MAX && matchingState[nextXY]!=null)
					matchingState[nextXY].add(noCareChild);
				recursiveMatchAndStoreState(noCareChild,startXY);
			}

			// Figure out what's on the board point to be examined next. 
			// Appropriately update node
			node = getNextNodeAndStoreState(node,startXY);
		}
	}
	
	private void recursiveMatchAndRemoveState(IncrementalPatternTreeNode node, int startXY)
	{
		// Some of the recursion has been unwound.  This loop deals with the
		// main line.  NoCare branches handled by a recursive call on this routine.
		while (node!=null)
		{
			List<IncrementalPatternTreeLeaf> leafList = node.getLeafList();
			if (leafList!=null)
				for (int i=leafList.size(); --i>=0;)
					removeMatch(leafList.get(i),startXY);

			IncrementalPatternTreeNode noCareChild = node.getNoCareChild();
			if (noCareChild!=null)
			{
				int nextXY = noCareChild.getNextCoordinate()+startXY;
				if (nextXY>0 && nextXY<GoArray.MAX && matchingState[nextXY]!=null)
					matchingState[nextXY].remove(noCareChild);
				recursiveMatchAndRemoveState(noCareChild,startXY);
			}

			// Figure out what's on the board point to be examined next. 
			// Appropriately update node
			node = getNextNodeAndRemoveState(node,startXY);
		}
	}
	
	/**
	 * Get the next node down the tree. The value on the actual board determines
	 * which branch down is taken.
	 * 
	 * @param startXY point on the board where the matching started.
	 * @param board
	 * 
	 * @return PatternTreeNode
	 */
	private final IncrementalPatternTreeNode getNextNode(IncrementalPatternTreeNode node, int startXY)
	{
		int xy=startXY+node.getNextCoordinate();
		
		if (xy<0 || xy>=GoArray.MAX)
			return null;
		else
		{
			switch (_boardModel.get(xy))
			{
				case EMPTY:
					return node.getEmptyChild();
				case BLACK:
					return node.getBlackChild();
				case WHITE:
					return node.getWhiteChild();
				case EDGE:
					return node.getEdgeChild();
				default:
					return null;
			}
		}
	}
	
	/**
	 * Get the next node down the tree. The value on the actual board determines
	 * which branch down is taken.
	 * 
	 * @param startXY point on the board where the matching started.
	 * @param board
	 * 
	 * @return PatternTreeNode
	 */
	private final IncrementalPatternTreeNode getNextNodeAndStoreState(IncrementalPatternTreeNode node, int startXY)
	{
		IncrementalPatternTreeNode nextNode = getNextNode(node,startXY);
		if (nextNode!=null)
		{
			int nextCoordinate = startXY + nextNode.getNextCoordinate();
			if (nextCoordinate>=0 && nextCoordinate<MAX)
			{
				MatchingState state = matchingState[nextCoordinate];
				if (state!=null)
					state.add(nextNode);
			}
		}
		return nextNode;
	}
	
	/**
	 * Get the next node down the tree. The value on the actual board determines
	 * which branch down is taken.
	 * 
	 * @param startXY point on the board where the matching started.
	 * @param board
	 * 
	 * @return PatternTreeNode
	 */
	private final IncrementalPatternTreeNode getNextNodeAndRemoveState(IncrementalPatternTreeNode node, int startXY)
	{
		IncrementalPatternTreeNode nextNode = getNextNode(node,startXY);
		if (nextNode!=null)
		{
			int nextCoordinate = startXY + nextNode.getNextCoordinate();
			if (nextCoordinate>=0 && nextCoordinate<MAX)
			{
				MatchingState state = matchingState[nextCoordinate];
				if (state!=null)
					state.remove(nextNode);
			}
		}
		return nextNode;
	}
	
	private void storeMatch(IncrementalPatternTreeLeaf leaf, int startXY)
	{
		PatternMatch match = PatternMatchFactory.createMatch(leaf, startXY);
		match.setMoveNr(_moveNr);
		_matchList.addMatch(match);
		matchingState[startXY].add(match);
		_newMatchList.add(match);
	}

	private void removeMatch(IncrementalPatternTreeLeaf leaf, int startXY)
	{
		PatternMatch match = matchingState[startXY].findAndRemoveMatch(leaf, startXY);
//		assert(match!=null); // It came here, there should be a match to be found.
		if (match==null)
			return; // Actually, it happens when patterns are added dynamically.
		_matchList.removeMatch(match);
		_deletedMatchList.add(match);
	}

	public void optimize()
	{
		if (tree.isDirty())
			recomputeTree();
	}
	
	public void save()
	{
//		if (group.getPatternList().size()>0)
//			FileTreeManager.savePatternList(tree.getRoot(), ""+group.getPatternList().get(0).getGroupId());
	}
	
	public void recomputeTree()
	{
		spiral = new PointSpiral();
		tree = new FullPatternTree(group,spiral);
	}
	
	public void changeBoard(BoardChange event)
	{
		_boardChangeList.add(BoardChangeFactory.createClone(event));
	}

	/**
     * @return the patternManager
     */
    public PatternManager getPatternManager()
    {
    	return patternManager;
    }

	/**
     * @param patternManager the patternManager to set
     */
    public void setPatternManager(PatternManager patternManager)
    {
    	this.patternManager = patternManager;
    }
    
    public void updatePatterns()
    {
    	for (Pattern pattern : group.getPatternList())
    		if (pattern.isDirty())
                patternManager.updatePattern(pattern);
    }
    
    public void buildTree()
    {
    	tree.addPatterns(group.getPatternList());
    }

	/**
     * @return the matchList
     */
    public PatternMatchList getMatchList()
    {
    	return _matchList;
    }

	/**
     * @return the moveNr
     */
    public int getMoveNr()
    {
    	return _moveNr;
    }

	/**
     * @return the newMatchList
     */
    public ArrayList<PatternMatch> getNewMatchList()
    {
    	return _newMatchList;
    }

	/**
     * @return the deletedMatchList
     */
    public PatternMatchList getDeletedMatchList()
    {
    	return _deletedMatchList;
    }
    
    public void copyDataFrom(IncrementalPatternMatcher source)
    {
    	spiral = source.spiral;
    	group = source.group;
    	tree = source.tree;
    	patternManager = source.patternManager;
    	
    	System.arraycopy(source._boardModel.getSingleArray(), 0, _boardModel.getSingleArray() , 0, source._boardModel.getSingleArray().length);
//    	GoArray.copy(source._boardModel.getSingleArray(), _boardModel.getSingleArray());
    	_matchList.copyDataFrom(source._matchList);
//    	assert(_matchList.equals(source._matchList));
    	_deletedMatchList.clear(); // TODO - check for correctness.
    	_boardChangeList.clear();  // TODO - check for correctness.
    	_moveNr = source._moveNr;
		for (int i=0; i<GoArray.MAX; i++)
		{
//			if (source.matchingState[i]!=null)
				matchingState[i].copyDataFrom(source.matchingState[i]);
		}
		for (int i=0; i<_matchList.size(); i++)
		{
			PatternMatch match = _matchList.get(i);
			matchingState[match.xy].add(match);
		}   	
		assert(isEqual(this,source));
		if (_boardModel.hasListeners())
			return;
    }
    
    /**
     * Obviously, an incremental pattern-matcher has a state.
     * Cloning the obejct will replicate the state.
     * 
     * @return the cloned pattern-matcher with state.
     */
    public IncrementalPatternMatcher createClone()
    {
    	IncrementalPatternMatcher clone = new IncrementalPatternMatcher();
    	
    	clone.spiral = spiral;
    	clone.group = group;
    	clone.tree = tree;
    	clone.patternManager = patternManager;
    	
    	System.arraycopy(_boardModel.getSingleArray(), 0, clone._boardModel.getSingleArray() , 0, _boardModel.getSingleArray().length);
//    	clone._boardModel = _boardModel.createClone();
    	clone._matchList = _matchList.createIndexedClone();
    	clone._moveNr = _moveNr;

		clone.matchingState = new MatchingState[GoArray.MAX];
		for (int i=0; i<GoArray.MAX; i++)
		{
//			if (matchingState[i]!=null)
				clone.matchingState[i] = matchingState[i].createClone();
		}
		for (int i=0; i<clone._matchList.size(); i++)
		{
			PatternMatch match = clone._matchList.get(i);
			clone.matchingState[match.xy].add(match);
		}
		
		assert(isEqual(this,clone));

		if (_boardModel.hasListeners())
			return clone;
		return clone;
    }
    
    public BoardModel getBoardModel()
    {
    	return _boardModel;
    }
    
    public void addNewPattern(Pattern pattern)
    {
    	_newPatternList.add(pattern);
    }
    
    // For debugging only
    private static boolean isEqual(IncrementalPatternMatcher set1,IncrementalPatternMatcher set2)
    {
		for (int i=0; i<GoArray.MAX; i++)
		{
//			if (set1.matchingState[i]!=null)
			{
				assert(set1.matchingState[i].equals(set2.matchingState[i]));
				assert(set1.matchingState[i].getMatchList().size()==set2.matchingState[i].getMatchList().size());
				assert(set1.matchingState[i].getNodeList().size()==set2.matchingState[i].getNodeList().size());
				for (int j=set1.matchingState[i].getMatchList().size(); --j>=0;)
				{
					PatternMatch match = set1.matchingState[i].getMatchList().get(j);
					assert(set2.matchingState[i].getMatchList().indexOf(match)>=0);
				}
			}
//			else
//				assert(set2.matchingState[i]==null);
		}
    	return true;
    }
}
