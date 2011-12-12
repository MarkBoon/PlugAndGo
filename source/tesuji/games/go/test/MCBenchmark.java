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

import tesuji.games.go.monte_carlo.MCPlayout;

/** Simply runs a bunch of playouts to test speed. */
public class MCBenchmark
{
	public static final int BOARD_SIZE = 19;
	public static final int KOMI = 2;
	
	public static final int NUMBER_OF_PLAYOUTS = 100000;
	public static final int NUMBER_OF_THREADS = 1;
	
	public static void doPlayout(MCPlayout playout, int nrPlayouts, int nrThreads)
	{
		long before;
		long after;
		before = System.currentTimeMillis();
		playout.playout(nrPlayouts,nrThreads);
		after = System.currentTimeMillis();
		playout.isConsistent();
		// Print the results
		System.out.println("Initial board:");
		System.out.println("komi: " + KOMI);
		long total = after - before;
		System.out.println("Performance:");
		System.out.println("  " + nrPlayouts + " playouts");
		System.out.println("  " + nrThreads + " threads");
		System.out.println("  " + total / 1000.0 + " seconds");
		System.out.println("  " + ((double) playout.getNrMovesPlayed() / (double) nrPlayouts) + " mpos");
		System.out.println("  " + ((double) nrPlayouts) / total + " kpps");
		System.out.println("Black wins = " + playout.getBlackWins());
		System.out.println("White wins = " + playout.getWhiteWins());
		System.out.println("P(black win) = " + ((double) playout.getBlackWins())
				/ (playout.getBlackWins() + playout.getWhiteWins()));
	}
}
