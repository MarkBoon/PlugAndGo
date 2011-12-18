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

package tesuji.games.go.benchmark;

import org.apache.log4j.Logger;

import tesuji.games.go.common.GoMove;

/** Simply runs a bunch of playouts to test speed. */
public class MCBenchmark
{
	private static Logger _logger = Logger.getLogger("MCBenchmark");
	
	public static void doPlayout(MCPlayout<GoMove> playout, int nrPlayouts, int nrThreads)
	{
		long before;
		long after;
		before = System.currentTimeMillis();
		playout.playout(nrPlayouts,nrThreads);
		after = System.currentTimeMillis();
		playout.isConsistent();
		// Print the results
		long total = after - before;
		_logger.info("");
		_logger.info("Testing '"+playout.getAdministration().getClass().getName()+"'");
		_logger.info("Performance:");
		_logger.info("  " + total / 1000.0 + " seconds");
		_logger.info("  " + ((double) playout.getNrMovesPlayed() / (double) nrPlayouts) + " mpos");
		_logger.info("  " + ((double) nrPlayouts) / total + " kpps");
		_logger.info("Black wins = " + playout.getBlackWins());
		_logger.info("White wins = " + playout.getWhiteWins());
		_logger.info("P(black win) = " + ((double) playout.getBlackWins())
				/ (playout.getBlackWins() + playout.getWhiteWins()));
	}
}
