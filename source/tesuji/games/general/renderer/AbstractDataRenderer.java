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

import tesuji.games.general.provider.DataProvider;

/**
 */
public abstract class AbstractDataRenderer
	implements DataRenderer
{
	private String _name;
    private boolean _active;
	protected DataProvider _dataProvider;
	
	/**
	 * 
	 */
	public AbstractDataRenderer(String name, DataProvider provider)
	{
		_name = name;
		_dataProvider = provider;
	}

	public String getName()
	{
		return _name;
	}
	
	public DataProvider getDataProvider()
	{
		return _dataProvider;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return (isActive()?"+ ":"- ")+getName();
	}

    /**
     * @param active The active to set.
     */
    public void setActive(boolean active)
    {
        _active = active;
    }

    /**
     * @return Returns the active.
     */
    public boolean isActive()
    {
        return _active;
    }
}
