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
package tesuji.games.go.pattern.common;

import java.io.Serializable;

import tesuji.core.util.ArrayList;

import static tesuji.games.go.common.GoConstant.NOCARE;

/**
 	PatternGroup defines a collection of patterns that belong together.
 	This usually means they're stored in the same file. Graphical
 	pattern editors should use this class instead of bothering with
 */
public class PatternGroup
	implements Serializable
{
	private static final long serialVersionUID = 5757043274052721477L;

	public static final String PATTERN_GROUP_TAG = "pattern-group";

	transient private ArrayList<Pattern> patternList;
	private String groupName;
	private int groupId;
	private String description;

	public PatternGroup()
	{
		description = "[description]";
		patternList = new ArrayList<Pattern>();
	}
	
	public ArrayList<Pattern> getPatternList()
	{
		return patternList;
	}
	
	public void setPatternList(ArrayList<Pattern> list)
	{
		patternList = list;
	}
	
	public void cleanup(PatternManager manager)
	{
		for (int i=patternList.size(); --i>=0;)
		{
			Pattern pattern = patternList.get(i);
			if (!pattern.isUseful())
			{
				patternList.delete(i);
				manager.removePattern(pattern);
			}
		}
	}
	
	public void purgeMutilations(PatternManager manager)
	{
		for (int i=patternList.size(); --i>=0;)
		{
			Pattern pattern = patternList.get(i);
			if (pattern.getRelativePoint(0, 0)==NOCARE 
			 || pattern.getRelativePoint(1, 0)==NOCARE
			 || pattern.getRelativePoint(0, 1)==NOCARE 
			 || pattern.getRelativePoint(-1, 0)==NOCARE 
			 || pattern.getRelativePoint(0, -1)==NOCARE)
			{
				patternList.delete(i);
				manager.removePattern(pattern);
			}
		}		
	}

	public void purgeMeaningless(PatternManager manager)
	{
		for (int i=patternList.size(); --i>=0;)
		{
			Pattern pattern = patternList.get(i);
			if (pattern.getBlackNrOccurrences()>10000 && (pattern.getBlackNrOccurrences() / pattern.getBlackNrSuccesses()) > 50)
			{
				patternList.delete(i);
				manager.removePattern(pattern);
			}
		}		
	}
	/**
	 * @return Returns the groupName.
	 */
	public String getGroupName()
	{
		return groupName;
	}
	
	/**
	 * @param name The group-name to set
	 */
	public void setGroupName(String name)
	{
		groupName = name;
	}
	
	/**
	 * @return Returns the groupId.
	 */
	public int getGroupId()
	{
		return groupId;
	}
	
	/**
	 * @param groupId The groupId to set.
	 */
	public void setGroupId(int groupId)
	{
		this.groupId = groupId;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return groupName;
	}
	
//	public String toXML()
//	{
//		StringBuffer xml = new StringBuffer();
//		xml.append("<"+PATTERN_GROUP_TAG+" name='"+groupName+"'>\n");
//		for (int i=0; i<patternList.size(); i++)
//		{
//			Pattern p = patternList.get(i);
//			xml.append(p.toXML());
//		}
//		xml.append("</"+PATTERN_GROUP_TAG+">\n");
//		return xml.toString();
//	}
	/**
	 * @return Returns the description.
	 */
	public String getDescription()
	{
		return description;
	}
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o)
	{
		if (o==null || !(o instanceof PatternGroup))
			return false;
		
		PatternGroup compare = (PatternGroup) o;
		return (compare.getGroupId()==groupId);
	}
	
	public boolean containsPattern(Pattern pattern)
	{
		for (int i=0; i<patternList.size(); i++)
		{
			Pattern p = patternList.get(i);
			if (pattern.isSamePattern(p))
				return true;
		}
		return false;
	}
}
