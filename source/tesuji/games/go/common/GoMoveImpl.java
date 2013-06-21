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
package tesuji.games.go.common;

import tesuji.core.util.ArrayList;
import tesuji.core.util.ArrayStack;
import tesuji.games.general.BoardMark;
import tesuji.games.go.util.ArrayFactory;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.IntStack;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;
import static tesuji.games.go.util.GoArray.*;
import static tesuji.games.gtp.GTPCommand.*;

/** 
 * Simple data class to define a Go move.
 */
public class GoMoveImpl
	implements GoMove
{
	protected boolean inUse;

	private static final long serialVersionUID = -5217137005185445957L;
	
	private byte _color;
	private int _moveNr;
	private int _xy;
	private String _text;
	
	private IntStack _captives;
	private ArrayList<BoardMark> _boardMarks;
	private int _urgency;
	private int _visits;
	private int _wins;
	
    private ArrayStack<GoMoveImpl> _owner;
    
	/** Default constructor for GoMoveImpl */
	protected GoMoveImpl(ArrayStack<GoMoveImpl> owner)
	{
        _owner = owner;
        
		_color = COLOR_UNDEFINED;
		_xy = UNDEFINED_COORDINATE;
		_text = "";
		_captives = null;
	}
	
	/** 
     * Constructor with x and y initialized
	 * 
	 * @param x
	 * @param y
	 */
	GoMoveImpl(int x, int y, ArrayStack<GoMoveImpl> owner)
	{
		this(toXY(x,y), owner);
	}
	
	/** 
     * Constructor with x and y initialized
	 * 
	 * @param xy
	 */
	GoMoveImpl(int xy, ArrayStack<GoMoveImpl> owner)
	{
		this(owner);
		_xy = xy;
	}
	
	/** 
     * Compares the x and y coordinates of the move
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		assert(inUse);
		if (o==null || !(o instanceof GoMove))
			return false;
		
		GoMove move = (GoMove)o;
		return (move.getXY()==_xy);
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.Move#isInitialised()
	 */
	public boolean isInitialised()
	{
		return (_xy!=UNDEFINED_COORDINATE);
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.Move#cloneMove()
	 */
	public GoMove cloneMove()
	{
		return GoMoveFactory.getSingleton().cloneMove(this);
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.Move#toSGF()
	 */
	public String toSGF()
	{
		assert(inUse);
		StringBuffer output = new StringBuffer();
		if (getXY()!=UNDEFINED_COORDINATE && !isResignation())
		{
			if (_color==BLACK)
				output.append("B[");
			else //if (color==WHITE)
				output.append("W[");
			if (!isPass())
			{
				output.append((char)('a'-1+getX()));
				output.append((char)('a'-1+getY()));
			}
			output.append(']');
		}
		if (hasBoardMarks())
		{
			for (int i=0; i<_boardMarks.size(); i++)
			{
				BoardMark boardMark = _boardMarks.get(i);
				output.append(boardMark.toSGF());
			}
		}
		String comment = getText();
		if (comment!=null && comment.length()!=0)
		{
			output.append("C[");
			output.append(comment);
			output.append(']');
		}
		return output.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.Move#toGTPWithColor()
	 */
	public String toGTPWithColor()
	{
		StringBuffer output = new StringBuffer();
		if (getXY()!=UNDEFINED_COORDINATE)
		{
			if (_color==BLACK)
				output.append(BLACK_COLOR+" ");
			else //if (color==WHITE)
				output.append(WHITE_COLOR+" ");
			
			output.append(toGTP());
		}
		else
			return "undefined";
		
		return output.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.Move#toGTP()
	 */
	public String toGTP()
	{
		StringBuffer output = new StringBuffer();
		if (isPass())
			output.append(PASS_MOVE);
		else if (isResignation())
			output.append(RESIGN_MOVE);
		else
		{
			output.append(Util.printCoordinate(getXY()));
		}		
		return output.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer output = new StringBuffer();
		if (getXY()!=UNDEFINED_COORDINATE)
		{
			if (_color==BLACK)
				output.append("B ");
			else //if (color==WHITE)
				output.append("W ");
			if (!isPass())
			{
				output.append(""+getX());
				output.append(",");
				output.append(""+getY());
			}
			else
				output.append("pass");
		}
		else
			return "undefined";
		
		return output.toString();
	}
	
	/**
	 * @return Returns the color.
	 */
	public final byte getColor()
	{
		assert(inUse);
		return _color;
	}
	/**
	 * @param color The color to set.
	 */
	public final void setColor(byte color)
	{
		assert(inUse);
		_color = color;
	}
	/**
	 * @return Returns the moveNr.
	 */
	public final int getMoveNr()
	{
		assert(inUse);
		return _moveNr;
	}
	
	/**
	 * @param moveNr The moveNr to set.
	 */
	public final void setMoveNr(int moveNr)
	{
		assert(inUse);
		_moveNr = moveNr;
	}
	
	/**
	 * @return Returns the xy coordinate.
	 */
	public final int getXY()
	{
		assert(inUse);
		return _xy;
	}
    
    public final void setXY(int xy)
    {
		assert(inUse);
		_xy = xy;
    }
    
	/**
	 * @return Returns the x-coordinate.
	 */
	public final int getX()
	{
		assert(inUse);
		return GoArray.getX(_xy);
	}
	
	/**
	 * @return Returns the y.
	 */
	public final int getY()
	{
		assert(inUse);
		return GoArray.getY(_xy);
	}
	
	public void setXY(int x, int y)
	{
		assert(inUse);
		setXY(toXY(x,y));		
	}
	
	public boolean hasCaptives()
	{
		assert(inUse);
		return _captives!=null;
	}
	
	public void addCaptive(int xy)
	{
		assert(inUse);
		if (_captives==null)
			_captives = ArrayFactory.createIntStack();
		_captives.push(xy);
	}
	
	/**
	 * @return the list of stones captured by this move.
	 */
	public IntStack getCaptives()
	{
		assert(inUse);
		return _captives;
	}
	
	/**
	 * @see tesuji.core.util.FlyWeight#recycle()
	 */
	public final void recycle()
	{
		assert(inUse);
		_color = COLOR_UNDEFINED;
		_moveNr = 0;
		_xy = UNDEFINED_COORDINATE;
		
		if (hasCaptives())
		{
			_captives.recycle();
			_captives = null;
		}
		
		if (hasBoardMarks())
		{
			for (int i=_boardMarks.size(); --i>=0;)
				_boardMarks.get(i).recycle();
			_boardMarks.clear();
		}
		
		if (_owner!=null)
			_owner.push(this);
		assert(!(inUse=false));
	}
	
	/**
	 * @return a list with board-marks (if any) associated with this move
	 */
	public ArrayList<BoardMark> getBoardMarks()
	{
		assert(inUse);
		return _boardMarks;
	}
	
	/**
	 * @return whether this move has any board-marks
	 */
	public final boolean hasBoardMarks()
	{
		assert(inUse);
		return (_boardMarks!=null);
	}
	
	/**
	 * Add a BoardMark to this move
	 * 
	 * @param boardMark
	 */
	public void addBoardMark(BoardMark boardMark)
	{
		assert(inUse);
		if (_boardMarks==null)
			_boardMarks = new ArrayList<BoardMark>();
		
		// Cancel a mark-stone-and-erase combination
		if (boardMark.getColor()==EMPTY)
		{
			for (int i=_boardMarks.size(); --i>=0;)
			{
				BoardMark mark = _boardMarks.get(i);
				if (mark.getColor()==BLACK || mark.getColor()==WHITE)
				{
					if (mark.hasSameLocation(boardMark))
					{
						_boardMarks.remove(i);
						return;
					}
				}
			}
		}
		
		_boardMarks.add(boardMark);
	}

	/* (non-Javadoc)
     * @see tesuji.games.general.Move#getText()
     */
    public String getText()
    {
		assert(inUse);
	    return _text;
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.Move#setText(java.lang.String)
     */
    public void setText(String text)
    {
		assert(inUse);
	    _text = text;
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#getUrgency()
     */
    public int getUrgency()
    {
		assert(inUse);
	    return _urgency;
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#setUrgency(int)
     */
    public void setUrgency(int urgency)
    {
		assert(inUse);
	    _urgency = urgency;   
    }


	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#getVisits()
     */
    public int getVisits()
    {
		assert(inUse);
	    return _visits;
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#setVisits(int)
     */
    public void setVisits(int visits)
    {
		assert(inUse);
	    _visits = visits;   
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#getWins()
     */
    public int getWins()
    {
		assert(inUse);
	    return _wins;
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#setWins(int)
     */
    public void setWins(int wins)
    {
		assert(inUse);
	    _wins = wins;   
    }
	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#isPass()
     */
	public boolean isPass() 
	{
		assert(inUse);
		return (_xy==PASS);
	}

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#isResignation()
     */
	public boolean isResignation() 
	{
		assert(inUse);
		return (_xy==RESIGN);
	}
}
