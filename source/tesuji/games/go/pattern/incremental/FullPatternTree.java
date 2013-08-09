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

/**
 * This is a 'full' pattern-tree in the sense that it stores all
 * 16 possible permutations of a pattern in the tree. I also needed
 * a different name to distinguish it from the 'PatternTree' class
 * still in the traditional package.
*/
class FullPatternTree
{
	private static Logger _logger = Logger.getLogger(FullPatternTree.class);
	
	private int nrNewPatterns;
	private int dirtyLimit = 100;
	
	private PointSpiral patternSpiral;
//	private int[] pointOrder;
	
	private IncrementalPatternTreeNode root;
	
	/**
	 * PatternTree constructor.
	 * 
	 * @param group of patterns from which to construct the tree
	 * @param spiral containing the order in which the points are going to be stored.
	 */
	public FullPatternTree(PatternGroup group, PointSpiral spiral)
	{
		this();
		
//		if (group.getPatternList().size()>0)
//		{
//			FastPatternTreeNode newRoot = FileTreeManager.readPatternTree(""+group.getPatternList().get(0).getGroupId());
//			if (newRoot!=null)
//				root = newRoot;
//		}
		
		patternSpiral = spiral;
		patternSpiral.computeSpiral(group);
	
		addPatterns(group.getPatternList());
		
		if (group.getPatternList().size()>dirtyLimit)
			dirtyLimit = group.getPatternList().size()/2;

		nrNewPatterns = 0;
//		pointOrder = patternSpiral.getPointOrder();
	}
	
	/**
	 * PatternTree default constructor.
	 */
	public FullPatternTree()
	{
		root = new IncrementalPatternTreeNode(PatternNodeType.ROOT);
	}
	
	/**
	 * Add a list of patterns to the tree.
	 * 
	 * @param patternList
	 */
	public void addPatterns(List<Pattern> patternList)
	{
		if (patternList.size()==0)
			return;
		
		root.addPatterns(patternList,patternSpiral,0,0);

		// Weed out the duplicates.
		ArrayList<Pattern> addedPatterns = new ArrayList<Pattern>(patternList.size());
		for (int i=0; i<patternList.size(); i++)
		{
			if (patternList.get(i).isAdded())
			{
				nrNewPatterns++;
				addedPatterns.add(patternList.get(i));
			}
		}

		// The tree is constructed for all 16 possible permutations of the pattern.
		for (int orientation=1; orientation<16; orientation++)
		{
			root.addPatterns(addedPatterns,patternSpiral,0,orientation);
			//if (patternList.size()>1000)
			//	System.out.println("Added patterns for orientation "+orientation);
		}
		_logger.info("Added "+nrNewPatterns+" patterns");
	}
	
	/**
	 * @return the root-node of the tree
	 */
	public IncrementalPatternTreeNode getRoot()
	{
		return root;
	}
	
	/**
	 * When patterns get added, this dirty flag is set. This is because (potentially) new patterns
	 * compromise the efficiency of the tree and the tree and spiral may need to be recomputed
	 * for optimal performance.
	 * 
	 * @return
	 */
	public boolean isDirty()
	{
		return nrNewPatterns>dirtyLimit;
	}		
}
