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
package tesuji.games.go.tactics;

import tesuji.games.go.util.ArrayFactory;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.IntStack;
import tesuji.games.go.util.BoardMarker;
import tesuji.games.go.util.FourCursor;
import tesuji.games.go.util.UniqueList;
import tesuji.games.model.BoardChange;
import tesuji.games.model.BoardModelListener;

import static tesuji.games.go.util.GoArray.*;
import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;
import static tesuji.games.go.tactics.TacticsConstant.*;

/**
 * Module for reading ladders, also called 2-liberty problems.<br>
 */
public class LadderReader
	implements BoardModelListener
{
	private static final int MAX_DEPTH = 60; // Adjust to necessity. Deeper is slower.
	private static final int MAX_ATARIS = 90;

 	private byte[]		board =				createBytes();
	private byte[]		rows =				createBytes();

	private IntStack	stoneList =			ArrayFactory.createIntStack();

	private int 		lastLadderMove;

	private int[]		ladderMarks =		createIntegers();
	private UniqueList	hitList =			new UniqueList();

	private byte ladderColor;		// Color of the chain in a ladder.
	private byte chasingColor;		// Color of the side that tries to catch the ladder.
	private int lib1;				// Coordinate of the first liberty of the ladder.
	private int lib2;				// Coordinate of the second liberty of the ladder.
	private int nrLiberties;		// Number of liberties in the ladder.
	private int ladderStone;		// The coordinate of the stone to chase.

	private boolean canCaptureKo;	// Set this flag if capture with ko is allowed.
	private boolean hasCapturedKo;	// A ko has already been captured.
	private int koPoint;			// External knowledge about a ko-point.

	private byte colorToMove;		// Color of the player to move next.
	private int ladderDepth;		// Reading depth of the ladder.
	private int saveByKo;			// Prevent escaping by ko until some depth. (?)
	
	private IntStack	toDoList =			ArrayFactory.createIntStack();
	private BoardMarker libertyLabels =	new BoardMarker();
	private IntStack	libertyStack =		ArrayFactory.createIntStack();

	private byte[]	dirtyMap = createBytes();
	
	public LadderReader()
	{
		this(19);
	}

	/**
	 * LadderReader module constructor
	 * 
	 * @param boardSize
	 */
	public LadderReader(int boardSize)
	{
		board = createBoardArray(boardSize);
		rows = createRowArray(boardSize);
		
		// This way we don't have to check whether there are
		// enough moves on the stack when checking for ko.
		stoneList.push(0);
		stoneList.push(0);
		stoneList.push(0);
	}

	/**
	 * Get the internal board-array used by the ladder-module.
	 * 
	 * @return byte[]
	 */
	public byte[] getBoardArray()
	{
		return board;
	}
	
	/**
	 * Set the internal board-array used by the ladder-module.
	 * 
	 * @param array which contains the board-position
	 */
	public void setBoardArray(byte[] array)
	{
//		copy(array,board); time has proven that the board returns to its initial state after reading the ladder :)
		board = array;
	}

	/**
	 * This in fact will either give the move that captures, or the move that escapes.
	 * 
	 * @return the coordinate of the last move read by the ladder-module.
	 */
	public int getLastLadderMove()
	{
		return lastLadderMove;
	}

	IntStack getLibertyStack()
	{
		return libertyStack;
	}

	/* Recursive version. It's usually slower than the non-recursive version below, but it can depend on the CPU.
	final int getNlib(int startXY, int max, byte color)
	{
		if (rows[startXY]==0)
			return 0;
		libertyLabels.getNewMarker();
		libertyStack.clear();
		
		_getNlib(startXY, max, color);
		return libertyStack.getSize();
	}
	
	private void _getNlib(int xy, int max, byte color)
	{
		libertyLabels.set(xy);
		int left = left(xy);
		byte boardLeft = board[left];
		if (boardLeft==EMPTY && libertyLabels.notSet(left))
		{
			libertyStack.push(left);
			libertyLabels.set(left);
		}
		if (libertyStack.getSize()>=max)
			return;
		int right = right(xy);
		byte boardRight = board[right];
		if (boardRight==EMPTY && libertyLabels.notSet(right))
		{
			libertyStack.push(right);
			libertyLabels.set(right);
		}
		if (libertyStack.getSize()>=max)
			return;
		int above = above(xy);
		byte boardAbove = board[above];
		if (boardAbove==EMPTY && libertyLabels.notSet(above))
		{
			libertyStack.push(above);
			libertyLabels.set(above);
		}
		if (libertyStack.getSize()>=max)
			return;
		int below = below(xy);
		byte boardBelow = board[below];
		if (boardBelow==EMPTY && libertyLabels.notSet(below))
		{
			libertyStack.push(below);
			libertyLabels.set(below);
		}
		if (libertyStack.getSize()>=max)
			return;
		
		if (boardLeft==color && libertyLabels.notSet(left))
			_getNlib(left, max, color);
		if (libertyStack.getSize()>=max)
			return;
		if (boardRight==color && libertyLabels.notSet(right))
			_getNlib(right, max, color);
		if (libertyStack.getSize()>=max)
			return;
		if (boardAbove==color && libertyLabels.notSet(above))
			_getNlib(above, max, color);
		if (libertyStack.getSize()>=max)
			return;
		if (boardBelow==color && libertyLabels.notSet(below))
			_getNlib(below, max, color);
	}*/

	final int getNlib(int startXY, int max, byte color)
	{
		// return Util.getNlib(startXY,max,color,board,libertyStack);

		// Even though the implementation here is exactly the same us in
		// Util.getNlib, having it here makes it go 10% faster. Beats me why.
		// I want to figure it out at some point, because preferable I'd
		// remove the duplicate code below.

		if (rows[startXY]==0)
			return 0;
		libertyLabels.getNewMarker();
		libertyStack.clear();
		
		int nLibs = 0;

		toDoList.clear();	
		toDoList.push(startXY);
		do
		{
			int xy = toDoList.pop();
			libertyLabels.set(xy);
			int left = left(xy);
			byte boardLeft = board[left];
			if (boardLeft==EMPTY && libertyLabels.notSet(left))
			{
				libertyStack.push(left);
				if (++nLibs>=max)
					return nLibs;
				libertyLabels.set(left);
			}
			int right = right(xy);
			byte boardRight = board[right];
			if (boardRight==EMPTY && libertyLabels.notSet(right))
			{
				libertyStack.push(right);
				if (++nLibs>=max)
					return nLibs;
				libertyLabels.set(right);
			}
			int above = above(xy);
			byte boardAbove = board[above];
			if (boardAbove==EMPTY && libertyLabels.notSet(above))
			{
				libertyStack.push(above);
				if (++nLibs>=max)
					return nLibs;
				libertyLabels.set(above);
			}
			int below = below(xy);
			byte boardBelow = board[below];
			if (boardBelow==EMPTY && libertyLabels.notSet(below))
			{
				libertyStack.push(below);
				if (++nLibs>=max)
					return nLibs;
				libertyLabels.set(below);
			}

			if (boardLeft==color && libertyLabels.notSet(left))
				toDoList.push(left);
			if (boardRight==color && libertyLabels.notSet(right))
				toDoList.push(right);
			if (boardAbove==color && libertyLabels.notSet(above))
				toDoList.push(above);
			if (boardBelow==color && libertyLabels.notSet(below))
				toDoList.push(below);
		}
		while (!toDoList.isEmpty());

		return nLibs;
	}

	public void setDirtyMap(byte[] map)
	{
		dirtyMap = map;
	}

	/**
	 * Check if a move is legal.
	 * 
	 * @param xy is the move to check.
	 * @return whether it's legal or not.
	 */
	private boolean isLegal(int xy)
	{
//		if (xy==PASS)
//			return true; 					// Pass is always legal.
		if (board[xy]!=EMPTY)
			return false;					// Occupied.
		if (getNlib(xy,1,colorToMove)!=0)
			return true;					// Has liberty, so is legal.

		byte otherColor = opposite(colorToMove);
		int left = left(xy);
		int right = right(xy);
		int above = above(xy);
		int below = below(xy);
		
		if (xy==koPoint)
		{
			if (board[left]!=colorToMove && board[right]!=colorToMove
			&& board[above]!=colorToMove && board[below]!=colorToMove)
				return false;
		}
		
		// A check is made if the move captures something, in which case it's not suicide.
		if ((board[left]!=otherColor || ladderMarks[left]!=0 || getNlib(left,2,otherColor)!=1)
		&& (board[right]!=otherColor || ladderMarks[right]!=0 || getNlib(right,2,otherColor)!=1)
		&& (board[above]!=otherColor || ladderMarks[above]!=0 || getNlib(above,2,otherColor)!=1) 
		&& (board[below]!=otherColor || ladderMarks[below]!=0 || getNlib(below,2,otherColor)!=1))
			return false;

		// Check for KO.
		if (stoneList.peek(1)==-xy && stoneList.peek(2)>=0)
		{
			int lastMove = stoneList.peek();
			if (board[left(lastMove) ]==otherColor)
				return true;
			if (board[right(lastMove)]==otherColor)
				return true;
			if (board[above(lastMove) ]==otherColor)
				return true;
			if (board[below(lastMove) ]==otherColor)
				return true;
			return false;  /* Ko */
		}

		return true;
	}

	/**
	 * Searches for the liberties of the chased chain, and for adjacent chains
	 * of the chaser that are in atari. In general the effect is just to see
	 * where the new liberties are and which chains of the opponent are captured
	 * or put into atari. But in case the ladder connects to another chain, a
	 * recursive call is needed.
	 * 
	 * @param xy is the coordinate of the chain to catch.
	 */
	private void ladderUpdate(int xy)
	{
		if (nrLiberties>=3)
			return;

		ladderMarks[xy] = ladderDepth;
		dirtyMap[xy] = 1;
		
		for (int n=3; n>=0; n--)
		{
			int neighbourXY = FourCursor.getNeighbour(xy, n);
			byte neighbour = board[neighbourXY];
			if (neighbour==chasingColor)
			{
				int nLib = getNlib(neighbourXY,2,chasingColor);
				if (nLib==0)		// Capture
					removeChain(neighbourXY);
				else if (nLib==1)	// Atari
					hitList.add(libertyStack.peek());
			}
			else if (neighbour==EMPTY)
			{
				if (lib1==UNDEFINED_COORDINATE || lib1==neighbourXY)
				{
					lib1 = neighbourXY;
					dirtyMap[neighbourXY] = 1;
				}
				else if (lib2==UNDEFINED_COORDINATE || lib2==neighbourXY)
				{
					lib2 = neighbourXY;
					dirtyMap[neighbourXY] = 1;
				}
				else
					nrLiberties = 3;
			}
			else if (ladderMarks[neighbourXY]==0 && neighbour!=EDGE)
				ladderUpdate(neighbourXY);

			if (nrLiberties>=3)		// In case of 3 liberties or more we can stop.
				return;
		}
	}

	/**
	 * Make a move and recompute all relevant information in an as efficient
	 * manner as possible. It doesn't have to be beautiful, but has to be very
	 * fast.
	 * 
	 * @param xy is the coordinate of the move.
	 */
	private void makeMove(int xy)
	{
		board[xy] = colorToMove;
		koPoint = UNDEFINED_COORDINATE;
		
		if (colorToMove==ladderColor && (xy==lib1 || xy==lib2))
		{
			// Just tried to escape by playing at it's only liberty. This means that
			// the liberties of the chased chain must be created by this move.
			nrLiberties = 0;
			lib1 = lib2 = UNDEFINED_COORDINATE;
			ladderUpdate(xy);
		}
		else	// It gets here either when the chaser moved or when the escapee
				// captured something.
		{
			dirtyMap[xy] = 1;
			if (xy==lib1)
			{
				lib1 = UNDEFINED_COORDINATE;
				nrLiberties = 1;
			}
			else if (xy==lib2)
			{
				lib2 = UNDEFINED_COORDINATE;
				nrLiberties = 1;
			}

			byte otherColor = opposite(colorToMove);
			int left = left(xy);
			int right = right(xy);
			int above = above(xy);
			int below = below(xy);

			// See if the move captured something.
			if (board[left]==otherColor && ladderMarks[left]==0 && getNlib(left,1,otherColor)==0)
				removeChain(left);
			if (board[right]==otherColor && ladderMarks[right]==0 && getNlib(right,1,otherColor)==0)
				removeChain(right);
			if (board[above]==otherColor && ladderMarks[above]==0 && getNlib(above,1,otherColor)==0)
				removeChain(above);
			if (board[below]==otherColor && ladderMarks[below]==0 && getNlib(below,1,otherColor)==0)
				removeChain(below);

			if (colorToMove==ladderColor)
			{	// Add any chains that were put in atari to the hitlist.
				// There's a potential duplicate call to getNlib here.
				// So here is maybe a possibility of some performance gain.
				if (board[left]==otherColor && getNlib(left,2,otherColor)==1)
					hitList.add(libertyStack.peek());
				if (board[right]==otherColor && getNlib(right,2,otherColor)==1)
					hitList.add(libertyStack.peek());
				if (board[above]==otherColor && getNlib(above,2,otherColor)==1)
					hitList.add(libertyStack.peek());
				if (board[below]==otherColor && getNlib(below,2,otherColor)==1)
					hitList.add(libertyStack.peek());
			}
			else
			{
				if (getNlib(xy,2,colorToMove)==1)
				{
					// The chaser put himself into atari.
					hitList.add(libertyStack.peek());
				}
			}
		}

		stoneList.push(xy);
		ladderDepth++;

		colorToMove = opposite(colorToMove);
	}

	/**
	 * This routine tries to skip the reading of the main diagonal part of the
	 * ladder. As long as there's a free path diagonally, it will just fill the
	 * arrays board[] and ladderMarks[] with the diagonal ladder pattern. This
	 * saves about 80% of the work in an ordinary ladder. As soon as it finds
	 * something in the way, it will continue reading from there.
	 * 
	 * @param l1
	 * @param l2 is the move that would make three liberties
	 * @return the result of reading the ladder after the shortcut.
	 */
	private int makeShortcut(int l1, int l2)
	{
		int position1 = 0;			// Coordinate of the last stone put in the ladder.
		int position2;				// Coordinate of the next chasing stone.
		int direction1;				// These two variables indicate the direction
		int direction2;				// the ladder is moving into.
		int steps = 0;				// Number of steps that are taken diagonally.
		int result = CANNOT_CATCH;	// result of the shortcut.

		// Note that lib2 makes 3 liberties, so the only neighbour must be a ladder-stone.
		if (board[left(l2)]!=EMPTY)
			position1 = left(l2);
		else if (board[right(l2)]!=EMPTY)
			position1 = right(l2);
		else if (board[above(l2)]!=EMPTY)
			position1 = above(l2);
		else if (board[below(l2)]!=EMPTY)
			position1 = below(l2);
		
		// Determine the direction in which the ladder moves.
		direction1 = l2-position1;
		direction2 = l1-position1;

		// Check if the situation is suitable for a shortcut.
		if (!isNeighbour(l1,position1)
			|| board[l1-direction1]!=chasingColor
			|| board[l1+direction2-direction1]!=EMPTY
			|| getNlib(l1-direction1,3,chasingColor)<3
			|| board[position1+direction2+direction2]!=EMPTY)
				return ILLEGAL;

		position2 = l2;

		do
		{
			board[position2] = chasingColor;
			dirtyMap[position2] = 1;
			position1 += direction2;
			position2 = position1+direction2;
			ladderMarks[position1] = ladderDepth;
			board[position1] = ladderColor;
			dirtyMap[position1] = 1;
			int d = direction1; direction1 = direction2; direction2 = d;
			steps++;
		}
		while (board[position2+direction1]==EMPTY
			&& board[position2+direction2]==EMPTY
			&& board[position1+direction2+direction2]==EMPTY);

		// Put just the last stone in the stone-list before continuing
		// reading the ladder and set the liberties of the ladder to
		// the two empty points next to it..
		stoneList.push(position1);

		// Set the two liberties of the ladder.
		lib1 = position1+direction1;
		lib2 = position1+direction2;

		// And now continue reading as normal.
		result = readLadder();

		stoneList.pop();

		while (steps!=0)
		{
			steps--;
			int d = direction1; direction1 = direction2; direction2 = d;
			board[position1] = EMPTY;
			ladderMarks[position1] = 0;
			position1 -= direction2;
			position2 = position1+direction1;
			board[position2] = EMPTY;
		}

		lastLadderMove = l2;
		return result;
	}

	/**
	 * Indirect recursive routine that reads the ladder. It calls
	 * tryEscapingMove() to try candidate moves. If it's a feasible move,
	 * tryEscapingMove() will call first call makeMove() to put the move on the
	 * board and then call readLadder().
	 * 
	 * @return result of the escape atempt
	 */
	private int readEscape()
	{
		int l1 = lib1;
		int l2 = lib2;
		int value = CAN_CATCH;
		int bestValue = CAN_CATCH;
		
//		int koMove=PASS;

		if (nrLiberties>1)
			return CANNOT_CATCH;
		if (hitList.getSize()>GoArray.WIDTH*4 || ladderDepth>GoArray.WIDTH*4)
			return CANNOT_CATCH;

		for (int i=hitList.getSize()-1; i>=0; i--)
		{
			int xy = hitList.get(i);
//			if (xy!=koMove)
			{
				if (isLegal(xy) && (value=tryEscapingMove(xy))==CANNOT_CATCH)
				{
					if (saveByKo<5) //&& koMove==PASS
					{
						int left = left(xy);
						int right = right(xy);
						int above = above(xy);
						int below = below(xy);

						if (board[left]!=EMPTY
						&& board[right]!=EMPTY
						&& board[above]!=EMPTY
						&& board[below]!=EMPTY
						&& board[left]!=ladderColor
						&& board[right]!=ladderColor
						&& board[above]!=ladderColor
						&& board[below]!=ladderColor)
						{
							int nAtaris = 0;
							for (int n=3; n<=0; n--)
							{
								int adjacentXY = FourCursor.getNeighbour(xy, n);
								if (board[adjacentXY]==chasingColor)
								{
									if (getNlib(adjacentXY,2,chasingColor)==1)
									{
										nAtaris++;
										if (board[left(adjacentXY)]==chasingColor)
											{ nAtaris++; break; }
										if (board[right(adjacentXY)]==chasingColor)
											{ nAtaris++; break; }
										if (board[above(adjacentXY)]==chasingColor)
											{ nAtaris++; break; }
										if (board[below(adjacentXY)]==chasingColor)
											{ nAtaris++; break; }
									}
								}
							}

							if (nAtaris==1)
							{
								saveByKo++;
//								koMove = xy;
								hitList.remove(xy);
								value = readLadder();
								saveByKo--;
								hitList.add(xy);
								if (value==CANNOT_CATCH)
									return CANNOT_CATCH;
								return CATCH_WITH_KO; // This should actually be handled as in Geta
//								assert(!wLadLib1 || !gsiBoard[wLadLib1]);
//								assert(!wLadLib2 || !gsiBoard[wLadLib2]);
//								if (wLadLib1 && ladderTry(wLadLib1)==eCannotCatch)
//									{xiSaveByKo--; return eCannotCatch;}
//								else if (wLadLib2 && ladderTry(wLadLib2)==eCannotCatch)
//									{xiSaveByKo--; return eCannotCatch;}
//								else {xiSaveByKo--; return eEscapeWithKo;} //iLadderValue=eEscapeWithKo;
							}
						}
					}
					if (value==CANNOT_CATCH)
						return CANNOT_CATCH;
				}
				if (value==CATCH_WITH_KO)
					bestValue = value;
			}
		}

		if (l1==UNDEFINED_COORDINATE)
			l1 = l2;

		// Otherwise play at the only liberty left.
		if (l1!=UNDEFINED_COORDINATE && (value=tryEscapingMove(l1))==CANNOT_CATCH)
			return CANNOT_CATCH;
		if (value==CATCH_WITH_KO)
			bestValue = value;

		return bestValue;
	}

	/**
	 * Indirect recursive routine that reads the ladder. It calls
	 * tryCatchingMove() to try candidate moves. If it's a feasible move,
	 * tryCatchingMove() will first call makeMove() to put the move on the board
	 * and then call readEscape(). The diagonal part across the board is skipped
	 * using makeShortcut().
	 * 
	 * @return result of the ladder.
	 */
	private int readLadder()
	{
		int nrOccupiedNeighbours1 = 0;
		int nrOccupiedNeighbours2 = 0;
		int l1 = lib1;
		int l2 = lib2;
		int value = CANNOT_CATCH;
		int bestValue = CANNOT_CATCH;
	
		if (nrLiberties > 2)
			return CANNOT_CATCH;
		if (hitList.getSize() > MAX_ATARIS || ladderDepth > MAX_DEPTH)
			return CANNOT_CATCH;
	
		// If the two liberties of the ladder are not next to each other, and
		// one of them makes three liberties, a shortcut is tried.
		if (!isNeighbour(l1,l2))
		{
			if (board[left(l1)] != EMPTY)
				nrOccupiedNeighbours1++;
			if (board[right(l1)] != EMPTY)
				nrOccupiedNeighbours1++;
			if (board[above(l1)] != EMPTY)
				nrOccupiedNeighbours1++;
			if (board[below(l1)] != EMPTY)
				nrOccupiedNeighbours1++;
	
			if (nrOccupiedNeighbours1 == 1)
			{
				if ((value = makeShortcut(l2, l1)) == CAN_CATCH)
					return CAN_CATCH;
				else if (value==CANNOT_CATCH)
					return CANNOT_CATCH;
				// The first liberty makes three liberties, so it HAS to play there.
				l2 = UNDEFINED_COORDINATE; // This ensures the second one is never tried.
				if (value == CATCH_WITH_KO)
					bestValue = value;
			}
			else
			{
		        if (board[left(l2)] != EMPTY)
		            nrOccupiedNeighbours2++;
		        if (board[right(l2)] != EMPTY)
		            nrOccupiedNeighbours2++;
		        if (board[above(l2)] != EMPTY)
		            nrOccupiedNeighbours2++;
		        if (board[below(l2)] != EMPTY)
		            nrOccupiedNeighbours2++;
	
				if (nrOccupiedNeighbours2 == 1)
				{
					if ((value = makeShortcut(l1, l2)) == CAN_CATCH)
						return CAN_CATCH;
					else if (value==CANNOT_CATCH)
						return CANNOT_CATCH;
					// The second liberty makes three liberties, so it HAS to play there.
					l1 = UNDEFINED_COORDINATE; // This ensures the first one is never tried.
					if (value == CATCH_WITH_KO)
						bestValue = value;
				}
			}
		}
	
		// Small optimization. Primary criteria is to play at the move that would make
		// the most liberties first. The secondary criteria is to play at the move that is
		// further from the side, so the ladder will be driven towards the closest side.
		if ((nrOccupiedNeighbours1 == nrOccupiedNeighbours2 && rows[l1] < rows[l2]) || nrOccupiedNeighbours1 > nrOccupiedNeighbours2)
		{
		    // Swap l1 and l2
		    int l3 = l1;
		    l1 = l2;
		    l2 = l3;
		}
	
		if (l1!=UNDEFINED_COORDINATE && (value = tryCatchingMove(l1)) == CAN_CATCH)
			return CAN_CATCH;
		if (value == CATCH_WITH_KO)
			bestValue = value;
		if (l2!=UNDEFINED_COORDINATE && (value = tryCatchingMove(l2)) == CAN_CATCH)
			return CAN_CATCH;
		if (value == CATCH_WITH_KO)
			bestValue = value;
	
		return bestValue;
	}

	/**
	 * Remove a chain that was captured while reading the ladder.
	 * 
	 * @param xy is the coordinate of the stones to be removed.
	 */
	private void removeChain(int xy)
	{
		int left = left(xy);
		int right = right(xy);
		int above = above(xy);
		int below = below(xy);

		if (colorToMove==ladderColor)
		{
			if (ladderMarks[left]!=0 || ladderMarks[right]!=0
			 || ladderMarks[above]!=0 || ladderMarks[below]!=0)
			{
				// A captured stone was next to the chased chain, so increase it's liberties.
				if (lib1==UNDEFINED_COORDINATE)
				{
					lib1 = xy;
					nrLiberties = 2;
				}
				else if (lib2==UNDEFINED_COORDINATE)
				{
					lib2 = xy;
					nrLiberties = 2;
				}
				else
					nrLiberties = 3;
			}
		}

		board[xy] = EMPTY;
		stoneList.push(-xy);

		byte otherColor = opposite(colorToMove);
		if (board[left]==otherColor)
			removeChain(left);
		if (board[right]==otherColor)
			removeChain(right);
		if (board[above]==otherColor)
			removeChain(above);
		if (board[below]==otherColor)
			removeChain(below);
	}

	/**
	 * Prepare to start reading a ladder.
	 * 
	 * @param xy is the coordinate of the chain to catch in a ladder.
	 * @param color is the color of the player to move.
	 */
	private void startLadder(int xy,byte color)
	{
		lastLadderMove = UNDEFINED_COORDINATE;
		lib1 = UNDEFINED_COORDINATE;
		lib2 = UNDEFINED_COORDINATE;
		colorToMove=color;
		ladderStone=xy;

		ladderColor=board[xy];
		chasingColor = opposite(ladderColor);

		hitList.clear();
		nrLiberties=0;
		ladderDepth=1;
		ladderUpdate(ladderStone);
		ladderDepth=2;
		if (nrLiberties!=3)
		{
			nrLiberties=0;
			if (lib1!=UNDEFINED_COORDINATE)
			{
				nrLiberties++;
				dirtyMap[lib1] = 1;
			}
			if (lib2!=UNDEFINED_COORDINATE)
			{
				nrLiberties++;
				dirtyMap[lib2] = 1;
			}
		}
		saveByKo=0;
		if (nrLiberties==1)
		{
			saveByKo=10;
		}
	}

	/**
	 	After reading a ladder make sure we can read the next one.

	 	<br><br>Creation date: (07-May-01 5:26:57 PM)<br><br>
	*/
	private void stopLadder()
	{
		linkMembers(0,ladderStone,board,ladderMarks);
	}

	/**
	 * Try a move to see if it cacthes a stone in a ladder.
	 * 
	 * @param xy is the move that tries to catch a stone in a ladder.
	 * @return result of the reading
	 */
	private int tryCatchingMove(int xy)
	{
		int hitListSize = hitList.getSize();
		int l1 = lib1;
		int l2 = lib2;
		boolean koCapture = hasCapturedKo;
		int value = CANNOT_CATCH;

		if (isLegal(xy))
		{
			int left = left(xy);
			int right = right(xy);
			int above = above(xy);
			int below = below(xy);

			// Check if the move is the capture of a ko.
			// Capturing a ko is only allowed as the first move. (?)
			// Something doesn't seem right, but I'm not sure yet what.
			// Maybe I messed up when moving to Java? (MB)
			if ((board[left]==ladderColor || board[left]==EDGE)
			&& (board[right]==ladderColor || board[right]==EDGE)
			&& (board[above]==ladderColor || board[above]==EDGE)
			&& (board[below]==ladderColor || board[below]==EDGE))
			{
				int count=0;

				for (int n=3; n<=0; n--)
				{
					int adjacentXY = FourCursor.getNeighbour(xy, n);
					if (rows[adjacentXY]!=0 && getNlib(adjacentXY,2,ladderColor)==1)
					{
						count++;
						if (board[left(adjacentXY)]==ladderColor)
							{ count++; break; }
						if (board[right(adjacentXY)]==ladderColor)
							{ count++; break; }
						if (board[above(adjacentXY)]==ladderColor)
							{ count++; break; }
						if (board[below(adjacentXY)]==ladderColor)
							{ count++; break; }
					}
				}
				
				// If it was, try something else.
				if (count==1)
				{
					if (hasCapturedKo || !canCaptureKo)
					{
						lib1=l1;
						lib2=l2;
						nrLiberties = 2;
						hasCapturedKo = koCapture;
						hitList.setSize(hitListSize);
						return CANNOT_CATCH;
					}
					else hasCapturedKo=true;
				}
			}
			
			makeMove(xy);

			if (nrLiberties==1)
			{
				value = readEscape();

				if (value==CAN_CATCH && hasCapturedKo && !koCapture)
					value = CATCH_WITH_KO;
			}
			unmakeMove();
			lastLadderMove = xy;
		}

		lib1=l1;
		lib2=l2;
		nrLiberties = 2;
		hasCapturedKo = koCapture;
		hitList.setSize(hitListSize);
		return value;
	}
	
	/**
	 * Read a ladder and store the result in the TacticsResult object.
	 * 
	 * @param result
	 * @return result
	 */
	public int readLadder(TacticsResult tacticsResult)
	{
		int result;
		dirtyMap = tacticsResult.getDirtyMap();
		clear(dirtyMap);
		if (tacticsResult.getNrLiberties()==1)
			result = tryEscape(tacticsResult.getXY());
		else
			result = tryLadder(tacticsResult.getXY());
		tacticsResult.setResult(result);
		return result;
	}

	/**
	 * Try to escape from a ladder with a stone or chain that has one liberty.<br>
	 * <br>
	 * 
	 * The values returned are CAN_CATCH if it can't escape, CANNOT_CATCH if it
	 * can escape and CATCH_WITH_KO if there's a ko involved.
	 * 
	 * @param xy is the coordinate of the chain to escape with
	 * @return the result value
	 */
	public int tryEscape(int xy)
	{
		int result = CAN_CATCH;
		startLadder(xy,board[xy]);
		if (nrLiberties!=1)
			result = CANNOT_CATCH;
		else result = readEscape();
		stopLadder();
		return result;
	}

	/**
	 * Try to escape from a ladder with a stone or chain that has one liberty.<br>
	 * <br>
	 * 
	 * The values returned are CAN_CATCH if it can't escape, CANNOT_CATCH if it
	 * can escape and CATCH_WITH_KO if there's a ko involved.
	 * 
	 * @param x coordinate of the chain to catch
	 * @param y coordinate of the chain to catch
	 * @return the result value
	 */
	public int tryEscape(int x, int y)
	{
		return tryEscape(toXY(x,y));
	}

	/**
	 * Try a move to see if it escapes from ladder.
	 * 
	 * @param xy is the move that tries to escape.
	 * @return result of the reading
	 */
	private int tryEscapingMove(int xy)
	{
		int hitListSize = hitList.getSize();
		int l1 = lib1;
		int l2 = lib2;
		boolean koCapture = hasCapturedKo;
		int value = CAN_CATCH;

		makeMove(xy);

		if (lib1!=UNDEFINED_COORDINATE && lib2!=UNDEFINED_COORDINATE)
			value = readLadder();

		unmakeMove();
		
		lastLadderMove = xy;

		lib1=l1;
		lib2=l2;
		nrLiberties = 1;
		hasCapturedKo = koCapture;
		hitList.setSize(hitListSize);

		return value;
	}

	/**
	 * Try catching a stone in a ladder.
	 * 
	 * A ladder is defined as a chain with two liberties which can be caught by
	 * continuously filling a liberty until the chain can't make more than one
	 * liberty in reply. If the chain ever makes three liberties, or two with
	 * the other player to move it's considered to have escaped.
	 * 
	 * Possible values are CAN_CATCH, CANNOT_CATCH or CATCH_WITH_KO
	 * 
	 * @param xy is the coordinate of the chain.
	 * @return result of reading the ladder.
	 */
	public int tryLadder(int xy)
	{
		int ladderValue;
		startLadder(xy,opposite(board[xy]));
		if (nrLiberties!=2)
			ladderValue = CANNOT_CATCH;
		else
			ladderValue=readLadder();
		stopLadder();
		return ladderValue;
	}

	/**
	 * Try catching a stone in a ladder.
	 * 
	 * A ladder is defined as a chain with two liberties which can be caught by
	 * continuously filling a liberty until the chain can't make more than one
	 * liberty in reply. If the chain ever makes three liberties, or two with
	 * the other player to move it's considered to have escaped.
	 * 
	 * Possible values are CAN_CATCH, CANNOT_CATCH or CATCH_WITH_KO
	 * 
	 * @param x is the x-coordinate of the chain.
	 * @param y is the y-coordinate of the chain.
	 * @return result of reading the ladder.
	 */
	public int tryLadder(int x, int y)
	{
		return tryLadder(toXY(x,y));
	}

	/**
	 * Unmake the last move and recompute all relevant information in an as
	 * efficient manner as possible. It doesn't have to be beautiful, but has to
	 * be very fast.
	 */
	private void unmakeMove()
	{
		ladderDepth--;
		int xy = stoneList.pop();

		// Pop the prisoners.
		while (stoneList.peek()<0)
		{
			int captive = -stoneList.pop();
			board[captive]= colorToMove;
		}
		
		board[xy]=EMPTY;
		colorToMove= opposite(colorToMove);
		if (colorToMove==ladderColor)
		{
			if (ladderMarks[xy]!=0)
			{
				ladderMarks[xy]=0;
				// Clear the marks in ladderMarks[] in the stones that are not part of the ladder anymore.
				// The comparison to ladderDepth ensures that the points that still belong to the
				// ladder are not erased, and is therefore timesaving.
				int left = left(xy);
				int right = right(xy);
				int above = above(xy);
				int below = below(xy);
				
				if (ladderMarks[left]>=ladderDepth)
					linkMembers(0,left,board,ladderMarks);
				if (ladderMarks[right]>=ladderDepth)
					linkMembers(0,right,board,ladderMarks);
				if (ladderMarks[above]>=ladderDepth)
					linkMembers(0,above,board,ladderMarks);
				if (ladderMarks[below]>=ladderDepth)
					linkMembers(0,below,board,ladderMarks);
			}
		}
	}

	/**
	 * See if a move at a certain point would be immediately captured in a
	 * ladder.
	 * 
	 * @param xy is the coordinate of where to move.
	 * @param color is the color of the move
	 * @return whether the move could be captured in a ladder.
	 */
	public int wouldBeLadder(int xy, byte color)
	{
		if (!isLegal(xy))
			return CAN_CATCH;

		// Assertion that the move that captures something will not be captured itself.
		// Not entirely accurate of course...
		byte otherColor = opposite(color);
		int left = left(xy);	
		int right = right(xy);	
		int above = above(xy);	
		int below = below(xy);	

		if (board[left]==otherColor && getNlib(left,2,otherColor)==1)
			return CANNOT_CATCH;
		if (board[right]==otherColor && getNlib(right,2,otherColor)==1)
			return CANNOT_CATCH;
		if (board[above]==otherColor && getNlib(above,2,otherColor)==1)
			return CANNOT_CATCH;
		if (board[below]==otherColor && getNlib(below,2,otherColor)==1)
			return CANNOT_CATCH;

		// Now see if it could be a ladder.
		int nLib = getNlib(xy,3,color);
		if (nLib==1)
			return CAN_CATCH;
		if (nLib>2)
			return CANNOT_CATCH;
		
		board[xy] = color;
		int result = tryLadder(xy);
		board[xy] = EMPTY;
		return result;
	}

	/**
	 * See if a move at a certain point would be immediately captured in a
	 * ladder.
	 * 
	 * @param x is the x-coordinate of where to move.
	 * @param y is the y-coordinate of where to move.
	 * @param color is the color of the move
	 * @return whether the move could be captured in a ladder.
	 */
	public int wouldBeLadder(int x, int y, byte color)
	{
		return wouldBeLadder(toXY(x,y),color);
	}

	public void changeBoard(BoardChange event)
	{
		board[event.getXY()] = event.getNewValue();		
	}
	
	/**
	 * This method does a 4-way flood-fill.
	 * 
	 * All points in the board array that are connected with the
	 * same value (most likely stone-color) get the same number set
	 * in the numbers array.
	 * 
	 * @param value to fill the numbers array with 
	 * @param startXY coordinate of the point where to start
	 * @param board
	 * @param numbers
	 */
	private final void linkMembers(int value, int startXY, byte[] board, int[] numbers)
	{
		byte color = board[startXY];
		IntStack toDoList = ArrayFactory.createIntStack();
		toDoList.push(startXY);
		do
		{
			int xy = toDoList.pop();
			numbers[xy] = value;
			int left = left(xy);
			if (numbers[left]!=value && board[left]==color)
				toDoList.push(left);
			int right = right(xy);
			if (numbers[right]!=value && board[right]==color)
				toDoList.push(right);
			int above = above(xy);
			if (numbers[above]!=value && board[above]==color)
				toDoList.push(above);
			int below = below(xy);
			if (numbers[below]!=value && board[below]==color)
				toDoList.push(below);
		}
		while (!toDoList.isEmpty());
		
		toDoList.recycle();
	}

	public void setKoPoint(int koPoint) 
	{
		this.koPoint = koPoint;
	}
	
	public String toString()
	{
		return GoArray.printBoardToString(board);
	}
}