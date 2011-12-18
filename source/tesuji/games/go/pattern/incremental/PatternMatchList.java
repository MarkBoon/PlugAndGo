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
 *	<font color="#00000"><font size=+1>
 */
package tesuji.games.go.pattern.incremental;

import tesuji.core.util.ArrayList;

/**
 * Simple ArrayList subclass used for holding PatternMatch objects.
 * The only 'added value' is the fact that the clear method
 * will recycle all it's objects.
 */
public class PatternMatchList
	extends ArrayList<PatternMatch>
{
	private static final long serialVersionUID = 5741246160017075484L;

	public PatternMatchList()
	{
		
	}
	
	public PatternMatchList(int capacity)
	{
		super(capacity);
	}

	/**
	 * Add a PatternMatch and store the index in the list in the
	 * PatternMatch object.
	 * 
	 * @param match
	 */
	public final void addMatch(PatternMatch match)
	{
		match.matchListIndex = size();
		add(match);
	}

	/**
	 * Remove a PatternMatch. Use the index stored in the PatternMatch
	 * object for quick removal.
	 * 
	 * @param match
	 */
	public void removeMatch(PatternMatch match)
	{
		int index = match.matchListIndex;
		assert(get(index).leaf==match.leaf);
		remove(index);
		get(index).matchListIndex = index;
	}

	public void reset()
	{
		super.clear();
	}
	
	/**
	 * Clear the list and make sure all the objects contained get recycled.
	 * 
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear()
	{
		int size =size();
		for (int i=0; i<size; i++)
		{
			PatternMatch match = get(i);
			match.recycle();
		}
		super.clear();
	}
	
	public void sort(byte colorToPlay)
	{
		int size =size();
		if (size<2)
			return;
		
		for (int i=0; i<size; i++)
		{
			for (int j=i+1; j<size; j++)
			{
				double urgency1 = get(i).getUrgencyValue(colorToPlay);
				double urgency2 = get(j).getUrgencyValue(colorToPlay);
				if (urgency2<urgency1)
				{
					PatternMatch temp = get(i);
					temp.matchListIndex = j;
					set(i,get(j));
					set(j,temp);
					get(i).matchListIndex = i;
				}
			}
		}
	}
	
	public void copyDataFrom(PatternMatchList source)
	{
		clear();
		
		for (int i=0; i<source.size(); i++)
			addMatch(source.get(i).createClone());
	}
	
	public PatternMatchList createIndexedClone()
	{
		PatternMatchList clone = new PatternMatchList(_capacity);
		
		for (int i=0; i<size(); i++)
			clone.addMatch(get(i).createClone());
		return clone;
	}
	
	@Override
	public PatternMatchList createClone()
	{
		PatternMatchList clone = new PatternMatchList(_capacity);
		
		for (int i=0; i<size(); i++)
			clone.add(get(i).createClone());
		return clone;
	}
}
