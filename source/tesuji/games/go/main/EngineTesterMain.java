package tesuji.games.go.main;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Hashtable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import tesuji.core.util.LoggerConfigurator;
import tesuji.games.general.ColorConstant;
import tesuji.games.general.MoveStack;
import tesuji.games.go.common.BasicGoMoveAdministration;
import tesuji.games.go.common.GoEngine;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.util.GoEngineBeanHelper;
import tesuji.games.go.util.GoGameProperties;
import tesuji.games.util.Console;

public class EngineTesterMain
{
	private static GoGameProperties gameProperties;
	private static int gameNr;
	private static boolean alt = false;
	private static String dataFile = "gtp/games/testA";
	
	public static void main(String[] args)
					throws Exception
	{
    	LoggerConfigurator.configure();
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(Console.getSingleton().getLogAppender());
		Logger.getRootLogger().setLevel(Level.INFO);
		Hashtable<String, GoEngine> engineList;
		
		Logger logger = Logger.getRootLogger();
		logger.info("Start EngineTesterMain");
		
		String firstEngineName = "Bot1";
		String secondEngineName = "Bot2";
		
		if (args.length==2)
		{
			firstEngineName = args[0];
			secondEngineName = args[1];
		}
		
		GoEngine engine1 = null;
		GoEngine engine2 = null;

		try
		{
			gameProperties = GoEngineBeanHelper.getGoGameProperties("source/tesuji/games/go/main/GoEngine.xml");
			if (gameProperties==null)
			{
				System.err.println("Couldn't instantiate GoGameProperties");
				System.exit(-1);
			}
			
			engineList = GoEngineBeanHelper.getGoEngineBeans("source/tesuji/games/go/main/GoEngine.xml",null);
			
			engine1 = engineList.get(firstEngineName);
			engine2 = engineList.get(secondEngineName);
			if (engine1==null)
			{
				System.err.println("Couldn't instantiate an engine with name "+firstEngineName);
				System.exit(-1);
			}
			if (engine2==null)
			{
				System.err.println("Couldn't instantiate an engine with name "+secondEngineName);
				System.exit(-1);
			}

			engine1.setProperties(gameProperties);
			engine2.setProperties(gameProperties);
			
			Console.getSingleton().setTitle(engine1.getEngineName()+" - "+engine1.getEngineVersion()+" vs. "+engine2.getEngineName()+" - "+engine2.getEngineVersion());

			BasicGoMoveAdministration administration = new BasicGoMoveAdministration(gameProperties);
			writeResultHeader(administration, engine1, engine2);

			while (true)
			{
				playGame(engine1,engine2);
				
				GoEngine tmp = engine1;
				engine1 = engine2;
				engine2 = tmp;
				alt = !alt;
				gameNr++;
			}
		}
		catch (Exception exception)
		{
			logger.error("Unexpected "+exception.getClass()+" in EngineTesterMain: "+exception.getMessage());
		}
	}

	private static void playGame(GoEngine engine1, GoEngine engine2)
	{
		BasicGoMoveAdministration administration = new BasicGoMoveAdministration(gameProperties);
		engine1.clearBoard();
		engine2.clearBoard();
//		setup(administration,engine1,engine2);
		engine1.requestMove(ColorConstant.BLACK);
		engine2.requestMove(ColorConstant.BLACK);
		engine1.clearBoard();
		engine2.clearBoard();
		
		GoMove move;
		while (!administration.isGameFinished())
		{
			if (administration.getColorToMove()==ColorConstant.BLACK)
				move = engine1.requestMove(ColorConstant.BLACK);
			else
				move = engine2.requestMove(ColorConstant.WHITE);
			administration.playMove(move);
			engine1.playMove(move);
			engine2.playMove(move);
		}
		writeResult(administration,engine1,engine2);
	}
	
	private static void setup(BasicGoMoveAdministration administration, GoEngine engine1, GoEngine engine2)
	{
		String[] diagram = new String[]{
						".XX.XXXX.",
						"X.XXXOOOX",
						".XX.XXO.O",
						"X.XX.XOOO",
						"XX.XXXXO.",
						"XXX.XXXXO",
						".X.XXOOOO",
						"XXXXXO.OO",
						"X.X.XXO.O"};
		administration.setup(diagram);
		MoveStack<GoMove> moves = administration.getMoves();
		for (int i=0; i<moves.size(); i++)
		{
			GoMove move = moves.get(i);
			engine1.playMove((GoMove)move.cloneMove());
			engine2.playMove((GoMove)move.cloneMove());
		}
		engine1.playMove((GoMove)GoMoveFactory.getSingleton().createPassMove(ColorConstant.WHITE));
		engine2.playMove((GoMove)GoMoveFactory.getSingleton().createPassMove(ColorConstant.WHITE));
	}
	
	private static void writeResultHeader(BasicGoMoveAdministration administration, GoEngine engine1, GoEngine engine2)
	{
		char c = 'B';
		do
		{
			try
			{
				File f = new File(dataFile+".dat");
				if	(!f.exists())
					break; 
				dataFile = dataFile+Character.toString(c);
				c++;
			}
			catch (Exception exception)
			{
				break;
			}
		} while (true);
	
		Date date = new Date();
		try
		{
			FileWriter writer = new FileWriter(dataFile+".dat",false);

			writer.write("#  Black: "+engine1.getEngineName()+"\n");
			writer.write("#  BlackCommand: internal\n");
			writer.write("#  BlackLabel: "+engine1.getEngineName()+"\n");
			writer.write("#  BlackVersion: "+engine1.getEngineVersion()+"\n");
			writer.write("#  Date: "+date+"\n");
			writer.write("#  Host: local\n");
			writer.write("#  Komi: "+administration.getKomi()+"\n");
			writer.write("#  Referee: - \n");
			writer.write("#  Size: "+administration.getBoardModel().getBoardSize()+"\n");
			writer.write("#  White: "+engine2.getEngineName()+"\n");
			writer.write("#  WhiteCommand: internal\n");
			writer.write("#  WhiteLabel: "+engine2.getEngineName()+"\n");
			writer.write("#  WhiteVersion: "+engine2.getEngineVersion()+"\n");
			writer.write("#  Xml: 0\n");
			writer.write("#\n");
			writer.write("#GAME\tRES_B\tRES_W\tRES_R\tALT\tDUP\tLEN\tTIME_B\tTIME_W\tCPU_B\tCPU_W\tERR\tERR_MSG\n");
			writer.flush();
			writer.close();
		}
		catch (Exception exception)
		{
			Logger.getRootLogger().error("Unexpected "+exception.getClass()+" in EngineTesterMain: "+exception.getMessage());
		}
	}
	
	private static void writeResult(BasicGoMoveAdministration administration, GoEngine engine1, GoEngine engine2)
	{
		double score = administration.getScore();
		String winner = (((score>0.0) ^ alt) ? "B+" : "W+")+Math.abs(score);
		try
		{
			FileWriter writer = new FileWriter(dataFile+".dat",true);

			writer.write(""+gameNr+"\t");
			writer.write(winner+"\t");
			writer.write(winner+"\t");
			writer.write("?\t");
			writer.write((alt?"1":"0")+"\t");
			writer.write("-\t");
			writer.write((administration.getNextMoveNr()-3)+"\t");
			writer.write("0.0\t");
			writer.write("0.0\t");
			writer.write("0\t");
			writer.write("0\t");
			writer.write("0\t");
			writer.write("-\t\n");
			writer.flush();
			writer.close();
		}
		catch (Exception exception)
		{
			Logger.getRootLogger().error("Unexpected "+exception.getClass()+" in EngineTesterMain: "+exception.getMessage());
		}
	}
	
	private static void writeGameRecord(BasicGoMoveAdministration administration, double score, GoEngine engine1, GoEngine engine2)
	{
		Date date = new Date();
		String winner = (((score>0.0) ^ !alt) ? "B" : "W");
		String fileName = "gtp/games/testA-"+gameNr+".sgf";
		gameNr++;
		try
		{
			FileWriter writer = new FileWriter(fileName);
			
			writer.write("(;FF[4]CA[UTF-8]AP[EngineTesterMain]SZ["+administration.getBoardModel().getBoardSize()+"]\n");
			writer.write("KM["+administration.getKomi()+"]PB["+engine1.getEngineName()+" - "+engine1.getEngineVersion()+"]PW["+engine1.getEngineName()+" - "+engine1.getEngineVersion()+"]DT["+date.toString()+"]RE["+winner+"+"+Math.abs(score)+"]\n");
			writer.write(administration.getMoves().toRawSGF()+")\n");
			writer.flush();
			writer.close();
		}
		catch (Exception exception)
		{
			Logger.getRootLogger().error("Unexpected "+exception.getClass()+" in EngineTesterMain: "+exception.getMessage());
		}
	}
}
