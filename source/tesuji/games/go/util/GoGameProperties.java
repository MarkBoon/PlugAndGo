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
package tesuji.games.go.util;

import tesuji.games.general.GameProperties;

/**
 * Class used to pass parameters of a game around.
 * As an extension of the Properties class it allows for easy addition of more
 * different kind of properties later, like time-control settings etc.
 */
public class GoGameProperties
	extends GameProperties
{
	public static final String BOARDSIZE =		"boardsize";
	public static final String HANDICAP =		"handicap";
	public static final String KOMI =			"komi";
	public static final String FIRST_PLAYER =	"go.FirstPlayer";
	public static final String RULES =			"go.Rules";
	public static final String SECONDS_PER_MOVE="go.SecondsPerMove";
	public static final String MAIN_TIME =		"go.MainTime";
	public static final String BYO_YOMI_TIME =	"go.ByoYomiTime";
	public static final String BYO_YOMI_MOVES =	"go.ByoYomiMoves";
	
	public static final String BLACK_VALUE =	"Black";
	public static final String WHITE_VALUE =	"White";
	
	public static final String JAPANESE_RULES_VALUE =	"Japanese Rules";
	public static final String CHINESE_RULES_VALUE =	"Chinese Rules";
	public static final String TT_RULES_VALUE =			"Tromp/Taylor Rules";
	
	private static final long serialVersionUID = -4520105316508659714L;

	public GoGameProperties()
	{
		super();

		setIntProperty(BOARDSIZE, 19);
		setIntProperty(HANDICAP, 1);
		setDoubleProperty(KOMI, 7.5);
		setProperty(RULES,CHINESE_RULES_VALUE);
		setProperty(FIRST_PLAYER,BLACK_VALUE);
		setIntProperty(SECONDS_PER_MOVE, 5);
	}
}