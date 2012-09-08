/**
 *	Product: Tesuji Software Go Library.<br><br>
 *
 *	<font color="#CC6600"><font size=-1>
 *	Copyright (c) 2001-2004 Tesuji Software B.V.<br>
 *	All rights reserved.<br><br>
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a
 *	copy of this software and associated documentation files (the
 *	"Software"), to deal in the Software without restriction, including
 *	without limitation the rights to use, copy, modify, merge, publish,
 *	distribute, and/or sell copies of the Software, and to permit persons
 *	to whom the Software is furnished to do so, provided that the above
 *	copyright notice(s) and this permission notice appear in all copies of
 *	the Software and that both the above copyright notice(s) and this
 *	permission notice appear in supporting documentation.<br><br>
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 *	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.<br><br>
 *
 *	Except as contained in this notice, the name of a Tesuji Software
 *	shall not be used in advertising or otherwise to promote the sale, use
 *	or other dealings in this Software without prior written authorization
 *	of Tesuji Software.<br><br>
 *	<font color="#00000"><font size=+1>
 */
package tesuji.games.go.pattern.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import tesuji.core.util.LoggerConfigurator;
import tesuji.games.go.pattern.common.PatternGroup;
import tesuji.games.go.pattern.common.PatternManager;

/**
 	Main class for the pattern editor.
 */
public class PatternMain
{
	/**
	 * PatternMain default constructor.
	 */
	public PatternMain() 
	{
	}
	
	private void init()
	{
		// Create an instantiation of the PatternManager, can be a
		// HibernatePatternManager or a PatternManagerEJB
		// Other implementations of PatternManager could be used here,
		// like a flat-file implementation.
//		PatternManager patternManager = createPatternManagerEJB();
		PatternManager patternManager = tesuji.games.go.pattern.common.HibernatePatternManager.getSingleton();
//		PatternManager patternManager = tesuji.games.go.pattern.common.FilePatternManager.getSingleton();
		
		JFrame frame = new JFrame();
		
		final PatternGroupController groupController = new PatternGroupController(patternManager);
		final PatternListController patternController = new PatternListController(groupController.getSelectedPatternGroup(),patternManager);
		
		// The following makes sure that when the user selects another group,
		// this group gets set in the PatternListController.
		groupController.getGroupList().addListSelectionListener(new ListSelectionListener()
				{
					public void valueChanged(ListSelectionEvent event)
					{
						PatternGroup group = (PatternGroup)groupController.getGroupList().getSelectedItem();
						patternController.setPatternGroup(group);
					}
				}
			);
		
		// Now construct the panels to put in the main frame.
		JPanel mainPanel = new JPanel();
		PatternGroupPanel groupPanel = new PatternGroupPanel(groupController,frame);
		PatternListPanel patternPanel = new PatternListPanel(patternController,true);	
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(groupPanel,BorderLayout.NORTH);
		mainPanel.add(patternPanel,BorderLayout.CENTER);
		
		frame.setSize(800,600);
		frame.addWindowListener(new WindowAdapter()
				{
					public void windowClosing(WindowEvent event)
					{
						//When the main window is closed the application terminates.
						System.exit(0);
					}
				}
			);

		//frame.setBackground(Color.lightGray);
		frame.getContentPane().setLayout( new GridLayout(1,1));
		frame.getContentPane().add(mainPanel);
		frame.setVisible(true);
	}
	
	/*
	private PatternManager createPatternManagerEJB()
	{
		try
		{
			return (PatternManager)EJBObjectFactory.createEJB(PatternManager.JNDI_NAME,PatternManagerHome.class);
		}
		catch (NamingException ex)
		{
			ReportCenter.reportException("Couldn't find server: "+PatternManager.JNDI_NAME,ex);
			System.exit(-1);
		}
		catch (RemoteException ex)
		{
			ReportCenter.reportException("Couldn't connect to server: "+PatternManager.JNDI_NAME,ex);
			System.exit(-1);
		}
		// Never gets here.
		return null;
	}
	*/
	
	public static void main(String[] args)
	{
		LoggerConfigurator.configure();
		Logger logger = Logger.getRootLogger();
		logger.setLevel(Level.INFO);
				
		PatternMain editor = new PatternMain();
		editor.init();
	}

}
