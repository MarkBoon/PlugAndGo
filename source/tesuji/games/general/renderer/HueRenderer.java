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
import java.awt.Graphics;

import tesuji.games.general.provider.DataProvider;

/**
 * Renderer that renders a square of a certain color in a certain hue based on the data it was constructed for.
 */
public class HueRenderer extends AbstractDataRenderer
{
	private Color _color;
	private int _cutoff;
	private int _factor;
	
	/**
	 * HueRenderer constuctor
	 * 
	 * @param name
	 * @param provider
	 * @param color
	 */
	public HueRenderer(DataProvider provider, Color color, int cap, int factor)
	{
		super(provider.getName(),provider);
		_color = color;
		_cutoff = cap;
		_factor = factor;
	}
	
	/**
	 * @see tesuji.games.general.provider.DataRenderer#renderData(int, int, java.awt.Graphics, int, int, int, int)
	 */
	public void renderData(int i,int j, Graphics g, int x, int y, int width, int height)
	{
		int data = _dataProvider.getData(i,j).intValue();

		data *= _factor;
		
		if (data>_cutoff)
			data = _cutoff;
		if (data<-_cutoff)
			data = -_cutoff;
		
		int red = _color.getRed()-data;
		int green = _color.getGreen()-data;
		int blue = _color.getBlue()-data;
		
		if (red<0)
			red = 0;
		if (green<0)
			green = 0;
		if (blue<0)
			blue = 0;
		
		if (red>255)
			red = 255;
		if (green>255)
			green = 255;
		if (blue>255)
			blue = 255;
		
		Color hue = new Color(red,green,blue);
		g.setColor(hue);
		g.fillRect(x,y,width,height);
	}
	
	public boolean isBackgroundRenderer()
	{
		return true;
	}
}
