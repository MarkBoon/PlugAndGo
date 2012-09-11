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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import tesuji.core.util.LoggerConfigurator;
import tesuji.games.general.search.Search;
import tesuji.games.general.search.SearchProperties;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.util.GoGameProperties;
import tesuji.games.go.util.Statistics;

public class SearchBenchmarkSuite
{
	private static Logger _logger = Logger.getLogger("SearchBenchmark");

	private int _boardSize;
	private double _komi;
	private int _nrPlayouts;
	private int _nrProcessors;

	private List<Search<GoMove>> _searchList;
	
	public static void main(String[] args)
	{
    	LoggerConfigurator.configure();
    	_logger.setLevel(Level.INFO);

    	try
		{
			Resource resource = new FileSystemResource("source/tesuji/games/go/benchmark/SearchBenchmarkConfig.xml");
			XmlBeanFactory factory = new XmlBeanFactory(resource);
			SearchBenchmarkSuite suite = (SearchBenchmarkSuite) factory.getBean("BenchmarkSuite");
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
		_logger.info("# playouts: \t\t" + _nrPlayouts);
		_logger.info("# processors: \t" + _nrProcessors);
		_logger.info("Board size: \t\t" + _boardSize);
		_logger.info("Komi: \t\t" + _komi);
		_logger.info("");
		for (Search<GoMove> search : _searchList)
		{
			search.getSearchProperties().setIntProperty(GoGameProperties.BOARDSIZE, _boardSize);
			search.getSearchProperties().setDoubleProperty(GoGameProperties.KOMI, _komi);
			search.getSearchProperties().setIntProperty(SearchProperties.NR_NODES, _nrPlayouts);
			search.getSearchProperties().setIntProperty(SearchProperties.NR_PROCESSORS, _nrProcessors);
			Statistics.reset();
			SearchBenchmark.doSearch(search);
		}		
		_logger.info("");
		_logger.info("Done.");
		System.exit(0);
	}
	
	public List<Search<GoMove>> getList()
	{
		return _searchList;
	}
	
	public void setList(List<Search<GoMove>> list)
	{
		_searchList = list;
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

	public void setNrProcessors(int nrProcessors) 
	{
		_nrProcessors = nrProcessors;
	}

	public List<Search<GoMove>> get_mcList()
	{
		return _searchList;
	}

	public void set_mcList(List<Search<GoMove>> list) 
	{
		_searchList = list;
	}
}
