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

import tesuji.core.util.FlyWeight;
import tesuji.games.general.Move;
import tesuji.games.go.util.IntStack;

/** 
 * Simple data class to define a Go move.
 * 
 * Note that the coordinate can be represented using an 'x' and 'y' coordinate
 * or by a single coordinate, usually referred to as 'xy'. The class GoArray
 * contains methods for conversion between the two coordinate systems.
 * 
 * @see tesuji.games.go.util.GoArray
 */
public interface GoMove
	extends Move, FlyWeight
{
	public static final int MINIMUM_PRIORITY = 30000;
	/**
	 * @return Returns the xy coordinate.
	 */
	public int getXY();
	
	/**
	 * @return Returns the x-coordinate.
	 */
	public int getX();
	
	/**
	 * @return Returns the y.
	 */
	public int getY();
	
	public void setXY(int xy);
	
	public void setXY(int x, int y);
	
	public boolean hasCaptives();

	public void addCaptive(int xy);

	/**
	 * @return the list of stones captured by this move. Returns null if hasCaptives() returns false.
	 */
	public IntStack getCaptives();
		
	// Specific to Goliath
	public int getUrgency();
	public void setUrgency(int urgency);
	public int getVisits();
	public void setVisits(int visits);
	public int getWins();
	public void setWins(int wins);
}
