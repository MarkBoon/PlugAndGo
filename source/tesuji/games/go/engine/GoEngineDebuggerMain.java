package tesuji.games.go.engine;

import java.util.Hashtable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import tesuji.core.util.LoggerConfigurator;
import tesuji.games.go.common.GoEngine;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.util.GoEngineBeanHelper;
import tesuji.games.gtp.GameEngineGTPSocketConnector;
import tesuji.games.util.Console;

/**
 * For this class to work you first need to start an external program that starts the
 * SocketEngine present in the plug-in framework. That would usually be something like:
 * "java -jar GoEngineGTP.jar SocketEngine". Candidates for this are GoGui, twogtp or
 * the GTPTester program.
 * 
 * Next you can start this class from your IDE using a debugger. For that to work this
 * class probably needs to be in your project, so this class will end up the same in
 * several places.
 * 
 * It also needs the file defined in XML_FILE to be present in the default class-path
 * which is usually your working directory. At least that's the default in Eclipse.
 * This default can be changed in the run configuration if required.
 */
public class GoEngineDebuggerMain
{
	public static final String XML_FILE = "TreeSearchEngine.xml";

	public static void main(String[] args)
	{
		boolean engineNameSet = false;
    	LoggerConfigurator.configure();
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(Console.getSingleton().getLogAppender());
		Logger.getRootLogger().setLevel(Level.INFO);
		Hashtable<String, GoEngine> engineList;
		
		Logger logger = Logger.getRootLogger();
		logger.info("Start GoEngineDebugger");
		
		String engineName = null;
		
		if (args.length!=0)
		{
			engineName = args[0];
			engineNameSet = true;
		}
		
		GoEngine engine = null;

		try
		{
			engineList = GoEngineBeanHelper.getGoEngineBeans(XML_FILE, engineName);
			
			if (engineNameSet)
			{
				engine = engineList.get(engineName);
			}
			else
			{
				engineList.keys();
				String selectedEngine = GoEngineBeanHelper.selectEngineFromList(engineList);
				if (selectedEngine==null)
					System.exit(0);
				engine = engineList.get(selectedEngine);
			}

			Console.getSingleton().setTitle(engine.getEngineName()+" - "+engine.getEngineVersion());
		}
		catch (Exception exception)
		{
			logger.error("Unexpected "+exception.getClass()+" in GoEngineDebuggerMain: "+exception.getMessage());
		}
		
		GameEngineGTPSocketConnector<GoMove> socketConnector = new GameEngineGTPSocketConnector<GoMove>();
		socketConnector.init(args);
		socketConnector.connectEngine(engine);
	}
}
