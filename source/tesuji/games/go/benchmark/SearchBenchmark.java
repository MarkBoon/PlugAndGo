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

//import tesuji.core.util.FactoryReport;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import tesuji.core.util.LoggerConfigurator;
import tesuji.core.util.MemoryUtil;
import tesuji.games.general.ColorConstant;
import tesuji.games.general.search.Search;
import tesuji.games.go.common.GoMove;

public class SearchBenchmark
{
	public static Logger _logger = Logger.getLogger(SearchBenchmark.class);
	
	public static void doSearch(Search<GoMove> search)
	{
		LoggerConfigurator.configure();
		
		Logger.getRootLogger().setLevel(Level.INFO);
		long beforeTime;
		long afterTime;
		long beforeMemory = MemoryUtil.getFreeMemory();
		long afterMemory;
		beforeTime = System.currentTimeMillis();
		GoMove move;
        try
        {
        	move = search.doSearch(ColorConstant.BLACK);
	        search.clear();
	        move = search.doSearch(ColorConstant.BLACK);
	        search.clear();
	        move = search.doSearch(ColorConstant.BLACK);
			afterTime = System.currentTimeMillis();
			afterMemory = MemoryUtil.getFreeMemory();
			double total = (afterTime - beforeTime)/1000;
			_logger.info("Time taken: "+total+ " seconds");
			_logger.info("Move chosen: "+move);
			_logger.info("Memory used: "+(afterMemory-beforeMemory));
//			_logger.info(FactoryReport.getFactoryReport());
        }
        catch (Exception e)
        {
        	_logger.error("Unexpected exception "+e.getClass()+": "+e.getMessage());
	        e.printStackTrace();
        }		
	}
}
