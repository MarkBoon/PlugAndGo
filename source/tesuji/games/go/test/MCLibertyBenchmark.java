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

package tesuji.games.go.test;

import tesuji.games.go.common.GoMove;
import tesuji.games.go.monte_carlo.MCLibertyAdministration;
import tesuji.games.go.monte_carlo.MCPlayout;
import tesuji.games.go.monte_carlo.AbstractMonteCarloAdministration;

/** Simply runs a bunch of playouts to test speed. */
public class MCLibertyBenchmark
{
	public static final int BOARD_SIZE = 9;
	public static final int KOMI = 5;
	
	public static final int NUMBER_OF_PLAYOUTS = 500000;
	public static final int NUMBER_OF_THREADS = 1;

	public static void main(String[] args)
	{
		AbstractMonteCarloAdministration administration = new MCLibertyAdministration();
		administration.setBoardSize(BOARD_SIZE);
		administration.setKomi(KOMI);
		MCPlayout<GoMove> playout = new MCPlayout<GoMove>(administration);
		MCBenchmark.doPlayout(playout,NUMBER_OF_PLAYOUTS,NUMBER_OF_THREADS);
	}
}