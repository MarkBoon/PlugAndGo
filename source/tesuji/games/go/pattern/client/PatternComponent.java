/**
 *	Product: Tesuji Software Go Library.<br><br>
 *
 *	<font color="#CC6600"><font size=-1>
 *	Copyright (c) 2001-2004 Tesuji Software B.V.<br>
 *	All rights reserved.<br><br>
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a
 *	copy of this software and associated documentation files (the
 *	"Software"), to deal in the Software without restriction, including
 *	without limitation the rights to use, copy, modify, merge, publish,
 *	distribute, and/or sell copies of the Software, and to permit persons
 *	to whom the Software is furnished to do so, provided that the above
 *	copyright notice(s) and this permission notice appear in all copies of
 *	the Software and that both the above copyright notice(s) and this
 *	permission notice appear in supporting documentation.<br><br>
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 *	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.<br><br>
 *
 *	Except as contained in this notice, the name of a Tesuji Software
 *	shall not be used in advertising or otherwise to promote the sale, use
 *	or other dealings in this Software without prior written authorization
 *	of Tesuji Software.<br><br>
 *	<font color="#00000"><font size=+1>
 */
package tesuji.games.go.pattern.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingUtilities;

import tesuji.games.go.pattern.common.Pattern;
import tesuji.games.go.pattern.common.PatternCondition;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;

/**
 *	A GUI component that displays a Go pattern.
 */
public class PatternComponent
	extends Component
	implements PropertyChangeListener
{
	private static final long serialVersionUID = -4147885678572918195L;
	/** Background color of the pattern. Some kind of board-color, or other. */
	public static final Color BOARD_COLOR =		new Color(254,67,124);
	/** Background color of the pattern when selected. */
	public static final Color SELECTED_COLOR =	BOARD_COLOR.darker();

	private Pattern pattern;
	private int pointSize;
	private int offsetX;
	private int offsetY;
	private boolean editable;
	private boolean selected;

	/**
	 * PatternComponent constructor
	 * 
	 * @param pattern to be displayed
	 */
	public PatternComponent(Pattern pattern)
	{
		this(false);
		setPattern(pattern);
	}

	/**
	 * PatternComponent constructor
	 * 
	 * @param pattern to be displayed
	 * @param editable flag indicating whethermous-clicks will modify the pattern
	 */
	public PatternComponent(Pattern pattern, boolean editable)
	{
		this(editable);
		setPattern(pattern);
	}
	
	/**
	 * PatternComponent constructor
	 * 
	 * @param editable flag indicating whethermous-clicks will modify the pattern
	 */
	public PatternComponent(boolean editable)
	{
		this.editable = editable;
		if (editable)
		{
			addMouseListener( new java.awt.event.MouseAdapter()
					{
						public void
						mouseClicked( java.awt.event.MouseEvent event )
						{
							if (SwingUtilities.isLeftMouseButton(event) && !event.isShiftDown())
								handleLeftClick(event);
							else if (SwingUtilities.isRightMouseButton(event) || event.isShiftDown())
								handleRightClick(event);
						}
					}
				);
		}
		setSize(getMinimumSize());
		calculateSizes();
	}
	
	protected void calculateSizes()
	{
		if (pattern!=null)
		{
			Dimension d = getSize();
			int width = pattern.getWidth();
			int height = pattern.getHeight();
			if ((d.width/width)<(d.height/height))
				pointSize = (d.width-4)/width;
			else
				pointSize = (d.height-4)/height;

			int totalWidth = pointSize*width;
			int totalHeight = pointSize*height;
			offsetX = (d.width-totalWidth)/2;
			offsetY = (d.height-totalHeight)/2;
		}
	}
	
	private void drawBlackStone(Graphics g, int x, int y)
	{
		int startX = offsetX + x*pointSize;
		int startY = offsetY + y*pointSize;

		g.setColor(selected?SELECTED_COLOR:BOARD_COLOR);
		g.fillRect(startX,startY,pointSize,pointSize);
		g.setColor(Color.black);
		g.fillOval(startX,startY,pointSize-1,pointSize-1);
	}
	
	private void drawEmptyPoint(Graphics g, int x, int y)
	{
		int startX = offsetX + x*pointSize;
		int startY = offsetY + y*pointSize;

		g.setColor(selected?SELECTED_COLOR:BOARD_COLOR);
		g.fillRect(startX,startY,pointSize,pointSize);

		startX += pointSize/2;
		startY += pointSize/2;

		g.setColor(Color.black);
		if (x>0 || !pattern.hasLeftEdge())
			g.drawLine(startX-pointSize/2,startY,startX,startY);
//		if (x<pattern.getWidth()-1)
			g.drawLine(startX+pointSize/2,startY,startX,startY);
		if (y>0 || !pattern.hasTopEdge())
			g.drawLine(startX,startY-pointSize/2,startX,startY);
//		if (y<pattern.getHeight()-1)
			g.drawLine(startX,startY+pointSize/2,startX,startY);
	}
	
	private void drawLetter(Graphics g, int x, int y, char c)
	{
		int startX = offsetX + x*pointSize+pointSize/3;
		int startY = offsetY + y*pointSize+(pointSize*2)/3;
		
		g.setColor(Color.blue);		
		g.drawString(""+c,startX,startY);
	}
	
	private void drawNoCare(Graphics g, int x, int y)
	{
		int startX = offsetX + x*pointSize;
		int startY = offsetY + y*pointSize;

		g.setColor(selected?SELECTED_COLOR:BOARD_COLOR);
		g.fillRect(startX,startY,pointSize,pointSize);
		g.setColor(Color.black);
	}
	
	private void drawPoint(Graphics g, int x, int y)
	{
		byte data = pattern.getPoint(x,y);

		switch(data)
		{
			case EMPTY:
				drawEmptyPoint(g,x,y);
			break;
			case BLACK:
				drawBlackStone(g,x,y);
			break;
			case WHITE:
				drawWhiteStone(g,x,y);
			break;
			default:
				drawNoCare(g,x,y);
		}
	}
	
	private void drawWhiteStone(Graphics g, int x, int y)
	{
		int startX = offsetX + x*pointSize;
		int startY = offsetY + y*pointSize;

		g.setColor(selected?SELECTED_COLOR:BOARD_COLOR);
		g.fillRect(startX,startY,pointSize,pointSize);
		g.setColor(Color.white);
		g.fillOval(startX,startY,pointSize-1,pointSize-1);
		g.setColor(Color.black);
		g.drawOval(startX,startY,pointSize-1,pointSize-1);
	}
	
	/**
	 * @see java.awt.Component#getMinimumSize()
	 */
	public Dimension getMinimumSize()
	{
		if (editable)
		{
			return new Dimension(200,200);
		}
		else
			return new Dimension(100,100);
	}
	
	/**
	 * @see java.awt.Component#getPreferredSize()
	 */
	public Dimension getPreferredSize()
	{
		return getMinimumSize();
	}

	/**
	 * @return the pattern being displayed
	 */
	public Pattern getPattern()
	{
		return pattern;
	}
	
	/**
	 * Left mouse-button is for Black
	 * 
	 * @param event
	 */
	private void handleLeftClick(MouseEvent event)
	{
		if (pattern==null)
			return;
		
		requestFocus();
		int x = ((event.getX()-offsetX)/pointSize);
		int y = ((event.getY()-offsetY)/pointSize);
		
		if (x>=0 && y>=0 && x<pattern.getWidth() && y<pattern.getHeight())
		{
			if (event.isControlDown())
			{
				if (pattern.getBlackX()==x && pattern.getBlackY()==y)
				{
					pattern.setBlackX(UNDEFINED_COORDINATE);
					pattern.setBlackY(UNDEFINED_COORDINATE);
				}
				else
				{
					pattern.setBlackX(x);
					pattern.setBlackY(y);
				}
			}
			else if (event.isAltDown())
			{
				if (pattern.getUserX()==x && pattern.getUserY()==y)
				{
					pattern.setUserX(UNDEFINED_COORDINATE);
					pattern.setUserY(UNDEFINED_COORDINATE);
				}
				else
				{
					pattern.setUserX(x);
					pattern.setUserY(y);
				}
			}
			else
			{
				byte newValue;
				byte oldValue = pattern.getPoint(x,y);
				if (oldValue==NOCARE)
					newValue = BLACK;
				else if (oldValue==BLACK || oldValue==WHITE)
					newValue = EMPTY;
				else
					newValue = NOCARE;

				pattern.setPoint(x,y,newValue);
			}
				
			repaint();
		}
	}
	
	/**
	 * Right mouse-button is for White
	 * 
	 * @param event
	 */
	private void handleRightClick(MouseEvent event)
	{
		if (pattern==null)
			return;
		
		requestFocus();
		int x = ((event.getX()-offsetX)/pointSize);
		int y = ((event.getY()-offsetY)/pointSize);
		
		if (x>=0 && y>=0 && x<pattern.getWidth() && y<pattern.getHeight())
		{
			if (event.isControlDown())
			{
				if (pattern.getWhiteX()==x && pattern.getWhiteY()==y)
				{
					pattern.setWhiteX(UNDEFINED_COORDINATE);
					pattern.setWhiteY(UNDEFINED_COORDINATE);
				}
				else
				{
					pattern.setWhiteX(x);
					pattern.setWhiteY(y);
				}
			}
			else
			{
				byte newValue;
				byte oldValue = pattern.getPoint(x,y);
				if (oldValue==NOCARE)
					newValue = WHITE;
				else if (oldValue==BLACK || oldValue==WHITE)
					newValue = EMPTY;
				else
					newValue = NOCARE;

				pattern.setPoint(x,y,newValue);
			}
			
			repaint();
		}
	}
	
	/**
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g)
	{
    	Graphics2D g2d = (Graphics2D)g;
    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.fillRect(0,0,getSize().width,getSize().height);

		if (pattern!=null)
		{
			for (int x=0; x<pattern.getWidth(); x++)
			{
				for (int y=0; y<pattern.getHeight(); y++)
				{
					drawPoint(g,x,y);
				}
			}
			int blackX = pattern.getBlackX();
			int blackY = pattern.getBlackY();
			int whiteX = pattern.getWhiteX();
			int whiteY = pattern.getWhiteY();
			int userX = pattern.getUserX();
			int userY = pattern.getUserY();
			
			if (blackX==whiteX && blackY==whiteY && blackX!=UNDEFINED_COORDINATE)
				drawLetter(g,blackX,blackY,'@');
			else
			{
				if (blackX!=UNDEFINED_COORDINATE)
					drawLetter(g,blackX,blackY,'X');
				if (whiteX!=UNDEFINED_COORDINATE)
					drawLetter(g,whiteX,whiteY,'O');
			}
			if (userX!=UNDEFINED_COORDINATE)
				drawLetter(g,userX,userY,'#');
			
			for (int i=0; i<pattern.getConditionList().size(); i++)
			{
				PatternCondition condition = pattern.getConditionList().get(i);
				drawLetter(g,condition.getX(), condition.getY(),(char)('1'+i));
			}
		}
	}
	
	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		calculateSizes();
		repaint();
	}
	
	/**
	 * When the component resizes, new dimension need to be recalculcated.
	 * 
	 * @see java.awt.Component#setBounds(int, int, int, int)
	 */
	public void setBounds( int x, int y, int w, int h )
	{
		super.setBounds(x,y,w,h);
		calculateSizes();
	}
	
	/**
	 * @param p pattern to be displayed
	 */
	public void setPattern(Pattern p)
	{
		if (pattern!=null)
			pattern.removePropertyChangeListener(this);
		pattern = p;
		if (pattern!=null)
			pattern.addPropertyChangeListener(this);
		calculateSizes();
//		repaint();
		if (getGraphics()!=null)
			paint(getGraphics());
	}
	
	/**
	 * Set whether the pattern is the 'selected' pattern,
	 * in which case it's painted darker.
	 * 
	 * @param flag
	 */
	public void setSelected(boolean flag)
	{
		selected = flag;
		invalidate();
		repaint();
	}
}
