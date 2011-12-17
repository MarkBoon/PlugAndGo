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

import java.awt.Graphics;

import tesuji.games.general.provider.DataProvider;

/**
 * DataRenderer interface used to render data on the screen coming from a DataProvider.
 * 
 * This can be seen as the front-end counterpart of DataProvider
 */
public interface DataRenderer
{
	/**
	 * The name of this renderer. Usually the same as the name of the DataProvider.
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * @return the DataProvider associated with this renderer.
	 */
	public DataProvider getDataProvider();
	
	/**
	 * A background renderer gets drawn before the board is drawn.
	 * 
	 * @return boolean
	 */
	public boolean isBackgroundRenderer();
	
	/**
	 * The renderer's activity can be switched on or off depending on this Active property.
	 * 
	 * @return boolean
	 */
    public boolean isActive();

    /**
	 * The renderer's activity can be switched on or off depending on this Active property.
	 * 
	 * @return boolean
	 */
    public void setActive(boolean flag);
	
	/**
	 * Render a piece of data, typically provided by a DataProvider
	 * 
	 * @param i x-coordinate of the data-point to render.
	 * @param j y-coordinate of the data-point to render.
	 * @param g Graphics context in which to draw the data
	 * @param x left-coordinate of the area in which to draw
	 * @param y top-coordinate of the area in which to draw
	 * @param width of the area in which to draw
	 * @param height of the area in which to draw
	 */
	public void renderData(int i, int j, Graphics g, int x, int y, int width, int height);
}
