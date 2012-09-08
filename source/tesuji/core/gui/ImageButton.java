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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

/**
 * A button that is displayed as an image.
 * The button can have four images associated with it;
 	<ul>
 	<li> Normal - its normal state
 	<li> Rollover - used to highlight the button when the mouse moves over it
 	<li> Pressed - shown when the button is pressed
 	<li> Disabled - shown when the button is disabled
 	</ul>
 */
public class ImageButton extends Canvas implements MouseListener
{
	private static final long serialVersionUID = 8013433024185907581L;

	private OffscreenBuffer normal;
	private OffscreenBuffer disabled;
	private OffscreenBuffer rollover;
	private OffscreenBuffer pressed;
	
	private OffscreenBuffer current;

	private boolean down;
	
	private Vector<ActionListener> actionListeners = new Vector<ActionListener>();
	private String actionCommand;

	/**
	 	@param normal - The name of the image in its normal state
	 	@param rollover - The name of the image used to highlight the button when the mouse moves over it (may be null)
	 	@param pressed - The name of the image shown when the button is pressed (may be null)
	 	@param disabled - The name of the image shown when the button is disabled (may be null)
	*/
	
	public ImageButton(String normal, String rollover, String pressed, String disabled)
	{
		this.normal = new OffscreenBuffer(normal);
		if (rollover != null)
			this.rollover = new OffscreenBuffer(rollover);
		if (pressed != null)
			this.pressed = new OffscreenBuffer(pressed);
		if (disabled != null)
			this.disabled = new OffscreenBuffer(disabled);
		current = this.normal;
		addMouseListener(this);
	}
	
	public void addActionListener(ActionListener listener)
	{
		if (!actionListeners.contains(listener))
			actionListeners.addElement(listener);
	}
	
	protected void fireActionEvent()
	{
		
		if (isEnabled())
		{
			ActionEvent ev = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand());
			
			for (ActionListener l : actionListeners)
				l.actionPerformed(ev);
		}
	}

	/**
	 * @return java.lang.String
	 */
	public String getActionCommand()
	{
		return actionCommand;
	}
	
	/**
	 * Insert the method's description here.
	 * @return OffscreenBuffer
	 */
	public OffscreenBuffer getDisabled()
	{
		return disabled;
	}
	
	public Dimension getMinimumSize()
	{
		return normal.getSize();
	}

	/**
	 * @return OffscreenBuffer
	 */
	public OffscreenBuffer getNormal()
	{
		return normal;
	}
	
	public Dimension getPreferredSize()
	{
		return normal.getSize();
	}

	/**
	 * @return OffscreenBuffer
	 */
	public OffscreenBuffer getPressed()
	{
		return pressed;
	}
	
	/**
	 * @return OffscreenBuffer
	 */
	public OffscreenBuffer getRollover()
	{
		return rollover;
	}
	
	public void mouseClicked(MouseEvent ev)
	{
		fireActionEvent();
	}
	
	public void mouseEntered(MouseEvent ev)
	{
		if (rollover != null)
		{
			current = rollover;
			this.repaint();
		}
	}
	
	public void mouseExited(MouseEvent ev)
	{
		if (rollover != null)
		{
			current = normal;
			this.repaint();
	
		}	
	}
	
	public void mousePressed(MouseEvent ev)
	{
		if (pressed != null)
		{
			current = pressed;
			this.repaint();
		}
	
		down = true;
	}
	
	public void mouseReleased(MouseEvent ev)
	{
		if (pressed != null)
		{
			if (rollover != null)
				current = rollover;
			else
				current = normal;
			this.repaint();
		}
	
	//	fireActionEvent();
		down = false;
	}
	
	public void paint(Graphics g)
	{
		current.copyImage(g);
		if (down)
		{
			Dimension size = current.getSize();
			g.setColor(Color.lightGray);
			g.drawRect(0, 0, size.width-1, size.height-1);
		}
	}
	
	public void removeActionListener(ActionListener listener)
	{
		actionListeners.removeElement(listener);
	}
	
	/**
	 * @param newActionCommand java.lang.String
	 */
	public void setActionCommand(java.lang.String newActionCommand) 
	{
		actionCommand = newActionCommand;
	}
	
	/**
	 * @param newDisabled OffscreenBuffer
	 */
	public void setDisabled(OffscreenBuffer newDisabled) 
	{
		disabled = newDisabled;
		repaint();
	}
	
	public void setEnabled(boolean enabled)
	{
		if (enabled == false)
		{
			if (disabled != null)
			{
				current = disabled;
				repaint();
			}
		}
		else
		{
			if (disabled != null)
			{			
				current = normal;
				repaint();
			}
		}
	
		super.setEnabled(enabled);
	}
	
	/**
	 * @param newNormal OffscreenBuffer
	 */
	public void setNormal(OffscreenBuffer newNormal) 
	{
		if (newNormal != null)
		{
			normal = newNormal;
			repaint();
		}
	}
	
	/**
	 * @param newPressed OffscreenBuffer
	 */
	public void setPressed(OffscreenBuffer newPressed) 
	{
		pressed = newPressed;
		if (down)
			repaint();
	}
	
	/**
	 * @param newRollover OffscreenBuffer
	 */
	public void setRollover(OffscreenBuffer newRollover) 
	{
		rollover = newRollover;
		repaint();
	}
}
