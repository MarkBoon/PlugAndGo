/*
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
 */
package tesuji.games.go.pattern.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Date;
import java.util.StringTokenizer;

import tesuji.core.util.ArrayList;
import tesuji.core.util.List;

import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.monte_carlo.MonteCarloGoAdministration;
import tesuji.games.go.pattern.util.PatternUtil;
import tesuji.games.go.util.GoArray;
import tesuji.games.model.BoardModel;
import tesuji.games.util.Point;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;

/**
 	Pattern class based on the C++ implementation by Charlie Carroll. <br><br>

 	This is a very straightforward data-structure that stores the
 	stone-formation plus some extra data. Since patterns are mostly
 	used to suggest moves, the extra data typically consists of
 	a suggested move for black, a suggested move for white, the type
 	of purpose of those moves etc... The bulk of this class is simple
 	getter and setters.<br><br>

 	The stone formation is stored as some bit-patterns. This limits
 	the maximum size of the patterns to a rectangle of 256 points.
 	(It doesn't have to be square.) But this implementation is
 	totally encapsulated by the getPoint and setPoint methods, so it
 	could later be replaced, say by 2-dimensional arrays.<br><br>
 */
public class Pattern
	implements java.io.Serializable
{
	private static final long serialVersionUID = 651814371296255812L;

	// XML tag definitions
	public static final String PATTERN_TAG =			"GoPattern";
	public static final String PATTERN_INFO_TAG =		"pattern-info";
	public static final String PATTERN_DATA_TAG =		"pattern-data";
	public static final String ROW_TAG =				"row";

	// Property-name definitions
	public static final String WIDTH_PROPERTY =			"width";
	public static final String HEIGHT_PROPERTY =		"height";
	public static final String PATTERN_NR_PROPERTY =	"patternNr";
	public static final String PATTERN_DATA_PROPERTY =	"patternData";
	public static final String TYPE_PROPERTY =			"type";
	public static final String STARTPOINT_PROPERTY =	"start";
	public static final String BLACKMOVE_PROPERTY =		"blackMove";
	public static final String WHITEMOVE_PROPERTY =		"blackMove";
	public static final String USERPOINT_PROPERTY =		"userPoint";
	public static final String TOPEDGE_PROPERTY =		"topEdge";
	public static final String LEFTEDGE_PROPERTY =		"leftEdge";
	public static final String TEXT_PROPERTY =			"text";
	public static final String CONDITIONS_PROPERTY =	"conditions";
	
	private static final int NR_BITS_PER_WORD = 64;	// Nr of bits in the basic data-type.
	private static final int NR_WORDS = 4;			// Nr of words used to store the pattern.
	private static final int MAX_NR_BITS = 256;		// Maximum nr of bits in the pattern. (4x64) Largest pattern is 16x16.

	private int width = 3;							// Width of the pattern. Default=3.
	private int height = 3;							// Height of the pattern. Default=3.
	private long[] blackBits = new long[NR_WORDS];	// Bit-pattern of the black stones.
	private long[] whiteBits = new long[NR_WORDS];	// Bit-pattern of the white stones.
	private long[] emptyBits = new long[NR_WORDS];	// Bit-pattern of the empty points.
	private int patternNr;							// Pattern number (index in array).
	private int type;								// Type indication of the pattern. (Killing pattern, escaping pattern, etc...)
	private double urgencyValueBlack;				// Value of the pattern. Usually indicating a level of urgency or importance.
	private double urgencyValueWhite;				// Value of the pattern. Usually indicating a level of urgency or importance.
	private int blackNrSuccesses;
	private int blackNrOccurrences;
	private int whiteNrSuccesses;
	private int whiteNrOccurrences;
	private transient int totalBlackLength;			// Total nr of moves this pattern has been on the board (of all games ever played.)
	private transient int totalWhiteLength;			// Total nr of moves this pattern has been on the board (of all games ever played.)
	private int startX = UNDEFINED_COORDINATE;		// X-coordinate of the reference point.
	private int startY = UNDEFINED_COORDINATE;		// Y-coordinate of the reference point.
	private int blackX = UNDEFINED_COORDINATE;		// X-coordinate of the suggested move for black in this pattern.
	private int blackY = UNDEFINED_COORDINATE;		// Y-coordinate of the suggested move for black in this pattern.
	private int whiteX = UNDEFINED_COORDINATE;		// X-coordinate of the suggested move for white in this pattern.
	private int whiteY = UNDEFINED_COORDINATE;		// Y-coordinate of the suggested move for white in this pattern.
	private int userX = UNDEFINED_COORDINATE;		// X-coordinate of a user-defined reference point.
	private int userY = UNDEFINED_COORDINATE;		// Y-coordinate of a user-defined reference point.
	private boolean topEdge;						// Whether the top edge is also the edge of the board.
	private boolean leftEdge;						// Whether the left edge is also the edge of the board.
	private transient boolean bottomEdge;			// Whether the bottom edge is also the edge of the board.
	private transient boolean rightEdge;			// Whether the right edge is also the edge of the board.
	private int pointCount;							// Nr of defined points (black, white or empty) in the pattern.
//	private int bestOrientation;					// Orientation that results in the most efficient way to put pattern into a pattern-tree
	private int lastSignificantPoint;				// Number (index) of last non-NoCare point.
	private String text;							// Arbitrary text attached to this pattern.
	
	private int uniqueId;							// Unique id for database purposes
	private int groupId;							// Unique id of the group this pattern belongs to.
	
	private String conditions;						// String from which the conditions will be parsed.
	
//	transient private PatternTreeLeaf[] treeLeafs;	// Array of back-pointers into the spiral tree for each of the eight possible rotations.
	private List<PatternCondition> conditionList;	// List with extra conditions that need to match.

	private boolean generated;						// Whether the pattern was generated or entered mannually.
	private Date createdDate;						// When the pattern was generated or entered mannually.
	
	transient private boolean dirty;				// Mark whether a pattern needs updating in the DB
	transient private boolean added;				// Mark whether a pattern was actually added to the tree.
	transient private boolean removed;				// Mark whether a pattern was actually removed from the tree.

	transient private PropertyChangeSupport changeSupport;

	/**
	 * Pattern constructor comment.
	 */
	public Pattern()
	{
//		treeLeafs = new PatternTreeLeaf[8];
		conditionList = new ArrayList<PatternCondition>();
		uniqueId = -1;
		text = "";
		conditions = "";
		urgencyValueBlack = 1000000;
		urgencyValueWhite = 1000000;
		createdDate = new Date(System.currentTimeMillis());
	}
	
	/**
	 * Get the point at the x,y coordinate in the pattern.
	 * The implementation how the points are stored should remain hidden.
	 * 
	 * @param x coordinate relative to the top-left
	 * @param y coordinate relative to the top-left
	 * 
	 * @return value of the point which is either BLACK, WHITE, EMPTY or NOCARE
	 */
	public byte getPoint(int x, int y)
	{
		if (x<-1 || x>=width)
			return NOCARE;
//			throw new ArrayIndexOutOfBoundsException("Illegal value for x = "+x);
		if (y<-1 || y>=height)
			return NOCARE;
//			throw new ArrayIndexOutOfBoundsException("Illegal value for y = "+y);
		
		// The following code returns a value EDGE but only for one point for each edge
		// which is the one aligning the center of the pattern. This basically adds a
		// point to the pattern for each edge it has defined.
		if (x==-1)
		{
			if (y==height/2 && leftEdge)
				return EDGE;
			return NOCARE;
		}
		if (y==-1)
		{
			if (x==width/2 && topEdge)
				return EDGE;
			return NOCARE;
		}
		
		int bitNr = x+y*width;
		int bitNrInWord = bitNr&(NR_BITS_PER_WORD-1);
		int wordNr = bitNr/NR_BITS_PER_WORD;
		long mask = 1L<<bitNrInWord;

		if ((emptyBits[wordNr]&mask)!=0)
			return EMPTY;
		else if ((blackBits[wordNr]&mask)!=0)
			return BLACK;
		else if ((whiteBits[wordNr]&mask)!=0)
			return WHITE;
		else
			return NOCARE;
	}
	
	/**
	 * Set the point at the x,y coordinate in the pattern.
	 * The implementation how the points are stored should remain hidden.
	 * 
	 * @param x  coordinate relative to the top-left
	 * @param y coordinate relative to the top-left
	 * @param value of the point which is either BLACK, WHITE, EMPTY or NOCARE
	 */
	public void setPoint(int x, int y, byte value)
	{
		if (x<0 || x>=width)
			throw new ArrayIndexOutOfBoundsException("Illegal value for x = "+x);
		if (y<0 || y>=height)
			throw new ArrayIndexOutOfBoundsException("Illegal value for y = "+y);

		int bitNr = x+y*width;
		int bitNrInWord = bitNr&(NR_BITS_PER_WORD-1);
		int wordNr = bitNr/NR_BITS_PER_WORD;
		long mask = 1L<<bitNrInWord;

		byte oldValue = getPoint(x,y);
		int oldPoint = (oldValue==NOCARE ? 0 : 1);

		blackBits[wordNr] &= (~mask);
		whiteBits[wordNr] &= (~mask);
		emptyBits[wordNr] &= (~mask);

		switch(value)
		{
			case BLACK: blackBits[wordNr] |= mask; break;
			case WHITE: whiteBits[wordNr] |= mask; break;
			case EMPTY: emptyBits[wordNr] |= mask; break;
		}

		int newPoint = 0;
		if (value!=NOCARE)
			newPoint = 1;

		pointCount += (newPoint-oldPoint);

		// Maybe if we need more specific information about the change
		// we could send a BoardChange object instead. For now this will do.
		firePropertyChange(PATTERN_DATA_PROPERTY,new Byte(oldValue),new Byte(value));
	}
	
	/**
	 * When removing rows or columns the residue in the unused part must be cleaned up
	 * in order for the rank-ordering to remain correct. Could probably be implemented faster
	 * than below, but it's not used in speed critical parts.
	 */
	private void clearUnusedPart()
	{
		int lastBitNr = height*width;
		for (int bit = lastBitNr; bit<MAX_NR_BITS; bit++)
		{
			int bitNrInWord = bit&(NR_BITS_PER_WORD-1);
			int wordNr = bit/NR_BITS_PER_WORD;
			long mask = 1L<<bitNrInWord;

			blackBits[wordNr] &= (~mask);
			whiteBits[wordNr] &= (~mask);
			emptyBits[wordNr] &= (~mask);			
		}
	}
	
	//
	// The methods below adjust the size of the pattern
	// by adding or removing rows and columns.
	//
	
	/**
	 	Make the pattern bigger by adding another row at the bottom.
	
	 	@return boolean value indicating if the operation succeeded
	*/
	public boolean addBottomRow()
	{
		if ((height+1)*width>MAX_NR_BITS)			// Bigger pattern won't fit
			return false;

		// Take care of the loop ordering to avoid overwriting.
		for (int y=height-1; y>=0; y--)
		{
			for (int x=width-1; x>=0; x--)
			{
				byte value = getPoint(x,y);			// Get the value from the old place.
				height++;
				setPoint(x,y,value);				// And set it in the new place.
				height--;
			}
		}

		height++;

		for (int x=width-1; x>=0; x--)
			setPoint(x,height-1,NOCARE);

		firePropertyChange(HEIGHT_PROPERTY,new Integer(height-1), new Integer(height));
	
		return true;
	}
	
	/**
	 	Make the pattern bigger by adding another row at the top.
	
	 	@return boolean value indicating if the operation succeeded
	*/
	public boolean addTopRow()
	{
		if ((height+1)*width>MAX_NR_BITS)			// Bigger pattern won't fit
			return false;
	
		// Take care of the loop ordering to avoid overwriting.
		for (int y=height-1; y>=0; y--)
		{
			for (int x=width-1; x>=0; x--)
			{
				byte value = getPoint(x,y);			// Get the value from the old place.
				height++;
				setPoint(x,y+1,value);				// And set it in the new place.
				height--;
			}
		}
	
		height++;
	
		for (int x=width-1; x>=0; x--)
			setPoint(x,0,NOCARE);
	
		if (blackY!=UNDEFINED_COORDINATE)
			blackY++;
		if (whiteY!=UNDEFINED_COORDINATE)
			whiteY++;
		if (userY!=UNDEFINED_COORDINATE)
			userY++;
		if (startY!=UNDEFINED_COORDINATE)
			startY++;
	
		// Adjust the coordinates of the conditions as well.
//		for (PatternCondition c : conditionList)
//			c.setY(c.getY()+1);
	
		firePropertyChange(HEIGHT_PROPERTY,new Integer(height-1), new Integer(height));
	
		return true;
	}
	/**
	 	Make the pattern bigger by adding another column at the left.
	
	 	@return boolean value indicating if the operation succeeded
	*/
	public boolean addLeftColumn()
	{
		if (height*(width+1)>MAX_NR_BITS)			// Bigger pattern won't fit
			return false;

		// Take care of the loop ordering to avoid overwriting.
		for (int y=height-1; y>=0; y--)
		{
			for (int x=width-1; x>=0; x--)
			{
				byte value = getPoint(x,y);			// Get the value from the old place.
				width++;
				setPoint(x+1,y,value);				// And set it in the new place.
				width--;
			}
		}

		width++;

		for (int y=height-1; y>=0; y--)
			setPoint(0,y,NOCARE);

		if (blackX!=UNDEFINED_COORDINATE)
			blackX++;
		if (whiteX!=UNDEFINED_COORDINATE)
			whiteX++;
		if (userX!=UNDEFINED_COORDINATE)
			userX++;
		if (startX!=UNDEFINED_COORDINATE)
			startX++;

		// Adjust the coordinates of the conditions as well.
//		for (PatternCondition c : conditionList)
//			c.setX(c.getX()+1);
	

		firePropertyChange(WIDTH_PROPERTY,new Integer(width-1), new Integer(width));

		return true;
	}

	/**
	 	Make the pattern bigger by adding another column at the right.
	
	 	@return boolean value indicating if the operation succeeded
	*/
	public boolean addRightColumn()
	{
		if (height*(width+1)>MAX_NR_BITS)			// Bigger pattern won't fit
			return false;

		// Take care of the loop ordering to avoid overwriting.
		for (int y=height-1; y>=0; y--)
		{
			for (int x=width-1; x>=0; x--)
			{
				byte value = getPoint(x,y);			// Get the value from the old place.
				width++;
				setPoint(x,y,value);				// And set it in the new place.
				width--;
			}
		}

		width++;

		for (int y=height-1; y>=0; y--)
			setPoint(width-1,y,NOCARE);

		firePropertyChange(WIDTH_PROPERTY,new Integer(width-1), new Integer(width));

		return true;
	}
	
	/**
	 	Make the pattern smaller by removing the bottom row.
	 	Only succeeds if no data would get erased with it.
	
	 	@return boolean value indicating if the operation succeeded
	*/
	public boolean removeBottomRow()
	{
		if (!hasEmptyBottomRow())
			return false;
	
		if (blackY==height-1 || whiteY==height-1 || userY==height-1)
			return false;
	
		// Take care of the loop ordering to avoid overwriting.
		for (int y=height-2; y>=0; y--)
		{
			for (int x=0; x<width; x++)
			{
				byte value = getPoint(x,y);
				height--;
				setPoint(x,y,value);
				height++;
			}
		}
	
		height--;
		
		clearUnusedPart();
	
		firePropertyChange(HEIGHT_PROPERTY,new Integer(height+1), new Integer(height));
	
		return true;
	}

	/**
     * 
     */
    public boolean hasEmptyBottomRow()
    {
	    for (int x=0; x<width; x++)
			if (getPoint(x,height-1)!=NOCARE)
				return false;
	    return true;
    }

	/**
	 	Make the pattern smaller by removing the right column.
	 	Only succeeds if no data would get erased with it.
	
	 	@return boolean value indicating if the operation succeeded
	*/
	public boolean removeRightColumn()
	{
		if (!hasEmptyRightColumn())
			return false;
	
		if (blackX==width-1 || whiteX==width-1 || userX==width-1)
			return false;
	
		// Take care of the loop ordering to avoid overwriting.
		for (int y=0; y<height; y++)
		{
			for (int x=0; x<width-1; x++)
			{
				byte value = getPoint(x,y);
				width--;
				setPoint(x,y,value);
				width++;
			}
		}
	
		width--;
	
		clearUnusedPart();

		firePropertyChange(WIDTH_PROPERTY,new Integer(width+1), new Integer(width));
	
		return true;
	}
	
	/**
     * 
     */
    public boolean hasEmptyRightColumn()
    {
		for (int y=0; y<height; y++)
			if (getPoint(width-1,y)!=NOCARE)
				return false;
	    return true;
    }

	/**
	 	Make the pattern smaller by removing the top row.
	 	Only succeeds if no data would get erased with it.
	
	 	@return boolean value indicating if the operation succeeded
	*/
	public boolean removeTopRow()
	{
		if (!hasEmptyTopRow())
			return false;
	
		if (blackY==0 || whiteY==0 || userY==0)
			return false;
	
		// Take care of the loop ordering to avoid overwriting.
		for (int y=1; y<height; y++)
		{
			for (int x=0; x<width; x++)
			{
				byte value = getPoint(x,y);
				height--;
				setPoint(x,y-1,value);
				height++;
			}
		}

		height--;
	
		clearUnusedPart();

		if (blackY!=UNDEFINED_COORDINATE)
			blackY--;
		if (whiteY!=UNDEFINED_COORDINATE)
			whiteY--;
		if (userY!=UNDEFINED_COORDINATE)
			userY--;
		if (startY!=UNDEFINED_COORDINATE)
			startY--;
	
		// Adjust the coordinates of the conditions as well.
//		for (PatternCondition c : conditionList)
//			c.setY(c.getY()-1);
	
		firePropertyChange(HEIGHT_PROPERTY,new Integer(height+1), new Integer(height));
	
		return true;
	}

	/**
     * 
     */
    public boolean hasEmptyTopRow()
    {
	    for (int x=0; x<width; x++)
			if (getPoint(x,0)!=NOCARE)
				return false;
	    return true;
    }

	/**
	 	Make the pattern smaller by removing the left column.
	 	Only succeeds if no data would get erased with it.
	
	 	@return boolean value indicating if the operation succeeded
	*/
	public boolean removeLeftColumn()
	{
		if (!hasEmptyLeftColumn())
			return false;
	
		if (blackX==0 || whiteX==0 || userX==0)
			return false;
	
		// Take care of the loop ordering to avoid overwriting.
		for (int y=0; y<height; y++)
		{
			for (int x=1; x<width; x++)
			{
				byte value = getPoint(x,y);
				width--;
				setPoint(x-1,y,value);
				width++;
			}
		}

		width--;
	
		clearUnusedPart();

		if (blackX!=UNDEFINED_COORDINATE)
			blackX--;
		if (whiteX!=UNDEFINED_COORDINATE)
			whiteX--;
		if (userX!=UNDEFINED_COORDINATE)
			userX--;
		if (startX!=UNDEFINED_COORDINATE)
			startX--;
	
		// Adjust the coordinates of the conditions as well.
//		for (PatternCondition c : conditionList)
//			c.setX(c.getX()-1);
	
		firePropertyChange(WIDTH_PROPERTY,new Integer(width+1), new Integer(width));
	
		return true;
	}

	/**
     * 
     */
    public boolean hasEmptyLeftColumn()
    {
		for (int y=0; y<height; y++)
			if (getPoint(0,y)!=NOCARE)
				return false;
	    return true;
    }
    
	/**
	 	By registering a listener it gets notified of all the
	 	important changes to a pattern. Handy for editors.
	*/
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		if (changeSupport==null)
			changeSupport = new PropertyChangeSupport(this);
		changeSupport.addPropertyChangeListener(listener);
	}
	
	/**
	 * @see java.lang.Object#clone()
	 * 
	 * Note: still thinking about whether the uniqueID should be cloned.
	 */
	@Override
	public Object clone()
	{
		Pattern newPattern = new Pattern();
		newPattern.copy(this);
		return newPattern;
	}
	
	public boolean isEmpty()
	{
		return (blackBits[0]==0 && blackBits[1]==0 && blackBits[2]==0 && blackBits[3]==0 && whiteBits[0]==0 && whiteBits[1]==0 && whiteBits[2]==0 && whiteBits[3]==0);
	}
	
	/**
	 * @param o object that is the source from which the copy is made
	 * 
	 * Note: the uniqueID is not copied.
	 */
	public void copy(Object o)
	{
		Pattern source = (Pattern) o;
		width = source.width;
		height = source.height;
		for (int i=0; i<NR_WORDS; i++)
		{
			blackBits[i] = source.blackBits[i];
			whiteBits[i] = source.whiteBits[i];
			emptyBits[i] = source.emptyBits[i];
		}
		patternNr = source.patternNr;
		type = source.type;

		startX = source.startX;
		startY = source.startY;
		blackX = source.blackX;
		blackY = source.blackY;
		whiteX = source.whiteX;
		whiteY = source.whiteY;
		userX = source.userX;
		userY = source.userY;
		urgencyValueBlack = source.urgencyValueBlack;
		urgencyValueWhite = source.urgencyValueWhite;
		blackNrSuccesses = source.blackNrSuccesses;
		whiteNrSuccesses = source.whiteNrSuccesses;
		blackNrOccurrences = source.blackNrOccurrences;
		whiteNrOccurrences = source.whiteNrOccurrences;
		
		topEdge = source.topEdge;
		leftEdge = source.leftEdge;
		pointCount = source.pointCount;
//		bestOrientation = source.bestOrientation;
		lastSignificantPoint = source.lastSignificantPoint;
		text = source.text;
		setConditions(source.getConditions());
		groupId = source.groupId;	
	}
	
	public Pattern createMutation(BoardModel boardModel, int offsetXY, int orientation, boolean inverted, boolean wasPlayed, byte colorToPlay)
	{
		Pattern newPattern = (Pattern) clone();
		if (colorToPlay==BLACK)
		{
			newPattern.blackNrSuccesses = wasPlayed? 1 : 0;
			newPattern.blackNrOccurrences = 1;
		}
		else
		{
			newPattern.whiteNrSuccesses = wasPlayed? 1 : 0;
			newPattern.whiteNrOccurrences = 1;
		}
		
		// Add padding around to allow expansion of the pattern.
		if (!newPattern.hasEmptyTopRow() && !newPattern.hasTopEdge())
			newPattern.addTopRow();
		if (!newPattern.hasEmptyBottomRow() && !newPattern.hasBottomEdge())
			newPattern.addBottomRow();
		if (!newPattern.hasEmptyLeftColumn() && !newPattern.hasLeftEdge())
			newPattern.addLeftColumn();
		if (!newPattern.hasEmptyRightColumn() && !newPattern.hasRightEdge())
			newPattern.addRightColumn();
		
		boolean found = false;
		int nrTries = 0;
		while (!found && nrTries<1000)
		{
			int x = (int)(Math.random()*newPattern.getWidth());
			int y = (int)(Math.random()*newPattern.getHeight());
			if (newPattern.getPoint(x, y)!=NOCARE)
			{
				/*int replace = (int)(Math.random()*4);
				if (replace==0 && (x!=startX || y!=startY))
				{
					int choice = (int)(Math.random()*4);
					byte newValue;
					switch(choice)
					{
						case 0: newValue = EMPTY; break;
						case 1: newValue = BLACK; break;
						case 2: newValue = WHITE; break;
						default: newValue = NOCARE; break;
					}
					if (newValue!=NOCARE || !newPattern.isCentered(x,y))
					{
						newPattern.setPoint(x, y, newValue);
						found = true;
					}
				}
				else*/
				{
					int dir = (int)(Math.random()*4);
					switch(dir)
					{
					case 0:
						if (x>0)
							x--;
						break;
					case 1:
						if (x<newPattern.width-1)
							x++;
						break;
					case 2:
						if (y>0)
							y--;
						break;
					case 3:
						if (y<newPattern.height-1)
							y++;
						break;
					}
					if (newPattern.getPoint(x, y)==NOCARE)
					{
						Point p = new Point();
						int relativeX = x - newPattern.startX;
						int relativeY = y - newPattern.startY;
						PatternUtil.adjustOrientation(relativeX, relativeY, orientation, p);
						int newXY = offsetXY+GoArray.toXY(p.x,p.y);
						byte boardValue = boardModel.get(newXY);
						if (inverted && (boardValue==BLACK || boardValue==WHITE))
							boardValue = opposite(boardValue);
						
						if (boardValue==EDGE)
						{
							if (x<0)
								newPattern.setLeftEdge(true);
							if (y<0)
								newPattern.setTopEdge(true);
							if (x==width)
								newPattern.setRightEdge(true);
							if (y==height)
								newPattern.setBottomEdge(true);
							return newPattern;
						}
						
						newPattern.setPoint(x, y, boardValue);
						found = !newPattern.isEmpty();
						nrTries++;
					}
				}
			}
		}
		
		// Remove the padding padding.
		if (newPattern.hasEmptyTopRow())
			newPattern.removeTopRow();
		if (newPattern.hasEmptyBottomRow())
			newPattern.removeBottomRow();
		if (newPattern.hasEmptyLeftColumn())
			newPattern.removeLeftColumn();
		if (newPattern.hasEmptyRightColumn())
			newPattern.removeRightColumn();

		return newPattern;
	}
	
	public static Pattern createFromBoard(int offsetXY, MonteCarloGoAdministration mcAdministration)
	{
		int limit = 256;
		int boardSize = mcAdministration.getBoardModel().getBoardSize();
		int[] beforeOwnership = GoArray.createIntegers();
		int[] afterOwnership = GoArray.createIntegers();
		int[] diffOwnership = GoArray.createIntegers();
		MonteCarloGoAdministration tmpAdministration = (MonteCarloGoAdministration)mcAdministration.createClone();
		MonteCarloGoAdministration startAdministration = (MonteCarloGoAdministration)mcAdministration.createClone();
		MonteCarloGoAdministration stepAdministration = (MonteCarloGoAdministration)mcAdministration.createClone();
		for (int i=0; i<limit; i++)
		{
			tmpAdministration.copyDataFrom(startAdministration);
			tmpAdministration.playout();
			for (int j=GoArray.FIRST; j<GoArray.LAST; j++)
			{
				beforeOwnership[j] += tmpAdministration.getBlackOwnership()[j];
			}
			GoArray.printNumbers(beforeOwnership);
			System.out.println("---\n");
		}
		stepAdministration.playMove(GoMoveFactory.getSingleton().createMove(offsetXY, startAdministration.getColorToMove()));
		for (int i=0; i<limit; i++)
		{
			tmpAdministration.copyDataFrom(stepAdministration);
			tmpAdministration.playout();
			for (int j=GoArray.FIRST; j<GoArray.LAST; j++)
			{
				afterOwnership[j] += tmpAdministration.getBlackOwnership()[j];
			}
			GoArray.printNumbers(afterOwnership);
			System.out.println("---\n");
		}
		for (int j=GoArray.FIRST; j<GoArray.LAST; j++)
		{
			beforeOwnership[j] -= limit/2;
			afterOwnership[j] -= limit/2;
			beforeOwnership[j] /= 10;
			afterOwnership[j] /= 10;
			diffOwnership[j] = afterOwnership[j]-beforeOwnership[j];
		}
		Pattern newPattern = new Pattern();
		newPattern.startX = GoArray.getX(offsetXY);
		newPattern.startY = GoArray.getY(offsetXY);
		newPattern.width = boardSize;
		newPattern.height = boardSize;
		if (tmpAdministration.getColorToMove()==BLACK)
		{
			newPattern.blackNrOccurrences = 1;
			newPattern.blackNrSuccesses = 1;
		}
		else
		{
			newPattern.whiteNrOccurrences = 1;
			newPattern.whiteNrSuccesses = 1;
		}
		GoArray.printBoard(startAdministration.getBoardModel());
		System.out.println("---\n");
		GoArray.printNumbers(beforeOwnership);
		System.out.println("---\n");
		GoArray.printBoard(stepAdministration.getBoardModel());
		System.out.println("---\n");
		GoArray.printNumbers(afterOwnership);
		System.out.println("---\n");
		GoArray.printNumbers(diffOwnership);
		/*
		PointSpiral spiral = new PointSpiral();
		int[] pointOrder = spiral.getPointOrder();
		for (int i=0; i<pointOrder.length; i++)
		{
			int relativeX = GoArray.getX(offsetXY) + GoArray.getX(pointOrder[i]);
			int relativeY = GoArray.getY(offsetXY) + GoArray.getY(pointOrder[i]);
			int pointXY = offsetXY + pointOrder[i];
			if (relativeX>0 && relativeY>0 && relativeX<=boardSize && relativeY<=boardSize)
			{
				int pointX = GoArray.getX(pointXY);
				int pointY = GoArray.getY(pointXY);
//				if (blackOwnership[pointXY]>threshold || blackOwnership[pointXY]<-threshold || whiteOwnership[pointXY]>threshold || whiteOwnership[pointXY]<-threshold)
//				{
//					newPattern.setPoint(pointX, pointY, mcAdministration.getBoardModel().get(pointXY));
//				}
			}
		}
		while (newPattern.removeTopRow());
		while (newPattern.removeBottomRow());
		while (newPattern.removeRightColumn());
		while (newPattern.removeLeftColumn());
		*/
		return newPattern;
	}
	
	private void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		if (changeSupport!=null)
			changeSupport.firePropertyChange(propertyName,oldValue,newValue);
	}
	
	/**
	 	Get the best orientation in which this pattern can be
	 	stored in the pattern-tree. Since this field is here
	 	only for convenience of tree-building it's access is
	 	package scope.
	*/
//	int getBestOrientation()
//	{
//		return bestOrientation;
//	}
	public int getBlackX()
	{
		return blackX;
	}
	public int getBlackY()
	{
		return blackY;
	}
	/**
	 	The implementation of conditions is not finished yet.
	
	 	<br><br>Creation date: (16-May-01 1:59:21 PM)<br><br>
	*/
	public List<PatternCondition> getConditionList()
	{
		return conditionList;
	}
	
	public String getConditions()
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (PatternCondition condition : conditionList)
		{
			stringBuilder.append('\t');
			stringBuilder.append(condition.toString());
		}
			
		return stringBuilder.toString();
	}
	
	public int getHeight()
	{
		return height;
	}
	
	/**
	 * Look at the pattern row by row.  The first row in which we find
	 * the find non-NoCare point is the minimum horizontal edge.
	 */
	public int getMaxHorizontalEdge()
	{		
		for (int j=height-1; j>=0; j--)
			for (int i=0; i<width; i++)
				if (getPoint(i,j) != NOCARE)
					return j;

		// Pattern consists only of NOCARE points.
		return -1;
	}
	
	/**
	 * Look at the pattern column by column.  The last column in which we find
	 * the find non-NoCare point is the maximum vertical edge.
	 * 
	 * @return int
	 */
	public int getMaxVerticalEdge()
	{
		for (int i=width-1; i>=0; i--)
			for (int j=0; j<height; j++)
				if (getPoint(i,j) != NOCARE)
					return i;

		// Pattern consists only of NOCARE points.
		return -1;
	}

	/**
	 * Look at the pattern row by row.  The first row in which we find
	 * the first non-NoCare point is the minimum horizontal edge.
	 * 
	 * @return int
	 */
	public int getMinHorizontalEdge()
	{
		for (int j=0; j<height; j++)
			for (int i=0; i<width; i++)
				if (getPoint(i,j) != NOCARE)
					return j;

		// Pattern consists only of NOCARE points.
		return height;
	}
	
	/**
	 * Look at the pattern column by column.  The first column in which we find
	 * the first non-NoCare point is the minimum vertical edge.
	 * 
	 * @return int
	 */
	public int getMinVerticalEdge()
	{
		for (int i=0; i<width; i++)
			for (int j=0; j<height; j++)
				if (getPoint(i,j) != NOCARE)
					return i;

		// Pattern consists only of NOCARE points.
		return width;
	}
	
	public int getPatternNr()
	{
		return patternNr;
	}
	
	public int getPointCount()
	{
		// return pointCount; - there are some bugs in keeping the point-count when resizing the pattern and when reading from the DB.

		int points = 0;

		for (int i=0; i<width; i++)
			for (int j=0; j<height; j++)
				if (getPoint(i,j)!=NOCARE)
					points++;

		return points;
	}
	
	public int getBlackCount()
	{
		int count = 0;

		for (int i=0; i<width; i++)
			for (int j=0; j<height; j++)
				if (getPoint(i,j)==BLACK)
					count++;

		return count;
	}
	
	public int getWhiteCount()
	{
		int count = 0;

		for (int i=0; i<width; i++)
			for (int j=0; j<height; j++)
				if (getPoint(i,j)==WHITE)
					count++;

		return count;
	}
	/**
	 	Get a point in the pattern relative to its starting point.
	
	 	@return byte
	*/
	public final byte getRelativePoint(int x, int y)
	{
		return getPoint(x+startX,y+startY);
	}
	
	public final void setRelativePoint(int x, int y, byte value)
	{
		setPoint(x+startX,y+startY, value);
	}
	
	public int getStartX()
	{
		return startX;
	}
	public int getStartY()
	{
		return startY;
	}
	
	public String getText()
	{
		return text;
	}
	
	/**
	 	Since this field is here only for convenience of tree-building
	 	it's access is package scope.
	*/
//	PatternTreeLeaf getTreeLeaf(int index)
//	{
//		return treeLeafs[index&7];
//	}
	
	/**
	 	Since this field is here only for convenience of tree-building
	 	it's access is package scope.
	*/
//	PatternTreeLeaf[] getTreeLeafs()
//	{
//		return treeLeafs;
//	}
	
	public int getUserX()
	{
		return userX;
	}
	public int getUserY()
	{
		return userY;
	}
	public int getWhiteX()
	{
		return whiteX;
	}
	public int getWhiteY()
	{
		return whiteY;
	}
	public int getWidth()
	{
		return width;
	}
	/**
	 	@return boolean whether the pattern contains a black stone or not.
	*/
	public boolean hasBlackPoints()
	{
		for (int i = NR_WORDS-1; i>=0; i--)
			if (blackBits[i]!=0) return true;
		return false;
	}
	/**
	 	@return boolean whether the pattern contains an empty point or not.
	 	(It's actually hard to imagine a pattern without any empty points.)
	*/
	public boolean hasEmptyPoints()
	{
		for (int i = NR_WORDS-1; i>=0; i--)
			if (emptyBits[i]!=0) return true;
		return false;
	}
	/**
	 	@return boolean whether the pattern contains a white stone or not.
	*/
	public boolean hasWhitePoints()
	{
		for (int i = NR_WORDS-1; i>=0; i--)
			if (whiteBits[i]!=0) return true;
		return false;
	}
	/**
	 	This prepares the pattern to be erased from a pattern-tree.
	 	It is done by eraseing the back-pointers to the leafs.
	*/
//	void removeFromTree()
//	{
//		for (int i=0; i<treeLeafs.length; i++)
//			treeLeafs[i] = null;
//	}
	
	/**
	 * Remove a PropertyChangeListener from the pattern.
	 * 
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		if (changeSupport==null)
			changeSupport = new PropertyChangeSupport(this);
		changeSupport.removePropertyChangeListener(listener);
	}
	
	/**
	 	Set the best orientation in which this pattern can be
	 	stored in the pattern-tree. Since this field is here
	 	only for convenience of tree-building it's access is
	 	package scope.
	*/
//	void setBestOrientation(int orientation)
//	{
//		bestOrientation = orientation;
//	}
	public void setBlackX(int x)
	{
		blackX = x;

		firePropertyChange(BLACKMOVE_PROPERTY,null,new Point(blackX,blackX));
	}
	public void setBlackY(int y)
	{
		blackY = y;

		firePropertyChange(BLACKMOVE_PROPERTY,null,new Point(blackX,blackX));
	}
	
	public void setConditions(String conditionText)
	{
		conditions = conditionText.trim();
		conditionList.clear();
		if (conditions!=null && conditions.length()!=0)
		{
			StringReader stringReader = new StringReader(conditionText);
			BufferedReader reader = new BufferedReader(stringReader);
			try
	        {
		        while (reader.ready())
		        {
		        	String line = reader.readLine();
		        	if (line!=null && line.length()!=0)
		        	{
		        		PatternCondition condition = PatternCondition.parse(line);
		        		conditionList.add(condition);
		        	}
		        	else
		        		break;
		        }
	        }
	        catch (Exception exception)
	        {
		        exception.printStackTrace();
	        }
		}

		firePropertyChange(CONDITIONS_PROPERTY,null,conditions);
	}
	
	public void setLeftEdge(boolean flag)
	{
		leftEdge = flag;
		
		firePropertyChange(LEFTEDGE_PROPERTY,null,new Boolean(leftEdge));
	}
	public void setPatternNr(int newPatternNr)
	{
		patternNr = newPatternNr;
			
		firePropertyChange(PATTERN_NR_PROPERTY,null,new Integer(patternNr));
	}
	
	public void setStartX(int x)
	{
		startX = x;
		
		firePropertyChange(STARTPOINT_PROPERTY,null,new Point(startX,startX));
	}
	
	public void setStartY(int y)
	{
		startY = y;
		
		firePropertyChange(STARTPOINT_PROPERTY,null,new Point(startX,startX));
	}
	
	public void setText(String str)
	{
		String oldText = getText();

		text = str;
		
		firePropertyChange(TEXT_PROPERTY,oldText,text);
	}
	
	public void setTopEdge(boolean flag)
	{
		topEdge = flag;
		
		firePropertyChange(TOPEDGE_PROPERTY,null,new Boolean(topEdge));
	}
	/**
	 	Since this field is here only for convenience of tree-building
	 	it's access is package scope.
	*/
//	void setTreeLeaf(int index, PatternTreeLeaf leaf)
//	{
//		if (treeLeafs==null)
//			treeLeafs = new PatternTreeLeaf[8];
//	
//		treeLeafs[index&7] = leaf;
//	}
	public void setUserX(int x)
	{
		userX = x;
		
		firePropertyChange(USERPOINT_PROPERTY,null,new Point(userX,userX));
	}
	public void setUserY(int y)
	{
		userY = y;
		
		firePropertyChange(USERPOINT_PROPERTY,null,new Point(userX,userX));
	}
	public void setWhiteX(int x)
	{
		whiteX = x;
		
		firePropertyChange(WHITEMOVE_PROPERTY,null,new Point(whiteX,whiteX));
	}
	public void setWhiteY(int y)
	{
		whiteY = y;
		
		firePropertyChange(WHITEMOVE_PROPERTY,null,new Point(whiteX,whiteX));
	}
	
	@Override
	public String toString()
	{
		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append('\n');
		if (hasTopEdge())
		{
			for (int i=0; i<width; i++)
				stringBuffer.append('_');
			stringBuffer.append('\n');
		}
		for (int j=0; j<height; j++)
		{
			if (hasLeftEdge())
				stringBuffer.append('|');
			for (int i=0; i<width; i++)
			{
				char c;
				byte value = getPoint(i, j);
				switch(value)
				{
					case BLACK: c = 'X'; break;
					case WHITE: c = 'O'; break;
					case EMPTY: c = '.'; break;
					case NOCARE: c = '?'; break;
					default: c = '#';
				}
				if (i==blackX && j==blackY)
				{
					c = 'x';
				}
				if (i==whiteX && j==whiteY)
				{
					if (c=='x')
						c = '@';
					else
						c = 'o';
				}
				
				stringBuffer.append(c);
			}
			if (hasRightEdge())
				stringBuffer.append('|');
			stringBuffer.append('\n');
		}
		if (hasBottomEdge())
		{
			for (int i=0; i<width; i++)
				stringBuffer.append('_');
			stringBuffer.append('\n');
		}
		stringBuffer.append(getConditions());
		stringBuffer.append("Black: urgency="+urgencyValueBlack+" nrOccurrences="+blackNrOccurrences+" nrTimesPlayed="+blackNrSuccesses+"\n");
		stringBuffer.append("White: urgency="+urgencyValueWhite+" nrOccurrences="+whiteNrOccurrences+" nrTimesPlayed="+whiteNrSuccesses+"\n");
		stringBuffer.append('\n');

		return stringBuffer.toString();
	}
	
	/**
	 * @return a string to be used to write to a file.
	 */
	public String toFile()
	{
		StringBuilder tmpString = new StringBuilder();
		
		tmpString.append(uniqueId);				tmpString.append(' ');
		tmpString.append(groupId); 				tmpString.append(' ');
		tmpString.append(patternNr); 			tmpString.append(' ');
		tmpString.append(width); 				tmpString.append(' ');
		tmpString.append(height); 				tmpString.append(' ');
		tmpString.append(type); 				tmpString.append(' ');
		tmpString.append(startX); 				tmpString.append(' ');
		tmpString.append(startY); 				tmpString.append(' ');
		tmpString.append(blackX); 				tmpString.append(' ');
		tmpString.append(blackY); 				tmpString.append(' ');
		tmpString.append(whiteX); 				tmpString.append(' ');
		tmpString.append(whiteY); 				tmpString.append(' ');
		tmpString.append(userX); 				tmpString.append(' ');
		tmpString.append(userY); 				tmpString.append(' ');
		tmpString.append(topEdge); 				tmpString.append(' ');
		tmpString.append(leftEdge); 			tmpString.append(' ');
//		tmpString.append("'"+text+"'"); 		tmpString.append(' ');
//		tmpString.append("'"+conditions+"'");	tmpString.append(' ');
		tmpString.append(blackBits[0]); 		tmpString.append(' ');
		tmpString.append(blackBits[1]); 		tmpString.append(' ');
		tmpString.append(blackBits[2]); 		tmpString.append(' ');
		tmpString.append(blackBits[3]); 		tmpString.append(' ');
		tmpString.append(whiteBits[0]); 		tmpString.append(' ');
		tmpString.append(whiteBits[1]); 		tmpString.append(' ');
		tmpString.append(whiteBits[2]); 		tmpString.append(' ');
		tmpString.append(whiteBits[3]); 		tmpString.append(' ');
		tmpString.append(emptyBits[0]); 		tmpString.append(' ');
		tmpString.append(emptyBits[1]); 		tmpString.append(' ');
		tmpString.append(emptyBits[2]); 		tmpString.append(' ');
		tmpString.append(emptyBits[3]); 		tmpString.append(' ');
		tmpString.append(urgencyValueBlack); 	tmpString.append(' ');
		tmpString.append(urgencyValueWhite); 	tmpString.append(' ');
		tmpString.append(blackNrOccurrences); 	tmpString.append(' ');
		tmpString.append(whiteNrOccurrences); 	tmpString.append(' ');
		tmpString.append(blackNrSuccesses); 	tmpString.append(' ');
		tmpString.append(whiteNrSuccesses); 	tmpString.append(' ');
		tmpString.append(generated); 			tmpString.append(' ');
//		tmpString.append(createdDate); 			tmpString.append(' ');
		
		return tmpString.toString();
	}
	
	/**
	 * Parse a pattern from an line containing its field-values.
	 * 
	 * @param line
	 * @return Pattern
	 */
	public static Pattern parse(String line)
	{
		Pattern pattern = new Pattern();
		
		StringTokenizer tokenizer = new StringTokenizer(line," ");
		
		pattern.uniqueId = Integer.parseInt(tokenizer.nextToken());
		pattern.groupId = Integer.parseInt(tokenizer.nextToken());
		pattern.patternNr = Integer.parseInt(tokenizer.nextToken());
		pattern.width = Integer.parseInt(tokenizer.nextToken());
		pattern.height = Integer.parseInt(tokenizer.nextToken());
		pattern.type = Integer.parseInt(tokenizer.nextToken());
		pattern.startX = Integer.parseInt(tokenizer.nextToken());
		pattern.startY = Integer.parseInt(tokenizer.nextToken());
		pattern.blackX = Integer.parseInt(tokenizer.nextToken());
		pattern.blackY = Integer.parseInt(tokenizer.nextToken());
		pattern.whiteX = Integer.parseInt(tokenizer.nextToken());
		pattern.whiteY = Integer.parseInt(tokenizer.nextToken());
		pattern.userX = Integer.parseInt(tokenizer.nextToken());
		pattern.userY = Integer.parseInt(tokenizer.nextToken());
		pattern.topEdge = Boolean.parseBoolean(tokenizer.nextToken());
		pattern.leftEdge = Boolean.parseBoolean(tokenizer.nextToken());
//		pattern.text = tokenizer.nextToken().substring(1,);
//		pattern.conditions = tokenizer.nextToken();
		pattern.blackBits[0] = Long.parseLong(tokenizer.nextToken());
		pattern.blackBits[1] = Long.parseLong(tokenizer.nextToken());
		pattern.blackBits[2] = Long.parseLong(tokenizer.nextToken());
		pattern.blackBits[3] = Long.parseLong(tokenizer.nextToken());
		pattern.whiteBits[0] = Long.parseLong(tokenizer.nextToken());
		pattern.whiteBits[1] = Long.parseLong(tokenizer.nextToken());
		pattern.whiteBits[2] = Long.parseLong(tokenizer.nextToken());
		pattern.whiteBits[3] = Long.parseLong(tokenizer.nextToken());
		pattern.emptyBits[0] = Long.parseLong(tokenizer.nextToken());
		pattern.emptyBits[1] = Long.parseLong(tokenizer.nextToken());
		pattern.emptyBits[2] = Long.parseLong(tokenizer.nextToken());
		pattern.emptyBits[3] = Long.parseLong(tokenizer.nextToken());
		pattern.urgencyValueBlack = Double.parseDouble(tokenizer.nextToken());
		pattern.urgencyValueWhite = Double.parseDouble(tokenizer.nextToken());
		pattern.blackNrOccurrences = Integer.parseInt(tokenizer.nextToken());
		pattern.blackNrSuccesses = Integer.parseInt(tokenizer.nextToken());
		pattern.whiteNrSuccesses = Integer.parseInt(tokenizer.nextToken());
		pattern.generated = Boolean.parseBoolean(tokenizer.nextToken());
//		pattern.createdDate = new Date(Date.parse(tokenizer.nextToken()));
		
		return pattern;
	}
	/**
	 * Parse a pattern from an array of strings representing a diagram.
	 * 
	 * @param diagram
	 * @return Pattern
	 */
	public static Pattern parse(String[] diagram)
	{
		Pattern pattern = new Pattern();
		
		int width = diagram[0].trim().length();
		int offset = 0;
		if (diagram.length>1 && diagram[1].startsWith("|"))
		{
			pattern.setLeftEdge(true);
			width--;
			offset = 1;
		}
		pattern.setWidth(width);
		pattern.setHeight(0);
		
		int height = 0;
		StringBuilder conditionText = new StringBuilder();
		for (int j=0; j<diagram.length; j++)
		{
			if (diagram[j].contains("_"))
			{
				pattern.setTopEdge(true);
			}
			else if (diagram[j].startsWith("\t"))
			{
				conditionText.append(diagram[j].trim());
				conditionText.append('\n');
			}
			else
			{
				
				pattern.addBottomRow();
				for (int i=0; i<width; i++)
				{
					int x = i;
					int y = j;
					if (pattern.hasTopEdge())
						y--;

					char c = diagram[j].charAt(i+offset);
					byte value = '#';
					switch(c)
					{
					case 'X':
						value = BLACK;
						break;
					case 'O':
						value = WHITE;
						break;
					case '.':
						value = EMPTY;
						break;
					case '?':
						value = NOCARE;
						break;
					case 'b':
						value = EMPTY;
						pattern.setBlackX(x);
						pattern.setBlackY(y);
						break;
					case 'w':
						value = EMPTY;
						pattern.setWhiteX(x);
						pattern.setWhiteY(y);
						break;
					case '@':
						value = EMPTY;
						pattern.setBlackX(x);
						pattern.setBlackX(y);
						pattern.setWhiteX(x);
						pattern.setWhiteX(y);
						break;
					case '#':
						value = EMPTY;
						pattern.setUserX(x);
						pattern.setUserX(y);
						break;
					case '*':
						value = EMPTY;
						pattern.setBlackX(x);
						pattern.setBlackX(y);
						pattern.setWhiteX(x);
						pattern.setWhiteX(y);
						pattern.setUserX(x);
						pattern.setUserX(y);
						break;
					case 'B':
						value = EMPTY;
						pattern.setBlackX(x);
						pattern.setBlackX(y);
						pattern.setUserX(x);
						pattern.setUserX(y);
						break;
					case 'W':
						value = EMPTY;
						pattern.setWhiteX(x);
						pattern.setWhiteX(y);
						pattern.setUserX(x);
						pattern.setUserX(y);
						break;
					}
					pattern.setPoint(i,height,value);
				}
				height++;
			}
		}
		pattern.setConditions(conditionText.toString());
		
		return pattern;
	}
	
	/**
	 * @return Returns the uniqueId.
	 */
	public int getUniqueId()
	{
		return uniqueId;
	}
	/**
	 * @param uniqueId The uniqueId to set.
	 */
	public void setUniqueId(int uniqueId)
	{
		this.uniqueId = uniqueId;
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
	
	// The following getters/setters are for Hibernate purposes only.
	
	/**
	 * @return Returns the type.
	 */
	public int getType()
	{
		return type;
	}
	
	/**
	 * @param type The type to set.
	 */
	public void setType(int type)
	{
		this.type = type;
	}
	
	/**
	 * @return Returns the urgencyValue.
	 */
	public double getUrgencyValueBlack()
	{
		return urgencyValueBlack;
	}
	/**
	 * @param urgencyValue The urgencyValue to set.
	 */
	public void setUrgencyValueBlack(double urgencyValue)
	{
		totalBlackLength = (int)(urgencyValue * blackNrSuccesses);
		this.urgencyValueBlack = urgencyValue;
	}
	/**
	 * @return Returns the urgencyValue.
	 */
	public double getUrgencyValueWhite()
	{
		return urgencyValueWhite;
	}
	/**
	 * @param urgencyValue The urgencyValue to set.
	 */
	public void setUrgencyValueWhite(double urgencyValue)
	{
		totalWhiteLength = (int)(urgencyValue * whiteNrSuccesses);
		this.urgencyValueWhite = urgencyValue;
	}
	
	public void updateBlackUrgency(int nrMoves)
	{
		totalBlackLength += nrMoves;
		if (blackNrSuccesses>0)
			urgencyValueBlack = totalBlackLength / blackNrSuccesses;
		assert urgencyValueBlack>=0.0;
		dirty = true;
	}
	
	public void updateWhiteUrgency(int nrMoves)
	{
		totalWhiteLength += nrMoves;
		if (whiteNrSuccesses>0)
			urgencyValueWhite = totalWhiteLength / whiteNrSuccesses;
		assert urgencyValueWhite>=0.0;
		dirty = true;
	}
	
	public boolean isUrgent()
	{
		return (urgencyValueBlack<50 || urgencyValueWhite<50);
	}
	
	public boolean isUseful()
	{
		if (blackNrSuccesses>10 || whiteNrSuccesses>10)
			return true;
		if (urgencyValueBlack<45 && blackNrSuccesses>2)
			return true;
		if (urgencyValueWhite<45 && whiteNrSuccesses>2)
			return true;
		return false;
	}
	
//	public boolean isUseful()
//	{
//		if (blackNrSuccesses>1000 || whiteNrSuccesses>1000)
//			return true;
//		if (urgencyValueBlack<200 && blackNrSuccesses>100)
//			return true;
//		if (urgencyValueWhite<200 && whiteNrSuccesses>100)
//			return true;
//		return false;
//	}
	/**
	 * @param height The height to set.
	 */
	public void setHeight(int height)
	{
		this.height = height;
	}
	/**
	 * @param width The width to set.
	 */
	public void setWidth(int width)
	{
		this.width = width;
	}
		
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o)
	{
		if (o==null || !(o instanceof Pattern))
			return false;
		
		Pattern compare = (Pattern) o;
		return (compare.getPatternNr()==patternNr);
	}

	public int getBlackNrOccurrences() {
		return blackNrOccurrences;
	}

	public void setBlackNrOccurrences(int blackNrOccurrences) {
		this.blackNrOccurrences = blackNrOccurrences;
	}

	public int getBlackNrSuccesses() {
		return blackNrSuccesses;
	}

	public void setBlackNrSuccesses(int blackNrSuccesses) {
		totalBlackLength = (int)(urgencyValueBlack * blackNrSuccesses);
		this.blackNrSuccesses = blackNrSuccesses;
	}

	public int getWhiteNrOccurrences() {
		return whiteNrOccurrences;
	}

	public void setWhiteNrOccurrences(int whiteNrOccurrences) {
		this.whiteNrOccurrences = whiteNrOccurrences;
	}

	public int getWhiteNrSuccesses() {
		return whiteNrSuccesses;
	}

	public void setWhiteNrSuccesses(int whiteNrSuccesses) {
		totalWhiteLength = (int)(urgencyValueWhite * whiteNrSuccesses);
		this.whiteNrSuccesses = whiteNrSuccesses;
	}
	
	public void increaseBlackRatio()
	{
		blackNrOccurrences++;
		blackNrSuccesses++;
		dirty = true;
	}
	
	public void decreaseBlackRatio()
	{
		blackNrOccurrences++;
		dirty = true;
	}
	
	public void punishBlackRatio()
	{
		blackNrOccurrences*=2;
		dirty = true;
	}
	
	public void increaseWhiteRatio()
	{
		whiteNrOccurrences++;
		whiteNrSuccesses++;
		dirty = true;
	}
	
	public void decreaseWhiteRatio()
	{
		whiteNrOccurrences++;
		dirty = true;
	}

	public void increaseBlackOccurrence()
	{
		blackNrOccurrences++;
		dirty = true;
	}

	public void increaseWhiteOccurrence()
	{
		whiteNrOccurrences++;
		dirty = true;
	}
	
	public void punishWhiteRatio()
	{
		whiteNrOccurrences*=2;
		dirty = true;
	}

	/**
     * @return the dirty
     */
    public boolean isDirty()
    {
    	return dirty;
    }

	/**
     * @param dirty the dirty to set
     */
    public void setDirty(boolean dirty)
    {
    	this.dirty = dirty;
    }

	public boolean rankBefore(Pattern comparePattern)
	{
		// Favour the edges towards the top-left.
		if (hasTopEdge())
		{
			if (!comparePattern.hasTopEdge())
				return true;
		}
		else if (comparePattern.hasTopEdge())
			return false;
		
		if (hasLeftEdge())
		{
			if (!comparePattern.hasLeftEdge())
			return true;
		}
		else if (comparePattern.hasLeftEdge())
			return false;
		
		if (emptyBits[0]<comparePattern.emptyBits[0])
			return true;
		if (emptyBits[0]==comparePattern.emptyBits[0])
		{
			if (emptyBits[1]<comparePattern.emptyBits[1])
				return true;
			if (emptyBits[1]==comparePattern.emptyBits[1])
			{
				if (emptyBits[2]<comparePattern.emptyBits[2])
					return true;
				if (emptyBits[2]==comparePattern.emptyBits[2])
				{
					if (emptyBits[3]<comparePattern.emptyBits[3])
						return true;
					if (emptyBits[3]==comparePattern.emptyBits[3])
					{
						if (blackBits[0]<comparePattern.blackBits[0])
							return true;
						if (blackBits[0]==comparePattern.blackBits[0])
						{
							if (blackBits[1]<comparePattern.blackBits[1])
								return true;
							if (blackBits[1]==comparePattern.blackBits[1])
							{
								if (blackBits[2]<comparePattern.blackBits[2])
									return true;
								if (blackBits[2]==comparePattern.blackBits[2])
								{
									if (blackBits[3]<comparePattern.blackBits[3])
										return true;
									if (blackBits[3]==comparePattern.blackBits[3])
									{
										if (whiteBits[0]<comparePattern.whiteBits[0])
											return true;
										if (whiteBits[0]==comparePattern.whiteBits[0])
										{
											if (whiteBits[1]<comparePattern.whiteBits[1])
												return true;
											if (whiteBits[1]==comparePattern.whiteBits[1])
											{
												if (whiteBits[2]<comparePattern.whiteBits[2])
													return true;
												if (whiteBits[2]==comparePattern.whiteBits[2])
												{
													if (whiteBits[3]<comparePattern.whiteBits[3])
														return true;
													if (whiteBits[3]==comparePattern.whiteBits[3])
													{
														return false; // Could check more?
													}
												}	
											}
										}										
									}
								}	
							}
						}
					}
				}	
			}
		}
		return false;
	}
	
	public boolean isSamePattern(Pattern comparePattern)
	{
		for (int i=0; i<4; i++)
		{
			if (emptyBits[i]!=comparePattern.emptyBits[i])
				return false;
			if (whiteBits[i]!=comparePattern.whiteBits[i])
				return false;
			if (blackBits[i]!=comparePattern.blackBits[i])
				return false;
		}
		if (blackX!=comparePattern.blackX)
			return false;
		if (blackY!=comparePattern.blackY)
			return false;
		if (whiteX!=comparePattern.whiteX)
			return false;
		if (whiteY!=comparePattern.whiteY)
			return false;
		if (topEdge!=comparePattern.topEdge)
			return false;
		if (leftEdge!=comparePattern.leftEdge)
			return false;
		
		for (PatternCondition condition : conditionList)
			if (comparePattern.conditionList.indexOf(condition)<0)
				return false;
		
		return true;
	}

	public boolean hasLeftEdge()
	{
		return leftEdge;
	}
	public boolean hasTopEdge()
	{
		return topEdge;
	}

	/**
     * @return the bottomEdge
     */
    public boolean hasBottomEdge()
    {
    	return bottomEdge;
    }

	/**
     * @param bottomEdge the bottomEdge to set
     */
    public void setBottomEdge(boolean bottomEdge)
    {
    	this.bottomEdge = bottomEdge;
    }

	/**
     * @return the rightEdge
     */
    public boolean hasRightEdge()
    {
    	return rightEdge;
    }

	/**
     * @param rightEdge the rightEdge to set
     */
    public void setRightEdge(boolean rightEdge)
    {
    	this.rightEdge = rightEdge;
    }

	//
	// The setters and getters below are for Hibernate only.
    //
	// Don't use them! *************************************
    //
    // They're marked deprecated to discourage use.
	//
	/**
     * @return the topEdge
     * @deprecated
     */
    public boolean isTopEdge()
    {
    	return topEdge;
    }

	/**
     * @return the leftEdge
     * @deprecated
     */
    public boolean isLeftEdge()
    {
    	return leftEdge;
    }

	
	/**
	 * @return Returns the blackBits[0].
     * @deprecated
	 */
	public long getBlackBits0()
	{
		return blackBits[0];
	}
	
	/**
	 * @param bits set at blackBits[0]
     * @deprecated
	 */
	public void setBlackBits0(long bits)
	{
		blackBits[0]= bits;
	}
	
	/**
	 * @return Returns the blackBits[1].
     * @deprecated
	 */
	public long getBlackBits1()
	{
		return blackBits[1];
	}
	
	/**
	 * @param bits set at blackBits[1]
     * @deprecated
	 */
	public void setBlackBits1(long bits)
	{
		blackBits[1]= bits;
	}
	
	/**
	 * @return Returns the blackBits[2].
     * @deprecated
	 */
	public long getBlackBits2()
	{
		return blackBits[2];
	}
	
	/**
	 * @param bits set at blackBits[2]
     * @deprecated
	 */
	public void setBlackBits2(long bits)
	{
		blackBits[2]= bits;
	}
	
	/**
	 * @return Returns the blackBits[3].
     * @deprecated
	 */
	public long getBlackBits3()
	{
		return blackBits[3];
	}
	
	/**
	 * @param bits set at blackBits[3]
     * @deprecated
	 */
	public void setBlackBits3(long bits)
	{
		blackBits[3]= bits;
	}
	
	/**
	 * @return Returns the whiteBits[0].
     * @deprecated
	 */
	public long getWhiteBits0()
	{
		return whiteBits[0];
	}
	
	/**
	 * @param bits set at whiteBits[0]
     * @deprecated
	 */
	public void setWhiteBits0(long bits)
	{
		whiteBits[0]= bits;
	}
	
	/**
	 * @return Returns the blackBits[1].
     * @deprecated
	 */
	public long getWhiteBits1()
	{
		return whiteBits[1];
	}
	
	/**
	 * @param bits set at whiteBits[1]
     * @deprecated
	 */
	public void setWhiteBits1(long bits)
	{
		whiteBits[1]= bits;
	}
	
	/**
	 * @return Returns the whiteBits[2].
     * @deprecated
	 */
	public long getWhiteBits2()
	{
		return whiteBits[2];
	}
	
	/**
	 * @param bits set at whiteBits[2]
     * @deprecated
	 */
	public void setWhiteBits2(long bits)
	{
		whiteBits[2]= bits;
	}
	
	/**
	 * @return Returns the whiteBits[3].
     * @deprecated
	 */
	public long getWhiteBits3()
	{
		return whiteBits[3];
	}
	
	/**
	 * @param bits set at whiteBits[3]
     * @deprecated
	 */
	public void setWhiteBits3(long bits)
	{
		whiteBits[3]= bits;
	}
	
	/**
	 * @return Returns the emptyBits[0].
     * @deprecated
	 */
	public long getEmptyBits0()
	{
		return emptyBits[0];
	}
	
	/**
	 * @param bits set at emptyBits[0]
     * @deprecated
	 */
	public void setEmptyBits0(long bits)
	{
		emptyBits[0]= bits;
	}
	
	/**
	 * @return Returns the emptyBits[1].
     * @deprecated
	 */
	public long getEmptyBits1()
	{
		return emptyBits[1];
	}
	
	/**
	 * @param bits set at emptyBits[1]
     * @deprecated
	 */
	public void setEmptyBits1(long bits)
	{
		emptyBits[1]= bits;
	}
	
	/**
	 * @return Returns the emptyBits[2].
     * @deprecated
	 */
	public long getEmptyBits2()
	{
		return emptyBits[2];
	}
	
	/**
	 * @param bits set at emptyBits[2]
     * @deprecated
	 */
	public void setEmptyBits2(long bits)
	{
		emptyBits[2]= bits;
	}
	
	/**
	 * @return Returns the emptyBits[3].
     * @deprecated
	 */
	public long getEmptyBits3()
	{
		return emptyBits[3];
	}
	
	/**
	 * @param bits set at emptyBits[3]
     * @deprecated
	 */
	public void setEmptyBits3(long bits)
	{
		emptyBits[3]= bits;
	}

	/**
     * @return the added
     */
    public boolean isAdded()
    {
    	return added;
    }

	/**
     * @param added the added to set
     */
    public void setAdded(boolean added)
    {
    	this.added = added;
    }

	/**
     * @return the removed
     */
    public boolean isRemoved()
    {
    	return removed;
    }

	/**
     * @param removed the removed to set
     */
    public void setRemoved(boolean removed)
    {
    	this.removed = removed;
    }

	/**
     * @return the generated
     */
    public boolean isGenerated()
    {
    	return generated;
    }

	/**
     * @param generated the generated to set
     */
    public void setGenerated(boolean generated)
    {
    	this.generated = generated;
    }
    
    public void initOccurrences()
    {
    	urgencyValueBlack = 1;
    	urgencyValueWhite = 1;
    	blackNrOccurrences = 1;
    	whiteNrOccurrences = 1;
    	blackNrSuccesses = 1;
    	whiteNrSuccesses = 1;
    }

	/**
     * @return the createdDate
     */
    public Date getCreatedDate()
    {
    	return createdDate;
    }

	/**
     * @param createdDate the createdDate to set
     */
    public void setCreatedDate(Date createdDate)
    {
    	this.createdDate = createdDate;
    }
    
    private boolean isCentered(int x, int y)
    {
    	if (x==0 && (y==0 || y==1 || y==-1))
    		return true;
    	if (y==0 && (x==1 || x==-1))
    		return true;
    	return false;
    }
}
