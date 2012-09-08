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

import javax.swing.DefaultListModel;

import tesuji.core.model.DefaultSelectableListModel;
import tesuji.core.model.SelectableListModel;
import tesuji.core.util.List;

import tesuji.games.go.pattern.common.PatternGroup;
import tesuji.games.go.pattern.common.PatternManager;

/**
 * Controller for PatternGroup. Used by the corresponding PatternGroupPanel.
 * 
 * It retrieves and manages a list of PatternGroup objects from the database,
 * which is represented by the PatternManager.
 * 
 * One of the PatternGroup objects in this list can be 'selected'.
 */
public class PatternGroupController
{
	private DefaultListModel patternGroupList;
	private DefaultSelectableListModel patternGroupSelectionList;
	private PatternManager patternManager;
	
	/**
	 * PatternGroupController constructor.
	 * 
	 * @param manager for pattern-group persistence.
	 */
	public PatternGroupController(PatternManager manager)
	{
		patternManager = manager;
		
		List<PatternGroup> patternGroups = patternManager.getPatternGroups();
		patternGroupList = new DefaultListModel();
		for (int i=0; i<patternGroups.size(); i++)
			patternGroupList.addElement(patternGroups.get(i));
		patternGroupSelectionList = new DefaultSelectableListModel(patternGroupList);
		
		if (patternGroupSelectionList!=null && patternGroupSelectionList.getSize()!=0)
			patternGroupSelectionList.setSelectedIndex(0);
	}

	/**
	 * Create a new PatternGroup object in the PatternManager
	 * 
	 * @param groupName
	 */
	public void createNewPatternGroup(String groupName)
	{
		PatternGroup newPatternGroup = new PatternGroup();
		newPatternGroup.setGroupName(groupName);
		
		patternManager.createPatternGroup(newPatternGroup);
		
		if (getSelectedPatternGroup()==null)
			patternGroupList.addElement(newPatternGroup);
		else
			patternGroupList.insertElementAt(newPatternGroup,patternGroupList.indexOf(getSelectedPatternGroup()));
	}
	
	/**
	 * Delete the 'selected' PatternGroup from the PatternManager
	 */
	public void deleteSelectedPatternGroup()
	{
		PatternGroup selectedPatternGroup = getSelectedPatternGroup();
		
		if (selectedPatternGroup==null)
			return;

		patternManager.removePatternGroup(selectedPatternGroup);
		
		patternGroupList.removeElement(selectedPatternGroup);
		if (patternGroupList.getSize()!=0)
			setSelectedPatternGroup((PatternGroup)patternGroupList.elementAt(0));
		else
			selectedPatternGroup = null;
	}

	/**
	 * Persist the selected PatternGroup in the database.
	 */
	public void updateSelectedPatternGroup()
	{
		PatternGroup selectedPatternGroup = getSelectedPatternGroup();
		if (selectedPatternGroup==null)
			return;

		patternManager.updatePatternGroup(selectedPatternGroup);
		
		patternGroupSelectionList.setSelectedItem(null);
		patternGroupSelectionList.setSelectedItem(selectedPatternGroup);
	}

	public PatternGroup getSelectedPatternGroup()
	{
		return (PatternGroup)patternGroupSelectionList.getSelectedItem();
	}
	
	public void setSelectedPatternGroup(PatternGroup patternGroup)
	{
		patternGroupSelectionList.setSelectedItem(patternGroup);
	}
	
	public SelectableListModel getGroupList()
	{
		return patternGroupSelectionList;
	}
}
