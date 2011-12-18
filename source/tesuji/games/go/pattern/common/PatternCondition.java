/**
 *	Product: Tesuji Software Go Library.<br><br>
 *
 *	<font color="#CC6600"><font size=-1>
 *	Copyright (c) 2001-2004 Tesuji Software B.V.<br>
 *	All rights reserved.<br><br>
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a
 *	copy of this software and associated documentation files (the
 *	"Software"), to deal in the Software without restriction, including
 *	without limitation the rights to use, copy, modify, merge, publish,
 *	distribute, and/or sell copies of the Software, and to permit persons
 *	to whom the Software is furnished to do so, provided that the above
 *	copyright notice(s) and this permission notice appear in all copies of
 *	the Software and that both the above copyright notice(s) and this
 *	permission notice appear in supporting documentation.<br><br>
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 *	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.<br><br>
 *
 *	Except as contained in this notice, the name of a Tesuji Software
 *	shall not be used in advertising or otherwise to promote the sale, use
 *	or other dealings in this Software without prior written authorization
 *	of Tesuji Software.<br><br>
 *	<font color="#00000"><font size=+1>
 */
package tesuji.games.go.pattern.common;

import static tesuji.games.go.pattern.util.PatternUtil.adjustOrientation;

import java.io.Serializable;
import java.util.StringTokenizer;

import tesuji.games.general.provider.DataProvider;
import tesuji.games.general.provider.DataProviderList;
import tesuji.games.util.Point;

/**
 	Pattern class based on the C++ implementation by Charlie Carroll. <br><br>

 	This class defines extra conditions that need to be
 	checked before a pattern is considered to match.<br><br>

 	It's a simple data-structure with getters and setters.
	The meaning of the data is defined elsewhere.
 */
public class PatternCondition
	implements Serializable, PatternConditionInterface
{
	public static final String SAFETY = "Safety";
	
	public static final char EQUALS = '=';
	public static final char GT = '>';
	public static final char LT = '<';
	
	private static final long serialVersionUID = 113564233662137768L;

    private String _dataProviderName;
	private int _x;			// The location of the data relative
	private int _y;			// to a reference point in the pattern.
	private char _operation; // The boolean operation to perform. (equals, greater-than, etc.)
	private byte _value;		// The value that we apply the operation to.
	transient private Point _tmpPoint;
	/**
	 * PatternCondition constructor comment.
	 */
	public PatternCondition()
	{
		_tmpPoint = Point.create();
	}
	/**
	 * PatternCondition constructor comment.
	 */
	public PatternCondition(String dataProviderName, int x, int y, char operation, byte value)
	{
		this();
		this._dataProviderName = dataProviderName;
		this._x = x;
		this._y = y;
		this._operation = operation;
		this._value = value;
	}
	public Object clone()
	{
		PatternCondition newCondition = new PatternCondition();
		newCondition.copy(this);
		return newCondition;
	}
	public void copy(Object src)
	{
		// super.copy(src);
		PatternCondition srcCondition = (PatternCondition) src;
        _dataProviderName = srcCondition._dataProviderName;
		_x = srcCondition._x;
		_y = srcCondition._y;
		_operation = srcCondition._operation;
		_value = srcCondition._value;
	}
	
	public char getOperation() 
	{
		return _operation;
	}
	public String getDataProviderName()
	{
		return _dataProviderName;
	}
	public byte getValue()
	{
		return _value;
	}
	public int getX()
	{
		return _x;
	}
	public int getY()
	{
		return _y;
	}
	public void setOperation(char operation)
	{
		_operation = operation;
	}
	public void setDataProviderName(String providerName)
	{
        _dataProviderName = providerName;
	}
	public void setValue(byte newValue)
	{
		_value = newValue;
	}
	public void setX(int newX)
	{
		_x = newX;
	}
	public void setY(int newY)
	{
		_y = newY;
	}
	
	public String toString()
	{
		return _dataProviderName+"("+_x+","+_y+")"+_operation+" "+_value+"\n";
	}
	
	public static PatternCondition parse(String conditionString)
	{
		StringTokenizer tokenizer = new StringTokenizer(conditionString.trim(),"(,) ");
		String dataProviderName = tokenizer.nextToken();
		int x = Integer.parseInt(tokenizer.nextToken());
		int y = Integer.parseInt(tokenizer.nextToken());
		char operation = tokenizer.nextToken().charAt(0);
		byte value = Byte.parseByte(tokenizer.nextToken());
		return new PatternCondition(dataProviderName,x,y,operation,value);
	}
	
	public boolean equals(Object o)
	{
		if (!(o instanceof PatternCondition))
			return false;
		PatternCondition compare = (PatternCondition) o;
		
		return (_dataProviderName.equals(compare._dataProviderName)
		&& _operation==compare._operation
		&& _value==compare._value
		&& _x==compare._x
		&& _y==compare._y);
	}
	/**
	 * @see tesuji.games.go.pattern.common.PatternConditionInterface#match(int, int, int, boolean, java.lang.Object)
	 */
	public boolean match(int startX, int startY, int orientation, boolean inverted, DataProviderList dataProviderList)
	{
		DataProvider dataProvider = dataProviderList.getDataProvider(getDataProviderName());
		
		if (dataProvider==null)
			return true; // Not sure if we should return false or true...
		
		adjustOrientation(_x,_y,orientation,_tmpPoint);
		int x = _tmpPoint.x+startX;
		int y = _tmpPoint.y+startY;
		int value = dataProvider.getData(x, y).intValue();
		switch(_operation)
		{
		case '=':
			return (value==_value);
		case '!':
			return (value!=_value);
		case '>':
			return (value>_value);
		case '<':
			return (value<_value);
		}

		return false;
	}
}
