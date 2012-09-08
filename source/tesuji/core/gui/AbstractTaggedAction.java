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
package tesuji.core.gui;

import java.util.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
	Abstract implementation of the TaggedAction interface.
	Subclasses need only to implement the actionPerformed mehod.<br><br>

	@see TaggedAction
	@see ActionManager<br><br>
*/
public abstract class AbstractTaggedAction
	implements TaggedAction
{
	private Vector<PropertyChangeListener> propertyChangeListeners = new Vector<PropertyChangeListener>();
	private Hashtable<String,Object> values = new Hashtable<String,Object>();

	private boolean enabled;

	/**
	 * Create an AbstractNamedAction with a certain tag for the ActionManager
	 */
	public AbstractTaggedAction(String actionTag)
	{
		setTag(actionTag);
		setEnabled(true);
	}
	/**
	 * @param actionTag The actions 'id'
	 * @param name The 'title' or visible text for this action
	 */
	public AbstractTaggedAction(String actionTag, String name)
	{
		setTag(actionTag);
		setName(name);
		setEnabled(true);
	}
	/**
	 	Add a property-change listener
	
	 	@param: PropertyChangeListener listener to changes in the NamedAction
	*/
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		if (!propertyChangeListeners.contains(listener))
			propertyChangeListeners.addElement(listener);
	}
	/**
	 	Notify all listeners of a property-change.
	
	 	@param: PropertyChangeEvent event that describes the change.
	*/
	private void firePropertyChange(PropertyChangeEvent event)
	{
		for (PropertyChangeListener listener : propertyChangeListeners)
			listener.propertyChange(event);
	}
	/**
	 	@return: String the name of the action as it's used by the ActionManager
	*/
	public String getName()
	{
		return (String) getValue(NAME);
	}
	/**
	 	@return: String the tag of the action as it's used by the ActionManager
	*/
	public String getTag()
	{
		return (String) getValue(TAG);
	}
	/**
		Get a property value from an Action
	
	 	@param: String name of the property for which to get the value.
	 	@return: the object that represents the value.
	*/
	public Object getValue(String propertyName)
	{
		return values.get(propertyName);
	}
	
	/**
	 	@return: whether the action is enabled or not.
	*/
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 	Change the value of a property in the Action
	
	 	@param: String name of the property to change
	 	@param: Object representing the new value.	
	*/
	public void putValue(String name, Object newValue)
	{
		Object oldValue = values.get(name);
		values.put(name,newValue);
		PropertyChangeEvent event = new PropertyChangeEvent(this,name,oldValue,newValue);
		firePropertyChange(event);
	}
	/**
	 	@param: PropertyChangeListener listener to be removed.
	*/
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChangeListeners.removeElement(listener);
	}
	
	/**
	 	Set whether the action is enabled ot not.
	
	 	@param: boolean value whether the action is enabled or not.
	*/
	public void setEnabled(boolean newEnabled)
	{
		boolean oldValue = isEnabled();
		enabled = newEnabled;
		PropertyChangeEvent event = new PropertyChangeEvent(this,Action.ENABLED, new Boolean(oldValue),new Boolean (newEnabled));
		firePropertyChange(event);
	}
	
	/**
	 	Change the name of the action.
	
	 	@param: String new name of the action
	*/
	public void setName(String name)
	{
		putValue(Action.NAME,name);
	}
	
	/**
	 	Change the tag-name of the action.
	
	 	@param: String new tag of the action
	*/
	public void setTag(String tag)
	{
		putValue(Action.TAG,tag);
	}
	
	/**
	 * @return name of the action
	 */
	public String toString()
	{
		return "Action "+getName();
	}
}
