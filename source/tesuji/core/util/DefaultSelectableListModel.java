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

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


/**
	This is a default implementation of a SelectableListModel that also incorporates a ListSelectionModel.
	See the documentation of SelectableListModel as well.
	
	Apart from the methods defined by the interface it also implements two more methods for
	convenience: indexOf() and select()
	
	@see ListModel
	@see ListSelectionModel
	@see SelectableListModel
*/
public class DefaultSelectableListModel
	extends DefaultListSelectionModel
	implements	SelectableListModel
{
	private static final long serialVersionUID = -6407432938747024010L;

	protected ListModel _listManager;
	private Vector<ListDataListener> _listDataListeners = new Vector<ListDataListener>(); // There's always at least one listener.

	/**
		Default constructor for DefaultSelectableListModel. It uses a DefaultListModel to manage the list.
	*/
	public DefaultSelectableListModel()
	{
		this(new DefaultListModel());
	}
	/**
		This SelectableListModel delegates the list functionality to the list-model that is passed
		in the constructor.
	*/
	public DefaultSelectableListModel(ListModel listManager)
	{
		_listManager = listManager;

		// By default the selection only allows single selections.
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Add a listener to its own data to ensure the selection-model doesn't have an
		// index selected that is greater than the list-size.
		addListDataListener( new ListDataListener()
				{
					public void
					contentsChanged(ListDataEvent event)
					{
						if (getSelectedIndex()>=getSize())
							setSelectionInterval(getSize()-1,getSize()-1);
					}
					
					public void intervalAdded(ListDataEvent event) { contentsChanged(event); }
					public void intervalRemoved(ListDataEvent event)
					{
	                    if (getSelectedIndex()<0) // Just in case the selected item gets deleted.
							clearSelection();
						contentsChanged(event);
					}

				}
			);
	}
	/**
		Add a listener for changes to the collection.
	*/	
	public void addListDataListener(ListDataListener listener)
	{
		if (_listManager!=null)
			_listManager.addListDataListener(listener);
		_listDataListeners.addElement(listener);
	}
	// Implementation of the ListModel by delegating it to the _listManager.
	
	/**
		@param index the row to be retrieved
		@returns the object at the row 'index'
	*/
	public Object getElementAt(int index)
	{
		if (index>=_listManager.getSize())
			return null;
		
		return _listManager.getElementAt(index);
	}
	/**
		@return the underlying list-model.
	*/	
	public ListModel getListModel()
	{
		return _listManager;
	}
	/**
		@return the index of the currently selected item. Returns -1 when the selection is empty.
	*/	
	public int getSelectedIndex()
	{
		if (getMinSelectionIndex()<0 || _listManager==null || _listManager.getSize()<=0)
			return -1;
			
		for (int i=getMinSelectionIndex() ; i<=getMaxSelectionIndex(); i++)
			if (isSelectedIndex(i))
				return i;

		// Should probably never come here.
		return -1;
	}
	/**
		Implementation getSelectedItem of the ComboBox model.
	*/
	public Object getSelectedItem()
	{
		int index = getSelectedIndex();
		if (index<0)
			return null;
		
		return getElementAt(index);
	}
	/**
		@return an array with selected objects.
		Usually only useful after calling setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	*/
	public Object[] getSelectedItems()
	{
		int size = 0;
		for (int i=getMinSelectionIndex() ; i<=getMaxSelectionIndex(); i++)
			if (isSelectedIndex(i))
				size++;
		
		Object[] array = new Object[size];
		
		int index = 0;
		for (int i=getMinSelectionIndex() ; i<=getMaxSelectionIndex(); i++)
			if (isSelectedIndex(i))
				array[index++] = getElementAt(i);
				index++;
		
		return array;
	}
	/**
		@returns the size of the collection.
	*/
	public int getSize()
	{
		if (_listManager==null)
			return 0;

		return _listManager.getSize();
	}
	/**
		Search for an object in the list and return its index. Returns -1 when it's not found.
		This method uses the equals method on the object to compare.
	*/
	public int indexOf(Object o)
	{
		int selectedIndex = -1;
		for (int i=0; i<getSize(); i++)
		{
			if (o.equals(getElementAt(i)))
			{
				selectedIndex = i;
				break;
			}
		}
		return selectedIndex;
	}
	/**
		Remove a listener for changes to the collection.
	*/	
	public void removeListDataListener(ListDataListener listener)
	{
		if (_listManager!=null)
			_listManager.removeListDataListener(listener);
		_listDataListeners.removeElement(listener);
	}
	/**
		This method needs to be called for garbage-collection. As long as the model has
		a reference to the listener, the table-model and all GUI components related to
		it will not be garbage-collected.
	*/
	@SuppressWarnings("unchecked")
    public void removeListModel()
	{
		if (_listManager!=null)
		{
			for (Enumeration e = _listDataListeners.elements(); e.hasMoreElements();)
			{
				ListDataListener l = (ListDataListener) e.nextElement();
				_listManager.removeListDataListener(l);
			}
		}
		_listManager = null;
	}
	/**
		This is a convenience method through which an item can be selected by passing a String
		that matches the toString() method of the objects in the list.
		If the item cannot be found, the selection is reset.
	*/
	public void select(String name)
	{
		if (name==null)
		{
			clearSelection();
			return;
		}
		
		boolean bFound = false;
		
		for (int i=0; i<_listManager.getSize(); i++)
		{
			if (name.equals(_listManager.getElementAt(i).toString()))
			{
				setSelectionInterval(i,i);
				bFound = true;
				break;
			}
		}
		if (!bFound)
			clearSelection();
	}
	/**
		Occasionally one might want to change the collection behind the model, for example by reusing the same
		list or table layout for different lists of data.
	*/
	@SuppressWarnings("unchecked")
    public void setListModel(ListModel listManager)
	{
		// Make sure that the listeners to the previous collection are removed...
		removeListModel();
		
		_listManager = listManager;
		ListDataEvent event = new ListDataEvent(this,0,_listManager.getSize()-1,ListDataEvent.CONTENTS_CHANGED);

		// and added as listeners to the new collection.
		for (ListDataListener listener : _listDataListeners)
		{
			_listManager.addListDataListener(listener);
			listener.contentsChanged(event);
		}
	
		//
		// Clear selection model 
		//

		clearSelection();
	}
	/**
		Set the item at position 'index' to be the selected item.
	*/
	public void setSelectedIndex(int index)
	{
		setSelectionInterval(index,index);
	}
	/**
		Implementation setSelectedItem of the ComboBox model.
	*/
	public void	setSelectedItem(Object o)
	{
		if (o==null)
		{
			clearSelection();
			return;
		}
		
		for (int i=0; i<_listManager.getSize(); i++)
		{
			if (o.equals(_listManager.getElementAt(i)))
			{
				setSelectionInterval(i,i);
				break;
			}
		}
	}
}
