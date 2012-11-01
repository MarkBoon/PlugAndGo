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
package tesuji.core.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * This is a helper class that represents a bit of an ugly hack in order to 
 * dynamically add a JAR-file to the class-loader.
 */
public class ClassPathLoader
{
	private static final Class<?>[] parameters = new Class[] { URL.class };

	public static void addFile(String s)
		throws IOException
	{
		File file = new File(s);

		addFile(file);
	}

	public static void addFile(File file)
		throws IOException
	{
		addURL(file.toURI().toURL());
	}

	public static void addURL(URL url)
		throws IOException
	{
		URLClassLoader sysloader = (URLClassLoader) ClassLoader
				.getSystemClassLoader();

		Class<URLClassLoader> sysclass = URLClassLoader.class;

		try 
		{
			Method method = sysclass.getDeclaredMethod("addURL", parameters);

			method.setAccessible(true);

			method.invoke(sysloader, new Object[] { url });

		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace();
			
			throw new IOException(
					"Error, could not add URL to system classloader");
		}
	}
}