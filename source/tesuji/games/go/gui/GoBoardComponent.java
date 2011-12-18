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
package tesuji.games.go.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.apache.log4j.Logger;

import tesuji.games.general.ColorConstant;
import tesuji.games.go.common.GoMove;
import tesuji.games.model.BoardChange;
import tesuji.games.model.BoardModel;

import static tesuji.games.general.ColorConstant.*;

/**
 * GUI component for displaying a Go board. It's a pretty basic implementation
 * that repaints the whole board on each change.
 */
public class GoBoardComponent
	extends JBoardComponent<GoMove>
{
	private static final long serialVersionUID = 8611898332499217291L;

	private static Logger _logger = Logger.getLogger(GoBoardComponent.class);
	
	/** Brown background color for the board */
	public static final Color BOARD_COLOR =		new Color(224,170,67);

    private int boardSize = 19;
	private int pointSize;
	private int offsetX;
	private int offsetY;
	
	private int lastX = 0;
	private int lastY = 0;
	
	public GoBoardComponent()
	{
		requestFocus();
	}
    
    /**
     * @see tesuji.games.go.client.JBoardComponent#setModel(tesuji.games.model.BoardModel)
     */
	@Override
    public void setModel(BoardModel model)
    {
        super.setModel(model);
        
        repaint();
    }
    
	@Override
    public void repaint()
    {
    	if (boardSize!=_boardModel.getBoardSize())
    	{
    		boardSize = _boardModel.getBoardSize();
    		calculateSizes(); // When the boardSize changes, the pointSize changes.
    	}
    	super.repaint();
    }
   
	@Override
    public void changeBoard(BoardChange change)
    {
		_logger.info("Change "+change.getX()+","+change.getY()+" from "+change.getOldValue()+" to "+change.getNewValue());
    	if (change.getNewValue()!=ColorConstant.EMPTY)
    	{
    		lastX = change.getX();
    		lastY = change.getX();
    	}
    	
    	super.changeBoard(change);
    }
    /**
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
	@Override
    public void paint(Graphics g)
    {
    	Graphics2D g2d = (Graphics2D)g;
    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        updateAll(g);
    }
    
	private void updateAll(Graphics g)
	{
		g.setColor(BOARD_COLOR)	;
		g.fill3DRect(offsetX,offsetY,pointSize*boardSize,pointSize*boardSize,true);
		
		updateBoard(g);
		
		if (_boardModel.get(lastX,lastY)!=ColorConstant.EMPTY)
			drawRectangle(g, lastX, lastY, Color.RED);
	}
 
 	private void updateBoard(Graphics g)
 	{
		for (int i=1; i<=boardSize; i++)
		{
			for (int j=1; j<=boardSize; j++)
			{
                updateBoardPoint(g,i,j);
			}
		}
 	}    
 	
	private void updateBoardPoint( Graphics g, int i, int j)
	{
		switch(_boardModel.get(i,j))
		{
			case EMPTY:
				drawEmptyPoint(g,i,j);
			break;
			case BLACK:
				drawBlackStone(g,i,j);
			break;
			case WHITE:
				drawWhiteStone(g,i,j);
			break;
			default:
				throw new IllegalStateException("Illegal board value: "+_boardModel.get(i,j));
		}
	}
    	
	/**
	 * @see java.awt.Component#setBounds(int, int, int, int)
	 */
	@Override
	public void	setBounds( int x, int y, int w, int h )
	{
		super.setBounds(x,y,w,h);
		calculateSizes();
	}
	
	private void calculateSizes()
	{
		Dimension d = getSize();
		if (d.width<d.height)
			pointSize = d.width/boardSize;
		else
			pointSize = d.height/boardSize;
		
		offsetX = (d.width-(pointSize*boardSize))/2;
		offsetY = (d.height-(pointSize*boardSize))/2;
	}
    
	private void drawEmptyPoint( Graphics g, int x, int y )
	{
		int startX = offsetX + (x-1)*pointSize;
		int startY = offsetY + (y-1)*pointSize;
		
		g.setColor(Color.black);
		
		startX += pointSize/2;
		startY += pointSize/2;
		
		if (x>1)
			g.drawLine(startX-pointSize/2,startY,startX,startY);
		if (x<boardSize)
			g.drawLine(startX+pointSize/2,startY,startX,startY);
		if (y>1)
			g.drawLine(startX,startY-pointSize/2,startX,startY);
		if (y<boardSize)
			g.drawLine(startX,startY+pointSize/2,startX,startY);
		
		if (boardSize==9)
		{
			if (x==5 && y==5)
				drawHandicapPoint(g,startX,startY);
		}
		else
		{
			if ((x==4 || x==10 || x==16) && (y==4 || y==10 || y==16))
				drawHandicapPoint(g,startX,startY);
		}
	}
    
	private void drawHandicapPoint(Graphics g, int x, int y)
	{
		g.fillOval(x-2,y-2,4,4);
	}
    
	private void drawBlackStone( Graphics g, int x, int y )
	{
		int startX = offsetX + (x-1)*pointSize;
		int startY = offsetY + (y-1)*pointSize;
		
		g.setColor(Color.black);
		g.fillOval(startX,startY,pointSize-1,pointSize-1);
	}
	
	private void drawWhiteStone( Graphics g, int x, int y )
	{
		int startX = offsetX + (x-1)*pointSize;
		int startY = offsetY + (y-1)*pointSize;

		g.setColor(Color.white);
		g.fillOval(startX,startY,pointSize-1,pointSize-1);
		g.setColor(Color.black);
		g.drawOval(startX,startY,pointSize-1,pointSize-1);
	}
	
	private void drawRectangle( Graphics g, int x, int y, Color color )
	{
		int startX = offsetX + (x-1)*pointSize;
		int startY = offsetY + (y-1)*pointSize;

		g.setColor(color);
		g.drawRect(startX,startY,pointSize-1,pointSize-1);
	}

}