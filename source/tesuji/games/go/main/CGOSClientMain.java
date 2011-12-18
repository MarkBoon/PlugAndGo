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

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JTextField;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import tesuji.core.util.MultiTypeProperties;
import tesuji.games.go.common.BasicGoMoveAdministration;
import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.gui.GoBoardComponent;
import tesuji.games.go.util.GoGameProperties;
import tesuji.games.gtp.GTPCommand;
import tesuji.games.gtp.GTPUtil;

public class CGOSClientMain
{
	private static final int MAX_MOVE_TIME =		300000;
	private static final int MAX_RESPONSE_TIME =	30000;

	private static String _host;
	private static int _port;
	private static Socket _socket;
	private static Process _process;
	
	private static BufferedWriter _socketWriter;
	private static BufferedReader _socketReader;
	private static Writer _gtpWriter;
	private static Reader _gtpReader;
	
	private static String _engineCommand;
	private static String _engineName;
	private static String _enginePassword;
	
	private static boolean quit = false;
	private static boolean ready = false;
	
	private static BasicGoMoveAdministration moveAdministration = new BasicGoMoveAdministration(new GoGameProperties());
	private static GoMoveFactory moveFactory = GoMoveFactory.getSingleton();
	
	private static JFrame window;
	private static JTextField textField;

	public static void main(String[] args)
	{
		MultiTypeProperties properties = new MultiTypeProperties();
		try
		{
			Resource resource = new FileSystemResource("CGOSClient.properties");
			properties.load(resource.getInputStream());
		}
		catch (Exception exception)
		{
			System.err.println("Error while reading the properties file: "+exception.getMessage());
			exception.printStackTrace();
			quit();
		}
		
		_host = properties.getProperty("host");
		_port = properties.getIntProperty("port");
		_engineCommand = properties.getProperty("engine.command");
		_engineName = properties.getProperty("engine.name");
		_enginePassword = properties.getProperty("engine.password");
		
		System.err.println("Start engine "+_engineName);
		startEngine(_engineCommand);
		System.err.println("Engine started");
		System.err.println("Connect to server "+_host+" : "+_port);
		connectToServer();
		System.err.println("Server connected");
		
		ready = true;
		displayWindow();
		
		try
		{
			while (true)
			{
				if (_socketReader.ready())
				{
					String serverCommand = _socketReader.readLine();
					String engineResponse = handleServerCommand(serverCommand);
					if (engineResponse.length()>0)
					{
						System.err.println("Received engine response: '"+engineResponse+"'");
						_socketWriter.write(engineResponse);
						_socketWriter.flush();
					}
				}
				else
				{
					Thread.sleep(10);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			quit();
		}
	}
	
	private static String handleServerCommand(String serverCommand)
	{
		System.err.println("Received server command: '"+serverCommand+"'");
		
		ArrayList<String> arguments = parseArguments(serverCommand);
		
		if (serverCommand.startsWith("protocol"))
			return "e1 Tesuji Software CGOS Client";
		if (serverCommand.startsWith("username"))
			return _engineName;
		if (serverCommand.startsWith("password"))
			return _enginePassword;
		if (serverCommand.startsWith("info"))
		{
			System.err.println(serverCommand);
			textField.setText(serverCommand.substring(5));
			return "";
		}
		if (serverCommand.startsWith("setup"))
		{
			String boardSize = arguments.get(1);
			String komi = arguments.get(2);
			String white = arguments.get(4);
			String black = arguments.get(5);
			String color = GTPCommand.BLACK_COLOR;
			
			window.setTitle(white+" - "+black);
			moveAdministration.getGameProperties().setProperty(GoGameProperties.BOARDSIZE, boardSize);
			moveAdministration.clear();
			
			writeGTP(GTPCommand.BOARDSIZE+" "+boardSize);
			readGTP(MAX_RESPONSE_TIME);
			writeGTP(GTPCommand.CLEAR_BOARD);
			readGTP(MAX_RESPONSE_TIME);
			writeGTP(GTPCommand.KOMI+" "+komi);
			readGTP(MAX_RESPONSE_TIME);
			
			for (int i=6; i<arguments.size(); i+=2)
			{
				String move = arguments.get(i);
				writeGTP(GTPCommand.PLAY+" "+color+" "+move);
				readGTP(MAX_RESPONSE_TIME);
				
				moveAdministration.playMove(moveFactory.parseMove(color+" "+move));
				
				if (color==GTPCommand.BLACK_COLOR)
					color = GTPCommand.WHITE_COLOR;
				else
					color = GTPCommand.BLACK_COLOR;
			}
			
			ready = false;
			
			return "";
		}
		if (serverCommand.startsWith("genmove"))
		{
			String color = arguments.get(0);
			writeGTP(GTPCommand.GENMOVE+" "+color);
			String move =  readGTP(MAX_MOVE_TIME);
			
			moveAdministration.playMove(moveFactory.parseMove(color+" "+move));
			
			return move;
		}
		if (serverCommand.startsWith("play"))
		{
			String color = arguments.get(0);
			String move = arguments.get(1);
			
			moveAdministration.playMove(moveFactory.parseMove(color+" "+move));
			
			writeGTP(GTPCommand.PLAY+" "+color+" "+move);
			return readGTP(MAX_RESPONSE_TIME);
		}
		if (serverCommand.startsWith("gameover"))
		{
			String result = arguments.get(1);
			textField.setText(result);
			
			if (quit)
				quit();
			
			ready = true;
			return "ready";
		}
		
		quit(); // For now, quit when receiving an unknown command.
		return "Command unknown to client";
	}
	
	private static void writeGTP(String gtpCommand)
	{
		System.err.println("Send GTP command: '"+gtpCommand+"'");
		try
		{
			_gtpWriter.write(gtpCommand+"\n");
			_gtpWriter.flush();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			quit();
		}
	}
	
	private static String readGTP(int timeout)
	{
		try
		{
			return GTPUtil.readGTPResponse(_gtpReader, timeout);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			quit();
		}
		return "?";
	}
	
	private static ArrayList<String> parseArguments(String command)
	{
		ArrayList<String> arguments = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(command);
		tokenizer.nextToken(); // Skip the command.
		while (tokenizer.hasMoreTokens())
			arguments.add(tokenizer.nextToken());
		return arguments;
	}

	private static void connectToServer()
	{
		try
		{
			_socket = new Socket(_host,_port);
			_socketWriter = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));
			_socketReader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
		}
		catch (UnknownHostException ex)
		{
			System.err.println("Unknown host: "+_host);
			quit();
		}
		catch (IOException ex)
		{
			System.err.println("IOException: "+ex.getMessage());
			quit();
		}
	}
	
	private static void startEngine(String commandLine)
	{
		try
		{
			_process = Runtime.getRuntime().exec(commandLine);
			_gtpWriter = new OutputStreamWriter(_process.getOutputStream());
			_gtpReader = new InputStreamReader(_process.getInputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			quit();
		}		
	}
	
	private static void displayWindow()
	{
		window = new JFrame();
		window.setSize(400,450);
		window.setLocation(200, 200);
		
		textField = new JTextField();
		textField.setEditable(false);
		
		GoBoardComponent goBoard = new GoBoardComponent();
		goBoard.setModel(moveAdministration.getBoardModel());
		window.add(goBoard,BorderLayout.CENTER);
		window.add(textField,BorderLayout.SOUTH);
		
		window.addWindowListener(new WindowAdapter()
				{
					@Override
					public void windowClosing(WindowEvent e)
					{
						window.setVisible(false);
						if (ready)
							quit();
						quit = true;
					}					
				}
			);
		window.setVisible(true);
	}
	
	private static void quit()
	{
		try
		{
			Thread.sleep(3000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		if (_process!=null)
			_process.destroy();
		System.exit(0);
	}
}
