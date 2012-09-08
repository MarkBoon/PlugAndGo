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

import java.beans.PropertyChangeListener;

/**
 	Interface describing a GUI action. The definition is very similar to
 	the Swing Action interface, with the only difference that the string
 	used to display the action in a menu-item or on a button is in the
 	property TITLE instead of NAME
*/
public interface Action extends javax.swing.Action
{
	// These are predefined property-names for actions.
	public static final String TAG =				"tag";
	public static final String DEFAULT =			"default";
	public static final String SMALL_ICON =			"smallIcon";
	public static final String SHORT_DESCRIPTION =	"shortDescription";
	public static final String LONG_DESCRIPTION =	"longDescription";
	public static final String ENABLED =			"enabled";

	public void addPropertyChangeListener(PropertyChangeListener listener);
	public Object getValue(String name);
	public boolean isEnabled();
	public void putValue(String name, Object value);
	public void removePropertyChangeListener(PropertyChangeListener listener);
	public void setEnabled(boolean flag);
}
