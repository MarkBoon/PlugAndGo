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

package tesuji.games.general.renderer;

import java.awt.Color;
import java.util.Hashtable;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import tesuji.games.general.provider.DataProvider;
import tesuji.games.general.provider.DataProviderList;
import tesuji.games.general.provider.DataProviderNames;

/**
 * Front-end manager of a list of DataRenderers. It listens to the list of DataProviders in the
 * singleton class DataProviderList and updates the list of renderers accordingly.
 */
public class DataRendererManager
{
	public static final Color BOARD_COLOR =		new Color(224,170,67);

	private final DefaultListModel _rendererList = new DefaultListModel();
	private final Hashtable<String, DataRenderer> _rendererTable = new Hashtable<String, DataRenderer>();
	
	public DataRendererManager()
	{
		updateRendererList();
		DataProviderList.getSingleton().addListDataListener(new ListDataListener()
			{

				public void contentsChanged(ListDataEvent event)
                {
					updateRendererList();
                }

				public void intervalAdded(ListDataEvent event)
                {
					updateRendererList();
                }

				public void intervalRemoved(ListDataEvent event)
                {
					updateRendererList();
                }			
			});
	}
	
	private void updateRendererList()
	{
		_rendererList.removeAllElements();
		ListModel providerList = DataProviderList.getSingleton();
		for (int i=0; i<providerList.getSize(); i++)
		{
			DataProvider provider = (DataProvider)providerList.getElementAt(i);
			DataRenderer renderer;
			if (provider.getName().equals(DataProviderNames.BOARD_PROVIDER))
				renderer = null;
			else if (provider.getDataClass().equals(Float.class))
				renderer = new HueRenderer(provider,BOARD_COLOR,100,1);
			else if (provider.getDataClass().equals(Double.class))
				renderer = new HueRenderer(provider,BOARD_COLOR,100,1);
			else
				renderer = new NumberRenderer(provider);
			if (renderer!=null)
			{
				DataRenderer oldRenderer = _rendererTable.put(provider.getName(), renderer);
				if (oldRenderer!=null)
					renderer.setActive(oldRenderer.isActive());
				_rendererList.addElement(renderer);
			}
		}
	}
	
	public ListModel getRendererList()
	{
		return _rendererList;
	}
}
