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
import java.awt.image.*;
import java.net.URL;

import org.apache.log4j.Logger;

import tesuji.core.gui.Util;


/**
 	This class implements an offscreen image.
 	This can either be a picture from a JPEG or GIF file, or just some
 	space to draw something in yourself.<br><br>

 	Several methods are provided for convenience to copy rectangles
 	between offscreen buffers or to a graphics context on the screen.
 	These are often called 'bit-blit' operations.
*/
public class OffscreenBuffer
{
	private Image pict;
	private Graphics graphics;

	private Dimension size;
	
	private static MediaTracker mTracker = new MediaTracker(Util.getTopFrame());
	private static ImageObserver observer = new ImageObserver()
		{
			public boolean imageUpdate(Image image, int info, int x, int y, int width, int height)
			{
				if ((info & ImageObserver.ALLBITS) != 0)
					return false;
				return true;
			}
		};

	/**
	 * @return a default image observer that only returns true when the complete image has been loaded properly.
	 */
	public static ImageObserver getDefaultObserver()
	{
		return observer;
	}
	
	/**
	 	Creates an OffscreenBuffer of specified dimensions to be used for drawing.
	*/
	public OffscreenBuffer(int width, int height)
	{
		size = new Dimension(width, height);
		
		pict = Util.getImage(width,height);
		graphics = pict.getGraphics();
	}
	/**
	 	Creates an OffscreenBuffer from a JPEG or GIF picture from a file.
	*/
	public OffscreenBuffer(String strFileName)
	{
		try
		{
			// Use a media-tracker to ensure the whole picture is loaded.
			Image image = Util.getImage(strFileName);
			mTracker.addImage(image, 1);
			try
			{
				mTracker.waitForID(1);
			}
			catch (InterruptedException e) {}
	
			int width = image.getWidth(observer);
			int height = image.getHeight(observer);
			size = new Dimension(width, height);
	
			pict = Util.getImage(width, height);
			graphics = pict.getGraphics();
			graphics.drawImage(image, 0, 0, observer);
		}
		catch (Exception e)
		{
			// This typically happens when the file is not found.
			Logger.getLogger(getClass()).error("Exception in OffscreenBuffer: " + strFileName);
		}
	}
	/**
	 	Creates an OffscreenBuffer from a JPEG or GIF picture from a file.
	*/
	public OffscreenBuffer(URL imageURL)
	{
		try
		{
			// Use a media-tracker to ensure the whole picture is loaded.
			Image image = Toolkit.getDefaultToolkit().createImage(imageURL);
			mTracker.addImage(image, 1);
			try
			{
				mTracker.waitForID(1);
			}
			catch (InterruptedException e) {}
	
			int width = image.getWidth(observer);
			int height = image.getHeight(observer);
			size = new Dimension(width, height);
	
			pict = Util.getImage(width, height);
			graphics = pict.getGraphics();
			graphics.drawImage(image, 0, 0, observer);
		}
		catch (Exception e)
		{
			// This typically happens when the file is not found.
			Logger.getLogger(getClass()).error("Exception in OffscreenBuffer: " + imageURL);
		}
	}
	public void copyImage(Graphics to)
	{
		to.drawImage(pict, 0, 0, observer);
	}
	/**
		Copy the OffscreenBuffer contents to a Graphics context.<br><br>
		
		If the picture is smaller than the destination rectangle,
		it's drawn in the top-left corner and the rest is left untouched.<br><br>
	
		If the picture is bigger than the destination rectangle, it gets clipped.
		
	 	@param Rectangle this is the destination rectangle in the Graphics context.
	 	@param Graphics is the destination graphics context.
	*/
	public void copyRect(Rectangle r, Graphics g)
	{
		Graphics tmp = g.create(r.x,r.y,r.width,r.height);
		tmp.drawImage(pict, 0, 0, observer);
	}
	/**
		Copy part of the OffscreenBuffer contents to a Graphics context.<br><br>
	
		The h,v parameters indicate where to start copying from.
			
	 	@param Rectangle this is the destination rectangle in the Graphics context.
	 	@param Graphics is the destination graphics context.
	 	@param int h is the horizontal position in the OffscreenBuffer where to start copying from.
	 	@param int v is the vertical position in the OffscreenBuffer where to start copying from.
	*/
	public void copyRect(Rectangle r, Graphics g, int h, int v )
	{
		Graphics tmp = g.create(r.x,r.y,r.width,r.height);
		tmp.drawImage(pict,-h,-v,observer);
	}
	/**
		Copy the OffscreenBuffer contents to another OffscreenBuffer.<br><br>
		
		If the picture is smaller than the destination rectangle,
		it's drawn in the top-left corner and the rest is left untouched.<br><br>
	
		If the picture is bigger than the destination rectangle, it gets clipped.
		
	 	@param Rectangle this is the destination rectangle in the OffscreenBuffer parameter.
	 	@param OffscreenBuffer is the destination OffscreenBuffer.
	*/
	public void copyRect(Rectangle r, OffscreenBuffer b)
	{
		Graphics g = b.graphics.create(r.x, r.y, r.width, r.height);
		g.drawImage(pict, 0, 0, observer);
	}
	/**
		Copy part of the OffscreenBuffer contents to another OffscreenBuffer.<br><br>
	
		The h,v parameters indicate where to start copying from.
			
	 	@param Rectangle r this is the destination rectangle in the OffscreenBuffer.
	 	@param OffscreenBuffer b is the destination OffscreenBuffer.
	 	@param int h is the horizontal position in the OffscreenBuffer where to start copying from.
	 	@param int v is the vertical position in the OffscreenBuffer where to start copying from.
	*/
	public void copyRect(Rectangle r, OffscreenBuffer b, int h, int v )
	{
		Graphics g = b.graphics.create(r.x,r.y,r.width,r.height);
		g.drawImage(pict,-h,-v,observer);
	}
	/**
		Copy the OffscreenBuffer contents to a Graphics context.<br><br>
		
		If the picture is a different size than the destination rectangle,
		it is scalled to match the size of the rectangle.
		
	 	@param Rectangle this is the destination rectangle in the Graphics context.
	 	@param Graphics is the destination graphics context.
	*/
	public void copyScaledRect(Rectangle r, Graphics g, int width , int height)
	{
		Graphics tmp = g.create(r.x,r.y,r.width,r.height);
		tmp.drawImage(pict, 0, 0, width, height, observer);
	}
	/**
		Copy the OffscreenBuffer contents to a Graphics context.<br><br>
		
		If the picture is a different size than the destination rectangle,
		it is scalled to match the size of the rectangle.
		
	 	@param Rectangle this is the destination rectangle in the Graphics context.
	 	@param Graphics is the destination graphics context.
	*/
	public void copyScaledRect(Rectangle r, OffscreenBuffer b, int width, int height)
	{
		Graphics g = b.graphics.create(r.x, r.y, r.width, r.height);
		g.drawImage(pict, 0, 0, width, height, observer);
	}
	/**
	 	@return: the Graphics context of the OffscreenBuffer
	*/
	public Graphics getGraphics()
	{
		return graphics;
	}
	
	public Dimension getSize()
	{
		return size;
	}
}
