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

import tesuji.games.go.util.IntStack;
import tesuji.games.general.MoveStack;
import tesuji.games.go.util.ArrayFactory;
import tesuji.games.go.util.BoardMarker;
import tesuji.games.go.util.GoArray;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;

/**
 * Here are some routines that are generally usefull and convenient to have.
 */
public class Util
{
	public static String printCoordinate(int xy)
	{
		if (xy==PASS)
			return "pass";
		if (xy==RESIGN)
			return "resign";

		char letter = (char)('a'+GoArray.getX(xy)-1);
		if (GoArray.getX(xy)>8)
			letter++;
		return ""+letter+GoArray.getY(xy);
	}
	
	public static int parseCoordinate(String s)
	{
		if (s.equals("pass"))
			return PASS;
		if (s.equals("resign"))
			return RESIGN;

		char letter = (char)(s.charAt(0)-'a'+1);
		int x = (letter>8?letter-1:letter);
		int y = Integer.parseInt(s.substring(1));
		return GoArray.toXY(x,y);
	}
	
	public static MoveStack<GoMove> parseDiagram(String[] row)
	{
		boolean hasBorder = false;
		int diagramSize = row.length;
		MoveStack<GoMove> moveList = new MoveStack<GoMove>(diagramSize*diagramSize);
		
		for (int i=0; i<diagramSize; i++)
		{
			assert row.length==row[i].length() : "Diagram is not square";
			
			for (int j=0; j<diagramSize; j++)
			{
				byte color = EMPTY;
				char c = row[i].charAt(j);
				switch(c)
				{
					case 'B':
					case 'b':
					case 'X':
					case 'x':
					case '#':		
						color = BLACK;
						break;
					case 'W':
					case 'w':
					case 'O':
					case 'o':
						color = WHITE;
						break;
					case '|':
					case '_':
						hasBorder = true;
						break;
					default:
						color = EMPTY;
				}
				if (color==BLACK || color==WHITE)
				{
					int shift = hasBorder? 0 : 1;
					GoMove move = GoMoveFactory.getSingleton().createMove(j+shift, i+shift, color);
					moveList.push(move);
				}
			}
		}
		return moveList;
	}
	
	/**
	 * This method fills the list with the coordinates of all the
	 * four-way-connected members of the same value in the board array.
	 * This is an additive operation, so you need to explicitly clear the list
	 * and call marker.getNewMarker() if it needs to start from scratch.<br>
	 * <br>
	 * For example this can be used to get the coordinates of all the stones
	 * that belong to the same chain.
	 * 
	 * @param startXY is the starting point.
	 * @param board is the array with values.
	 * @param marker
	 */
	public static final void getMembers(IntStack members, int startXY, byte[] board, BoardMarker marker)
	{
		byte color = board[startXY];
		IntStack toDoList = ArrayFactory.createIntStack();
		toDoList.push(startXY);
		do
		{
			int xy = toDoList.pop();
			if (marker.notSet(xy))
			{
				members.push(xy);
				marker.set(xy);
				int left = GoArray.left(xy);
				int right = GoArray.right(xy);
				int above = GoArray.above(xy);
				int below = GoArray.below(xy);
				if (board[left]==color && marker.notSet(left))
					toDoList.push(left);
				if (board[right]==color && marker.notSet(right))
					toDoList.push(right);
				if (board[above]==color && marker.notSet(above))
					toDoList.push(above);
				if (board[below]==color && marker.notSet(below))
					toDoList.push(below);
			}	
		}
		while (!toDoList.isEmpty());
		toDoList.recycle();
	}

	/**
	 * @param xy is the coordinate of the move.
	 * @param color is the color of the move.
	 * @param board is the array containing the stones.
	 * 
	 * @return whether a move would make an empty triangle
	 */
	public static boolean isEmptyTriangle(int xy, byte color, byte[] board)
	{
		int left = GoArray.left(xy);
		int right = GoArray.right(xy);
		int above = GoArray.above(xy);
		int below = GoArray.below(xy);
		int left_above = GoArray.left(GoArray.above(xy));
		int left_below = GoArray.left(GoArray.below(xy));
		int right_above = GoArray.right(GoArray.above(xy));
		int right_below = GoArray.right(GoArray.below(xy));
		byte otherColor = opposite(color);
		
		if (board[left]==color && board[above]==color && board[left_above]!=otherColor)
			return true;
		if (board[left]==color && board[below]==color && board[left_below]!=otherColor)
			return true;
		if (board[right]==color && board[above]==color && board[right_above]!=otherColor)
			return true;
		if (board[right]==color && board[below]==color && board[right_below]!=otherColor)
			return true;

		if (board[left]==color)
		{
			if (board[above]==EMPTY && board[left_above]==color)
				return true;
			if (board[below]==EMPTY && board[left_below]==color)
				return true;
		}
		if (board[right]==color)
		{
			if (board[above]==EMPTY && board[right_above]==color)
				return true;
			if (board[below]==EMPTY && board[right_below]==color)
				return true;
		}
		if (board[above]==color)
		{
			if (board[left]==EMPTY && board[left_above]==color)
				return true;
			if (board[right]==EMPTY && board[right_above]==color)
				return true;
		}
		if (board[below]==color)
		{
			if (board[left]==EMPTY && board[left_below]==color)
				return true;
			if (board[right]==EMPTY && board[right_below]==color)
				return true;
		}

		return false;
	}
}