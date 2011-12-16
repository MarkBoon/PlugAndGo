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
package tesuji.games.sgf;

import tesuji.core.util.ArrayStack;
import tesuji.core.util.FlyWeight;
import tesuji.core.util.StringBufferFactory;

/**
 * A property of an SGF node.
 */
public class SGFProperty
    implements FlyWeight
{
    private static ArrayStack<SGFProperty> pool = new ArrayStack<SGFProperty>();
    
	private StringBuffer name;
	private StringBuffer value;
	
	private static int nrProperties = 0;
	
    public static SGFProperty createSGFProperty()
    {
        SGFProperty newProperty;
        if (pool.isEmpty())
        {
            newProperty = new SGFProperty();
            nrProperties++;
            assert nrProperties<2000;
        }
        else
            newProperty = pool.pop();
        
        return newProperty;        
    }
    
	/**
	 * SGFProperty default constructor
	 */
	private SGFProperty()
	{
	}
	
	/**
	 * @return Returns the name.
	 */
	public StringBuffer getName()
	{
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(StringBuffer name)
	{
		this.name = name;
	}

	/**
	 * @return Returns the (first) value.
	 */
	public StringBuffer getValue()
	{
		return value;
	}
	/**
	 * @param value The value to set.
	 */
	public void setValue(StringBuffer value)
	{
		this.value = value;
	}
    
	/*
	 * (non-Javadoc)
	 * @see tesuji.core.util.FlyWeight#recycle()
	 */
    public void recycle()
    {
    	StringBufferFactory.recycleSmallStringBuffer(name);
    	StringBufferFactory.recycleSmallStringBuffer(value);
    	
        pool.push(this);
    }
}
