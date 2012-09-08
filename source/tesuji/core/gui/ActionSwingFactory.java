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

import javax.swing.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 	This factory class is used to create Swing GUI components from actions.

	@see Action<br><br>
*/
public class ActionSwingFactory
{
	/**
	 	Create an Swing button component that is associated with an Action.
	
	 	@param: Action action that is triggered when the button is pressed.
	 	@return: Button Swing component that will trigger the action
	*/
	public static JButton createJButton(Action action)
	{
		final JButton button = new JButton((String)action.getValue(Action.NAME));
		button.addActionListener(action);

		// Listen to the 'enabled' property and enable/disable the button accordingly.
		action.addPropertyChangeListener(new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent event)
					{
						String propertyName = event.getPropertyName();
						if (propertyName.equals(Action.ENABLED))
						{
							boolean enabled = ((Boolean)event.getNewValue()).booleanValue();
							button.setEnabled(enabled);
						}
						button.repaint();
					}
				}
			);

		return button;
	}
	/**
	 	Create an Swing menu component that is associated with an Action.
	
	 	@param: Action action that is triggered when the menu-item is selected.
	 	@return: Swing menu-item component that will trigger the action
	*/
	public static JMenuItem createJMenuItem(Action action)
	{
		final JMenuItem menu = new JMenuItem((String)action.getValue(Action.NAME));
		menu.addActionListener(action);

		// Listen to the 'enabled' property and enable/disable the menu-item accordingly.
		action.addPropertyChangeListener(new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent event)
					{
						String propertyName = event.getPropertyName();
						if (propertyName.equals(Action.ENABLED))
						{
							boolean enabled = ((Boolean)event.getNewValue()).booleanValue();
							menu.setEnabled(enabled);
						}
						menu.repaint();
					}
				}
			);

		return menu;
	}
	/**
	 	Create an Swing text-field component that is associated with an Action.
	
	 	@param: Action action that is triggered when the button is pressed.
	 	@return: JTextField Swing component that will trigger the action
	*/
	public static JTextField createJTextField(Action action)
	{
		final JTextField field = new JTextField();
		field.addActionListener(action);

		// Listen to the 'enabled' property and enable/disable the button accordingly.
		action.addPropertyChangeListener(new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent event)
					{
						String propertyName = event.getPropertyName();
						if (propertyName.equals(Action.ENABLED))
						{
							boolean enabled = ((Boolean)event.getNewValue()).booleanValue();
							field.setEnabled(enabled);
							field.setEditable(enabled);
						}
						field.repaint();
					}
				}
			);

		return field;
	}
}
