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

import java.awt.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 	This factory class is used to create AWT GUI components from actions.

	@see Action<br><br>
*/
public class ActionGUIFactory
{
	/**
	 	Create an AWT button component that is associated with an Action.
	
	 	@param: Action action that is triggered when the button is pressed.
	 	@return: Button AWT component that will trigger the action
	*/
	public static Button createButton(Action action)
	{
		final Button button = new Button((String)action.getValue(Action.NAME));
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
	 	Create an ImageButton component that is associated with an Action.
	
	 	@param: Action action that is triggered when the button is pressed.
	 	@param normalImg The name of the imgage for the button in its 'normal' state
	 	@param rolloverImg The name of the image to be show when the mouse is over the button (may be null)
	 	@param pressedImg The name of the image to be show when the button is pressed (may be null)
 		@param disabledImg - The name of the image shown when the button is disabled (may be null)
	 	
	 	@return: ImageButton component that will trigger the action
	*/
	public static ImageButton createImageButton(Action action, String normalImg, String rolloverImg, String pressedImg, String disabledImg)
	{
		final ImageButton button = new ImageButton(normalImg, rolloverImg, pressedImg, disabledImg);
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
}
