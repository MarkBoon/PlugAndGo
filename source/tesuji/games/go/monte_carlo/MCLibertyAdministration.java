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

package tesuji.games.go.monte_carlo;

import tesuji.games.go.common.GoMove;

import tesuji.games.go.util.BoardMarker;
import tesuji.games.go.util.FourCursor;
import tesuji.games.go.util.GoArray;
import tesuji.games.go.util.SGFUtil;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.util.GoArray.*;
import static tesuji.games.go.common.GoConstant.*;

/**
 * 
 */
public class MCLibertyAdministration
	extends AbstractMonteCarloAdministration
{		
	private BoardMarker _boardMarker = new BoardMarker();
	
	public MCLibertyAdministration()
	{
		super();
	}
	
	protected MCLibertyAdministration(int boardSize)
	{
		super();
		_boardSize = boardSize;
		initBoardModel(getBoardSize());		
	}

	@Override
	public boolean isLegal(int xy)
	{
		if (xy<0) // Temporary hack
			return false;

		byte[] board = _boardModel.getSingleArray();
		if (board[xy]!=EMPTY)
			return false; // Occupied.

		if (_neighbours[xy]!=4)
			return true;
		if (xy==_koPoint && _otherNeighbours[xy]==4)
			return false;

		for (int n=0; n<4; n++)
		{
			int next = FourCursor.getNeighbour(xy, n);
			int liberties = _liberties[_chain[next]];
			byte nextBoardValue = board[next];
			if (nextBoardValue==_oppositeColor)
			{
				if (liberties==1)
					return true;
			}
			else if (nextBoardValue==_colorToPlay)
			{
				if (liberties>1)
					return true;
			}
		}
		return false;
	}

	@Override
	public void play(int xy)
	{
		if (xy!=PASS)
		{
			boolean extended = false;
			boolean merged = false;
			byte[] board = _boardModel.getSingleArray();
			
			assert _boardModel.get(xy) == EMPTY : SGFUtil.createSGF(getMoveStack());
			
			_chain[xy] = xy;
			_chainNext[xy] = xy;
			_stoneAge[xy] = _moveStack.getSize();
			
			int left = left(xy);
			int right = right(xy);
			int above = above(xy);
			int below = below(xy);
			
			int leftChain = _chain[left];
			int rightChain = _chain[right];
			int aboveChain = _chain[above];
			int belowChain = _chain[below];
			
			if (board[left]==_oppositeColor)
				_liberties[leftChain]--;
			if (board[right]==_oppositeColor && leftChain!=rightChain)
				_liberties[rightChain]--;
			if (board[above]==_oppositeColor && leftChain!=aboveChain && rightChain!=aboveChain)
				_liberties[aboveChain]--;
			if (board[below]==_oppositeColor && leftChain!=belowChain && rightChain!=belowChain && aboveChain!=belowChain)
				_liberties[belowChain]--;
			
			addStone(xy);

			for (int n=0; n<4; n++)
			{
				int next = FourCursor.getNeighbour(xy, n);
				if (board[next]==_colorToPlay)
				{
					if (!extended)
					{
						extended = true;
						_chain[xy] = _chain[next];
						_chainNext[xy] = _chainNext[next];
						_chainNext[next] = xy;
					}
					else if (_chain[next]!=_chain[xy])
					{
						merged = true;
						int mergeLocation = next;
						int chain = _chain[xy];
						do
						{
							_chain[mergeLocation] = chain;
							mergeLocation = _chainNext[mergeLocation];
						}
						while (mergeLocation!=next);
						int temp = _chainNext[xy];
						_chainNext[xy] = _chainNext[next];
						_chainNext[next] = temp;
					}
				}
			}

			if (merged) // No way but the expensive way.
				_liberties[_chain[xy]] = getLiberties(xy);
			else if (extended)
			{
				// When adding a stone to an existing chain, just take care of the shared liberties.
				int shared = getSharedLiberties(xy, _chain[xy]);
				_liberties[_chain[xy]] += (3 - _neighbours[xy]) - shared;
			}
			else // For a single stone we get the liberties cheapo.
				_liberties[_chain[xy]] = 4 - _neighbours[xy];
			
			for (int n=0; n<4; n++)
			{
				int next = FourCursor.getNeighbour(xy, n);
				byte nextColor = board[next];
				if (nextColor==_oppositeColor)
				{
					int liberties = _liberties[_chain[next]];
					if (liberties==0)
					{
						if (_ownNeighbours[next]==4 && _otherNeighbours[xy]==4)
							_koPoint = next;
						removeCapturedChain(next);
					}
				}
			}
			
			assert _liberties[_chain[xy]]==getLiberties(xy) :
				"Computed liberties incorrect at "+GoArray.getX(xy)+","+GoArray.getY(xy)+"\nGot "+_liberties[_chain[xy]]+" but should be "+getLiberties(xy)+"\n\n"+toString();
		}
		
		_oppositeColor = _colorToPlay;
		_colorToPlay = opposite(_colorToPlay);
		
		setNeighbourArrays();
		
		assert isLibertiesConsistent() : toString();
	}
	
	@Override
	public MonteCarloAdministration<GoMove> createClone()
	{
		MCLibertyAdministration clone = new MCLibertyAdministration(getBoardSize());
		clone.copyDataFrom(this);
		
		return clone;
	}
	
	private int getLiberties(int xy)
	{
		assert xy!=0 : "Cannot get liberties for pass";

		int nrLiberties = 0;
		_boardMarker.getNewMarker();
		byte[] board = _boardModel.getSingleArray();
		
		int stoneXY = xy;
		do
		{
			assert stoneXY!=0 : "Coordinate 0 cannot be part of a chain";

			if (_neighbours[stoneXY]!=4)
			{
				int left = left(stoneXY);
				int right = right(stoneXY);
				int above = above(stoneXY);
				int below = below(stoneXY);
				if (board[left]==EMPTY && _boardMarker.notSet(left))
				{
					_boardMarker.set(left);
					nrLiberties++;
				}
				if (board[right]==EMPTY && _boardMarker.notSet(right))
				{
					_boardMarker.set(right);
					nrLiberties++;
				}
				if (board[above]==EMPTY && _boardMarker.notSet(above))
				{
					_boardMarker.set(above);
					nrLiberties++;
				}
				if (board[below]==EMPTY && _boardMarker.notSet(below))
				{
					_boardMarker.set(below);
					nrLiberties++;
				}
			}
			
			stoneXY = _chainNext[stoneXY];
		}
		while (stoneXY!=xy);

		return nrLiberties;
	}

	private void removeCapturedChain(int xy)
	{
		assert !hasLiberty(xy) : SGFUtil.createSGF(getMoveStack());

		byte[] board = _boardModel.getSingleArray();
		int nrStones = 0;
		int captive = xy;
		do
		{
			assert _boardModel.get(captive)==_oppositeColor : SGFUtil.createSGF(getMoveStack());
			
			_chain[captive] = 0;
			nrStones++;
			
			int left = left(captive);
			int right = right(captive);
			int above = above(captive);
			int below = below(captive);
			int leftChain = _chain[left];
			int rightChain = _chain[right];
			int aboveChain = _chain[above];
			int belowChain = _chain[below];
			
			if (board[left]==_colorToPlay)
				_liberties[leftChain]++;
			if (board[right]==_colorToPlay && leftChain!=rightChain)
				_liberties[rightChain]++;
			if (board[above]==_colorToPlay && leftChain!=aboveChain && rightChain!=aboveChain)
				_liberties[aboveChain]++;
			if (board[below]==_colorToPlay && leftChain!=belowChain && rightChain!=belowChain && aboveChain!=belowChain)
				_liberties[belowChain]++;

			removeStone(captive);
			
			captive = _chainNext[captive];
		}
		while (captive!=xy);		
	}
	
	private int getSharedLiberties(int xy, int chain)
	{
		int shared = 0;
		int left = left(xy);
		int right = right(xy);
		int above = above(xy);
		int below = below(xy);
		byte[] board = _boardModel.getSingleArray();
		
		if (board[left]==EMPTY &&
						(_chain[left(left)]==chain || _chain[left(above)]==chain || _chain[left(below)]==chain))
			shared++;
		if (board[right]==EMPTY &&
						(_chain[right(right)]==chain || _chain[right(above)]==chain || _chain[right(below)]==chain))
			shared++;
		if (board[above]==EMPTY &&
						(_chain[above(above)]==chain || _chain[above(left)]==chain || _chain[above(right)]==chain))
			shared++;
		if (board[below]==EMPTY &&
						(_chain[below(below)]==chain || _chain[below(left)]==chain || _chain[below(right)]==chain))
			shared++;
		return shared;
	}
	
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.go.monte_carlo.AbstractMonteCarloAdministration#isConsistent()
	 */
    @Override
	public boolean isConsistent()
    {
    	if (!super.isConsistent())
    		return false;
    	return isLibertiesConsistent();
    }

    /**
     * This is for verification purposes.
     * 
     * @return
     */
    private boolean isLibertiesConsistent()
    {
    	for (int i=FIRST; i<=LAST; i++)
    	{
    		if (_boardModel.get(i)==BLACK || _boardModel.get(i)==WHITE)
    		{
    			assert _liberties[_chain[i]]==getLiberties(i) : "Inconsistent liberties at "+getX(i)+","+getY(i)+"\n"+
    				"Countedliberties="+getLiberties(i)+" Recorded liberties="+_liberties[_chain[i]]+"\n"+
    				getBoardModel().toString();
    		}
    	}
    	
    	return true;
    }
}
