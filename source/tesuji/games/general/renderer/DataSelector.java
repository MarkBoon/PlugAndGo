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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tesuji.core.util.ComboBox;
import tesuji.core.util.DefaultSelectableListModel;

/**
 * A component displaying a combo-box with a list of available renderers.
 */
public class DataSelector
	extends JPanel
{
	private static final long serialVersionUID = -6814353455008597618L;
	
    private DefaultSelectableListModel model;
	
	public DataSelector(DataRendererManager manager)
	{
		model = new DefaultSelectableListModel(manager.getRendererList());
		
		JLabel label = new JLabel("Data: ");
		ComboBox comboBox = new ComboBox(model);
		add(label);
		add(comboBox);
		
		model.addListSelectionListener( new ListSelectionListener()
				{
					public void valueChanged(ListSelectionEvent event)
					{
						if (!model.isSelectionEmpty())
						{
							DataRenderer renderer = (DataRenderer)model.getSelectedItem();
                            renderer.setActive(!renderer.isActive());
							model.clearSelection();
						}
					}
				}
			);
	}
	
	public ListSelectionModel getModel()
	{
		return model;
	}
}
