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
/*
 * DataProviderAdapter.java
 * 
 * Created on Dec 6, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tesuji.games.general.provider;

/**
 * Instead of implementing the DataProvider it's usually easier to subclass
 * this partial implementation.
 */
public abstract class DataProviderAdapter
	implements DataProvider
{
	private String _name = "Unknown";
	private int _boardSize;
	
	/**
	 * @see tesuji.games.general.DataProvider#getName()
	 */
	public String getName()
	{
		return _name;
	}
	
	public int getBoardSize()
	{
		return _boardSize;
	}
	
	/**
	 * @param name
	 */
	public void setName(String name)
	{
		_name = name;
	}
	
	protected void setBoardSize(int size)
	{
		_boardSize = size;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return _name;
	}

	public int hashCode()
	{
		return _name.hashCode();
	}
	
	public boolean equals(Object o)
	{
		DataProvider provider = (DataProvider) o;
		return provider.getName().equals(_name);
	}
	
	/**
	 * @see tesuji.games.general.provider.DataProvider#fillData(int, int)
	 */
	public void fillData(int x, int y)
	{
		// Since isFullProvider is the default this is added as a dummy method.
	}
		
	/**
	 * @see tesuji.games.general.DataProvider#isFullProvider()
	 */
	public boolean isFullProvider()
	{
		return true;
	}
}
