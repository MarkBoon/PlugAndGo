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

import java.util.Hashtable;
import java.util.Enumeration;

/**
	The ActionManager holds a collection of Actions which it can group into
	logical named groups and associate ActionRules to.<br><br>

	An action can only belong to one group.<br><br>
	
	An action cannot be assigned more than one rule.<br><br>
	
	When the rules are applied, ActionManager iterates all of the relevant
	actions and tests if the action should be enabled by querying the rule (if
	there is one) associated with it. The action is enabled or disabled according
	to the return of ActionRule.isActive().

	@see NamedAction
	@see ActionRule<br><br>
*/
public class ActionManager
{
	public static final String DEFAULT_GROUP = "DEFAULT_GROUP";
	protected Hashtable<String,Hashtable<String,TaggedAction>> groups =	new Hashtable<String,Hashtable<String,TaggedAction>>();
	protected Hashtable<String,TaggedAction> actions =	new Hashtable<String,TaggedAction>();
	protected Hashtable<String,ActionRule> rules =	new Hashtable<String,ActionRule>();

	/**
	 * Add an action to the manager. The action will be placed in the default group
	 * @param action The action to add.
	 * @param group The group to add this action to. If this is null the action is added to the 'default' group
	 */
	public void addAction(TaggedAction action)
	{
		addAction(action, null);
	}
	/**
	 * Add an action to the manager.
	 * @param action The action to add. The actions name is used to identify the action.
	 * @param group The group to add this action to. If this is null the action is added to the 'default' group
	 */
	public void addAction(TaggedAction action, String group)
	{
		Hashtable<String,TaggedAction> g = null;
		if (group == null)
			group = DEFAULT_GROUP;

		synchronized (groups)
		{
			g = groups.get(group);
			if (g == null)
			{
				g = new Hashtable<String,TaggedAction>();
				groups.put(group, g);
			}

			String tag = action.getTag();

			if (actions.get(tag) == null)
				actions.put(tag, action);
				
			if (g.get(tag) == null)
				g.put(tag, action);
		}
	}
	/**
	 * Apply the rules to all actions in a group
	 * @param The group to apply the rules to
	 */
	public synchronized void applyRule(String actionName)
	{
		ActionRule ar = rules.get(actionName);
		if (ar != null)
		{
			Action a = actions.get(actionName);
			if (a != null)
				a.setEnabled(ar.isActive());
		}
	}
	/**
	 * Apply the rules to all actions
	 */
	public synchronized void applyRules()
	{
		Enumeration<String> e = actions.keys();
		while (e.hasMoreElements())
		{
			String name = e.nextElement();
			ActionRule ar = rules.get(name);
			if (ar != null)
			{
				Action a = actions.get(name);
				if (a != null)
					a.setEnabled(ar.isActive());
			}
		}

	}
	/**
	 * Apply the rules to all actions in a group
	 * @param The group to apply the rules to
	 */
	public synchronized void applyRules(String group)
	{
		Hashtable<String,TaggedAction> g = groups.get(group);
		if (g == null)
			return;

		Enumeration<String> e = g.keys();
		while (e.hasMoreElements())
		{
			String name = e.nextElement();
			ActionRule ar = rules.get(name);
			if (ar != null)
			{
				Action a = (Action)g.get(name);
				if (a != null)
					a.setEnabled(ar.isActive());
			}
		}
	}
	/**
	 * Assign a rule to an action.
	 * @param actionName The name of the action to add the rule to. This name should match the Action.getValue(Action.TAG)
	 * @param rule The rule to assign to the action.
	 */
	public void assignRule(String actionTag, ActionRule rule)
	{
		synchronized (rules)
		{
			rules.put(actionTag, rule);
		}
	}
	/**
	 * This method is implemented to conform to the ActionManager interface.
	 * use getNamedAction(name) to get a NamedAction with out needing to cast.
	 * @param name The name of the action to return
	 * @return The Action or null if not found
	 */
	public Action getAction(String name)
	{
		return actions.get(name);
	}
	/**
	 * @return All the action names. These names are the TAG names which the
	 * action manager uses to identify the actions.
	 */
	public String[] getActionNames()
	{
		synchronized (actions)
		{
			String[] names = new String[actions.size()];
			Enumeration<String> e = actions.keys();
			for (int i=0; e.hasMoreElements(); i++)
			{
				names[i] = e.nextElement();
			}
			return names;
		}
	}
	/**
	 * @param group The group to get the action names for.
	 * @return All the action names for the specified group. These names are
	 * the TAG names which the action manager uses to identify the actions.
	 */
	public String[] getActionNames(String group)
	{
		Hashtable<String,TaggedAction> g = groups.get(group);
		if (g == null)
			return null;

		synchronized (g)
		{
			String[] names = new String[actions.size()];
			Enumeration<String> e = actions.keys();
			for (int i=0; e.hasMoreElements(); i++)
			{
				names[i] = e.nextElement();
			}
			return names;
		}
	}
	/**
	 * This method is implemented to conform to the ActionManager interface.
	 * use getTaggedActions to get TaggedActions with out needing to cast
	 * @return All the actions.
	 */
	public Action[] getActions()
	{
		return getTaggedActions();
	}
	/**
	 * This method is implemented to conform to the ActionManager interface.
	 * use getTaggedActions(group) to get TaggedActions with out needing to cast.
	 * @param group The group to get the actions for.
	 * @return All the actions for the specified group.
	 */
	public Action[] getActions(String group)
	{
		return getTaggedActions(group);
	}
	/**
	 * @return All the group names.
	 */
	public String[] getGroupNames()
	{
		synchronized (groups)
		{
			String[] names = new String[groups.size()];
			Enumeration<String> e = groups.keys();
			for (int i=0; e.hasMoreElements(); i++)
			{
				names[i] = e.nextElement();
			}
			return names;
		}
	}
	/**
	 * @param actionName The name of the action to get the rules for.
	 * @return The rule for the specified action. If there are no rules for the action, null is returned.
	 */
	public ActionRule getRuleForAction(String actionName)
	{
		return rules.get(actionName);
	}
	/**
	 * @param actionName The name of the action to return
	 * @return The NamedAction or null if not found
	 */
	public TaggedAction getTaggedAction(String actionName)
	{
		return (TaggedAction)actions.get(actionName);
	}
	/**
	 * @return All the NamedActions.
	 */
	public TaggedAction[] getTaggedActions()
	{
		synchronized (actions)
		{
			TaggedAction[] a = new TaggedAction[actions.size()];
			Enumeration<TaggedAction> e = actions.elements();
			for (int i=0; e.hasMoreElements(); i++)
			{
				a[i] = e.nextElement();
			}
			return a;
		}
	}
	/**
	 * @param group The group to get the actions for.
	 * @return All the TaggedActions for the specified group.
	 */
	public TaggedAction[] getTaggedActions(String group)
	{
		Hashtable<String,TaggedAction> g = groups.get(group);
		if (g == null)
			return null;

		synchronized (g)
		{
			TaggedAction[] a = new TaggedAction[g.size()];
			Enumeration<TaggedAction> e = g.elements();
			for (int i=0; e.hasMoreElements(); i++)
			{
				a[i] = e.nextElement();
			}
			return a;
		}
	}
	public Action removeAction(String actionName)
	{
		Action a = actions.remove(actionName);
		if (a != null)
		{
			// go through the groups removing the action
			Enumeration<Hashtable<String,TaggedAction>> grps = groups.elements();
			while (grps.hasMoreElements())
			{
				Hashtable<String,TaggedAction> grp = grps.nextElement();
				grp.remove(actionName);
			}
		}
		rules.remove(actionName);

		return a;
	}
	public void removeAll()
	{
		Enumeration<String> tags = actions.keys();
		while (tags.hasMoreElements())
		{
			removeAction(tags.nextElement());
		}
	}
}
