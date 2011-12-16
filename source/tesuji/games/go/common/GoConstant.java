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
package tesuji.games.go.common;

/**
 * This class just contains some definitions of constant values used in Go
 * programming. The maximum numbers are valid for board-sizes up to 19x19.
 */
public class GoConstant
{
	/** Coordinate representing a pass move */
	public static final int PASS = 0;
	
	/** Coordinate representing resignation */
	public static final int RESIGN = -1;
	
	/** Maximum number of moves we allow.
	 * The theoretical maximum is a huge number, but this is enough
	 * for just about all practical purposes.	 * */
	public static final int MAXMOVE = 1000;
	
	/** Maximum number of liberties of a chain on a 19x19 board */
	public static final int MAXLIBERTY = 229;
	
	/** Maximum number of neigbouring chains of a chain on a 19x19 board */
	public static final int MAXCHAINS = 100;
	
	/** Maximum number of points on a 19x19 board */
	public static final int MAXPOINTS = 361;
	
	/** Undefined coordinate value.*/
	public static final int UNDEFINED_COORDINATE = Short.MIN_VALUE;
	
	/** No-care or wild-card value for patterns. */
	public static final byte NOCARE = Byte.MAX_VALUE;

	/** Set this to true to switch on data-structure consistency checks */
	public static final boolean CHECK_CONSISTENCY = true;	
}