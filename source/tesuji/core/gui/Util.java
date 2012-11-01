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


import java.awt.FontMetrics;
import java.applet.Applet;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Util is a core helper class.
 * 
 */

public class Util
{
    /**
        This class provides a few static methods that provide some functionality
        uniformly for applets and applications.
    */
    private static Applet applet;
    private static boolean application = true;

    private static Component topFrame = null;
    private static boolean soundOn = true;
    private static boolean cannotPlaySound = false;
    private static java.util.Locale currentLocale;
    private static String resourceDir;

    public static Applet getApplet()
    {
        if (!isApplication())
            return applet;
        else
            return null;
    }
    
    /**
     * @return java.util.Locale
     */
    public static java.util.Locale getCurrentLocale()
    {
        if (currentLocale==null)
            currentLocale = java.util.Locale.getDefault();

        return currentLocale;
    }
    
    /**
        This method gets a file as a BufferedInputStream.
        Depending on whether the program is run as an applet
        or as an application, a different method is used.
    
        @param: fileName is the name of the file to open.
        @return: BufferedInputStream from the file specified.
    */
    public static BufferedInputStream getFile( String fileName )
        throws IOException
    {
        if (isApplication())
        {
            if (resourceDir!=null && resourceDir.length()!=0)
            {
                String name = resourceDir+fileName;
                return new BufferedInputStream(new FileInputStream(name));
            }
            else
            {
                URL fileURL = Util.class.getResource("/"+fileName);
                return new BufferedInputStream(fileURL.openStream());
            }
        }
        else
        {
            URL urlIn = new URL(applet.getCodeBase(),fileName);
            return new BufferedInputStream(urlIn.openStream());
        }
    }
    
    public static Image getImage(int width, int height)
    {
        if (application)
        {
//              if (topFrame==null)
//              {
//                  topFrame = new Frame();
//                  topFrame.setVisible(true);
//              }
            return getTopFrame().createImage(width,height);
        }
        else
            return applet.createImage(width, height);
    }
    
    /**

        Fetch a picture. Depending on whether it's an applet or
        an application it will get it using an URL or the Toolkit

        @param theName the name of the file containing the picture.
        @return returns the Image when the picture was found successfully.
    */
    public static Image getImage(String theName)
    {
        if (isApplication())
        {
            String fileName = theName;
            //String fileName = "images/"+theName;
            return Toolkit.getDefaultToolkit().getImage(fileName);
        }
        else
        {
            String protocol = applet.getCodeBase().getProtocol();
            String host = applet.getCodeBase().getHost();
            try
            {
                String fileName = "/vplay/applets/images/"+theName;
                URL url = new URL(protocol,host,fileName);
                return applet.getImage(url,fileName);
            }
            catch (MalformedURLException ex)
            {
                System.out.println(ex.getClass().getName()+": Host="+host+" File="+theName);
                return null;
            }
        }
    }
    
    public static Component getTopFrame()
    {
        if (topFrame!=null)
            return topFrame;

        try
        {
            Component c = applet;
            while (c!=null && c.getParent()!=null && !(c instanceof Frame))
                c = c.getParent();
            if (c==null)
            {
                topFrame = new Frame();
                topFrame.setVisible(true);
                return topFrame;
            }
            return c;
        }
        catch (Exception e)
        {
            // If there is no top-frame, make one up. It will remain invisible.
            topFrame = new Frame();
            //topFrame.setSize(100,100);
            topFrame.setVisible(true);
            return topFrame;
        }
    }
    
    public static Frame getTopFrame(Component c)
    {
        while (c!=null && !(c instanceof Frame))
            c = c.getParent();

        return (Frame) c;
    }
    
    public static boolean isApplication()
    {
        return application;
    }
    
    public static boolean isSoundOn()
    {
        return soundOn;
    }
    
    public static void playSound( String theName )
    {
        if (!isSoundOn())
            return;

        if (isApplication())
        {
        }
        else
        {
            try
            {
                URL urlIn = new URL(applet.getCodeBase(),"Sounds/"+theName+".au");
                //System.out.println("Read sound '"+urlIn+"'");
                applet.play(urlIn);
            }
            catch (MalformedURLException e) {}
        catch (Exception e)
          {
            System.out.println("Sound deactivated");
            setSoundOn(false);
            cannotPlaySound = true;
          }
        }
    }
    
    public static void setApplet(Applet app)
    {
        applet = app;
        application = false;
    }
    
    public static void setApplication(boolean flag)
    {
        application = flag;
    }
    
    /**
     * @param newCurrentLocale java.util.Locale
     */
    public static void setCurrentLocale(java.util.Locale newCurrentLocale)
    {
        currentLocale = newCurrentLocale;
    }
    
    public static void setMessageLine(String theText)
    {
        if (isApplication())
            System.out.println(theText);
        else
            applet.showStatus(theText);
    }
    
    public static void setSoundOn(boolean bOn)
    {
        if (cannotPlaySound)
            soundOn = false;

        soundOn = bOn;
    }
    
    public static void setTopFrame( Component theFrame )
    {
        topFrame.setVisible(false);
        ((Frame)topFrame).dispose();
        topFrame = theFrame;
    }
    
    public static void setTopFrame( Frame theFrame )
    {
        topFrame.setVisible(false);
        ((Frame)topFrame).dispose();
        topFrame = theFrame;
    }
    
    public static String getResourceDir()
    {
        return resourceDir;
    }
    
    public static void setResourceDir(String dir)
    {
        resourceDir = dir;
    }
    
    /**
		A utility method to calculate the average width of a printable character.
		This is mostly used since getMaxAdvance gives a too large number on NT.
		@return the average width of the first 256 characters.
	*/
	static public int calcAverageFontWidth(FontMetrics fm)
	{
		int w[] = fm.getWidths();
		int len = w.length;
		int sum = 0;
		int div = 0;
		
		for (int i=0; i<len; i++)
		{
			if (w[i]> 0) // Only count printable characters
			{
				div++;
				sum += w[i];
			}
		}
		return sum/div;
	}
	public static void center(java.awt.Frame frame)
	{
		// Retrieve the size of the screen.
		java.awt.Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		// And move the dialog accordingly.
		frame.setLocation((d.width-frame.getSize().width)/2,(d.height-frame.getSize().height)/2);
	}
}
