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

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import tesuji.core.util.LoggerConfigurator;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.monte_carlo.MonteCarloAdministration;
import tesuji.games.gtp.GTPCommand;

/** Simply runs a bunch of playouts to test speed. */
public class MCBenchmarkSuite
{
	private static Logger _logger = Logger.getLogger("MCBenchmark");

	private int _boardSize;
	private double _komi;
	private int _nrPlayouts;
	private int _nrProcessors;

	private List<MonteCarloAdministration<?>> _mcList;
	
	public static void main(String[] args)
	{
    	LoggerConfigurator.configure();

    	try
		{
			Resource resource = new FileSystemResource("source/tesuji/games/go/benchmark/MCBenchmarkConfig.xml");
			XmlBeanFactory factory = new XmlBeanFactory(resource);
			MCBenchmarkSuite suite = (MCBenchmarkSuite) factory.getBean("BenchmarkSuite");
			suite.runBenchmarks();
		}
		catch (Exception exception)
		{
			System.err.println(exception.getMessage());
		}
	}
	
	private void runBenchmarks()
	{
		_logger.info("");
		_logger.info("# playouts: \t" + _nrPlayouts);
		_logger.info("# threads: \t" + _nrProcessors);
		_logger.info("Board size: \t" + _boardSize);
		_logger.info("Komi: \t\t" + _komi);
		_logger.info("");
		for (MonteCarloAdministration<?> mcAdministration : _mcList)
		{
			mcAdministration.set(GTPCommand.BOARDSIZE, Integer.toString(_boardSize));
			mcAdministration.set(GTPCommand.KOMI, Double.toString(_komi));
			MCPlayout<GoMove> playout = new MCPlayout<GoMove>(mcAdministration);
			MCBenchmark.doPlayout(playout,_nrPlayouts,_nrProcessors);
		}		
		_logger.info("");
		_logger.info("Done.");
	}
	
	public List<MonteCarloAdministration<?>> getList()
	{
		return _mcList;
	}
	
	public void setList(List<MonteCarloAdministration<?>> list)
	{
		_mcList = list;
	}

	public int getBoardSize() 
	{
		return _boardSize;
	}

	public void setBoardSize(int size) 
	{
		_boardSize = size;
	}

	public double getKomi() 
	{
		return _komi;
	}

	public void setKomi(double _komi) 
	{
		this._komi = _komi;
	}

	public int getNrPlayouts() 
	{
		return _nrPlayouts;
	}

	public void setNrPlayouts(int playouts) 
	{
		_nrPlayouts = playouts;
	}

	public int getNrProcessors() 
	{
		return _nrProcessors;
	}

	public void setNrProcessors(int processors) 
	{
		_nrProcessors = processors;
	}

	public List<MonteCarloAdministration<?>> get_mcList()
	{
		return _mcList;
	}

	public void set_mcList(List<MonteCarloAdministration<?>> list) 
	{
		_mcList = list;
	}
}