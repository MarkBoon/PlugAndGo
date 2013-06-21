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
package tesuji.games.general;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import tesuji.core.util.MultiTypeProperties;

/**
 * This is a simple key-values container that is an extension of the Properties class.
 * 
 * The main difference is it supports attaching a PropertyChangeListener so that other
 * objects can monitor changes to the set of properties.
 * 
 * In addition it implements the PropertyChangeListener itself, so that it can be used
 * to connect to other objects to monitor changes.
 *
 */
public class GameProperties
	extends MultiTypeProperties
	implements PropertyChangeListener
{
	private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);

	/**
	 * 
	 */
	private static final long serialVersionUID = 6233934756971511202L;

	public void setProperties(List<Property> list)
	{
		for (Property p : list)
			setProperty(p.getName(), p.getValue());
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Properties#setProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public Object setProperty(String propertyName, String propertyValue)
	{
		Object oldValue = super.setProperty(propertyName, propertyValue);
		_propertyChangeSupport.firePropertyChange(propertyName, oldValue, propertyValue);
		return oldValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		setProperty(event.getPropertyName(), event.getNewValue().toString());
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		_propertyChangeSupport.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		_propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
