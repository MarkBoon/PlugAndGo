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
import java.awt.FontMetrics;
import java.awt.Graphics;

import tesuji.games.general.provider.DataProvider;

/**
 * Renderer class that renders its data as simple numbers
 */
public class NumberRenderer extends AbstractDataRenderer
{
	private int zeroWidth = -1;
	
	/**
	 * NumberRenderer constructor
	 */
	public NumberRenderer(DataProvider provider)
	{
		super(provider.getName(),provider);
	}
	
	/**
	 * @see tesuji.games.general.DataRenderer#renderData(java.awt.Graphics, int, int, int, int)
	 */
	public void renderData(int i, int j, Graphics g, int x, int y, int width, int height)
	{
		FontMetrics metrics = g.getFontMetrics();
		if (zeroWidth==-1)
			zeroWidth = metrics.charWidth('0');
		
		int xOffset = (width-zeroWidth*3)/2;
		
		int iStartX = x+xOffset;
		int iStartY = y+(height*2)/3;
		
		String strNr;
		g.setColor(Color.blue);

		int nr = _dataProvider.getData(i,j).intValue();
		
		if (nr==0)
			return;
		if (nr<0)
		{
			nr = -nr;
			g.setColor(Color.red);
		}
		strNr = ""+nr;
		if (nr<10) iStartX += zeroWidth;
		else if (nr<100) iStartX += zeroWidth/2;
		g.drawString(strNr,iStartX,iStartY);	
	}
	
	public boolean isBackgroundRenderer()
	{
		return false;
	}
}
