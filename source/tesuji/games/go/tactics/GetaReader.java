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

import tesuji.games.go.common.Util;

import tesuji.games.go.util.ArrayFactory;
import tesuji.games.go.util.BoardMarker;
import tesuji.games.go.util.FourCursor;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.IntStack;
import tesuji.games.go.util.UniqueList;
import tesuji.games.model.BoardChange;
import tesuji.games.model.BoardModelListener;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;
import static tesuji.games.go.tactics.TacticsConstant.*;

/**
 * Module for reading getas, also called 3-liberty problems or loose-ladders.<br>
 * <br>
 * 
 * This module works in close connection with the ladder module to which it
 * delegates a lot of the work. Therefore this module can only be instantiated
 * together with a ladder module.
 * 
 */
public class GetaReader
implements BoardModelListener
{
	private static final boolean TRACE = false;
	
	private LadderReader ladderModule;
	private int[] getaMarks =		GoArray.createIntegers();

	// These contain the three liberties of the chain to capture.
	private int lib1;
	private int lib2;
	private int lib3;
	
	private int nrLiberties;
	private int getaDepth;
	private int maxDepth;
	private int max3Depth;

	private byte colorToMove;
	private byte getaColor;
	private byte chasingColor;
	private int lastGetaMove;
	private int getaStone;
	private boolean hasCapturedKo;
	private int nrMovesRead;
	private int nrLadderMovesRead;
	
	private IntStack	stoneList =		ArrayFactory.createIntStack();
	private UniqueList	hitList =		ArrayFactory.createUniqueList();
	private UniqueList	atariList =		ArrayFactory.createUniqueList();
	private UniqueList	ladderList =	ArrayFactory.createUniqueList();

	// These are shared with the Ladder object.
	byte[] board;
	byte[] rows;
	private IntStack libertyStack;

	// These should be 2 higher than the actual reading depth.
	private static final int MAX_DEPTH = 15;
	private static final int MAX_3LIB_DEPTH = 11;

	private BoardMarker marker = new BoardMarker();
	
	/**
	 * This module works in close connection with the ladder module to which it
	 * delegates a lot of the work. Therefore this module can only be
	 * instantiated together with a ladder module.
	 * 
	 * @param ladder module
	 */
	public GetaReader(LadderReader ladder)
	{
		this(19,ladder);
	}
	
	/**
	 * This module works in close connection with the ladder module to which it
	 * delegates a lot of the work. Therefore this module can only be
	 * instantiated together with a ladder module.
	 * 
	 * @param boardSize
	 * @param ladder module
	 */
	public GetaReader(int boardSize, LadderReader ladder)
	{
		ladderModule = ladder;
	
		// The board array is shared with the ladder module so we don't
		// waste time to communicate board-changes.
		board = ladderModule.getBoardArray();
		rows = GoArray.createRowArray(boardSize);
		libertyStack = ladderModule.getLibertyStack();
	
		// This way we don't have to check whether there are enough moves on the stack.
		stoneList.push(0);
		stoneList.push(0);
		stoneList.push(0);
	}

	/**
	 * Try to catch a stone or chain in a geta. A geta is defined as a chain
	 * with two or three liberties that can be captured by filling it's
	 * liberties or by playing a 'geta', a netting move nearby. The chain is
	 * considered to have escaped when it makes four liberties, or three
	 * liberties with the other player to move.
	 * 
	 * Possible values are CAN_CATCH, CANNOT_CATCH or CATCH_WITH_KO
	 * 
	 * @param xy is the coordinate of the chain to catch
	 * @return the result of the reading.
	 */
	public int catchGeta(int xy)
	{
		startGeta(xy, opposite(board[xy]));
		int result = readGeta();
		stopGeta();
		return result;
	}

	/**
	 * Try to catch a stone or chain in a geta. A geta is defined as a chain
	 * with two or three liberties that can be captured by filling its liberties
	 * or by playing a 'geta', a netting move nearby. The chain is considered to
	 * have escaped when it makes four liberties, or three liberties with the
	 * other player to move.
	 * 
	 * Possible values are CAN_CATCH, CANNOT_CATCH or CATCH_WITH_KO
	 * 
	 * @param x coordinate of the chain to catch
	 * @param y coordinate of the chain to catch
	 * @return the result of the reading.
	 */
	public int catchGeta(int x, int y)
	{
		return catchGeta(GoArray.toXY(x,y));
	}
	
	/**
	 * Try to escape from a geta.
	 * 
	 * @param xy is the coordinate of the chain to escape with
	 * @return the result of the reading.
	 */
	public int escapeGeta(int xy)
	{
		startGeta(xy, board[xy]);
		int result = readGeta();
		stopGeta();
		return result;
	}

	/**
	 * Try to escape from a ladder. This is a little more srtict than escapeGeta.
	 * It stops immediately as soon as the escaper makes 3 liberties.
	 * 
	 * @param xy is the coordinate of the chain to escape with
	 * @return the result of the reading.
	 */
	public int escapeLadder(int xy)
	{
		startGeta(xy, board[xy]);
		max3Depth = 0;
		int result = readGeta();
		stopGeta();
		return result;
	}

	/**
	 * Try to escape from a geta.
	 * 
	 * @param x coordinate of the chain to escape with
	 * @param y coordinate of the chain to escape with
	 * 
	 * @return is the result of the reading.
	 */
	public int escapeGeta(int x, int y)
	{
		return escapeGeta(GoArray.toXY(x,y));
	}

	/**
	 * Fill one of the possible three liberties to see if it leads to capture.
	 * 
	 * @param l1
	 * @param l2
	 * @param l3
	 * @param movesTried is the list of moves already tried so far.
	 * @param bestResult is the best result achieved so far.
	 * 
	 * @return the result of simply filling a liberty
	 */
	private int fillLiberty(int l1, int l2, int l3, UniqueList movesTried, int bestResult)
	{
		int result = CANNOT_CATCH;
		
		if ((result=tryCandidate(l1,movesTried))==CAN_CATCH)
			return CAN_CATCH;
		if (result==CATCH_WITH_KO)
			bestResult = CATCH_WITH_KO;
		if ((result=tryCandidate(l2,movesTried))==CAN_CATCH)
			return CAN_CATCH;
		if (result==CATCH_WITH_KO)
			bestResult = CATCH_WITH_KO;
		if ((result=tryCandidate(l3,movesTried))==CAN_CATCH)
			return CAN_CATCH;
		if (result==CATCH_WITH_KO)
			bestResult = CATCH_WITH_KO;

		return bestResult;
	}

	/**
	 * Searches for the liberties of the chased chain, and for adjacent chains
	 * of the chaser that are in atari. In general the effect is just to see
	 * where the new liberties are and which chains of the opponent are captured
	 * or put into atari. But in case the geta connects to another chain, a
	 * recursive call is needed.
	 * 
	 * @param xy coordinate of a stone in the geta, generally the last move.
	 */
	private void getaUpdate(int xy)
	{
		getaMarks[xy] = getaDepth;

		for (int n=3; n>=0; n--)
		{
			int neighbourXY = FourCursor.getNeighbour(xy, n);
			byte neighbour = board[neighbourXY];
			if (neighbour==chasingColor)
			{
				int nLib = getNlib(neighbourXY,3,chasingColor);
				if (nLib==0)
					removeChain(neighbourXY);
				else if (nLib==1)
					hitList.add(libertyStack.peek());
				else if (nLib==2)
				{
					atariList.add(libertyStack.peek());
					atariList.add(libertyStack.peek(1));
					ladderList.add(neighbourXY);
				}
			}
			else if (neighbour==EMPTY)
			{
				if (getaMarks[neighbourXY]==0)
				{
					if (lib1==UNDEFINED_COORDINATE || lib1==neighbourXY)
						lib1 = neighbourXY;
					else if (lib2==UNDEFINED_COORDINATE || lib2==neighbourXY)
						lib2 = neighbourXY;
					else if (lib3==UNDEFINED_COORDINATE || lib3==neighbourXY)
						lib3 = neighbourXY;
					else
						nrLiberties = 4;
				}
			}
			else if (getaMarks[neighbourXY]==0 && neighbour!=EDGE)
				getaUpdate(neighbourXY);
		}
	}

	private int getNlib(int xy, int max, byte color)
	{
//		return Util.getNlib(xy,max,color,board,libertyStack);
		return ladderModule.getNlib(xy,max,color);
	}
	
	/**
	 * Used for collecting some metrics.
	 * 
	 * @return the number of moves read by calls delegated to the ladder-module.
	 */
	public int getNrLadderMovesRead()
	{
		return nrLadderMovesRead;
	}

	/**
	 * Used for collecting some metrics.
	 * 
	 * @return the number of moves read by the geta-module.
	 */
	public int getNrMovesRead()
	{
		return nrMovesRead;
	}

	/**
	 * Check if point xy is next to the geta-chain.
	 * 
	 * @param xy
	 * @return whether it is or not.
	 */
// private boolean isDiagonalToGeta(int xy)
//	{
//		if (getaMarks[GoArray.left(GoArray.above(xy))]!=0)
//			return true;
//		if (getaMarks[GoArray.left(GoArray.below(xy))]!=0)
//			return true;
//		if (getaMarks[GoArray.right(GoArray.above(xy))]!=0)
//			return true;
//		if (getaMarks[GoArray.right(GoArray.below(xy))]!=0)
//			return true;
//		return false;
//	}

	/**
	 * Check if a move is legal.
	 * 
	 * @param xy is the coordinate of the move.
	 * @return whether the move is legal or not.
	 */
	private boolean isLegal(int xy)
	{
//		if (xy==PASS)
//			return true;
		if (board[xy]!=EMPTY)
			return false;					// Occupied.
		if (getNlib(xy,1,colorToMove)!=0)
			return true;					// Has liberty, so is legal.

		byte otherColor = opposite(colorToMove);
		int left = GoArray.left(xy);
		int right = GoArray.right(xy);
		int above = GoArray.above(xy);
		int below = GoArray.below(xy);

		// A check is made if the move captures something, in which case it's not suicide.
		if ((board[left]!=otherColor || getaMarks[left]!=0 || getNlib(left,2,otherColor)!=1)
		&& (board[right]!=otherColor || getaMarks[right]!=0 || getNlib(right,2,otherColor)!=1)
		&& (board[above]!=otherColor || getaMarks[above]!=0 || getNlib(above,2,otherColor)!=1) 
		&& (board[below]!=otherColor || getaMarks[below]!=0 || getNlib(below,2,otherColor)!=1))
			return false;

		// Check for KO.
		if (stoneList.peek(1)==-xy && stoneList.peek(2)>=0)
		{
			int lastMove = stoneList.peek();
			if (board[GoArray.left(lastMove) ]==otherColor)
				return true;
			if (board[GoArray.right(lastMove)]==otherColor)
				return true;
			if (board[GoArray.above(lastMove)]==otherColor)
				return true;
			if (board[GoArray.below(lastMove)]==otherColor)
				return true;
			return false;  // Ko
		}
		
		return true;
	}

	/**
	 * Check if point xy is next to the geta-chain.
	 * 
	 * @param xy
	 * @return whether it is or not.
	 */
// private boolean isNextToGeta(int xy)
//	{
//		if (getaMarks[GoArray.left(xy)]!=0)
//			return true;
//		if (getaMarks[GoArray.right(xy)]!=0)
//			return true;
//		if (getaMarks[GoArray.above(xy)]!=0)
//			return true;
//		if (getaMarks[GoArray.below(xy)]!=0)
//			return true;
//		return false;
//	}
	
	/**
	 * make a move and recompute all relevant information in an as efficient
	 * manner as possible. It doesn't have to be beautiful, but has to be very
	 * fast.
	 * 
	 * @param xy is the coordinate of the move
	 */
	private void makeMove(int xy)
	{
		nrMovesRead++;
//		playMove(xy,colorToMove);
		board[xy] = colorToMove;

		if (TRACE)
			GoArray.printBoard(board);
		
		if (colorToMove==getaColor && (xy==lib1 || xy==lib2 || xy==lib3))
		{
			nrLiberties=0;
			if (xy==lib1)
				lib1 = UNDEFINED_COORDINATE;
			if (xy==lib2)
				lib2 = UNDEFINED_COORDINATE;
			if (xy==lib3)
				lib3 = UNDEFINED_COORDINATE;

			if (lib1!=UNDEFINED_COORDINATE)
				getaMarks[lib1] = 1;
			if (lib2!=UNDEFINED_COORDINATE)
				getaMarks[lib2] = 1;
			if (lib3!=UNDEFINED_COORDINATE)
				getaMarks[lib3] = 1;
			
			getaUpdate(xy);
			
			if (lib1!=UNDEFINED_COORDINATE)
				getaMarks[lib1] = 0;
			if (lib2!=UNDEFINED_COORDINATE)
				getaMarks[lib2] = 0;
			if (lib3!=UNDEFINED_COORDINATE)
				getaMarks[lib3] = 0;
		}
		else
		{
			if (xy==lib1)
				lib1 = UNDEFINED_COORDINATE;
			if (xy==lib2)
				lib2 = UNDEFINED_COORDINATE;
			if (xy==lib3)
				lib3 = UNDEFINED_COORDINATE;

			byte otherColor = opposite(colorToMove);
			int left = GoArray.left(xy);
			int right = GoArray.right(xy);
			int above = GoArray.above(xy);
			int below = GoArray.below(xy);

			// See if the move captured something.
			if (board[left]==otherColor && getaMarks[left]==0 && getNlib(left,1,otherColor)==0)
				removeChain(left);
			if (board[right]==otherColor && getaMarks[right]==0 && getNlib(right,1,otherColor)==0)
				removeChain(right);
			if (board[above]==otherColor && getaMarks[above]==0 && getNlib(above,1,otherColor)==0)
				removeChain(above);
			if (board[below]==otherColor && getaMarks[below]==0 && getNlib(below,1,otherColor)==0)
				removeChain(below);

			if (colorToMove==getaColor)
			{
				// See if the escaping move put something in (pre-) atari.
				for (int n=3; n>=0; n--)
				{
					int neighbourXY = FourCursor.getNeighbour(xy, n);
					byte neighbour = board[neighbourXY];
					if (neighbour==otherColor)
					{
						int nLib = getNlib(neighbourXY,3,otherColor);
						if (nLib==1)
							hitList.add(libertyStack.peek());
						else if (nLib==2)
						{
							atariList.add(libertyStack.peek());
							atariList.add(libertyStack.peek(1));
							ladderList.add(neighbourXY);
						}
					}
				}
			}
			else
			{
				int nLib=getNlib(xy,3,colorToMove);
				
				if (nLib==1)
				{
					// The chaser put himself into atari.
					hitList.add(libertyStack.peek());
				}
				else if (nLib==2)
				{
					ladderList.add(xy);
					atariList.add(libertyStack.peek());
					atariList.add(libertyStack.peek(1));
				}
			}
		}

		stoneList.push(xy);
		getaDepth++;
		colorToMove = opposite(colorToMove);
		if (nrLiberties!=4)
		{
			nrLiberties=0;
			if (lib1!=UNDEFINED_COORDINATE)
				nrLiberties=1;
			if (lib2!=UNDEFINED_COORDINATE)
				nrLiberties++;
			if (lib3!=UNDEFINED_COORDINATE)
				nrLiberties++;
		}
	}

	/**
	 * Indirect recursive routine that reads the geta. It calls tryCandidate()
	 * to try candidate moves. If it's a feasible move, tryCandidate() will call
	 * first call makeMove() to put the move on the board and then call
	 * readGeta() again.
	 * 
	 * @return the result of reading the geta
	 */
	private int readGeta()
	{
		// Stopping conditions.
		if (nrLiberties > 3)
		{
	//		sendTextEvent("Made 4+ liberties");
			return CANNOT_CATCH;
		}
		if (nrLiberties == 3 && colorToMove == getaColor)
		{
	//		sendTextEvent("Made 3+ liberties");
			return CANNOT_CATCH;
		}
		if (nrLiberties == 3 && getaDepth > max3Depth)
		{
	//		if (eventSupport.hasListeners())
	//			sendTextEvent("Too deep for 3 liberties\n");
			return CANNOT_CATCH;
		}
		if (getaDepth > maxDepth)
		{
	//		if (eventSupport.hasListeners())
	//			sendTextEvent("Too deep\n");
			return CANNOT_CATCH;
		}
		
		// Copy the three liberties into the local variables l1, l2 and l3;
		int l1 = lib1;
		int l2 = lib2;
		int l3 = lib3;
		// These will get the number of liberties the chain will get if he played there.
		int iGNL1 = -1;
		int iGNL2 = -1;
		int iGNL3 = -1;
		int iGN;
	
		int result;
		int bestResult;
	
		// This list will contain all the moves tried so far.
		// Each move will be checked against this list to
		// prevent the same move to be tried more than once.
		UniqueList movesTried = ArrayFactory.createUniqueList();
	
		try
		{	
			if (colorToMove == getaColor)
			{
				// Compute the number of liberties a move at each liberty would make.
				if (l1 != UNDEFINED_COORDINATE)
					iGNL1 = getNlib(l1, 4, getaColor);
				if (l2 != UNDEFINED_COORDINATE)
					iGNL2 = getNlib(l2, 4, getaColor);
				if (l3 != UNDEFINED_COORDINATE)
					iGNL3 = getNlib(l3, 4, getaColor);
	
				// If there's a move that makes at least 4 liberties, it can't be caught.
				if (iGNL1 == 4)
				{
					lastGetaMove = l1;
	//				sendTextEvent("Can make 4 liberties");
					return CANNOT_CATCH;
				}
				if (iGNL2 == 4)
				{
					lastGetaMove = l2;
	//				sendTextEvent("Can make 4 liberties");
					return CANNOT_CATCH;
				}
				if (iGNL3 == 4)
				{
					lastGetaMove = l3;
	//				sendTextEvent("Can make 4 liberties");
					return CANNOT_CATCH;
				}
	
				result = bestResult = CAN_CATCH;
	
				// First try to capture any surrounding chain.
				for (int i=hitList.getSize()-1; i>=0; i--)
				{
					int xy = hitList.get(i);
	
					if ((result = tryCandidate(xy, movesTried)) == CANNOT_CATCH)
						return CANNOT_CATCH;
					if (result == CATCH_WITH_KO)
						bestResult = result;
				}
	
				if (nrLiberties == 1)
				{
					// Try a snap-back. This is the only case where the chain to
					// catch can remain in atari with the opponent to move.
					if (board[GoArray.left(getaStone)] != colorToMove
						&& board[GoArray.right(getaStone)] != colorToMove
						&& board[GoArray.above(getaStone)] != colorToMove
						&& board[GoArray.below(getaStone)] != colorToMove)
					{
						for (int i=ladderList.getSize()-1; i>=0; i--)
						{
							int xy = ladderList.get(i);
							if (board[GoArray.left(xy)] == chasingColor
								|| board[GoArray.right(xy)] == chasingColor
								|| board[GoArray.above(xy)] == chasingColor
								|| board[GoArray.below(xy)] == chasingColor)
							{
								if (board[xy] == chasingColor)
								{
									int ladderResult = ladderModule.tryLadder(xy);
	//								int nrMoves = ladderModule.getNrMovesRead();
	//								if (eventSupport.hasListeners())
	//									sendTextEvent("Ladder ("+TACTICS[ladderResult]+") at "+xy+" took "+nrMoves+" moves\n");
	//								nrLadderMovesRead += nrMoves;
									if (ladderResult != CANNOT_CATCH)
									{
										lastGetaMove = ladderModule.getLastLadderMove();
	//									sendTextEvent("Can make snap-back");
										return CANNOT_CATCH;
									}
								}
							}
						}
					}
				}
				else // if (nrLiberties>1)
				{
					// Try to capture a surrounding chain in a ladder.
					for (int i=ladderList.getSize()-1; i>=0 ; i--)
					{
						int xy = ladderList.get(i);
						if (board[xy] == chasingColor)
						{
							int ladderResult = ladderModule.tryLadder(xy);
	//						int nrMoves = ladderModule.getNrMovesRead();
	//						if (eventSupport.hasListeners())
	//							sendTextEvent("Ladder ("+TACTICS[ladderResult]+") at "+xy+" took "+nrMoves+" moves\n");
	//						nrLadderMovesRead += nrMoves;
							if (ladderResult != CANNOT_CATCH)
							{
								int ladderXY = ladderModule.getLastLadderMove();
								if ((result = tryCandidate(ladderXY, movesTried)) == CANNOT_CATCH)
									return CANNOT_CATCH;
								if (result == CATCH_WITH_KO)
									bestResult = result;
								lastGetaMove = xy;
							}
						}
					}
	
					// If that didn't work, try escaping by putting a surrounding chain in atari.
					for (int i=atariList.getSize()-1; i>=0; i--)
					{
						int xy = atariList.get(i);
		//					if (getRlib(xy,2,colorToMove)>1) need to prevent useless auto-ataris to save time.
						{
							maxDepth++; // Don't count the depth for forcing moves
							max3Depth+=2;
							if ((result = tryCandidate(xy, movesTried)) == CANNOT_CATCH)
							{
								maxDepth--;
								max3Depth-=2;
								return CANNOT_CATCH;
							}
							if (result == CATCH_WITH_KO)
								bestResult = result;
							lastGetaMove = xy;
							maxDepth--;
							max3Depth-=2;
						}
					}
				}
	
				// Some rudimentary move sorting.
				if (iGNL3 > iGNL2)
				{
					int tmp = iGNL2;
					iGNL2 = iGNL3;
					iGNL3 = tmp;
					tmp = l2;
					l2 = l3;
					l3 = tmp;
				}
				if (iGNL2 > iGNL1)
				{
					int tmp = iGNL1;
					iGNL1 = iGNL2;
					iGNL2 = tmp;
					tmp = l1;
					l1 = l2;
					l2 = tmp;
				}
				if (iGNL3 > iGNL2)
				{
					int tmp = iGNL2;
					iGNL2 = iGNL3;
					iGNL3 = tmp;
					tmp = l2;
					l2 = l3;
					l3 = tmp;
				}
				if (nrLiberties == 2)
				{
					//if (getaDepth < MAX_3LIB_DEPTH) // Should reconsider this depth?
					{
						IntStack tmpList1 = ArrayFactory.createIntStack(); // List with moves next to chain to which can be connected.
						IntStack tmpList2 = ArrayFactory.createIntStack(); // List with stones to which can be connected.
						IntStack tmpList3 = ArrayFactory.createIntStack(); // List with moves that capture an opponents chain next to a chain to which can be connected.
						boolean b1 = false;
						boolean b2 = false;
	
						marker.getNewMarker();
						
						try
						{
							// Try to play a little further away, if the chaser can't cut it immediately anyway.
							// If there are chains to which the chased stones can connect and that connection
							// cannot be prevented by the opponent because of auto-atari, try playing at one
							// of the liberties of those neighbouring chains, or try capturing opponent chains
							// that are in atari and adjacent to those neighbouring chains.
							if (getNlib(l1, 2, chasingColor) <= 1)
							{
								b1 = true;
								for (int i=getNlib(l1, iGNL1, getaColor); --i>=0;)
									tmpList1.push(libertyStack.get(i));
							}
							if (getNlib(l2, 2, chasingColor) <= 1)
							{
								b2 = true;
								for (int i=getNlib(l2, iGNL2, getaColor); --i>=0;)
									tmpList1.push(libertyStack.get(i));
							}
	
							if (b1 || b2)
							{
								if (b1)
								{
									for (int n=3; n>=0 ; n--)
									{
										int adjacentXY = FourCursor.getNeighbour(l1, n);
										if (board[adjacentXY] == getaColor && getaMarks[adjacentXY] == 0)
											tmpList2.getMembers(adjacentXY, board, marker);
									}
								}
								if (b2)
								{
									for (int n=3; n>=0 ; n--)
									{
										int adjacentXY = FourCursor.getNeighbour(l2, n);
										if (board[adjacentXY] == getaColor && getaMarks[adjacentXY] == 0)
											tmpList2.getMembers(adjacentXY, board, marker);
									}
								}
								for (int i=tmpList2.getSize(); --i>=0;)
								{
									int nearStone = tmpList2.get(i);
									for (int n=3; n>=0 ; n--)
									{
										int oppStone = FourCursor.getNeighbour(nearStone, n);
										if (board[oppStone] == chasingColor && getNlib(oppStone, 2, chasingColor) == 1)
											tmpList3.push(libertyStack.peek());
									}
								}
							}
							
							for (int i=tmpList3.getSize(); --i>=0;)
							{
								int xy = tmpList3.get(i);
								if (!movesTried.hasMember(xy)
									&& (result = tryCandidate(xy, movesTried)) == CANNOT_CATCH)
										return CANNOT_CATCH;
								if (result == CATCH_WITH_KO)
									bestResult = result;
							}
	
							for (int i=tmpList1.getSize(); --i>=0;)
							{
								int xy = tmpList1.get(i);
								if (!movesTried.hasMember(xy) && getNlib(xy, 4, getaColor) > 3
									&& (result = tryCandidate(xy, movesTried)) == CANNOT_CATCH)
										return CANNOT_CATCH;
								if (result == CATCH_WITH_KO)
									bestResult = result;
							}
						}
						finally
						{
							tmpList1.recycle();
							tmpList2.recycle();
							tmpList3.recycle();
						}
					} // fi getaDepth<4
	
					// Try not to escape with empty triangle. Try a move next to it instead.
					if (Util.isEmptyTriangle(l1, getaColor, board))
					{
						for (int n=3; n>=0 ; n--)
						{
							int neighbourXY = FourCursor.getNeighbour(l1, n);
							if (board[neighbourXY] == EMPTY //&& rows[neighbourXY]>1
								&& (result = tryCandidate(neighbourXY, movesTried)) == CANNOT_CATCH)
									return CANNOT_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
						}
						if (rows[l1] == 1)
						{
							if ((board[GoArray.left(l1)] == EMPTY && board[GoArray.right(l1)] == EMPTY)
							|| (board[GoArray.above(l1)] == EMPTY && board[GoArray.below(l1)] == EMPTY))
								movesTried.add(l1);
						}
					}
					if (Util.isEmptyTriangle(l2, getaColor, board))
					{
						for (int n=3; n>=0 ; n--)
						{
							int neighbourXY = FourCursor.getNeighbour(l2, n);
							if (board[neighbourXY] == EMPTY //&& rows[neighbourXY]>1
								&& (result = tryCandidate(neighbourXY, movesTried)) == CANNOT_CATCH)
									return CANNOT_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
						}
						if (rows[l2] == 1)
						{
							if ((board[GoArray.left(l2)] == EMPTY && board[GoArray.right(l2)] == EMPTY)
							|| (board[GoArray.above(l2)] == EMPTY && board[GoArray.below(l2)] == EMPTY))
								movesTried.add(l2);
						}
					}
				} // fi nrLiberties==2
	
				// Now try playing at one of its actual liberties.
				if ((result = tryCandidate(l1, movesTried)) == CANNOT_CATCH)
					return CANNOT_CATCH;
				if (result == CATCH_WITH_KO)
					bestResult = result;
				if ((result = tryCandidate(l2, movesTried)) == CANNOT_CATCH)
					return CANNOT_CATCH;
				if (result == CATCH_WITH_KO)
					bestResult = result;
				if ((result = tryCandidate(l3, movesTried)) == CANNOT_CATCH)
					return CANNOT_CATCH;
				if (result == CATCH_WITH_KO)
					bestResult = result;
	
				// Now try playing at a point next to one of the liberties.
				// These may have been tried before if they made an empty triangle.
				// This is for ordering purposes.
				if (nrLiberties == 2)
				{
					for (int n=3; n>=0; n--)
					{
						int neighbourXY = FourCursor.getNeighbour(l1, n);
						if (board[neighbourXY] == EMPTY && (result = tryCandidate(neighbourXY, movesTried)) == CANNOT_CATCH)
							return CANNOT_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
					}
					for (int n=3; n>=0; n--)
					{
						int neighbourXY = FourCursor.getNeighbour(l2, n);
						if (board[neighbourXY] == EMPTY && (result = tryCandidate(neighbourXY, movesTried)) == CANNOT_CATCH)
							return CANNOT_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
					}
	
					// Try to put him into damezumari.
					if ((iGN = getNlib(l1, 3, chasingColor)) == 2)
					{
						int xy1 = libertyStack.peek();
						int xy2 = libertyStack.peek(1);
						// There was something about auto-ataris here I didn't understand anymore...
						if (getNlib(xy1, 2, getaColor) != 1 && (result = tryCandidate(xy1, movesTried)) == CANNOT_CATCH)
							return CANNOT_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
						if (getNlib(xy2, 2, getaColor) != 1 && (result = tryCandidate(xy2, movesTried)) == CANNOT_CATCH)
							return CANNOT_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
					}
					else if (iGN == 1)
					{
						int xy1 = libertyStack.peek();
						if (getNlib(xy1, 2, getaColor) != 1 && (result = tryCandidate(xy1, movesTried)) == CANNOT_CATCH)
							return CANNOT_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
					}
					if ((iGN = getNlib(l2, 3, chasingColor)) == 2)
					{
						int xy1 = libertyStack.peek();
						int xy2 = libertyStack.peek(1);
						// There was something about auto-ataris here I didn't understand anymore...
						if (getNlib(xy1, 2, getaColor) != 1 && (result = tryCandidate(xy1, movesTried)) == CANNOT_CATCH)
							return CANNOT_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
						if (getNlib(xy2, 2, getaColor) != 1 && (result = tryCandidate(xy2, movesTried)) == CANNOT_CATCH)
							return CANNOT_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
					}
					else if (iGN == 1)
					{
						int xy1 = libertyStack.peek();
						if (getNlib(xy1, 2, getaColor) != 1 && (result = tryCandidate(xy1, movesTried)) == CANNOT_CATCH)
								return CANNOT_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
					}
	
					// Last measure, try a pass.
					result = ladderModule.tryLadder(getaStone);
	//				int nrMoves = ladderModule.getNrMovesRead();
	//				if (eventSupport.hasListeners())
	//					sendTextEvent("Pass resulted in ladder ("+TACTICS[result]+") at "+getaStone+" took "+nrMoves+" moves\n");
	//				nrLadderMovesRead += nrMoves;
					return result;
				}
			} // fi colorToMove==getaColor
			else
			{
				result = CANNOT_CATCH;
				bestResult = CANNOT_CATCH;
	
				if (nrLiberties == 2)
				{
					// May want to skip single stones, since it certainly isn't a ladder then...
					result = ladderModule.tryLadder(getaStone);
	//				int nrMoves = ladderModule.getNrMovesRead();
	//				if (eventSupport.hasListeners())
	//					sendTextEvent("Ladder ("+TACTICS[result]+") at "+getaStone+" took "+nrMoves+" moves\n");
	//				nrLadderMovesRead += nrMoves;
					if (result == CAN_CATCH)
					{
						lastGetaMove = ladderModule.getLastLadderMove();
	//					sendTextEvent("Ladder");
						return CAN_CATCH;
					}
					if (result == CATCH_WITH_KO)
						bestResult = result;
	
					for (int i=hitList.getSize()-1; i>=0; i--)
					{
						int xy  = hitList.get(i);
						/*
						result = ladderModule.wouldBeLadder(xy,colorToMove);
						nrMoves = ladderModule.getNrMovesRead();
						sendTextEvent("Would-be-ladder ("+result+") at "+xy+" took "+nrMoves+" moves");
						nrLadderMovesRead += nrMoves;
						if (result==CANNOT_CATCH)
						{
							if ((result = tryCandidate(xy, movesTried)) == CAN_CATCH)
								return CAN_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
						}
						else
						*/
						{
							for (int n=3; n>=0; n--)
							{
								int escapingStone = FourCursor.getNeighbour(xy, n);
								if (board[escapingStone]==colorToMove && getNlib(escapingStone,2,colorToMove)==1)
								{
									int escapeResult = ladderModule.tryEscape(escapingStone);
	//								nrMoves = ladderModule.getNrMovesRead();
	//								if (eventSupport.hasListeners())
	//									sendTextEvent("Escape ("+TACTICS[result]+") at "+escapingStone+" took "+nrMoves+" moves\n");
	//								nrLadderMovesRead += nrMoves;
	
									// Generally we don't want to sacrifice more than one stone.
									if (escapeResult==CANNOT_CATCH)
									{
										int escapingMove = ladderModule.getLastLadderMove();
										if ((result = tryCandidate(escapingMove, movesTried)) == CAN_CATCH)
											return CAN_CATCH;
										if (result == CATCH_WITH_KO)
											bestResult = result;
									}
									else // except when trying to sacrifice two stones at the 1st line.
									{
										if (rows[xy]==1 && rows[escapingStone]==2
											&& board[GoArray.left(escapingStone)]!=colorToMove
											&& board[GoArray.right(escapingStone)]!=colorToMove
											&& board[GoArray.above(escapingStone)]!=colorToMove
											&& board[GoArray.below(escapingStone)]!=colorToMove)
										{
											// We may need some more restrictions...
											if ((result = tryCandidate(xy, movesTried)) == CAN_CATCH)
												return CAN_CATCH;
											if (result == CATCH_WITH_KO)
												bestResult = result;
											
										}
									}
								}
							}
						}
					}
				}
				if (l1 != UNDEFINED_COORDINATE)
					iGNL1 = getNlib(l1, 6, getaColor);
				if (l2 != UNDEFINED_COORDINATE)
					iGNL2 = getNlib(l2, 6, getaColor);
				if (l3 != UNDEFINED_COORDINATE)
					iGNL3 = getNlib(l3, 6, getaColor);
	
				// A few straightforward heuristics:
				// - If there's a move that would give more than 4 liberties,
				// then we cannot allow the opponent to play there.
				// - If can't play there because it's illegal or there are
				// two moves that make more than 4 liberties, then the chain escapes.
				if (iGNL1 > 4)
				{
					if (iGNL2 > 4 || iGNL3 > 4)
					{
	//					sendTextEvent("2 moves that make 5+ liberties");
						return bestResult;
					}
					l2 = l3 = UNDEFINED_COORDINATE;
				}
				if (iGNL2 > 4)
				{
					if (iGNL1 > 4 || iGNL3 > 4)
					{
	//					sendTextEvent("2 moves that make 5+ liberties");
						return bestResult;
					}
					l1 = l3 = UNDEFINED_COORDINATE;
				}
				if (iGNL3 > 4)
				{
					if (iGNL1 > 4 || iGNL2 > 4)
					{
	//					sendTextEvent("2 moves that make 5+ liberties");
						return bestResult;
					}
					l1 = l2 = UNDEFINED_COORDINATE;
				}
				
				if (iGNL1 > 4 && !isLegal(l1))
				{
	//				sendTextEvent("Protected move that make 5+ liberties");
					return bestResult;
				}
				if (iGNL2 > 4 && !isLegal(l2))
				{
	//				sendTextEvent("Protected move that make 5+ liberties");
					return bestResult;
				}
				if (iGNL3 > 4 && !isLegal(l3))
				{
	//				sendTextEvent("Protected move that make 5+ liberties");
					return bestResult;
				}
	
				// Some rudimentary move sorting.
				if (iGNL3 > iGNL2 || (iGNL3 == iGNL2 && l3!=UNDEFINED_COORDINATE && rows[l3] > rows[l2]))
				{
					int tmp = iGNL2; iGNL2 = iGNL3; iGNL3 = tmp;
					tmp = l2; l2 = l3; l3 = tmp;
				}
				if (iGNL2 > iGNL1 || (iGNL2 == iGNL1 && l2!=UNDEFINED_COORDINATE && rows[l2] > rows[l1]))
				{
					int tmp = iGNL1; iGNL1 = iGNL2;
					iGNL2 = tmp; tmp = l1;
					l1 = l2; l2 = tmp;
				}
				if (iGNL3 > iGNL2 || (iGNL3 == iGNL2 && l3!=UNDEFINED_COORDINATE && rows[l3] > rows[l2]))
				{
					int tmp = iGNL2; iGNL2 = iGNL3; iGNL3 = tmp;
					tmp = l2; l2 = l3; l3 = tmp;
				}
	
				if (nrLiberties == 2)
				{
					if (!GoArray.isNeighbour(l1, l2))
					{
						// Try a simple geta, we already know a simple ladder doesn't work.
						if (GoArray.left(GoArray.above(l1)) == l2 && (result = tryCandidate(GoArray.above(l1), movesTried)) == CAN_CATCH)
							return CAN_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
						if (GoArray.left(GoArray.above(l2)) == l1 && (result = tryCandidate(GoArray.above(l2), movesTried)) == CAN_CATCH)
							return CAN_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
						if (GoArray.left(GoArray.below(l1)) == l2 && (result = tryCandidate(GoArray.below(l1), movesTried)) == CAN_CATCH)
							return CAN_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
						if (GoArray.left(GoArray.below(l2)) == l1 && (result = tryCandidate(GoArray.below(l2), movesTried)) == CAN_CATCH)
							return CAN_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
						if (GoArray.right(GoArray.above(l1)) == l2 && (result = tryCandidate(GoArray.above(l1), movesTried)) == CAN_CATCH)
							return CAN_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
						if (GoArray.right(GoArray.above(l2)) == l1 && (result = tryCandidate(GoArray.above(l2), movesTried)) == CAN_CATCH)
							return CAN_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
						if (GoArray.right(GoArray.below(l1)) == l2 && (result = tryCandidate(GoArray.below(l1), movesTried)) == CAN_CATCH)
							return CAN_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
						if (GoArray.right(GoArray.below(l2)) == l1 && (result = tryCandidate(GoArray.below(l2), movesTried)) == CAN_CATCH)
							return CAN_CATCH;
						if (result == CATCH_WITH_KO)
							bestResult = result;
	
						// Try an approach-move, provided one wasn't tried yet before.
						int approachMove1 = UNDEFINED_COORDINATE;
						int approachMove2 = UNDEFINED_COORDINATE;
						int makesLibs1 = -1;
						int makesLibs2 = -1;
	
						if (l1!=UNDEFINED_COORDINATE)
						{
							makesLibs1 = getNlib(l1,2,colorToMove);
							if (makesLibs1>1)
								return fillLiberty(l1,l2,l3,movesTried,bestResult);
							else if (makesLibs1==1)
								approachMove1 = libertyStack.peek();
						}
						if (l2!=UNDEFINED_COORDINATE)
						{
							makesLibs2 = getNlib(l2,2,colorToMove);
							if (makesLibs2>1)
								return fillLiberty(l1,l2,l3,movesTried,bestResult);
							else if (makesLibs2==1)
								approachMove2 = libertyStack.peek();
						}
	
						if (l1!=UNDEFINED_COORDINATE && (makesLibs1==0 || getNlib(approachMove1,2,colorToMove)==1))
						{
							// Try an approach-move by capturing another chain than the one you're chasing.
							for (int n=3; n>=0; n--)
							{
								int adjacentXY = FourCursor.getNeighbour(l1, n);
								if (board[adjacentXY]==colorToMove)
								{
									IntStack chasingStones = ArrayFactory.createIntStack();
									marker.getNewMarker();
									chasingStones.getMembers(adjacentXY,board,marker);
									for (int j=chasingStones.getSize()-1; j>=0; j--)
									{
										int stoneXY = chasingStones.get(j);
										for (int k=3; k>=0; k--)
										{
											int captureStone = FourCursor.getNeighbour(stoneXY, k);
											if (board[captureStone]==getaColor
												&& getNlib(captureStone,2,getaColor)==1)
											{
												if ((result = tryCandidate(libertyStack.peek(), movesTried)) == CAN_CATCH)
												{
													chasingStones.recycle();
													return CAN_CATCH;
												}
												if (result == CATCH_WITH_KO)
													bestResult = result;
											}
										}
									}
									chasingStones.recycle();
								}
							}
						}
						else if (makesLibs1>0
							&& (board[GoArray.left(approachMove1)]==colorToMove
							|| board[GoArray.right(approachMove1)]==colorToMove
							|| board[GoArray.above(approachMove1)]==colorToMove
							|| board[GoArray.below(approachMove1)]==colorToMove))
						{
							if ((result = tryCandidate(approachMove1, movesTried)) == CAN_CATCH)
								return CAN_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
						}
						
						if (l2!=UNDEFINED_COORDINATE && (makesLibs2==0 || getNlib(approachMove2,2,colorToMove)==1))
						{
							// Try an approach-move by capturing another chain than the one you're chasing.
							for (int n=3; n>=0; n--)
							{
								int adjacentXY = FourCursor.getNeighbour(l2, n);
								if (board[adjacentXY]==colorToMove)
								{
									IntStack chasingStones = ArrayFactory.createIntStack();
									marker.getNewMarker();
									chasingStones.getMembers(adjacentXY,board,marker);
									for (int j=chasingStones.getSize()-1; j>=0; j--)
									{
										int stoneXY = chasingStones.get(j);
										for (int k=3; k>=0; k--)
										{
											int captureStone = FourCursor.getNeighbour(stoneXY, k);
											if (board[captureStone]==getaColor
												&& getNlib(captureStone,2,getaColor)==1)
											{
												if ((result = tryCandidate(libertyStack.peek(), movesTried)) == CAN_CATCH)
												{
													chasingStones.recycle();
													return CAN_CATCH;
												}
												if (result == CATCH_WITH_KO)
													bestResult = result;
											}
										}
									}
									chasingStones.recycle();
								}
							}
						}
						else if (makesLibs2>0
							&& (board[GoArray.left(approachMove2)]==colorToMove
							|| board[GoArray.right(approachMove2)]==colorToMove
							|| board[GoArray.above(approachMove2)]==colorToMove
							|| board[GoArray.below(approachMove2)]==colorToMove))
						{
							if ((result = tryCandidate(approachMove2, movesTried)) == CAN_CATCH)
								return CAN_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
						}
					}// fi isNeighbours(l1,l2)
					else
					{
						if (l1!=UNDEFINED_COORDINATE)
						{
							int left = GoArray.left(l1);
							int right = GoArray.right(l1);
							int above = GoArray.above(l1);
							int below = GoArray.below(l1);
							
							if (getaMarks[left]!=0 && (result = tryCandidate(right, movesTried)) == CAN_CATCH)
								return CAN_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
							if (getaMarks[right]!=0 && (result = tryCandidate(left, movesTried)) == CAN_CATCH)
								return CAN_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
							if (getaMarks[above]!=0 && (result = tryCandidate(below, movesTried)) == CAN_CATCH)
								return CAN_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
							if (getaMarks[below]!=0 && (result = tryCandidate(above, movesTried)) == CAN_CATCH)
								return CAN_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
						}
						if (l2!=UNDEFINED_COORDINATE)
						{
							int left = GoArray.left(l2);
							int right = GoArray.right(l2);
							int above = GoArray.above(l2);
							int below = GoArray.below(l2);
							
							if (getaMarks[left]!=0 && (result = tryCandidate(right, movesTried)) == CAN_CATCH)
								return CAN_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
							if (getaMarks[right]!=0 && (result = tryCandidate(left, movesTried)) == CAN_CATCH)
								return CAN_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
							if (getaMarks[above]!=0 && (result = tryCandidate(below, movesTried)) == CAN_CATCH)
								return CAN_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
							if (getaMarks[below]!=0 && (result = tryCandidate(above, movesTried)) == CAN_CATCH)
								return CAN_CATCH;
							if (result == CATCH_WITH_KO)
								bestResult = result;
						}
					}
				}
				return fillLiberty(l1,l2,l3,movesTried,bestResult);
			}
			return bestResult;
		}
		finally
		{
			movesTried.recycle();
		}
	}
	
	/**
	 * Remove a chain that was captured while reading the geta.
	 * 
	 * @param xy is the coordinate of the stone to remove.
	 */
	private void removeChain(int xy)
	{
		int left = GoArray.left(xy);
		int right = GoArray.right(xy);
		int above = GoArray.above(xy);
		int below = GoArray.below(xy);
		
		if (colorToMove==getaColor)
		{
			if (getaMarks[left]!=0
			 || getaMarks[right]!=0
			 || getaMarks[above]!=0
			 || getaMarks[below]!=0)
			{
				if (lib1==UNDEFINED_COORDINATE)
					lib1 = xy;
				else if (lib2==UNDEFINED_COORDINATE)
					lib2 = xy;
				else if (lib3==UNDEFINED_COORDINATE)
					lib3 = xy;
				else
					nrLiberties = 4;
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
	 	Register an event listener. Any change of the board during
	 	reading gets sent here which is good for debugging or for
	 	flashing the ladders on the screen.
	
	 	@param listener
	*/
//	public void addGameEventListener(GameEventListener listener)
//	{
//		eventSupport.addGameEventListener(listener);
//	}
	
	/**
	 * Remove a listener
	 * 
	 * @param listener
	 */
//	public void removeGameEventListener(GameEventListener listener)
//	{
//		eventSupport.removeGameEventListener(listener);
//	}

//	private void playMove(int xy, byte color)
//	{
//		eventSupport.playMove(xy,color);
//	}
//
//	private void takeBack()
//	{
//		eventSupport.takeBack();
//	}
//	
//	private final void sendTextEvent(String text)
//	{
//		eventSupport.sendTextEvent(text);
//	}

	/**
	 * Initialise some stuff for starting a geta.
	 * 
	 * @param xy is the coordinate of the chain to capture.
	 * @param color is the color of the player to move first.
	 */
	private void startGeta(int xy, byte color)
	{
		board = ladderModule.getBoardArray();
		nrMovesRead = 0;
		nrLadderMovesRead = 0;
		lib1 = UNDEFINED_COORDINATE;
		lib2 = UNDEFINED_COORDINATE;
		lib3 = UNDEFINED_COORDINATE;
		nrLiberties = 0;
		colorToMove = color;
		getaColor = board[xy];
		chasingColor = opposite(getaColor);
		getaDepth = 1;
		lastGetaMove = UNDEFINED_COORDINATE;
		getaStone = xy;
		hasCapturedKo = false;

		hitList.clear();
		atariList.clear();
		ladderList.clear();

		getaUpdate(xy);

		getaDepth = 2;
		if (nrLiberties!=4)
		{
			nrLiberties = 0;
			if (lib1!=UNDEFINED_COORDINATE)
				nrLiberties++;
			if (lib2!=UNDEFINED_COORDINATE)
				nrLiberties++;
			if (lib3!=UNDEFINED_COORDINATE)
				nrLiberties++;
		}
		maxDepth = MAX_DEPTH;
		max3Depth = MAX_3LIB_DEPTH;
	}

	/**
	 * After reading a geta make sure we can read the next one.
	 */
	private void stopGeta()
	{
		GoArray.link(0,getaStone,board,getaMarks);
	}

	private int tryCandidate(int xy, UniqueList triedMoves)
	{
		if (xy==UNDEFINED_COORDINATE)
			return ILLEGAL;

		if (triedMoves.hasMember(xy))
			return ILLEGAL;
		if (!isLegal(xy))
			return ILLEGAL;

		int hitListSize = hitList.getSize();
		int ladderListSize = ladderList.getSize();
		int atariListSize = atariList.getSize();
		int result = ILLEGAL;

		int l1 = lib1;
		int l2 = lib2;
		int l3 = lib3;
		boolean koCapture = hasCapturedKo;
		int oldLibs = nrLiberties;
		
		triedMoves.add(xy);

		// Check for ko
		if (!hasCapturedKo && getaDepth<8) // May want to increase value 8.
		{
			byte otherColor = opposite(colorToMove);
			int left = GoArray.left(xy);
			int right = GoArray.right(xy);
			int above = GoArray.above(xy);
			int below = GoArray.below(xy);

			// Check if the move is the capture of a ko.
			// Capturing a ko is only allowed as the first move.  ?!?
			if ((board[left]==getaColor || board[left]==EDGE)
			&& (board[right]==getaColor || board[right]==EDGE)
			&& (board[above]==getaColor || board[above]==EDGE)
			&& (board[below]==getaColor || board[below]==EDGE))
			{
				int count=0;

				for (int n=3; n>=0; n--)
				{
					int adjacentXY = FourCursor.getNeighbour(xy, n);
					if (rows[adjacentXY]!=0 && getNlib(adjacentXY,2,otherColor)==1)
					{
						count++;
						if (board[GoArray.left(adjacentXY)]==otherColor)
							{ count++; break; }
						if (board[GoArray.right(adjacentXY)]==otherColor)
							{ count++; break; }
						if (board[GoArray.above(adjacentXY)]==otherColor)
							{ count++; break; }
						if (board[GoArray.below(adjacentXY)]==otherColor)
							{ count++; break; }
					}
				}
				
				if (count==1)
					hasCapturedKo=true;
			}
		}

		if (colorToMove==getaColor)
		{
			makeMove(xy);
			if (nrLiberties>1 && (result=readGeta())!=CAN_CATCH)
			{
				if (hasCapturedKo && !koCapture)
					result = CATCH_WITH_KO;
				lastGetaMove = xy;
			}
			unmakeMove();
			
			// Try the opponents successful move.
			if (result==CAN_CATCH && getaDepth<6 && lastGetaMove!=UNDEFINED_COORDINATE
			&& lastGetaMove!=l1 && lastGetaMove!=l2 && lastGetaMove!=l3)
			{
				xy = lastGetaMove;

				if (isLegal(xy) && !triedMoves.hasMember(xy))
				{
					lib1 = l1;
					lib2 = l2;
					lib3 = l3;
					nrLiberties = oldLibs;
					hasCapturedKo = koCapture;
					makeMove(xy);
					if (nrLiberties>1 && (result=readGeta())==CANNOT_CATCH)
						lastGetaMove = xy;
					unmakeMove();
					triedMoves.add(xy);
				}
			}
		}
		else 
		{
			makeMove(xy);

			boolean firstRowFailure = false;
			boolean approachFailure = false;

			/*
			int size = ladderList.getSize();
			if (rows[xy]==1 && size!=0 && ladderList.get(size-1)==xy)
			{
				int ladderResult = ladderModule.tryLadder(xy);
				if (ladderResult==CAN_CATCH)
					firstRowFailure = true;
				int nrMoves = ladderModule.getNrMovesRead();
//				sendTextEvent("Ladder ("+ladderResult+") at "+xy+" took "+nrMoves+" moves");
				nrLadderMovesRead += nrMoves;
			}
			*/
			
			if (!firstRowFailure && nrLiberties==2 /*&& xy!=l1 && xy!=l2 && xy!=l3*/)
			{
				int ladderResult = ladderModule.tryLadder(getaStone);
				if (ladderResult==CANNOT_CATCH)
				{
//					sendTextEvent("Approach move didn't work\n");
					approachFailure = true;
				}
//				int nrMoves = ladderModule.getNrMovesRead();
//				if (eventSupport.hasListeners())
//					sendTextEvent("Ladder ("+TACTICS[ladderResult]+") at "+getaStone+" took "+nrMoves+" moves\n");
//				nrLadderMovesRead += nrMoves;
			}

			if (firstRowFailure || approachFailure)
			{
//				sendTextEvent("Failed\n");
				result = CANNOT_CATCH;
			}
			else if ((result=readGeta())==CAN_CATCH)
			{
				if (hasCapturedKo && !koCapture)
					result = CATCH_WITH_KO;
				lastGetaMove = xy;
			}					
			unmakeMove();
		}

		hitList.setSize(hitListSize);
		ladderList.setSize(ladderListSize);
		atariList.setSize(atariListSize);
		lib1 = l1;
		lib2 = l2;
		lib3 = l3;
		hasCapturedKo = koCapture;
		nrLiberties = oldLibs;	
		return result;
	}

	/**
	 * Unmake the last move and recompute all relevant information in an as
	 * efficient manner as possible. It doesn't have to be beautiful, but has to
	 * be very fast.
	 */
	private void unmakeMove()
	{
		getaDepth--;
		int xy = stoneList.pop();
//		takeBack();
		
		// Pop the prisoners.
		while (stoneList.peek()<0)
		{
			int captive = -stoneList.pop();
			board[captive]= colorToMove;
		}
		
		board[xy]=EMPTY;
		colorToMove= opposite(colorToMove);
		if (colorToMove==getaColor)
		{
			if (getaMarks[xy]!=0)
			{
				getaMarks[xy]=0;
				// Clear the marks in getaMarks[] in the stones that are not part of the geta anymore.
				// The comparison to getaDepth ensures that the points that still belong to the
				// geta are not erased, and is therefore timesaving.
				int left = GoArray.left(xy);
				int right = GoArray.right(xy);
				int above = GoArray.above(xy);
				int below = GoArray.below(xy);
				
				if (getaMarks[left]>=getaDepth)
					GoArray.link(0,left,board,getaMarks);
				if (getaMarks[right]>=getaDepth)
					GoArray.link(0,right,board,getaMarks);
				if (getaMarks[above]>=getaDepth)
					GoArray.link(0,above,board,getaMarks);
				if (getaMarks[below]>=getaDepth)
					GoArray.link(0,below,board,getaMarks);
			}
		}
	}

	public void changeBoard(BoardChange event)
	{
		board[event.getXY()] = event.getNewValue();		
	}
	
	public int getLastGetaMove()
	{
		return lastGetaMove;
	}
	
//	public TacticsProvider getGetaReader()
//	{
//		if (reader==null)
//		{
//			reader = new TacticsProvider()
//				{
//					private GameEventListener eventListener;
//					private boolean active;
//					
//					public String getName()
//					{
//						return "Loose-ladder";
//					}
//
//					public void read(int x, int y, byte startColor)
//					{
//						Geta.this.addGameEventListener(eventListener);
//						sendTextEvent("Read geta at "+x+","+y+"\n");
//						int result = catchGeta(GoArray.toXY(x,y));
//						sendTextEvent("Geta result="+TACTICS[result]+"\n");
//						Geta.this.removeGameEventListener(eventListener);						
//					}
//
//					public void addGameEventListener(GameEventListener listener)
//					{
//						eventListener = listener;
//					}
//
//					public void removeGameEventListener(GameEventListener listener)
//					{
//						if (eventListener==listener)
//							eventListener = null;
//					}
//
//					public boolean isActive()
//					{
//						return active;
//					}
//
//					public void setActive(boolean flag)
//					{
//						active = flag;
//					}
//					
//					public String toString()
//					{
//						return (isActive()?"+ ":"- ") + getName();
//					}
//				};
//		}
//		return reader;
//	}
}
