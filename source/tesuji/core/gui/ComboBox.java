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
package tesuji.core.gui;

import javax.swing.JComboBox;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tesuji.core.model.SelectableListModel;

/**
 * Sub-class of JComboBox that takes a SelectableListModel as a model instead of a ComboBoxModel
 */
public class ComboBox
    extends JComboBox
{
	private static final long serialVersionUID = -8498553482301075933L;

	private ListSelectionListener _selectionListener;
	private SelectableListModel _model;
	
	/** Creates a new instance of ComboBox */
    public ComboBox()
    {
    	initSelectionListener();
    }
    
    /**
     * ComboBox constructor
     * 
     * @param model used for maintaining the selection
     */
    public ComboBox(final SelectableListModel model)
    {
        super(model);
        initSelectionListener();
        setModel(model);
    }
    
    private void initSelectionListener()
    {
    	_selectionListener = new ListSelectionListener()
	        {
	            public void valueChanged(ListSelectionEvent event)
	            {
	                ComboBox.this.setSelectedIndex(_model.getSelectedIndex());
	                ComboBox.this.repaint();
	            }
	        };
    }
    
	/**
	 * @param model used for maintaining the selection
	 */
    public void setModel(SelectableListModel model)
    {
    	if (_model!=null)
    		_model.removeListSelectionListener(_selectionListener);
    	
        super.setModel(model);
        _model = model;
        
        model.addListSelectionListener(_selectionListener);
    }
}
