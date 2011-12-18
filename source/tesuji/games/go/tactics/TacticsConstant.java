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
package tesuji.games.go.tactics;

/**
 * These are the possible results of reading done by a tactical module.
 * TODO - should think about using 'enum' now that it's part of Java.
 */
public interface TacticsConstant
{
	/** Constant value indicating a stone cannot be caught by a tactical module. */
	public static final byte RESULT_UNDEFINED = -1;
	/** Constant value indicating a stone cannot be caught by a tactical module. */
	public static final byte CANNOT_CATCH = 1;
	/** Constant value indicating a stone can be caught by a tactical module. */
	public static final byte CAN_CATCH = 2;
	/** Constant value indicating the tactical module returned an illegal value. */
	public static final byte ILLEGAL = 3;
	/** Constant value indicating a stone can only be caught with 'ko' by a tactical module. */
	public static final byte CATCH_WITH_KO = 4;
	
	/** A chain with one liberty that cannot make 3 liberties */
	public static final byte CANNOT_ESCAPE_ATARI = 5;
	/** A chain with one liberty, but that can make 3 liberties */
	public static final byte CAN_ESCAPE_ATARI = 10;
	/** A chain with two liberties that can be caught in a ladder and that can't make 3 liberties */
	public static final byte CANNOT_ESCAPE_LADDER = 15;
	/** A chain with two liberties that can be caught in a ladder, but that can make 3 liberties */
	public static final byte CAN_LADDER = 20;
	/** A chain with two or three liberties that can be caught in a loose-ladder or geta */
	public static final byte CAN_GETA = 30;
	/** A chain with two liberties that can be caught in a ladder but that needs to capture a ko in doing so */
	public static final byte CAN_LADDER_WITH_KO = 25;
	/** A chain with two or three liberties that can be caught in a loose-ladder but that needs to capture a ko in doing so */
	public static final byte CAN_GETA_WITH_KO = 35;
	
	public static final String[] TACTICS =
	{
		null,"CANNOT_CATCH","CAN_CATCH","ILLEGAL","CATCH_WITH_KO","CANNOT_ESCAPE_ATARI",null,null,null,null,
		"CAN_ESCAPE_ATARI",null,null,null,null,"CANNOT_ESCAPE_LADDER",null,null,null,null,
		"CAN_LADDER",null,null,null,null,null,null,null,null,null,
		"CAN_GETA",null,null,null,null,"CAN_LADDER_WITH_KO",null,null,null,null,
		null,null,null,null,null,"CAN_GETA_WITH_KO"
	};

}
