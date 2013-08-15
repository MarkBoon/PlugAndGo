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

package tesuji.games.go.pattern.incremental;

import tesuji.core.util.ArrayList;

/**
 * 
 */
public class MatchingState
{
	// TODO - determine appropriate starting size.
	private PatternMatchList _matchList = new PatternMatchList();
	private ArrayList<IncrementalPatternTreeNode> _nodeList = new ArrayList<IncrementalPatternTreeNode>(100);
	/**
     * @return the matchList
     */
    public PatternMatchList getMatchList()
    {
    	return _matchList;
    }
	/**
     * @return the nodeList
     */
    public ArrayList<IncrementalPatternTreeNode> getNodeList()
    {
    	return _nodeList;
    }
    
    public void clear()
    {
    	_matchList.clear();
    	_nodeList.clear();
    }

    public void add(PatternMatch match)
    {
    	_matchList.add(match);
    }
    
    public PatternMatch findAndRemoveMatch(IncrementalPatternTreeLeaf leaf, int startXY)
    {
    	for (int i=_matchList.size(); --i>=0;)
    	{
    		PatternMatch match = _matchList.get(i);
    		if (match.getLeaf()==leaf && match.getXY()==startXY)
    		{
    			_matchList.remove(i);
    			return match;
    		}
    	}
    	assert false; // Should never come here.
    	return null;
    }
    
    public PatternMatch findMatch(IncrementalPatternTreeLeaf leaf, int startXY)
    {
    	for (int i=_matchList.size(); --i>=0;)
    	{
    		PatternMatch match = _matchList.get(i);
    		if (match.getLeaf()==leaf && match.getXY()==startXY)
    			return match;
    	}
    	return null;
    }
    
    IncrementalPatternTreeNode findNode(IncrementalPatternTreeNode node)
    {
    	for (int i=_nodeList.size(); --i>=0;)
    	{
    		if (_nodeList.get(i)==node)
    			return node;
    	}
    	return null;
    }
    
    public void add(IncrementalPatternTreeNode node)
    {
    	assert(findNode(node)==null);
    	_nodeList.add(node);
    }
    
    public void remove(IncrementalPatternTreeNode node)
    {
    	for (int i=_nodeList.size(); --i>=0;)
    	{
    		if (_nodeList.get(i)==node)
    		{
    			_nodeList.remove(i);
    			return;
    		}
    	}
    	assert(false);
    }
       
    public boolean hasMatches()
    {
    	return !_matchList.isEmpty();
    }
    
    public boolean hasNodes()
    {
    	return !_nodeList.isEmpty();
    }
   
    public void copyDataFrom(MatchingState source)
    {
    	_matchList.reset();
    	_nodeList.clear();
    	for (int i=0; i<source._nodeList.size(); i++)
    		_nodeList.add(source._nodeList.get(i));
    }
    
    public MatchingState createClone()
    {
    	MatchingState clone = new MatchingState();
    	clone._matchList = new PatternMatchList();
    	clone._nodeList = _nodeList.createClone();
    	
    	return clone;
    }
    
    public boolean equals(Object o)
    {
    	if (!(o instanceof MatchingState))
    		return false;
    	
    	MatchingState compare = (MatchingState) o;
    	
    	for (int i=0; i<compare._matchList.size(); i++)
    	{
    		PatternMatch match = compare._matchList.get(i);
    		if (findMatch(match.leaf,match.xy)==null)
    			return false;
    	}
    	
    	for (int i=0; i<compare._nodeList.size(); i++)
    	{
    		IncrementalPatternTreeNode node = compare._nodeList.get(i);
    		if (findNode(node)==null)
    			return false;
    	}
    	
    	return true;
    }
}
