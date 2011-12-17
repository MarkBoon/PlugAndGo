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
package tesuji.games.general.provider;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A static list with DataProviders. This is used so that many components of an
 * engine can conveniently provide a DataProvider without having to keep track 
 * of all the different ones on the GoEngine level.
 * 
 * Changes to the list can be monitored by registering ListModelListeners.
 */
public class DataProviderList
	extends DefaultListModel
{
	private static final long serialVersionUID = -2329022943507126592L;
	
	private static DataProviderList singleton;
	
	private Hashtable<String,DataProvider> providerTable = new Hashtable<String,DataProvider>();
	private Vector<ChangeListener> listenerList = new Vector<ChangeListener>();
	
	/**
	 * @return the list of DataProviders
	 */
	public static DataProviderList getSingleton()
	{
		if (singleton==null)
			singleton = new DataProviderList();
		
		return singleton;
	}
	
	/**
	 * Add a DataProvider
	 * 
	 * @param provider
	 */
	public void addDataProvider(DataProvider provider)
	{
		DataProvider oldProvider = providerTable.put(provider.getName(),provider);
		if (oldProvider!=null)
			removeElement(oldProvider);
		addElement(provider);

		fireContentsChanged(this, 0, getSize());
	}

	/**
	 * Remove a DataProvider 
	 * 
	 * @param provider
	 */
	public void removeDataProvider(DataProvider provider)
	{
		removeElement(provider);
		providerTable.remove(provider.getName());
	}
	
	public final DataProvider getDataProvider(String name)
	{
		return providerTable.get(name);
	}
	
	public void addChangeListener(ChangeListener listener)
	{
		if (!listenerList.contains(listener))
			listenerList.addElement(listener);
	}
	
	public void removeChangeListener(ChangeListener listener)
	{
		listenerList.removeElement(listener);
	}
	
	public void fireDataChange()
	{
		ChangeEvent event = new ChangeEvent(this);
		for (ChangeListener listener : listenerList)
			listener.stateChanged(event);
	}
}
