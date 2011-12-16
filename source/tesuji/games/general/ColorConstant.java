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

package tesuji.games.general;

/**
 * A class defining some static definitions related to the color to move.
 */
public class ColorConstant
{
	/** Value used for the black pieces. */
	public static final byte BLACK = 1;
	/** Value used for the white pieces. */
	public static final byte WHITE = -1;	
	/** Value of empty board-point. */
	public static final byte EMPTY = 0;
	/** Value of undefined move-color. */
	public static final byte COLOR_UNDEFINED = Byte.MAX_VALUE;
	/** Value of empty border-point. */
	public static final byte EDGE = Byte.MIN_VALUE;

//	private static final byte[] oppositeColor = new byte[] {EMPTY, WHITE, BLACK};
//	public static byte opposite(byte color) { return oppositeColor[color]; }
//	public static byte opposite(byte color) { return (color==BLACK?WHITE:BLACK); }
	/**
	 * @return the opposite color of the current color to move
	 */
	public static byte opposite(byte color) { return (byte)-color; }
	
	/**
	 * Determines if the first value passed is better than the second value taking into account the color.
	 * Usually higher values are better for the black player and smaller values are better for white.
	 * 
	 * @param value1
	 * @param value2
	 * @param color
	 * @return boolean
	 */
	public static final boolean isBetter(double value1, double value2, byte color)
	{
		double c = color;
		return (value1*c > value2*c);
	}
}
