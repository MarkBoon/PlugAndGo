package tesuji.games.go.main;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.swing.JOptionPane;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import tesuji.core.util.MultiTypeProperties;
import tesuji.games.general.ColorConstant;
import tesuji.games.go.common.BasicGoMoveAdministration;
import tesuji.games.go.common.GoEngine;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.util.GoGameProperties;
import tesuji.games.go.util.RandomTestEngine;
import tesuji.games.gtp.GTPCommand;

import static tesuji.games.gtp.GTPUtil.readGTPResponse;

public class GTPTesterMain
{
	private static Writer writer;
	private static Reader reader;
	private static String supportedCommands;
	
	private static BasicGoMoveAdministration moveAdministration;
	private static GoEngine randomEngine;
	private static int nrPasses;
	
	private static final int MAX_MOVE_TIME =		300000;
	private static final int MAX_RESPONSE_TIME =	30000;
	
	public static void main(String[] args)
	{
		String commandLine = null;
		
		if (args.length!=1)
		{
			MultiTypeProperties properties = new MultiTypeProperties();
			try
			{
				Resource resource = new FileSystemResource("CGOSClient.properties");
				properties.load(resource.getInputStream());
				commandLine = properties.getProperty("engine.command");
				if (commandLine==null || commandLine.length()==0)
				{
					System.err.println("Usage: java -jar GTPTester.jar \"engine-command\"\nor make sure the CGOSClient.properties file is present containing the 'engine.command' property");
					System.exit(0);
				}
			}
			catch (Exception exception)
			{
				System.err.println("Error while reading the properties file: "+exception.getMessage());
				exception.printStackTrace();
				System.exit(-1);
			}
		}
		else
			commandLine = args[0];
		
		moveAdministration = new BasicGoMoveAdministration(new GoGameProperties());
		moveAdministration.getGameProperties().setIntProperty(GoGameProperties.BOARDSIZE, 9);
		moveAdministration.getGameProperties().setDoubleProperty(GoGameProperties.KOMI, 7.5);
		
		randomEngine = new RandomTestEngine();
		randomEngine.set(GoGameProperties.KOMI, "7.5");
		randomEngine.set(GoGameProperties.BOARDSIZE, "9");

		try
		{
			Process process = Runtime.getRuntime().exec(commandLine);
			reader = new InputStreamReader(process.getInputStream());
			writer = new OutputStreamWriter(process.getOutputStream());
			
			sendCommand(GTPCommand.NAME);
			String engineName = readGTPResponse(reader, MAX_MOVE_TIME);
			sendCommand(GTPCommand.VERSION);
			String engineVersion = readGTPResponse(reader, MAX_RESPONSE_TIME);
			System.err.println("Testing engine '"+engineName+"'"+" version "+engineVersion);
			sendCommand(GTPCommand.LIST_COMMANDS);
			supportedCommands = readGTPResponse(reader, MAX_RESPONSE_TIME);
			System.err.println("Engine supports the following commands:\n"+supportedCommands);
			checkCommand(GTPCommand.CLEAR_BOARD);
			checkCommand(GTPCommand.PLAY);
			checkCommand(GTPCommand.GENMOVE);
			checkCommand(GTPCommand.KOMI);
			checkCommand(GTPCommand.BOARDSIZE);
			sendCommand(GTPCommand.KOMI+" 7.5");
			readGTPResponse(reader, MAX_RESPONSE_TIME);
			sendCommand(GTPCommand.BOARDSIZE+" 9");
			readGTPResponse(reader, MAX_RESPONSE_TIME);
			playGameWithBlack();
			playGameWithWhite();
			
			process.destroy();
			System.err.println("\nYour Go engine checked out OK!\n");
			JOptionPane.showMessageDialog(null, "Your Go engine checked out OK!");
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			System.exit(-1);
		}
	}
	
	private static void sendCommand(String command)
		throws IOException
	{
		System.err.println("Sending GTP command: "+command);
		writer.write(command+"\n");
		writer.flush();
	}
	
	private static void checkCommand(String command)
	{
		if (!supportedCommands.contains(command))
		{
			System.err.println("The engine doesn't support the required command '"+command+"'");
			System.exit(-1);
		}
	}
	
	private static void playGameWithBlack()
		throws Exception
	{
		nrPasses = 0;
		moveAdministration.clear();
		randomEngine.clearBoard();
		sendCommand(GTPCommand.CLEAR_BOARD);
		readGTPResponse(reader, MAX_RESPONSE_TIME);
		while (nrPasses<2)
		{
			// First the engine under testing.
			sendCommand(GTPCommand.GENMOVE+" "+GTPCommand.BLACK_COLOR);
			String moveString = readGTPResponse(reader, MAX_MOVE_TIME);
			GoMove move = GoMoveFactory.getSingleton().parseMove(moveString.toLowerCase());
			move.setColor(ColorConstant.BLACK);
			if (move.isPass())
				nrPasses++;
			else
				nrPasses = 0;
			if (nrPasses>1)
				break;
			
			if (!moveAdministration.isLegalMove(move))
			{
				System.err.println("The Go engine produced an illegal move: "+move);
				System.err.println(moveAdministration.getBoardModel().toString());
				System.exit(-1);
			}

			// Update the internal data.
			moveAdministration.playMove(move);
			randomEngine.playMove(move);
			
			// Now the internal engine.
			GoMove randomMove = randomEngine.requestMove(ColorConstant.WHITE);
			randomEngine.playMove(randomMove);
			moveAdministration.playMove(randomMove);
			if (randomMove.isPass())
				nrPasses++;
			else
				nrPasses++;
			sendCommand(GTPCommand.PLAY+" "+randomMove.toGTPWithColor());
			readGTPResponse(reader, MAX_RESPONSE_TIME);
		}
	}
	
	private static void playGameWithWhite()
		throws Exception
	{
		nrPasses = 0;
		moveAdministration.clear();
		randomEngine.clearBoard();
		sendCommand(GTPCommand.CLEAR_BOARD);
		readGTPResponse(reader, MAX_RESPONSE_TIME);
		while (nrPasses<2)
		{
			// First the internal engine.
			GoMove randomMove = randomEngine.requestMove(ColorConstant.BLACK);
			randomEngine.playMove(randomMove);
			moveAdministration.playMove(randomMove);
			if (randomMove.isPass())
				nrPasses++;
			else
				nrPasses++;
			sendCommand(GTPCommand.PLAY+" "+randomMove.toGTPWithColor());
			readGTPResponse(reader, MAX_RESPONSE_TIME);

			// Now the engine under testing.
			sendCommand(GTPCommand.GENMOVE+" "+GTPCommand.WHITE_COLOR);
			String moveString = readGTPResponse(reader, MAX_MOVE_TIME);
			GoMove move = GoMoveFactory.getSingleton().parseMove(moveString.toLowerCase());
			move.setColor(ColorConstant.WHITE);
			if (move.isPass())
				nrPasses++;
			else
				nrPasses = 0;
			if (nrPasses>1)
				break;
			
			if (!moveAdministration.isLegalMove(move))
			{
				System.err.println("The Go engine produced an illegal move: "+move);
				System.exit(-1);
			}
			// Update the internal data.
			moveAdministration.playMove(move);
			randomEngine.playMove(move);
		}
	}
}
