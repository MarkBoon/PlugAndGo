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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import tesuji.core.gui.AbstractTaggedAction;
import tesuji.core.gui.ActionManager;
import tesuji.core.gui.ActionRule;
import tesuji.core.gui.ActionSwingFactory;

import tesuji.games.go.pattern.common.Pattern;

/**
 	This panel shows the list of patterns that belong to the same PatternGroup.
 */
public class PatternListPanel
	extends JPanel
{
	private static final long serialVersionUID = -8160576746209912452L;

	private static final String SAVE_ACTION =		"save";
	private static final String NEW_ACTION =		"new";
	private static final String COPY_ACTION =		"copy";
	private static final String DELETE_ACTION =		"delete";
	private static final String EDIT_ACTION =		"edit";
	private static final String REPLACE_ACTION =	"replace";
	private static final String ADD_ACTION =		"add";
	private static final String FIND_ACTION =		"find";

	private static final String NEXT_ACTION = 		"next";
	private static final String PREV_ACTION = 		"prev";

	private static final int WIDTH = 3;
	private static final int HEIGHT = 3;
	private static final int NR_PATTERNS = WIDTH*HEIGHT;
	private static final int PATTERN_WIDTH = 100;
	private static final int PATTERN_HEIGHT = 100;

	private PatternComponent[] patternComponents;
	private PatternEditor patternEditor;
	private PatternComponent selectedPatternComponent;
	private JTextField patternNrField;
	
	private boolean editable;

	private PatternListController controller;
	private ActionManager actionManager = new ActionManager();
	
	/**
	 * PatternListPanel constructor.
	 * 
	 * @param controller
	 * @param editable
	 */
	public PatternListPanel(PatternListController controller, boolean editable)
	{
		this.controller = controller;
		this.editable = editable;
		controller.setNrPatterns(NR_PATTERNS);
	
		patternComponents = new PatternComponent[NR_PATTERNS];
		for (int i=0; i<NR_PATTERNS; i++)
			patternComponents[i] = new PatternComponent(false);
	
		initActions();
		initGUIComponents();
		initEvents();
		update();
		actionManager.applyRules();
		getValues();
	}
	
	/**
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	public Dimension getMinimumSize()
	{
		return new Dimension(PATTERN_WIDTH*WIDTH,PATTERN_HEIGHT*HEIGHT);
	}
	
	public ActionManager getActionManager()
	{
		return actionManager;
	}

	private void handleMouseClick(int x, int y)
	{
		Component component = findComponentAt(x,y);
		if (component instanceof PatternComponent)
		{
			if (selectedPatternComponent!=null)
				selectedPatternComponent.setSelected(false);
			PatternComponent patternComponent = (PatternComponent) component;
			patternComponent.setSelected(true);
			controller.setSelectedPattern(patternComponent.getPattern());
			actionManager.applyRules();
		}
	}
	
	/**
	 * Initialise the actions defined for this GUI component
	 */
	private void initActions()
	{
		AbstractTaggedAction saveAction = new AbstractTaggedAction(SAVE_ACTION,"Save")
			{
				public void actionPerformed(ActionEvent event)
				{
					controller.savePatterns();
				}
			};
		AbstractTaggedAction newAction = new AbstractTaggedAction(NEW_ACTION,"New")
			{
				public void actionPerformed(ActionEvent event)
				{
					controller.createNewPattern();
					update();
					invalidate();
					repaint();
				}
			};
		AbstractTaggedAction copyAction = new AbstractTaggedAction(COPY_ACTION,"Copy")
			{
				public void actionPerformed(ActionEvent event)
				{
					controller.duplicateSelectedPattern();
					update();
					patternEditor.invalidate();
					patternEditor.repaint();
				}
			};
		AbstractTaggedAction deleteAction = new AbstractTaggedAction(DELETE_ACTION,"Delete")
			{
				public void actionPerformed(ActionEvent event)
				{
					controller.deleteSelectedPattern();
					actionManager.applyRules();
					update();
					patternEditor.invalidate();
					patternEditor.repaint();
				}
			};
		AbstractTaggedAction editAction = new AbstractTaggedAction(EDIT_ACTION,"Edit")
			{
				public void actionPerformed(ActionEvent event)
				{
					patternEditor.getPatternComponent().setPattern((Pattern)controller.getSelectedPattern().clone());
					actionManager.applyRules();
					patternEditor.getValues();
				}
			};
		AbstractTaggedAction replaceAction = new AbstractTaggedAction(REPLACE_ACTION,"Replace")
			{
				public void actionPerformed(ActionEvent event)
				{
					patternEditor.setValues();
					controller.replaceSelectedPattern((Pattern)patternEditor.getPatternComponent().getPattern().clone());
					actionManager.applyRules();
					update();
					patternEditor.invalidate();
					patternEditor.repaint();
				}
			};
			AbstractTaggedAction addAction = new AbstractTaggedAction(ADD_ACTION,"Add")
			{
				public void actionPerformed(ActionEvent event)
				{
					patternEditor.setValues();
					controller.addNewPattern((Pattern)patternEditor.getPatternComponent().getPattern().clone());
					actionManager.applyRules();
					update();
					patternEditor.invalidate();
					patternEditor.repaint();
				}
			};
		AbstractTaggedAction findAction = new AbstractTaggedAction(FIND_ACTION,"Find")
			{
				public void actionPerformed(ActionEvent event)
				{
					controller.findPattern((Pattern)patternEditor.getPatternComponent().getPattern().clone());
					actionManager.applyRules();
					getValues();
					invalidate();
					update();
				}
			};

		AbstractTaggedAction prevAction = new AbstractTaggedAction(PREV_ACTION,"<")
			{
				public void actionPerformed(ActionEvent event)
				{
					controller.goToPrevious();
					update();
					actionManager.applyRules();
					patternEditor.invalidate();
					patternEditor.repaint();
					getValues();
				}
			};
		AbstractTaggedAction nextAction = new AbstractTaggedAction(NEXT_ACTION,">")
			{
				public void actionPerformed(ActionEvent event)
				{
					controller.goToNext();
					update();
					actionManager.applyRules();
					patternEditor.invalidate();
					patternEditor.repaint();
					getValues();
				}
			};

		ActionRule hasPatternGroupActionRule = new ActionRule()
			{
				public boolean isActive()
				{
					return (controller.getPatternGroup()!=null);
				}
			};
		ActionRule isSelectedActionRule = new ActionRule()
			{
				public boolean isActive()
				{
					return (controller.getSelectedPattern()!=null);
				}
			};
		ActionRule replaceActionRule = new ActionRule()
			{
				public boolean isActive()
				{
					return (controller.getSelectedPattern()!=null && patternEditor.getPatternComponent().getPattern()!=null);
				}
			};

		ActionRule nextActionRule = new ActionRule()
			{
				public boolean isActive()
				{
					return (controller.canGoToNext());
				}
			};
		ActionRule prevActionRule = new ActionRule()
			{
				public boolean isActive()
				{
					return (controller.canGoToPrevious());
				}
			};

		actionManager.addAction(saveAction);
		actionManager.addAction(newAction);
		actionManager.addAction(copyAction);
		actionManager.addAction(deleteAction);
		actionManager.addAction(editAction);
		actionManager.addAction(replaceAction);
		actionManager.addAction(addAction);
		actionManager.addAction(findAction);
		actionManager.addAction(nextAction);
		actionManager.addAction(prevAction);
		actionManager.assignRule(NEW_ACTION,hasPatternGroupActionRule);
		actionManager.assignRule(ADD_ACTION,hasPatternGroupActionRule);
		actionManager.assignRule(FIND_ACTION,hasPatternGroupActionRule);
		actionManager.assignRule(COPY_ACTION,isSelectedActionRule);
		actionManager.assignRule(DELETE_ACTION,isSelectedActionRule);
		actionManager.assignRule(EDIT_ACTION,isSelectedActionRule);
		actionManager.assignRule(REPLACE_ACTION,replaceActionRule);
		actionManager.assignRule(NEXT_ACTION,nextActionRule);
		actionManager.assignRule(PREV_ACTION,prevActionRule);
	}
	
	/**
	 * Define which events this component listens to.
	 */
	private void initEvents()
	{
		addMouseListener(new MouseAdapter()
				{
					public void mouseClicked( MouseEvent event )
					{
						handleMouseClick(event.getX(),event.getY());
					}
				}
			);
		
		controller.addPropertyChangeListener( new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent event)
					{
						actionManager.applyRules();
						invalidate();
						update();
					}
				}
			);
		
		patternNrField.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent event)
					{
						setValues();
						invalidate();
						update();
					}
				}
			);
	}
	
	/**
	 * Put together the GUI components that make up this panel.
	 */
	private void initGUIComponents()
	{
		JPanel contentPanel = new JPanel();
		JToolBar topToolBar = new JToolBar();
		JPanel bottomToolBar = new JPanel();

		topToolBar.add(actionManager.getTaggedAction(SAVE_ACTION));
		topToolBar.addSeparator();
		topToolBar.add(actionManager.getTaggedAction(NEW_ACTION));
		topToolBar.add(actionManager.getTaggedAction(COPY_ACTION));
		topToolBar.add(actionManager.getTaggedAction(DELETE_ACTION));

		bottomToolBar.setLayout(new FlowLayout(FlowLayout.CENTER));
		bottomToolBar.add(ActionSwingFactory.createJButton(actionManager.getTaggedAction(PREV_ACTION)));
		bottomToolBar.add(ActionSwingFactory.createJButton(actionManager.getTaggedAction(NEXT_ACTION)));
		patternNrField = new JTextField(4);
		bottomToolBar.add(patternNrField);
		
		contentPanel.setLayout(new GridLayout(WIDTH,HEIGHT));
		for (int i=0; i<NR_PATTERNS; i++)
		{
			final PatternComponent patternComponent = patternComponents[i];
			contentPanel.add(patternComponent);
			patternComponent.addMouseListener(new MouseAdapter()
					{
						public void mouseClicked(MouseEvent event)
						{
							if (selectedPatternComponent!=null)
								selectedPatternComponent.setSelected(false);
							controller.setSelectedPattern(patternComponent.getPattern());
							System.out.println(patternComponent.getPattern().toString());
							actionManager.applyRules();
							selectedPatternComponent = patternComponent;
							patternComponent.setSelected(true);
							update();
						}
					}
				);
		}

		setLayout(new BorderLayout());
		add(contentPanel,BorderLayout.CENTER);
		add(topToolBar,BorderLayout.NORTH);
		add(bottomToolBar,BorderLayout.SOUTH);

		if (editable)
		{
			patternEditor = new PatternEditor();
			add(patternEditor,BorderLayout.EAST);
			topToolBar.addSeparator();
			topToolBar.add(actionManager.getTaggedAction(EDIT_ACTION));
			topToolBar.add(actionManager.getTaggedAction(REPLACE_ACTION));
			topToolBar.add(actionManager.getTaggedAction(ADD_ACTION));
			topToolBar.add(actionManager.getTaggedAction(FIND_ACTION));
		}
	}
	
	private void update()
	{
		boolean updateSelection = (selectedPatternComponent==null || selectedPatternComponent.getPattern()!=controller.getSelectedPattern());
		if (updateSelection && selectedPatternComponent!=null)
			selectedPatternComponent.setSelected(false);
			
		Pattern[] patterns = controller.getPatterns();
		for (int i=0; i<patterns.length; i++)
		{
			Pattern pattern = patterns[i];
			PatternComponent patternComponent = patternComponents[i];
			patternComponent.setPattern(pattern);
			if (updateSelection && pattern!=null && pattern==controller.getSelectedPattern())
			{
				patternComponent.setSelected(true);
				selectedPatternComponent = patternComponent;
			}
		}
		if (selectedPatternComponent!=null)
			controller.setSelectedPattern(selectedPatternComponent.getPattern());
	}
	
	/**
	 * 'Get' the values from the controller and 'set' them in the
	 * GUI components.
	 */
	private void getValues()
	{
		patternNrField.setText(""+controller.getFirstPatternNr());
	}
		
	/**
	 * Get the contents of the GUI components and 'set' them
	 * in the data object held by patternComponent.
	 */
	private void setValues()
	{
		String text = patternNrField.getText().trim();
		if(text.length()>0)
		{
			int nr = Integer.parseInt(text);
			controller.setFirstPatternNr(nr);
		}
	}
	/**
	 * @see javax.swing.JComponent#update(java.awt.Graphics)
	 */
	public void update(Graphics g)
	{
		update();
		super.update(g);
	}
}
