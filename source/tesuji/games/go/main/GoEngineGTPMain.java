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
package tesuji.games.go.main;

import java.util.Hashtable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import tesuji.core.util.LoggerConfigurator;
import tesuji.games.go.common.GoEngine;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.util.GoEngineBeanHelper;
import tesuji.games.gtp.GTPToGameEngine;
import tesuji.games.util.Console;

/**
 * Use Spring to load a playing engine to hook up through GTP.
 * The JAR-file that holds the engine is specified as a property
 * and it's dynamically loaded.
 */
public class GoEngineGTPMain 
{
	public static void main(String[] args)
		throws Exception
	{
		boolean engineNameSet = false;
    	LoggerConfigurator.configure();
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(Console.getSingleton().getLogAppender());
		Logger.getRootLogger().setLevel(Level.INFO);
		Hashtable<String, GoEngine> engineList;
		
		Logger logger = Logger.getRootLogger();
		logger.info("Start GoEngineGTP");
		
		String engineName = null;
		
		if (args.length!=0)
		{
			engineName = args[0];
			engineNameSet = true;
			logger.info("with engine '"+engineName+"'");
		}
		
		GoEngine engine = null;

		try
		{
			engineList = GoEngineBeanHelper.getGoEngineBeans("GoEngineGTP.xml",engineName);
			
			if (engineNameSet)
			{
				engine = engineList.get(engineName);
				if (engine==null)
				{
					System.err.println("Couldn't instantiate an engine with name "+engineName);
//					System.exit(-1);
				}
			}
			else
			{
				engineList.keys();
				String selectedEngine = GoEngineBeanHelper.selectEngineFromList(engineList);
				if (selectedEngine==null)
				{
					System.exit(0);
				}
				engine = engineList.get(selectedEngine);
			}

			Console.getSingleton().setTitle(engine.getEngineName()+" - "+engine.getEngineVersion());
		}
		catch (Exception exception)
		{
			logger.error("Unexpected "+exception.getClass()+" in GoEngineGTPMain: "+exception.getMessage());
		}

		GTPToGameEngine<GoMove> bridge = new GTPToGameEngine<GoMove>(engine);
		bridge.start(System.in, System.out, System.err);
	}
}
