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
 *
 *	Except as contained in this notice, the name of a Tesuji Software
 *	shall not be used in advertising or otherwise to promote the sale, use
 *	or other dealings in this Software without prior written authorization
 *	of Tesuji Software.<br><br>
 *	<font color="#00000"><font size=+1>
 */
package tesuji.games.go.pattern.client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Comparator;

import tesuji.core.util.ArrayList;

import tesuji.games.go.pattern.common.Pattern;
import tesuji.games.go.pattern.common.PatternGroup;
import tesuji.games.go.pattern.common.PatternManager;

/**
 	This is the controller for the PatternListPanel class.
 	There's a 'window' defined on the list of patterns.
 */
public class PatternListController
{
	private PatternGroup patternGroup;
	private ArrayList<Pattern> patternList;
	private int firstIndex = 0;		// First pattern in the 'window'
	private int nrPatterns;			// Number of patterns in the 'window'.
	private Pattern selectedPattern;
	private PatternManager patternManager;
	private PropertyChangeSupport changeSupport;
	
	public static final String SELECTED_PATTERN_GROUP_PROPERTY = "selectedPatternGroup";
	
	/**
	 * @param group PatternGroup for which this this controller handles the list of patterns.
	 * @param manager for storage of the patterns
	 */
	public PatternListController(PatternGroup group, PatternManager manager)
	{
		patternManager = manager;
		
		changeSupport = new PropertyChangeSupport(this);
		setPatternGroup(group);
	}
	
	/**
	 * @return whether the 'window' can be moved forward
	 */
	public boolean canGoToNext()
	{
		if (patternList==null)
			return false;
		
		return (firstIndex < patternList.size()-nrPatterns);
	}
	
	/**
	 * @return whether the 'window' can be moved backward
	 */
	public boolean canGoToPrevious()
	{
		return (firstIndex>0);
	}
	
	/**
	 * Create a new pattern for the PatternGroup
	 */
	public void createNewPattern()
	{
		Pattern newPattern = new Pattern();
		newPattern.setGroupId(patternGroup.getGroupId());
		newPattern = patternManager.createPattern(newPattern);

		/*if (selectedPattern==null)
			patternList.add(newPattern);
		else
			patternList.add(patternList.indexOf(selectedPattern),newPattern);*/
	}
	
	/**
	 * Remove the selected pattern from the list and
	 * remove it from the database.
	 */
	public void deleteSelectedPattern()
	{
		if (selectedPattern==null)
			return;

		patternManager.removePattern(selectedPattern);
		patternList.remove(selectedPattern);
		selectedPattern = null;
	}
	
	/**
	 * Create a new pattern that is a copy of the selected pattern.
	 */
	public void duplicateSelectedPattern()
	{
		if (selectedPattern==null)
			return;

		Pattern copy = (Pattern) selectedPattern.clone();
		copy = patternManager.createPattern(copy);
		patternList.insert(patternList.indexOf(selectedPattern),copy);
	}
	
	public PatternGroup getPatternGroup()
	{
		return patternGroup;
	}
	
	public void setPatternGroup(PatternGroup group)
	{
		patternGroup = group;
		if (group!=null)
		{
			ArrayList<Pattern> newList = patternManager.getPatterns(group);
			patternGroup.setPatternList(newList);
			patternList = patternGroup.getPatternList();
		}
		else
			patternList = new ArrayList<Pattern>();
		
		changeSupport.firePropertyChange(SELECTED_PATTERN_GROUP_PROPERTY,false,true);
	}
	
	/**
	 * @return array of patterns that fall in the 'window'
	 */
	public Pattern[] getPatterns()
	{
		if (patternList==null)
			return new Pattern[0];

		Pattern[] patterns = new Pattern[nrPatterns];

		for (int i=firstIndex; i<patternList.size() && i<firstIndex+nrPatterns; i++)
			patterns[i-firstIndex] = patternList.get(i);
	
		return patterns;
	}
	
	public Pattern getSelectedPattern()
	{
		return selectedPattern;
	}
	
	/**
	 * Move the 'window' one page forward
	 */
	public void goToNext()
	{
		if (patternList.size()<=nrPatterns)
			return;

		if (firstIndex+nrPatterns<patternList.size())
			firstIndex += nrPatterns;
		else
			firstIndex = patternList.size()-nrPatterns;
	}
	
	/**
	 * Move the 'window' one page back
	 */
	public void goToPrevious()
	{
		if (firstIndex<=nrPatterns)
			firstIndex = 0;
		else
			firstIndex -= nrPatterns;
	}
	
	public int getFirstPatternNr()
	{
		return firstIndex;
	}
	
	/**
	 * @param nr Set the window to start at a particular pattern-number
	 */
	public void setFirstPatternNr(int nr)
	{
		if (nr<0 || nr>patternList.size()-nrPatterns)
			return;
		
		setSelectedPattern(null);
		firstIndex = nr;
	}
	
	/**
	 * Replace the selected pattern with the pattern from the editor component.
	 * But retain the original groupId and patternNr
	 * 
	 * @param newPattern
	 */
	public void replaceSelectedPattern(Pattern newPattern)
	{
		if (selectedPattern==null)
			return;

		int nr = selectedPattern.getPatternNr();
		int groupId = selectedPattern.getGroupId();
		selectedPattern.copy(newPattern);
		selectedPattern.setPatternNr(nr);
		selectedPattern.setGroupId(groupId);
		
		patternManager.updatePattern(selectedPattern);
	}
	
	/**
	 * Add a new pattern for the PatternGroup
	 * 
	 * @param newPattern
	 */
	public void addNewPattern(Pattern newPattern)
	{
		newPattern.setGroupId(patternGroup.getGroupId());
		newPattern = patternManager.createPattern(newPattern);
		patternList.add(newPattern);
	}
	
	/**
	 * See if a pattern can be found in the pattern-list
	 * 
	 * @param searchPattern
	 */
	public void findPattern(Pattern searchPattern)
	{
		/*
		byte[] board = GoArray.createBytes();
		
		for (int i=0; i<searchPattern.getWidth(); i++)
		{
			for (int j=0; j<searchPattern.getHeight(); j++)
			{
				byte value = searchPattern.getPoint(i,j);
				int xy = GoArray.toXY(i+1,j+1);
				if (value==BLACK)
					board[xy] = BLACK;
				if (value==WHITE)
					board[xy] = WHITE;
				if (value==NOCARE)
					board[xy] = NOCARE;
			}
		}
		PatternMatchList matchList = new PatternMatchList();
		if (searchPattern.getUserX()==UNDEFINED_COORDINATE)
			patternSet.matchPatterns(board,19,null,matchList);
		else
		{
			IntStack pointList = ArrayFactory.createIntStack();
			int xy = GoArray.toXY(searchPattern.getUserX(),searchPattern.getUserY());
			pointList.push(xy);
			patternSet.matchPatterns(pointList,board,19,null,matchList);
			pointList.recycle();
		}
		if (matchList.size()>0)
		{
			PatternMatch match = matchList.get(0);
			System.err.println("Found pattern "+match.getPattern().getPatternNr());
			setFirstPatternNr(match.getPattern().getPatternNr());
		}
		*/
	}
	
	/**
	 * Set how many patterns are in the 'window'
	 * 
	 * @param n
	 */
	public void setNrPatterns(int n)
	{
		nrPatterns = n;
	}
	
	/**
	 * Set a pattern as the 'selected' one.
	 * 
	 * @param pattern that is selected in the panel.
	 */
	public void setSelectedPattern(Pattern pattern)
	{
		selectedPattern = pattern;
		if (pattern==null || patternList.indexOf(pattern)<0)
			System.err.println("Selected pattern is not in the group.");
	}
	
	
	public void sortPatterns(Comparator<Pattern> comparator)
	{
		Arrays.sort(patternList.toArray(),comparator);
	}

	
	/**
	 * Save the patterns in the database.
	 * They are actually already in the database, but this operation
	 * sets the pattern-numbers in the order that they are in the list
	 * in this controller.
	 */
	public void savePatterns()
	{
		patternManager.updatePatterns(patternList);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		changeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		changeSupport.removePropertyChangeListener(listener);
	}
}
