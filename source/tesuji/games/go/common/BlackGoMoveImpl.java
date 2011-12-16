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
import tesuji.core.util.NotImplementedException;
import tesuji.games.general.BoardMark;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.IntStack;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;
import static tesuji.games.go.util.GoArray.*;
import static tesuji.games.gtp.GTPCommand.*;

/** 
 * Simple data class to define a Go move. This is a 'light' version of GoMoveImpl.
 */
public class BlackGoMoveImpl
	implements GoMove
{
    private static final long serialVersionUID = -7938143642008342000L;
    
	private short _xy;
	private short _urgency = MINIMUM_PRIORITY;
	private short _visits = 0;
	private short _wins = 0;
	
	ArrayStack<BlackGoMoveImpl> _owner;
	
	/** Default constructor for GoMoveImpl */
	protected BlackGoMoveImpl(ArrayStack<BlackGoMoveImpl> owner)
	{
		_owner = owner;
		_xy = (short) UNDEFINED_COORDINATE;
	}
	
	/** 
     * Compares the x and y coordinates of the move
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
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
		StringBuffer output = new StringBuffer();
		if (getXY()!=UNDEFINED_COORDINATE && !isResignation())
		{
			output.append("B[");
			if (!isPass())
			{
				output.append((char)('a'-1+getX()));
				output.append((char)('a'-1+getY()));
			}
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
			output.append(BLACK_COLOR+" ");			
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
			output.append("B ");
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

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.Move#getColor()
	 */
	public final byte getColor()
	{
		return BLACK;
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.Move#setColor(byte)
	 */
	public final void setColor(byte color)
	{
		throw new NotImplementedException("Unexpected call on a BlackGoMoveImpl object");
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.Move#getMoveNr()
	 */
	public final int getMoveNr()
	{
		throw new NotImplementedException("Unexpected call on a BlackGoMoveImpl object");
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.Move#setMoveNr(int)
	 */
	public final void setMoveNr(int moveNr)
	{
		throw new NotImplementedException("Unexpected call on a BlackGoMoveImpl object");
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.common.GoMove#getXY()
	 */
	public final int getXY()
	{
		return _xy;
	}
    
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.common.GoMove#setXY(int)
	 */
    public final void setXY(int xy)
    {
		_xy = (short) xy;
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#getX()
     */
	public final int getX()
	{
		return GoArray.getX(_xy);
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.common.GoMove#getY()
	 */
	public final int getY()
	{
		return GoArray.getY(_xy);
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.common.GoMove#setXY(int, int)
	 */
	public void setXY(int x, int y)
	{
		setXY(toXY(x,y));		
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.common.GoMove#hasCaptives()
	 */
	public boolean hasCaptives()
	{
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.common.GoMove#addCaptive(int)
	 */
	public void addCaptive(int xy)
	{
		throw new NotImplementedException("Unexpected call on a BlackGoMoveImpl object");
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.common.GoMove#getCaptives()
	 */
	public IntStack getCaptives()
	{
		throw new NotImplementedException("Unexpected call on a BlackGoMoveImpl object");
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.core.util.FlyWeight#recycle()
	 */
	public final void recycle()
	{
		if (_xy==PASS)
			return;

		_xy = UNDEFINED_COORDINATE;
		_urgency = MINIMUM_PRIORITY;
		
		_owner.push(this);
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.Move#getBoardMarks()
	 */
	public ArrayList<BoardMark> getBoardMarks()
	{
		throw new NotImplementedException("Unexpected call on a BlackGoMoveImpl object");
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.Move#hasBoardMarks()
	 */
	public final boolean hasBoardMarks()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.Move#addBoardMark(tesuji.games.general.BoardMark)
	 */
	public void addBoardMark(BoardMark boardMark)
	{
		throw new NotImplementedException("Unexpected call on a BlackGoMoveImpl object");
	}

	/* (non-Javadoc)
     * @see tesuji.games.general.Move#getText()
     */
    public String getText()
    {
		throw new NotImplementedException("Unexpected call on a BlackGoMoveImpl object");
    }

	/* (non-Javadoc)
     * @see tesuji.games.general.Move#setText(java.lang.String)
     */
    public void setText(String text)
    {
		throw new NotImplementedException("Unexpected call on a BlackGoMoveImpl object");
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#getUrgency()
     */
    public int getUrgency()
    {
	    return _urgency;
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#setUrgency(int)
     */
    public void setUrgency(int urgency)
    {
	    _urgency = (short) urgency;   
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#getVisits()
     */
    public int getVisits()
    {
	    return _visits;
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#setVisits(int)
     */
    public void setVisits(int visits)
    {
	    _visits = (short) visits;   
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#getWins()
     */
    public int getWins()
    {
	    return _wins;
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#setWins(int)
     */
    public void setWins(int wins)
    {
	    _wins = (short) wins;   
    }

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#isPass()
     */
	public boolean isPass() 
	{
		return (_xy==PASS);
	}

	/* (non-Javadoc)
     * @see tesuji.games.go.common.GoMove#isResignation()
     */
	public boolean isResignation() 
	{
		return (_xy==RESIGN);
	}
}
