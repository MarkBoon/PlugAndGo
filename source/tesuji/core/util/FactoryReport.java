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

/**
 * This class maintains a list of registered Factory objects and puts out
 * a report about the object allocations of each factory upon request.
 *
 */
public class FactoryReport
{
	public static ArrayList<Factory> _factoryList = new ArrayList<Factory>();
	
	public static void addFactory(Factory factory)
	{
		_factoryList.add(factory);
	}
	
	public static void removeFactory(Factory factory)
	{
		_factoryList.remove(factory);
	}
	
	public static String getFactoryReport()
	{
		StringBuffer out = new StringBuffer();
		for (Factory factory : _factoryList)
		{
			out.append(factory.getFactoryName()+"\n");
			out.append("====================\n");
			out.append(factory.getFactoryReport()+"\n\n");
		}
		return out.toString();
	}
}
