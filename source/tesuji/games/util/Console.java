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

package tesuji.games.util;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This is a class that implements a window displaying text. It has a Log4j appender that can be used to direct
 * output to it or the methods addText and addLine can be used explicitly. The reason for this class is that
 * when using GTP for a playing engine the System.in and System.out are often tied up for GTP communication.
 */
public class Console
{
	private static final int MAX_BUFFER_LENGTH = 100000;
	private static final int DELETION_SIZE = 20000;
	
	private JFrame _consoleWindow;
	private JTextArea _textArea;
	private StringBuffer _consoleText = new StringBuffer();

	private Socket _socket;
	private Appender _logAppender;
	private JScrollPane _scrollPane;
	private JPanel _dataPanel;
	
	private static Console _singleton;
	
	public static Console getSingleton()
	{
		if (_singleton==null)
			_singleton = new Console();
		
		return _singleton;
	}
	
	private Console()
	{
		_consoleWindow = new JFrame();
		_textArea = new JTextArea(_consoleText.toString());
		Font font = new Font("Courier",Font.PLAIN,10);
		_textArea.setFont(font);
		_scrollPane = new JScrollPane(_textArea);
		
		_dataPanel = new JPanel();
		_dataPanel.setLayout(new BorderLayout());
		
		_consoleWindow.setLayout(new BorderLayout());
//		_consoleWindow.setLayout(new GridLayout(1,2));
		_consoleWindow.add(_scrollPane,BorderLayout.CENTER);
		_consoleWindow.add(_dataPanel,BorderLayout.EAST);
		_consoleWindow.setSize(600, 200);
		_consoleWindow.setVisible(true);
		
		_consoleWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_consoleWindow.addWindowListener(new WindowAdapter()
				{
					@Override
					public void windowClosing(WindowEvent event)
					{
						if (_socket!=null)
						{
							try
                            {
	                            _socket.close();
                            }
                            catch (IOException e)
                            {
                            	// The engine is exiting anyway.
                            }
						}
					}
				}
			);
	}
	
	public Appender getLogAppender()
	{
		if (_logAppender==null)
			_logAppender = new AppenderSkeleton()
				{
					@Override
					protected void append(LoggingEvent event)
					{
						if (layout==null)
							layout = new PatternLayout("%r [%t] %-5p %c#%M  - %m%n");
						addText(layout.format(event));
					}

					public void close()
					{
					}

					public boolean requiresLayout()
					{
						return true;
					}					
				};
				
		return _logAppender;
	}
	
	public synchronized void addText(String text)
	{
		_consoleText.append(text);
		if (_consoleText.length()>MAX_BUFFER_LENGTH)
			_consoleText.delete(0,MAX_BUFFER_LENGTH-DELETION_SIZE);
		_textArea.setText(_consoleText.toString());
		JScrollBar verticalScrollBar = _scrollPane.getVerticalScrollBar();
		verticalScrollBar.setValue(verticalScrollBar.getMaximum()-verticalScrollBar.getVisibleAmount());
	}
	
	public synchronized void addLine(String line)
	{
		_consoleText.append('\n');
		addText(line);
	}
	
	public JFrame getWindow()
	{
		return _consoleWindow;
	}
	
	public JPanel getDataPanel()
	{
		return _dataPanel;
	}
	
	public void setTitle(String title)
	{
		_consoleWindow.setTitle(title);
	}
	
	public void setSocket(Socket socket)
	{
		_socket = socket;
	}
}
