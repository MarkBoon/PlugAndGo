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

import tesuji.games.go.pattern.common.Pattern;

/**
 * 
 */
public class SucsessRatioPatternComparator
	implements Comparator<Pattern>
{

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Pattern arg0, Pattern arg1)
	{
		int nrSuccesses0 = arg0.getBlackNrSuccesses()+arg0.getWhiteNrSuccesses();
		int nrSuccesses1 = arg1.getBlackNrSuccesses()+arg1.getWhiteNrSuccesses();
		double winRatio0 = (double)nrSuccesses0 /
		(double)(arg0.getBlackNrOccurrences() + arg0.getWhiteNrOccurrences());
		double winRatio1 = (double)nrSuccesses1 /
		(double)(arg1.getBlackNrOccurrences() + arg1.getWhiteNrOccurrences());
		
		if (winRatio0<winRatio1)
			return -1;
		if (winRatio0>winRatio1)
			return 1;
		
		if (nrSuccesses0 < nrSuccesses1)
			return -1;
		if (nrSuccesses0 > nrSuccesses1)
			return 1;
		
		return 0;
	}

}
