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

import java.text.ParseException;

import tesuji.core.util.ArrayStack;
import tesuji.core.util.FactoryReport;
import tesuji.games.general.BoardMark;
import tesuji.games.general.BoardMarkFactory;
import tesuji.games.general.MoveFactory;
import tesuji.games.general.MoveStack;
import tesuji.games.go.util.SGFUtil;
import tesuji.games.sgf.SGFData;
import tesuji.games.sgf.SGFProperty;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;
import static tesuji.games.sgf.SGFSymbols.BLACK_MARK;
import static tesuji.games.sgf.SGFSymbols.BLACK_MOVE;
import static tesuji.games.sgf.SGFSymbols.COMMENT;
import static tesuji.games.sgf.SGFSymbols.EMPTY_MARK;
import static tesuji.games.sgf.SGFSymbols.WHITE_MARK;
import static tesuji.games.sgf.SGFSymbols.WHITE_MOVE;

/** 
 * Factory class that creates GoMove objects.
 */
public class GoMoveFactory
	implements MoveFactory<GoMove>
{
	/**
	 * This helper-class ensures there's a unique factory for each thread.
	 * This has the double benefit of not needing synchronization of the object-pool
	 * and that threads don't have to wait for each other to create objects.
	 */
	static class LocalAllocationHelper
		extends ThreadLocal<GoMoveFactory>
	{
		@Override
		public GoMoveFactory initialValue()
		{
			return new GoMoveFactory();
		}
	}

	private static int nrMoves = 0;
	private static int nrLightMoves = 0;
	private static int nrMoveInfo = 0;
	
	private static LocalAllocationHelper _singleton;

    private ArrayStack<GoMoveImpl> movePool = new ArrayStack<GoMoveImpl>();
    private ArrayStack<BlackGoMoveImpl> blackMovePool = new ArrayStack<BlackGoMoveImpl>();
    private ArrayStack<WhiteGoMoveImpl> whiteMovePool = new ArrayStack<WhiteGoMoveImpl>();
    private ArrayStack<GoMoveInfo> moveInfoPool = new ArrayStack<GoMoveInfo>();
	
    private static final GoMove _blackPass = getSingleton().createLightMove(PASS, BLACK);
    private static final GoMove _whitePass = getSingleton().createLightMove(PASS, WHITE);
    
	public static GoMoveFactory getSingleton()
	{
		if (_singleton==null)
		{
			_singleton = new LocalAllocationHelper();
			FactoryReport.addFactory(_singleton.get());
		}
		
		return _singleton.get();
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.core.util.Factory#getFactoryName()
	 */
	public String getFactoryName()
	{
		return "GoMoveFactory";
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.core.util.Factory#getFactoryReport()
	 */
	public String getFactoryReport()
	{
		return "Number of GoMove objects:\t\t\t"+nrMoves+"\n"+
			"Number of light GoMove objects:\t\t\t"+nrLightMoves+"\n" +
			"Number of MoveInfo objects:\t\t\t"+nrMoveInfo;
	}
    
    private GoMoveFactory()
    {	
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.general.MoveFactory#createMove()
     */
    public GoMove createMove()
    {
        GoMoveImpl newMove;
        if (movePool.isEmpty())
        {
            newMove = new GoMoveImpl(movePool);
            nrMoves++;
        }
        else
            newMove = movePool.pop();
        
        return newMove;
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.general.MoveFactory#createMove()
     */
    public GoMove createBlackMove()
    {
    	GoMove newMove;
        if (blackMovePool.isEmpty())
        {
            newMove = new BlackGoMoveImpl(blackMovePool);
            nrMoves++;
        }
        else
            newMove = blackMovePool.pop();
        
        return newMove;
    }
    
    /*
     * (non-Javadoc)
     * @see tesuji.games.general.MoveFactory#createMove()
     */
    public GoMove createWhiteMove()
    {
    	GoMove newMove;
        if (whiteMovePool.isEmpty())
        {
            newMove = new WhiteGoMoveImpl(whiteMovePool);
            nrMoves++;
        }
        else
            newMove = whiteMovePool.pop();
        
        return newMove;
    }
    
    public GoMove createMove(int xy, byte color)
    {
    	GoMove move = createMove();
    	move.setXY(xy);
    	move.setColor(color);
    	return move;
    }
            
    public GoMove createLightMove(int xy, byte color)
    {
    	GoMove move;
    	if (color==BLACK)
    		move = createBlackMove();
    	else
    		move = createWhiteMove();
    	move.setXY(xy);
    	return move;
    }
            
    public GoMove createMove(int x, int y, byte color)
    {
    	GoMove move = createMove();
    	move.setXY(x, y);
    	move.setColor(color);
    	return move;
    }

    /*
     * (non-Javadoc)
     * @see tesuji.games.general.MoveFactory#cloneMove(tesuji.games.general.Move)
     */
    public GoMove cloneMove(GoMove move)
    {
        GoMove clone = createMove();
      
        clone.setXY(move.getXY());
        clone.setColor(move.getColor());
        clone.setUrgency(move.getUrgency());
        
        if (move.hasCaptives())
        {
        	for (int i=move.getCaptives().getSize(); --i>=0;)
        		clone.getCaptives().push(move.getCaptives().get(i));
        }

        return clone;
    }    

    /*
     * (non-Javadoc)
     * @see tesuji.games.general.MoveFactory#createMove(tesuji.games.general.BoardMark)
     */
	public GoMove createMove(BoardMark boardMark)
	{
		if (boardMark.getColor()==BLACK || boardMark.getColor()==WHITE)
			return createMove(boardMark.getX(),boardMark.getY(), boardMark.getColor());
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.MoveFactory#parseMove(java.lang.String)
	 */
	public GoMove parseMove(String moveString)
	{
		String parseString = moveString.toLowerCase();
		byte color = EMPTY;
		if (parseString.startsWith("w "))
		{
			color = WHITE;
			parseString = parseString.substring(2);
		}
		if (parseString.startsWith("white "))
		{
			color = WHITE;
			parseString = parseString.substring(6);
		}
		if (parseString.startsWith("b "))
		{
			color = BLACK;
			parseString = parseString.substring(2);
		}
		if (parseString.startsWith("black "))
		{
			color = BLACK;
			parseString = parseString.substring(6);
		}
	
		if (parseString.indexOf("pass")>=0)
		{
			GoMove goMove = createMove();
			goMove.setXY(PASS);
			goMove.setColor(color);
			return goMove;
		}

		if (parseString.indexOf("resign")>=0)
		{
			GoMove goMove = createMove();
			goMove.setXY(RESIGN);
			goMove.setColor(color);
			return goMove;
		}

		int x = parseString.charAt(0)-'a'+1;
		if (x>8)
			x--;
		parseString = parseString.substring(1);
		int y = Integer.parseInt(parseString);
		GoMove goMove = createMove();
		goMove.setXY(x,y);
		goMove.setColor(color);
		return goMove;
	}

	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.MoveFactory#parseMove(tesuji.games.sgf.SGFData)
	 */
	public GoMove parseMove(SGFData<GoMove> sgfNode)
	{
		GoMove goMove;
		if (sgfNode.getMove()==null)
		{
			goMove = GoMoveFactory.getSingleton().createMove();

			for (SGFProperty property : sgfNode.getPropertyList())
			{
				String propertyName = property.getName().toString();
				if (propertyName.equals(BLACK_MOVE))
				{				
					goMove.setColor(BLACK);
					parseMoveLocation(property, goMove);
				}
				else if (propertyName.equals(WHITE_MOVE))
				{
					goMove.setColor(WHITE);
					parseMoveLocation(property, goMove);
				}
				else if (propertyName.equals(COMMENT))
				{
//					move.setText(property.getValue());
				}
				else if (propertyName.equals(BLACK_MARK))
				{
					parseBoardMark(goMove, property, BLACK);
				}
				else if (propertyName.equals(WHITE_MARK))
				{
					parseBoardMark(goMove, property, WHITE);
				}
				else if (propertyName.equals(EMPTY_MARK)) // TODO - review whether this should be supported
				{
//					parseBoardMarks(move, property, EMPTY);
				}
			}
		}
		else
			goMove = sgfNode.getMove();
		
		return goMove;
	}
	
	/**
	 * @param property
	 * @param move
	 */
	public static void parseMoveLocation(SGFProperty property, GoMove move)
	{
		StringBuffer value = property.getValue();
		if (value.length()==2)
		{
			int x = value.charAt(0)-'a'+1;
			int y = value.charAt(1)-'a'+1;
			
			//	There are old game-records where this means pass.
			if (x==20 && y==20) 
				move.setXY(0,0);
			else
			{
				move.setXY(x,y);
				assert(x>=1 && x<=19 && y>=1 && y<=19);
			}
		}
		else if (value.length()==0)
			move.setXY(0,0);
	}
	
	/**
	 * @param property
	 * @param index
	 * @param boardMark
	 */
	private static void parseBoardMarkLocation(SGFProperty property, BoardMark boardMark)
	{
		StringBuffer value = property.getValue();
		if (value.length()==2)
		{
			int x = value.charAt(0)-'a'+1;
			int y = value.charAt(1)-'a'+1;
			assert(x>=1 && x<=19 && y>=1 && y<=19);
			boardMark.setX(x);
			boardMark.setY(y);
		}
	}
	
	/**
	 * @param move
	 * @param property
	 * @param type
	 */
	private static void parseBoardMark(GoMove move, SGFProperty property, byte color)
	{
		BoardMark boardMark = BoardMarkFactory.createBoardMark();
		boardMark.setColor(color);
		parseBoardMarkLocation(property,boardMark); // TODO - some more complex marking
		move.addBoardMark(boardMark);
	}

	
   /**
     * @return GoMoveInfo
     */
    public static GoMoveInfo createGoMoveInfo()
    {
    	return getSingleton()._createGoMoveInfo();
    }
    private GoMoveInfo _createGoMoveInfo()
    {
    	synchronized(moveInfoPool)
    	{
	        GoMoveInfo newMoveInfo;
	        if (moveInfoPool.isEmpty())
	        {
	            newMoveInfo = new GoMoveInfo(moveInfoPool);
	            nrMoveInfo++;
	        }
	        else
	        	newMoveInfo = moveInfoPool.pop();
	        return newMoveInfo;
    	}
    }

	/* (non-Javadoc)
	 * @see tesuji.games.general.MoveFactory#createPassMove(byte)
	 */
	public GoMove createPassMove(byte color)
	{
		return createMove(PASS, color);
	}
	
	public GoMove createLightPassMove(byte color)
	{
		if (color==BLACK)
			return _blackPass;
		else
			return _whitePass;
	}
	
	/* (non-Javadoc)
	 * @see tesuji.games.general.MoveFactory#createResignMove(byte)
	 */
	public GoMove createResignMove(byte color)
	{
		return createMove(RESIGN, color);
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.general.MoveFactory#createDummyMove(byte)
	 */
	public GoMove createDummyMove(byte color)
	{
		return createMove(UNDEFINED_COORDINATE, color);
	}
	
	/* (non-Javadoc)
	 * @see tesuji.games.general.MoveFactory#parseDiagram(java.lang.String[])
	 */
	public MoveStack<GoMove> parseDiagram(String[] diagram)
	{
		return Util.parseDiagram(diagram);
	}

	/* (non-Javadoc)
     * @see tesuji.games.general.MoveFactory#parseSGF(java.lang.String)
     */
    public MoveStack<GoMove> parseSGF(String sgfData)
    	throws ParseException
    {
	    return SGFUtil.parseSGFPath(sgfData);
    }
    
    public ArrayStack<BlackGoMoveImpl> getBlackMovePool()
    {
    	return blackMovePool;
    }
    
    public ArrayStack<WhiteGoMoveImpl> getWhiteMovePool()
    {
    	return whiteMovePool;
    }
}
