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
package tesuji.core.util;

import javax.swing.ComboBoxModel;
import javax.swing.ListSelectionModel;

/**
	SelectableListModel
	
	Purpose:
	========
	Very often one needs to represent a list of objects and select one of them. Basically this is represented by the
	ComboBoxModel in Swing. However it would be nice to be able to listen to selection-events that happen when
	the selected item changes. The ComboBoxModel doesn't provide this, but the ListSelectionModel does.
	Instead of having to manage two models separately, it is more convenient to have a ListSelectionModel associated
	with the ComboBoxModel so that whenever the selected item changes because of a setSelectedItem call, the
	selection in the ListSelectionModel updates its selection automatically as well. Vice versa, whenever the
	selection in the ListSelectionModel changes one would want the selected item in the ComboBoxModel to change
	its selection to that particular item as well.
	
	In order not to have to implement this association between a ComboBoxModel and a ListSelectionModel over and
	over again, this interface is defined plus a default implementation of it in the DefaultSelectableListModel class.
	
	Note:
	=====
	Currently it only supports single selections, but it's considered to be extended to handle multiple selections
	as well. In order not to restrict it too much from the start, ListSelectionModel is used rather than
	SingleListSelectionModel. The methods getSelectedIndex and setSelectedIndex are defined as well anyway to
	make it possible to treat it as a single-selection list when you want to. The listeners that must be registered
	must be ListSelectionListeners however, rather than ChangeListeners.
	
	@see DefaultSelectableListModel
*/

public interface SelectableListModel
	extends ListSelectionModel,
			ComboBoxModel
{
	public int getSelectedIndex();
	public void setSelectedIndex(int index);
}
