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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tesuji.core.gui.AbstractTaggedAction;
import tesuji.core.gui.ActionManager;
import tesuji.core.gui.ComboBox;

import tesuji.games.go.pattern.common.PatternGroup;

/**
 * GUI component for displaying a list of PatternGroup objects
 * and the (editable) details of the selected one from the list.
 */
public class PatternGroupPanel
	extends JPanel
{
	private static final long serialVersionUID = 7917488375792716408L;

	private static final String NEW_GROUP_ACTION = "new group";
	private static final String DELETE_GROUP_ACTION = "delete group";
	private static final String UPDATE_GROUP_ACTION = "update group";
	
	private JFrame parentFrame;
	private PatternGroupController controller;
	private ActionManager actionManager = new ActionManager();
	
	JTextField groupNameField = new JTextField(20);		
	JTextField descriptionField = new JTextField(20);
	
	/**
	 * PatternGroupPanel constructor
	 * 
	 * @param controller managing the PatternGroup data
	 * @param parent window
	 */
	public PatternGroupPanel(PatternGroupController controller, JFrame parent)
	{
		this.controller = controller;
		parentFrame = parent;
		
		initActions();
		initGUIComponents();
		initEvents();
	}

	/**
	 * Initialise the actions defined for this GUI component
	 */
	private void initActions()
	{
		AbstractTaggedAction newGroupAction = new AbstractTaggedAction(NEW_GROUP_ACTION,"New Group")
		{
			public void actionPerformed(ActionEvent event)
			{
				String groupName = JOptionPane.showInputDialog(parentFrame,"Enter group name");
				if (groupName!=null && groupName.length()>0)
					controller.createNewPatternGroup(groupName);
			}
		};
		
		AbstractTaggedAction deleteGroupAction = new AbstractTaggedAction(DELETE_GROUP_ACTION,"Delete Group")
		{
			public void actionPerformed(ActionEvent event)
			{
				controller.deleteSelectedPatternGroup();
			}
		};
		
		AbstractTaggedAction updateGroupAction = new AbstractTaggedAction(UPDATE_GROUP_ACTION,"Save Group")
		{
			public void actionPerformed(ActionEvent event)
			{
				setValues();
				controller.updateSelectedPatternGroup();
			}
		};
		
		actionManager.addAction(newGroupAction);
		actionManager.addAction(deleteGroupAction);
		actionManager.addAction(updateGroupAction);
	}
	
	/**
	 * Put together the GUI components that make up this panel.
	 */
	private void initGUIComponents()
	{
		JToolBar toolBar = new JToolBar();
		JPanel contentPanel = new JPanel();
		JPanel toolPanel = new JPanel();
		
		toolBar.add(actionManager.getTaggedAction(NEW_GROUP_ACTION));
		toolBar.addSeparator();
		toolBar.add(actionManager.getTaggedAction(UPDATE_GROUP_ACTION));
		toolBar.addSeparator();
		toolBar.add(actionManager.getTaggedAction(DELETE_GROUP_ACTION));
		
		toolPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		toolPanel.add(toolBar);
		
		ComboBox groupComboBox = new ComboBox(controller.getGroupList());
		contentPanel.setLayout(new GridLayout(1,3));
		contentPanel.add(groupComboBox);
		contentPanel.add(groupNameField);
		contentPanel.add(descriptionField);
		
		setLayout(new BorderLayout());
		add(toolPanel,BorderLayout.NORTH);
		add(contentPanel,BorderLayout.CENTER);
		getValues();
	}
	
	/**
	 * Define which events this component listens to.
	 */
	private void initEvents()
	{
		controller.getGroupList().addListSelectionListener(new ListSelectionListener()
				{
					public void valueChanged(ListSelectionEvent event)
					{
						getValues();
					}
				}
			);
	}
	
	/**
	 * 'Get' the values from the controller and 'set' them in the
	 * GUI components.
	 */
	private void getValues()
	{
		if (controller.getSelectedPatternGroup()==null)
		{
			groupNameField.setText("");
			descriptionField.setText("");
		}
		else
		{
			groupNameField.setText(controller.getSelectedPatternGroup().getGroupName());
			descriptionField.setText(controller.getSelectedPatternGroup().getDescription());
		}
	}
	
	/**
	 * Get the contents of the GUI components and 'set' them
	 * in the data object held by patternComponent.
	 */
	private void setValues()
	{
		PatternGroup group = controller.getSelectedPatternGroup();
		if (group!=null)
		{
			group.setGroupName(groupNameField.getText());
			group.setDescription(descriptionField.getText());
		}
	}
}
