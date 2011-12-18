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

import javax.swing.JFrame;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tesuji.games.general.provider.DataProvider;
import tesuji.games.general.provider.DataProviderList;
import tesuji.games.general.provider.DataProviderNames;
import tesuji.games.general.renderer.DataRenderer;
import tesuji.games.general.renderer.DataRendererManager;
import tesuji.games.util.Console;

import static tesuji.games.general.ColorConstant.*;

/**
 * GUI component for displaying a Go board with data overlaid on it.
 */
public class GoDataPanel
	extends	javax.swing.JComponent
{
	private static final long serialVersionUID = 8611898332499217291L;

	/** Brown background color for the board */
	public static final Color BOARD_COLOR =		new Color(224,170,67);
	
	private static final Dimension MINIMUM_SIZE = new Dimension(200,200);

	private static final boolean WAIT_FOR_RENDERERS = true;
	
    private int _boardSize;
	private int _pointSize;
	private int _offsetX;
	private int _offsetY;

	private int rightClickX = -1;
	
	private DataProvider _boardProvider;
	
	private DataRendererManager _dataManager;
	
	public GoDataPanel(DataRendererManager manager)
	{
		_dataManager = manager;
 		_boardProvider = DataProviderList.getSingleton().getDataProvider(DataProviderNames.BOARD_PROVIDER);
		_boardSize  =_boardProvider.getBoardSize();
		
		calculateSizes();
		
		addMouseListener(new MouseAdapter());
		DataProviderList.getSingleton().addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent event)
                {
					repaint();
					if (WAIT_FOR_RENDERERS && hasActiveRenderer())
					{
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e) {}
					}
                }
				
			});
		requestFocus();
		
	}
    
    /**
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        //System.out.println("paint(g)");
        updateAll(g);
    }
    
	private void updateAll(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(BOARD_COLOR)	;
		g.fillRect(_offsetX,_offsetY,_pointSize*_boardSize,_pointSize*_boardSize);
		
		updateBackgroundRenderers(g);
		updateBoard(g);
		updateFullRenderers(g);
		updateLocalRenderers(g);
		//runEngines();
	}
 
 	private void updateBoard(Graphics g)
 	{
 		_boardProvider = DataProviderList.getSingleton().getDataProvider(DataProviderNames.BOARD_PROVIDER);
        //System.out.println("updateBoard");
		for (int i=1; i<=_boardSize; i++)
		{
			for (int j=1; j<=_boardSize; j++)
			{
                updateBoardPoint(g,i,j);
			}
		}
 	}    
 	
	private void updateBoardPoint( Graphics g, int i, int j)
	{
		byte boardData = _boardProvider.getData(i, j).byteValue();
		
		switch(boardData)
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
				throw new IllegalStateException("Illegal board value: "+boardData);
		}
	}
    	    	
	/**
	 * @see java.awt.Component#setBounds(int, int, int, int)
	 */
	public void	setBounds( int x, int y, int w, int h )
	{
		//System.out.println("GoBoard#setBounds");
		super.setBounds(x,y,w,h);
		calculateSizes();
	}
	
	@Override
	public Dimension getMinimumSize()
	{
		return MINIMUM_SIZE;
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		Dimension d = new Dimension();
		JFrame window = Console.getSingleton().getWindow();
		window.getSize(d);
		d.height -= 60;
		d.width = d.height;
		return d;
	}
	
	private void calculateSizes()
	{
		Dimension d = getSize();
		if (d.width<d.height)
			_pointSize = d.width/_boardSize;
		else
			_pointSize = d.height/_boardSize;
		
		_offsetX = (d.width-(_pointSize*_boardSize))/2;
		_offsetY = (d.height-(_pointSize*_boardSize))/2;
		
	}
    
	void drawEmptyPoint( Graphics g, int x, int y )
	{
		int iStartX = _offsetX + (x-1)*_pointSize;
		int iStartY = _offsetY + (y-1)*_pointSize;
		
		g.setColor(Color.black);
		
		iStartX += _pointSize/2;
		iStartY += _pointSize/2;
		
		if (x>1)
			g.drawLine(iStartX-_pointSize/2,iStartY,iStartX,iStartY);
		if (x<_boardSize)
			g.drawLine(iStartX+_pointSize/2,iStartY,iStartX,iStartY);
		if (y>1)
			g.drawLine(iStartX,iStartY-_pointSize/2,iStartX,iStartY);
		if (y<_boardSize)
			g.drawLine(iStartX,iStartY+_pointSize/2,iStartX,iStartY);
		
		if (_boardSize==9)
		{
			if (x==5 && y==5)
				drawHandicapPoint(g,iStartX,iStartY);
		}
		else
		{
			if ((x==4 || x==10 || x==16) && (y==4 || y==10 || y==16))
				drawHandicapPoint(g,iStartX,iStartY);
		}
	}
    
	void drawHandicapPoint(Graphics g, int x, int y)
	{
		g.fillRect(x-1,y-1,3,3);
	}
    
	void drawBlackStone( Graphics g, int x, int y )
	{
		int iStartX = _offsetX + (x-1)*_pointSize;
		int iStartY = _offsetY + (y-1)*_pointSize;
		
		g.setColor(Color.black);
		g.fillOval(iStartX,iStartY,_pointSize-1,_pointSize-1);
	}
	
	void drawWhiteStone( Graphics g, int x, int y )
	{
		int iStartX = _offsetX + (x-1)*_pointSize;
		int iStartY = _offsetY + (y-1)*_pointSize;

		g.setColor(Color.white);
		g.fillOval(iStartX,iStartY,_pointSize-1,_pointSize-1);
		g.setColor(Color.black);
		g.drawOval(iStartX,iStartY,_pointSize-1,_pointSize-1);
	}
	
	private void handleMouseClick(java.awt.event.MouseEvent event)
	{
		requestFocus();
		int x = ((event.getX()-_offsetX)/_pointSize)+1;
		int y = ((event.getY()-_offsetY)/_pointSize)+1;
		
		if (x>0 && y>0 && x<=_boardSize && y<=_boardSize)
		{
			ListModel list = _dataManager.getRendererList();
			for (int i=0; i<list.getSize(); i++)
			{
	            DataRenderer renderer = (DataRenderer)list.getElementAt(i);
				if (renderer.isActive())
	                renderer.getDataProvider().fillData(x,y);
			}
		}
	}

//	private void handleRightClick(MouseEvent event)
//	{
//		int x = ((event.getX()-_offsetX)/_pointSize)+1;
//		int y = ((event.getY()-_offsetY)/_pointSize)+1;
//		if (x==rightClickX)
//			rightClickX = -1;
//		else
//		{
//			rightClickX = x;
//			
//			DataRendererList renderers = DataRendererList.getRenderers();
//			for (int i=0; i<renderers.size(); i++)
//			{
//                DataRenderer renderer = (DataRenderer)renderers.getElementAt(i);
//				if (renderer.isActive())
//                    renderer.getDataProvider().fillData(x,y);
//			}
//			TacticsProviderList readers = TacticsProviderList.getReaders();
//			for (int i=0; i<readers.size(); i++)
//			{
//				TacticsProvider reader = (TacticsProvider)readers.getElementAt(i);
//				if (reader.isActive())
//					reader.read(x,y,boardModel.getBoardPoint(x,y));
//			}
//		}
//		
//		repaint();
//	}
    	
	private void updateBackgroundRenderers(Graphics g)
	{
        //System.out.println("updateBackgroundRenderers");
		ListModel rendererList = _dataManager.getRendererList();
		for (int k=0; k<rendererList.getSize(); k++)
		{
			DataRenderer renderer = (DataRenderer)rendererList.getElementAt(k);
			DataProvider dataProvider = renderer.getDataProvider();
			if (renderer.isActive() && dataProvider.isFullProvider() && renderer.isBackgroundRenderer())
			{
				for (int i=1; i<=_boardSize; i++)
				{
					for (int j=1; j<=_boardSize; j++)
					{
						int startX = _offsetX + (i-1)*_pointSize;
						int startY = _offsetY + (j-1)*_pointSize;
						renderer.renderData(i,j,g,startX,startY,_pointSize,_pointSize);
					}
				}
			}
		}
	}
    
	private void updateFullRenderers(Graphics g)
	{
        //System.out.println("updateFullRenderers");
		ListModel rendererList = _dataManager.getRendererList();
		for (int k=0; k<rendererList.getSize(); k++)
		{
			DataRenderer renderer = (DataRenderer)rendererList.getElementAt(k);
			DataProvider dataProvider = renderer.getDataProvider();
			if (renderer.isActive() && dataProvider.isFullProvider() && !renderer.isBackgroundRenderer())
			{
				for (int i=1; i<=_boardSize; i++)
				{
					for (int j=1; j<=_boardSize; j++)
					{
						int startX = _offsetX + (i-1)*_pointSize;
						int startY = _offsetY + (j-1)*_pointSize;
						renderer.renderData(i,j,g,startX,startY,_pointSize,_pointSize);
					}
				}
			}
		}
	}
	
	private boolean hasActiveRenderer()
	{
		ListModel rendererList = _dataManager.getRendererList();
		for (int k=0; k<rendererList.getSize(); k++)
		{
			DataRenderer renderer = (DataRenderer)rendererList.getElementAt(k);
			if (renderer.isActive())
				return true;
		}
		return false;
	}
    
	private void updateLocalRenderers(Graphics g)
	{
		if (rightClickX==-1)
			return;
		
		ListModel rendererList = _dataManager.getRendererList();
		for (int k=0; k<rendererList.getSize(); k++)
		{
			DataRenderer renderer = (DataRenderer)rendererList.getElementAt(k);
			DataProvider dataProvider = renderer.getDataProvider();
			if (renderer.isActive() && !dataProvider.isFullProvider() && !renderer.isBackgroundRenderer())
			{
				for (int i=1; i<=_boardSize; i++)
				{
					for (int j=1; j<=_boardSize; j++)
					{
						int startX = _offsetX + (i-1)*_pointSize;
						int startY = _offsetY + (j-1)*_pointSize;
						renderer.renderData(i,j,g,startX,startY,_pointSize,_pointSize);
					}
				}
			}
		}
	}
	    
	private class MouseAdapter extends java.awt.event.MouseAdapter
	{
		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked( java.awt.event.MouseEvent event )
		{
//			if (MoveGeneratorHelper.isGenerating())
//			{
//				//System.out.println("System busy");
//				return;
//			}
			
			//System.out.println("Clicked on GoBoardComponent at: "+event.getX()+","+event.getY()+" with modifiers: "+event.getModifiers());
			if (!event.isMetaDown())
				GoDataPanel.this.handleMouseClick(event);
//			else
//				GoDataPanel.this.handleRightClick(event);
		}
	}    
}