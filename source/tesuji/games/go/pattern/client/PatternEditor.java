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

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import tesuji.core.gui.AbstractTaggedAction;
import tesuji.core.gui.ActionSwingFactory;
import tesuji.core.gui.ActionManager;
import tesuji.core.gui.TaggedAction;
//import tesuji.games.go.monte_carlo.MCPatternAdministration;
//import tesuji.games.go.monte_carlo.MCPatternEvaluator;
import tesuji.games.go.pattern.common.Pattern;
//import tesuji.games.go.pattern.common.PatternGroup;
//import tesuji.games.go.pattern.incremental.IncrementalPatternMatcher;

/**
 *	A GUI component for editing a pattern.
 *	It holds a PatternComponent for the display of the pattern being edited.
 */
public class PatternEditor
	extends JPanel
{
	private static final long serialVersionUID = -4438532534306582778L;

	// Actions for modifying the size of the pattern.
	public static final String ADD_TOP_ACTION = "addTopRow";
	public static final String DEL_TOP_ACTION = "delTopRow";
	public static final String ADD_BOTTOM_ACTION = "addBottomRow";
	public static final String DEL_BOTTOM_ACTION = "delBottomRow";
	public static final String ADD_LEFT_ACTION = "addLeftColumn";
	public static final String DEL_LEFT_ACTION = "delleftColumn";
	public static final String ADD_RIGHT_ACTION = "addRightColumn";
	public static final String DEL_RIGHT_ACTION = "delRightColumn";

	public static final String COMPUTE_URGENCY_ACTION = "computeUrgency";

	private PatternComponent patternComponent;
	private ActionManager actionManager;
	
	private JCheckBox topEdgeCheckBox;
	private JCheckBox leftEdgeCheckBox;
	private JTextField textField;
	private JTextField blackUrgencyField;
	private JTextField whiteUrgencyField;
	private JTextField blackNrSuccessField;
	private JTextField whiteNrSuccessField;
	private JTextField blackNrOccurrencesField;
	private JTextField whiteNrOccurrencesField;
	private JTextArea conditionsField;
	
	boolean updating = false;
	
	/**
	 * PatternEditor default constructor.
	 */
	public PatternEditor()
	{
		initActions();
		initGUIComponents();
		initEvents();
	}
	
	public PatternComponent getPatternComponent()
	{
		return patternComponent;
	}
	
	/**
	 * Initialise the actions defined for this component
	 */
	private void initActions()
	{
		actionManager = new ActionManager();

		TaggedAction addTopAction = new AbstractTaggedAction(ADD_TOP_ACTION,"+")
			{
				public void actionPerformed(ActionEvent event)
				{
					if (patternComponent.getPattern()!=null)
						patternComponent.getPattern().addTopRow();
				}
			};
		TaggedAction delTopAction = new AbstractTaggedAction(DEL_TOP_ACTION,"-")
			{
				public void actionPerformed(ActionEvent event)
				{
					if (patternComponent.getPattern()!=null)
						patternComponent.getPattern().removeTopRow();
				}
			};
		TaggedAction addBottomAction = new AbstractTaggedAction(ADD_BOTTOM_ACTION,"+")
			{
				public void actionPerformed(ActionEvent event)
				{
					if (patternComponent.getPattern()!=null)
						patternComponent.getPattern().addBottomRow();
				}
			};
		TaggedAction delBottomAction = new AbstractTaggedAction(DEL_BOTTOM_ACTION,"-")
			{
				public void actionPerformed(ActionEvent event)
				{
					if (patternComponent.getPattern()!=null)
						patternComponent.getPattern().removeBottomRow();
				}
			};
		TaggedAction addLeftAction = new AbstractTaggedAction(ADD_LEFT_ACTION,"+")
			{
				public void actionPerformed(ActionEvent event)
				{
					if (patternComponent.getPattern()!=null)
						patternComponent.getPattern().addLeftColumn();
				}
			};
		TaggedAction delLeftAction = new AbstractTaggedAction(DEL_LEFT_ACTION,"-")
			{
				public void actionPerformed(ActionEvent event)
				{
					if (patternComponent.getPattern()!=null)
						patternComponent.getPattern().removeLeftColumn();
				}
			};
		TaggedAction addRightAction = new AbstractTaggedAction(ADD_RIGHT_ACTION,"+")
			{
				public void actionPerformed(ActionEvent event)
				{
					if (patternComponent.getPattern()!=null)
						patternComponent.getPattern().addRightColumn();
				}
			};
		TaggedAction delRightAction = new AbstractTaggedAction(DEL_RIGHT_ACTION,"-")
			{
				public void actionPerformed(ActionEvent event)
				{
					if (patternComponent.getPattern()!=null)
						patternComponent.getPattern().removeRightColumn();
				}
			};
		TaggedAction computeUrgencyAction = new AbstractTaggedAction(COMPUTE_URGENCY_ACTION,"Compute")
			{
				public void actionPerformed(ActionEvent event)
				{
					/*
					Pattern pattern = patternComponent.getPattern();
					PatternSet patternSet = new PatternSet();
					PatternGroup group = new PatternGroup();
					group.getPatternList().add(patternComponent.getPattern());
					patternSet.setPatternGroup(group);
					MCPatternAdministration administration = new MCPatternAdministration(patternSet);
					administration.setBoardSize(19);
					administration.setKomi(2);
					MCPatternEvaluator evaluator = new MCPatternEvaluator(administration);
					int blackUrgency = evaluator.playout(5000, pattern.getPatternNr(), false);
					int whiteUrgency = evaluator.playout(5000, pattern.getPatternNr(), true);
					pattern.setUrgencyValueBlack(blackUrgency);
					pattern.setUrgencyValueWhite(whiteUrgency);
					if (patternComponent.getPattern()!=null)
						patternComponent.getPattern().removeRightColumn();
					getValues();
					*/
				}
			};

		actionManager.addAction(addTopAction);
		actionManager.addAction(delTopAction);
		actionManager.addAction(addBottomAction);
		actionManager.addAction(delBottomAction);
		actionManager.addAction(addLeftAction);
		actionManager.addAction(delLeftAction);
		actionManager.addAction(addRightAction);
		actionManager.addAction(delRightAction);
		actionManager.addAction(computeUrgencyAction);
	}
	
	/**
	 * Put together the GUI components for the editor.
	 */
	private void initGUIComponents()
	{
		JPanel topButtons =		new JPanel();
		JPanel bottomButtons =	new JPanel();
		JPanel leftButtons =	new JPanel();
		JPanel rightButtons =	new JPanel();
		JPanel patternPanel =	new JPanel();
		JPanel bottomPanel =	new JPanel();
		JPanel fieldPanel = 	new JPanel();
		JPanel conditionsPanel= new JPanel();
		
		topEdgeCheckBox =		new JCheckBox();
		leftEdgeCheckBox =		new JCheckBox();
		textField =				new JTextField();
		blackUrgencyField =		new JTextField(4);
		whiteUrgencyField =		new JTextField(4);
		blackNrSuccessField = 	new JTextField(4);
		whiteNrSuccessField = 	new JTextField(4);
		blackNrOccurrencesField=new JTextField(4);
		whiteNrOccurrencesField=new JTextField(4);
		conditionsField =		new JTextArea(10,3);
		
		JButton addTopButton = ActionSwingFactory.createJButton(actionManager.getTaggedAction(ADD_TOP_ACTION));
		JButton delTopButton = ActionSwingFactory.createJButton(actionManager.getTaggedAction(DEL_TOP_ACTION));
		topButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
		topButtons.add(topEdgeCheckBox);
		topButtons.add(addTopButton);
		topButtons.add(delTopButton);

		JButton addBottomButton = ActionSwingFactory.createJButton(actionManager.getTaggedAction(ADD_BOTTOM_ACTION));
		JButton delBottomButton = ActionSwingFactory.createJButton(actionManager.getTaggedAction(DEL_BOTTOM_ACTION));
		bottomButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
		bottomButtons.add(addBottomButton);
		bottomButtons.add(delBottomButton);

		JButton addLeftButton = ActionSwingFactory.createJButton(actionManager.getTaggedAction(ADD_LEFT_ACTION));
		JButton delLeftButton = ActionSwingFactory.createJButton(actionManager.getTaggedAction(DEL_LEFT_ACTION));
		leftButtons.setLayout(new BoxLayout(leftButtons,BoxLayout.Y_AXIS));
		leftButtons.add(leftEdgeCheckBox);
		leftButtons.add(addLeftButton);
		leftButtons.add(delLeftButton);

		JButton addRightButton = ActionSwingFactory.createJButton(actionManager.getTaggedAction(ADD_RIGHT_ACTION));
		JButton delRightButton = ActionSwingFactory.createJButton(actionManager.getTaggedAction(DEL_RIGHT_ACTION));
		rightButtons.setLayout(new BoxLayout(rightButtons,BoxLayout.Y_AXIS));
		rightButtons.add(addRightButton);
		rightButtons.add(delRightButton);

		patternPanel.setLayout(new BorderLayout());
		patternPanel.add(topButtons,BorderLayout.NORTH);
		patternPanel.add(bottomButtons,BorderLayout.SOUTH);
		patternPanel.add(leftButtons,BorderLayout.WEST);
		patternPanel.add(rightButtons,BorderLayout.EAST);
		patternComponent = new PatternComponent(true);
		patternPanel.add(patternComponent,BorderLayout.CENTER);

//		JButton computeButton = ActionSwingFactory.createJButton(actionManager.getTaggedAction(COMPUTE_URGENCY_ACTION));
		fieldPanel.setLayout(new GridLayout(0,3));
		fieldPanel.add(new JLabel("Urgency:"));
		fieldPanel.add(blackUrgencyField);
		fieldPanel.add(whiteUrgencyField);
//		urgencyPanel.add(computeButton);
		blackUrgencyField.setHorizontalAlignment(JTextField.RIGHT);
		whiteUrgencyField.setHorizontalAlignment(JTextField.RIGHT);
		fieldPanel.add(new JLabel("Black Ratio:"));
		fieldPanel.add(blackNrSuccessField);
		fieldPanel.add(blackNrOccurrencesField);
		fieldPanel.add(new JLabel("White Ratio:"));
		fieldPanel.add(whiteNrSuccessField);
		fieldPanel.add(whiteNrOccurrencesField);
		
		conditionsPanel.setLayout(new GridLayout(1,1));
		conditionsPanel.setBorder(new TitledBorder("Conditions"));
		conditionsPanel.add(conditionsField);
		
		bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.Y_AXIS));
		bottomPanel.add(fieldPanel);
		bottomPanel.add(conditionsPanel);
		
		setLayout(new BorderLayout());
		add(patternPanel,BorderLayout.CENTER);
		add(textField,BorderLayout.NORTH);
		add(bottomPanel,BorderLayout.SOUTH);
	}
	
	/**
	 * Initialise the events this component listens to.
	 */
	private void initEvents()
	{
		ChangeListener changeListener = new ChangeListener()
			{
				public void stateChanged(ChangeEvent event)
				{
					setValues();
					patternComponent.invalidate();
					repaint();
				}
			};
		topEdgeCheckBox.addChangeListener(changeListener);
		leftEdgeCheckBox.addChangeListener(changeListener);
		
		ActionListener textChangeListener = new ActionListener()
			{

				public void actionPerformed(ActionEvent arg0)
                {
	                setValues();
                }
			
			};
			
		textField.addActionListener(textChangeListener);
		blackUrgencyField.addActionListener(textChangeListener);
		whiteUrgencyField.addActionListener(textChangeListener);
		blackNrSuccessField.addActionListener(textChangeListener);
		whiteNrSuccessField.addActionListener(textChangeListener);
		blackNrOccurrencesField.addActionListener(textChangeListener);
		whiteNrOccurrencesField.addActionListener(textChangeListener);
	}
	
	/**
	 * 'Get' the values from the controller and 'set' them in the
	 * GUI components.
	 */
	public void getValues()
	{
		updating = true;
		Pattern pattern = patternComponent.getPattern();
		textField.setText(pattern.getText());
		conditionsField.setText(pattern.getConditions());
		topEdgeCheckBox.setSelected(pattern.hasTopEdge());
		leftEdgeCheckBox.setSelected(pattern.hasLeftEdge());
		blackUrgencyField.setText(""+pattern.getUrgencyValueBlack());
		whiteUrgencyField.setText(""+pattern.getUrgencyValueWhite());
		blackNrSuccessField.setText(""+pattern.getBlackNrSuccesses());
		whiteNrSuccessField.setText(""+pattern.getWhiteNrSuccesses());
		blackNrOccurrencesField.setText(""+pattern.getBlackNrOccurrences());
		whiteNrOccurrencesField.setText(""+pattern.getWhiteNrOccurrences());
		updating = false;
	}
	
	/**
	 * Get the contents of the GUI components and 'set' them
	 * in the data object held by patternComponent.
	 */
	public void setValues()
	{
		if (!updating)
		{
			Pattern pattern = patternComponent.getPattern();
			pattern.setText(textField.getText());
			pattern.setConditions(conditionsField.getText());
			pattern.setTopEdge(topEdgeCheckBox.isSelected());
			pattern.setLeftEdge(leftEdgeCheckBox.isSelected());
			pattern.setUrgencyValueBlack(Double.parseDouble(blackUrgencyField.getText()));
			pattern.setUrgencyValueWhite(Double.parseDouble(whiteUrgencyField.getText()));
			pattern.setBlackNrSuccesses(Integer.parseInt(blackNrSuccessField.getText()));
			pattern.setWhiteNrSuccesses(Integer.parseInt(whiteNrSuccessField.getText()));
			pattern.setBlackNrOccurrences(Integer.parseInt(blackNrOccurrencesField.getText()));
			pattern.setWhiteNrOccurrences(Integer.parseInt(whiteNrOccurrencesField.getText()));
		}
	}
}
