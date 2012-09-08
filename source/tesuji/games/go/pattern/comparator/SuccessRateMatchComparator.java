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

package tesuji.games.go.pattern.comparator;

import java.util.Comparator;

import tesuji.games.go.pattern.incremental.PatternMatch;

import static tesuji.games.general.ColorConstant.*;

/**
 * 
 */
public class SuccessRateMatchComparator
	implements Comparator<PatternMatch>
{
	private byte _color;

	public SuccessRateMatchComparator(byte color)
	{
		_color = color;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(PatternMatch arg0, PatternMatch arg1)
	{
		int nrSuccesses0;
		int nrSuccesses1;
		int nrOccurrences0;
		int nrOccurrences1;
		if (_color==BLACK)
		{
			nrSuccesses0 = (arg0.isInverted()? arg0.getPattern().getWhiteNrSuccesses() : arg0.getPattern().getBlackNrSuccesses());
			nrSuccesses1 = (arg1.isInverted()? arg1.getPattern().getWhiteNrSuccesses() : arg1.getPattern().getBlackNrSuccesses());
			nrOccurrences0 = (arg0.isInverted()? arg0.getPattern().getWhiteNrOccurrences() : arg0.getPattern().getBlackNrOccurrences());
			nrOccurrences1 = (arg1.isInverted()? arg1.getPattern().getWhiteNrOccurrences() : arg1.getPattern().getBlackNrOccurrences());
		}
		else
		{
			nrSuccesses0 = (arg0.isInverted()? arg0.getPattern().getBlackNrSuccesses() : arg0.getPattern().getWhiteNrSuccesses());
			nrSuccesses1 = (arg1.isInverted()? arg1.getPattern().getBlackNrSuccesses() : arg1.getPattern().getWhiteNrSuccesses());
			nrOccurrences0 = (arg0.isInverted()? arg0.getPattern().getBlackNrOccurrences() : arg0.getPattern().getWhiteNrOccurrences());
			nrOccurrences1 = (arg1.isInverted()? arg1.getPattern().getBlackNrOccurrences() : arg1.getPattern().getWhiteNrOccurrences());
			
		}
		
		if (nrSuccesses0/nrOccurrences0 < nrSuccesses1/nrOccurrences1)
			return -1;
		if (nrSuccesses0/nrOccurrences0 == nrSuccesses1/nrOccurrences1 && nrSuccesses0<nrSuccesses1)
			return -1;
		if (nrSuccesses0==nrSuccesses1 && nrOccurrences0==nrOccurrences1)
			return 0;
		return 1;
	}
}
